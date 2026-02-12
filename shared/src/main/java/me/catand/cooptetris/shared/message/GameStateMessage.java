package me.catand.cooptetris.shared.message;

import java.util.List;

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
    
    public int[][] getBoard() {
        return board;
    }
    
    public void setBoard(int[][] board) {
        this.board = board;
    }
    
    public int getCurrentPiece() {
        return currentPiece;
    }
    
    public void setCurrentPiece(int currentPiece) {
        this.currentPiece = currentPiece;
    }
    
    public int getCurrentPieceX() {
        return currentPieceX;
    }
    
    public void setCurrentPieceX(int currentPieceX) {
        this.currentPieceX = currentPieceX;
    }
    
    public int getCurrentPieceY() {
        return currentPieceY;
    }
    
    public void setCurrentPieceY(int currentPieceY) {
        this.currentPieceY = currentPieceY;
    }
    
    public int getCurrentPieceRotation() {
        return currentPieceRotation;
    }
    
    public void setCurrentPieceRotation(int currentPieceRotation) {
        this.currentPieceRotation = currentPieceRotation;
    }
    
    public int getNextPiece() {
        return nextPiece;
    }
    
    public void setNextPiece(int nextPiece) {
        this.nextPiece = nextPiece;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public int getLines() {
        return lines;
    }
    
    public void setLines(int lines) {
        this.lines = lines;
    }
}
