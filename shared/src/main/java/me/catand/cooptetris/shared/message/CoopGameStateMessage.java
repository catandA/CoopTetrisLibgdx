package me.catand.cooptetris.shared.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 合作模式游戏状态消息
 * 包含共享的游戏板和所有玩家的物块信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CoopGameStateMessage extends NetworkMessage {
    // 共享游戏板状态
    private int[][] board;
    private int[][] boardColor; // 每个格子的颜色（由哪个玩家放置）
    private int score;
    private int level;
    private int lines;
    private boolean gameOver;

    // 每个玩家的物块状态
    private PlayerPieceState[] playerPieces;
    private int playerCount;

    public CoopGameStateMessage() {
        super("coopGameState");
    }

    /**
     * 玩家物块状态
     */
    @Data
    public static class PlayerPieceState {
        private int playerIndex;
        private int pieceType;
        private int x;
        private int y;
        private int rotation;
        private boolean active;

        public PlayerPieceState() {}

        public PlayerPieceState(int playerIndex, int pieceType, int x, int y, int rotation, boolean active) {
            this.playerIndex = playerIndex;
            this.pieceType = pieceType;
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.active = active;
        }
    }
}
