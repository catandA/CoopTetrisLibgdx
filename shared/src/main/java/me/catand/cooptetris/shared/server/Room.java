package me.catand.cooptetris.shared.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import me.catand.cooptetris.shared.message.CountdownMessage;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.message.PlayerScoresMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.shared.tetris.GameLogic;
import me.catand.cooptetris.shared.tetris.GameMode;
import me.catand.cooptetris.shared.util.Random;

@Data
public class Room {
    private final String id;
    private final String name;
    private final List<ClientConnection> players;
    private final int maxPlayers;
    private boolean started;
    private final List<GameLogic> gameLogics;
    private final ServerManager serverManager;
    private ClientConnection host;
    private final boolean isDefaultLobby;
    private Thread gameLoopThread;
    private boolean gameLoopRunning;
    private GameMode gameMode;
    private long gameSeed; // 游戏随机数种子，用于同步方块生成

    public Room(String name, int maxPlayers, ServerManager serverManager) {
        this(name, maxPlayers, serverManager, false);
    }

    public Room(String name, int maxPlayers, ServerManager serverManager, boolean isDefaultLobby) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.players = new ArrayList<>();
        this.maxPlayers = maxPlayers;
        this.started = false;
        this.serverManager = serverManager;
        this.gameLogics = new ArrayList<>();
        this.isDefaultLobby = isDefaultLobby;
        this.gameLoopThread = null;
        this.gameLoopRunning = false;
        this.gameMode = GameMode.COOP;
        this.gameSeed = 0;
    }

    public boolean addPlayer(ClientConnection client) {
        if (players.size() < maxPlayers && !started) {
            players.add(client);
            client.setCurrentRoom(this);

            // 根据游戏模式处理游戏逻辑创建
            if (gameMode == GameMode.COOP) {
                // 合作模式：所有玩家共享一个游戏逻辑
                if (gameLogics.isEmpty()) {
                    gameLogics.add(new GameLogic());
                }
                // 合作模式下所有玩家使用索引0的游戏逻辑
                client.setGameLogicIndex(0);
            } else {
                // PVP模式：每个玩家独立的游戏逻辑
                gameLogics.add(new GameLogic());
                client.setGameLogicIndex(gameLogics.size() - 1);
            }

            // 第一个加入的玩家成为房主（默认聊天室除外）
            if (players.size() == 1 && !isDefaultLobby) {
                host = client;
            }

            broadcastRoomStatus();

            // 广播房间列表更新给所有不在房间中的客户端
            if (serverManager != null) {
                serverManager.broadcastRoomListUpdate();
            }

            return true;
        }
        return false;
    }

    public void removePlayer(ClientConnection client) {
        int index = players.indexOf(client);
        if (index != -1) {
            players.remove(index);
            client.setCurrentRoom(null);

            // PVP模式下移除对应的游戏逻辑
            if (gameMode == GameMode.PVP) {
                int gameLogicIndex = client.getGameLogicIndex();
                if (gameLogicIndex >= 0 && gameLogicIndex < gameLogics.size()) {
                    gameLogics.remove(gameLogicIndex);
                }
                // 更新其他玩家的游戏逻辑索引
                for (int i = 0; i < players.size(); i++) {
                    players.get(i).setGameLogicIndex(i);
                }
            } else if (gameMode == GameMode.COOP && players.isEmpty()) {
                // 合作模式下所有玩家离开后才清除游戏逻辑
                gameLogics.clear();
            }
            client.setGameLogicIndex(-1);

            // 如果离开的是房主，设置下一个玩家为房主（默认聊天室除外）
            if (client == host && !players.isEmpty() && !isDefaultLobby) {
                host = players.get(0);
            }

            if (players.isEmpty()) {
                // 停止游戏循环线程
                stopGameLoop();

                // 检查服务器类型
                boolean isLocalServer = serverManager.getServerType() == ServerManager.ServerType.LOCAL_SERVER;

                // 决定是否移除房间
                if (!isDefaultLobby || isLocalServer) {
                    // 非默认聊天室或本地服务器模式下的默认聊天室，从服务器中移除
                    serverManager.removeRoom(this);
                }
            } else {
                broadcastRoomStatus();

                // 广播房间列表更新给所有不在房间中的客户端
                if (serverManager != null) {
                    serverManager.broadcastRoomListUpdate();
                }
            }
        }
    }

    private boolean isCountingDown = false;
    private int countdownSeconds = 3;

    public boolean startGame(ClientConnection requester) {
        if (!started && !isCountingDown && requester == host && !players.isEmpty()) {
            // 开始倒计时，而不是立即开始游戏
            startCountdown();
            return true;
        }
        return false;
    }

    private void startCountdown() {
        isCountingDown = true;
        countdownSeconds = 3;

        // 广播倒计时开始消息给所有玩家
        broadcastCountdownMessage(countdownSeconds, true);

        // 启动倒计时线程
        new Thread(() -> {
            while (countdownSeconds > 0 && isCountingDown) {
                try {
                    Thread.sleep(1000); // 等待1秒
                    countdownSeconds--;

                    if (countdownSeconds > 0) {
                        // 广播剩余秒数
                        broadcastCountdownMessage(countdownSeconds, true);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            if (isCountingDown) {
                // 倒计时结束，真正开始游戏
                isCountingDown = false;
                broadcastCountdownMessage(0, false);
                actuallyStartGame();
            }
        }).start();
    }

    private void broadcastCountdownMessage(int seconds, boolean isStarting) {
        CountdownMessage message = new CountdownMessage(seconds, isStarting);
        for (ClientConnection client : players) {
            client.sendMessage(message);
        }
    }

    private void actuallyStartGame() {
        started = true;

        // 生成游戏种子，用于同步所有客户端的方块生成
        gameSeed = Random.Long();

        // 清除旧的游戏逻辑
        gameLogics.clear();

        if (gameMode == GameMode.COOP) {
            // 合作模式：所有玩家共享一个游戏逻辑
            gameLogics.add(new GameLogic());
            gameLogics.get(0).reset(gameSeed);
        } else {
            // PVP模式：每个玩家有自己的游戏逻辑
            for (int i = 0; i < players.size(); i++) {
                GameLogic logic = new GameLogic();
                logic.reset(gameSeed);
                gameLogics.add(logic);
                // 设置每个玩家的游戏逻辑索引
                players.get(i).setGameLogicIndex(i);
            }
        }

        for (int i = 0; i < players.size(); i++) {
            ClientConnection client = players.get(i);
            serverManager.sendGameStartMessage(client, this, i, gameSeed);
        }

        // 启动游戏循环线程
        startGameLoop();

        // 立即广播一次游戏状态，确保所有客户端同步初始状态
        broadcastGameState();

        broadcastRoomStatus();
    }

    public void handleMove(ClientConnection client, int moveType) {
        if (!started) return;

        int gameLogicIndex = client.getGameLogicIndex();
        if (gameLogicIndex >= 0 && gameLogicIndex < gameLogics.size()) {
            GameLogic gameLogic = gameLogics.get(gameLogicIndex);

            switch (moveType) {
                case 0: // LEFT
                    gameLogic.moveLeft();
                    break;
                case 1: // RIGHT
                    gameLogic.moveRight();
                    break;
                case 2: // DOWN
                    gameLogic.moveDown();
                    break;
                case 3: // DROP
                    gameLogic.dropPiece();
                    break;
                case 4: // ROTATE_CLOCKWISE
                    gameLogic.rotateClockwise();
                    break;
            }

            broadcastGameState();
        }
    }

    public void broadcastGameState() {
        if (gameMode == GameMode.COOP) {
            // 合作模式：所有玩家共享同一个游戏状态
            if (!gameLogics.isEmpty()) {
                GameLogic gameLogic = gameLogics.get(0);
                GameStateMessage message = createGameStateMessage(gameLogic);

                for (ClientConnection client : players) {
                    client.sendMessage(message);
                }
            }
        } else {
            // PVP模式：广播所有玩家的游戏状态给所有人
            // 发送给每个玩家：包含所有玩家的游戏状态
            for (int i = 0; i < players.size(); i++) {
                ClientConnection client = players.get(i);
                int playerGameLogicIndex = client.getGameLogicIndex();

                // 发送自己的游戏状态（设置playerIndex为自己的索引）
                if (playerGameLogicIndex >= 0 && playerGameLogicIndex < gameLogics.size()) {
                    GameStateMessage myState = createGameStateMessage(gameLogics.get(playerGameLogicIndex));
                    myState.setPlayerIndex(playerGameLogicIndex);
                    client.sendMessage(myState);
                }

                // 发送其他玩家的游戏状态（用于显示对手游戏板）
                for (int j = 0; j < gameLogics.size(); j++) {
                    if (j != playerGameLogicIndex) {
                        // 为每个对手创建独立的游戏状态消息副本
                        GameStateMessage opponentState = createGameStateMessage(gameLogics.get(j));
                        opponentState.setPlayerIndex(j); // 标记这是哪个玩家的状态
                        client.sendMessage(opponentState);
                    }
                }
            }

            // PVP模式下同时广播所有玩家分数
            broadcastPlayerScores();
        }
    }

    /**
     * 广播所有玩家的分数信息（用于PVP模式）
     */
    private void broadcastPlayerScores() {
        List<PlayerScoresMessage.PlayerScore> scores = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            ClientConnection player = players.get(i);
            int gameLogicIndex = player.getGameLogicIndex();
            if (gameLogicIndex >= 0 && gameLogicIndex < gameLogics.size()) {
                GameLogic gameLogic = gameLogics.get(gameLogicIndex);
                scores.add(new PlayerScoresMessage.PlayerScore(
                    i,
                    player.getPlayerName(),
                    gameLogic.getScore(),
                    gameLogic.getLines(),
                    gameLogic.getLevel(),
                    gameLogic.isGameOver()
                ));
            }
        }

        // 按分数降序排序
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        // 发送给每个玩家
        for (int i = 0; i < players.size(); i++) {
            PlayerScoresMessage message = new PlayerScoresMessage(scores, i);
            players.get(i).sendMessage(message);
        }
    }

    private GameStateMessage createGameStateMessage(GameLogic gameLogic) {
        GameStateMessage message = new GameStateMessage();
        message.setBoard(gameLogic.getBoard());
        message.setCurrentPiece(gameLogic.getCurrentPiece());
        message.setCurrentPieceX(gameLogic.getCurrentPieceX());
        message.setCurrentPieceY(gameLogic.getCurrentPieceY());
        message.setCurrentPieceRotation(gameLogic.getCurrentPieceRotation());
        message.setNextPiece(gameLogic.getNextPiece());
        message.setScore(gameLogic.getScore());
        message.setLevel(gameLogic.getLevel());
        message.setLines(gameLogic.getLines());
        return message;
    }

    public void broadcastRoomStatus() {
        List<String> playerNames = new ArrayList<>();
        for (ClientConnection player : players) {
            playerNames.add(player.getPlayerName());
        }

        for (ClientConnection client : players) {
            RoomMessage message = new RoomMessage(RoomMessage.RoomAction.STATUS);
            message.setRoomId(id);
            message.setRoomName(name);
            message.setPlayers(playerNames);
            message.setStarted(started);
            message.setSuccess(true);
            message.setGameMode(gameMode);
            message.setHost(client == host);
            client.sendMessage(message);
        }
    }

    public boolean kickPlayer(ClientConnection requester, String playerName) {
        if (requester == host) {
            for (ClientConnection player : players) {
                if (player.getPlayerName().equals(playerName)) {
                    // 根据客户端语言发送本地化的踢出通知
                    String language = player.getLanguage();
                    NotificationMessage kickNotification = createLocalizedKickNotification(language);
                    player.sendMessage(kickNotification);

                    // 短暂延迟后移除玩家，确保通知先到达
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    player.sendMessage(new RoomMessage(RoomMessage.RoomAction.KICK));
                    removePlayer(player);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 创建本地化的踢出通知消息
     */
    private NotificationMessage createLocalizedKickNotification(String language) {
        NotificationMessage notification = new NotificationMessage();
        notification.setNotificationType(NotificationMessage.NotificationType.KICKED);

        // 根据语言设置本地化文本
        if ("zh".equals(language)) {
            notification.setTitle("被踢出房间");
            notification.setMessage("你已被房主踢出房间。");
            notification.setReason("房主决定");
        } else {
            // 默认英文
            notification.setTitle("Kicked from Room");
            notification.setMessage("You have been kicked from the room by the host.");
            notification.setReason("Host decision");
        }

        return notification;
    }

    public void broadcastChatMessage(String sender, String message) {
        for (ClientConnection client : players) {
            RoomMessage chatMessage = new RoomMessage(RoomMessage.RoomAction.CHAT);
            chatMessage.setMessage(sender + ": " + message);
            client.sendMessage(chatMessage);
        }
    }

    /**
     * 启动游戏循环线程，处理方块自动下落
     */
    private void startGameLoop() {
        if (gameLoopThread == null || !gameLoopThread.isAlive()) {
            gameLoopRunning = true;
            gameLoopThread = new Thread(() -> {
                long lastTime = System.currentTimeMillis();
                final long DROP_INTERVAL = 1000; // 1秒

                while (gameLoopRunning && started) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastTime >= DROP_INTERVAL) {
                        // 处理游戏逻辑
                        updateGameState();
                        lastTime = currentTime;
                    }

                    try {
                        Thread.sleep(100); // 避免CPU占用过高
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            gameLoopThread.start();
        }
    }

    /**
     * 停止游戏循环线程
     */
    private void stopGameLoop() {
        gameLoopRunning = false;
        if (gameLoopThread != null && gameLoopThread.isAlive()) {
            try {
                gameLoopThread.join(1000); // 等待线程结束，最多1秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        gameLoopThread = null;
    }

    /**
     * 更新游戏状态，处理方块自动下落
     */
    public void updateGameState() {
        // 遍历所有玩家的游戏逻辑
        for (int i = 0; i < gameLogics.size(); i++) {
            GameLogic gameLogic = gameLogics.get(i);
            // 执行方块自动下落
            gameLogic.moveDown();
        }
        // 广播游戏状态更新，确保所有客户端同步
        broadcastGameState();
    }
}
