package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import me.catand.cooptetris.input.InputBinding;
import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.model.Tetromino;
import me.catand.cooptetris.shared.tetris.GameLogic;
import me.catand.cooptetris.tetris.GameStateManager;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 游戏状态 - 使用新的简化缩放接口
 */
public class GameState extends BaseUIState {
    private Table uiTable;
    private final GameStateManager gameStateManager;
    private float cellSize;
    private Label scoreLabel;
    private Label levelLabel;
    private Label linesLabel;
    private boolean isDownKeyPressed = false;
    private float downKeyPressTime = 0;
    private final float initialDelay = 0.5f; // 初始延迟0.5秒
    private final float softDropInterval = 0.1f; // 软降间隔0.1秒
    private float lastSoftDropTime = 0;
    private float boardX; // 游戏板X坐标
    private float boardY; // 游戏板Y坐标

    public GameState(UIManager uiManager, GameStateManager gameStateManager) {
        super(uiManager);
        this.gameStateManager = gameStateManager;
    }

    // 辅助方法，获取LanguageManager实例
    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    @Override
    protected void createUI() {
        // 计算游戏板位置和大小
        calculateBoardPosition();

        // 创建根容器，限制在16:9显示区域内
        uiTable = new Table();
        uiTable.setPosition(offsetX(), offsetY());
        uiTable.setSize(displayWidth(), displayHeight());

        // 创建一个容器表格来放置UI元素
        Table uiContainer = new Table();
        uiContainer.right().top();
        uiContainer.padTop(h(100f));
        uiContainer.padRight(w(100f));

        Label scoreTitle = new Label(lang().get("score.title"), skin);
        scoreLabel = new Label("0", skin);

        Label levelTitle = new Label(lang().get("level.title"), skin);
        levelLabel = new Label("1", skin);

        Label linesTitle = new Label(lang().get("lines.title"), skin);
        linesLabel = new Label("0", skin);

        TextButton pauseButton = new TextButton(lang().get("pause"), skin);
        addCyanHoverEffect(pauseButton);
        pauseButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                // 暂停游戏
            }
            return true;
        });

        TextButton exitButton = new TextButton(lang().get("exit"), skin);
        addCyanHoverEffect(exitButton);
        exitButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                // 断开与服务端的连接
                if (uiManager.getNetworkManager() != null && uiManager.getNetworkManager().isConnected()) {
                    uiManager.getNetworkManager().disconnect();
                }
                // 停止本地服务器
                if (uiManager.getLocalServerManager() != null && uiManager.getLocalServerManager().isRunning()) {
                    uiManager.getLocalServerManager().stopServer();
                }
                // 返回主菜单
                uiManager.setScreen(new MainMenuState(uiManager));
            }
            return true;
        });

        // 按钮宽度和高度（使用设计分辨率的像素值）
        float buttonWidth = w(150f);
        float buttonHeight = h(50f);
        uiContainer.add(scoreTitle).right().padRight(w(10f));
        uiContainer.add(scoreLabel).padBottom(h(10f)).row();
        uiContainer.add(levelTitle).right().padRight(w(10f));
        uiContainer.add(levelLabel).padBottom(h(10f)).row();
        uiContainer.add(linesTitle).right().padRight(w(10f));
        uiContainer.add(linesLabel).padBottom(h(30f)).row();
        uiContainer.add(pauseButton).colspan(2).width(buttonWidth).height(buttonHeight).padBottom(h(10f)).row();
        uiContainer.add(exitButton).colspan(2).width(buttonWidth).height(buttonHeight).row();

        // 将容器表格添加到主表格的右侧
        uiTable.add().expandX();
        uiTable.add(uiContainer);
        uiTable.add().expandY();

        stage.addActor(uiTable);
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
        // 检查第一套和第二套控制键位
        if (isInputJustPressed(TetrisSettings.leftKey(), TetrisSettings.leftKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.LEFT);
        } else if (isInputJustPressed(TetrisSettings.rightKey(), TetrisSettings.rightKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.RIGHT);
        } else if (isInputJustPressed(TetrisSettings.rotateKey(), TetrisSettings.rotateKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.ROTATE_CLOCKWISE);
        } else if (isInputJustPressed(TetrisSettings.dropKey(), TetrisSettings.dropKey2())) {
            gameStateManager.handleInput(MoveMessage.MoveType.DROP);
        }

        // 单独处理下方向键（软降）
        if (isInputPressed(TetrisSettings.downKey(), TetrisSettings.downKey2())) {
            if (!isDownKeyPressed) {
                // 第一次按下，发送下落请求
                gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                isDownKeyPressed = true;
                downKeyPressTime = 0;
                lastSoftDropTime = 0;
            } else {
                // 持续按住
                downKeyPressTime += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
                if (downKeyPressTime >= initialDelay) {
                    // 超过初始延迟，开始发送连续下落请求
                    lastSoftDropTime += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
                    if (lastSoftDropTime >= softDropInterval) {
                        gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                        lastSoftDropTime = 0;
                    }
                }
            }
        } else {
            // 松开下方向键
            isDownKeyPressed = false;
        }
    }

    /**
     * 检查两个输入绑定中是否有任意一个被按下（单次触发）
     */
    private boolean isInputJustPressed(InputBinding input1, InputBinding input2) {
        return (input1 != null && input1.isJustPressed()) || (input2 != null && input2.isJustPressed());
    }

    /**
     * 检查两个输入绑定中是否有任意一个被按住（持续触发）
     */
    private boolean isInputPressed(InputBinding input1, InputBinding input2) {
        return (input1 != null && input1.isPressed()) || (input2 != null && input2.isPressed());
    }

    private void updateUI() {
        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        scoreLabel.setText(String.valueOf(gameLogic.getScore()));
        levelLabel.setText(String.valueOf(gameLogic.getLevel()));
        linesLabel.setText(String.valueOf(gameLogic.getLines()));
    }

    public void renderGame(ShapeRenderer shapeRenderer) {
        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        int[][] board = gameLogic.getBoard();

        // 应用viewport的变换
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 绘制游戏板
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rect(boardX, boardY, GameLogic.BOARD_WIDTH * cellSize, GameLogic.BOARD_HEIGHT * cellSize);

        // 绘制方块
        for (int y = 0; y < GameLogic.BOARD_HEIGHT; y++) {
            for (int x = 0; x < GameLogic.BOARD_WIDTH; x++) {
                int cell = board[y][x];
                if (cell != 0) {
                    // 计算正确的Y坐标，确保方块在屏幕内
                    float screenY = boardY + (GameLogic.BOARD_HEIGHT - 1 - y) * cellSize;
                    shapeRenderer.setColor(getColorForCell(cell - 1));
                    shapeRenderer.rect(boardX + x * cellSize, screenY, cellSize - 1, cellSize - 1);
                }
            }
        }

        // 绘制当前方块
        int currentPiece = gameLogic.getCurrentPiece();
        int currentPieceX = gameLogic.getCurrentPieceX();
        int currentPieceY = gameLogic.getCurrentPieceY();
        int currentPieceRotation = gameLogic.getCurrentPieceRotation();

        // 绘制当前方块
        int[][] pieceShape = gameLogic.getPieceShape(currentPiece, currentPieceRotation);
        for (int y = 0; y < pieceShape.length; y++) {
            for (int x = 0; x < pieceShape[y].length; x++) {
                if (pieceShape[y][x] != 0) {
                    int boardXPos = currentPieceX + x;
                    int boardYPos = currentPieceY + y;
                    if (boardXPos >= 0 && boardXPos < GameLogic.BOARD_WIDTH && boardYPos >= 0 && boardYPos < GameLogic.BOARD_HEIGHT) {
                        // 计算正确的Y坐标，确保方块在屏幕内
                        float screenY = boardY + (GameLogic.BOARD_HEIGHT - 1 - boardYPos) * cellSize;
                        shapeRenderer.setColor(getColorForCell(currentPiece));
                        shapeRenderer.rect(boardX + boardXPos * cellSize, screenY, cellSize - 1, cellSize - 1);
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

    /**
     * 计算游戏板的位置，确保它在屏幕内
     */
    private void calculateBoardPosition() {
        // 基础cellSize（基于1280x720分辨率）
        float baseCellSize = 30f;

        // 根据缩放比例调整cellSize
        cellSize = baseCellSize * scale();

        // 确保cellSize不会太大
        float maxCellSize = h(40f);
        if (cellSize > maxCellSize) {
            cellSize = maxCellSize;
        }

        // 计算游戏板在设计分辨率下的高度
        float boardHeight = GameLogic.BOARD_HEIGHT * cellSize;
        // 计算游戏板在设计分辨率下的垂直居中位置
        float boardYPos = (displayHeight() - boardHeight) / 2 + offsetY();

        // 计算游戏板X坐标（左侧留出空间）
        boardX = offsetX() + w(100f);
        boardY = boardYPos;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        // 调整游戏板大小和位置
        calculateBoardPosition();
    }

    @Override
    public void dispose() {
    }
}
