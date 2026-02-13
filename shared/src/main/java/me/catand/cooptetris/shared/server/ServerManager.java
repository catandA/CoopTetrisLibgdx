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
            System.out.println("ServerManager: 正在启动服务器...");
            // 创建kryonet服务器
            server = new Server();
            
            // 注册消息类
            registerMessages();
            System.out.println("ServerManager: 消息类注册完成");
            
            // 启动服务器
            server.start();
            server.bind(port);
            running = true;
            System.out.println("ServerManager: 服务器启动成功，监听端口: " + port);
            System.out.println("ServerManager: 服务器类型: " + (serverType == ServerType.LOCAL_SERVER ? "本地服务器" : "专用服务器"));
            
            // 添加监听器
            server.addListener(new Listener() {
                @Override
                public void connected(Connection connection) {
                    // 创建新的客户端连接
                    ClientConnection client = new ClientConnection(connection, ServerManager.this);
                    clients.add(client);
                    System.out.println("ServerManager: 客户端连接: " + connection.getRemoteAddressTCP());
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
                            String playerName = client.getPlayerName() != null ? client.getPlayerName() : "未命名";
                            // 从房间中移除
                            if (client.getCurrentRoom() != null) {
                                System.out.println("ServerManager: 玩家 " + playerName + " 断开连接，从房间 " + client.getCurrentRoom().getName() + " 中移除");
                                client.getCurrentRoom().removePlayer(client);
                            } else {
                                System.out.println("ServerManager: 玩家 " + playerName + " 断开连接");
                            }
                            clients.remove(i);
                            break;
                        }
                    }
                }
            });
            

            // 服务器启动时默认创建一个房间
            createDefaultRoom();
            System.out.println("ServerManager: 默认房间创建完成");
            System.out.println("ServerManager: 服务器初始化完成，等待客户端连接...");
        } catch (IOException e) {
            System.err.println("ServerManager: 服务器启动失败: " + e.getMessage());
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
     * 创建默认房间
     * 在专有服务器上，这个房间永远不会开始游戏，没人有房主权限，用户可以在此自由聊天即聊天室
     * 在内置服务器上，这个房间用作唯一房间进行游戏
     */
    public void createDefaultRoom() {
        // 创建默认房间（设置为默认聊天室，没有房主）
        defaultRoom = new Room("Lobby", 10, this, true);
        rooms.add(defaultRoom);
        System.out.println("ServerManager: 默认房间创建成功: Lobby (ID: " + defaultRoom.getId() + ")");
        System.out.println("ServerManager: 默认房间最大玩家数: 10");
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
        System.out.println("ServerManager: 收到连接请求，玩家名称: " + playerName);
        
        if (playerName != null && !playerName.isEmpty()) {
            client.setPlayerName(playerName);
            ConnectMessage response = new ConnectMessage();
            response.setSuccess(true);
            response.setClientId(client.getClientId());
            response.setMessage("Connected successfully");
            client.sendMessage(response);
            
            System.out.println("ServerManager: 玩家 " + playerName + " 连接成功，客户端ID: " + client.getClientId());

            // 在内置服务器模式下，自动将客户端加入默认房间
            if (serverType == ServerType.LOCAL_SERVER && defaultRoom != null) {
                defaultRoom.addPlayer(client);
                // 发送加入房间的响应
                RoomMessage roomResponse = new RoomMessage(RoomMessage.RoomAction.JOIN);
                roomResponse.setSuccess(true);
                roomResponse.setRoomId(defaultRoom.getId());
                roomResponse.setMessage("Joined default room automatically");
                client.sendMessage(roomResponse);
                
                System.out.println("ServerManager: 玩家 " + playerName + " 自动加入默认房间: " + defaultRoom.getName());
            }
        } else {
            ConnectMessage response = new ConnectMessage();
            response.setSuccess(false);
            response.setMessage("Invalid player name");
            client.sendMessage(response);
            client.disconnect();
            
            System.out.println("ServerManager: 连接失败: 无效的玩家名称");
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
        System.out.println("ServerManager: 玩家 " + client.getPlayerName() + " 请求创建房间: " + roomName);
        
        if (roomName != null && !roomName.isEmpty()) {
            Room room = new Room(roomName, 4, this);
            rooms.add(room);
            room.addPlayer(client);

            RoomMessage response = new RoomMessage(RoomMessage.RoomAction.CREATE);
            response.setSuccess(true);
            response.setRoomId(room.getId());
            response.setMessage("Room created successfully");
            client.sendMessage(response);
            
            System.out.println("ServerManager: 房间创建成功: " + roomName + " (ID: " + room.getId() + ")");
            System.out.println("ServerManager: 房主: " + client.getPlayerName());
        } else {
            RoomMessage response = new RoomMessage(RoomMessage.RoomAction.CREATE);
            response.setSuccess(false);
            response.setMessage("Invalid room name");
            client.sendMessage(response);
            
            System.out.println("ServerManager: 房间创建失败: 无效的房间名称");
        }
    }

    private void handleJoinRoom(ClientConnection client, RoomMessage message) {
        String roomId = message.getRoomId();
        System.out.println("ServerManager: 玩家 " + client.getPlayerName() + " 请求加入房间: " + roomId);
        
        Room room = findRoomById(roomId);

        if (room != null) {
            if (room.addPlayer(client)) {
                RoomMessage response = new RoomMessage(RoomMessage.RoomAction.JOIN);
                response.setSuccess(true);
                response.setRoomId(room.getId());
                response.setMessage("Joined room successfully");
                client.sendMessage(response);
                
                System.out.println("ServerManager: 玩家 " + client.getPlayerName() + " 成功加入房间: " + room.getName());
            } else {
                RoomMessage response = new RoomMessage(RoomMessage.RoomAction.JOIN);
                response.setSuccess(false);
                response.setMessage("Room is full or game has started");
                client.sendMessage(response);
                
                System.out.println("ServerManager: 玩家 " + client.getPlayerName() + " 加入房间失败: 房间已满或游戏已开始");
            }
        } else {
            RoomMessage response = new RoomMessage(RoomMessage.RoomAction.JOIN);
            response.setSuccess(false);
            response.setMessage("Room not found");
            client.sendMessage(response);
            
            System.out.println("ServerManager: 玩家 " + client.getPlayerName() + " 加入房间失败: 房间不存在");
        }
    }

    private void handleLeaveRoom(ClientConnection client) {
        Room room = client.getCurrentRoom();
        if (room != null) {
            String playerName = client.getPlayerName();
            String roomName = room.getName();
            
            room.removePlayer(client);

            RoomMessage response = new RoomMessage(RoomMessage.RoomAction.LEAVE);
            response.setSuccess(true);
            response.setMessage("Left room successfully");
            client.sendMessage(response);
            
            System.out.println("ServerManager: 玩家 " + playerName + " 离开房间: " + roomName);

            // 在内置服务器模式下，如果用户离开房间，直接断开连接
            if (serverType == ServerType.LOCAL_SERVER) {
                client.disconnect();
                System.out.println("ServerManager: 本地服务器模式: 断开玩家 " + playerName + " 的连接");
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
        
        System.out.println("ServerManager: 玩家 " + client.getPlayerName() + " 请求房间列表，返回 " + roomInfos.size() + " 个房间");
    }

    private void handleStartGame(ClientConnection client) {
        Room room = client.getCurrentRoom();
        if (room != null) {
            System.out.println("ServerManager: 玩家 " + client.getPlayerName() + " 请求开始游戏: " + room.getName());
            
            if (room.startGame(client)) {
                RoomMessage response = new RoomMessage(RoomMessage.RoomAction.START);
                response.setSuccess(true);
                response.setMessage("Game started successfully");
                client.sendMessage(response);
                
                System.out.println("ServerManager: 游戏开始成功: " + room.getName() + "，玩家数: " + room.getPlayers().size());
            } else {
                RoomMessage response = new RoomMessage(RoomMessage.RoomAction.START);
                response.setSuccess(false);
                if (room.getHost() != client) {
                    response.setMessage("Only host can start game");
                    System.out.println("ServerManager: 游戏开始失败: 只有房主可以开始游戏");
                } else {
                    response.setMessage("Cannot start game");
                    System.out.println("ServerManager: 游戏开始失败: 无法开始游戏");
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
        System.out.println("ServerManager: 玩家 " + client.getPlayerName() + " 请求踢出玩家: " + targetPlayer + " (房间: " + room.getName() + ")");
        
        if (room != null && targetPlayer != null) {
            boolean success = room.kickPlayer(client, targetPlayer);
            RoomMessage response = new RoomMessage(RoomMessage.RoomAction.KICK);
            response.setSuccess(success);
            response.setMessage(success ? "Player kicked successfully" : "Failed to kick player");
            client.sendMessage(response);
            
            if (success) {
                System.out.println("ServerManager: 踢出玩家成功: " + targetPlayer + " (由 " + client.getPlayerName() + " 执行)");
            } else {
                System.out.println("ServerManager: 踢出玩家失败: " + targetPlayer + " (由 " + client.getPlayerName() + " 执行)");
            }
        }
    }

    private void handleChatMessage(ClientConnection client, RoomMessage message) {
        Room room = client.getCurrentRoom();
        String chatMessage = message.getChatMessage();
        if (room != null && chatMessage != null && !chatMessage.isEmpty()) {
            room.broadcastChatMessage(client.getPlayerName(), chatMessage);
            System.out.println("ServerManager: 聊天消息 [" + room.getName() + "] " + client.getPlayerName() + ": " + chatMessage);
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
        
        // 在内置服务器模式下，如果默认房间被移除，停止整个服务器
        if (serverType == ServerType.LOCAL_SERVER && room == defaultRoom) {
            System.out.println("ServerManager: 本地服务器模式: 默认房间被移除，停止服务器");
            stop();
        }
    }

    private Room findRoomById(String roomId) {
        for (Room room : rooms) {
            if (room.getId().equals(roomId)) {
                return room;
            }
        }
        return null;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void stop() {
        System.out.println("ServerManager: 正在停止服务器...");
        running = false;
        try {
            if (server != null) {
                server.stop();
                server = null;
                System.out.println("ServerManager: 服务器停止成功");
            }
        } catch (Exception e) {
            System.err.println("ServerManager: 服务器停止失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
