package me.catand.cooptetris.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import me.catand.cooptetris.input.InputBinding;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.model.Tetromino;
import me.catand.cooptetris.shared.tetris.CoopGameLogic;
import me.catand.cooptetris.tetris.GameStateManager;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 多人合作模式游戏界面（联机版）
 * - 20格宽的游戏板
 * - 4个出口，每个玩家从自己的出口下落
 * - 方块颜色根据玩家决定（蓝红绿黄）
 */
public class CoopGameState extends BaseUIState {

    // 玩家颜色：蓝、红、绿、黄
    private static final Color[] PLAYER_COLORS = {
        new Color(0.2f, 0.5f, 1.0f, 1.0f),    // 蓝色 - 玩家1
        new Color(1.0f, 0.2f, 0.2f, 1.0f),    // 红色 - 玩家2
        new Color(0.2f, 0.8f, 0.2f, 1.0f),    // 绿色 - 玩家3
        new Color(1.0f, 0.9f, 0.2f, 1.0f),    // 黄色 - 玩家4
    };

    private static final Color COLOR_BG = new Color(0.06f, 0.07f, 0.09f, 1f);
    private static final Color COLOR_PANEL = new Color(0.1f, 0.12f, 0.15f, 0.9f);
    private static final Color COLOR_PANEL_BORDER = new Color(0.2f, 0.23f, 0.28f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);

    private GameStateManager gameStateManager;
    private ShapeRenderer shapeRenderer;

    // 游戏板渲染参数
    private float boardX, boardY;
    private float cellSize;

    // UI元素
    private Label scoreLabel;
    private Label levelLabel;
    private Label linesLabel;
    private Label[] playerLabels;

    // 输入控制
    private boolean[] isDownKeyPressed;
    private float[] downKeyPressTime;
    private float[] lastSoftDropTime;
    private static final float initialDelay = 0.15f;
    private static final float softDropInterval = 0.05f;

    // 我的玩家索引
    private int myPlayerIndex;
    // 实际玩家数量
    private int playerCount;

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
        // 创建根表格
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // 顶部信息栏
        Table topTable = new Table();
        topTable.setBackground(createPanelBackground(COLOR_PANEL));
        topTable.pad(h(10));

        scoreLabel = new Label(lang().get("score") + ": 0", skin);
        scoreLabel.setColor(COLOR_TEXT);
        scoreLabel.setFontScale(scale());

        levelLabel = new Label(lang().get("level") + ": 1", skin);
        levelLabel.setColor(COLOR_TEXT);
        levelLabel.setFontScale(scale());

        linesLabel = new Label(lang().get("lines") + ": 0", skin);
        linesLabel.setColor(COLOR_TEXT);
        linesLabel.setFontScale(scale());

        topTable.add(scoreLabel).padRight(w(30));
        topTable.add(levelLabel).padRight(w(30));
        topTable.add(linesLabel);

        rootTable.add(topTable).expandX().fillX().pad(h(10)).row();

        // 游戏区域（由ShapeRenderer绘制）
        Table gameArea = new Table();
        rootTable.add(gameArea).expand().fill().row();

        // 底部玩家信息
        Table bottomTable = new Table();
        bottomTable.setBackground(createPanelBackground(COLOR_PANEL));
        bottomTable.pad(h(10));

        String[] playerNames = {"P1", "P2", "P3", "P4"};

        // 只显示实际玩家数量，使用正确的玩家索引颜色
        for (int i = 0; i < playerCount && i < 4; i++) {
            // 获取第i个玩家分配到的玩家索引
            int assignedPlayerIndex = CoopGameLogic.PLAYER_ASSIGNMENT_ORDER[i];
            playerLabels[i] = new Label(playerNames[i], skin);
            playerLabels[i].setColor(PLAYER_COLORS[assignedPlayerIndex]);
            playerLabels[i].setFontScale(scale());
            bottomTable.add(playerLabels[i]).padRight(w(20));
        }

        rootTable.add(bottomTable).expandX().fillX().pad(h(10)).row();

