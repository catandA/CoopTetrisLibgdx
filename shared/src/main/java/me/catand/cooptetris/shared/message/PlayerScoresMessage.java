package me.catand.cooptetris.shared.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 玩家分数同步消息 - 用于PVP模式同步所有玩家的分数信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PlayerScoresMessage extends NetworkMessage {

    /**
     * 单个玩家的分数信息
     */
    @Data
    public static class PlayerScore {
        private int playerIndex;
        private String playerName;
        private int score;
        private int lines;
        private int level;
        private boolean gameOver;

        public PlayerScore() {}

        public PlayerScore(int playerIndex, String playerName, int score, int lines, int level, boolean gameOver) {
            this.playerIndex = playerIndex;
            this.playerName = playerName;
            this.score = score;
            this.lines = lines;
            this.level = level;
            this.gameOver = gameOver;
        }
    }

    private List<PlayerScore> playerScores;
    private int yourIndex; // 当前接收消息的玩家的索引

    public PlayerScoresMessage() {
        super("playerScores");
    }

    public PlayerScoresMessage(List<PlayerScore> playerScores, int yourIndex) {
        super("playerScores");
        this.playerScores = playerScores;
        this.yourIndex = yourIndex;
    }
}
