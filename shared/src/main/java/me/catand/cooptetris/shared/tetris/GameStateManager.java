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
        // 无论是单机模式还是多人模式，都执行自动下落逻辑
        dropTimer += delta;
        if (dropTimer >= DROP_INTERVAL) {
            localGameLogic.moveDown();
            dropTimer = 0f;
        }
    }

    public void handleInput(MoveMessage.MoveType moveType) {
        if (!isMultiplayer) {
            switch (moveType) {
                case LEFT:
                    localGameLogic.moveLeft();
                    break;
                case RIGHT:
                    localGameLogic.moveRight();
                    break;
                case DOWN:
                    localGameLogic.moveDown();
                    break;
                case DROP:
                    localGameLogic.dropPiece();
                    break;
                case ROTATE_CLOCKWISE:
                    localGameLogic.rotateClockwise();
                    break;
            }
        }
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
