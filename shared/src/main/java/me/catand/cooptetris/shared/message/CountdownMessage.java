package me.catand.cooptetris.shared.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 倒计时消息 - 用于同步所有玩家的游戏开始倒计时
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CountdownMessage extends NetworkMessage {

    private int countdownSeconds; // 倒计时秒数
    private boolean isStarting;   // 是否正在倒计时

    public CountdownMessage() {
        super("countdown");
    }

    public CountdownMessage(int countdownSeconds, boolean isStarting) {
        super("countdown");
        this.countdownSeconds = countdownSeconds;
        this.isStarting = isStarting;
    }
}
