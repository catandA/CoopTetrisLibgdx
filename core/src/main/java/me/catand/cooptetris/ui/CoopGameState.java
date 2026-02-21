package me.catand.cooptetris.ui;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.input.InputBinding;
import me.catand.cooptetris.input.TouchInputProcessor;
import me.catand.cooptetris.ui.FontUtils;
import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.tetris.CoopGameLogic;
import me.catand.cooptetris.tetris.GameStateManager;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 多人合作模式游戏界面（联机版）- 颜色跟随玩家版
 * - 20格宽的游戏板
 * - 4个出口，每个玩家从自己的出口下落
 * - 方块颜色由玩家选择，跟随玩家移动
 * - 现代化的暗色UI风格，与其他页面保持一致
 */
public class CoopGameState extends BaseUIState {

    // 玩家可选择的颜色：蓝、红、绿、黄
    public static final Color[] PLAYER_COLORS = {
        new Color(0.2f, 0.5f, 1.0f, 1.0f),    // 0 - 蓝色
        new Color(1.0f, 0.2f, 0.2f, 1.0f),    // 1 - 红色
        new Color(0.2f, 0.8f, 0.2f, 1.0f),    // 2 - 绿色
        new Color(1.0f, 0.9f, 0.2f, 1.0f),    // 3 - 黄色
    };

