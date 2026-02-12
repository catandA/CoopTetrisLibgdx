package me.catand.cooptetris.shared.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.catand.cooptetris.shared.message.ConnectMessage;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.message.NetworkMessage;
import me.catand.cooptetris.shared.message.RoomMessage;

public class ServerManager {
    public enum ServerType {
        LOCAL_SERVER,  // 内置服务器
        DEDICATED_SERVER  // 专有服务器
    }

    private Server server;
    private final List<ClientConnection> clients;
    private final List<Room> rooms;
    private boolean running;
    private final ServerType serverType;
    private Room defaultRoom;

    public ServerManager(int port) {
        this(port, ServerType.DEDICATED_SERVER);
    }

    public ServerManager(int port, ServerType serverType) {
        clients = new ArrayList<>();
        rooms = new ArrayList<>();
        this.serverType = serverType;

        try {
            // 创建kryonet服务器
            server = new Server();
            
            // 注册消息类
            registerMessages();
            
            // 启动服务器
            server.start();
            server.bind(port);
            running = true;
            
            // 添加监听器
            server.addListener(new Listener() {
                @Override
                public void connected(Connection connection) {
                    // 创建新的客户端连接
                    ClientConnection client = new ClientConnection(connection, ServerManager.this);
                    clients.add(client);
                }

                @Override
                public void received(Connection connection, Object object) {
                    if (object instanceof NetworkMessage) {
                        // 找到对应的客户端连接
                        for (ClientConnection client : clients) {
                            if (client.getConnection() == connection) {
                                handleMessage(client, (NetworkMessage) object);
                                break;
                            }
                        }
                    }
                }

                @Override
                public void disconnected(Connection connection) {
                    // 找到对应的客户端连接并移除
                    for (int i = 0; i < clients.size(); i++) {
                        ClientConnection client = clients.get(i);
                        if (client.getConnection() == connection) {
                            // 从房间中移除
                            if (client.getCurrentRoom() != null) {
                                client.getCurrentRoom().removePlayer(client);
                            }
                            clients.remove(i);
                            break;
                        }
                    }
                }
            });
            
            // 启动游戏循环线程，处理方块自动下落
            startGameLoop();
            // 服务器启动时默认创建一个房间
            createDefaultRoom();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册消息类
     */
    private void registerMessages() {
        server.getKryo().register(ConnectMessage.class);
        server.getKryo().register(RoomMessage.class);
        server.getKryo().register(RoomMessage.RoomAction.class);
        server.getKryo().register(RoomMessage.RoomInfo.class);
        server.getKryo().register(GameStartMessage.class);
        server.getKryo().register(GameStateMessage.class);
        server.getKryo().register(MoveMessage.class);
        server.getKryo().register(MoveMessage.MoveType.class);
        server.getKryo().register(java.util.ArrayList.class);
        server.getKryo().register(int[].class);
        server.getKryo().register(int[][].class);
    }

    /**
     * 启动游戏循环线程，处理方块自动下落
     */
    private void startGameLoop() {
        new Thread(() -> {
            long lastTime = System.currentTimeMillis();
            final long DROP_INTERVAL = 1000; // 1秒

            while (running) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTime >= DROP_INTERVAL) {
                    // 使用副本遍历，避免ConcurrentModificationException
                    for (Room room : new ArrayList<>(rooms)) {
                        // 只有已开始游戏的房间需要处理自动下落
                        if (room.isStarted()) {
                            // 处理每个房间的游戏逻辑
                            room.updateGameState();
                        }
                    }
                    lastTime = currentTime;
                }

                try {
                    Thread.sleep(100); // 避免CPU占用过高
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 创建默认房间
     * 在专有服务器上，这个房间永远不会开始游戏，没人有房主权限，用户可以在此自由聊天即聊天室
     * 在内置服务器上，这个房间用作唯一房间进行游戏
     */
    public void createDefaultRoom() {
        // 创建默认房间（设置为默认聊天室，没有房主）
        defaultRoom = new Room("Lobby", 10, this, true);
        rooms.add(defaultRoom);
    }

    public void handleMessage(ClientConnection client, NetworkMessage message) {
        switch (message.getType()) {
            case "connect":
                handleConnectMessage(client, (ConnectMessage) message);
                break;
            case "room":
                handleRoomMessage(client, (RoomMessage) message);
                break;
            case "move":
                handleMoveMessage(client, (MoveMessage) message);
                break;
        }
    }

    private void handleConnectMessage(ClientConnection client, ConnectMessage message) {
        String playerName = message.getPlayerName();
        if (playerName != null && !playerName.isEmpty()) {
            client.setPlayerName(playerName);
            ConnectMessage response = new ConnectMessage();
            response.setSuccess(true);
            response.setClientId(client.getClientId());
            response.setMessage("Connected successfully");
            client.sendMessage(response);

            // 在内置服务器模式下，自动将客户端加入默认房间
            if (serverType == ServerType.LOCAL_SERVER && defaultRoom != null) {
                defaultRoom.addPlayer(client);
                // 发送加入房间的响应
                RoomMessage roomResponse = new RoomMessage(RoomMessage.RoomAction.JOIN);
                roomResponse.setSuccess(true);
                roomResponse.setRoomId(defaultRoom.getId());
                roomResponse.setMessage("Joined default room automatically");
                client.sendMessage(roomResponse);
            }
        } else {
            ConnectMessage response = new ConnectMessage();
            response.setSuccess(false);
            response.setMessage("Invalid player name");
            client.sendMessage(response);
            client.disconnect();
        }
    }

    private void handleRoomMessage(ClientConnection client, RoomMessage message) {
        switch (message.getAction()) {
            case CREATE:
                handleCreateRoom(client, message);
                break;
            case JOIN:
                handleJoinRoom(client, message);
                break;
            case LEAVE:
                handleLeaveRoom(client);
                break;
            case LIST:
                handleListRooms(client);
                break;
            case START:
                handleStartGame(client);
                break;
            case KICK:
                handleKickPlayer(client, message);
                break;
            case CHAT:
                handleChatMessage(client, message);
                break;
        }
    }

    private void handleCreateRoom(ClientConnection client, RoomMessage message) {
        String roomName = message.getRoomName();
        if (roomName != null && !roomName.isEmpty()) {
            Room room = new Room(roomName, 4, this);
            rooms.add(room);
            room.addPlayer(client);

            RoomMessage response = new RoomMessage(RoomMessage.RoomAction.CREATE);
            response.setSuccess(true);
            response.setRoomId(room.getId());
            response.setMessage("Room created successfully");
            client.sendMessage(response);
        } else {
            RoomMessage response = new RoomMessage(RoomMessage.RoomAction.CREATE);
            response.setSuccess(false);
            response.setMessage("Invalid room name");
            client.sendMessage(response);
        }
    }

    private void handleJoinRoom(ClientConnection client, RoomMessage message) {
        String roomId = message.getRoomId();
        Room room = findRoomById(roomId);

        if (room != null) {
            if (room.addPlayer(client)) {
                RoomMessage response = new RoomMessage(RoomMessage.RoomAction.JOIN);
                response.setSuccess(true);
                response.setRoomId(room.getId());
                response.setMessage("Joined room successfully");
                client.sendMessage(response);
            } else {
                RoomMessage response = new RoomMessage(RoomMessage.RoomAction.JOIN);
                response.setSuccess(false);
                response.setMessage("Room is full or game has started");
                client.sendMessage(response);
            }
        } else {
            RoomMessage response = new RoomMessage(RoomMessage.RoomAction.JOIN);
            response.setSuccess(false);
            response.setMessage("Room not found");
            client.sendMessage(response);
        }
    }

    private void handleLeaveRoom(ClientConnection client) {
        Room room = client.getCurrentRoom();
        if (room != null) {
            room.removePlayer(client);

            RoomMessage response = new RoomMessage(RoomMessage.RoomAction.LEAVE);
            response.setSuccess(true);
            response.setMessage("Left room successfully");
            client.sendMessage(response);

            // 在内置服务器模式下，如果用户离开房间，直接断开连接
            if (serverType == ServerType.LOCAL_SERVER) {
                client.disconnect();
            }
        }
    }

    private void handleListRooms(ClientConnection client) {
        List<RoomMessage.RoomInfo> roomInfos = new ArrayList<>();
        for (Room room : rooms) {
            if (!room.isStarted()) {
                roomInfos.add(new RoomMessage.RoomInfo(
                    room.getId(),
                    room.getName(),
                    room.getPlayers().size(),
                    room.getMaxPlayers(),
                    room.isStarted()
                ));
            }
        }

        RoomMessage response = new RoomMessage(RoomMessage.RoomAction.LIST);
        response.setSuccess(true);
        response.setRooms(roomInfos);
        client.sendMessage(response);
    }

    private void handleStartGame(ClientConnection client) {
        Room room = client.getCurrentRoom();
        if (room != null) {
            if (room.startGame(client)) {
                RoomMessage response = new RoomMessage(RoomMessage.RoomAction.START);
                response.setSuccess(true);
                response.setMessage("Game started successfully");
                client.sendMessage(response);
            } else {
                RoomMessage response = new RoomMessage(RoomMessage.RoomAction.START);
                response.setSuccess(false);
                if (room.getHost() != client) {
                    response.setMessage("Only host can start game");
                } else {
                    response.setMessage("Cannot start game");
                }
                client.sendMessage(response);
            }
        }
    }

    private void handleMoveMessage(ClientConnection client, MoveMessage message) {
        Room room = client.getCurrentRoom();
        if (room != null && room.isStarted()) {
            MoveMessage.MoveType moveType = message.getMoveType();
            int moveTypeInt = moveType.ordinal();
            room.handleMove(client, moveTypeInt);
        }
    }

    private void handleKickPlayer(ClientConnection client, RoomMessage message) {
        Room room = client.getCurrentRoom();
        String targetPlayer = message.getTargetPlayer();
        if (room != null && targetPlayer != null) {
            boolean success = room.kickPlayer(client, targetPlayer);
            RoomMessage response = new RoomMessage(RoomMessage.RoomAction.KICK);
            response.setSuccess(success);
            response.setMessage(success ? "Player kicked successfully" : "Failed to kick player");
            client.sendMessage(response);
        }
    }

    private void handleChatMessage(ClientConnection client, RoomMessage message) {
        Room room = client.getCurrentRoom();
        String chatMessage = message.getChatMessage();
        if (room != null && chatMessage != null && !chatMessage.isEmpty()) {
            room.broadcastChatMessage(client.getPlayerName(), chatMessage);
        }
    }

    public void sendGameStartMessage(ClientConnection client, Room room, int playerIndex) {
        GameStartMessage message = new GameStartMessage();
        message.setRoomId(room.getId());
        message.setPlayerCount(room.getPlayers().size());
        message.setYourIndex(playerIndex);
        client.sendMessage(message);
    }

    public void removeClient(ClientConnection client) {
        clients.remove(client);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
    }

    private Room findRoomById(String roomId) {
        for (Room room : rooms) {
            if (room.getId().equals(roomId)) {
                return room;
            }
        }
        return null;
    }

    public void stop() {
        running = false;
        try {
            if (server != null) {
                server.stop();
                server = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
