package me.catand.cooptetris.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.UIScaler;

public class MainMenuState implements UIState {
    private Stage stage;
    private Skin skin;
    private Table table;
    private final UIManager uiManager;
    private BitmapFont titleFont;

    public MainMenuState(UIManager uiManager) {
        this.uiManager = uiManager;
    }

    @Override
    public void show(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;

        table = new Table();
        table.setFillParent(true);
        table.center();

        // 使用UIScaler获取缩放比例
        UIScaler scaler = UIScaler.getInstance();
        float scale = scaler.getScale();

        LanguageManager lang = LanguageManager.getInstance();

        // 为标题创建一个更大的字体，考虑缩放比例
        int titleFontSize = (int) (32 * scale);
        titleFont = uiManager.generateFont(titleFontSize);

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
                // 进入联机模式界面
                uiManager.pushState(new OnlineMenuState(uiManager));
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

        // 使用UIScaler缩放按钮大小和间距
        float buttonWidth = scaler.toScreenWidth(200f);
        float buttonHeight = scaler.toScreenHeight(60f);
        float padBottomTitle = scaler.toScreenHeight(50f);
        float padBottomButton = scaler.toScreenHeight(20f);

        table.add(title).padBottom(padBottomTitle).row();
        table.add(startButton).width(buttonWidth).height(buttonHeight).padBottom(padBottomButton).row();
        table.add(onlineButton).width(buttonWidth).height(buttonHeight).padBottom(padBottomButton).row();
        table.add(settingsButton).width(buttonWidth).height(buttonHeight).padBottom(padBottomButton).row();
        table.add(exitButton).width(buttonWidth).height(buttonHeight).row();

        stage.addActor(table);
    }

    @Override
    public void hide() {
        table.remove();
    }

    @Override
    public void update(float delta) {
        // 主菜单不需要更新逻辑
    }

    @Override
    public void resize(int width, int height) {
        // 更新UIScaler
        UIScaler.getInstance().update();
        
        // 重新创建表格，确保UI元素正确缩放
        if (table != null) {
            table.remove();
            show(stage, skin);
        }
    }

    @Override
    public void dispose() {
        // 释放生成的标题字体
        if (titleFont != null) {
            titleFont.dispose();
        }
    }
}
