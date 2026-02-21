package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.util.LanguageManager;

/**
 * 通知弹窗 - 现代化暗色游戏UI风格
 */
public class NotificationDialog {

    private Table dialogTable;
    private Label titleLabel;
    private Label messageLabel;
    private Label reasonLabel;
    private TextButton okButton;
    private Runnable onCloseAction;
    private com.badlogic.gdx.scenes.scene2d.ui.Skin skin;

    private BitmapFont titleFont;
    private BitmapFont messageFont;

    private NotificationMessage currentMessage;
    private boolean isVisible = false;

    // UI颜色配置
    private static final Color COLOR_PANEL = new Color(0.12f, 0.14f, 0.17f, 0.98f);
    private static final Color COLOR_PANEL_BORDER = new Color(0.25f, 0.28f, 0.35f, 1f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);
    private static final Color COLOR_SECONDARY = new Color(0.8f, 0.3f, 0.9f, 1f);
    private static final Color COLOR_SUCCESS = new Color(0.3f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_WARNING = new Color(1f, 0.7f, 0.2f, 1f);
    private static final Color COLOR_DANGER = new Color(1f, 0.3f, 0.3f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color COLOR_TEXT_MUTED = new Color(0.5f, 0.52f, 0.55f, 1f);

    // 设计尺寸
    private static final float DIALOG_WIDTH = 400f;
    private static final float DIALOG_PADDING = 30f;

    // 弹窗位置枚举
    public enum Position {
        CENTER, LEFT, RIGHT
    }

    public NotificationDialog(com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
        createDialog(skin);
    }

    private void createDialog(com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
        this.skin = skin;
        LanguageManager lang = LanguageManager.getInstance();

        // 创建对话框表格
        dialogTable = new Table();
        dialogTable.setBackground(createPanelBackground(COLOR_PANEL));
        dialogTable.pad(w(DIALOG_PADDING));
        dialogTable.setSize(w(DIALOG_WIDTH), h(200f));

        // 标题标签 - 使用空字符串初始化，在updateContent中设置字体
        titleLabel = new Label("", skin);
        titleLabel.setAlignment(Align.center);

        // 消息标签 - 使用空字符串初始化，在updateContent中设置字体
        messageLabel = new Label("", skin);
        messageLabel.setAlignment(Align.center);
        messageLabel.setWrap(true);

        // 原因标签 - 使用空字符串初始化，在updateContent中设置字体
        reasonLabel = new Label("", skin);
        reasonLabel.setAlignment(Align.center);
        reasonLabel.setWrap(true);

        // 确定按钮
        okButton = FontUtils.createTextButton(lang.get("notification.button.ok"), skin, (int)fontSize(18), COLOR_PRIMARY);
        okButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                hide();
                if (onCloseAction != null) {
                    onCloseAction.run();
                }
            }
            return true;
        });

        // 组装对话框
        dialogTable.add(titleLabel).fillX().padBottom(h(20f)).row();
        dialogTable.add(messageLabel).width(w(DIALOG_WIDTH - DIALOG_PADDING * 2)).padBottom(h(10f)).row();
        dialogTable.add(reasonLabel).width(w(DIALOG_WIDTH - DIALOG_PADDING * 2)).padBottom(h(25f)).row();
        dialogTable.add(okButton).width(w(120f)).height(h(45f));

