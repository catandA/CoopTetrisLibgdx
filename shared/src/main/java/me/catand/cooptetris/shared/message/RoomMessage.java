package me.catand.cooptetris.shared.message;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.catand.cooptetris.shared.tetris.GameMode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RoomMessage extends NetworkMessage {
    public enum RoomAction {
        CREATE,
        JOIN,
        LEAVE,
        LIST,
        START,
        STATUS,
        KICK,
        CHAT,
        SET_GAME_MODE
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
    private boolean isHost;
    private GameMode gameMode;

    public RoomMessage() {
        super("room");
    }

    public RoomMessage(RoomAction action) {
        super("room");
        this.action = action;
    }

    @Data
    public static class RoomInfo {
        private String id;
        private String name;
        private int playerCount;
        private int maxPlayers;
        private boolean started;
        private int displayPlayerCount; // 显示的玩家数量（包含锁定的槽位）

        public RoomInfo() {
        }

        public RoomInfo(String id, String name, int playerCount, int maxPlayers, boolean started) {
            this.id = id;
            this.name = name;
            this.playerCount = playerCount;
            this.maxPlayers = maxPlayers;
            this.started = started;
            this.displayPlayerCount = playerCount;
        }

        public RoomInfo(String id, String name, int playerCount, int maxPlayers, boolean started, int displayPlayerCount) {
            this.id = id;
            this.name = name;
            this.playerCount = playerCount;
            this.maxPlayers = maxPlayers;
            this.started = started;
            this.displayPlayerCount = displayPlayerCount;
        }
    }
}
