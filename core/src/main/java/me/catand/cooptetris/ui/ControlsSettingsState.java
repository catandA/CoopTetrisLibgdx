package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 控制设置界面状态 - 使用新的简化缩放接口
 */
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
    protected void createUI() {
        table = new Table();
        table.setPosition(offsetX(), offsetY());
        table.setSize(displayWidth(), displayHeight());
        table.center();

        LanguageManager lang = LanguageManager.getInstance();

        // 生成标题字体和section字体，考虑缩放比例
        titleFont = Main.platform.getFont(fontSize(24), lang.get("controls.title"), false, false);
        sectionFont = Main.platform.getFont(fontSize(19), "Section Title", false, false);

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

        // 使用新的简化方法进行缩放
        // 添加控制设置行
        controlsContentTable.add(leftKeyLabel).right().padRight(w(10f));
        controlsContentTable.add(leftKeyField).width(w(80f)).padRight(w(20f));
        controlsContentTable.add(leftKey2Field).width(w(80f)).padBottom(h(15f)).row();

        controlsContentTable.add(rightKeyLabel).right().padRight(w(10f));
        controlsContentTable.add(rightKeyField).width(w(80f)).padRight(w(20f));
        controlsContentTable.add(rightKey2Field).width(w(80f)).padBottom(h(15f)).row();

        controlsContentTable.add(downKeyLabel).right().padRight(w(10f));
        controlsContentTable.add(downKeyField).width(w(80f)).padRight(w(20f));
        controlsContentTable.add(downKey2Field).width(w(80f)).padBottom(h(15f)).row();

        controlsContentTable.add(rotateKeyLabel).right().padRight(w(10f));
        controlsContentTable.add(rotateKeyField).width(w(80f)).padRight(w(20f));
        controlsContentTable.add(rotateKey2Field).width(w(80f)).padBottom(h(15f)).row();

        controlsContentTable.add(dropKeyLabel).right().padRight(w(10f));
        controlsContentTable.add(dropKeyField).width(w(80f)).padRight(w(20f));
        controlsContentTable.add(dropKey2Field).width(w(80f)).padBottom(h(15f)).row();

        // 创建滚动窗
        com.badlogic.gdx.scenes.scene2d.ui.ScrollPane scrollPane = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(controlsContentTable, skin);
        scrollPane.setHeight(h(300f));
        scrollPane.setWidth(w(500f));
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
        table.add(title).padBottom(h(30f)).row();
        table.add(scrollPane).padBottom(h(15f)).row();
        table.add(saveButton).width(w(150f)).padBottom(h(15f)).row();
        table.add(backButton).width(w(150f)).row();

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