    // UI颜色配置 - 与其他页面保持一致
    private static final Color COLOR_BG = new Color(0.06f, 0.07f, 0.09f, 1f);
    private static final Color COLOR_PANEL = new Color(0.1f, 0.12f, 0.15f, 0.9f);
    private static final Color COLOR_PANEL_BORDER = new Color(0.2f, 0.23f, 0.28f, 1f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);
    private static final Color COLOR_SECONDARY = new Color(0.8f, 0.3f, 0.9f, 1f);
    private static final Color COLOR_SUCCESS = new Color(0.3f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_WARNING = new Color(1f, 0.7f, 0.2f, 1f);
    private static final Color COLOR_DANGER = new Color(1f, 0.3f, 0.3f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color COLOR_TEXT_MUTED = new Color(0.5f, 0.52f, 0.55f, 1f);

    private final GameStateManager gameStateManager;
    private final ShapeRenderer shapeRenderer;

    // UI元素
    private Table uiTable;
    private Table boardArea;
    private Label scoreValueLabel;
    private Label levelValueLabel;
    private Label linesValueLabel;
    private final Label[] playerNameLabels; // 出口上方的玩家名字标签
    private BitmapFont titleFont;
    private BitmapFont statsFont;
    private BitmapFont smallFont;
    private BitmapFont playerNameFont; // 用于绘制玩家名字的字体

    // 游戏板渲染参数
    private float boardX, boardY;
    private float cellSize;

    // 输入控制
    private final boolean[] isDownKeyPressed;
    private final float[] downKeyPressTime;
    private final float[] lastSoftDropTime;
    private static final float INITIAL_DELAY = 0.15f;
    private static final float SOFT_DROP_INTERVAL = 0.05f;

    // 我的槽位索引
    private final int mySlotIndex;
    // 实际玩家数量
    private final int playerCount;

    // 游戏结束弹窗
    private NotificationDialog gameOverDialog;
    private boolean isGameOverShown = false;

    // 暂停状态
    private boolean isPaused = false;
    private Table pauseOverlay;

    // 玩家名字列表（按槽位索引 0-3，空字符串表示该槽位无人）
    private final List<String> playerNames;
    // 玩家颜色列表（按槽位索引 0-3，-1表示该槽位无人）
    private final List<Integer> playerColors;

    // 触屏输入处理器
    private TouchInputProcessor touchInput;

    public CoopGameState(UIManager uiManager, GameStateManager gameStateManager) {
        super(uiManager);
        this.gameStateManager = gameStateManager;
        this.shapeRenderer = new ShapeRenderer();
        this.isDownKeyPressed = new boolean[4];
        this.downKeyPressTime = new float[4];
        this.lastSoftDropTime = new float[4];
        this.playerNameLabels = new Label[4];
        this.mySlotIndex = gameStateManager.getPlayerIndex();
        this.playerCount = gameStateManager.getPlayerCount();
        this.playerNames = gameStateManager.getPlayerNames();
        this.playerColors = gameStateManager.getPlayerColors();
    }

    @Override
    protected void createUI() {
        calculateBoardPosition();

        // 初始化触屏输入处理器
        touchInput = new TouchInputProcessor(stage);
        com.badlogic.gdx.Gdx.input.setInputProcessor(new com.badlogic.gdx.InputMultiplexer(stage, touchInput));

        titleFont = Main.platform.getFont(fontSize(24), lang().get("coop.game.title"), false, false);
        statsFont = Main.platform.getFont(fontSize(20), "0123456789", false, false);
        smallFont = Main.platform.getFont(fontSize(14), "Players", false, false);
        playerNameFont = Main.platform.getFont(fontSize(12), "Player Names", false, false);

        // 主容器
        uiTable = new Table();
        uiTable.setFillParent(true);

        // 内容容器
        Table contentTable = new Table();
        contentTable.center();

        // 上部区域：游戏板 + HUD
        Table gameArea = new Table();

        // 左侧：游戏板区域
        Table leftPanel = createGameBoardPanel();
        gameArea.add(leftPanel).width(w(500f)).height(h(600f)).padRight(w(20f));

        // 右侧：HUD面板
        Table hudPanel = createHUDPanel();
        gameArea.add(hudPanel).width(w(280f)).height(h(600f));

        contentTable.add(gameArea).row();

        // 底部按钮区域
        Table bottomPanel = createBottomPanel();
        contentTable.add(bottomPanel).fillX().padTop(h(20f));

        uiTable.add(contentTable).expand().center();
        stage.addActor(uiTable);

        // 创建出口上方的玩家名字标签
        createPlayerNameLabels();
    }

    /**
     * 创建出口上方的玩家名字标签
     */
    private void createPlayerNameLabels() {
        // 遍历所有4个槽位，显示有玩家的出口
        for (int i = 0; i < 4; i++) {
            String playerName = (playerNames != null && i < playerNames.size() && !playerNames.get(i).isEmpty())
                ? playerNames.get(i)
                : null;

            if (playerName != null) {
                // 使用玩家选择的颜色
                int colorIndex = (playerColors != null && i < playerColors.size())
                    ? playerColors.get(i)
                    : i; // 默认使用槽位索引作为颜色
                if (colorIndex < 0 || colorIndex >= 4) {
                    colorIndex = i;
                }
                Color playerColor = PLAYER_COLORS[colorIndex];
                Label.LabelStyle nameStyle = new Label.LabelStyle(playerNameFont, playerColor);
                playerNameLabels[i] = new Label(playerName, nameStyle);
                playerNameLabels[i].setAlignment(Align.center);
                stage.addActor(playerNameLabels[i]);
            }
        }
    }

    /**
     * 更新玩家名字标签位置
     */
    private void updatePlayerNameLabelPositions() {
        if (boardArea == null) return;

        com.badlogic.gdx.math.Vector2 boardPos = boardArea.localToStageCoordinates(new com.badlogic.gdx.math.Vector2(0, 0));
        float boardWidth = CoopGameLogic.BOARD_WIDTH * cellSize;
        float boardHeight = CoopGameLogic.BOARD_HEIGHT * cellSize;
        float actualBoardX = boardPos.x + (boardArea.getWidth() - boardWidth) / 2;
        float actualBoardY = boardPos.y + (boardArea.getHeight() - boardHeight) / 2;

        // 遍历所有4个槽位，更新有玩家名字的标签
        for (int i = 0; i < 4; i++) {
            if (playerNameLabels[i] != null) {
                // 使用槽位索引获取出口位置
                int exitX = CoopGameLogic.EXIT_POSITIONS[i];
                float exitCenterX = actualBoardX + exitX * cellSize + (3 * cellSize) / 2;
                float exitTopY = actualBoardY + CoopGameLogic.BOARD_HEIGHT * cellSize + 20;

                playerNameLabels[i].setPosition(exitCenterX - playerNameLabels[i].getWidth() / 2, exitTopY);
            }
        }
    }

    /**
     * 创建游戏板面板（左侧）
     */
    private Table createGameBoardPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(15f));

        // 游戏标题
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, COLOR_PRIMARY);
        Label gameTitle = new Label(lang().get("coop.game.title"), titleStyle);
        gameTitle.setAlignment(Align.center);
        panel.add(gameTitle).fillX().padBottom(h(10f)).row();

