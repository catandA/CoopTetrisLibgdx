package me.catand.cooptetris.shared.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConnectMessage extends NetworkMessage {
    private String playerName;
    private boolean success;
    private String message;
    private String clientId;
    private String language; // 客户端语言设置

    public ConnectMessage() {
        super("connect");
    }
}