        // 控制按钮
        Table buttonTable = new Table();
        TextButton exitButton = new TextButton(lang().get("exit"), skin);
        exitButton.getLabel().setFontScale(scale());
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                returnToMenu();
            }
        });

        buttonTable.add(exitButton).width(w(120)).height(h(40));

        rootTable.add(buttonTable).pad(h(10));

        // 设置输入
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        // 计算游戏板位置
        calculateBoardPosition();
    }

    private void calculateBoardPosition() {
        // 计算单元格大小
        float maxBoardWidth = displayWidth() * 0.8f;
        float maxBoardHeight = displayHeight() * 0.6f;

        float cellWidth = maxBoardWidth / CoopGameLogic.BOARD_WIDTH;
        float cellHeight = maxBoardHeight / CoopGameLogic.BOARD_HEIGHT;
        cellSize = Math.min(cellWidth, cellHeight);

        float boardWidth = CoopGameLogic.BOARD_WIDTH * cellSize;
        float boardHeight = CoopGameLogic.BOARD_HEIGHT * cellSize;

        boardX = (displayWidth() - boardWidth) / 2;
        boardY = (displayHeight() - boardHeight) / 2;
    }

    @Override
    public void update(float delta) {
        stage.act(delta);

        // 处理输入（只处理自己的玩家）
        handleInput(delta);

        // 更新UI
        updateUI();
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
                if (downKeyPressTime[myPlayerIndex] >= initialDelay) {
                    lastSoftDropTime[myPlayerIndex] += delta;
                    if (lastSoftDropTime[myPlayerIndex] >= softDropInterval) {
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

        scoreLabel.setText(lang().get("score") + ": " + coopGameLogic.getScore());
        levelLabel.setText(lang().get("level") + ": " + coopGameLogic.getLevel());
        linesLabel.setText(lang().get("lines") + ": " + coopGameLogic.getLines());

        // 更新玩家状态显示（只更新实际玩家数量）
        for (int i = 0; i < playerCount && i < 4; i++) {
            // 获取第i个玩家分配到的玩家索引
            int assignedPlayerIndex = CoopGameLogic.PLAYER_ASSIGNMENT_ORDER[i];
            CoopGameLogic.PlayerPiece piece = coopGameLogic.getPlayerPiece(assignedPlayerIndex);
            if (piece.isActive()) {
                playerLabels[i].setText("P" + (i + 1) + " ✓");
            } else {
                playerLabels[i].setText("P" + (i + 1) + " ✗");
            }
        }
    }

    /**
     * 渲染游戏板 - 由 Main.java 调用
     */
    public void renderGame(ShapeRenderer shapeRenderer) {
        renderGameBoard(shapeRenderer);
    }

    private void renderGameBoard(ShapeRenderer shapeRenderer) {
        CoopGameLogic coopGameLogic = gameStateManager.getSharedManager().getCoopGameLogic();
        if (coopGameLogic == null) return;

        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
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
        if (coopGameLogic != null) {
            for (int i = 0; i < playerCount && i < 4; i++) {
                // 获取第i个玩家分配到的玩家索引
                int assignedPlayerIndex = CoopGameLogic.PLAYER_ASSIGNMENT_ORDER[i];
                int exitX = CoopGameLogic.EXIT_POSITIONS[assignedPlayerIndex];
                shapeRenderer.setColor(PLAYER_COLORS[assignedPlayerIndex]);
                shapeRenderer.rect(boardX + exitX * cellSize - 2, boardY + CoopGameLogic.BOARD_HEIGHT * cellSize,
                    3 * cellSize + 4, 6);
            }
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
            // 获取第i个玩家分配到的玩家索引
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

    private void returnToMenu() {
        uiManager.setScreen(new MainMenuState(uiManager));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        calculateBoardPosition();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    /**
     * 创建面板背景
     */
    protected com.badlogic.gdx.scenes.scene2d.utils.Drawable createPanelBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
            new com.badlogic.gdx.graphics.g2d.TextureRegion(texture));
    }
}
