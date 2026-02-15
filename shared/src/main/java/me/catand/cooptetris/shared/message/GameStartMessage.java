package me.catand.cooptetris.shared.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GameStartMessage extends NetworkMessage {
    private String roomId;
    private int playerCount;
    private int yourIndex;

    public GameStartMessage() {
        super("gameStart");
    }
}
