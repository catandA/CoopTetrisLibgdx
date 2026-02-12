package me.catand.cooptetris.shared.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.shared.tetris.GameLogic;

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
    }

    public boolean addPlayer(ClientConnection client) {
        if (players.size() < maxPlayers && !started) {
            players.add(client);
            client.setCurrentRoom(this);
            gameLogics.add(new GameLogic());

            // 第一个加入的玩家成为房主（默认聊天室除外）
            if (players.size() == 1 && !isDefaultLobby) {
                host = client;
            }

            broadcastRoomStatus();
            return true;
        }
        return false;
    }

    public void removePlayer(ClientConnection client) {
        int index = players.indexOf(client);
        if (index != -1) {
            players.remove(index);
            gameLogics.remove(index);
            client.setCurrentRoom(null);

            // 如果离开的是房主，设置下一个玩家为房主（默认聊天室除外）
            if (client == host && !players.isEmpty() && !isDefaultLobby) {
                host = players.get(0);
            }

            if (players.isEmpty()) {
                serverManager.removeRoom(this);
            } else {
                broadcastRoomStatus();
            }
        }
    }

    public boolean startGame(ClientConnection requester) {
        if (!started && requester == host && !players.isEmpty()) {
            started = true;

            for (int i = 0; i < players.size(); i++) {
                ClientConnection client = players.get(i);
                serverManager.sendGameStartMessage(client, this, i);
            }

            broadcastRoomStatus();
            return true;
        }
        return false;
    }

    public void handleMove(ClientConnection client, int moveType) {
        int playerIndex = players.indexOf(client);
        if (playerIndex != -1 && started) {
            GameLogic gameLogic = gameLogics.get(playerIndex);

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
        for (int i = 0; i < players.size(); i++) {
            ClientConnection client = players.get(i);
            GameLogic gameLogic = gameLogics.get(i);

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

            client.sendMessage(message);
        }
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
            client.sendMessage(message);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ClientConnection> getPlayers() {
        return players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isStarted() {
        return started;
    }

    public ClientConnection getHost() {
        return host;
    }

    public boolean kickPlayer(ClientConnection requester, String playerName) {
        if (requester == host) {
            for (ClientConnection player : players) {
                if (player.getPlayerName().equals(playerName)) {
                    player.sendMessage(new RoomMessage(RoomMessage.RoomAction.KICK));
                    removePlayer(player);
                    return true;
                }
            }
        }
        return false;
    }

    public void broadcastChatMessage(String sender, String message) {
        for (ClientConnection client : players) {
            RoomMessage chatMessage = new RoomMessage(RoomMessage.RoomAction.CHAT);
            chatMessage.setMessage(sender + ": " + message);
            client.sendMessage(chatMessage);
        }
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
