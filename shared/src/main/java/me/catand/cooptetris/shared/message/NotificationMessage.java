package me.catand.cooptetris.shared.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class NotificationMessage extends NetworkMessage {
    public enum NotificationType {
        INFO,       // 普通信息
        WARNING,    // 警告
        ERROR,      // 错误
        KICKED,     // 被踢出房间
        DISCONNECTED, // 连接断开
        BANNED      // 被禁止连接
    }

    private NotificationType notificationType;
    private String title;
    private String message;
    private String reason;

    public NotificationMessage() {
        super("notification");
    }

    public NotificationMessage(NotificationType notificationType, String title, String message) {
        super("notification");
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
    }

    public NotificationMessage(NotificationType notificationType, String title, String message, String reason) {
        super("notification");
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.reason = reason;
    }
}
