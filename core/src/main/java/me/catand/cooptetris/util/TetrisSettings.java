package me.catand.cooptetris.util;

import me.catand.cooptetris.input.InputBinding;

public class TetrisSettings extends GameSettings {

    // 游戏难度
    public static final String KEY_DIFFICULTY = "difficulty";

    // 控制键位 - 存储为枚举名称
    public static final String KEY_LEFT_KEY = "left_key";
    public static final String KEY_RIGHT_KEY = "right_key";
    public static final String KEY_DOWN_KEY = "down_key";
    public static final String KEY_ROTATE_KEY = "rotate_key";
    public static final String KEY_DROP_KEY = "drop_key";

    // 第二套控制键位
    public static final String KEY_LEFT_KEY2 = "left_key2";
    public static final String KEY_RIGHT_KEY2 = "right_key2";
    public static final String KEY_DOWN_KEY2 = "down_key2";
    public static final String KEY_ROTATE_KEY2 = "rotate_key2";
    public static final String KEY_DROP_KEY2 = "drop_key2";

    // 网络设置
    public static final String KEY_DEFAULT_HOST = "default_host";
    public static final String KEY_DEFAULT_PORT = "default_port";

    // 语言设置
    public static final String KEY_LANGUAGE = "language";

    // 玩家名称
    public static final String KEY_PLAYER_NAME = "player_name";

    // 全屏设置
    public static final String KEY_FULLSCREEN = "fullscreen";

    // 窗口分辨率设置
    public static final String KEY_WINDOW_WIDTH = "window_width";
    public static final String KEY_WINDOW_HEIGHT = "window_height";
    public static final String KEY_WINDOW_MAXIMIZED = "window_maximized";

    // ==================== 游戏难度设置 ====================

    public static void difficulty(int value) {
        put(KEY_DIFFICULTY, value);
    }

    public static int difficulty() {
        return getInt(KEY_DIFFICULTY, 1, 1, 3);
    }

    // ==================== 第一套控制键位设置 ====================

    public static void leftKey(InputBinding key) {
        put(KEY_LEFT_KEY, key != null ? key.name() : InputBinding.LEFT.name());
    }

    public static InputBinding leftKey() {
        return getInputBinding(KEY_LEFT_KEY, InputBinding.LEFT);
    }

    public static void rightKey(InputBinding key) {
        put(KEY_RIGHT_KEY, key != null ? key.name() : InputBinding.RIGHT.name());
    }

    public static InputBinding rightKey() {
        return getInputBinding(KEY_RIGHT_KEY, InputBinding.RIGHT);
    }

    public static void downKey(InputBinding key) {
        put(KEY_DOWN_KEY, key != null ? key.name() : InputBinding.DOWN.name());
    }

    public static InputBinding downKey() {
        return getInputBinding(KEY_DOWN_KEY, InputBinding.DOWN);
    }

    public static void rotateKey(InputBinding key) {
        put(KEY_ROTATE_KEY, key != null ? key.name() : InputBinding.UP.name());
    }

    public static InputBinding rotateKey() {
        return getInputBinding(KEY_ROTATE_KEY, InputBinding.UP);
    }

    public static void dropKey(InputBinding key) {
        put(KEY_DROP_KEY, key != null ? key.name() : InputBinding.SPACE.name());
    }

    public static InputBinding dropKey() {
        return getInputBinding(KEY_DROP_KEY, InputBinding.SPACE);
    }

    // ==================== 第二套控制键位设置 ====================

    public static void leftKey2(InputBinding key) {
        put(KEY_LEFT_KEY2, key != null ? key.name() : InputBinding.A.name());
    }

    public static InputBinding leftKey2() {
        return getInputBinding(KEY_LEFT_KEY2, InputBinding.A);
    }

    public static void rightKey2(InputBinding key) {
        put(KEY_RIGHT_KEY2, key != null ? key.name() : InputBinding.D.name());
    }

