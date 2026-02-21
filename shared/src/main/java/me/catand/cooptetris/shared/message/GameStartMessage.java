package me.catand.cooptetris.shared.message;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.catand.cooptetris.shared.tetris.GameMode;

/**
 * 游戏开始消息
 * 颜色由玩家选择，跟随玩家移动
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GameStartMessage extends NetworkMessage {
	private String roomId;
	private int playerCount;
	private int yourIndex;      // 玩家的槽位索引 (0-3)
	private int yourColorIndex; // 玩家选择的颜色索引 (0-3)
	private long seed;          // 游戏随机数种子，用于同步方块生成
	private GameMode gameMode;  // 游戏模式
	private List<String> playerNames; // 玩家名字列表（按槽位索引 0-3，空字符串表示该槽位无人）
	private List<Integer> playerColors; // 玩家颜色列表（按槽位索引 0-3，-1表示该槽位无人）

	public GameStartMessage() {
		super("gameStart");
	}
}
