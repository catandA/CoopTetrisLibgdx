package me.catand.cooptetris.shared.tetris;

import lombok.Data;
import me.catand.cooptetris.shared.util.Random;

/**
 * 多人合作模式游戏逻辑 - 颜色跟随玩家版
 * - 场地宽度为15格（3*4+3）：4个槽位各3格 + 3个分隔格
 * - 支持4个出口（顶部4个位置）
 * - 每个出口3格宽，出口之间1格分隔
 * - 玩家可以选择颜色，颜色跟随玩家移动
 * - 每个玩家独立控制自己的物块
 * - 共用一个游戏板和游戏状态
 * - 只有两种三格砖块：直线型(I3)和小L型(L3)
 *
 * 设计：
 * - 槽位索引(0-3) = 出口位置
 * - 颜色由玩家选择，存储在 slotColorIndices 数组中
 * - 玩家移动槽位时，颜色跟随移动
 */
@Data
public class CoopGameLogic {
	public static final int BOARD_WIDTH = 15;  // 3*4+3 = 15格（4个槽位各3格 + 3个分隔格）
	public static final int BOARD_HEIGHT = 20;
	public static final int MAX_PLAYERS = 4;

	// 可用颜色：蓝、红、绿、黄
	public static final int[] AVAILABLE_COLORS = {0, 1, 2, 3};

	private int[][] board;
	private int[][] boardColor; // 记录每个格子的颜色（玩家选择的颜色索引 0-3）

	// 每个槽位的当前物块（索引0-3对应槽位0-3）
	private PlayerPiece[] playerPieces;

	private int score;
	private int level;
	private int lines;
	private boolean gameOver;
	private long randomSeed;
	private int activePlayerCount; // 实际活跃玩家数量

	// 槽位激活状态（哪些槽位有玩家）
	private boolean[] slotActive;

	// 每个槽位玩家的颜色选择（-1表示未选择）
	private int[] slotColorIndices;

	// 出口位置（每个出口3格宽，出口之间1格分隔）
	// 布局: [槽位0:0-2] [分隔:3] [槽位1:4-6] [分隔:7] [槽位2:8-10] [分隔:11] [槽位3:12-14]
	// 出口起始X坐标（物块生成位置）
	public static final int[] EXIT_POSITIONS = {0, 4, 8, 12}; // 每个出口的起始X坐标

	// 合作模式专用砖块类型
	public static final int PIECE_I3 = 0;  // 直线型三格
	public static final int PIECE_L3 = 1;  // 小L型三格
	public static final int PIECE_COUNT = 2; // 只有两种砖块

	// 合作模式专用砖块形状定义（3x3矩阵）
	public static final int[][][] COOP_SHAPES = {
		// I3: 直线型三格
		{
			{0, 0, 0},
			{1, 1, 1},
			{0, 0, 0}
		},
		// L3: 小L型三格 (形状: 10 / 11)
		{
			{1, 0, 0},
			{1, 1, 0},
			{0, 0, 0}
		}
	};

	public CoopGameLogic() {
		board = new int[BOARD_HEIGHT][BOARD_WIDTH];
		boardColor = new int[BOARD_HEIGHT][BOARD_WIDTH];
		playerPieces = new PlayerPiece[MAX_PLAYERS];
		slotActive = new boolean[MAX_PLAYERS];
		slotColorIndices = new int[MAX_PLAYERS];
		for (int i = 0; i < MAX_PLAYERS; i++) {
			playerPieces[i] = new PlayerPiece();
			slotActive[i] = false;
			slotColorIndices[i] = -1; // 初始未选择颜色
		}
		randomSeed = 0;
		reset();
	}

	public void reset(long seed, int playerCount) {
		this.randomSeed = seed;
		this.activePlayerCount = Math.min(playerCount, MAX_PLAYERS);
		Random.pushGenerator(seed);
		try {
			reset();
		} finally {
			Random.popGenerator();
		}
	}

	public void reset(long seed) {
		reset(seed, MAX_PLAYERS);
	}

