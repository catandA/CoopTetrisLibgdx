package me.catand.cooptetris.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class ConfigManager {
    private static final String PREFS_NAME = "coop_tetris_settings";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_LEFT_KEY = "left_key";
    private static final String KEY_RIGHT_KEY = "right_key";
    private static final String KEY_DOWN_KEY = "down_key";
    private static final String KEY_ROTATE_KEY = "rotate_key";
    private static final String KEY_DROP_KEY = "drop_key";
    private static final String KEY_DEFAULT_HOST = "default_host";
    private static final String KEY_DEFAULT_PORT = "default_port";
    private static final String KEY_LANGUAGE = "language";

    private final Preferences prefs;

    public ConfigManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public void saveSettings(int difficulty, String leftKey, String rightKey, String downKey, String rotateKey, String dropKey, String defaultHost, int defaultPort, String language) {
        prefs.putInteger(KEY_DIFFICULTY, difficulty);
        prefs.putString(KEY_LEFT_KEY, leftKey);
        prefs.putString(KEY_RIGHT_KEY, rightKey);
        prefs.putString(KEY_DOWN_KEY, downKey);
        prefs.putString(KEY_ROTATE_KEY, rotateKey);
        prefs.putString(KEY_DROP_KEY, dropKey);
        prefs.putString(KEY_DEFAULT_HOST, defaultHost);
        prefs.putInteger(KEY_DEFAULT_PORT, defaultPort);
        prefs.putString(KEY_LANGUAGE, language);
        prefs.flush();
    }

    // 保持向后兼容的方法
    public void saveSettings(int difficulty, String leftKey, String rightKey, String downKey, String rotateKey, String dropKey, String defaultHost, int defaultPort) {
        String currentLanguage = LanguageManager.getInstance().getCurrentLanguageCode();
        saveSettings(difficulty, leftKey, rightKey, downKey, rotateKey, dropKey, defaultHost, defaultPort, currentLanguage);
    }

    public int getDifficulty() {
        return prefs.getInteger(KEY_DIFFICULTY, 1);
    }

    public String getLeftKey() {
        return prefs.getString(KEY_LEFT_KEY, "LEFT");
    }

    public String getRightKey() {
        return prefs.getString(KEY_RIGHT_KEY, "RIGHT");
    }

    public String getDownKey() {
        return prefs.getString(KEY_DOWN_KEY, "DOWN");
    }

    public String getRotateKey() {
        return prefs.getString(KEY_ROTATE_KEY, "UP");
    }

    public String getDropKey() {
        return prefs.getString(KEY_DROP_KEY, "SPACE");
    }

    public String getDefaultHost() {
        return prefs.getString(KEY_DEFAULT_HOST, "localhost");
    }

    public int getDefaultPort() {
        return prefs.getInteger(KEY_DEFAULT_PORT, 8080);
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    public void resetToDefaults() {
        prefs.clear();
        prefs.flush();
    }
}
