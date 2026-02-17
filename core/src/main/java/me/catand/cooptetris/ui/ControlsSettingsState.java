package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

public class ControlsSettingsState extends BaseUIState {
    private Table table;
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
        titleFont = Main.platform.getFont(titleFontSize, lang.get("controls.title"), false, false);
        sectionFont = Main.platform.getFont(sectionFontSize, "Section Title", false, false);

        // 创建标题
        Label title;
        if (titleFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(titleFont, skin.getColor("font"));
            title = new Label(lang.get("controls.settings"), labelStyle);
        } else {
            title = new Label(lang.get("controls.settings"), skin);
        }

        // 创建控制设置区域
        Table controlsContentTable = new Table();

        // 创建标签
        Label leftKeyLabel = new Label(lang.get("left"), skin);
        Label rightKeyLabel = new Label(lang.get("right"), skin);
        Label downKeyLabel = new Label(lang.get("down"), skin);
        Label rotateKeyLabel = new Label(lang.get("rotate"), skin);
        Label dropKeyLabel = new Label(lang.get("drop"), skin);

        // 创建文本字段
        leftKeyField = new TextField(TetrisSettings.leftKey(), skin);
        rightKeyField = new TextField(TetrisSettings.rightKey(), skin);
        downKeyField = new TextField(TetrisSettings.downKey(), skin);
        rotateKeyField = new TextField(TetrisSettings.rotateKey(), skin);
        dropKeyField = new TextField(TetrisSettings.dropKey(), skin);
        leftKey2Field = new TextField(TetrisSettings.leftKey2(), skin);
        rightKey2Field = new TextField(TetrisSettings.rightKey2(), skin);
        downKey2Field = new TextField(TetrisSettings.downKey2(), skin);
        rotateKey2Field = new TextField(TetrisSettings.rotateKey2(), skin);
        dropKey2Field = new TextField(TetrisSettings.dropKey2(), skin);

        // 使用UIScaler缩放间距和元素大小
        float padBottomTitle = toScreenHeight(30f);
        float padRight = toScreenWidth(10f);
        float padLeft = toScreenWidth(10f);
        float padBottomRow = toScreenHeight(15f);
        float smallFieldWidth = toScreenWidth(80f);
        float buttonWidth = toScreenWidth(150f);
        float scrollPaneHeight = toScreenHeight(300f);

        // 添加控制设置行
        controlsContentTable.add(leftKeyLabel).right().padRight(padRight);
        controlsContentTable.add(leftKeyField).width(smallFieldWidth).padRight(padLeft * 2);
        controlsContentTable.add(leftKey2Field).width(smallFieldWidth).padBottom(padBottomRow).row();

        controlsContentTable.add(rightKeyLabel).right().padRight(padRight);
        controlsContentTable.add(rightKeyField).width(smallFieldWidth).padRight(padLeft * 2);
        controlsContentTable.add(rightKey2Field).width(smallFieldWidth).padBottom(padBottomRow).row();

        controlsContentTable.add(downKeyLabel).right().padRight(padRight);
        controlsContentTable.add(downKeyField).width(smallFieldWidth).padRight(padLeft * 2);
        controlsContentTable.add(downKey2Field).width(smallFieldWidth).padBottom(padBottomRow).row();

        controlsContentTable.add(rotateKeyLabel).right().padRight(padRight);
        controlsContentTable.add(rotateKeyField).width(smallFieldWidth).padRight(padLeft * 2);
        controlsContentTable.add(rotateKey2Field).width(smallFieldWidth).padBottom(padBottomRow).row();

        controlsContentTable.add(dropKeyLabel).right().padRight(padRight);
        controlsContentTable.add(dropKeyField).width(smallFieldWidth).padRight(padLeft * 2);
        controlsContentTable.add(dropKey2Field).width(smallFieldWidth).padBottom(padBottomRow).row();

        // 创建滚动窗
        com.badlogic.gdx.scenes.scene2d.ui.ScrollPane scrollPane = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(controlsContentTable, skin);
        scrollPane.setHeight(scrollPaneHeight);
        scrollPane.setWidth(toScreenWidth(500f));
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, true);

        // 创建按钮
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

        // 组装主表格
        table.add(title).padBottom(padBottomTitle).row();
        table.add(scrollPane).padBottom(padBottomRow).row();
        table.add(saveButton).width(buttonWidth).padBottom(padBottomRow).row();
        table.add(backButton).width(buttonWidth).row();

        stage.addActor(table);
    }

    private void saveSettings() {
        // 保存设置到配置文件
        TetrisSettings.leftKey(leftKeyField.getText());
        TetrisSettings.rightKey(rightKeyField.getText());
        TetrisSettings.downKey(downKeyField.getText());
        TetrisSettings.rotateKey(rotateKeyField.getText());
        TetrisSettings.dropKey(dropKeyField.getText());
        // 保存第二套控制键位
        TetrisSettings.leftKey2(leftKey2Field.getText());
        TetrisSettings.rightKey2(rightKey2Field.getText());
        TetrisSettings.downKey2(downKey2Field.getText());
        TetrisSettings.rotateKey2(rotateKey2Field.getText());
        TetrisSettings.dropKey2(dropKey2Field.getText());
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