	public void reset() {
		for (int i = 0; i < BOARD_HEIGHT; i++) {
			for (int j = 0; j < BOARD_WIDTH; j++) {
				board[i][j] = 0;
				boardColor[i][j] = -1;
			}
		}

		// 重置所有槽位为未激活
		for (int i = 0; i < MAX_PLAYERS; i++) {
			playerPieces[i].setActive(false);
			slotActive[i] = false;
			slotColorIndices[i] = -1; // 重置颜色选择
		}

		score = 0;
		level = 1;
		lines = 0;
		gameOver = false;
	}

	/**
	 * 激活槽位并为该槽位生成物块
	 * 由服务器在游戏开始时调用，传入实际有玩家的槽位索引
	 * @param slotIndex 要激活的槽位索引(0-3)
	 */
	public void activateSlot(int slotIndex) {
		if (slotIndex >= 0 && slotIndex < MAX_PLAYERS) {
			slotActive[slotIndex] = true;
			spawnNewPieceForSlot(slotIndex);
		}
	}

	/**
	 * 设置槽位玩家的颜色选择
	 * @param slotIndex 槽位索引(0-3)
	 * @param colorIndex 颜色索引(0-3)
	 */
	public void setSlotColorIndex(int slotIndex, int colorIndex) {
		if (slotIndex >= 0 && slotIndex < MAX_PLAYERS && colorIndex >= 0 && colorIndex < 4) {
			slotColorIndices[slotIndex] = colorIndex;
		}
	}

	/**
	 * 获取槽位玩家的颜色选择
	 * @param slotIndex 槽位索引(0-3)
	 * @return 颜色索引(0-3)，-1表示未选择
	 */
	public int getSlotColorIndex(int slotIndex) {
		if (slotIndex >= 0 && slotIndex < MAX_PLAYERS) {
			return slotColorIndices[slotIndex];
		}
		return -1;
	}

	/**
	 * 获取所有槽位的颜色选择数组
	 */
	public int[] getSlotColorIndices() {
		return slotColorIndices;
	}

	/**
	 * 检查槽位是否激活
	 */
	public boolean isSlotActive(int slotIndex) {
		return slotIndex >= 0 && slotIndex < MAX_PLAYERS && slotActive[slotIndex];
	}

	/**
	 * 获取槽位玩家的颜色索引
	 * 如果该槽位玩家未选择颜色，返回槽位索引作为默认颜色
	 */
	public int getSlotColor(int slotIndex) {
		if (slotIndex >= 0 && slotIndex < MAX_PLAYERS) {
			if (slotColorIndices[slotIndex] >= 0) {
				return slotColorIndices[slotIndex]; // 返回玩家选择的颜色
			}
			return slotIndex; // 默认使用槽位索引作为颜色
		}
		return -1;
	}

	/**
	 * 为指定槽位生成新物块
	 * 合作模式只有两种三格砖块
	 */
	public void spawnNewPieceForSlot(int slotIndex) {
		PlayerPiece piece = playerPieces[slotIndex];
		piece.setPieceType(Random.Int(PIECE_COUNT)); // 只有两种砖块: 0或1
		piece.setX(EXIT_POSITIONS[slotIndex]); // 在对应出口起始位置生成
		piece.setY(0);
		piece.setRotation(Random.Int(4));
		piece.setActive(true);

		// 检查是否可以放置
		if (!canMove(slotIndex, piece.getX(), piece.getY(), piece.getRotation())) {
			// 该槽位游戏结束
			piece.setActive(false);
			checkAllSlotsGameOver();
		}
	}

	/**
	 * 检查是否所有槽位都游戏结束
	 */
	private void checkAllSlotsGameOver() {
		boolean allInactive = true;
		for (int i = 0; i < MAX_PLAYERS; i++) {
			if (slotActive[i] && playerPieces[i].isActive()) {
				allInactive = false;
				break;
			}
		}
		if (allInactive) {
			gameOver = true;
		}
	}

	/**
	 * 玩家移动控制（通过槽位索引）
	 */
	public boolean moveLeft(int slotIndex) {
		if (gameOver || !isSlotActive(slotIndex) || !playerPieces[slotIndex].isActive()) return false;

		PlayerPiece piece = playerPieces[slotIndex];
		if (canMove(slotIndex, piece.getX() - 1, piece.getY(), piece.getRotation())) {
			piece.setX(piece.getX() - 1);
			return true;
		}
		return false;
	}

