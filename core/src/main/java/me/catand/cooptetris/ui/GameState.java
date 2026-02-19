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
import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.model.Tetromino;
import me.catand.cooptetris.shared.tetris.GameLogic;
import me.catand.cooptetris.tetris.GameStateManager;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 游戏状态 - 现代化暗色游戏UI风格
 */
public class GameState extends BaseUIState {
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
        calculateBoardPosition();

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

        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get("default", com.badlogic.gdx.graphics.g2d.BitmapFont.class), COLOR_TEXT_MUTED);
        Label titleLabel = new Label(title, titleStyle);
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

        Label.LabelStyle previewTitleStyle = new Label.LabelStyle(skin.get("default", com.badlogic.gdx.graphics.g2d.BitmapFont.class), COLOR_TEXT_MUTED);
        Label previewTitle = new Label(lang().get("next.piece"), previewTitleStyle);
        previewTitle.setAlignment(Align.center);

        previewPanel.add(previewTitle).fillX().padBottom(h(10f)).row();

        // 预览区域（空白，后续可添加方块预览）
        Table previewArea = new Table();
        previewArea.setBackground(createPanelBackground(COLOR_PANEL));
        previewPanel.add(previewArea).height(w(100f)).fillX();

        return previewPanel;
    }

    private Table createBottomPanel() {
        Table bottomPanel = new Table();

        TextButton pauseButton = new TextButton(lang().get("pause"), skin);
        pauseButton.setColor(COLOR_PRIMARY);
        pauseButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                // 暂停游戏
            }
            return true;
        });

        TextButton exitButton = new TextButton(lang().get("exit"), skin);
        exitButton.setColor(COLOR_DANGER);
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
    }

    @Override
    public void update(float delta) {
        handleInput();
        gameStateManager.update(delta);
        updateUI();
    }

    private void handleInput() {
        if (isInputJustPressed(TetrisSettings.leftKey(), TetrisSettings.leftKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.LEFT);
        } else if (isInputJustPressed(TetrisSettings.rightKey(), TetrisSettings.rightKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.RIGHT);
        } else if (isInputJustPressed(TetrisSettings.rotateKey(), TetrisSettings.rotateKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.ROTATE_CLOCKWISE);
        } else if (isInputJustPressed(TetrisSettings.dropKey(), TetrisSettings.dropKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.DROP);
        }

        // 软降处理
        if (isInputPressed(TetrisSettings.downKey(), TetrisSettings.downKey2())) {
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
        } else {
            isDownKeyPressed = false;
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
}
