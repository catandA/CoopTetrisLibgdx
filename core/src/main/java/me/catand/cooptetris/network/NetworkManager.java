package me.catand.cooptetris.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import me.catand.cooptetris.shared.message.ConnectMessage;
import me.catand.cooptetris.shared.message.CountdownMessage;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.message.NetworkMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.message.PlayerScoresMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.shared.tetris.GameMode;
import me.catand.cooptetris.util.LanguageManager;

public class NetworkManager {
    private Client client;
    @Getter
    private String clientId;
    @Getter
    private String playerName;
    @Getter
    private boolean connected;
    private final List<NetworkListener> listeners;
    @Getter
    private ConnectionType currentConnectionType;

    public enum ConnectionType {
        NONE,         // 未连接
        LOCAL_SERVER,  // 连接到本地服务器
        EXTERNAL_SERVER  // 连接到外部服务器
    }

    public NetworkManager() {
        listeners = new ArrayList<>();
        connected = false;
        currentConnectionType = ConnectionType.NONE;
    }

    /**
     * 连接到指定服务器
     */
    public boolean connect(String host, int port, String playerName) {
        // 如果已经连接，先断开
        if (connected) {
            disconnect();
        }

        try {
            // 创建kryonet客户端
            client = new Client();

            // 注册消息类
            registerMessages();

            // 启动客户端
            client.start();

            // 连接到服务器
            client.connect(5000, host, port);

            this.playerName = playerName;
            connected = true;

            // 确定连接类型
            if (host.equals("localhost") || host.equals("127.0.0.1")) {
                currentConnectionType = ConnectionType.LOCAL_SERVER;
            } else {
                currentConnectionType = ConnectionType.EXTERNAL_SERVER;
            }

            // 添加监听器
            client.addListener(new Listener() {
                @Override
                public void received(Connection connection, Object object) {
                    if (object instanceof NetworkMessage) {
                        handleMessage((NetworkMessage) object);
                    }
                }

                @Override
                public void disconnected(Connection connection) {
                    handleDisconnected();
                }
            });

            // 发送连接消息
            ConnectMessage connectMessage = new ConnectMessage();
            connectMessage.setPlayerName(playerName);
            connectMessage.setLanguage(LanguageManager.getInstance().getCurrentLanguageCode());
            sendMessage(connectMessage);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            currentConnectionType = ConnectionType.NONE;
            return false;
        }
    }

    /**
     * 注册消息类
     */
    private void registerMessages() {
        Kryo kryo = client.getKryo();

        // 注册基本类型
        kryo.register(boolean.class);
        kryo.register(int.class);
        kryo.register(String.class);
        kryo.register(java.util.ArrayList.class);
        kryo.register(int[].class);
        kryo.register(int[][].class);

        // 注册消息类
        kryo.register(ConnectMessage.class);
        kryo.register(RoomMessage.class);
        kryo.register(RoomMessage.RoomAction.class);
        kryo.register(RoomMessage.RoomInfo.class);
        kryo.register(GameStartMessage.class);
        kryo.register(GameStateMessage.class);
        kryo.register(MoveMessage.class);
        kryo.register(MoveMessage.MoveType.class);
        kryo.register(NotificationMessage.class);
        kryo.register(NotificationMessage.NotificationType.class);
        kryo.register(PlayerScoresMessage.class);
        kryo.register(PlayerScoresMessage.PlayerScore.class);
        kryo.register(CountdownMessage.class);
        kryo.register(GameMode.class);
        kryo.register(long.class);
    }

    private void handleMessage(NetworkMessage message) {
        switch (message.getType()) {
            case "connect":
                handleConnectMessage((ConnectMessage) message);
                break;
            case "room":
                handleRoomMessage((RoomMessage) message);
                break;
            case "gameStart":
                handleGameStartMessage((GameStartMessage) message);
                break;
            case "gameState":
                handleGameStateMessage((GameStateMessage) message);
                break;
            case "notification":
                handleNotificationMessage((NotificationMessage) message);
                break;
            case "playerScores":
                handlePlayerScoresMessage((PlayerScoresMessage) message);
                break;
            case "countdown":
                handleCountdownMessage((CountdownMessage) message);
                break;
        }
    }

    private void handleConnectMessage(ConnectMessage message) {
        if (message.isSuccess()) {
            clientId = message.getClientId();

            // 根据连接类型决定下一步操作
            if (currentConnectionType == ConnectionType.EXTERNAL_SERVER) {
                // 连接到专有服务器，显示房间列表
                listRooms();
            }
            // 连接到本地服务器时，不再自动加入默认房间
            // 由OnlineMenuState控制房间的创建和加入
        }

        // 确保在主线程中调用监听器方法
        final ConnectMessage finalMessage = message;
        Gdx.app.postRunnable(() -> {
            // 使用监听器列表的副本进行遍历，避免ConcurrentModificationException
            for (NetworkListener listener : new ArrayList<>(listeners)) {
                listener.onConnectResponse(finalMessage.isSuccess(), finalMessage.getMessage(), finalMessage.getClientId());
            }
        });
    }

    private void handleRoomMessage(RoomMessage message) {
        // 确保在主线程中调用监听器方法
        final RoomMessage finalMessage = message;
        Gdx.app.postRunnable(() -> {
            // 使用监听器列表的副本进行遍历，避免ConcurrentModificationException
            for (NetworkListener listener : new ArrayList<>(listeners)) {
                listener.onRoomResponse(finalMessage);
            }
        });
    }

