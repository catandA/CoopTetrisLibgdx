package me.catand.cooptetris.shared.tetris;

import lombok.Data;
import me.catand.cooptetris.shared.util.Random;

/**
 * 多人合作模式游戏逻辑 - 重构版
 * - 场地宽度为15格（3*4+3）：4个玩家各3格 + 3个分隔格
 * - 支持4个出口（顶部4个位置）
 * - 每个出口3格宽，出口之间1格分隔
 * - 每个玩家有独立的颜色（蓝红绿黄）
 * - 每个玩家独立控制自己的物块
 * - 共用一个游戏板和游戏状态
 * - 只有两种三格砖块：直线型(I3)和小L型(L3)
 */
@Data
public class CoopGameLogic {
    public static final int BOARD_WIDTH = 15;  // 3*4+3 = 15格（4个玩家各3格 + 3个分隔格）
    public static final int BOARD_HEIGHT = 20;
    public static final int MAX_PLAYERS = 4;

    // 玩家颜色：蓝、红、绿、黄
    public static final int[] PLAYER_COLORS = {0, 1, 2, 3}; // 颜色索引

    private int[][] board;
    private int[][] boardColor; // 记录每个格子的颜色（由哪个玩家放置）

    // 每个玩家的当前物块
    private PlayerPiece[] playerPieces;

    private int score;
    private int level;
    private int lines;
    private boolean gameOver;
    private long randomSeed;
    private int activePlayerCount; // 实际活跃玩家数量

    // 出口位置（每个出口3格宽，出口之间1格分隔）
    // 布局: [P1:0-2] [分隔:3] [P2:4-6] [分隔:7] [P3:8-10] [分隔:11] [P4:12-14]
    // 出口中心X坐标（物块生成位置）
    public static final int[] EXIT_POSITIONS = {0, 4, 8, 12}; // 每个出口的起始X坐标

    // 玩家分配顺序（从中心向两边）：2(红), 3(绿), 1(蓝), 4(黄)
    // 对应玩家索引：1, 2, 0, 3
    public static final int[] PLAYER_ASSIGNMENT_ORDER = {1, 2, 0, 3};

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
        for (int i = 0; i < MAX_PLAYERS; i++) {
            playerPieces[i] = new PlayerPiece();
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

        // 为每个活跃玩家初始化物块
        // 根据 PLAYER_ASSIGNMENT_ORDER 生成对应玩家索引的物块
        for (int i = 0; i < MAX_PLAYERS; i++) {
            playerPieces[i].setActive(false);
        }
        for (int i = 0; i < activePlayerCount && i < MAX_PLAYERS; i++) {
            int assignedPlayerIndex = PLAYER_ASSIGNMENT_ORDER[i];
            spawnNewPieceForPlayer(assignedPlayerIndex);
        }

        score = 0;
        level = 1;
        lines = 0;
        gameOver = false;
    }

    /**
     * 为指定玩家生成新物块
     * 合作模式只有两种三格砖块
     */
    public void spawnNewPieceForPlayer(int playerIndex) {
        PlayerPiece piece = playerPieces[playerIndex];
        piece.setPieceType(Random.Int(PIECE_COUNT)); // 只有两种砖块: 0或1
        piece.setX(EXIT_POSITIONS[playerIndex]); // 在对应出口起始位置生成
        piece.setY(0);
        piece.setRotation(Random.Int(4));
        piece.setActive(true);

        // 检查是否可以放置
        if (!canMove(playerIndex, piece.getX(), piece.getY(), piece.getRotation())) {
            // 该玩家游戏结束
            piece.setActive(false);
            checkAllPlayersGameOver();
        }
    }

