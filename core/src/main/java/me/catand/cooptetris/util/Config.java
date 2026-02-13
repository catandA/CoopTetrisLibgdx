package me.catand.cooptetris.util;

import java.util.Objects;

public class Config {
    // 游戏难度：1-简单，2-普通，3-困难
    private int difficulty;
    
    // 控制键位
    private String leftKey;
    private String rightKey;
    private String downKey;
    private String rotateKey;
    private String dropKey;
    
    // 网络设置
    private String defaultHost;
    private int defaultPort;
    
    // 语言设置
    private String language;
    
    // 玩家名称
    private String playerName;

    public Config() {
        // 默认值
        this.difficulty = 1;
        this.leftKey = "LEFT";
        this.rightKey = "RIGHT";
        this.downKey = "DOWN";
        this.rotateKey = "UP";
        this.dropKey = "SPACE";
        this.defaultHost = "localhost";
        this.defaultPort = 8080;
        this.language = "en";
        this.playerName = "Player" + (int)(Math.random() * 1000);
    }

    public Config(int difficulty, String leftKey, String rightKey, String downKey, 
                  String rotateKey, String dropKey, String defaultHost, int defaultPort, 
                  String language, String playerName) {
        this.difficulty = difficulty;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.downKey = downKey;
        this.rotateKey = rotateKey;
        this.dropKey = dropKey;
        this.defaultHost = defaultHost;
        this.defaultPort = defaultPort;
        this.language = language;
        this.playerName = playerName;
    }

    // Getter 和 Setter 方法
    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getLeftKey() {
        return leftKey;
    }

    public void setLeftKey(String leftKey) {
        this.leftKey = leftKey;
    }

    public String getRightKey() {
        return rightKey;
    }

    public void setRightKey(String rightKey) {
        this.rightKey = rightKey;
    }

    public String getDownKey() {
        return downKey;
    }

    public void setDownKey(String downKey) {
        this.downKey = downKey;
    }

    public String getRotateKey() {
        return rotateKey;
    }

    public void setRotateKey(String rotateKey) {
        this.rotateKey = rotateKey;
    }

    public String getDropKey() {
        return dropKey;
    }

    public void setDropKey(String dropKey) {
        this.dropKey = dropKey;
    }

    public String getDefaultHost() {
        return defaultHost;
    }

    public void setDefaultHost(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return difficulty == config.difficulty &&
                defaultPort == config.defaultPort &&
                Objects.equals(leftKey, config.leftKey) &&
                Objects.equals(rightKey, config.rightKey) &&
                Objects.equals(downKey, config.downKey) &&
                Objects.equals(rotateKey, config.rotateKey) &&
                Objects.equals(dropKey, config.dropKey) &&
                Objects.equals(defaultHost, config.defaultHost) &&
                Objects.equals(language, config.language) &&
                Objects.equals(playerName, config.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(difficulty, leftKey, rightKey, downKey, rotateKey, dropKey, 
                          defaultHost, defaultPort, language, playerName);
    }

    @Override
    public String toString() {
        return "Config{" +
                "difficulty=" + difficulty +
                ", leftKey='" + leftKey + '\'' +
                ", rightKey='" + rightKey + '\'' +
                ", downKey='" + downKey + '\'' +
                ", rotateKey='" + rotateKey + '\'' +
                ", dropKey='" + dropKey + '\'' +
                ", defaultHost='" + defaultHost + '\'' +
                ", defaultPort=" + defaultPort +
                ", language='" + language + '\'' +
                ", playerName='" + playerName + '\'' +
                '}';
    }

    /**
     * 创建默认配置实例
     */
    public static Config createDefault() {
        return new Config();
    }

    /**
     * 从当前配置创建一个副本
     */
    public Config copy() {
        return new Config(
                this.difficulty,
                this.leftKey,
                this.rightKey,
                this.downKey,
                this.rotateKey,
                this.dropKey,
                this.defaultHost,
                this.defaultPort,
                this.language,
                this.playerName
        );
    }
}
