package me.catand.cooptetris.util;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Config {
    // 游戏难度：1-简单，2-普通，3-困难
    private int difficulty;

    // 控制键位
    private String leftKey;
    private String rightKey;
    private String downKey;
    private String rotateKey;
    private String dropKey;

    // 第二套控制键位
    private String leftKey2;
    private String rightKey2;
    private String downKey2;
    private String rotateKey2;
    private String dropKey2;

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
        // 第二套控制键位默认值
        this.leftKey2 = "A";
        this.rightKey2 = "D";
        this.downKey2 = "S";
        this.rotateKey2 = "W";
        this.dropKey2 = "SPACE";
        this.defaultHost = "localhost";
        this.defaultPort = 8080;
        this.language = "en";
        this.playerName = "Player" + (int) (Math.random() * 1000);
    }

    public Config(int difficulty, String leftKey, String rightKey, String downKey,
                  String rotateKey, String dropKey, String leftKey2, String rightKey2,
                  String downKey2, String rotateKey2, String dropKey2, String defaultHost,
                  int defaultPort, String language, String playerName) {
        this.difficulty = difficulty;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.downKey = downKey;
        this.rotateKey = rotateKey;
        this.dropKey = dropKey;
        this.leftKey2 = leftKey2;
        this.rightKey2 = rightKey2;
        this.downKey2 = downKey2;
        this.rotateKey2 = rotateKey2;
        this.dropKey2 = dropKey2;
        this.defaultHost = defaultHost;
        this.defaultPort = defaultPort;
        this.language = language;
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
            Objects.equals(leftKey2, config.leftKey2) &&
            Objects.equals(rightKey2, config.rightKey2) &&
            Objects.equals(downKey2, config.downKey2) &&
            Objects.equals(rotateKey2, config.rotateKey2) &&
            Objects.equals(dropKey2, config.dropKey2) &&
            Objects.equals(defaultHost, config.defaultHost) &&
            Objects.equals(language, config.language) &&
            Objects.equals(playerName, config.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(difficulty, leftKey, rightKey, downKey, rotateKey, dropKey,
            leftKey2, rightKey2, downKey2, rotateKey2, dropKey2,
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
            ", leftKey2='" + leftKey2 + '\'' +
            ", rightKey2='" + rightKey2 + '\'' +
            ", downKey2='" + downKey2 + '\'' +
            ", rotateKey2='" + rotateKey2 + '\'' +
            ", dropKey2='" + dropKey2 + '\'' +
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
}
