package me.catand.cooptetris.shared.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 合作模式游戏状态消息
 * 包含共享的游戏板和所有玩家的物块信息
 * 颜色由玩家选择，跟随玩家移动
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CoopGameStateMessage extends NetworkMessage {
	// 共享游戏板状态
	private int[][] board;
	private int[][] boardColor; // 每个格子的颜色（玩家选择的颜色索引 0-3）
	private int score;
	private int level;
	private int lines;
	private boolean gameOver;

	// 每个槽位的物块状态（索引对应槽位 0-3）
	private PlayerPieceState[] playerPieces;
	private int playerCount;

	// 每个槽位玩家的颜色选择（索引对应槽位 0-3，-1表示空槽位）
	private int[] slotColorIndices;

	public CoopGameStateMessage() {
		super("coopGameState");
	}

	/**
	 * 玩家物块状态
	 */
	@Data
	public static class PlayerPieceState {
		private int slotIndex;      // 槽位索引 (0-3)，颜色由此决定
		private int pieceType;
		private int x;
		private int y;
		private int rotation;
		private boolean active;

		public PlayerPieceState() {}

		public PlayerPieceState(int slotIndex, int pieceType, int x, int y, int rotation, boolean active) {
			this.slotIndex = slotIndex;
			this.pieceType = pieceType;
			this.x = x;
			this.y = y;
			this.rotation = rotation;
			this.active = active;
		}
	}
}
