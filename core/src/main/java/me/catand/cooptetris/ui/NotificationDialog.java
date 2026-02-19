package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.UIScaler;

/**
 * 通知弹窗 - 用于显示服务器发送的通知、错误、警告等信息
 * <p>
 * 适配UIScaler缩放系统，以1280x720为设计基准
 * 使用Main.platform.getFont生成清晰字体
 */
public class NotificationDialog extends Dialog {

    // 设计基准尺寸（与UIScaler保持一致）
    private static final float DESIGN_WIDTH = 1280f;
    private static final float DESIGN_HEIGHT = 720f;

    // 弹窗设计尺寸
    private static final float DIALOG_DESIGN_WIDTH = 400f;
    private static final float DIALOG_DESIGN_HEIGHT = 250f;

    // 字体设计大小
    private static final int TITLE_FONT_SIZE = 24;
    private static final int MESSAGE_FONT_SIZE = 18;
    private static final int REASON_FONT_SIZE = 16;
    private static final int BUTTON_FONT_SIZE = 18;

    private Label titleLabel;
    private Label messageLabel;
    private Label reasonLabel;
    private final TextButton okButton;
    private Runnable onCloseAction;
    private final UIScaler scaler;

    // 字体缓存
    private BitmapFont titleFont;
    private BitmapFont messageFont;
    private BitmapFont reasonFont;
    private BitmapFont buttonFont;

    // 当前消息
    private NotificationMessage currentMessage;

    // 保存skin引用（父类的skin是private）
    private final Skin dialogSkin;

