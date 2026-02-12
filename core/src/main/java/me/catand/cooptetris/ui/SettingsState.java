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
import me.catand.cooptetris.util.LanguageManager;

public class SettingsState implements UIState {
    private Stage stage;
    private Skin skin;
    private Table table;
    private final UIManager uiManager;
    private final ConfigManager configManager;
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
        this.configManager = new ConfigManager();
    }

    @Override
    public void show(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;

        table = new Table();
        table.setFillParent(true);
        table.center();

        LanguageManager lang = LanguageManager.getInstance();

        // 生成标题字体和section字体
        titleFont = uiManager.generateFont(24);
        sectionFont = uiManager.generateFont(19);

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
        difficultyField = new TextField(String.valueOf(configManager.getDifficulty()), skin);

        // 控制设置
        Label controlsLabel;
        if (sectionFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(sectionFont, skin.getColor("font"));
            controlsLabel = new Label(lang.get("controls"), labelStyle);
        } else {
            controlsLabel = new Label(lang.get("controls"), skin);
        }

        Label leftKeyLabel = new Label(lang.get("left"), skin);
        leftKeyField = new TextField(configManager.getLeftKey(), skin);

        Label rightKeyLabel = new Label(lang.get("right"), skin);
        rightKeyField = new TextField(configManager.getRightKey(), skin);

        Label downKeyLabel = new Label(lang.get("down"), skin);
        downKeyField = new TextField(configManager.getDownKey(), skin);

        Label rotateKeyLabel = new Label(lang.get("rotate"), skin);
        rotateKeyField = new TextField(configManager.getRotateKey(), skin);

        Label dropKeyLabel = new Label(lang.get("drop"), skin);
        dropKeyField = new TextField(configManager.getDropKey(), skin);

        // 网络设置
        Label networkLabel;
        if (sectionFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(sectionFont, skin.getColor("font"));
            networkLabel = new Label(lang.get("network"), labelStyle);
        } else {
            networkLabel = new Label(lang.get("network"), skin);
        }

        Label defaultHostLabel = new Label(lang.get("default.host"), skin);
        defaultHostField = new TextField(configManager.getDefaultHost(), skin);

        Label defaultPortLabel = new Label(lang.get("default.port"), skin);
        defaultPortField = new TextField(String.valueOf(configManager.getDefaultPort()), skin);

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

        table.add(title).padBottom(30f).row();

        // 难度设置
        table.add(difficultyLabel).right().padRight(10f);
        table.add(difficultyField).width(100f).padBottom(20f).row();

        // 控制设置
        table.add(controlsLabel).colspan(2).padBottom(10f).row();
        table.add(leftKeyLabel).right().padRight(10f);
        table.add(leftKeyField).width(100f).padBottom(10f).row();
        table.add(rightKeyLabel).right().padRight(10f);
        table.add(rightKeyField).width(100f).padBottom(10f).row();
        table.add(downKeyLabel).right().padRight(10f);
        table.add(downKeyField).width(100f).padBottom(10f).row();
        table.add(rotateKeyLabel).right().padRight(10f);
        table.add(rotateKeyField).width(100f).padBottom(10f).row();
        table.add(dropKeyLabel).right().padRight(10f);
        table.add(dropKeyField).width(100f).padBottom(20f).row();

        // 网络设置
        table.add(networkLabel).colspan(2).padBottom(10f).row();
        table.add(defaultHostLabel).right().padRight(10f);
        table.add(defaultHostField).width(200f).padBottom(10f).row();
        table.add(defaultPortLabel).right().padRight(10f);
        table.add(defaultPortField).width(100f).padBottom(20f).row();

        // 语言设置
        table.add(languageLabel).right().padRight(10f);
        table.add(languageBox).width(150f).padBottom(20f).row();

        // 按钮
        table.add(saveButton).width(150f).padBottom(10f).row();
        table.add(backButton).width(150f).row();

        stage.addActor(table);
    }

    private void saveSettings() {
        // 保存设置到配置文件
        int difficulty = Integer.parseInt(difficultyField.getText());
        String leftKey = leftKeyField.getText();
        String rightKey = rightKeyField.getText();
        String downKey = downKeyField.getText();
        String rotateKey = rotateKeyField.getText();
        String dropKey = dropKeyField.getText();
        String defaultHost = defaultHostField.getText();
        int defaultPort = Integer.parseInt(defaultPortField.getText());

        // 获取当前选择的语言
        LanguageManager lang = LanguageManager.getInstance();
        String currentLanguage = lang.getCurrentLanguageCode();

        configManager.saveSettings(difficulty, leftKey, rightKey, downKey, rotateKey, dropKey, defaultHost, defaultPort, currentLanguage);
    }

    /**
     * 立即保存语言设置
     */
    private void saveLanguageSetting() {
        // 获取当前选择的语言
        LanguageManager lang = LanguageManager.getInstance();
        String currentLanguage = lang.getCurrentLanguageCode();

        // 保存语言设置，使用当前的其他设置值
        int difficulty = Integer.parseInt(difficultyField.getText());
        String leftKey = leftKeyField.getText();
        String rightKey = rightKeyField.getText();
        String downKey = downKeyField.getText();
        String rotateKey = rotateKeyField.getText();
        String dropKey = dropKeyField.getText();
        String defaultHost = defaultHostField.getText();
        int defaultPort = Integer.parseInt(defaultPortField.getText());

        configManager.saveSettings(difficulty, leftKey, rightKey, downKey, rotateKey, dropKey, defaultHost, defaultPort, currentLanguage);
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
        // 自动处理大小调整
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
