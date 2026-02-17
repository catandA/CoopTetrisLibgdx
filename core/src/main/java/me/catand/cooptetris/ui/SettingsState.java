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

import me.catand.cooptetris.Main;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

public class SettingsState extends BaseUIState {
    private Table table;
    private TextField difficultyField;
    private TextField defaultHostField;
    private TextField defaultPortField;
    private com.badlogic.gdx.scenes.scene2d.ui.SelectBox<String> languageBox;
    private BitmapFont titleFont;
    private BitmapFont sectionFont;

    public SettingsState(UIManager uiManager) {
        super(uiManager);
    }

    @Override
    public void show(Stage stage, Skin skin) {
        super.show(stage, skin);

        table = new Table();
        table.setFillParent(true);
        table.center();

        LanguageManager lang = LanguageManager.getInstance();

        // 生成标题字体和section字体，考虑缩放比例
        int titleFontSize = (int) (24 * getScale());
        int sectionFontSize = (int) (19 * getScale());
        titleFont = Main.platform.getFont(titleFontSize, lang.get("settings.title"), false, false);
        sectionFont = Main.platform.getFont(sectionFontSize, "Section Title", false, false);

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
        difficultyField = new TextField(String.valueOf(TetrisSettings.difficulty()), skin);

        // 网络设置
        Label networkLabel;
        if (sectionFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(sectionFont, skin.getColor("font"));
            networkLabel = new Label(lang.get("network"), labelStyle);
        } else {
            networkLabel = new Label(lang.get("network"), skin);
        }

        Label defaultHostLabel = new Label(lang.get("default.host"), skin);
        defaultHostField = new TextField(TetrisSettings.defaultHost(), skin);

        Label defaultPortLabel = new Label(lang.get("default.port"), skin);
        defaultPortField = new TextField(String.valueOf(TetrisSettings.defaultPort()), skin);

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

        TextButton controlsButton = new TextButton(lang.get("controls"), skin);
        controlsButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                // 打开控制设置界面
                uiManager.pushState(new ControlsSettingsState(uiManager));
            }
            return true;
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
        float padBottomTitle = toScreenHeight(30f);
        float padRight = toScreenWidth(10f);
        float padBottomField = toScreenHeight(10f);
        float padBottomSection = toScreenHeight(20f);
        float smallFieldWidth = toScreenWidth(100f);
        float mediumFieldWidth = toScreenWidth(150f);
        float largeFieldWidth = toScreenWidth(200f);
        float buttonWidth = toScreenWidth(150f);

        table.add(title).padBottom(padBottomTitle).row();

        // 难度设置
        table.add(difficultyLabel).right().padRight(padRight);
        table.add(difficultyField).width(smallFieldWidth).padBottom(padBottomSection).row();

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
        table.add(controlsButton).width(buttonWidth).padBottom(padBottomField).row();
        table.add(saveButton).width(buttonWidth).padBottom(padBottomField).row();
        table.add(backButton).width(buttonWidth).row();

        stage.addActor(table);
    }

    private void saveSettings() {
        // 保存设置到配置文件
        TetrisSettings.difficulty(Integer.parseInt(difficultyField.getText()));
        TetrisSettings.defaultHost(defaultHostField.getText());
        TetrisSettings.defaultPort(Integer.parseInt(defaultPortField.getText()));

        // 获取当前选择的语言
        LanguageManager lang = LanguageManager.getInstance();
        TetrisSettings.language(lang.getCurrentLanguageCode());
    }

    /**
     * 立即保存语言设置
     */
    private void saveLanguageSetting() {
        // 获取当前选择的语言
        LanguageManager lang = LanguageManager.getInstance();
        TetrisSettings.language(lang.getCurrentLanguageCode());
    }

    @Override
    public void hide() {
        if (table != null) {
            table.remove();
        }
    }

    @Override
    public void update(float delta) {
        // 设置界面不需要更新逻辑
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
