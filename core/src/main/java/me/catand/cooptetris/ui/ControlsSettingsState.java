package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.input.InputBinding;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 键位设置界面 - 现代化暗色游戏UI风格
 */
public class ControlsSettingsState extends BaseUIState {

    private Table mainTable;
    private BitmapFont titleFont;
    private BitmapFont sectionFont;

    // 玩家1键位
    private InputBindingButton player1LeftButton;
    private InputBindingButton player1RightButton;
    private InputBindingButton player1DownButton;
    private InputBindingButton player1RotateButton;
    private InputBindingButton player1DropButton;

    // 玩家2键位
    private InputBindingButton player2LeftButton;
    private InputBindingButton player2RightButton;
    private InputBindingButton player2DownButton;
    private InputBindingButton player2RotateButton;
    private InputBindingButton player2DropButton;

    // UI颜色配置
    private static final Color COLOR_BG = new Color(0.08f, 0.09f, 0.11f, 1f);
    private static final Color COLOR_PANEL = new Color(0.12f, 0.14f, 0.17f, 0.95f);
    private static final Color COLOR_PANEL_HIGHLIGHT = new Color(0.15f, 0.17f, 0.21f, 0.95f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);
    private static final Color COLOR_SECONDARY = new Color(0.8f, 0.3f, 0.9f, 1f);
    private static final Color COLOR_SUCCESS = new Color(0.3f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_WARNING = new Color(1f, 0.7f, 0.2f, 1f);
    private static final Color COLOR_DANGER = new Color(1f, 0.3f, 0.3f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color COLOR_TEXT_MUTED = new Color(0.5f, 0.52f, 0.55f, 1f);

    // 统一的尺寸
    private static final float LABEL_WIDTH = 80f;
    private static final float BUTTON_WIDTH = 100f;
    private static final float ROW_HEIGHT = 40f;

    public ControlsSettingsState(UIManager uiManager) {
        super(uiManager);
    }

    @Override
    protected void createUI() {
        LanguageManager lang = LanguageManager.getInstance();

        titleFont = Main.platform.getFont(fontSize(32), lang.get("controls.settings.title"), false, false);
        sectionFont = Main.platform.getFont(fontSize(18), "Section", false, false);

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        mainTable.pad(h(30f));

        // 创建主面板
        Table contentPanel = createMainPanel();
        mainTable.add(contentPanel).expand().center();

        stage.addActor(mainTable);
    }

    private Table createMainPanel() {
        LanguageManager lang = LanguageManager.getInstance();

        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(30f));

        // 标题
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, COLOR_PRIMARY);
        Label titleLabel = new Label(lang.get("controls.settings.title"), titleStyle);
        titleLabel.setAlignment(Align.center);

        // 玩家键位区域
        Table playersTable = new Table();

        // 玩家1卡片
        Table player1Card = createPlayerCard(1,
            TetrisSettings.leftKey(),
            TetrisSettings.rightKey(),
            TetrisSettings.downKey(),
            TetrisSettings.rotateKey(),
            TetrisSettings.dropKey()
        );

        // 玩家2卡片
        Table player2Card = createPlayerCard(2,
            TetrisSettings.leftKey2(),
            TetrisSettings.rightKey2(),
            TetrisSettings.downKey2(),
            TetrisSettings.rotateKey2(),
            TetrisSettings.dropKey2()
        );

        playersTable.add(player1Card).width(w(280f)).padRight(w(20f));
        playersTable.add(player2Card).width(w(280f));

        // 底部按钮
        Table buttonTable = createBottomButtons();

        // 组装主界面
        panel.add(titleLabel).padBottom(h(25f)).row();
        panel.add(playersTable).expandX().padBottom(h(25f)).row();
        panel.add(buttonTable);

        return panel;
    }

