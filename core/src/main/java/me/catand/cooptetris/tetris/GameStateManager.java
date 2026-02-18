package me.catand.cooptetris.tetris;

import lombok.Setter;
import me.catand.cooptetris.network.LocalServerManager;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.MoveMessage;

public class GameStateManager implements NetworkManager.NetworkListener {
    private final me.catand.cooptetris.shared.tetris.GameStateManager sharedManager;
    private NetworkManager networkManager;
    @Setter
    private LocalServerManager localServerManager;
    private boolean isLocalServerStarted;
    private boolean isSinglePlayerMode;

    public GameStateManager() {
        sharedManager = new me.catand.cooptetris.shared.tetris.GameStateManager();
        this.isLocalServerStarted = false;
        this.isSinglePlayerMode = false;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
        if (networkManager != null) {
            networkManager.addListener(this);
        }
    }

    public void startSinglePlayer() {
        // 设置为单人模式
        isSinglePlayerMode = true;
        // 启动本地服务器（如果尚未启动）
        if (!isLocalServerStarted && localServerManager != null && !localServerManager.isRunning()) {
            int actualPort = localServerManager.startServer(56148);
            if (actualPort > 0) {
                System.out.println("Local server started for single player mode on port: " + actualPort);
                isLocalServerStarted = true;
                // 连接到本地服务器
                if (networkManager != null) {
                    networkManager.connect("localhost", actualPort, "SinglePlayer");
                }
            } else {
                System.err.println("Failed to start local server for single player mode");
            }
        }
        sharedManager.startSinglePlayer();
    }

    public void startMultiplayer(int playerCount, int playerIndex) {
        sharedManager.startMultiplayer(playerCount, playerIndex);
    }

    public void startMultiplayer(int playerCount, int playerIndex, long seed) {
        sharedManager.startMultiplayer(playerCount, playerIndex, seed);
    }

    public void update(float delta) {
        sharedManager.update(delta);
    }

    public boolean handleInput(MoveMessage.MoveType moveType) {
        if (sharedManager.isMultiplayer() && networkManager != null && networkManager.isConnected()) {
            networkManager.sendMove(moveType);
            return false;
        } else {
            return sharedManager.handleInput(moveType);
        }
    }

    @Override
    public void onConnectResponse(boolean success, String message, String clientId) {
        // 处理连接响应
        System.out.println("Connection response: " + message + " (" + (success ? "success" : "failure") + ")");

        // 只有在单人模式下，连接到本地服务器成功时才自动开始游戏
        if (success && networkManager != null && networkManager.getCurrentConnectionType() == NetworkManager.ConnectionType.LOCAL_SERVER && isSinglePlayerMode) {
            // 延迟一秒，确保服务器完全启动
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    // 发送开始游戏请求
                    networkManager.startGame();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void onRoomResponse(me.catand.cooptetris.shared.message.RoomMessage message) {
    }

    @Override
    public void onGameStart(me.catand.cooptetris.shared.message.GameStartMessage message) {
        // 使用服务器发送的种子启动多人游戏，确保方块生成同步
        startMultiplayer(message.getPlayerCount(), message.getYourIndex(), message.getSeed());
    }

    @Override
    public void onGameStateUpdate(GameStateMessage message) {
        sharedManager.updateGameLogic(message);
    }

    @Override
    public void onDisconnected() {
        // 处理断开连接
        sharedManager.startSinglePlayer();
        // 重置本地服务器启动标志，以便下次可以重新启动
        isLocalServerStarted = false;
        // 重置单人模式标志
        isSinglePlayerMode = false;
    }

    public me.catand.cooptetris.shared.tetris.GameStateManager getSharedManager() {
        return sharedManager;
    }

    public boolean isMultiplayer() {
        return sharedManager.isMultiplayer();
    }

    public int getPlayerIndex() {
        return sharedManager.getPlayerIndex();
    }

    public int getPlayerCount() {
        return sharedManager.getPlayerCount();
    }

    public boolean isLocalServerStarted() {
        return isLocalServerStarted;
    }
}