    public NotificationDialog(Skin skin) {
        super("", skin);
        this.dialogSkin = skin;
        this.scaler = UIScaler.getInstance();

        // 设置对话框样式
        setModal(true);
        setMovable(false);
        setResizable(false);

        // 创建确定按钮
        okButton = new TextButton("OK", skin);
        // 添加青色悬停效果
        okButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                okButton.setColor(Color.CYAN);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                okButton.setColor(Color.WHITE);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
                if (onCloseAction != null) {
                    onCloseAction.run();
                }
            }
        });
    }

    /**
     * 创建或更新字体
     */
    private void updateFonts() {
        float scale = scaler.getScale();

        // 计算实际字体大小
        int actualTitleSize = Math.round(TITLE_FONT_SIZE * scale);
        int actualMessageSize = Math.round(MESSAGE_FONT_SIZE * scale);
        int actualReasonSize = Math.round(REASON_FONT_SIZE * scale);
        int actualButtonSize = Math.round(BUTTON_FONT_SIZE * scale);

        // 使用Main.platform.getFont生成清晰字体
        titleFont = Main.platform.getFont(actualTitleSize, "NotificationTitle", false, false);
        messageFont = Main.platform.getFont(actualMessageSize, "NotificationMessage", false, false);
        reasonFont = Main.platform.getFont(actualReasonSize, "NotificationReason", false, false);
        buttonFont = Main.platform.getFont(actualButtonSize, "OK", false, false);

        // 创建或更新标签样式
        if (titleLabel == null) {
            Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont != null ? titleFont : dialogSkin.getFont("default"), Color.WHITE);
            titleLabel = new Label("", titleStyle);
            titleLabel.setAlignment(Align.center);
        } else {
            titleLabel.setStyle(new Label.LabelStyle(titleFont != null ? titleFont : dialogSkin.getFont("default"), titleLabel.getColor()));
        }

        if (messageLabel == null) {
            Label.LabelStyle messageStyle = new Label.LabelStyle(messageFont != null ? messageFont : dialogSkin.getFont("default"), Color.WHITE);
            messageLabel = new Label("", messageStyle);
            messageLabel.setWrap(true);
            messageLabel.setAlignment(Align.center);
        } else {
            messageLabel.setStyle(new Label.LabelStyle(messageFont != null ? messageFont : dialogSkin.getFont("default"), Color.WHITE));
        }

        if (reasonLabel == null) {
            Label.LabelStyle reasonStyle = new Label.LabelStyle(reasonFont != null ? reasonFont : dialogSkin.getFont("default"), Color.GRAY);
            reasonLabel = new Label("", reasonStyle);
            reasonLabel.setWrap(true);
            reasonLabel.setAlignment(Align.center);
        } else {
            reasonLabel.setStyle(new Label.LabelStyle(reasonFont != null ? reasonFont : dialogSkin.getFont("default"), Color.GRAY));
        }

        // 更新按钮字体样式
        updateButtonStyle();
    }

    /**
     * 更新按钮样式以应用字体缩放
     */
    private void updateButtonStyle() {
        if (buttonFont != null) {
            // 创建新的按钮样式，使用缩放后的字体
            TextButton.TextButtonStyle newStyle = new TextButton.TextButtonStyle(
                okButton.getStyle().up,
                okButton.getStyle().down,
                okButton.getStyle().checked,
                buttonFont
            );
            newStyle.over = okButton.getStyle().over;
            newStyle.disabled = okButton.getStyle().disabled;
            newStyle.fontColor = okButton.getStyle().fontColor;
            newStyle.downFontColor = okButton.getStyle().downFontColor;
            newStyle.overFontColor = okButton.getStyle().overFontColor;
            newStyle.disabledFontColor = okButton.getStyle().disabledFontColor;
            okButton.setStyle(newStyle);
        }
    }

    /**
     * 应用缩放设置
     */
    private void applyScale() {
        float scale = scaler.getScale();

        // 更新字体
        updateFonts();

        // 清空并重新组装对话框内容
        getContentTable().clear();
        getButtonTable().clear();

        // 重新组装对话框内容
        // 使用设计时的固定宽度比例，确保文字换行行为一致
        float labelWidth = 360 * scale;
        getContentTable().add(titleLabel).pad(10 * scale).row();
        getContentTable().add(messageLabel).width(labelWidth).pad(10 * scale).row();
        getContentTable().add(reasonLabel).width(labelWidth).pad(5 * scale).row();
        getButtonTable().add(okButton).width(100 * scale).height(40 * scale).pad(10 * scale);

        // 设置弹窗大小（基于设计尺寸缩放）
        float dialogWidth = DIALOG_DESIGN_WIDTH * scale;
        float dialogHeight = DIALOG_DESIGN_HEIGHT * scale;
        setSize(dialogWidth, dialogHeight);

        // 更新内容
        if (currentMessage != null) {
            updateContent(currentMessage);
        }

        // 关键：布局完成后，根据实际内容调整弹窗高度
        // 这样可以防止文字被挤出弹窗
        getContentTable().layout();
        float contentHeight = getContentTable().getPrefHeight();
        float buttonHeight = getButtonTable().getPrefHeight();
        float newHeight = Math.max(dialogHeight, contentHeight + buttonHeight + 40 * scale);
        setHeight(newHeight);
    }

    /**
     * 更新内容显示
     */
    private void updateContent(NotificationMessage message) {
        LanguageManager lang = LanguageManager.getInstance();

        // 设置标题
        String title = message.getTitle();
        if (title == null || title.isEmpty()) {
            title = getDefaultTitle(message.getNotificationType());
        }
        titleLabel.setText(title);

        // 根据类型设置标题颜色
        titleLabel.setColor(getColorForType(message.getNotificationType()));

        // 设置消息内容
        messageLabel.setText(message.getMessage() != null ? message.getMessage() : "");

        // 设置原因（如果有）
        if (message.getReason() != null && !message.getReason().isEmpty()) {
            String reasonLabelText = lang.get("notification.reason.label") + " " + message.getReason();
            reasonLabel.setText(reasonLabelText);
            reasonLabel.setVisible(true);
        } else {
            reasonLabel.setVisible(false);
        }

        // 根据类型设置按钮文字
        okButton.setText(getButtonTextForType(message.getNotificationType()));
    }

    @Override
    public Dialog show(Stage stage) {
        // 先应用缩放
        applyScale();

        // 调用父类show方法
        super.show(stage);

        // 居中显示（考虑偏移量）
        updatePosition();

        return this;
    }

    /**
     * 更新弹窗位置（用于窗口大小变化时）
     */
    public void updatePosition() {
        float offsetX = scaler.getOffsetX();
        float offsetY = scaler.getOffsetY();
        float displayWidth = scaler.getDisplayWidth();
        float displayHeight = scaler.getDisplayHeight();

        setPosition(
            offsetX + (displayWidth - getWidth()) / 2,
            offsetY + (displayHeight - getHeight()) / 2
        );
    }

    /**
     * 窗口大小变化时更新弹窗
     */
    public void onResize() {
        if (isVisible()) {
            applyScale();
            updatePosition();
        }
    }

    /**
     * 显示通知弹窗
     */
    public static void show(Stage stage, Skin skin, NotificationMessage message) {
        show(stage, skin, message, null);
    }

    /**
     * 显示通知弹窗，带关闭回调
     */
    public static void show(Stage stage, Skin skin, NotificationMessage message, Runnable onClose) {
        NotificationDialog dialog = new NotificationDialog(skin);
        dialog.currentMessage = message;
        dialog.setNotification(message);
        dialog.setOnCloseAction(onClose);
        dialog.show(stage);
    }

    /**
     * 显示简单的错误弹窗
     */
    public static void showError(Stage stage, Skin skin, String title, String message) {
        NotificationMessage msg = new NotificationMessage();
        msg.setNotificationType(NotificationMessage.NotificationType.ERROR);
        msg.setTitle(title);
        msg.setMessage(message);
        show(stage, skin, msg);
    }

    /**
     * 显示简单的信息弹窗
     */
    public static void showInfo(Stage stage, Skin skin, String title, String message) {
        NotificationMessage msg = new NotificationMessage();
        msg.setNotificationType(NotificationMessage.NotificationType.INFO);
        msg.setTitle(title);
        msg.setMessage(message);
        show(stage, skin, msg);
    }

    /**
     * 设置通知内容
     */
    public void setNotification(NotificationMessage message) {
        this.currentMessage = message;
        // 确保标签已初始化
        if (titleLabel == null) {
            applyScale();
        } else {
            updateContent(message);
        }
    }

    /**
     * 设置关闭回调
     */
    public void setOnCloseAction(Runnable action) {
        this.onCloseAction = action;
    }

    /**
     * 获取默认标题
     */
    private String getDefaultTitle(NotificationMessage.NotificationType type) {
        LanguageManager lang = LanguageManager.getInstance();
        switch (type) {
            case INFO:
                return lang.get("notification.title.info");
            case WARNING:
                return lang.get("notification.title.warning");
            case ERROR:
                return lang.get("notification.title.error");
            case KICKED:
                return lang.get("notification.title.kicked");
            case DISCONNECTED:
                return lang.get("notification.title.disconnected");
            case BANNED:
                return lang.get("notification.title.banned");
            default:
                return lang.get("notification.title.info");
        }
    }

    /**
     * 获取类型对应的颜色
     */
    private Color getColorForType(NotificationMessage.NotificationType type) {
        switch (type) {
            case INFO:
                return Color.WHITE;
            case WARNING:
                return Color.YELLOW;
            case ERROR:
                return Color.RED;
            case KICKED:
                return Color.ORANGE;
            case DISCONNECTED:
                return Color.GRAY;
            case BANNED:
                return Color.RED;
            default:
                return Color.WHITE;
        }
    }

    /**
     * 获取按钮文字
     */
    private String getButtonTextForType(NotificationMessage.NotificationType type) {
        LanguageManager lang = LanguageManager.getInstance();
        switch (type) {
            case KICKED:
            case DISCONNECTED:
            case BANNED:
                return lang.get("notification.button.leave");
            case ERROR:
                return lang.get("notification.button.ok");
            default:
                return lang.get("notification.button.ok");
        }
    }
}