    private Table createPlayerCard(int playerNum,
                                   InputBinding left, InputBinding right,
                                   InputBinding down, InputBinding rotate, InputBinding drop) {
        LanguageManager lang = LanguageManager.getInstance();

        Table card = new Table();
        card.setBackground(createPanelBackground(COLOR_PANEL_HIGHLIGHT));
        card.pad(w(20f));

        // 卡片标题
        Label.LabelStyle sectionStyle = new Label.LabelStyle(sectionFont,
            playerNum == 1 ? COLOR_PRIMARY : COLOR_SECONDARY);
        Label cardTitle = new Label(lang.get("player") + " " + playerNum, sectionStyle);

        // 创建键位行
        Table controlsTable = new Table();
        controlsTable.defaults().padBottom(h(10f));

        // 左移
        controlsTable.add(createControlRow(lang.get("control.left"), left, binding -> {
            if (playerNum == 1) {
                player1LeftButton = binding;
            } else {
                player2LeftButton = binding;
            }
        })).row();

        // 右移
        controlsTable.add(createControlRow(lang.get("control.right"), right, binding -> {
            if (playerNum == 1) {
                player1RightButton = binding;
            } else {
                player2RightButton = binding;
            }
        })).row();

        // 下移
        controlsTable.add(createControlRow(lang.get("control.down"), down, binding -> {
            if (playerNum == 1) {
                player1DownButton = binding;
            } else {
                player2DownButton = binding;
            }
        })).row();

        // 旋转
        controlsTable.add(createControlRow(lang.get("control.rotate"), rotate, binding -> {
            if (playerNum == 1) {
                player1RotateButton = binding;
            } else {
                player2RotateButton = binding;
            }
        })).row();

        // 硬降
        controlsTable.add(createControlRow(lang.get("control.drop"), drop, binding -> {
            if (playerNum == 1) {
                player1DropButton = binding;
            } else {
                player2DropButton = binding;
            }
        }));

        card.add(cardTitle).left().padBottom(h(15f)).row();
        card.add(controlsTable).fillX();

        return card;
    }

    private Table createControlRow(String labelText, InputBinding binding,
                                   java.util.function.Consumer<InputBindingButton> buttonConsumer) {
        Table rowTable = new Table();

        // 左侧标签
        Label label = new Label(labelText, skin);
        label.setColor(COLOR_TEXT_MUTED);
        rowTable.add(label).left().width(w(LABEL_WIDTH)).padRight(w(10f));

        // 右侧绑定按钮
        InputBindingButton bindingButton = new InputBindingButton(binding);
        rowTable.add(bindingButton).left().width(w(BUTTON_WIDTH)).height(h(ROW_HEIGHT));

        buttonConsumer.accept(bindingButton);

        return rowTable;
    }

