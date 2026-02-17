package me.catand.cooptetris.util;

public class TetrisSettings extends GameSettings {

    // 游戏难度
    public static final String KEY_DIFFICULTY = "difficulty";

    // 控制键位
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

    // 游戏难度设置
    public static void difficulty(int value) {
        put(KEY_DIFFICULTY, value);
    }

    public static int difficulty() {
        return getInt(KEY_DIFFICULTY, 1, 1, 3);
    }

    // 第一套控制键位设置
    public static void leftKey(String value) {
        put(KEY_LEFT_KEY, value);
    }

    public static String leftKey() {
        return getString(KEY_LEFT_KEY, "LEFT");
    }

    public static void rightKey(String value) {
        put(KEY_RIGHT_KEY, value);
    }

    public static String rightKey() {
        return getString(KEY_RIGHT_KEY, "RIGHT");
    }

    public static void downKey(String value) {
        put(KEY_DOWN_KEY, value);
    }

    public static String downKey() {
        return getString(KEY_DOWN_KEY, "DOWN");
    }

    public static void rotateKey(String value) {
        put(KEY_ROTATE_KEY, value);
    }

    public static String rotateKey() {
        return getString(KEY_ROTATE_KEY, "UP");
    }

    public static void dropKey(String value) {
        put(KEY_DROP_KEY, value);
    }

    public static String dropKey() {
        return getString(KEY_DROP_KEY, "SPACE");
    }

    // 第二套控制键位设置
    public static void leftKey2(String value) {
        put(KEY_LEFT_KEY2, value);
    }

    public static String leftKey2() {
        return getString(KEY_LEFT_KEY2, "A");
    }

    public static void rightKey2(String value) {
        put(KEY_RIGHT_KEY2, value);
    }

    public static String rightKey2() {
        return getString(KEY_RIGHT_KEY2, "D");
    }

    public static void downKey2(String value) {
        put(KEY_DOWN_KEY2, value);
    }

    public static String downKey2() {
        return getString(KEY_DOWN_KEY2, "S");
    }

    public static void rotateKey2(String value) {
        put(KEY_ROTATE_KEY2, value);
    }

    public static String rotateKey2() {
        return getString(KEY_ROTATE_KEY2, "W");
    }

    public static void dropKey2(String value) {
        put(KEY_DROP_KEY2, value);
    }

    public static String dropKey2() {
        return getString(KEY_DROP_KEY2, "SPACE");
    }

    // 网络设置
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

    // 语言设置
    public static void language(String value) {
        put(KEY_LANGUAGE, value);
    }

    public static String language() {
        return getString(KEY_LANGUAGE, "en");
    }

    // 玩家名称设置
    public static void playerName(String value) {
        put(KEY_PLAYER_NAME, value);
    }

    public static String playerName() {
        return getString(KEY_PLAYER_NAME, "Player" + (int) (Math.random() * 1000), 20);
    }

    // 全屏设置
    public static final String KEY_FULLSCREEN = "fullscreen";

    public static void fullscreen(boolean value) {
        put(KEY_FULLSCREEN, value);
    }

    public static boolean fullscreen() {
        return getBoolean(KEY_FULLSCREEN, false);
    }

    // 窗口分辨率设置
    public static final String KEY_WINDOW_WIDTH = "window_width";
    public static final String KEY_WINDOW_HEIGHT = "window_height";

    public static final String KEY_WINDOW_MAXIMIZED = "window_maximized";

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

    // 重置所有设置为默认值
    public static void resetToDefaults() {
        difficulty(1);
        leftKey("LEFT");
        rightKey("RIGHT");
        downKey("DOWN");
        rotateKey("UP");
        dropKey("SPACE");
        leftKey2("A");
        rightKey2("D");
        downKey2("S");
        rotateKey2("W");
        dropKey2("SPACE");
        defaultHost("localhost");
        defaultPort(8080);
        language("en");
        playerName("Player" + (int) (Math.random() * 1000));
        fullscreen(false);
        windowResolution(new Point(800, 600));
        windowMaximized(false);
    }
}
