package me.catand.cooptetris.shared.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.catand.cooptetris.shared.tetris.GameMode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GameStartMessage extends NetworkMessage {
    private String roomId;
    private int playerCount;
    private int yourIndex;
    private long seed; // 游戏随机数种子，用于同步方块生成
    private GameMode gameMode; // 游戏模式

    public GameStartMessage() {
        super("gameStart");
    }
}