    private Table createBottomButtons() {
        LanguageManager lang = LanguageManager.getInstance();

        Table buttonTable = new Table();

        // 保存按钮
        TextButton saveButton = new TextButton(lang.get("save"), skin);
        saveButton.setColor(COLOR_SUCCESS);
        saveButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                saveControls();
                uiManager.popState();
            }
            return true;
        });

        // 重置按钮
        TextButton resetButton = new TextButton(lang.get("reset"), skin);
        resetButton.setColor(COLOR_WARNING);
        resetButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                resetControls();
            }
            return true;
        });

        // 返回按钮
        TextButton backButton = new TextButton(lang.get("back"), skin);
        backButton.setColor(COLOR_TEXT_MUTED);
        backButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                uiManager.popState();
            }
            return true;
        });

        buttonTable.add(saveButton).width(w(120f)).height(h(45f)).padRight(w(10f));
        buttonTable.add(resetButton).width(w(120f)).height(h(45f)).padRight(w(10f));
        buttonTable.add(backButton).width(w(120f)).height(h(45f));

        return buttonTable;
    }

    private void saveControls() {
        // 玩家1
        if (player1LeftButton != null) TetrisSettings.leftKey(player1LeftButton.getBinding());
        if (player1RightButton != null) TetrisSettings.rightKey(player1RightButton.getBinding());
        if (player1DownButton != null) TetrisSettings.downKey(player1DownButton.getBinding());
        if (player1RotateButton != null) TetrisSettings.rotateKey(player1RotateButton.getBinding());
        if (player1DropButton != null) TetrisSettings.dropKey(player1DropButton.getBinding());

        // 玩家2
        if (player2LeftButton != null) TetrisSettings.leftKey2(player2LeftButton.getBinding());
        if (player2RightButton != null) TetrisSettings.rightKey2(player2RightButton.getBinding());
        if (player2DownButton != null) TetrisSettings.downKey2(player2DownButton.getBinding());
        if (player2RotateButton != null) TetrisSettings.rotateKey2(player2RotateButton.getBinding());
        if (player2DropButton != null) TetrisSettings.dropKey2(player2DropButton.getBinding());
    }

    private void resetControls() {
        // 重置为默认值
        TetrisSettings.leftKey(InputBinding.LEFT);
        TetrisSettings.rightKey(InputBinding.RIGHT);
        TetrisSettings.downKey(InputBinding.DOWN);
        TetrisSettings.rotateKey(InputBinding.UP);
        TetrisSettings.dropKey(InputBinding.SPACE);

        TetrisSettings.leftKey2(InputBinding.A);
        TetrisSettings.rightKey2(InputBinding.D);
        TetrisSettings.downKey2(InputBinding.S);
        TetrisSettings.rotateKey2(InputBinding.W);
        TetrisSettings.dropKey2(InputBinding.Q);

        // 刷新UI
        recreateUI();
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createPanelBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(texture);
    }

    @Override
    protected void clearUI() {
        if (mainTable != null) {
            mainTable.remove();
            mainTable = null;
        }
    }

    @Override
    public void update(float delta) {
        // 更新所有绑定按钮
        if (player1LeftButton != null) player1LeftButton.update();
        if (player1RightButton != null) player1RightButton.update();
        if (player1DownButton != null) player1DownButton.update();
        if (player1RotateButton != null) player1RotateButton.update();
        if (player1DropButton != null) player1DropButton.update();

        if (player2LeftButton != null) player2LeftButton.update();
        if (player2RightButton != null) player2RightButton.update();
        if (player2DownButton != null) player2DownButton.update();
        if (player2RotateButton != null) player2RotateButton.update();
        if (player2DropButton != null) player2DropButton.update();
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
     * 键位绑定按钮组件
     */
    private class InputBindingButton extends TextButton {
        private InputBinding binding;
        private boolean isWaitingForInput = false;
        private float inputDelayTimer = 0;
        private static final float INPUT_DELAY = 0.2f; // 200ms延迟，避免立即捕获点击按钮的鼠标事件

        public InputBindingButton(InputBinding binding) {
            super(binding != null ? binding.getDisplayName() : "?", skin);
            this.binding = binding;

            addListener(event -> {
                if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                    if (!isWaitingForInput) {
                        startBinding();
                    }
                }
                return true;
            });
        }

        private void startBinding() {
            isWaitingForInput = true;
            inputDelayTimer = 0;
            setText("...");
            setColor(COLOR_WARNING);
        }

        public void update() {
            if (isWaitingForInput) {
                // 延迟计时器，避免捕获点击按钮本身的鼠标事件
                if (inputDelayTimer < INPUT_DELAY) {
                    inputDelayTimer += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
                    return;
                }

                // 检测按键输入
                for (int i = 0; i < 256; i++) {
                    if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(i)) {
                        // 排除ESC键（用于取消）
                        if (i == com.badlogic.gdx.Input.Keys.ESCAPE) {
                            isWaitingForInput = false;
                            updateDisplay();
                            return;
                        }

                        binding = InputBinding.fromKeyCode(i);
                        if (binding == null) {
                            // 如果没有找到对应的绑定，使用一个默认值
                            binding = InputBinding.SPACE;
                        }
                        isWaitingForInput = false;
                        updateDisplay();
                        return;
                    }
                }

                // 检测鼠标输入
                if (com.badlogic.gdx.Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
                    binding = InputBinding.MOUSE_LEFT;
                    isWaitingForInput = false;
                    updateDisplay();
                    return;
                }
                if (com.badlogic.gdx.Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.RIGHT)) {
                    binding = InputBinding.MOUSE_RIGHT;
                    isWaitingForInput = false;
                    updateDisplay();
                    return;
                }
            }
        }

        private void updateDisplay() {
            if (binding != null) {
                setText(binding.getDisplayName());
            } else {
                setText("?");
            }
            setColor(COLOR_PRIMARY);
        }

        public InputBinding getBinding() {
            return binding;
        }
    }
}
