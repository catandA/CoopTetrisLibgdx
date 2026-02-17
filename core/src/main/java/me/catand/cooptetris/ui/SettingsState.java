package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 设置界面状态 - 使用新的简化缩放接口
 */
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
    protected void createUI() {
        table = new Table();
        table.setPosition(offsetX(), offsetY());
        table.setSize(displayWidth(), displayHeight());
        table.center();

        LanguageManager lang = LanguageManager.getInstance();

        // 生成标题字体和section字体，考虑缩放比例
        titleFont = Main.platform.getFont(fontSize(24), lang.get("settings.title"), false, false);
        sectionFont = Main.platform.getFont(fontSize(19), "Section Title", false, false);

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
                recreateUI();
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

        // 使用新的简化方法进行缩放
        table.add(title).padBottom(h(30f)).row();

        // 难度设置
        table.add(difficultyLabel).right().padRight(w(10f));
        table.add(difficultyField).width(w(100f)).height(h(40f)).padBottom(h(20f)).row();

        // 网络设置
        table.add(networkLabel).colspan(2).padBottom(h(10f)).row();
        table.add(defaultHostLabel).right().padRight(w(10f));
        table.add(defaultHostField).width(w(200f)).height(h(40f)).padBottom(h(10f)).row();
        table.add(defaultPortLabel).right().padRight(w(10f));
        table.add(defaultPortField).width(w(100f)).height(h(40f)).padBottom(h(20f)).row();

        // 语言设置
        table.add(languageLabel).right().padRight(w(10f));
        table.add(languageBox).width(w(150f)).height(h(40f)).padBottom(h(20f)).row();

        // 按钮
        table.add(controlsButton).width(w(150f)).height(h(50f)).padBottom(h(10f)).row();
        table.add(saveButton).width(w(150f)).height(h(50f)).padBottom(h(10f)).row();
        table.add(backButton).width(w(150f)).height(h(50f)).row();

        stage.addActor(table);
    }

    @Override
    protected void clearUI() {
        if (table != null) {
            table.remove();
            table = null;
        }
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
    public void update(float delta) {
        // 设置界面不需要更新逻辑
    }

    @Override
    public void dispose() {
        // 释放生成的字体
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
        if (sectionFont != null) {
            sectionFont.dispose();
            sectionFont = null;
        }
    }
}