	public boolean moveRight(int slotIndex) {
		if (gameOver || !isSlotActive(slotIndex) || !playerPieces[slotIndex].isActive()) return false;

		PlayerPiece piece = playerPieces[slotIndex];
		if (canMove(slotIndex, piece.getX() + 1, piece.getY(), piece.getRotation())) {
			piece.setX(piece.getX() + 1);
			return true;
		}
		return false;
	}

	public boolean moveDown(int slotIndex) {
		if (gameOver || !isSlotActive(slotIndex) || !playerPieces[slotIndex].isActive()) return false;

		PlayerPiece piece = playerPieces[slotIndex];
		// 检查是否可以向下移动（检查边界、游戏板和其他玩家）
		if (canMove(slotIndex, piece.getX(), piece.getY() + 1, piece.getRotation())) {
			piece.setY(piece.getY() + 1);
			return true;
		} else {
			// 检查是否是因为碰到游戏板或边界而不能移动
			if (!canMoveToBoard(slotIndex, piece.getX(), piece.getY() + 1, piece.getRotation())) {
				// 锁定物块（只有碰到游戏板或边界时才锁定）
				lockPiece(slotIndex);
				clearLines();
				spawnNewPieceForSlot(slotIndex);
			}
			// 如果是因为碰到其他玩家，则不锁定，只是停止下落
			return false;
		}
	}

