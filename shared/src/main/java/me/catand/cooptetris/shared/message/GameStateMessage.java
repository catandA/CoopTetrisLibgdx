package me.catand.cooptetris.shared.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GameStateMessage extends NetworkMessage {
    private int[][] board;
    private int currentPiece;
    private int currentPieceX;
    private int currentPieceY;
    private int currentPieceRotation;
    private int nextPiece;
    private int score;
    private int level;
    private int lines;

    public GameStateMessage() {
        super("gameState");
    }
}