        // 游戏板区域（由ShapeRenderer绘制）
        boardArea = new Table();
        boardArea.setBackground(createPanelBackground(COLOR_BG));
        panel.add(boardArea).width(w(400f)).height(h(500f));

        return panel;
    }

    /**
     * 创建HUD面板（右侧）
     */
    private Table createHUDPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(20f));
        panel.top();

        // 游戏标题
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, COLOR_PRIMARY);
        Label gameTitle = new Label(lang().get("stats.title"), titleStyle);
        gameTitle.setAlignment(Align.center);
        panel.add(gameTitle).fillX().padBottom(h(25f)).row();

        // 分数面板 - 使用当前玩家的颜色
        Color mySlotColor = getMyPlayerColor();
        panel.add(createStatPanel(lang().get("score.title"), "0", mySlotColor)).fillX().padBottom(h(15f)).row();

        // 等级面板 - 使用当前玩家颜色的变体
        Color mySlotColorVariant = mySlotColor.cpy().lerp(COLOR_SECONDARY, 0.3f);
        panel.add(createStatPanel(lang().get("level.title"), "1", mySlotColorVariant)).fillX().padBottom(h(15f)).row();

        // 行数面板 - 使用当前玩家颜色的另一个变体
        Color mySlotColorVariant2 = mySlotColor.cpy().lerp(COLOR_SUCCESS, 0.3f);
        panel.add(createStatPanel(lang().get("lines.title"), "0", mySlotColorVariant2)).fillX().padBottom(h(25f)).row();

        // 玩家颜色说明 - 显示所有玩家及其名字
        Table colorLegendPanel = createColorLegendPanelWithNames();
        panel.add(colorLegendPanel).fillX().expand().bottom();

        return panel;
    }

    /**
     * 获取当前玩家的颜色
     */
    private Color getMyPlayerColor() {
        if (mySlotIndex >= 0 && mySlotIndex < 4) {
            int colorIndex = (playerColors != null && mySlotIndex < playerColors.size())
                ? playerColors.get(mySlotIndex)
                : mySlotIndex;
            if (colorIndex < 0 || colorIndex >= 4) {
                colorIndex = mySlotIndex;
            }
            return PLAYER_COLORS[colorIndex];
        }
        return COLOR_PRIMARY; // 默认颜色
    }

    /**
     * 获取指定槽位玩家的颜色
     */
    private Color getPlayerColor(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < 4) {
            int colorIndex = (playerColors != null && slotIndex < playerColors.size())
                ? playerColors.get(slotIndex)
                : slotIndex;
            if (colorIndex < 0 || colorIndex >= 4) {
                colorIndex = slotIndex;
            }
            return PLAYER_COLORS[colorIndex];
        }
        return COLOR_PRIMARY;
    }

    private Table createStatPanel(String title, String initialValue, Color accentColor) {
        Table statPanel = new Table();
        statPanel.setBackground(createPanelBackground(COLOR_BG));
        statPanel.pad(w(12f));

        Label titleLabel = FontUtils.createLabel(title, skin, fontSize(16), COLOR_TEXT_MUTED);
        titleLabel.setAlignment(Align.left);

        Label.LabelStyle valueStyle = new Label.LabelStyle(statsFont, accentColor);
        Label valueLabel = new Label(initialValue, valueStyle);
        valueLabel.setAlignment(Align.right);

        // 保存引用以便更新
        if (title.equals(lang().get("score.title"))) {
            scoreValueLabel = valueLabel;
        } else if (title.equals(lang().get("level.title"))) {
            levelValueLabel = valueLabel;
        } else if (title.equals(lang().get("lines.title"))) {
            linesValueLabel = valueLabel;
        }

        statPanel.add(titleLabel).left().expandX();
        statPanel.add(valueLabel).right();

        return statPanel;
    }

    /**
     * 创建玩家颜色说明面板（带玩家名字）
     */
    private Table createColorLegendPanelWithNames() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_BG));
        panel.pad(w(12f));

        Label.LabelStyle legendTitleStyle = new Label.LabelStyle(smallFont, COLOR_TEXT_MUTED);
        Label legendTitle = new Label(lang().get("player.colors"), legendTitleStyle);
        legendTitle.setAlignment(Align.center);
        panel.add(legendTitle).fillX().padBottom(h(10f)).row();

        // 显示所有槽位中有玩家的（按照槽位索引 0-3）
        for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
            String playerName = (playerNames != null && slotIndex < playerNames.size() && !playerNames.get(slotIndex).isEmpty())
                ? playerNames.get(slotIndex)
                : null;

            if (playerName == null) continue;

            Table row = new Table();

            // 获取玩家选择的颜色
            Color slotColor = getPlayerColor(slotIndex);

            // 颜色方块
            Table colorBlock = new Table();
            colorBlock.setBackground(createColoredBackground(slotColor));
            row.add(colorBlock).size(w(16f), h(16f)).padRight(w(8f));

            // 玩家名字（使用对应的颜色）
            Label.LabelStyle nameStyle = new Label.LabelStyle(smallFont, slotColor);
            Label nameLabel = new Label(playerName, nameStyle);
            row.add(nameLabel).left();

            panel.add(row).fillX().padBottom(h(5f)).row();
        }

        return panel;
    }

    private Table createBottomPanel() {
        Table bottomPanel = new Table();

        TextButton pauseButton = FontUtils.createTextButton(lang().get("pause"), skin, fontSize(24), COLOR_PRIMARY);
        pauseButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                togglePause();
            }
            return true;
        });

        TextButton exitButton = FontUtils.createTextButton(lang().get("exit"), skin, fontSize(24), COLOR_DANGER);
        exitButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                returnToMenu();
            }
            return true;
        });

        bottomPanel.add(pauseButton).width(w(120f)).height(h(45f)).padRight(w(15f));
        bottomPanel.add(exitButton).width(w(120f)).height(h(45f));

        return bottomPanel;
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            showPauseOverlay();
        } else {
            hidePauseOverlay();
        }
    }

    private void showPauseOverlay() {
        if (pauseOverlay == null) {
            pauseOverlay = new Table();
            pauseOverlay.setFillParent(true);
            pauseOverlay.setBackground(createPanelBackground(new Color(0, 0, 0, 0.7f)));

            BitmapFont pauseFont = Main.platform.getFont(fontSize(36), lang().get("pause.title"), false, false);
            Label.LabelStyle pauseStyle = new Label.LabelStyle(pauseFont, COLOR_PRIMARY);
            Label pauseLabel = new Label(lang().get("pause.title"), pauseStyle);
            pauseLabel.setAlignment(Align.center);

            TextButton resumeButton = FontUtils.createTextButton(lang().get("resume"), skin, fontSize(24), COLOR_SUCCESS);
            resumeButton.addListener(event -> {
                if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                    togglePause();
                }
                return true;
            });

            pauseOverlay.add(pauseLabel).padBottom(h(30f)).row();
            pauseOverlay.add(resumeButton).width(w(160f)).height(h(50f));
        }

        stage.addActor(pauseOverlay);
    }

    private void hidePauseOverlay() {
        if (pauseOverlay != null) {
            pauseOverlay.remove();
        }
    }

    private void calculateBoardPosition() {
        float baseCellSize = 28f;
        cellSize = baseCellSize * scale();

        float maxCellSize = h(35f);
        if (cellSize > maxCellSize) {
            cellSize = maxCellSize;
        }
    }

    private void updateBoardPositionFromUI() {
        if (boardArea != null) {
            com.badlogic.gdx.math.Vector2 boardPos = boardArea.localToStageCoordinates(new com.badlogic.gdx.math.Vector2(0, 0));
            float boardWidth = CoopGameLogic.BOARD_WIDTH * cellSize;
            float boardHeight = CoopGameLogic.BOARD_HEIGHT * cellSize;
            boardX = boardPos.x + (boardArea.getWidth() - boardWidth) / 2;
            boardY = boardPos.y + (boardArea.getHeight() - boardHeight) / 2;
        }
    }

    @Override
    protected void clearUI() {
        if (uiTable != null) {
            uiTable.remove();
            uiTable = null;
        }
        if (pauseOverlay != null) {
            pauseOverlay.remove();
            pauseOverlay = null;
        }
        if (gameOverDialog != null) {
            gameOverDialog.hide();
            gameOverDialog = null;
        }
    }

    @Override
    public void update(float delta) {
        if (!isPaused) {
            handleInput(delta);
            updateUI();
            checkGameOver();
        }
        // 更新玩家名字标签位置
        updatePlayerNameLabelPositions();
        stage.act(delta);
    }

    private void handleInput(float delta) {
        // 只控制自己的物块
        if (mySlotIndex < 0 || mySlotIndex >= 4) return;

        // 处理键盘输入
        boolean keyboardInputHandled = false;

        // 使用键位设置（支持双键位）
        if (isInputJustPressed(TetrisSettings.leftKey(), TetrisSettings.leftKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.LEFT);
            keyboardInputHandled = true;
        } else if (isInputJustPressed(TetrisSettings.rightKey(), TetrisSettings.rightKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.RIGHT);
            keyboardInputHandled = true;
        } else if (isInputJustPressed(TetrisSettings.rotateKey(), TetrisSettings.rotateKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.ROTATE_CLOCKWISE);
            keyboardInputHandled = true;
        } else if (isInputJustPressed(TetrisSettings.dropKey(), TetrisSettings.dropKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.DROP);
            keyboardInputHandled = true;
        }

        // 键盘软降（带延迟）
        boolean keyboardSoftDrop = isInputPressed(TetrisSettings.downKey(), TetrisSettings.downKey2());
        if (keyboardSoftDrop) {
            if (!isDownKeyPressed[mySlotIndex]) {
                gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                isDownKeyPressed[mySlotIndex] = true;
                downKeyPressTime[mySlotIndex] = 0;
                lastSoftDropTime[mySlotIndex] = 0;
            } else {
                downKeyPressTime[mySlotIndex] += delta;
                if (downKeyPressTime[mySlotIndex] >= INITIAL_DELAY) {
                    lastSoftDropTime[mySlotIndex] += delta;
                    if (lastSoftDropTime[mySlotIndex] >= SOFT_DROP_INTERVAL) {
                        gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                        lastSoftDropTime[mySlotIndex] = 0;
                    }
                }
            }
            keyboardInputHandled = true;
        } else {
            isDownKeyPressed[mySlotIndex] = false;
        }

        // 处理触屏输入（如果键盘没有输入）
        if (!keyboardInputHandled && touchInput != null) {
            handleTouchInput(delta);
        }
    }

    private void handleTouchInput(float delta) {
        // 获取触屏操作（只在触摸开始时返回一次）
        TouchInputProcessor.TouchAction action = touchInput.pollAction();

        // 处理瞬时操作（左/右/旋转/硬降）
        switch (action) {
            case LEFT:
                gameStateManager.handleInput(MoveMessage.MoveType.LEFT);
                break;
            case RIGHT:
                gameStateManager.handleInput(MoveMessage.MoveType.RIGHT);
                break;
            case ROTATE:
                gameStateManager.handleInput(MoveMessage.MoveType.ROTATE_CLOCKWISE);
                break;
            case HARD_DROP:
                gameStateManager.handleInput(MoveMessage.MoveType.DROP);
                break;
            case SOFT_DROP:
                // 软降首次触发，初始化状态
                if (!isDownKeyPressed[mySlotIndex]) {
                    gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                    isDownKeyPressed[mySlotIndex] = true;
                    downKeyPressTime[mySlotIndex] = 0;
                    lastSoftDropTime[mySlotIndex] = 0;
                }
                break;
            default:
                break;
        }

        // 处理触屏软降的持续下降（长按下半部分）
        if (touchInput.isSoftDropping()) {
            // 首次触发已经在上面处理了，这里只处理后续的持续下降
            if (isDownKeyPressed[mySlotIndex]) {
                downKeyPressTime[mySlotIndex] += delta;
                if (downKeyPressTime[mySlotIndex] >= INITIAL_DELAY) {
                    lastSoftDropTime[mySlotIndex] += delta;
                    if (lastSoftDropTime[mySlotIndex] >= SOFT_DROP_INTERVAL) {
                        gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                        lastSoftDropTime[mySlotIndex] = 0;
                    }
                }
            }
        } else if (!isInputPressed(TetrisSettings.downKey(), TetrisSettings.downKey2())) {
            // 如果键盘也没有按下，则重置软降状态
            isDownKeyPressed[mySlotIndex] = false;
        }
    }

    private boolean isInputJustPressed(InputBinding input1, InputBinding input2) {
        return (input1 != null && input1.isJustPressed()) || (input2 != null && input2.isJustPressed());
    }

    private boolean isInputPressed(InputBinding input1, InputBinding input2) {
        return (input1 != null && input1.isPressed()) || (input2 != null && input2.isPressed());
    }

    private void updateUI() {
        CoopGameLogic coopGameLogic = gameStateManager.getSharedManager().getCoopGameLogic();
        if (coopGameLogic == null) return;

        if (scoreValueLabel != null) {
            scoreValueLabel.setText(String.valueOf(coopGameLogic.getScore()));
        }
        if (levelValueLabel != null) {
            levelValueLabel.setText(String.valueOf(coopGameLogic.getLevel()));
        }
        if (linesValueLabel != null) {
            linesValueLabel.setText(String.valueOf(coopGameLogic.getLines()));
        }
    }

    private void checkGameOver() {
        CoopGameLogic coopGameLogic = gameStateManager.getSharedManager().getCoopGameLogic();
        if (coopGameLogic == null) return;

        if (coopGameLogic.isGameOver() && !isGameOverShown) {
            isGameOverShown = true;
            showGameOverDialog();
        }
    }

    private void showGameOverDialog() {
        CoopGameLogic coopGameLogic = gameStateManager.getSharedManager().getCoopGameLogic();
        if (coopGameLogic == null) return;

        int score = coopGameLogic.getScore();
        int lines = coopGameLogic.getLines();
        int level = coopGameLogic.getLevel();

        String message = String.format(lang().get("coop.game.over.message"), score, lines, level);

        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setNotificationType(NotificationMessage.NotificationType.INFO);
        notificationMessage.setTitle(lang().get("coop.game.over.title"));
        notificationMessage.setMessage(message);

        gameOverDialog = new NotificationDialog(skin);
        gameOverDialog.setNotification(notificationMessage);
        gameOverDialog.setOnCloseAction(() -> {
            returnToMenu();
        });

        // 显示在右侧，避免被游戏板遮挡
        gameOverDialog.show(stage, NotificationDialog.Position.RIGHT);
    }

    private void returnToMenu() {
        // 断开网络连接
        if (uiManager.getNetworkManager() != null && uiManager.getNetworkManager().isConnected()) {
            uiManager.getNetworkManager().disconnect();
        }
        // 停止本地服务器
        if (uiManager.getLocalServerManager() != null && uiManager.getLocalServerManager().isRunning()) {
            uiManager.getLocalServerManager().stopServer();
        }
        uiManager.setScreen(new MainMenuState(uiManager));
    }

    /**
     * 渲染游戏板 - 由 Main.java 调用
     */
    public void renderGame(ShapeRenderer shapeRenderer) {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);

        // 更新游戏板位置（基于UI实际位置）
        updateBoardPositionFromUI();

        renderGameBoard(shapeRenderer);
    }

    private void renderGameBoard(ShapeRenderer shapeRenderer) {
        CoopGameLogic coopGameLogic = gameStateManager.getSharedManager().getCoopGameLogic();
        if (coopGameLogic == null) return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 绘制游戏板背景
        shapeRenderer.setColor(COLOR_PANEL);
        shapeRenderer.rect(boardX - 4, boardY - 4,
            CoopGameLogic.BOARD_WIDTH * cellSize + 8,
            CoopGameLogic.BOARD_HEIGHT * cellSize + 8);

        // 绘制游戏板内部
        shapeRenderer.setColor(COLOR_BG);
        shapeRenderer.rect(boardX, boardY,
            CoopGameLogic.BOARD_WIDTH * cellSize,
            CoopGameLogic.BOARD_HEIGHT * cellSize);

        // 绘制网格线
        shapeRenderer.setColor(COLOR_PANEL_BORDER);
        for (int x = 0; x <= CoopGameLogic.BOARD_WIDTH; x++) {
            shapeRenderer.rect(boardX + x * cellSize, boardY, 1,
                CoopGameLogic.BOARD_HEIGHT * cellSize);
        }
        for (int y = 0; y <= CoopGameLogic.BOARD_HEIGHT; y++) {
            shapeRenderer.rect(boardX, boardY + y * cellSize,
                CoopGameLogic.BOARD_WIDTH * cellSize, 1);
        }

        // 绘制出口标记（根据槽位索引 0-3）
        // 布局: [槽位0:0-2] [分隔:3] [槽位1:4-6] [分隔:7] [槽位2:8-10] [分隔:11] [槽位3:12-14]
        for (int i = 0; i < 4; i++) {
            // 检查该槽位是否有玩家
            if (playerNames != null && i < playerNames.size() && !playerNames.get(i).isEmpty()) {
                int exitX = CoopGameLogic.EXIT_POSITIONS[i];
                // 使用玩家选择的颜色
                int colorIndex = coopGameLogic.getSlotColor(i);
                Color playerColor = PLAYER_COLORS[colorIndex];
                shapeRenderer.setColor(playerColor);
                // 每个出口3格宽
                shapeRenderer.rect(boardX + exitX * cellSize - 2, boardY + CoopGameLogic.BOARD_HEIGHT * cellSize,
                    3 * cellSize + 4, 6);
            }
        }

        // 绘制分隔区域标记
        shapeRenderer.setColor(COLOR_PANEL_BORDER);
        int[] separatorX = {3, 7, 11}; // 分隔格位置
        for (int sepX : separatorX) {
            shapeRenderer.rect(boardX + sepX * cellSize, boardY + CoopGameLogic.BOARD_HEIGHT * cellSize - 2,
                cellSize, 4);
        }

        // 绘制已固定的方块
        int[][] board = coopGameLogic.getBoard();
        for (int y = 0; y < CoopGameLogic.BOARD_HEIGHT; y++) {
            for (int x = 0; x < CoopGameLogic.BOARD_WIDTH; x++) {
                int cell = board[y][x];
                if (cell != 0) {
                    float screenY = boardY + (CoopGameLogic.BOARD_HEIGHT - 1 - y) * cellSize;
                    int colorIndex = coopGameLogic.getCellColor(x, y);
                    Color cellColor = (colorIndex >= 0 && colorIndex < 4)
                        ? PLAYER_COLORS[colorIndex]
                        : Color.GRAY;

                    shapeRenderer.setColor(cellColor);
                    shapeRenderer.rect(boardX + x * cellSize + 1, screenY + 1,
                        cellSize - 2, cellSize - 2);

                    // 高光效果
                    shapeRenderer.setColor(cellColor.cpy().add(0.2f, 0.2f, 0.2f, 0));
                    shapeRenderer.rect(boardX + x * cellSize + 1, screenY + cellSize - 4,
                        cellSize - 2, 3);
                }
            }
        }

        // 绘制每个槽位的当前物块（遍历所有4个槽位）
        for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
            if (coopGameLogic.isSlotActive(slotIndex)) {
                CoopGameLogic.PlayerPiece piece = coopGameLogic.getPlayerPiece(slotIndex);
                if (piece.isActive()) {
                    int[][] pieceShape = coopGameLogic.getPieceShape(piece.getPieceType(), piece.getRotation());
                    // 使用玩家选择的颜色
                    int colorIndex = coopGameLogic.getSlotColor(slotIndex);
                    Color pieceColor = PLAYER_COLORS[colorIndex];

                    for (int y = 0; y < pieceShape.length; y++) {
                        for (int x = 0; x < pieceShape[y].length; x++) {
                            if (pieceShape[y][x] != 0) {
                                int boardXPos = piece.getX() + x;
                                int boardYPos = piece.getY() + y;
                                if (boardXPos >= 0 && boardXPos < CoopGameLogic.BOARD_WIDTH
                                    && boardYPos >= 0 && boardYPos < CoopGameLogic.BOARD_HEIGHT) {
                                    float screenY = boardY + (CoopGameLogic.BOARD_HEIGHT - 1 - boardYPos) * cellSize;
                                    shapeRenderer.setColor(pieceColor);
                                    shapeRenderer.rect(boardX + boardXPos * cellSize + 1, screenY + 1,
                                        cellSize - 2, cellSize - 2);

                                    // 高光效果
                                    shapeRenderer.setColor(pieceColor.cpy().add(0.2f, 0.2f, 0.2f, 0));
                                    shapeRenderer.rect(boardX + boardXPos * cellSize + 1,
                                        screenY + cellSize - 4, cellSize - 2, 3);
                                }
                            }
                        }
                    }
                }
            }
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        calculateBoardPosition();
    }

    @Override
    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
        if (statsFont != null) {
            statsFont.dispose();
            statsFont = null;
        }
        if (smallFont != null) {
            smallFont.dispose();
            smallFont = null;
        }
        if (playerNameFont != null) {
            playerNameFont.dispose();
            playerNameFont = null;
        }
        shapeRenderer.dispose();
    }

    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createPanelBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(texture);
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createColoredBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(texture);
    }
}