    public static InputBinding rightKey2() {
        return getInputBinding(KEY_RIGHT_KEY2, InputBinding.D);
    }

    public static void downKey2(InputBinding key) {
        put(KEY_DOWN_KEY2, key != null ? key.name() : InputBinding.S.name());
    }

    public static InputBinding downKey2() {
        return getInputBinding(KEY_DOWN_KEY2, InputBinding.S);
    }

    public static void rotateKey2(InputBinding key) {
        put(KEY_ROTATE_KEY2, key != null ? key.name() : InputBinding.W.name());
    }

    public static InputBinding rotateKey2() {
        return getInputBinding(KEY_ROTATE_KEY2, InputBinding.W);
    }

    public static void dropKey2(InputBinding key) {
        put(KEY_DROP_KEY2, key != null ? key.name() : InputBinding.SPACE.name());
    }

    public static InputBinding dropKey2() {
        return getInputBinding(KEY_DROP_KEY2, InputBinding.SPACE);
    }

    // ==================== 辅助方法 ====================

    private static InputBinding getInputBinding(String key, InputBinding defaultValue) {
        String value = getString(key, defaultValue.name());
        try {
            return InputBinding.valueOf(value);
        } catch (IllegalArgumentException e) {
            // 如果存储的值无效，返回默认值
            return defaultValue;
        }
    }

    // ==================== 网络设置 ====================

    public static void defaultHost(String value) {
        put(KEY_DEFAULT_HOST, value);
    }

    public static String defaultHost() {
        return getString(KEY_DEFAULT_HOST, "localhost");
    }

    public static void defaultPort(int value) {
        put(KEY_DEFAULT_PORT, value);
    }

    public static int defaultPort() {
        return getInt(KEY_DEFAULT_PORT, 8080, 1, 65535);
    }

    // ==================== 语言设置 ====================

    public static void language(String value) {
        put(KEY_LANGUAGE, value);
    }

    public static String language() {
        return getString(KEY_LANGUAGE, "en");
    }

    // ==================== 玩家名称设置 ====================

    public static void playerName(String value) {
        put(KEY_PLAYER_NAME, value);
    }

    public static String playerName() {
        return getString(KEY_PLAYER_NAME, "Player" + (int) (Math.random() * 1000), 20);
    }

    // ==================== 窗口设置 ====================

    public static void fullscreen(boolean value) {
        put(KEY_FULLSCREEN, value);
    }

    public static boolean fullscreen() {
        return getBoolean(KEY_FULLSCREEN, false);
    }

    public static void windowResolution(Point p) {
        put(KEY_WINDOW_WIDTH, p.x);
        put(KEY_WINDOW_HEIGHT, p.y);
    }

    public static Point windowResolution() {
        int width = getInt(KEY_WINDOW_WIDTH, 800, 640, 3840);
        int height = getInt(KEY_WINDOW_HEIGHT, 600, 480, 2160);
        return new Point(width, height);
    }

    public static void windowMaximized(boolean value) {
        put(KEY_WINDOW_MAXIMIZED, value);
    }

    public static boolean windowMaximized() {
        return getBoolean(KEY_WINDOW_MAXIMIZED, false);
    }

    // ==================== 重置所有设置 ====================

    public static void resetToDefaults() {
        difficulty(1);
        leftKey(InputBinding.LEFT);
        rightKey(InputBinding.RIGHT);
        downKey(InputBinding.DOWN);
        rotateKey(InputBinding.UP);
        dropKey(InputBinding.SPACE);
        leftKey2(InputBinding.A);
        rightKey2(InputBinding.D);
        downKey2(InputBinding.S);
        rotateKey2(InputBinding.W);
        dropKey2(InputBinding.SPACE);
        defaultHost("localhost");
        defaultPort(8080);
        language("en");
        playerName("Player" + (int) (Math.random() * 1000));
        fullscreen(false);
        windowResolution(new Point(800, 600));
        windowMaximized(false);
    }
}
