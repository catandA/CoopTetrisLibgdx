package me.catand.cooptetris.shared.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import me.catand.cooptetris.shared.message.ConnectMessage;
import me.catand.cooptetris.shared.message.CountdownMessage;
import me.catand.cooptetris.shared.message.CoopGameStateMessage;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.message.NetworkMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.message.PlayerScoresMessage;
import me.catand.cooptetris.shared.message.PlayerSlotMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.shared.tetris.GameMode;

public class ServerManager {
	public enum ServerType {
		LOCAL_SERVER,  // 内置服务器
		DEDICATED_SERVER  // 专有服务器
	}

	private Server server;
	private final List<ClientConnection> clients;
	private final List<Room> rooms;
	private boolean running;
	@Getter
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
			server = new Server(32768, 16384);

			// 注册消息类
			registerMessages();
			System.out.println("ServerManager: 消息类注册完成");

			// 启动服务器
			server.bind(port);
			System.out.println("ServerManager: 端口绑定完成: " + port);

			// 启动服务器
			server.start();
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
		Kryo kryo = server.getKryo();

		// 注册基本类型
		kryo.register(boolean.class);
		kryo.register(int.class);
		kryo.register(Integer.class);
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
		kryo.register(CoopGameStateMessage.class);
		kryo.register(CoopGameStateMessage.PlayerPieceState.class);
		kryo.register(CoopGameStateMessage.PlayerPieceState[].class);
		kryo.register(PlayerSlotMessage.class);
		kryo.register(PlayerSlotMessage.SlotAction.class);
		kryo.register(PlayerSlotMessage.SlotInfo.class);
		kryo.register(GameMode.class);
		kryo.register(long.class);

