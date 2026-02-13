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
    private static final String KEY_PLAYER_NAME = "player_name";

    private final Preferences prefs;
    private Config currentConfig;

    public ConfigManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        loadConfig();
    }

    /**
     * 从Preferences加载配置到Config对象
     */
    private void loadConfig() {
        currentConfig = new Config(
            prefs.getInteger(KEY_DIFFICULTY, 1),
            prefs.getString(KEY_LEFT_KEY, "LEFT"),
            prefs.getString(KEY_RIGHT_KEY, "RIGHT"),
            prefs.getString(KEY_DOWN_KEY, "DOWN"),
            prefs.getString(KEY_ROTATE_KEY, "UP"),
            prefs.getString(KEY_DROP_KEY, "SPACE"),
            prefs.getString(KEY_DEFAULT_HOST, "localhost"),
            prefs.getInteger(KEY_DEFAULT_PORT, 8080),
            prefs.getString(KEY_LANGUAGE, "en"),
            prefs.getString(KEY_PLAYER_NAME, "Player" + (int)(Math.random() * 1000))
        );
    }

    /**
     * 保存Config对象到Preferences
     */
    private void saveConfig() {
        if (currentConfig == null) {
            currentConfig = Config.createDefault();
        }
        
        prefs.putInteger(KEY_DIFFICULTY, currentConfig.getDifficulty());
        prefs.putString(KEY_LEFT_KEY, currentConfig.getLeftKey());
        prefs.putString(KEY_RIGHT_KEY, currentConfig.getRightKey());
        prefs.putString(KEY_DOWN_KEY, currentConfig.getDownKey());
        prefs.putString(KEY_ROTATE_KEY, currentConfig.getRotateKey());
        prefs.putString(KEY_DROP_KEY, currentConfig.getDropKey());
        prefs.putString(KEY_DEFAULT_HOST, currentConfig.getDefaultHost());
        prefs.putInteger(KEY_DEFAULT_PORT, currentConfig.getDefaultPort());
        prefs.putString(KEY_LANGUAGE, currentConfig.getLanguage());
        prefs.putString(KEY_PLAYER_NAME, currentConfig.getPlayerName());
        prefs.flush();
    }

    /**
     * 保存设置（使用Config对象）
     */
    public void saveSettings(Config config) {
        this.currentConfig = config;
        saveConfig();
    }

    /**
     * 获取当前配置
     */
    public Config getConfig() {
        return currentConfig;
    }

    /**
     * 设置当前配置
     */
    public void setConfig(Config config) {
        this.currentConfig = config;
    }

    public void resetToDefaults() {
        prefs.clear();
        prefs.flush();
        currentConfig = Config.createDefault();
    }
}
