package me.catand.cooptetris.shared.model;

public class Tetromino {
    public static final int I = 0;
    public static final int J = 1;
    public static final int L = 2;
    public static final int O = 3;
    public static final int S = 4;
    public static final int T = 5;
    public static final int Z = 6;

    public static final int[][][] SHAPES = {
        // I piece
        {
            {0, 0, 0, 0},
            {1, 1, 1, 1},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
        },
        // J piece
        {
            {1, 0, 0},
            {1, 1, 1},
            {0, 0, 0}
        },
        // L piece
        {
            {0, 0, 1},
            {1, 1, 1},
            {0, 0, 0}
        },
        // O piece
        {
            {1, 1},
            {1, 1}
        },
        // S piece
        {
            {0, 1, 1},
            {1, 1, 0},
            {0, 0, 0}
        },
        // T piece
        {
            {0, 1, 0},
            {1, 1, 1},
            {0, 0, 0}
        },
        // Z piece
        {
            {1, 1, 0},
            {0, 1, 1},
            {0, 0, 0}
        }
    };

    public static final int[] COLORS = {
        0x00FFFF, // Cyan (I)
        0x0000FF, // Blue (J)
        0xFFA500, // Orange (L)
        0xFFFF00, // Yellow (O)
        0x00FF00, // Green (S)
        0x800080, // Purple (T)
        0xFF0000  // Red (Z)
    };

    public static int[][] rotateClockwise(int[][] piece) {
        int size = piece.length;
        int[][] rotated = new int[size][size];

        // 标准的顺时针旋转90度，旋转中心点为方块中心
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                rotated[j][size - 1 - i] = piece[i][j];
            }
        }

        return rotated;
    }
}
