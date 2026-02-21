package me.catand.cooptetris.tetris;

import lombok.Getter;
import lombok.Setter;
import me.catand.cooptetris.network.LocalServerManager;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.shared.message.CoopGameStateMessage;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.message.PlayerScoresMessage;

import java.util.ArrayList;
import java.util.List;

public class GameStateManager implements NetworkManager.NetworkListener {
    private final me.catand.cooptetris.shared.tetris.GameStateManager sharedManager;
    private NetworkManager networkManager;
    @Setter
    private LocalServerManager localServerManager;
    private boolean isLocalServerStarted;
    private boolean isSinglePlayerMode;

    // PVP模式玩家分数信息
    private List<PlayerScoresMessage.PlayerScore> playerScores;
    private PlayerScoresListener playerScoresListener;

    // 合作模式玩家名字列表
    @Getter
    private List<String> playerNames;
    // 合作模式玩家颜色列表
    @Getter
    private List<Integer> playerColors;
    @Getter
    private GameStartMessage lastGameStartMessage;

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

    public void startCoopMode(int playerCount, int playerIndex, long seed) {
        sharedManager.startCoopMode(playerCount, playerIndex, seed);
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
        // 保存游戏开始消息（包含玩家名字）
        this.lastGameStartMessage = message;
        // 保存玩家名字列表
        if (message.getPlayerNames() != null) {
            this.playerNames = new ArrayList<>(message.getPlayerNames());
        } else {
            this.playerNames = new ArrayList<>();
        }
        // 保存玩家颜色列表（按槽位索引）
        if (message.getPlayerColors() != null) {
            this.playerColors = new ArrayList<>(message.getPlayerColors());
        } else {
            // 如果没有颜色列表，默认使用槽位索引作为颜色
            this.playerColors = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                this.playerColors.add(i);
            }
        }
        // 根据游戏模式启动不同的游戏
        if (message.getGameMode() == me.catand.cooptetris.shared.tetris.GameMode.COOP) {
            // 合作模式
            startCoopMode(message.getPlayerCount(), message.getYourIndex(), message.getSeed());
        } else {
            // PVP模式
            startMultiplayer(message.getPlayerCount(), message.getYourIndex(), message.getSeed());
        }
    }

    @Override
    public void onGameStateUpdate(GameStateMessage message) {
        // PVP模式下，根据playerIndex判断是更新自己还是对手的游戏状态
        if (sharedManager.isMultiplayer() && message.getPlayerIndex() != sharedManager.getPlayerIndex()) {
            // 更新对手的游戏状态
            sharedManager.updateRemoteGameLogic(message.getPlayerIndex(), message);
        } else {
            // 更新自己的游戏状态
            sharedManager.updateGameLogic(message);
        }
    }

    @Override
    public void onPlayerScoresUpdate(PlayerScoresMessage message) {
        this.playerScores = message.getPlayerScores();
        if (playerScoresListener != null) {
            playerScoresListener.onPlayerScoresUpdated(playerScores, message.getYourIndex());
        }
    }

    @Override
    public void onCoopGameStateUpdate(CoopGameStateMessage message) {
        // 合作模式游戏状态更新
        // 颜色直接绑定到槽位索引，无需颜色映射
        sharedManager.updateCoopGameLogic(message);
    }

    public List<PlayerScoresMessage.PlayerScore> getPlayerScores() {
        return playerScores;
    }

    public void setPlayerScoresListener(PlayerScoresListener listener) {
        this.playerScoresListener = listener;
    }

    /**
     * PVP玩家分数更新监听器接口
     */
    public interface PlayerScoresListener {
        void onPlayerScoresUpdated(List<PlayerScoresMessage.PlayerScore> scores, int yourIndex);
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
