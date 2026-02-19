package me.catand.cooptetris.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.input.InputBinding;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 控制设置界面状态 - 支持键盘和鼠标输入
 */
public class ControlsSettingsState extends BaseUIState {
    private Table rootTable;
    private InputBindingButton[] player1Buttons;
    private InputBindingButton[] player2Buttons;
    private BitmapFont titleFont;
    private BitmapFont sectionFont;

    // 等待输入状态
    private InputBindingButton waitingButton = null;
    private Label waitingLabel;
    private Label instructionLabel;

    // 按键标签
    private static final String[] ACTION_LABELS = {"left", "right", "down", "rotate", "drop"};

    public ControlsSettingsState(UIManager uiManager) {
        super(uiManager);
    }

    @Override
    protected void createUI() {
        LanguageManager lang = LanguageManager.getInstance();

        // 生成字体
        titleFont = Main.platform.getFont(fontSize(28), lang.get("controls.settings"), false, false);
        sectionFont = Main.platform.getFont(fontSize(20), "Player", false, false);

        // 创建根表格
        rootTable = new Table();
        rootTable.setFillParent(true);

        // 创建标题
        Label titleLabel = createTitleLabel(lang.get("controls.settings"));

        // 创建提示标签
        waitingLabel = new Label("", skin);
        waitingLabel.setVisible(false);
        waitingLabel.setColor(Color.YELLOW);

        instructionLabel = new Label(lang.get("controls.press_key"), skin);
        instructionLabel.setVisible(false);
        instructionLabel.setColor(Color.LIGHT_GRAY);

        // 创建玩家控制区域
        Table playersTable = new Table();

        // 玩家1控制区域
        Table player1Table = createPlayerControlTable(
                lang.get("player1") + " " + lang.get("controls"),
                new InputBinding[]{
                        TetrisSettings.leftKey(),
                        TetrisSettings.rightKey(),
                        TetrisSettings.downKey(),
                        TetrisSettings.rotateKey(),
                        TetrisSettings.dropKey()
                },
                buttons -> player1Buttons = buttons
        );

        // 玩家2控制区域
        Table player2Table = createPlayerControlTable(
                lang.get("player2") + " " + lang.get("controls"),
                new InputBinding[]{
                        TetrisSettings.leftKey2(),
                        TetrisSettings.rightKey2(),
                        TetrisSettings.downKey2(),
                        TetrisSettings.rotateKey2(),
                        TetrisSettings.dropKey2()
                },
                buttons -> player2Buttons = buttons
        );

        // 添加玩家控制区域到主表格
        playersTable.add(player1Table).padRight(w(30f));
        playersTable.add(player2Table);

        // 创建按钮区域
        Table buttonTable = new Table();

        TextButton saveButton = new TextButton(lang.get("save"), skin);
        addCyanHoverEffect(saveButton);
        saveButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                saveSettings();
            }
            return true;
        });

        TextButton resetButton = new TextButton(lang.get("reset_default"), skin);
        addCyanHoverEffect(resetButton);
        resetButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                resetToDefaults();
            }
            return true;
        });

        TextButton backButton = new TextButton(lang.get("back"), skin);
        addCyanHoverEffect(backButton);
        backButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                uiManager.popState();
            }
            return true;
        });

        buttonTable.add(saveButton).width(w(120f)).height(h(45f)).padRight(w(10f));
        buttonTable.add(resetButton).width(w(120f)).height(h(45f)).padRight(w(10f));
        buttonTable.add(backButton).width(w(120f)).height(h(45f));

        // 组装主表格
        rootTable.add(titleLabel).padBottom(h(20f)).row();
        rootTable.add(waitingLabel).padBottom(h(5f)).row();
        rootTable.add(instructionLabel).padBottom(h(15f)).row();
        rootTable.add(playersTable).padBottom(h(30f)).row();
        rootTable.add(buttonTable);

        stage.addActor(rootTable);
    }

    private Label createTitleLabel(String text) {
        if (titleFont != null) {
            Label.LabelStyle style = new Label.LabelStyle(titleFont, Color.WHITE);
            return new Label(text, style);
        }
        return new Label(text, skin);
    }

    private Label createSectionLabel(String text) {
        if (sectionFont != null) {
            Label.LabelStyle style = new Label.LabelStyle(sectionFont, Color.WHITE);
            return new Label(text, style);
        }
        return new Label(text, skin);
    }

    private Table createPlayerControlTable(String title, InputBinding[] initialBindings,
                                           java.util.function.Consumer<InputBindingButton[]> callback) {
        LanguageManager lang = LanguageManager.getInstance();
        Table table = new Table();

        // 添加标题背景
        Table headerTable = new Table();
        headerTable.setBackground(skin.newDrawable("white", new Color(0.2f, 0.2f, 0.3f, 0.8f)));

        Label titleLabel = createSectionLabel(title);
        headerTable.add(titleLabel).pad(w(10f));

        table.add(headerTable).fillX().padBottom(h(15f)).row();

        // 创建按键绑定按钮数组
        InputBindingButton[] buttons = new InputBindingButton[5];

        // 添加每个动作的按键绑定行
        for (int i = 0; i < ACTION_LABELS.length; i++) {
            Table rowTable = new Table();

            // 动作标签
            Label actionLabel = new Label(lang.get(ACTION_LABELS[i]), skin);
            actionLabel.setColor(Color.LIGHT_GRAY);

            // 按键绑定按钮
            final int index = i;
            InputBindingButton keyButton = new InputBindingButton(
                    initialBindings[i],
                    skin.get(TextButton.TextButtonStyle.class)
            );
            keyButton.onClick(() -> startWaitingForKey(keyButton, ACTION_LABELS[index]));
            buttons[i] = keyButton;

            rowTable.add(actionLabel).left().width(w(80f)).padRight(w(15f));
            rowTable.add(keyButton).width(w(100f)).height(h(45f));

            table.add(rowTable).fillX().padBottom(h(10f)).row();
        }

        callback.accept(buttons);
        return table;
    }

    private void startWaitingForKey(InputBindingButton button, String actionName) {
        LanguageManager lang = LanguageManager.getInstance();

        // 如果已经在等待输入，先恢复之前的按钮
        if (waitingButton != null) {
            waitingButton.cancelWaiting();
        }

        waitingButton = button;
        button.startWaiting();

        // 更新提示文本
        waitingLabel.setText(lang.get("waiting_for_key") + ": " + lang.get(actionName));
        waitingLabel.setVisible(true);
        instructionLabel.setVisible(true);
    }

    private void cancelWaiting() {
        if (waitingButton != null) {
            waitingButton.cancelWaiting();
            waitingButton = null;
            waitingLabel.setVisible(false);
            instructionLabel.setVisible(false);
        }
    }

    private void setInputBinding(InputBinding inputBinding) {
        if (waitingButton == null || inputBinding == null) return;

        waitingButton.setInputBinding(inputBinding);
        waitingButton = null;
        waitingLabel.setVisible(false);
        instructionLabel.setVisible(false);
    }

    private void resetToDefaults() {
        LanguageManager lang = LanguageManager.getInstance();

        // 重置设置
        TetrisSettings.resetToDefaults();

        // 更新按钮显示
        updateButtonDisplay();

        // 显示提示
        waitingLabel.setText(lang.get("settings_reset"));
        waitingLabel.setColor(Color.GREEN);
        waitingLabel.setVisible(true);

        // 2秒后隐藏提示
        Gdx.app.postRunnable(() -> {
            try {
                Thread.sleep(2000);
                Gdx.app.postRunnable(() -> {
                    waitingLabel.setVisible(false);
                    waitingLabel.setColor(Color.YELLOW);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateButtonDisplay() {
        // 更新玩家1按钮
        player1Buttons[0].setInputBinding(TetrisSettings.leftKey());
        player1Buttons[1].setInputBinding(TetrisSettings.rightKey());
        player1Buttons[2].setInputBinding(TetrisSettings.downKey());
        player1Buttons[3].setInputBinding(TetrisSettings.rotateKey());
        player1Buttons[4].setInputBinding(TetrisSettings.dropKey());

        // 更新玩家2按钮
        player2Buttons[0].setInputBinding(TetrisSettings.leftKey2());
        player2Buttons[1].setInputBinding(TetrisSettings.rightKey2());
        player2Buttons[2].setInputBinding(TetrisSettings.downKey2());
        player2Buttons[3].setInputBinding(TetrisSettings.rotateKey2());
        player2Buttons[4].setInputBinding(TetrisSettings.dropKey2());
    }

    @Override
    protected void clearUI() {
        if (rootTable != null) {
            rootTable.remove();
            rootTable = null;
        }
    }

    private void saveSettings() {
        TetrisSettings.leftKey(player1Buttons[0].getInputBinding());
        TetrisSettings.rightKey(player1Buttons[1].getInputBinding());
        TetrisSettings.downKey(player1Buttons[2].getInputBinding());
        TetrisSettings.rotateKey(player1Buttons[3].getInputBinding());
        TetrisSettings.dropKey(player1Buttons[4].getInputBinding());

        TetrisSettings.leftKey2(player2Buttons[0].getInputBinding());
        TetrisSettings.rightKey2(player2Buttons[1].getInputBinding());
        TetrisSettings.downKey2(player2Buttons[2].getInputBinding());
        TetrisSettings.rotateKey2(player2Buttons[3].getInputBinding());
        TetrisSettings.dropKey2(player2Buttons[4].getInputBinding());

        // 显示保存成功提示
        LanguageManager lang = LanguageManager.getInstance();
        waitingLabel.setText(lang.get("settings_saved"));
        waitingLabel.setColor(Color.GREEN);
        waitingLabel.setVisible(true);

        // 2秒后隐藏
        Gdx.app.postRunnable(() -> {
            try {
                Thread.sleep(2000);
                Gdx.app.postRunnable(() -> {
                    waitingLabel.setVisible(false);
                    waitingLabel.setColor(Color.YELLOW);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void update(float delta) {
        // 检查是否有按键输入
        if (waitingButton != null) {
            // 检查ESC键取消
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                cancelWaiting();
                return;
            }

            // 检查键盘按键
            for (int i = 0; i < 256; i++) {
                if (Gdx.input.isKeyJustPressed(i)) {
                    InputBinding inputBinding = InputBinding.fromKeyCode(i);
                    if (inputBinding != null) {
                        setInputBinding(inputBinding);
                    }
                    return;
                }
            }

            // 检查鼠标按钮
            for (int i = 0; i < 5; i++) {
                if (Gdx.input.isButtonJustPressed(i)) {
                    InputBinding inputBinding = InputBinding.fromMouseButton(i);
                    if (inputBinding != null) {
                        setInputBinding(inputBinding);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
        if (sectionFont != null) {
            sectionFont.dispose();
            sectionFont = null;
        }
    }

    /**
     * 自定义输入绑定按钮类
     */
    private class InputBindingButton extends TextButton {
        private InputBinding inputBinding;
        private Runnable onClickAction;

        public InputBindingButton(InputBinding inputBinding, TextButtonStyle style) {
            super(inputBinding.getShortName(), style);
            this.inputBinding = inputBinding;

            // 设置按钮样式
            setColor(Color.WHITE);

            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (onClickAction != null) {
                        onClickAction.run();
                    }
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                    if (waitingButton == null) {
                        setColor(Color.CYAN);
                    }
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                    if (waitingButton == null) {
                        setColor(Color.WHITE);
                    }
                }
            });
        }

        public void onClick(Runnable action) {
            this.onClickAction = action;
        }

        public InputBinding getInputBinding() {
            return inputBinding;
        }

        public void setInputBinding(InputBinding inputBinding) {
            this.inputBinding = inputBinding;
            setText(inputBinding.getShortName());
            setColor(Color.WHITE);
        }

        public void startWaiting() {
            setText("???");
            setColor(Color.YELLOW);
        }

        public void cancelWaiting() {
            setText(inputBinding.getShortName());
            setColor(Color.WHITE);
        }
    }
}