	/**
	 * 检查是否可以移动到指定位置（只检查边界和游戏板，不检查其他玩家物块）
	 * 用于判断是否应该锁定物块
	 */
	private boolean canMoveToBoard(int slotIndex, int x, int y, int rotation) {
		PlayerPiece piece = playerPieces[slotIndex];
		int[][] shape = getCoopShape(piece.getPieceType(), rotation);

		int size = shape.length;

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (shape[i][j] != 0) {
					int newX = x + j;
					int newY = y + i;
					// 检查边界
					if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) {
						return false;
					}
					// 检查与游戏板的碰撞
					if (newY >= 0 && board[newY][newX] != 0) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public void dropPiece(int slotIndex) {
		while (moveDown(slotIndex)) ;
	}

	public boolean rotateClockwise(int slotIndex) {
		if (gameOver || !isSlotActive(slotIndex) || !playerPieces[slotIndex].isActive()) return false;

		PlayerPiece piece = playerPieces[slotIndex];
		int newRotation = (piece.getRotation() + 1) % 4;

		// 尝试标准旋转
		if (canMove(slotIndex, piece.getX(), piece.getY(), newRotation)) {
			piece.setRotation(newRotation);
			return true;
		}

		// 墙踢机制
		int[][] kicks = {{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {-1, -1}, {1, -1}};
		for (int[] kick : kicks) {
			int kickX = piece.getX() + kick[0];
			int kickY = piece.getY() + kick[1];
			if (canMove(slotIndex, kickX, kickY, newRotation)) {
				piece.setX(kickX);
				piece.setY(kickY);
				piece.setRotation(newRotation);
				return true;
			}
		}

		return false;
	}

	private boolean canMove(int slotIndex, int x, int y, int rotation) {
		PlayerPiece piece = playerPieces[slotIndex];
		int[][] shape = getCoopShape(piece.getPieceType(), rotation);

		int size = shape.length;

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (shape[i][j] != 0) {
					int newX = x + j;
					int newY = y + i;
					// 检查边界
					if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) {
						return false;
					}
					// 检查与游戏板的碰撞
					if (newY >= 0 && board[newY][newX] != 0) {
						return false;
					}
					// 检查与其他槽位物块的碰撞
					for (int otherSlotIndex = 0; otherSlotIndex < MAX_PLAYERS; otherSlotIndex++) {
						if (otherSlotIndex != slotIndex && slotActive[otherSlotIndex] && playerPieces[otherSlotIndex].isActive()) {
							PlayerPiece otherPiece = playerPieces[otherSlotIndex];
							int[][] otherShape = getCoopShape(otherPiece.getPieceType(), otherPiece.getRotation());
							int otherSize = otherShape.length;
							for (int oy = 0; oy < otherSize; oy++) {
								for (int ox = 0; ox < otherSize; ox++) {
									if (otherShape[oy][ox] != 0) {
										int otherX = otherPiece.getX() + ox;
										int otherY = otherPiece.getY() + oy;
										if (newX == otherX && newY == otherY) {
											return false;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	private void lockPiece(int slotIndex) {
		PlayerPiece piece = playerPieces[slotIndex];
		int[][] shape = getCoopShape(piece.getPieceType(), piece.getRotation());

		int size = shape.length;
		boolean pieceLocked = false;

		// 获取该槽位玩家选择的颜色
		int colorIndex = getSlotColor(slotIndex);

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (shape[i][j] != 0) {
					int y = piece.getY() + i;
					int x = piece.getX() + j;
					if (y >= 0 && y < BOARD_HEIGHT && x >= 0 && x < BOARD_WIDTH) {
						board[y][x] = piece.getPieceType() + 1;
						// 使用玩家选择的颜色记录
						boardColor[y][x] = colorIndex;
						pieceLocked = true;
					}
				}
			}
		}

		if (!pieceLocked) {
			piece.setActive(false);
			checkAllSlotsGameOver();
		}
	}

	private void clearLines() {
		int linesCleared = 0;

		for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
			boolean lineFull = true;
			for (int j = 0; j < BOARD_WIDTH; j++) {
				if (board[i][j] == 0) {
					lineFull = false;
					break;
				}
			}

			if (lineFull) {
				linesCleared++;
				for (int k = i; k > 0; k--) {
					System.arraycopy(board[k - 1], 0, board[k], 0, BOARD_WIDTH);
					System.arraycopy(boardColor[k - 1], 0, boardColor[k], 0, BOARD_WIDTH);
				}
				for (int j = 0; j < BOARD_WIDTH; j++) {
					board[0][j] = 0;
					boardColor[0][j] = -1;
				}
				i++;
			}
		}

		if (linesCleared > 0) {
			updateScore(linesCleared);
		}
	}

	private void updateScore(int linesCleared) {
		int[] lineScores = {0, 100, 300, 500, 800};
		score += lineScores[linesCleared] * level;
		lines += linesCleared;
		level = lines / 10 + 1;
	}

	/**
	 * 获取指定槽位的当前物块
	 */
	public PlayerPiece getPlayerPiece(int slotIndex) {
		if (slotIndex >= 0 && slotIndex < MAX_PLAYERS) {
			return playerPieces[slotIndex];
		}
		return null;
	}

	/**
	 * 获取指定位置的颜色（槽位颜色索引 0-3）
	 */
	public int getCellColor(int x, int y) {
		if (y >= 0 && y < BOARD_HEIGHT && x >= 0 && x < BOARD_WIDTH) {
			return boardColor[y][x];
		}
		return -1;
	}

	/**
	 * 获取指定类型和旋转状态的物块形状（合作模式专用）
	 */
	public int[][] getPieceShape(int pieceType, int rotation) {
		return getCoopShape(pieceType, rotation);
	}

	/**
	 * 获取合作模式砖块形状（带旋转）
	 */
	private int[][] getCoopShape(int pieceType, int rotation) {
		// 确保pieceType在有效范围内
		if (pieceType < 0 || pieceType >= PIECE_COUNT) {
			pieceType = 0;
		}

		int[][] shape = copyShape(COOP_SHAPES[pieceType]);
		int rotations = rotation % 4;
		for (int i = 0; i < rotations; i++) {
			shape = rotateShapeClockwise(shape);
		}
		return shape;
	}

	/**
	 * 复制形状矩阵
	 */
	private int[][] copyShape(int[][] original) {
		int size = original.length;
		int[][] copy = new int[size][size];
		for (int i = 0; i < size; i++) {
			System.arraycopy(original[i], 0, copy[i], 0, size);
		}
		return copy;
	}

	/**
	 * 顺时针旋转形状矩阵
	 */
	private int[][] rotateShapeClockwise(int[][] piece) {
		int size = piece.length;
		int[][] rotated = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				rotated[j][size - 1 - i] = piece[i][j];
			}
		}
		return rotated;
	}

	/**
	 * 玩家物块类
	 */
	@Data
	public static class PlayerPiece {
		private int pieceType;
		private int x;
		private int y;
		private int rotation;
		private boolean active;

		public PlayerPiece() {
			this.pieceType = 0;
			this.x = 0;
			this.y = 0;
			this.rotation = 0;
			this.active = false;
		}
	}
}
