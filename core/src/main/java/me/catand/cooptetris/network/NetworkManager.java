package me.catand.cooptetris.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.ArrayList;
import java.util.List;

import me.catand.cooptetris.shared.message.ConnectMessage;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.message.NetworkMessage;
import me.catand.cooptetris.shared.message.RoomMessage;

public class NetworkManager {
    private Client client;
    private String clientId;
    private String playerName;
    private boolean connected;
    private final List<NetworkListener> listeners;
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
        client.getKryo().register(ConnectMessage.class);
        client.getKryo().register(RoomMessage.class);
        client.getKryo().register(RoomMessage.RoomAction.class);
        client.getKryo().register(RoomMessage.RoomInfo.class);
        client.getKryo().register(GameStartMessage.class);
        client.getKryo().register(GameStateMessage.class);
        client.getKryo().register(MoveMessage.class);
        client.getKryo().register(MoveMessage.MoveType.class);
        client.getKryo().register(java.util.ArrayList.class);
        client.getKryo().register(int[].class);
        client.getKryo().register(int[][].class);
    }

    /**
     * 连接到本地服务器
     */
    public boolean connectToLocalServer(int port, String playerName) {
        return connect("localhost", port, playerName);
    }

    /**
     * 连接到外部服务器
     */
    public boolean connectToExternalServer(String host, int port, String playerName) {
        return connect(host, port, playerName);
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

    /**
     * 加入默认房间
     */
    private void joinDefaultRoom() {
        // 发送请求获取房间列表
        RoomMessage roomMessage = new RoomMessage(RoomMessage.RoomAction.LIST);
        sendMessage(roomMessage);
    }

    private void handleRoomMessage(RoomMessage message) {
        // 确保在主线程中调用监听器方法
        final RoomMessage finalMessage = message;
        Gdx.app.postRunnable(() -> {
            // 使用监听器列表的副本进行遍历，避免ConcurrentModificationException
            for (NetworkListener listener : new ArrayList<>(listeners)) {
                listener.onRoomResponse(finalMessage);
            }

            // 处理不同类型的房间消息
            switch (finalMessage.getAction()) {
                case LIST:
                    if (finalMessage.getRooms() != null) {
                        // 尝试获取当前UI状态
                        try {
                            // 通过反射获取UIManager的实例
                            Object uiManager = null;

                            // 尝试获取主应用实例
                            Class<?> mainClass = Class.forName("me.catand.cooptetris.Main");
                            java.lang.reflect.Field[] fields = mainClass.getDeclaredFields();

                            // 首先尝试获取Gdx.app实例，然后从中获取Main实例
                            try {
                                Class<?> applicationClass = Class.forName("com.badlogic.gdx.Application");
                                Class<?> gdxClass = Class.forName("com.badlogic.gdx.Gdx");
                                java.lang.reflect.Field appField = gdxClass.getDeclaredField("app");
                                appField.setAccessible(true);
                                Object appInstance = appField.get(null);

                                // 检查appInstance是否是Main的实例
                                if (mainClass.isInstance(appInstance)) {
                                    // 从Main实例中获取UIManager
                                    for (java.lang.reflect.Field field : fields) {
                                        if (field.getType().getName().equals("me.catand.cooptetris.ui.UIManager")) {
                                            field.setAccessible(true);
                                            uiManager = field.get(appInstance);
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // 如果获取Gdx.app失败，尝试其他方式
                                e.printStackTrace();
                            }

                            // 如果上述方式失败，尝试遍历所有字段，看是否有静态的Main实例
                            if (uiManager == null) {
                                for (java.lang.reflect.Field field : fields) {
                                    if (field.getType().getName().equals("me.catand.cooptetris.ui.UIManager")) {
                                        field.setAccessible(true);
                                        try {
                                            // 先尝试静态字段
                                            uiManager = field.get(null);
                                        } catch (Exception e) {
                                            // 如果是实例字段，暂时跳过
                                            // 因为我们没有Main类的实例
                                        }
                                        if (uiManager != null) {
                                            break;
                                        }
                                    }
                                }
                            }

                            if (uiManager != null) {
                                // 获取当前UI状态
                                java.lang.reflect.Method getCurrentStateMethod = uiManager.getClass().getMethod("getCurrentState");
                                Object currentState = getCurrentStateMethod.invoke(uiManager);

                                if (currentState != null && currentState.getClass().getName().equals("me.catand.cooptetris.ui.OnlineMenuState")) {
                                    // 调用updateRoomList方法更新房间列表
                                    java.lang.reflect.Method updateRoomListMethod = currentState.getClass().getMethod("updateRoomList", java.util.List.class);
                                    updateRoomListMethod.invoke(currentState, finalMessage.getRooms());
                                }
                            }
                        } catch (Exception e) {
                            // 反射失败，忽略
                            e.printStackTrace();
                        }
                    }
                    break;
                case STATUS:
                    if (finalMessage.getPlayers() != null) {
                        // 尝试获取当前UI状态
                        try {
                            // 通过反射获取UIManager的实例
                            Object uiManager = null;

                            // 尝试获取主应用实例
                            Class<?> mainClass = Class.forName("me.catand.cooptetris.Main");
                            java.lang.reflect.Field[] fields = mainClass.getDeclaredFields();

                            // 首先尝试获取Gdx.app实例，然后从中获取Main实例
                            try {
                                Class<?> applicationClass = Class.forName("com.badlogic.gdx.Application");
                                Class<?> gdxClass = Class.forName("com.badlogic.gdx.Gdx");
                                java.lang.reflect.Field appField = gdxClass.getDeclaredField("app");
                                appField.setAccessible(true);
                                Object appInstance = appField.get(null);

                                // 检查appInstance是否是Main的实例
                                if (mainClass.isInstance(appInstance)) {
                                    // 从Main实例中获取UIManager
                                    for (java.lang.reflect.Field field : fields) {
                                        if (field.getType().getName().equals("me.catand.cooptetris.ui.UIManager")) {
                                            field.setAccessible(true);
                                            uiManager = field.get(appInstance);
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // 如果获取Gdx.app失败，尝试其他方式
                                e.printStackTrace();
                            }

                            // 如果上述方式失败，尝试遍历所有字段，看是否有静态的Main实例
                            if (uiManager == null) {
                                for (java.lang.reflect.Field field : fields) {
                                    if (field.getType().getName().equals("me.catand.cooptetris.ui.UIManager")) {
                                        field.setAccessible(true);
                                        try {
                                            // 先尝试静态字段
                                            uiManager = field.get(null);
                                        } catch (Exception e) {
                                            // 如果是实例字段，暂时跳过
                                            // 因为我们没有Main类的实例
                                        }
                                        if (uiManager != null) {
                                            break;
                                        }
                                    }
                                }
                            }

                            if (uiManager != null) {
                                // 获取当前UI状态
                                java.lang.reflect.Method getCurrentStateMethod = uiManager.getClass().getMethod("getCurrentState");
                                Object currentState = getCurrentStateMethod.invoke(uiManager);

                                if (currentState != null && currentState.getClass().getName().equals("me.catand.cooptetris.ui.RoomLobbyState")) {
                                    // 调用updatePlayerList方法更新玩家列表
                                    java.lang.reflect.Method updatePlayerListMethod = currentState.getClass().getMethod("updatePlayerList", java.util.List.class);
                                    updatePlayerListMethod.invoke(currentState, finalMessage.getPlayers());

                                    // 设置房间信息
                                    java.lang.reflect.Method setRoomInfoMethod = currentState.getClass().getMethod("setRoomInfo", String.class, int.class, boolean.class);
                                    setRoomInfoMethod.invoke(currentState, finalMessage.getRoomName(), 4, false); // 暂时使用默认maxPlayers
                                }
                            }
                        } catch (Exception e) {
                            // 反射失败，忽略
                            e.printStackTrace();
                        }
                    }
                    break;
                case CHAT:
                    if (finalMessage.getMessage() != null) {
                        // 尝试获取当前UI状态
                        try {
                            // 通过反射获取UIManager的实例
                            Object uiManager = null;

                            // 尝试获取主应用实例
                            Class<?> mainClass = Class.forName("me.catand.cooptetris.Main");
                            java.lang.reflect.Field[] fields = mainClass.getDeclaredFields();

                            // 首先尝试获取Gdx.app实例，然后从中获取Main实例
                            try {
                                Class<?> applicationClass = Class.forName("com.badlogic.gdx.Application");
                                Class<?> gdxClass = Class.forName("com.badlogic.gdx.Gdx");
                                java.lang.reflect.Field appField = gdxClass.getDeclaredField("app");
                                appField.setAccessible(true);
                                Object appInstance = appField.get(null);

                                // 检查appInstance是否是Main的实例
                                if (mainClass.isInstance(appInstance)) {
                                    // 从Main实例中获取UIManager
                                    for (java.lang.reflect.Field field : fields) {
                                        if (field.getType().getName().equals("me.catand.cooptetris.ui.UIManager")) {
                                            field.setAccessible(true);
                                            uiManager = field.get(appInstance);
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // 如果获取Gdx.app失败，尝试其他方式
                                e.printStackTrace();
                            }

                            // 如果上述方式失败，尝试遍历所有字段，看是否有静态的Main实例
                            if (uiManager == null) {
                                for (java.lang.reflect.Field field : fields) {
                                    if (field.getType().getName().equals("me.catand.cooptetris.ui.UIManager")) {
                                        field.setAccessible(true);
                                        try {
                                            // 先尝试静态字段
                                            uiManager = field.get(null);
                                        } catch (Exception e) {
                                            // 如果是实例字段，暂时跳过
                                            // 因为我们没有Main类的实例
                                        }
                                        if (uiManager != null) {
                                            break;
                                        }
                                    }
                                }
                            }

                            if (uiManager != null) {
                                // 获取当前UI状态
                                java.lang.reflect.Method getCurrentStateMethod = uiManager.getClass().getMethod("getCurrentState");
                                Object currentState = getCurrentStateMethod.invoke(uiManager);

                                if (currentState != null && currentState.getClass().getName().equals("me.catand.cooptetris.ui.RoomLobbyState")) {
                                    // 调用addChatMessage方法添加聊天消息
                                    java.lang.reflect.Method addChatMessageMethod = currentState.getClass().getMethod("addChatMessage", String.class);
                                    addChatMessageMethod.invoke(currentState, finalMessage.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            // 反射失败，忽略
                            e.printStackTrace();
                        }
                    }
                    break;
                case KICK:
                    // 踢出消息已经通过监听器传递
                    break;
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

    public boolean isConnected() {
        return connected;
    }

    public String getClientId() {
        return clientId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public ConnectionType getCurrentConnectionType() {
        return currentConnectionType;
    }

    public interface NetworkListener {
        void onConnectResponse(boolean success, String message, String clientId);

        void onRoomResponse(RoomMessage message);

        void onGameStart(GameStartMessage message);

        void onGameStateUpdate(GameStateMessage message);

        void onDisconnected();
    }
}
