package me.catand.cooptetris.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import me.catand.cooptetris.shared.message.MoveMessage;
import me.catand.cooptetris.shared.model.Tetromino;
import me.catand.cooptetris.shared.tetris.GameLogic;
import me.catand.cooptetris.tetris.GameStateManager;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.UIScaler;

public class GameState implements UIState {
    private Stage stage;
    private Skin skin;
    private Table uiTable;
    private final UIManager uiManager;
    private final GameStateManager gameStateManager;
    private final ShapeRenderer shapeRenderer;
    private float cellSize;
    private Label scoreLabel;
    private Label levelLabel;
    private Label linesLabel;
    private boolean isDownKeyPressed = false;
    private float downKeyPressTime = 0;
    private float initialDelay = 0.5f; // 初始延迟0.5秒
    private float softDropInterval = 0.1f; // 软降间隔0.1秒
    private float lastSoftDropTime = 0;
    private float boardX; // 游戏板X坐标
    private float boardY; // 游戏板Y坐标

    public GameState(UIManager uiManager, GameStateManager gameStateManager) {
        this.uiManager = uiManager;
        this.gameStateManager = gameStateManager;
        shapeRenderer = new ShapeRenderer();
        cellSize = 30f;
        calculateBoardPosition();
    }

    // 辅助方法，获取LanguageManager实例
    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    @Override
    public void show(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;

        uiTable = new Table();
        // 使用UIScaler计算UI表的位置，使其在屏幕右侧
        UIScaler scaler = UIScaler.getInstance();
        float x = scaler.toScreenX(1080); // 设计时X坐标
        float y = scaler.toScreenY(100);  // 设计时Y坐标
        uiTable.setPosition(x, y);

        Label scoreTitle = new Label(lang().get("score.title"), skin);
        scoreLabel = new Label("0", skin);

        Label levelTitle = new Label(lang().get("level.title"), skin);
        levelLabel = new Label("1", skin);

        Label linesTitle = new Label(lang().get("lines.title"), skin);
        linesLabel = new Label("0", skin);

        TextButton pauseButton = new TextButton(lang().get("pause"), skin);
        pauseButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                // 暂停游戏
            }
            return true;
        });

        TextButton exitButton = new TextButton(lang().get("exit"), skin);
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

        // 使用UIScaler缩放按钮宽度
        float buttonWidth = scaler.toScreenWidth(150f);
        uiTable.add(scoreTitle).right().padRight(10f);
        uiTable.add(scoreLabel).padBottom(10f).row();
        uiTable.add(levelTitle).right().padRight(10f);
        uiTable.add(levelLabel).padBottom(10f).row();
        uiTable.add(linesTitle).right().padRight(10f);
        uiTable.add(linesLabel).padBottom(30f).row();
        uiTable.add(pauseButton).colspan(2).width(buttonWidth).padBottom(10f).row();
        uiTable.add(exitButton).colspan(2).width(buttonWidth).row();

        stage.addActor(uiTable);
    }

    @Override
    public void hide() {
        uiTable.remove();
    }

    @Override
    public void update(float delta) {
        handleInput();
        gameStateManager.update(delta);
        updateUI();
    }
    
    private boolean isProcessingSoftDrop = false;

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            gameStateManager.handleInput(MoveMessage.MoveType.LEFT);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            gameStateManager.handleInput(MoveMessage.MoveType.RIGHT);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            gameStateManager.handleInput(MoveMessage.MoveType.ROTATE_CLOCKWISE);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            gameStateManager.handleInput(MoveMessage.MoveType.DROP);
        }
        
        // 单独处理下方向键
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (!isDownKeyPressed) {
                // 第一次按下，发送下落请求
                gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                isDownKeyPressed = true;
                downKeyPressTime = 0;
                lastSoftDropTime = 0;
            } else {
                // 持续按住
                downKeyPressTime += Gdx.graphics.getDeltaTime();
                if (downKeyPressTime >= initialDelay) {
                    // 超过初始延迟，开始发送连续下落请求
                    lastSoftDropTime += Gdx.graphics.getDeltaTime();
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

    private void updateUI() {
        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        scoreLabel.setText(String.valueOf(gameLogic.getScore()));
        levelLabel.setText(String.valueOf(gameLogic.getLevel()));
        linesLabel.setText(String.valueOf(gameLogic.getLines()));
    }

    public void renderGame(ShapeRenderer shapeRenderer) {
        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        int[][] board = gameLogic.getBoard();

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
        // 使用UIScaler获取缩放比例
        UIScaler scaler = UIScaler.getInstance();
        float scale = scaler.getScale();
        
        // 设计时的cellSize（基于1280x720分辨率）
        float designCellSize = 30f;
        
        // 根据缩放比例计算实际的cellSize
        cellSize = designCellSize * scale;
        
        // 确保cellSize不会太大
        float maxCellSize = 40f * scale;
        if (cellSize > maxCellSize) {
            cellSize = maxCellSize;
        }
        
        // 计算游戏板大小
        float boardWidth = GameLogic.BOARD_WIDTH * cellSize;
        float boardHeight = GameLogic.BOARD_HEIGHT * cellSize;
        
        // 使用UIScaler计算游戏板位置，基于设计时的坐标
        boardX = scaler.toScreenX(100); // 设计时X坐标
        boardY = scaler.toScreenY((720 - boardHeight) / 2); // 垂直居中
        
        // 确保游戏板不会超出屏幕
        if (boardY < scaler.getOffsetY()) {
            boardY = scaler.getOffsetY();
        }
    }

    @Override
    public void resize(int width, int height) {
        // 更新UIScaler
        UIScaler scaler = UIScaler.getInstance();
        scaler.update();
        
        // 调整游戏板大小和位置
        calculateBoardPosition();
        
        // 调整UI表的位置
        if (uiTable != null) {
            float x = scaler.toScreenX(1080); // 设计时X坐标
            float y = scaler.toScreenY(100);  // 设计时Y坐标
            uiTable.setPosition(x, y);
            
            // 调整按钮宽度
            float buttonWidth = scaler.toScreenWidth(150f);
            uiTable.getCells().forEach(cell -> {
                if (cell.getActor() instanceof TextButton) {
                    cell.width(buttonWidth);
                }
            });
            uiTable.invalidateHierarchy(); // 重新计算布局
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
}
