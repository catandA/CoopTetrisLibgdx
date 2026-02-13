package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import me.catand.cooptetris.util.ConfigManager;
import me.catand.cooptetris.util.Config;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.UIScaler;

public class SettingsState implements UIState {
    private Stage stage;
    private Skin skin;
    private Table table;
    private final UIManager uiManager;
    private ConfigManager configManager;
    private Config config;
    private TextField difficultyField;
    private TextField leftKeyField;
    private TextField rightKeyField;
    private TextField downKeyField;
    private TextField rotateKeyField;
    private TextField dropKeyField;
    private TextField defaultHostField;
    private TextField defaultPortField;
    private com.badlogic.gdx.scenes.scene2d.ui.SelectBox<String> languageBox;
    private BitmapFont titleFont;
    private BitmapFont sectionFont;

    public SettingsState(UIManager uiManager) {
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
            title = new Label(lang.get("settings.title"), labelStyle);
        } else {
            title = new Label(lang.get("settings.title"), skin);
        }

        // 难度设置
        Label difficultyLabel = new Label(lang.get("difficulty"), skin);
        difficultyField = new TextField(String.valueOf(config.getDifficulty()), skin);

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

        // 网络设置
        Label networkLabel;
        if (sectionFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(sectionFont, skin.getColor("font"));
            networkLabel = new Label(lang.get("network"), labelStyle);
        } else {
            networkLabel = new Label(lang.get("network"), skin);
        }

        Label defaultHostLabel = new Label(lang.get("default.host"), skin);
        defaultHostField = new TextField(config.getDefaultHost(), skin);

        Label defaultPortLabel = new Label(lang.get("default.port"), skin);
        defaultPortField = new TextField(String.valueOf(config.getDefaultPort()), skin);

        // 语言设置
        Label languageLabel = new Label(lang.get("language"), skin);
        languageBox = new com.badlogic.gdx.scenes.scene2d.ui.SelectBox<>(skin);
        String[] languages = {lang.get("en"), lang.get("zh")};
        languageBox.setItems(languages);
        // 设置当前选中的语言
        if (lang.getCurrentLanguageCode().equals("zh")) {
            languageBox.setSelectedIndex(1);
        } else {
            languageBox.setSelectedIndex(0);
        }
        // 添加语言切换监听器
        languageBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                int selectedIndex = languageBox.getSelectedIndex();
                if (selectedIndex == 0) {
                    lang.setLanguage("en");
                } else if (selectedIndex == 1) {
                    lang.setLanguage("zh");
                }

                // 立即保存语言设置，确保持久化
                saveLanguageSetting();

                // 重新加载当前界面以更新语言
                hide();
                show(stage, skin);
            }
        });

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
        float mediumFieldWidth = scaler.toScreenWidth(150f);
        float largeFieldWidth = scaler.toScreenWidth(200f);
        float buttonWidth = scaler.toScreenWidth(150f);

        table.add(title).padBottom(padBottomTitle).row();

        // 难度设置
        table.add(difficultyLabel).right().padRight(padRight);
        table.add(difficultyField).width(smallFieldWidth).padBottom(padBottomSection).row();

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
        table.add(dropKeyField).width(smallFieldWidth).padBottom(padBottomSection).row();

        // 网络设置
        table.add(networkLabel).colspan(2).padBottom(padBottomField).row();
        table.add(defaultHostLabel).right().padRight(padRight);
        table.add(defaultHostField).width(largeFieldWidth).padBottom(padBottomField).row();
        table.add(defaultPortLabel).right().padRight(padRight);
        table.add(defaultPortField).width(smallFieldWidth).padBottom(padBottomSection).row();

        // 语言设置
        table.add(languageLabel).right().padRight(padRight);
        table.add(languageBox).width(mediumFieldWidth).padBottom(padBottomSection).row();

        // 按钮
        table.add(saveButton).width(buttonWidth).padBottom(padBottomField).row();
        table.add(backButton).width(buttonWidth).row();

        stage.addActor(table);
    }

    private void saveSettings() {
        // 保存设置到配置文件
        config.setDifficulty(Integer.parseInt(difficultyField.getText()));
        config.setLeftKey(leftKeyField.getText());
        config.setRightKey(rightKeyField.getText());
        config.setDownKey(downKeyField.getText());
        config.setRotateKey(rotateKeyField.getText());
        config.setDropKey(dropKeyField.getText());
        config.setDefaultHost(defaultHostField.getText());
        config.setDefaultPort(Integer.parseInt(defaultPortField.getText()));

        // 获取当前选择的语言
        LanguageManager lang = LanguageManager.getInstance();
        config.setLanguage(lang.getCurrentLanguageCode());

        // 保存配置
        configManager.saveSettings(config);
    }

    /**
     * 立即保存语言设置
     */
    private void saveLanguageSetting() {
        // 获取当前选择的语言
        LanguageManager lang = LanguageManager.getInstance();
        config.setLanguage(lang.getCurrentLanguageCode());

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
