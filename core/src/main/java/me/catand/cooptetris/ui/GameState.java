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

public class GameState implements UIState {
    private Stage stage;
    private Skin skin;
    private Table uiTable;
    private final UIManager uiManager;
    private final GameStateManager gameStateManager;
    private final ShapeRenderer shapeRenderer;
    private final float cellSize;
    private Label scoreLabel;
    private Label levelLabel;
    private Label linesLabel;

    public GameState(UIManager uiManager, GameStateManager gameStateManager) {
        this.uiManager = uiManager;
        this.gameStateManager = gameStateManager;
        shapeRenderer = new ShapeRenderer();
        cellSize = 30f;
    }

    @Override
    public void show(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;

        uiTable = new Table();
        uiTable.setPosition(350, 100);

        Label scoreTitle = new Label("Score", skin);
        scoreLabel = new Label("0", skin);

        Label levelTitle = new Label("Level", skin);
        levelLabel = new Label("1", skin);

        Label linesTitle = new Label("Lines", skin);
        linesLabel = new Label("0", skin);

        TextButton pauseButton = new TextButton("Pause", skin);
        pauseButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                // 暂停游戏
            }
            return true;
        });

        TextButton exitButton = new TextButton("Exit", skin);
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

        uiTable.add(scoreTitle).right().padRight(10f);
        uiTable.add(scoreLabel).padBottom(10f).row();
        uiTable.add(levelTitle).right().padRight(10f);
        uiTable.add(levelLabel).padBottom(10f).row();
        uiTable.add(linesTitle).right().padRight(10f);
        uiTable.add(linesLabel).padBottom(30f).row();
        uiTable.add(pauseButton).colspan(2).width(150f).padBottom(10f).row();
        uiTable.add(exitButton).colspan(2).width(150f).row();

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

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            gameStateManager.handleInput(MoveMessage.MoveType.LEFT);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            gameStateManager.handleInput(MoveMessage.MoveType.RIGHT);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            gameStateManager.handleInput(MoveMessage.MoveType.ROTATE_CLOCKWISE);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            gameStateManager.handleInput(MoveMessage.MoveType.DROP);
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
        shapeRenderer.rect(50, 50, GameLogic.BOARD_WIDTH * cellSize, GameLogic.BOARD_HEIGHT * cellSize);

        // 绘制方块
        for (int y = 0; y < GameLogic.BOARD_HEIGHT; y++) {
            for (int x = 0; x < GameLogic.BOARD_WIDTH; x++) {
                int cell = board[y][x];
                if (cell != 0) {
                    // 计算正确的Y坐标，确保方块在屏幕内
                    float screenY = 50 + (GameLogic.BOARD_HEIGHT - 1 - y) * cellSize;
                    shapeRenderer.setColor(getColorForCell(cell - 1));
                    shapeRenderer.rect(50 + x * cellSize, screenY, cellSize - 1, cellSize - 1);
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
                    int boardX = currentPieceX + x;
                    int boardY = currentPieceY + y;
                    if (boardX >= 0 && boardX < GameLogic.BOARD_WIDTH && boardY >= 0 && boardY < GameLogic.BOARD_HEIGHT) {
                        // 计算正确的Y坐标，确保方块在屏幕内
                        float screenY = 50 + (GameLogic.BOARD_HEIGHT - 1 - boardY) * cellSize;
                        shapeRenderer.setColor(getColorForCell(currentPiece));
                        shapeRenderer.rect(50 + boardX * cellSize, screenY, cellSize - 1, cellSize - 1);
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

    @Override
    public void resize(int width, int height) {
        // 调整游戏板大小
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
}
