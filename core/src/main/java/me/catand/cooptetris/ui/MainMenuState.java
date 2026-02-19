package me.catand.cooptetris.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.VersionManager;

/**
 * 主菜单状态 - 使用新的简化缩放接口
 */
public class MainMenuState extends BaseUIState {
    private Table table;
    private BitmapFont titleFont;
    private Label versionLabel;

    public MainMenuState(UIManager uiManager) {
        super(uiManager);
    }

    @Override
    protected void createUI() {
        table = new Table();
        table.setPosition(offsetX(), offsetY());
        table.setSize(displayWidth(), displayHeight());
        table.center();

        LanguageManager lang = LanguageManager.getInstance();

        // 为标题创建一个更大的字体，考虑缩放比例
        titleFont = Main.platform.getFont(fontSize(32), lang.get("title"), false, false);

        // 创建标题
        Label title;
        if (titleFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(titleFont, skin.getColor("font"));
            title = new Label(lang.get("title"), labelStyle);
        } else {
            // 如果字体生成失败，使用默认字体
            title = new Label(lang.get("title"), skin);
        }
        title.setAlignment(Align.center);

        // 直接使用skin中的按钮样式
        TextButton startButton = new TextButton(lang.get("start.game"), skin);
        TextButton onlineButton = new TextButton(lang.get("online.mode"), skin);
        TextButton settingsButton = new TextButton(lang.get("settings"), skin);
        TextButton exitButton = new TextButton(lang.get("exit"), skin);

        // 添加青色悬停效果
        addCyanHoverEffect(startButton);
        addCyanHoverEffect(onlineButton);
        addCyanHoverEffect(settingsButton);
        addCyanHoverEffect(exitButton);

        startButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                // 开始单机游戏，使用uiManager中已经初始化的gameStateManager
                if (uiManager.gameStateManager != null) {
                    GameState gameState = new GameState(uiManager, uiManager.gameStateManager);
                    uiManager.setScreen(gameState);
                    // 启动单人游戏模式，包括本地服务器
                    uiManager.gameStateManager.startSinglePlayer();
                }
            }
            return true;
        });

        onlineButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                // 进入联机模式界面 - 新的两阶段流程
                uiManager.pushState(new ServerConnectionState(uiManager));
            }
            return true;
        });

        settingsButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                // 进入设置界面
                uiManager.pushState(new SettingsState(uiManager));
            }
            return true;
        });
        exitButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                Gdx.app.exit();
            }
            return true;
        });

        // 使用新的简化方法进行缩放
        table.add(title).padBottom(h(50f)).row();
        table.add(startButton).width(w(200f)).height(h(60f)).padBottom(h(20f)).row();
        table.add(onlineButton).width(w(200f)).height(h(60f)).padBottom(h(20f)).row();
        table.add(settingsButton).width(w(200f)).height(h(60f)).padBottom(h(20f)).row();
        table.add(exitButton).width(w(200f)).height(h(60f)).row();

        stage.addActor(table);

        // 添加版本信息标签，显示在右下角
        VersionManager versionManager = VersionManager.getInstance();
        String versionInfo = versionManager.getVersionInfo();
        versionLabel = new Label(versionInfo, skin);
        versionLabel.setAlignment(Align.bottomRight);
        // 使用displayWidth()和offsetX()来定位，确保在缩放时位置正确
        versionLabel.setPosition(
            offsetX() + displayWidth() - versionLabel.getWidth() - w(20f),
            offsetY() + h(20f)
        );
        stage.addActor(versionLabel);
    }

    @Override
    protected void clearUI() {
        if (table != null) {
            table.remove();
            table = null;
        }
        if (versionLabel != null) {
            versionLabel.remove();
            versionLabel = null;
        }
    }

    @Override
    public void update(float delta) {
        // 主菜单不需要更新逻辑
    }

    @Override
    public void dispose() {
        // 释放生成的标题字体
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
    }
}
