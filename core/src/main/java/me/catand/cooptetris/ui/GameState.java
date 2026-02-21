package me.catand.cooptetris.ui;

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
import me.catand.cooptetris.input.TouchInputProcessor;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.ui.FontUtils;
import me.catand.cooptetris.shared.message.CountdownMessage;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.message.PlayerScoresMessage;
import me.catand.cooptetris.shared.message.PlayerSlotMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.shared.model.Tetromino;
import me.catand.cooptetris.shared.tetris.GameLogic;
import me.catand.cooptetris.tetris.GameStateManager;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 游戏状态 - 现代化暗色游戏UI风格
 */
public class GameState extends BaseUIState implements NetworkManager.NetworkListener {
    private Table uiTable;
    private final GameStateManager gameStateManager;
    private float cellSize;
    private Label scoreLabel;
    private Label levelLabel;
    private Label linesLabel;
    private Label scoreValueLabel;
    private Label levelValueLabel;
    private Label linesValueLabel;
    private BitmapFont titleFont;
    private BitmapFont statsFont;

    private boolean isDownKeyPressed = false;
    private float downKeyPressTime = 0;
    private final float initialDelay = 0.5f;
    private final float softDropInterval = 0.1f;
    private float lastSoftDropTime = 0;
    private float boardX;
    private float boardY;

    // 游戏结束弹窗
    private NotificationDialog gameOverDialog;
    private boolean isGameOverShown = false;

    // 暂停状态
    private boolean isPaused = false;
    private Table pauseOverlay;

    // 下一个方块预览
    private Table previewArea;
    private float previewCellSize = 20f;

    // 触屏输入处理器
    private TouchInputProcessor touchInput;

    // UI颜色配置
    private static final Color COLOR_BG = new Color(0.06f, 0.07f, 0.09f, 1f);
    private static final Color COLOR_PANEL = new Color(0.1f, 0.12f, 0.15f, 0.9f);
    private static final Color COLOR_PANEL_BORDER = new Color(0.2f, 0.23f, 0.28f, 1f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);
    private static final Color COLOR_SECONDARY = new Color(0.8f, 0.3f, 0.9f, 1f);
    private static final Color COLOR_SUCCESS = new Color(0.3f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_DANGER = new Color(1f, 0.3f, 0.3f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color COLOR_TEXT_MUTED = new Color(0.5f, 0.52f, 0.55f, 1f);

    public GameState(UIManager uiManager, GameStateManager gameStateManager) {
        super(uiManager);
        this.gameStateManager = gameStateManager;
    }

    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    @Override
    protected void createUI() {
        // 注册网络监听器
        if (uiManager.getNetworkManager() != null) {
            uiManager.getNetworkManager().addListener(this);
        }

        calculateBoardPosition();

        // 初始化触屏输入处理器
        touchInput = new TouchInputProcessor(stage);
        com.badlogic.gdx.Gdx.input.setInputProcessor(new com.badlogic.gdx.InputMultiplexer(stage, touchInput));

        titleFont = Main.platform.getFont(fontSize(24), lang().get("game.title"), false, false);
        statsFont = Main.platform.getFont(fontSize(20), "0123456789", false, false);

        // 主容器，限制在16:9显示区域内
        uiTable = new Table();
        uiTable.setPosition(offsetX(), offsetY());
        uiTable.setSize(displayWidth(), displayHeight());

        // 内容容器，垂直居中
        Table contentTable = new Table();
        contentTable.center();

        // 上部区域：游戏板 + HUD
        Table gameArea = new Table();

        // 左侧：游戏板区域（留空，由ShapeRenderer绘制）
        gameArea.add().width(w(450f)).height(h(600f));

        // 右侧：HUD面板
        Table hudPanel = createHUDPanel();
        gameArea.add(hudPanel).width(w(280f)).height(h(600f)).padLeft(w(20f));

        contentTable.add(gameArea).row();

        // 底部按钮区域
        Table bottomPanel = createBottomPanel();
        contentTable.add(bottomPanel).fillX().padTop(h(20f));

        uiTable.add(contentTable).expand().center();
        stage.addActor(uiTable);
    }

    private Table createHUDPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(20f));
        panel.top();

        // 游戏标题
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, COLOR_PRIMARY);
        Label gameTitle = new Label(lang().get("game.title"), titleStyle);
        gameTitle.setAlignment(Align.center);
        panel.add(gameTitle).fillX().padBottom(h(25f)).row();

