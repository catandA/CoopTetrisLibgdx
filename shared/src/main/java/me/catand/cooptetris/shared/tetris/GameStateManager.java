package me.catand.cooptetris.shared.tetris;

import me.catand.cooptetris.shared.message.CoopGameStateMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.MoveMessage;

public class GameStateManager {
    private final GameLogic localGameLogic;
    private GameLogic[] remoteGameLogics;
    private boolean isMultiplayer;
    private int playerIndex;
    private int playerCount;
    private float dropTimer;
    private static final float DROP_INTERVAL = 1.0f; // 默认下落间隔为1秒

    // 合作模式相关
    private boolean isCoopMode;
    private CoopGameLogic coopGameLogic;

    public GameStateManager() {
        localGameLogic = new GameLogic();
        isMultiplayer = false;
    }

    public void startSinglePlayer() {
        isMultiplayer = false;
        isCoopMode = false;
        localGameLogic.reset();
    }

    public void startCoopMode(int playerCount, int playerIndex, long seed) {
        isMultiplayer = true;
        isCoopMode = true;
        this.playerCount = playerCount;
        this.playerIndex = playerIndex;
        coopGameLogic = new CoopGameLogic();
        coopGameLogic.reset(seed, playerCount);
    }

    public void startMultiplayer(int playerCount, int playerIndex) {
        startMultiplayer(playerCount, playerIndex, 0);
    }

    public void startMultiplayer(int playerCount, int playerIndex, long seed) {
        isMultiplayer = true;
        this.playerCount = playerCount;
        this.playerIndex = playerIndex;
        remoteGameLogics = new GameLogic[playerCount];
        for (int i = 0; i < playerCount; i++) {
            remoteGameLogics[i] = new GameLogic();
        }
        // 使用种子初始化游戏逻辑，确保所有客户端生成相同的方块序列
        localGameLogic.reset(seed);
    }

    public void update(float delta) {
        // 只有在单机模式下执行本地自动下落逻辑
        // 多人模式下，由服务器控制方块下落，客户端只接收状态更新
        if (!isMultiplayer) {
            dropTimer += delta;
            if (dropTimer >= DROP_INTERVAL) {
                localGameLogic.moveDown();
                dropTimer = 0f;
            }
        }
    }

    public boolean handleInput(MoveMessage.MoveType moveType) {
        if (!isMultiplayer) {
            switch (moveType) {
                case LEFT:
                    return localGameLogic.moveLeft();
                case RIGHT:
                    return localGameLogic.moveRight();
                case DOWN:
                    return localGameLogic.moveDown();
                case DROP:
                    localGameLogic.dropPiece();
                    return true;
                case ROTATE_CLOCKWISE:
                    return localGameLogic.rotateClockwise();
                default:
                    return false;
            }
        }
        return false;
    }

    public void updateGameLogic(GameStateMessage message) {
        if (isMultiplayer && !isCoopMode && remoteGameLogics != null) {
            // 更新本地游戏状态
            updateGameLogic(localGameLogic, message);
        }
    }

    /**
     * 更新指定远程玩家的游戏逻辑（用于PVP模式显示对手游戏板）
     */
    public void updateRemoteGameLogic(int playerIndex, GameStateMessage message) {
        if (isMultiplayer && !isCoopMode && remoteGameLogics != null && playerIndex >= 0 && playerIndex < remoteGameLogics.length) {
            updateGameLogic(remoteGameLogics[playerIndex], message);
        }
    }

    private void updateGameLogic(GameLogic gameLogic, GameStateMessage message) {
        gameLogic.updateFromMessage(
            message.getBoard(),
            message.getCurrentPiece(),
            message.getCurrentPieceX(),
            message.getCurrentPieceY(),
            message.getCurrentPieceRotation(),
            message.getNextPiece(),
            message.getScore(),
            message.getLevel(),
            message.getLines()
        );
    }

    /**
     * 更新合作模式游戏逻辑
     */
    public void updateCoopGameLogic(CoopGameStateMessage message) {
        if (!isCoopMode || coopGameLogic == null) return;

        // 更新游戏板
        int[][] board = message.getBoard();
        int[][] boardColor = message.getBoardColor();
        for (int y = 0; y < CoopGameLogic.BOARD_HEIGHT; y++) {
            System.arraycopy(board[y], 0, coopGameLogic.getBoard()[y], 0, CoopGameLogic.BOARD_WIDTH);
            System.arraycopy(boardColor[y], 0, coopGameLogic.getBoardColor()[y], 0, CoopGameLogic.BOARD_WIDTH);
        }

        // 更新游戏状态
        coopGameLogic.setScore(message.getScore());
        coopGameLogic.setLevel(message.getLevel());
        coopGameLogic.setLines(message.getLines());
        coopGameLogic.setGameOver(message.isGameOver());

        // 更新每个玩家的物块
        CoopGameStateMessage.PlayerPieceState[] playerPieces = message.getPlayerPieces();
        if (playerPieces != null) {
            for (int i = 0; i < playerPieces.length && i < CoopGameLogic.MAX_PLAYERS; i++) {
                CoopGameStateMessage.PlayerPieceState pieceState = playerPieces[i];
                // 使用消息中的玩家索引来获取正确的物块
                int playerIndex = pieceState.getPlayerIndex();
                if (playerIndex >= 0 && playerIndex < CoopGameLogic.MAX_PLAYERS) {
                    CoopGameLogic.PlayerPiece piece = coopGameLogic.getPlayerPiece(playerIndex);
                    piece.setPieceType(pieceState.getPieceType());
                    piece.setX(pieceState.getX());
                    piece.setY(pieceState.getY());
                    piece.setRotation(pieceState.getRotation());
                    piece.setActive(pieceState.isActive());
                }
            }
        }
    }

    public GameLogic getLocalGameLogic() {
        return localGameLogic;
    }

    public GameLogic[] getRemoteGameLogics() {
        return remoteGameLogics;
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public boolean isCoopMode() {
        return isCoopMode;
    }

    public CoopGameLogic getCoopGameLogic() {
        return coopGameLogic;
    }
}
