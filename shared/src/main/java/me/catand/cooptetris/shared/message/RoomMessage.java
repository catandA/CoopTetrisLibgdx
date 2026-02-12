package me.catand.cooptetris.shared.message;

import java.util.List;

public class RoomMessage extends NetworkMessage {
    public enum RoomAction {
        CREATE,
        JOIN,
        LEAVE,
        LIST,
        START,
        STATUS,
        KICK,
        CHAT
    }

    private RoomAction action;
    private String roomId;
    private String roomName;
    private List<RoomInfo> rooms;
    private boolean success;
    private String message;
    private List<String> players;
    private boolean started;
    private String targetPlayer;
    private String chatMessage;

    public RoomMessage(RoomAction action) {
        super("room");
        this.action = action;
    }

    public RoomAction getAction() {
        return action;
    }

    public void setAction(RoomAction action) {
        this.action = action;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public List<RoomInfo> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomInfo> rooms) {
        this.rooms = rooms;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public String getTargetPlayer() {
        return targetPlayer;
    }

    public void setTargetPlayer(String targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }

    public static class RoomInfo {
        private final String id;
        private final String name;
        private final int playerCount;
        private final int maxPlayers;
        private final boolean started;

        public RoomInfo(String id, String name, int playerCount, int maxPlayers, boolean started) {
            this.id = id;
            this.name = name;
            this.playerCount = playerCount;
            this.maxPlayers = maxPlayers;
            this.started = started;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getPlayerCount() {
            return playerCount;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public boolean isStarted() {
            return started;
        }
    }
}
