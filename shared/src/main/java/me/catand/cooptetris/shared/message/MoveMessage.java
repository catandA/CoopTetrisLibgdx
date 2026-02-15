package me.catand.cooptetris.shared.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MoveMessage extends NetworkMessage {
    public enum MoveType {
        LEFT,
        RIGHT,
        DOWN,
        DROP,
        ROTATE_CLOCKWISE
    }

    private MoveType moveType;

    public MoveMessage() {
        super("move");
    }

    public MoveMessage(MoveType moveType) {
        super("move");
        this.moveType = moveType;
    }
}