    private void handleGameStartMessage(GameStartMessage message) {
        // 确保在主线程中调用监听器方法
        final GameStartMessage finalMessage = message;
        Gdx.app.postRunnable(() -> {
            // 使用监听器列表的副本进行遍历，避免ConcurrentModificationException
            for (NetworkListener listener : new ArrayList<>(listeners)) {
                listener.onGameStart(finalMessage);
            }
        });
    }

    private void handleGameStateMessage(GameStateMessage message) {
        // 确保在主线程中调用监听器方法
        final GameStateMessage finalMessage = message;
        Gdx.app.postRunnable(() -> {
            // 使用监听器列表的副本进行遍历，避免ConcurrentModificationException
            for (NetworkListener listener : new ArrayList<>(listeners)) {
                listener.onGameStateUpdate(finalMessage);
            }
        });
    }

    private void handleNotificationMessage(NotificationMessage message) {
        // 确保在主线程中调用监听器方法
        final NotificationMessage finalMessage = message;
        Gdx.app.postRunnable(() -> {
            // 使用监听器列表的副本进行遍历，避免ConcurrentModificationException
            for (NetworkListener listener : new ArrayList<>(listeners)) {
                listener.onNotification(finalMessage);
            }
        });
    }

    private void handlePlayerScoresMessage(PlayerScoresMessage message) {
        // 确保在主线程中调用监听器方法
        final PlayerScoresMessage finalMessage = message;
        Gdx.app.postRunnable(() -> {
            // 使用监听器列表的副本进行遍历，避免ConcurrentModificationException
            for (NetworkListener listener : new ArrayList<>(listeners)) {
                listener.onPlayerScoresUpdate(finalMessage);
            }
        });
    }

    private void handleCountdownMessage(CountdownMessage message) {
        // 确保在主线程中调用监听器方法
        final CountdownMessage finalMessage = message;
        Gdx.app.postRunnable(() -> {
            // 使用监听器列表的副本进行遍历，避免ConcurrentModificationException
            for (NetworkListener listener : new ArrayList<>(listeners)) {
                listener.onCountdownUpdate(finalMessage);
            }
        });
    }

    private void handleDisconnected() {
        if (connected) {
            connected = false;

            // 确保在主线程中调用监听器方法
            Gdx.app.postRunnable(() -> {
                // 使用监听器列表的副本进行遍历，避免ConcurrentModificationException
                for (NetworkListener listener : new ArrayList<>(listeners)) {
                    listener.onDisconnected();
                }
            });
        }
    }

    public void sendMessage(NetworkMessage message) {
        if (connected && client != null) {
            try {
                client.sendTCP(message);
            } catch (Exception e) {
                e.printStackTrace();
                disconnect();
            }
        }
    }

    public void sendMove(MoveMessage.MoveType moveType) {
        MoveMessage message = new MoveMessage(moveType);
        sendMessage(message);
    }

    public void createRoom(String roomName) {
        RoomMessage message = new RoomMessage(RoomMessage.RoomAction.CREATE);
        message.setRoomName(roomName);
        sendMessage(message);
    }

    public void joinRoom(String roomId) {
        RoomMessage message = new RoomMessage(RoomMessage.RoomAction.JOIN);
        message.setRoomId(roomId);
        sendMessage(message);
    }

    public void leaveRoom() {
        RoomMessage message = new RoomMessage(RoomMessage.RoomAction.LEAVE);
        sendMessage(message);
    }

    public void listRooms() {
        RoomMessage message = new RoomMessage(RoomMessage.RoomAction.LIST);
        sendMessage(message);
    }

    public void startGame() {
        RoomMessage message = new RoomMessage(RoomMessage.RoomAction.START);
        sendMessage(message);
    }

    public void kickPlayer(String playerName) {
        RoomMessage message = new RoomMessage(RoomMessage.RoomAction.KICK);
        message.setTargetPlayer(playerName);
        sendMessage(message);
    }

    public void sendChatMessage(String message) {
        RoomMessage chatMessage = new RoomMessage(RoomMessage.RoomAction.CHAT);
        chatMessage.setChatMessage(message);
        sendMessage(chatMessage);
    }

    public void setGameMode(GameMode gameMode) {
        RoomMessage message = new RoomMessage(RoomMessage.RoomAction.SET_GAME_MODE);
        message.setGameMode(gameMode);
        sendMessage(message);
    }

    public void disconnect() {
        if (connected) {
            connected = false;

            try {
                if (client != null) {
                    client.close();
                    client = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 确保在主线程中调用监听器方法
            Gdx.app.postRunnable(() -> {
                // 使用监听器列表的副本进行遍历，避免ConcurrentModificationException
                for (NetworkListener listener : new ArrayList<>(listeners)) {
                    listener.onDisconnected();
                }
            });
        }
    }

    public void addListener(NetworkListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }

    public interface NetworkListener {
        void onConnectResponse(boolean success, String message, String clientId);

        void onRoomResponse(RoomMessage message);

        void onGameStart(GameStartMessage message);

        void onGameStateUpdate(GameStateMessage message);

        void onDisconnected();

        default void onNotification(NotificationMessage message) {
            // 默认空实现，让实现类可以选择性覆盖
        }

        default void onPlayerScoresUpdate(PlayerScoresMessage message) {
            // 默认空实现，用于PVP模式同步玩家分数
        }

        default void onCountdownUpdate(CountdownMessage message) {
            // 默认空实现，用于游戏开始倒计时
        }
    }
}
