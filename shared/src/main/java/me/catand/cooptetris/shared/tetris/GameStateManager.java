package me.catand.cooptetris.shared.tetris;

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

    public GameStateManager() {
        localGameLogic = new GameLogic();
        isMultiplayer = false;
    }

    public void startSinglePlayer() {
        isMultiplayer = false;
        localGameLogic.reset();
    }

    public void startMultiplayer(int playerCount, int playerIndex) {
        isMultiplayer = true;
        this.playerCount = playerCount;
        this.playerIndex = playerIndex;
        remoteGameLogics = new GameLogic[playerCount];
        for (int i = 0; i < playerCount; i++) {
            remoteGameLogics[i] = new GameLogic();
        }
        localGameLogic.reset();
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
        if (isMultiplayer && remoteGameLogics != null) {
            // 更新本地游戏状态
            updateGameLogic(localGameLogic, message);
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
}
