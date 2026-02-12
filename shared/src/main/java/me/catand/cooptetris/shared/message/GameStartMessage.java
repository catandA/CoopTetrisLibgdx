package me.catand.cooptetris.shared.message;

public class GameStartMessage extends NetworkMessage {
    private String roomId;
    private int playerCount;
    private int yourIndex;

    public GameStartMessage() {
        super("gameStart");
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getYourIndex() {
        return yourIndex;
    }

    public void setYourIndex(int yourIndex) {
        this.yourIndex = yourIndex;
    }
}
