package me.catand.cooptetris.shared.message;

public class MoveMessage extends NetworkMessage {
    public enum MoveType {
        LEFT,
        RIGHT,
        DOWN,
        DROP,
        ROTATE_CLOCKWISE
    }

    private MoveType moveType;

    public MoveMessage(MoveType moveType) {
        super("move");
        this.moveType = moveType;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }
}
