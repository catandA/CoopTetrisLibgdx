package me.catand.cooptetris.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.input.InputBinding;
import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.tetris.CoopGameLogic;
import me.catand.cooptetris.tetris.GameStateManager;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 多人合作模式游戏界面（联机版）- 重构版
 * - 20格宽的游戏板
 * - 4个出口，每个玩家从自己的出口下落
 * - 方块颜色根据玩家决定（蓝红绿黄）
 * - 现代化的暗色UI风格，与其他页面保持一致
 */
public class CoopGameState extends BaseUIState {

    // 玩家颜色：蓝、红、绿、黄
    private static final Color[] PLAYER_COLORS = {
        new Color(0.2f, 0.5f, 1.0f, 1.0f),    // 蓝色 - 玩家1
        new Color(1.0f, 0.2f, 0.2f, 1.0f),    // 红色 - 玩家2
        new Color(0.2f, 0.8f, 0.2f, 1.0f),    // 绿色 - 玩家3
        new Color(1.0f, 0.9f, 0.2f, 1.0f),    // 黄色 - 玩家4
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
    private final Label[] playerLabels;
    private BitmapFont titleFont;
    private BitmapFont statsFont;
    private BitmapFont smallFont;

    // 游戏板渲染参数
    private float boardX, boardY;
    private float cellSize;

    // 输入控制
    private final boolean[] isDownKeyPressed;
    private final float[] downKeyPressTime;
    private final float[] lastSoftDropTime;
    private static final float INITIAL_DELAY = 0.15f;
    private static final float SOFT_DROP_INTERVAL = 0.05f;

    // 我的玩家索引
    private final int myPlayerIndex;
    // 实际玩家数量
    private final int playerCount;

    // 游戏结束弹窗
    private NotificationDialog gameOverDialog;
    private boolean isGameOverShown = false;

    // 暂停状态
    private boolean isPaused = false;
    private Table pauseOverlay;

    public CoopGameState(UIManager uiManager, GameStateManager gameStateManager) {
        super(uiManager);
        this.gameStateManager = gameStateManager;
        this.shapeRenderer = new ShapeRenderer();
        this.isDownKeyPressed = new boolean[4];
        this.downKeyPressTime = new float[4];
        this.lastSoftDropTime = new float[4];
        this.playerLabels = new Label[4];
        this.myPlayerIndex = gameStateManager.getPlayerIndex();
        this.playerCount = gameStateManager.getPlayerCount();
    }

    @Override
    protected void createUI() {
        calculateBoardPosition();

        titleFont = Main.platform.getFont(fontSize(24), lang().get("coop.game.title"), false, false);
        statsFont = Main.platform.getFont(fontSize(20), "0123456789", false, false);
        smallFont = Main.platform.getFont(fontSize(14), "Players", false, false);

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
        panel.add(boardArea).width(w(400f)).height(h(500f)).padBottom(h(10f)).row();

        // 底部玩家状态
        Table playerStatusPanel = createPlayerStatusPanel();
        panel.add(playerStatusPanel).fillX().height(h(60f));

        return panel;
    }

    /**
     * 创建玩家状态面板
     */
    private Table createPlayerStatusPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_BG));
        panel.pad(w(10f));

        String[] playerNames = {"P1", "P2", "P3", "P4"};

        // 只显示实际玩家数量，使用正确的玩家索引颜色
        for (int i = 0; i < playerCount && i < 4; i++) {
            int assignedPlayerIndex = CoopGameLogic.PLAYER_ASSIGNMENT_ORDER[i];
            playerLabels[i] = new Label(playerNames[i], skin);
            playerLabels[i].setColor(PLAYER_COLORS[assignedPlayerIndex]);
            panel.add(playerLabels[i]).padRight(w(15f)).expandX();
        }

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

        // 分数面板
        panel.add(createStatPanel(lang().get("score.title"), "0", COLOR_PRIMARY)).fillX().padBottom(h(15f)).row();

        // 等级面板
        panel.add(createStatPanel(lang().get("level.title"), "1", COLOR_SECONDARY)).fillX().padBottom(h(15f)).row();

        // 行数面板
        panel.add(createStatPanel(lang().get("lines.title"), "0", COLOR_SUCCESS)).fillX().padBottom(h(25f)).row();

        // 玩家颜色说明
        Table colorLegendPanel = createColorLegendPanel();
        panel.add(colorLegendPanel).fillX().expand().bottom();

        return panel;
    }

    private Table createStatPanel(String title, String initialValue, Color accentColor) {
        Table statPanel = new Table();
        statPanel.setBackground(createPanelBackground(COLOR_BG));
        statPanel.pad(w(12f));

        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get("default", com.badlogic.gdx.graphics.g2d.BitmapFont.class), COLOR_TEXT_MUTED);
        Label titleLabel = new Label(title, titleStyle);
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
     * 创建玩家颜色说明面板
     */
    private Table createColorLegendPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_BG));
        panel.pad(w(12f));

        Label.LabelStyle legendTitleStyle = new Label.LabelStyle(smallFont, COLOR_TEXT_MUTED);
        Label legendTitle = new Label(lang().get("player.colors"), legendTitleStyle);
        legendTitle.setAlignment(Align.center);
        panel.add(legendTitle).fillX().padBottom(h(10f)).row();

        // 颜色说明
        String[] colorNames = {lang().get("color.blue"), lang().get("color.red"), lang().get("color.green"), lang().get("color.yellow")};
        for (int i = 0; i < 4; i++) {
            Table row = new Table();

            // 颜色方块
            Table colorBlock = new Table();
            colorBlock.setBackground(createColoredBackground(PLAYER_COLORS[i]));
            row.add(colorBlock).size(w(16f), h(16f)).padRight(w(8f));

            // 颜色名称
            Label.LabelStyle nameStyle = new Label.LabelStyle(smallFont, PLAYER_COLORS[i]);
            Label nameLabel = new Label(colorNames[i], nameStyle);
            row.add(nameLabel).left();

            panel.add(row).fillX().padBottom(h(5f)).row();
        }

        return panel;
    }

    private Table createBottomPanel() {
        Table bottomPanel = new Table();

        TextButton pauseButton = new TextButton(lang().get("pause"), skin);
        pauseButton.setColor(COLOR_PRIMARY);
        pauseButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                togglePause();
            }
            return true;
        });

        TextButton exitButton = new TextButton(lang().get("exit"), skin);
        exitButton.setColor(COLOR_DANGER);
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

            TextButton resumeButton = new TextButton(lang().get("resume"), skin);
            resumeButton.setColor(COLOR_SUCCESS);
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
        stage.act(delta);
    }

    private void handleInput(float delta) {
        // 只控制自己的物块
        if (myPlayerIndex < 0 || myPlayerIndex >= 4) return;

        // 使用键位设置（支持双键位）
        if (isInputJustPressed(TetrisSettings.leftKey(), TetrisSettings.leftKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.LEFT);
        } else if (isInputJustPressed(TetrisSettings.rightKey(), TetrisSettings.rightKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.RIGHT);
        } else if (isInputJustPressed(TetrisSettings.rotateKey(), TetrisSettings.rotateKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.ROTATE_CLOCKWISE);
        } else if (isInputJustPressed(TetrisSettings.dropKey(), TetrisSettings.dropKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.DROP);
        }

        // 软降（带延迟）
        if (isInputPressed(TetrisSettings.downKey(), TetrisSettings.downKey2())) {
            if (!isDownKeyPressed[myPlayerIndex]) {
                gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                isDownKeyPressed[myPlayerIndex] = true;
                downKeyPressTime[myPlayerIndex] = 0;
                lastSoftDropTime[myPlayerIndex] = 0;
            } else {
                downKeyPressTime[myPlayerIndex] += delta;
                if (downKeyPressTime[myPlayerIndex] >= INITIAL_DELAY) {
                    lastSoftDropTime[myPlayerIndex] += delta;
                    if (lastSoftDropTime[myPlayerIndex] >= SOFT_DROP_INTERVAL) {
                        gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                        lastSoftDropTime[myPlayerIndex] = 0;
                    }
                }
            }
        } else {
            isDownKeyPressed[myPlayerIndex] = false;
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

        // 更新玩家状态显示（只更新实际玩家数量）
        for (int i = 0; i < playerCount && i < 4; i++) {
            int assignedPlayerIndex = CoopGameLogic.PLAYER_ASSIGNMENT_ORDER[i];
            CoopGameLogic.PlayerPiece piece = coopGameLogic.getPlayerPiece(assignedPlayerIndex);
            if (piece.isActive()) {
                playerLabels[i].setText("P" + (i + 1) + " ✓");
            } else {
                playerLabels[i].setText("P" + (i + 1) + " ✗");
            }
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

        // 绘制出口标记（根据实际分配的玩家索引）
        for (int i = 0; i < playerCount && i < 4; i++) {
            int assignedPlayerIndex = CoopGameLogic.PLAYER_ASSIGNMENT_ORDER[i];
            int exitX = CoopGameLogic.EXIT_POSITIONS[assignedPlayerIndex];
            shapeRenderer.setColor(PLAYER_COLORS[assignedPlayerIndex]);
            shapeRenderer.rect(boardX + exitX * cellSize - 2, boardY + CoopGameLogic.BOARD_HEIGHT * cellSize,
                3 * cellSize + 4, 6);
        }

        // 绘制已固定的方块
        int[][] board = coopGameLogic.getBoard();
        for (int y = 0; y < CoopGameLogic.BOARD_HEIGHT; y++) {
            for (int x = 0; x < CoopGameLogic.BOARD_WIDTH; x++) {
                int cell = board[y][x];
                if (cell != 0) {
                    float screenY = boardY + (CoopGameLogic.BOARD_HEIGHT - 1 - y) * cellSize;
                    int playerColorIndex = coopGameLogic.getCellColor(x, y);
                    Color cellColor = (playerColorIndex >= 0 && playerColorIndex < 4)
                        ? PLAYER_COLORS[playerColorIndex]
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

        // 绘制每个玩家的当前物块（只绘制实际玩家数量）
        for (int i = 0; i < playerCount && i < 4; i++) {
            int assignedPlayerIndex = CoopGameLogic.PLAYER_ASSIGNMENT_ORDER[i];
            CoopGameLogic.PlayerPiece piece = coopGameLogic.getPlayerPiece(assignedPlayerIndex);
            if (piece.isActive()) {
                int[][] pieceShape = coopGameLogic.getPieceShape(piece.getPieceType(), piece.getRotation());
                Color pieceColor = PLAYER_COLORS[assignedPlayerIndex];

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