        // 默认隐藏
        dialogTable.setVisible(false);
    }

    public void setNotification(NotificationMessage message) {
        this.currentMessage = message;
        updateContent();
    }

    private void updateContent() {
        if (currentMessage == null) return;

        LanguageManager lang = LanguageManager.getInstance();

        // 设置标题
        String title = currentMessage.getTitle();
        if (title == null || title.isEmpty()) {
            title = getDefaultTitle(currentMessage.getNotificationType());
        }
        // 动态获取字体并设置
        titleFont = Main.platform.getFont((int)fontSize(24), title, false, false);
        titleLabel.setStyle(new Label.LabelStyle(titleFont, COLOR_TEXT));
        titleLabel.setText(title);

        // 根据类型设置标题颜色
        titleLabel.setColor(getColorForType(currentMessage.getNotificationType()));

        // 设置消息内容
        String message = currentMessage.getMessage() != null ? currentMessage.getMessage() : "";
        messageFont = Main.platform.getFont((int)fontSize(16), message, false, false);
        messageLabel.setStyle(new Label.LabelStyle(messageFont, COLOR_TEXT));
        messageLabel.setText(message);

        // 设置原因（如果有）
        if (currentMessage.getReason() != null && !currentMessage.getReason().isEmpty()) {
            String reasonText = lang.get("notification.reason.label") + " " + currentMessage.getReason();
            // 原因标签使用消息字体，但颜色不同
            reasonLabel.setStyle(new Label.LabelStyle(messageFont, COLOR_TEXT_MUTED));
            reasonLabel.setText(reasonText);
            reasonLabel.setVisible(true);
        } else {
            reasonLabel.setVisible(false);
        }

        // 根据类型设置按钮文字
        String buttonText = getButtonTextForType(currentMessage.getNotificationType());
        okButton.setText(buttonText);
        // 更新按钮字体以包含新的按钮文本
        FontUtils.updateButtonFont(okButton, skin, (int)fontSize(18));

        // 调整对话框高度以适应内容
        dialogTable.pack();
        float minHeight = h(200f);
        if (dialogTable.getHeight() < minHeight) {
            dialogTable.setHeight(minHeight);
        }
    }

    public void show(Stage stage) {
        show(stage, Position.CENTER);
    }

    public void show(Stage stage, Position position) {
        if (!isVisible) {
            stage.addActor(dialogTable);
            isVisible = true;
        }
        dialogTable.setVisible(true);
        // 确保弹窗在最上层显示
        dialogTable.toFront();
        // 确保在下一帧布局完成后更新位置
        com.badlogic.gdx.Gdx.app.postRunnable(() -> {
            if (isVisible && dialogTable != null) {
                updatePosition(stage, position);
                dialogTable.toFront();
            }
        });
    }

    public void hide() {
        dialogTable.setVisible(false);
        isVisible = false;
    }

    private void updatePosition(Stage stage, Position position) {
        float x, y;
        switch (position) {
            case RIGHT:
                // 显示在右侧
                x = stage.getWidth() - dialogTable.getWidth() - w(20f);
                y = (stage.getHeight() - dialogTable.getHeight()) / 2;
                break;
            case LEFT:
                // 显示在左侧
                x = w(20f);
                y = (stage.getHeight() - dialogTable.getHeight()) / 2;
                break;
            case CENTER:
            default:
                // 居中显示
                x = (stage.getWidth() - dialogTable.getWidth()) / 2;
                y = (stage.getHeight() - dialogTable.getHeight()) / 2;
                break;
        }
        dialogTable.setPosition(x, y);
    }

    public void onResize(Stage stage) {
        if (isVisible) {
            updatePosition(stage, Position.CENTER);
        }
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setOnCloseAction(Runnable action) {
        this.onCloseAction = action;
    }

    // ==================== 静态方法 ====================

    public static void show(Stage stage, com.badlogic.gdx.scenes.scene2d.ui.Skin skin, NotificationMessage message) {
        show(stage, skin, message, null);
    }

    public static void show(Stage stage, com.badlogic.gdx.scenes.scene2d.ui.Skin skin, NotificationMessage message, Runnable onClose) {
        NotificationDialog dialog = new NotificationDialog(skin);
        dialog.setNotification(message);
        dialog.setOnCloseAction(onClose);
        dialog.show(stage);
    }

    public static void showError(Stage stage, com.badlogic.gdx.scenes.scene2d.ui.Skin skin, String title, String message) {
        NotificationMessage msg = new NotificationMessage();
        msg.setNotificationType(NotificationMessage.NotificationType.ERROR);
        msg.setTitle(title);
        msg.setMessage(message);
        show(stage, skin, msg);
    }

    public static void showInfo(Stage stage, com.badlogic.gdx.scenes.scene2d.ui.Skin skin, String title, String message) {
        NotificationMessage msg = new NotificationMessage();
        msg.setNotificationType(NotificationMessage.NotificationType.INFO);
        msg.setTitle(title);
        msg.setMessage(message);
        show(stage, skin, msg);
    }

    // ==================== 辅助方法 ====================

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

    private Color getColorForType(NotificationMessage.NotificationType type) {
        switch (type) {
            case INFO:
                return COLOR_PRIMARY;
            case WARNING:
                return COLOR_WARNING;
            case ERROR:
                return COLOR_DANGER;
            case KICKED:
                return COLOR_WARNING;
            case DISCONNECTED:
                return COLOR_TEXT_MUTED;
            case BANNED:
                return COLOR_DANGER;
            default:
                return COLOR_TEXT;
        }
    }

    private String getButtonTextForType(NotificationMessage.NotificationType type) {
        LanguageManager lang = LanguageManager.getInstance();
        switch (type) {
            case KICKED:
            case DISCONNECTED:
            case BANNED:
                return lang.get("notification.button.leave");
            case ERROR:
            default:
                return lang.get("notification.button.ok");
        }
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createPanelBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(texture);
    }

    private float fontSize(int baseSize) {
        return baseSize * getUIScale();
    }

    private float w(float designWidth) {
        return designWidth * getUIScale();
    }

    private float h(float designHeight) {
        return designHeight * getUIScale();
    }

    private float getUIScale() {
        // 使用与BaseUIState相同的缩放逻辑
        float designWidth = 1280f;
        float designHeight = 720f;
        float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
        float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
        float scaleX = screenWidth / designWidth;
        float scaleY = screenHeight / designHeight;
        return Math.min(scaleX, scaleY);
    }
}