    /**
     * 检查是否所有玩家都游戏结束
     */
    private void checkAllPlayersGameOver() {
        boolean allInactive = true;
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (playerPieces[i].isActive()) {
                allInactive = false;
                break;
            }
        }
        if (allInactive) {
            gameOver = true;
        }
    }

    /**
     * 玩家移动控制
     */
    public boolean moveLeft(int playerIndex) {
        if (gameOver || !playerPieces[playerIndex].isActive()) return false;

        PlayerPiece piece = playerPieces[playerIndex];
        if (canMove(playerIndex, piece.getX() - 1, piece.getY(), piece.getRotation())) {
            piece.setX(piece.getX() - 1);
            return true;
        }
        return false;
    }

    public boolean moveRight(int playerIndex) {
        if (gameOver || !playerPieces[playerIndex].isActive()) return false;

        PlayerPiece piece = playerPieces[playerIndex];
        if (canMove(playerIndex, piece.getX() + 1, piece.getY(), piece.getRotation())) {
            piece.setX(piece.getX() + 1);
            return true;
        }
        return false;
    }

    public boolean moveDown(int playerIndex) {
        if (gameOver || !playerPieces[playerIndex].isActive()) return false;

        PlayerPiece piece = playerPieces[playerIndex];
        // 检查是否可以向下移动（检查边界、游戏板和其他玩家）
        if (canMove(playerIndex, piece.getX(), piece.getY() + 1, piece.getRotation())) {
            piece.setY(piece.getY() + 1);
            return true;
        } else {
            // 检查是否是因为碰到游戏板或边界而不能移动
            if (!canMoveToBoard(playerIndex, piece.getX(), piece.getY() + 1, piece.getRotation())) {
                // 锁定物块（只有碰到游戏板或边界时才锁定）
                lockPiece(playerIndex);
                clearLines();
                spawnNewPieceForPlayer(playerIndex);
            }
            // 如果是因为碰到其他玩家，则不锁定，只是停止下落
            return false;
        }
    }

    /**
     * 检查是否可以移动到指定位置（只检查边界和游戏板，不检查其他玩家物块）
     * 用于判断是否应该锁定物块
     */
    private boolean canMoveToBoard(int playerIndex, int x, int y, int rotation) {
        PlayerPiece piece = playerPieces[playerIndex];
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

    public void dropPiece(int playerIndex) {
        while (moveDown(playerIndex)) ;
    }

    public boolean rotateClockwise(int playerIndex) {
        if (gameOver || !playerPieces[playerIndex].isActive()) return false;

        PlayerPiece piece = playerPieces[playerIndex];
        int newRotation = (piece.getRotation() + 1) % 4;

        // 尝试标准旋转
        if (canMove(playerIndex, piece.getX(), piece.getY(), newRotation)) {
            piece.setRotation(newRotation);
            return true;
        }

        // 墙踢机制
        int[][] kicks = {{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {-1, -1}, {1, -1}};
        for (int[] kick : kicks) {
            int kickX = piece.getX() + kick[0];
            int kickY = piece.getY() + kick[1];
            if (canMove(playerIndex, kickX, kickY, newRotation)) {
                piece.setX(kickX);
                piece.setY(kickY);
                piece.setRotation(newRotation);
                return true;
            }
        }

        return false;
    }

    private boolean canMove(int playerIndex, int x, int y, int rotation) {
        PlayerPiece piece = playerPieces[playerIndex];
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
                    // 检查与其他玩家物块的碰撞
                    for (int otherPlayerIndex = 0; otherPlayerIndex < MAX_PLAYERS; otherPlayerIndex++) {
                        if (otherPlayerIndex != playerIndex && playerPieces[otherPlayerIndex].isActive()) {
                            PlayerPiece otherPiece = playerPieces[otherPlayerIndex];
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

    private void lockPiece(int playerIndex) {
        PlayerPiece piece = playerPieces[playerIndex];
        int[][] shape = getCoopShape(piece.getPieceType(), piece.getRotation());

        int size = shape.length;
        boolean pieceLocked = false;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (shape[i][j] != 0) {
                    int y = piece.getY() + i;
                    int x = piece.getX() + j;
                    if (y >= 0 && y < BOARD_HEIGHT && x >= 0 && x < BOARD_WIDTH) {
                        board[y][x] = piece.getPieceType() + 1;
                        boardColor[y][x] = playerIndex; // 记录是哪个玩家放置的
                        pieceLocked = true;
                    }
                }
            }
        }

        if (!pieceLocked) {
            piece.setActive(false);
            checkAllPlayersGameOver();
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
     * 获取指定玩家的当前物块
     */
    public PlayerPiece getPlayerPiece(int playerIndex) {
        return playerPieces[playerIndex];
    }

    /**
     * 获取指定位置的颜色（玩家索引）
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