		// 注册String列表类型（用于玩家名字列表）
		kryo.register(java.util.List.class);
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
			case "playerSlot":
				handlePlayerSlotMessage(client, (PlayerSlotMessage) message);
				break;
		}
	}

	private void handleConnectMessage(ClientConnection client, ConnectMessage message) {
		String playerName = message.getPlayerName();
		String language = message.getLanguage();
		System.out.println("ServerManager: 收到连接请求，玩家名称: " + playerName + ", 语言: " + language);

		if (playerName != null && !playerName.isEmpty()) {
			client.setPlayerName(playerName);
			// 保存客户端语言设置
			if (language != null && !language.isEmpty()) {
				client.setLanguage(language);
			}
			ConnectMessage response = new ConnectMessage();
			response.setSuccess(true);
			response.setClientId(client.getClientId());
			response.setMessage("Connected successfully");
			client.sendMessage(response);

			System.out.println("ServerManager: 玩家 " + playerName + " 连接成功，客户端ID: " + client.getClientId() + ", 语言: " + client.getLanguage());

			// 注意：新流程中不再自动加入默认房间
			// 本地服务器创建者会在客户端主动创建房间，并成为房主
			// 这样可以确保创建者有完整的房主权限
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
			case STATUS:
				handleStatusRequest(client);
				break;
			case SET_GAME_MODE:
				handleSetGameMode(client, message);
				break;
		}
	}

	private void handleStatusRequest(ClientConnection client) {
		Room room = client.getCurrentRoom();
		if (room != null) {
			room.broadcastRoomStatus();
			System.out.println("ServerManager: 玩家 " + client.getPlayerName() + " 请求房间状态更新");
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
			response.setRoomName(room.getName());
			response.setMessage("Room created successfully");
			client.sendMessage(response);

			System.out.println("ServerManager: 房间创建成功: " + roomName + " (ID: " + room.getId() + ")");
			System.out.println("ServerManager: 房主: " + client.getPlayerName());

			// 广播房间列表更新给所有不在房间中的客户端
			broadcastRoomListUpdate();
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
				response.setRoomName(room.getName());
				response.setMessage("Joined room successfully");
				// 告诉客户端他们是否是房主
				response.setHost(room.getHost() == client);
				client.sendMessage(response);

				// 立即向新加入的玩家发送房间状态和玩家槽位信息
				room.broadcastRoomStatus();
				room.broadcastPlayerSlots();

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
				// 根据客户端语言获取本地化的房间名称
				String roomName = getLocalizedRoomName(room, client.getLanguage());
				roomInfos.add(new RoomMessage.RoomInfo(
					room.getId(),
					roomName,
					room.getActualPlayerCount(),
					room.getMaxPlayers(),
					room.isStarted(),
					room.getDisplayPlayerCount() // 显示的玩家数量（包含锁定的槽位）
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

	private void handlePlayerSlotMessage(ClientConnection client, PlayerSlotMessage message) {
		Room room = client.getCurrentRoom();
		if (room == null) {
			return;
		}

		switch (message.getAction()) {
			case REQUEST_SLOT:
				// 玩家请求移动到指定槽位
				boolean slotSuccess = room.requestSlotChange(client, message.getSlotIndex());
				PlayerSlotMessage slotResponse = new PlayerSlotMessage(PlayerSlotMessage.SlotAction.SLOT_ASSIGNED);
				slotResponse.setSuccess(slotSuccess);
				slotResponse.setSlotIndex(message.getSlotIndex());
				slotResponse.setMessage(slotSuccess ? "Slot changed successfully" : "Failed to change slot");
				client.sendMessage(slotResponse);
				break;

			case REQUEST_COLOR:
				// 玩家请求更改颜色
				int newColorIndex = message.getColorIndex();
				if (newColorIndex >= 0 && newColorIndex < 4) {
					client.setColorIndex(newColorIndex);
					// 广播颜色变更给所有玩家
					PlayerSlotMessage colorResponse = new PlayerSlotMessage(PlayerSlotMessage.SlotAction.COLOR_CHANGED);
					colorResponse.setSuccess(true);
					colorResponse.setSlotIndex(client.getSlotIndex());
					colorResponse.setColorIndex(newColorIndex);
					colorResponse.setMessage("Color changed successfully");
					room.broadcastPlayerSlots();
				}
				break;

			case REQUEST_LOCK:
				// 房主请求锁定/解锁槽位
				boolean lockSuccess = room.requestLockToggle(client, message.getSlotIndex());
				PlayerSlotMessage lockResponse = new PlayerSlotMessage(PlayerSlotMessage.SlotAction.LOCK_CHANGED);
				lockResponse.setSuccess(lockSuccess);
				lockResponse.setSlotIndex(message.getSlotIndex());
				lockResponse.setLocked(room.getSlot(message.getSlotIndex()) != null && room.getSlot(message.getSlotIndex()).isLocked());
				lockResponse.setMessage(lockSuccess ? "Lock status changed successfully" : "Failed to change lock status");
				client.sendMessage(lockResponse);
				break;

			case REQUEST_KICK:
				// 房主请求踢出玩家
				boolean kickSuccess = room.requestKick(client, message.getSlotIndex());
				PlayerSlotMessage kickResponse = new PlayerSlotMessage(PlayerSlotMessage.SlotAction.REQUEST_KICK);
				kickResponse.setSuccess(kickSuccess);
				kickResponse.setSlotIndex(message.getSlotIndex());
				kickResponse.setMessage(kickSuccess ? "Player kicked successfully" : "Failed to kick player");
				client.sendMessage(kickResponse);
				break;
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

	private void handleSetGameMode(ClientConnection client, RoomMessage message) {
		Room room = client.getCurrentRoom();
		if (room != null && room.getHost() == client && !room.isStarted()) {
			GameMode newMode = message.getGameMode();
			if (newMode != null) {
				room.setGameMode(newMode);
				System.out.println("ServerManager: 房主 " + client.getPlayerName() + " 将房间 " + room.getName() + " 的游戏模式设置为 " + newMode);

				// 广播房间状态更新
				room.broadcastRoomStatus();

				// 发送成功响应
				RoomMessage response = new RoomMessage(RoomMessage.RoomAction.SET_GAME_MODE);
				response.setSuccess(true);
				response.setGameMode(newMode);
				client.sendMessage(response);
			}
		} else {
			// 发送失败响应
			RoomMessage response = new RoomMessage(RoomMessage.RoomAction.SET_GAME_MODE);
			response.setSuccess(false);
			response.setMessage("Only host can change game mode before game starts");
			client.sendMessage(response);
		}
	}

	public void sendGameStartMessage(ClientConnection client, Room room, int playerIndex, long seed) {
		GameStartMessage message = new GameStartMessage();
		message.setRoomId(room.getId());
		message.setPlayerCount(room.getPlayers().size());
		message.setYourIndex(playerIndex);  // 玩家的槽位索引
		message.setYourColorIndex(client.getColorIndex()); // 玩家选择的颜色索引
		message.setSeed(seed);
		message.setGameMode(room.getGameMode());

		// 收集所有槽位的玩家名字和颜色（按槽位索引 0-3）
		List<String> playerNames = new ArrayList<>();
		List<Integer> playerColors = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			PlayerSlot slot = room.getSlot(i);
			if (slot != null && !slot.isEmpty()) {
				playerNames.add(slot.getPlayerName());
				playerColors.add(slot.getColorIndex()); // 获取玩家的颜色选择
			} else {
				playerNames.add(""); // 空槽位用空字符串表示
				playerColors.add(-1); // 空槽位用-1表示
			}
		}
		message.setPlayerNames(playerNames);
		message.setPlayerColors(playerColors);

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

		// 广播房间列表更新给所有不在房间中的客户端
		broadcastRoomListUpdate();
	}

	/**
	 * 广播房间列表更新给所有不在房间中的客户端
	 */
	public void broadcastRoomListUpdate() {
		// 发送给所有不在房间中的客户端，每个客户端根据自己的语言获取本地化房间名称
		int recipientCount = 0;
		for (ClientConnection client : clients) {
			if (client.getCurrentRoom() == null) {
				List<RoomMessage.RoomInfo> roomInfos = new ArrayList<>();
				for (Room r : rooms) {
					if (!r.isStarted()) {
						// 根据客户端语言获取本地化的房间名称
						String roomName = getLocalizedRoomName(r, client.getLanguage());
						roomInfos.add(new RoomMessage.RoomInfo(
							r.getId(),
							roomName,
							r.getActualPlayerCount(),
							r.getMaxPlayers(),
							r.isStarted(),
							r.getDisplayPlayerCount() // 显示的玩家数量（包含锁定的槽位）
						));
					}
				}

				RoomMessage listMessage = new RoomMessage(RoomMessage.RoomAction.LIST);
				listMessage.setSuccess(true);
				listMessage.setRooms(roomInfos);
				client.sendMessage(listMessage);
				recipientCount++;
			}
		}

		if (recipientCount > 0) {
			System.out.println("ServerManager: 广播房间列表更新给 " + recipientCount + " 个客户端");
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

	/**
	 * 根据客户端语言获取本地化的房间名称
	 * 对于默认聊天室，根据语言返回本地化名称；其他房间返回原始名称
	 */
	private String getLocalizedRoomName(Room room, String language) {
		if (room.isDefaultLobby()) {
			// 根据客户端语言返回本地化的默认聊天室名称
			if ("zh".equals(language)) {
				return "聊天室";
			} else {
				// 默认英文
				return "Chat Room";
			}
		}
		// 非默认房间返回原始名称
		return room.getName();
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
