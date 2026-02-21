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
        private boolean spectatorLocked; // 观战是否被锁定
        private int spectatorCount; // 观战者数量

        public RoomInfo() {
        }

        public RoomInfo(String id, String name, int playerCount, int maxPlayers, boolean started) {
            this.id = id;
            this.name = name;
            this.playerCount = playerCount;
            this.maxPlayers = maxPlayers;
            this.started = started;
            this.displayPlayerCount = playerCount;
            this.spectatorLocked = false;
            this.spectatorCount = 0;
        }

        public RoomInfo(String id, String name, int playerCount, int maxPlayers, boolean started, int displayPlayerCount) {
            this.id = id;
            this.name = name;
            this.playerCount = playerCount;
            this.maxPlayers = maxPlayers;
            this.started = started;
            this.displayPlayerCount = displayPlayerCount;
            this.spectatorLocked = false;
            this.spectatorCount = 0;
        }

        public RoomInfo(String id, String name, int playerCount, int maxPlayers, boolean started, int displayPlayerCount, boolean spectatorLocked, int spectatorCount) {
            this.id = id;
            this.name = name;
            this.playerCount = playerCount;
            this.maxPlayers = maxPlayers;
            this.started = started;
            this.displayPlayerCount = displayPlayerCount;
            this.spectatorLocked = spectatorLocked;
            this.spectatorCount = spectatorCount;
        }

        /**
         * 检查是否可以作为普通玩家加入
         */
        public boolean canJoinAsPlayer() {
            return !started && displayPlayerCount < maxPlayers;
        }

        /**
         * 检查是否可以作为观战者加入
         * 房间已开始或满人时，只要观战未被锁定就可以加入观战
         */
        public boolean canJoinAsSpectator() {
            return !spectatorLocked;
        }

        /**
         * 获取显示状态文本
         */
        public String getStatusText(String waitingText, String fullText, String inGameText, String spectatorText) {
            if (started) {
                return inGameText;
            } else if (displayPlayerCount >= maxPlayers) {
                if (!spectatorLocked) {
                    return spectatorText;
                }
                return fullText;
            }
            return waitingText;
        }
    }
}