        // 分数面板
        panel.add(createStatPanel(lang().get("score.title"), "0", COLOR_PRIMARY)).fillX().padBottom(h(15f)).row();

        // 等级面板
        panel.add(createStatPanel(lang().get("level.title"), "1", COLOR_SECONDARY)).fillX().padBottom(h(15f)).row();

        // 行数面板
        panel.add(createStatPanel(lang().get("lines.title"), "0", COLOR_SUCCESS)).fillX().padBottom(h(25f)).row();

        // 下一个方块预览区域（预留）
        Table previewPanel = createPreviewPanel();
        panel.add(previewPanel).fillX().height(h(150f));

        return panel;
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
            scoreLabel = titleLabel;
            scoreValueLabel = valueLabel;
        } else if (title.equals(lang().get("level.title"))) {
            levelLabel = titleLabel;
            levelValueLabel = valueLabel;
        } else if (title.equals(lang().get("lines.title"))) {
            linesLabel = titleLabel;
            linesValueLabel = valueLabel;
        }

        statPanel.add(titleLabel).left().expandX();
        statPanel.add(valueLabel).right();

        return statPanel;
    }

    private Table createPreviewPanel() {
        Table previewPanel = new Table();
        previewPanel.setBackground(createPanelBackground(COLOR_BG));
        previewPanel.pad(w(12f));

        Label previewTitle = FontUtils.createLabel(lang().get("next.piece"), skin, fontSize(16), COLOR_TEXT_MUTED);
        previewTitle.setAlignment(Align.center);

        previewPanel.add(previewTitle).fillX().padBottom(h(10f)).row();

        // 预览区域
        previewArea = new Table();
        previewArea.setBackground(createPanelBackground(COLOR_PANEL));
        previewPanel.add(previewArea).height(w(100f)).fillX();

        return previewPanel;
    }

    private void updatePreview() {
        if (previewArea == null) return;

        previewArea.clear();

        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        int nextPiece = gameLogic.getNextPiece();
        int[][] pieceShape = Tetromino.SHAPES[nextPiece];

        // 计算预览方块的单元格大小
        float cellSize = w(20f);
        int pieceSize = pieceShape.length;

        // 创建预览表格
        Table pieceTable = new Table();

        for (int y = 0; y < pieceSize; y++) {
            for (int x = 0; x < pieceSize; x++) {
                if (pieceShape[y][x] != 0) {
                    // 创建方块单元格
                    Table cell = new Table();
                    cell.setBackground(createColoredBackground(Tetromino.COLORS[nextPiece]));
                    pieceTable.add(cell).size(cellSize).pad(w(1f));
                } else {
                    // 空白占位
                    pieceTable.add().size(cellSize).pad(w(1f));
                }
            }
            pieceTable.row();
        }

        previewArea.add(pieceTable).center().expand();
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createColoredBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(texture);
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
                if (uiManager.getNetworkManager() != null && uiManager.getNetworkManager().isConnected()) {
                    uiManager.getNetworkManager().disconnect();
                }
                if (uiManager.getLocalServerManager() != null && uiManager.getLocalServerManager().isRunning()) {
                    uiManager.getLocalServerManager().stopServer();
                }
                uiManager.setScreen(new MainMenuState(uiManager));
            }
            return true;
        });

        bottomPanel.add(pauseButton).width(w(120f)).height(h(45f)).padRight(w(15f));
        bottomPanel.add(exitButton).width(w(120f)).height(h(45f));

        return bottomPanel;
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
        if (uiTable != null) {
            uiTable.remove();
            uiTable = null;
        }
        // 移除网络监听器
        if (uiManager.getNetworkManager() != null) {
            uiManager.getNetworkManager().removeListener(this);
        }
    }

    @Override
    public void update(float delta) {
        if (!isPaused) {
            handleInput();
            gameStateManager.update(delta);
            updateUI();
            checkGameOver();
        }
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

            // 暂停标题
            BitmapFont pauseFont = Main.platform.getFont(fontSize(36), lang().get("pause.title"), false, false);
            Label.LabelStyle pauseStyle = new Label.LabelStyle(pauseFont, COLOR_PRIMARY);
            Label pauseLabel = new Label(lang().get("pause.title"), pauseStyle);
            pauseLabel.setAlignment(Align.center);

            // 继续按钮
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

    private void checkGameOver() {
        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        if (gameLogic.isGameOver() && !isGameOverShown) {
            isGameOverShown = true;
            showGameOverDialog();
        }
    }

    private void showGameOverDialog() {
        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();

        NotificationMessage message = new NotificationMessage();
        message.setNotificationType(NotificationMessage.NotificationType.INFO);
        message.setTitle(lang().get("game.over.title"));
        message.setMessage(lang().get("game.over.message")
            .replace("%d", String.valueOf(gameLogic.getScore()))
            .replace("%l", String.valueOf(gameLogic.getLines()))
            .replace("%v", String.valueOf(gameLogic.getLevel())));

        gameOverDialog = new NotificationDialog(skin);
        gameOverDialog.setNotification(message);
        gameOverDialog.setOnCloseAction(() -> {
            returnToPreviousScreen();
        });
        gameOverDialog.show(stage);
    }

    private void returnToPreviousScreen() {
        if (gameStateManager.isMultiplayer()) {
            // 多人模式：返回房间
            if (uiManager.getNetworkManager() != null && uiManager.getNetworkManager().isConnected()) {
                uiManager.setScreen(new RoomLobbyState(uiManager, uiManager.getNetworkManager()));
            } else {
                uiManager.setScreen(new MainMenuState(uiManager));
            }
        } else {
            // 单人模式：返回主菜单
            if (uiManager.getLocalServerManager() != null && uiManager.getLocalServerManager().isRunning()) {
                uiManager.getLocalServerManager().stopServer();
            }
            if (uiManager.getNetworkManager() != null) {
                uiManager.getNetworkManager().disconnect();
            }
            uiManager.setScreen(new MainMenuState(uiManager));
        }
    }

    private void handleInput() {
        // 处理键盘输入
        boolean keyboardInputHandled = false;

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

        // 键盘软降处理
        boolean keyboardSoftDrop = isInputPressed(TetrisSettings.downKey(), TetrisSettings.downKey2());
        if (keyboardSoftDrop) {
            if (!isDownKeyPressed) {
                gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                isDownKeyPressed = true;
                downKeyPressTime = 0;
                lastSoftDropTime = 0;
            } else {
                downKeyPressTime += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
                if (downKeyPressTime >= initialDelay) {
                    lastSoftDropTime += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
                    if (lastSoftDropTime >= softDropInterval) {
                        gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                        lastSoftDropTime = 0;
                    }
                }
            }
            keyboardInputHandled = true;
        } else {
            isDownKeyPressed = false;
        }

        // 处理触屏输入（如果键盘没有输入）
        if (!keyboardInputHandled && touchInput != null) {
            handleTouchInput();
        }
    }

    // 触屏软降状态（独立于键盘）
    private boolean isTouchDownKeyPressed = false;
    private float touchDownKeyPressTime = 0;
    private float touchLastSoftDropTime = 0;

    private void handleTouchInput() {
        // 获取触屏操作
        TouchInputProcessor.TouchAction action = touchInput.pollAction();

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
            default:
                break;
        }

        // 处理触屏软降（长按下半部分）
        if (touchInput.isSoftDropping()) {
            if (!isTouchDownKeyPressed) {
                gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                isTouchDownKeyPressed = true;
                touchDownKeyPressTime = 0;
                touchLastSoftDropTime = 0;
            } else {
                touchDownKeyPressTime += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
                if (touchDownKeyPressTime >= initialDelay) {
                    touchLastSoftDropTime += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
                    if (touchLastSoftDropTime >= softDropInterval) {
                        gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                        touchLastSoftDropTime = 0;
                    }
                }
            }
        } else {
            isTouchDownKeyPressed = false;
        }
    }

    private boolean isInputJustPressed(InputBinding input1, InputBinding input2) {
        return (input1 != null && input1.isJustPressed()) || (input2 != null && input2.isJustPressed());
    }

    private boolean isInputPressed(InputBinding input1, InputBinding input2) {
        return (input1 != null && input1.isPressed()) || (input2 != null && input2.isPressed());
    }

    private void updateUI() {
        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        if (scoreValueLabel != null) {
            scoreValueLabel.setText(String.valueOf(gameLogic.getScore()));
        }
        if (levelValueLabel != null) {
            levelValueLabel.setText(String.valueOf(gameLogic.getLevel()));
        }
        if (linesValueLabel != null) {
            linesValueLabel.setText(String.valueOf(gameLogic.getLines()));
        }
        updatePreview();
    }

    public void renderGame(ShapeRenderer shapeRenderer) {
        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        int[][] board = gameLogic.getBoard();

        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 绘制游戏板背景
        shapeRenderer.setColor(COLOR_PANEL);
        shapeRenderer.rect(boardX - 4, boardY - 4, GameLogic.BOARD_WIDTH * cellSize + 8, GameLogic.BOARD_HEIGHT * cellSize + 8);

        // 绘制游戏板内部
        shapeRenderer.setColor(COLOR_BG);
        shapeRenderer.rect(boardX, boardY, GameLogic.BOARD_WIDTH * cellSize, GameLogic.BOARD_HEIGHT * cellSize);

        // 绘制网格线
        shapeRenderer.setColor(COLOR_PANEL_BORDER);
        for (int x = 0; x <= GameLogic.BOARD_WIDTH; x++) {
            shapeRenderer.rect(boardX + x * cellSize, boardY, 1, GameLogic.BOARD_HEIGHT * cellSize);
        }
        for (int y = 0; y <= GameLogic.BOARD_HEIGHT; y++) {
            shapeRenderer.rect(boardX, boardY + y * cellSize, GameLogic.BOARD_WIDTH * cellSize, 1);
        }

        // 绘制已固定的方块
        for (int y = 0; y < GameLogic.BOARD_HEIGHT; y++) {
            for (int x = 0; x < GameLogic.BOARD_WIDTH; x++) {
                int cell = board[y][x];
                if (cell != 0) {
                    float screenY = boardY + (GameLogic.BOARD_HEIGHT - 1 - y) * cellSize;
                    Color cellColor = getColorForCell(cell - 1);
                    shapeRenderer.setColor(cellColor);
                    shapeRenderer.rect(boardX + x * cellSize + 1, screenY + 1, cellSize - 2, cellSize - 2);

                    // 绘制高光效果
                    shapeRenderer.setColor(cellColor.cpy().add(0.2f, 0.2f, 0.2f, 0));
                    shapeRenderer.rect(boardX + x * cellSize + 1, screenY + cellSize - 4, cellSize - 2, 3);
                }
            }
        }

        // 绘制当前方块
        int currentPiece = gameLogic.getCurrentPiece();
        int currentPieceX = gameLogic.getCurrentPieceX();
        int currentPieceY = gameLogic.getCurrentPieceY();
        int currentPieceRotation = gameLogic.getCurrentPieceRotation();

        int[][] pieceShape = gameLogic.getPieceShape(currentPiece, currentPieceRotation);
        Color pieceColor = getColorForCell(currentPiece);

        for (int y = 0; y < pieceShape.length; y++) {
            for (int x = 0; x < pieceShape[y].length; x++) {
                if (pieceShape[y][x] != 0) {
                    int boardXPos = currentPieceX + x;
                    int boardYPos = currentPieceY + y;
                    if (boardXPos >= 0 && boardXPos < GameLogic.BOARD_WIDTH && boardYPos >= 0 && boardYPos < GameLogic.BOARD_HEIGHT) {
                        float screenY = boardY + (GameLogic.BOARD_HEIGHT - 1 - boardYPos) * cellSize;
                        shapeRenderer.setColor(pieceColor);
                        shapeRenderer.rect(boardX + boardXPos * cellSize + 1, screenY + 1, cellSize - 2, cellSize - 2);

                        // 高光效果
                        shapeRenderer.setColor(pieceColor.cpy().add(0.2f, 0.2f, 0.2f, 0));
                        shapeRenderer.rect(boardX + boardXPos * cellSize + 1, screenY + cellSize - 4, cellSize - 2, 3);
                    }
                }
            }
        }

        shapeRenderer.end();
    }

    private Color getColorForCell(int cell) {
        if (cell >= 0 && cell < Tetromino.COLORS.length) {
            return Tetromino.COLORS[cell];
        }
        return Color.GRAY;
    }

    private void calculateBoardPosition() {
        float baseCellSize = 32f;
        cellSize = baseCellSize * scale();

        float maxCellSize = h(42f);
        if (cellSize > maxCellSize) {
            cellSize = maxCellSize;
        }

        float boardHeight = GameLogic.BOARD_HEIGHT * cellSize;
        float boardYPos = (displayHeight() - boardHeight) / 2 + offsetY();

        boardX = offsetX() + w(80f);
        boardY = boardYPos;
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
    }

    // ==================== NetworkManager.NetworkListener 接口实现 ====================

    @Override
    public void onConnectResponse(boolean success, String message, String clientId) {
    }

    @Override
    public void onRoomResponse(RoomMessage message) {
    }

    @Override
    public void onGameStart(GameStartMessage message) {
    }

    @Override
    public void onGameStateUpdate(GameStateMessage message) {
    }

    @Override
    public void onDisconnected() {
        // 显示断开连接弹窗并返回主菜单
        LanguageManager lang = LanguageManager.getInstance();
        NotificationMessage message = new NotificationMessage();
        message.setNotificationType(NotificationMessage.NotificationType.DISCONNECTED);
        message.setTitle(lang.get("notification.title.disconnected"));
        message.setMessage(lang.get("error.connection.lost"));

        NotificationDialog dialog = new NotificationDialog(skin);
        dialog.setNotification(message);
        dialog.setOnCloseAction(() -> {
            // 停止本地服务器
            if (uiManager.getLocalServerManager() != null && uiManager.getLocalServerManager().isRunning()) {
                uiManager.getLocalServerManager().stopServer();
            }
            // 断开网络连接
            if (uiManager.getNetworkManager() != null) {
                uiManager.getNetworkManager().disconnect();
            }
            // 返回主菜单
            uiManager.setScreen(new MainMenuState(uiManager));
        });
        dialog.show(stage);
    }

    @Override
    public void onNotification(NotificationMessage message) {
    }

    @Override
    public void onPlayerScoresUpdate(PlayerScoresMessage message) {
    }

    @Override
    public void onCountdownUpdate(CountdownMessage message) {
    }

    @Override
    public void onCoopGameStateUpdate(me.catand.cooptetris.shared.message.CoopGameStateMessage message) {
    }

    @Override
    public void onPlayerSlotUpdate(PlayerSlotMessage message) {
    }
}
