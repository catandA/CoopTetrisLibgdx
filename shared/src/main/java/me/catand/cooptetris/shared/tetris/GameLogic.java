package me.catand.cooptetris.shared.tetris;

import java.util.Random;

import lombok.Data;
import me.catand.cooptetris.shared.model.Tetromino;

@Data
public class GameLogic {
    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;

    private int[][] board;
    private int currentPiece;
    private int currentPieceX;
    private int currentPieceY;
    private int currentPieceRotation;
    private int nextPiece;
    private int score;
    private int level;
    private int lines;
    private final Random random;
    private boolean gameOver;

    public GameLogic() {
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        random = new Random();
        reset();
    }

    public void reset() {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                board[i][j] = 0;
            }
        }
        currentPiece = random.nextInt(7);
        nextPiece = random.nextInt(7);
        currentPieceX = BOARD_WIDTH / 2 - 2;
        currentPieceY = 0;
        currentPieceRotation = 0;
        score = 0;
        level = 1;
        lines = 0;
        gameOver = false;
    }

    public boolean moveLeft() {
        if (!gameOver && canMove(currentPieceX - 1, currentPieceY, currentPieceRotation)) {
            currentPieceX--;
            return true;
        }
        return false;
    }

    public boolean moveRight() {
        if (!gameOver && canMove(currentPieceX + 1, currentPieceY, currentPieceRotation)) {
            currentPieceX++;
            return true;
        }
        return false;
    }

    public boolean moveDown() {
        if (!gameOver && canMove(currentPieceX, currentPieceY + 1, currentPieceRotation)) {
            currentPieceY++;
            return true;
        } else if (!gameOver) {
            // 尝试锁定方块
            lockPiece();
            // 清除行
            clearLines();
            // 生成新方块
            spawnNewPiece();
            return false;
        }
        return false;
    }

    public void dropPiece() {
        while (moveDown()) ;
    }

    public boolean rotateClockwise() {
        // 尝试标准旋转
        int newRotation = (currentPieceRotation + 1) % 4;

        // 为不同类型的方块使用不同的旋转中心偏移
        int offsetX = 0;
        int offsetY = 0;

        // I型方块的旋转中心偏移
        if (currentPiece == 0) { // I型方块
            offsetX = 0;
            offsetY = 0;
        }

        // 尝试旋转
        if (!gameOver && canMove(currentPieceX + offsetX, currentPieceY + offsetY, newRotation)) {
            currentPieceX += offsetX;
            currentPieceY += offsetY;
            currentPieceRotation = newRotation;
            return true;
        }

        // 墙踢机制：尝试不同的位移
        int[][] kicks = {{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {-1, -1}, {1, -1}};
        for (int[] kick : kicks) {
            int kickX = currentPieceX + offsetX + kick[0];
            int kickY = currentPieceY + offsetY + kick[1];
            if (!gameOver && canMove(kickX, kickY, newRotation)) {
                currentPieceX = kickX;
                currentPieceY = kickY;
                currentPieceRotation = newRotation;
                return true;
            }
        }

        return false;
    }

    private boolean canMove(int x, int y, int rotation) {
        int[][] shape = Tetromino.SHAPES[currentPiece];

        // 根据旋转状态获取旋转后的形状
        int rotations = rotation % 4;
        for (int i = 0; i < rotations; i++) {
            shape = Tetromino.rotateClockwise(shape);
        }

        int size = shape.length;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (shape[i][j] != 0) {
                    int newX = x + j;
                    int newY = y + i;
                    if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) {
                        return false;
                    }
                    if (newY >= 0 && board[newY][newX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void lockPiece() {
        int[][] shape = Tetromino.SHAPES[currentPiece];

        // 根据当前旋转状态获取旋转后的形状
        int rotations = currentPieceRotation % 4;
        for (int i = 0; i < rotations; i++) {
            shape = Tetromino.rotateClockwise(shape);
        }

        int size = shape.length;

        // 确保至少有一个方块被锁定
        boolean pieceLocked = false;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (shape[i][j] != 0) {
                    int y = currentPieceY + i;
                    int x = currentPieceX + j;
                    if (y >= 0 && y < BOARD_HEIGHT && x >= 0 && x < BOARD_WIDTH) {
                        board[y][x] = currentPiece + 1;
                        pieceLocked = true;
                    }
                }
            }
        }

        // 如果没有方块被锁定，可能是因为方块完全在游戏板上方
        // 这种情况应该导致游戏结束
        if (!pieceLocked) {
            gameOver = true;
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
                }
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[0][j] = 0;
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

    private void spawnNewPiece() {
        currentPiece = nextPiece;
        nextPiece = random.nextInt(7);
        currentPieceX = BOARD_WIDTH / 2 - 2;
        currentPieceY = 0;
        // 随机生成旋转状态，使方块朝向不固定
        currentPieceRotation = random.nextInt(4);

        if (!canMove(currentPieceX, currentPieceY, currentPieceRotation)) {
            gameOver = true;
        }
    }

    public void updateFromMessage(int[][] board, int currentPiece, int currentPieceX, int currentPieceY, int currentPieceRotation, int nextPiece, int score, int level, int lines) {
        this.board = board;
        this.currentPiece = currentPiece;
        this.currentPieceX = currentPieceX;
        this.currentPieceY = currentPieceY;
        this.currentPieceRotation = currentPieceRotation;
        this.nextPiece = nextPiece;
        this.score = score;
        this.level = level;
        this.lines = lines;
    }

    /**
     * 获取指定方块和旋转状态的形状
     *
     * @param piece    方块类型
     * @param rotation 旋转状态
     * @return 方块的形状数组
     */
    public int[][] getPieceShape(int piece, int rotation) {
        int[][] shape = Tetromino.SHAPES[piece];

        // 根据旋转状态获取旋转后的形状
        int rotations = rotation % 4;
        for (int i = 0; i < rotations; i++) {
            shape = Tetromino.rotateClockwise(shape);
        }

        return shape;
    }
}
