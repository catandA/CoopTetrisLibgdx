package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import me.catand.cooptetris.util.ConfigManager;
import me.catand.cooptetris.util.Config;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.UIScaler;

public class ControlsSettingsState implements UIState {
    private Stage stage;
    private Skin skin;
    private Table table;
    private final UIManager uiManager;
    private ConfigManager configManager;
    private Config config;
    private TextField leftKeyField;
    private TextField rightKeyField;
    private TextField downKeyField;
    private TextField rotateKeyField;
    private TextField dropKeyField;
    // 第二套控制键位
    private TextField leftKey2Field;
    private TextField rightKey2Field;
    private TextField downKey2Field;
    private TextField rotateKey2Field;
    private TextField dropKey2Field;
    private BitmapFont titleFont;
    private BitmapFont sectionFont;

    public ControlsSettingsState(UIManager uiManager) {
        this.uiManager = uiManager;
        this.configManager = uiManager.getConfigManager();
    }

    @Override
    public void show(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;

        // 使用UIScaler获取缩放比例
        UIScaler scaler = UIScaler.getInstance();
        float scale = scaler.getScale();

        table = new Table();
        table.setFillParent(true);
        table.center();

        LanguageManager lang = LanguageManager.getInstance();

        // 生成标题字体和section字体，考虑缩放比例
        int titleFontSize = (int) (24 * scale);
        int sectionFontSize = (int) (19 * scale);
        titleFont = uiManager.generateFont(titleFontSize);
        sectionFont = uiManager.generateFont(sectionFontSize);

        // 获取配置
        config = configManager.getConfig();

        // 创建标题
        Label title;
        if (titleFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(titleFont, skin.getColor("font"));
            title = new Label(lang.get("controls.settings"), labelStyle);
        } else {
            title = new Label(lang.get("controls.settings"), skin);
        }

        // 控制设置
        Label controlsLabel;
        if (sectionFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(sectionFont, skin.getColor("font"));
            controlsLabel = new Label(lang.get("controls"), labelStyle);
        } else {
            controlsLabel = new Label(lang.get("controls"), skin);
        }

        Label leftKeyLabel = new Label(lang.get("left"), skin);
        leftKeyField = new TextField(config.getLeftKey(), skin);

        Label rightKeyLabel = new Label(lang.get("right"), skin);
        rightKeyField = new TextField(config.getRightKey(), skin);

        Label downKeyLabel = new Label(lang.get("down"), skin);
        downKeyField = new TextField(config.getDownKey(), skin);

        Label rotateKeyLabel = new Label(lang.get("rotate"), skin);
        rotateKeyField = new TextField(config.getRotateKey(), skin);

        Label dropKeyLabel = new Label(lang.get("drop"), skin);
        dropKeyField = new TextField(config.getDropKey(), skin);

        // 第二套控制键位
        Label controls2Label;
        if (sectionFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(sectionFont, skin.getColor("font"));
            controls2Label = new Label(lang.get("controls.2"), labelStyle);
        } else {
            controls2Label = new Label(lang.get("controls.2"), skin);
        }

        Label leftKey2Label = new Label(lang.get("left"), skin);
        leftKey2Field = new TextField(config.getLeftKey2(), skin);

        Label rightKey2Label = new Label(lang.get("right"), skin);
        rightKey2Field = new TextField(config.getRightKey2(), skin);

        Label downKey2Label = new Label(lang.get("down"), skin);
        downKey2Field = new TextField(config.getDownKey2(), skin);

        Label rotateKey2Label = new Label(lang.get("rotate"), skin);
        rotateKey2Field = new TextField(config.getRotateKey2(), skin);

        Label dropKey2Label = new Label(lang.get("drop"), skin);
        dropKey2Field = new TextField(config.getDropKey2(), skin);

        TextButton saveButton = new TextButton(lang.get("save"), skin);
        saveButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                // 保存设置
                saveSettings();
            }
            return true;
        });

        TextButton backButton = new TextButton(lang.get("back"), skin);
        backButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                uiManager.popState();
            }
            return true;
        });

        // 使用UIScaler缩放间距和元素大小
        float padBottomTitle = scaler.toScreenHeight(30f);
        float padRight = scaler.toScreenWidth(10f);
        float padBottomField = scaler.toScreenHeight(10f);
        float padBottomSection = scaler.toScreenHeight(20f);
        float smallFieldWidth = scaler.toScreenWidth(100f);
        float buttonWidth = scaler.toScreenWidth(150f);

        table.add(title).padBottom(padBottomTitle).row();

        // 控制设置
        table.add(controlsLabel).colspan(2).padBottom(padBottomField).row();
        table.add(leftKeyLabel).right().padRight(padRight);
        table.add(leftKeyField).width(smallFieldWidth).padBottom(padBottomField).row();
        table.add(rightKeyLabel).right().padRight(padRight);
        table.add(rightKeyField).width(smallFieldWidth).padBottom(padBottomField).row();
        table.add(downKeyLabel).right().padRight(padRight);
        table.add(downKeyField).width(smallFieldWidth).padBottom(padBottomField).row();
        table.add(rotateKeyLabel).right().padRight(padRight);
        table.add(rotateKeyField).width(smallFieldWidth).padBottom(padBottomField).row();
        table.add(dropKeyLabel).right().padRight(padRight);
        table.add(dropKeyField).width(smallFieldWidth).padBottom(padBottomField).row();
        
        // 第二套控制设置
        table.add(controls2Label).colspan(2).padBottom(padBottomField).row();
        table.add(leftKey2Label).right().padRight(padRight);
        table.add(leftKey2Field).width(smallFieldWidth).padBottom(padBottomField).row();
        table.add(rightKey2Label).right().padRight(padRight);
        table.add(rightKey2Field).width(smallFieldWidth).padBottom(padBottomField).row();
        table.add(downKey2Label).right().padRight(padRight);
        table.add(downKey2Field).width(smallFieldWidth).padBottom(padBottomField).row();
        table.add(rotateKey2Label).right().padRight(padRight);
        table.add(rotateKey2Field).width(smallFieldWidth).padBottom(padBottomField).row();
        table.add(dropKey2Label).right().padRight(padRight);
        table.add(dropKey2Field).width(smallFieldWidth).padBottom(padBottomSection).row();

        // 按钮
        table.add(saveButton).width(buttonWidth).padBottom(padBottomField).row();
        table.add(backButton).width(buttonWidth).row();

        stage.addActor(table);
    }

    private void saveSettings() {
        // 保存设置到配置文件
        config.setLeftKey(leftKeyField.getText());
        config.setRightKey(rightKeyField.getText());
        config.setDownKey(downKeyField.getText());
        config.setRotateKey(rotateKeyField.getText());
        config.setDropKey(dropKeyField.getText());
        // 保存第二套控制键位
        config.setLeftKey2(leftKey2Field.getText());
        config.setRightKey2(rightKey2Field.getText());
        config.setDownKey2(downKey2Field.getText());
        config.setRotateKey2(rotateKey2Field.getText());
        config.setDropKey2(dropKey2Field.getText());

        // 保存配置
        configManager.saveSettings(config);
    }

    @Override
    public void hide() {
        table.remove();
    }

    @Override
    public void update(float delta) {
        // 设置界面不需要更新逻辑
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
        // 释放生成的字体
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (sectionFont != null) {
            sectionFont.dispose();
        }
    }
}