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

import java.util.ArrayList;
import java.util.List;

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
 * PVP游戏状态 - 对战模式界面
 * 左侧：自己的游戏区域
 * 中间：分割区域，上部显示自己的分数信息，下部显示排行榜/对手信息
 * 右侧：对手游戏区域（两人对战显示对手，多人对战显示分数最高的两人）
 */
public class PVPGameState extends BaseUIState implements GameStateManager.PlayerScoresListener, NetworkManager.NetworkListener {
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
    private BitmapFont smallFont;

    private boolean isDownKeyPressed = false;
    private float downKeyPressTime = 0;
    private final float initialDelay = 0.5f;
    private final float softDropInterval = 0.1f;
    private float lastSoftDropTime = 0;
    private float selfBoardX;
    private float selfBoardY;
    private float opponentBoardX;
    private float opponentBoardY;
    private float selfCellSize;
    private float opponentCellSize;

    // 游戏结束弹窗
    private NotificationDialog gameOverDialog;
    private boolean isGameOverShown = false;

    // 暂停状态
    private boolean isPaused = false;
    private Table pauseOverlay;

    // 下一个方块预览
    private Table previewArea;

    // PVP玩家分数信息
    private List<PlayerScoresMessage.PlayerScore> playerScores = new ArrayList<>();
    private int myPlayerIndex = 0;
    private Table leaderboardTable;
    private Table opponentPanel;

    // 游戏板Table引用（用于计算实际位置）
    private Table selfBoardArea;
    private Table opponentBoardArea;

    // 触屏输入处理器
    private TouchInputProcessor touchInput;

    // 观战者模式
    private boolean spectatorMode = false;

    // UI颜色配置
    private static final Color COLOR_BG = new Color(0.06f, 0.07f, 0.09f, 1f);
    private static final Color COLOR_PANEL = new Color(0.1f, 0.12f, 0.15f, 0.9f);
    private static final Color COLOR_PANEL_BORDER = new Color(0.2f, 0.23f, 0.28f, 1f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);
    private static final Color COLOR_SECONDARY = new Color(0.8f, 0.3f, 0.9f, 1f);
    private static final Color COLOR_SUCCESS = new Color(0.3f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_DANGER = new Color(1f, 0.3f, 0.3f, 1f);
    private static final Color COLOR_WARNING = new Color(1f, 0.7f, 0.2f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color COLOR_TEXT_MUTED = new Color(0.5f, 0.52f, 0.55f, 1f);

    public PVPGameState(UIManager uiManager, GameStateManager gameStateManager) {
        super(uiManager);
        this.gameStateManager = gameStateManager;
        // 设置玩家分数监听器
        gameStateManager.setPlayerScoresListener(this);
    }

    /**
     * 设置观战者模式
     */
    public void setSpectatorMode(boolean spectatorMode) {
        this.spectatorMode = spectatorMode;
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

        calculateBoardPositions();

        // 初始化触屏输入处理器
        touchInput = new TouchInputProcessor(stage);
        com.badlogic.gdx.Gdx.input.setInputProcessor(new com.badlogic.gdx.InputMultiplexer(stage, touchInput));

        titleFont = Main.platform.getFont(fontSize(24), lang().get("game.title"), false, false);
        statsFont = Main.platform.getFont(fontSize(20), "0123456789", false, false);
        smallFont = Main.platform.getFont(fontSize(14), "Players", false, false);

        // 主容器
        uiTable = new Table();
        uiTable.setFillParent(true);

        // 内容容器
        Table contentTable = new Table();
        contentTable.center();

        // 上部区域：左中右三栏
        Table gameArea = new Table();

        // 左侧：自己的游戏板区域
        Table leftPanel = createSelfGamePanel();
        gameArea.add(leftPanel).width(w(380f)).height(h(580f)).padRight(w(10f));

        // 中间：分数信息和排行榜
        Table centerPanel = createCenterPanel();
        gameArea.add(centerPanel).width(w(200f)).height(h(580f)).padRight(w(10f));

        // 右侧：对手游戏区域
        Table rightPanel = createOpponentGamePanel();
        gameArea.add(rightPanel).width(w(380f)).height(h(580f));

        contentTable.add(gameArea).row();

        // 底部按钮区域
        Table bottomPanel = createBottomPanel();
        contentTable.add(bottomPanel).fillX().padTop(h(20f));

        uiTable.add(contentTable).expand().center();
        stage.addActor(uiTable);
    }

    /**
     * 创建自己的游戏面板（左侧）
     */
    private Table createSelfGamePanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(10f));

        // 玩家名称标签 - 使用动态字体
        Label nameLabel = FontUtils.createLabel(lang().get("you"), skin, fontSize(14), COLOR_PRIMARY);
        nameLabel.setAlignment(Align.center);
        panel.add(nameLabel).fillX().padBottom(h(10f)).row();

        // 游戏板区域（由ShapeRenderer绘制）
        selfBoardArea = new Table();
        selfBoardArea.setBackground(createPanelBackground(COLOR_BG));
        panel.add(selfBoardArea).width(w(280f)).height(h(420f)).padBottom(h(10f)).row();

        // 下一个方块预览
        Table previewPanel = createPreviewPanel();
        panel.add(previewPanel).fillX().height(h(100f));

        return panel;
    }

    /**
     * 创建对手游戏面板（右侧）
     */
    private Table createOpponentGamePanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(10f));

        int playerCount = gameStateManager.getPlayerCount();

        if (playerCount <= 2) {
            // 两人对战：显示单个对手的游戏板
            Label nameLabel = FontUtils.createLabel(lang().get("opponent"), skin, fontSize(14), COLOR_SECONDARY);
            nameLabel.setAlignment(Align.center);
            panel.add(nameLabel).fillX().padBottom(h(10f)).row();

            // 对手游戏板区域
            opponentBoardArea = new Table();
            opponentBoardArea.setBackground(createPanelBackground(COLOR_BG));
            panel.add(opponentBoardArea).width(w(280f)).height(h(420f)).padBottom(h(10f)).row();

            // 对手信息
            for (PlayerScoresMessage.PlayerScore score : playerScores) {
                if (score.getPlayerIndex() != myPlayerIndex) {
                    Table infoTable = createOpponentInfoTable(score);
                    panel.add(infoTable).fillX().height(h(100f));
                    break;
                }
            }
        } else {
            // 多人对战：显示排行榜和顶部玩家
            Label titleLabel = FontUtils.createLabel(lang().get("top.players"), skin, fontSize(14), COLOR_SECONDARY);
            titleLabel.setAlignment(Align.center);
            panel.add(titleLabel).fillX().padBottom(h(10f)).row();

            // 显示前两名（不包括自己）
            int displayed = 0;
            for (PlayerScoresMessage.PlayerScore score : playerScores) {
                if (score.getPlayerIndex() != myPlayerIndex && displayed < 2) {
                    Table infoTable = createOpponentInfoTable(score);
                    panel.add(infoTable).fillX().padBottom(h(10f)).row();
                    displayed++;
                }
            }

            // 填充剩余空间
            panel.add().expand().fill();
        }

        return panel;
    }

    /**
     * 创建中间面板（分数信息 + 排行榜）
     */
    private Table createCenterPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(10f));

        // 上部：自己的分数信息
        Table scorePanel = createScorePanel();
        panel.add(scorePanel).fillX().height(h(250f)).padBottom(h(10f)).row();

        // 下部：排行榜
        Table leaderboardPanel = createLeaderboardPanel();
        panel.add(leaderboardPanel).fillX().height(h(320f));

        return panel;
    }

    /**
     * 创建分数面板
     */
    private Table createScorePanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_BG));
        panel.pad(w(10f));

        Label titleLabel = FontUtils.createLabel(lang().get("your.stats"), skin, fontSize(14), COLOR_PRIMARY);
        titleLabel.setAlignment(Align.center);
        panel.add(titleLabel).fillX().padBottom(h(15f)).row();

        // 分数
        panel.add(createStatRow(lang().get("score.title"), "0", COLOR_PRIMARY)).fillX().padBottom(h(10f)).row();

        // 等级
        panel.add(createStatRow(lang().get("level.title"), "1", COLOR_SECONDARY)).fillX().padBottom(h(10f)).row();

        // 行数
        panel.add(createStatRow(lang().get("lines.title"), "0", COLOR_SUCCESS)).fillX();

        return panel;
    }

    private Table createStatRow(String title, String initialValue, Color accentColor) {
        Table row = new Table();

        Label titleLabel = FontUtils.createLabel(title, skin, fontSize(16), COLOR_TEXT_MUTED);
        titleLabel.setAlignment(Align.left);

        Label.LabelStyle valueStyle = new Label.LabelStyle(statsFont, accentColor);
        Label valueLabel = new Label(initialValue, valueStyle);
        valueLabel.setAlignment(Align.right);

        // 保存引用
        if (title.equals(lang().get("score.title"))) {
            scoreValueLabel = valueLabel;
        } else if (title.equals(lang().get("level.title"))) {
            levelValueLabel = valueLabel;
        } else if (title.equals(lang().get("lines.title"))) {
            linesValueLabel = valueLabel;
        }

        row.add(titleLabel).left().expandX();
        row.add(valueLabel).right();

        return row;
    }

    /**
     * 创建排行榜面板
     */
    private Table createLeaderboardPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_BG));
        panel.pad(w(10f));

        Label titleLabel = FontUtils.createLabel(lang().get("leaderboard"), skin, fontSize(14), COLOR_WARNING);
        titleLabel.setAlignment(Align.center);
        panel.add(titleLabel).fillX().padBottom(h(10f)).row();

        // 排行榜内容区域
        leaderboardTable = new Table();
        leaderboardTable.top().left();
        panel.add(leaderboardTable).fill().expand();

        return panel;
    }

    /**
     * 更新排行榜显示
     */
    private void updateLeaderboard() {
        if (leaderboardTable == null) return;

        leaderboardTable.clear();

        int rank = 1;
        for (PlayerScoresMessage.PlayerScore score : playerScores) {
            Table row = new Table();
            row.padBottom(h(8f));

            // 排名
            String rankText = rank + ".";
            Color rankColor = rank == 1 ? COLOR_WARNING : (rank == 2 ? COLOR_SECONDARY : COLOR_TEXT_MUTED);
            Label rankLabel = FontUtils.createLabel(rankText, skin, fontSize(14), rankColor);
            row.add(rankLabel).width(w(25f)).left();

            // 玩家名称
            String nameText = score.getPlayerName();
            if (score.getPlayerIndex() == myPlayerIndex) {
                nameText += " " + lang().get("you.suffix");
            }
            Color nameColor = score.getPlayerIndex() == myPlayerIndex ? COLOR_PRIMARY : COLOR_TEXT;
            Label nameLabel = FontUtils.createLabel(nameText, skin, fontSize(14), nameColor);
            row.add(nameLabel).left().expandX().padRight(w(5f));

            // 分数
            Label.LabelStyle scoreStyle = new Label.LabelStyle(statsFont, COLOR_TEXT);
            Label scoreLabel = new Label(String.valueOf(score.getScore()), scoreStyle);
            row.add(scoreLabel).right();

            // 游戏结束标记
            if (score.isGameOver()) {
                Label overLabel = FontUtils.createLabel(lang().get("game.over.short"), skin, fontSize(14), COLOR_DANGER);
                row.add(overLabel).width(w(40f)).right().padLeft(w(5f));
            }

            leaderboardTable.add(row).fillX().row();
            rank++;
        }
    }

    /**
     * 创建对手面板（两人对战时显示对手，多人对战时显示前两名）
     */
    private Table createOpponentPanel() {
        opponentPanel = new Table();
        opponentPanel.setBackground(createPanelBackground(COLOR_PANEL));
        opponentPanel.pad(w(10f));

        // 动态更新对手显示
        updateOpponentPanel();

        return opponentPanel;
    }

    /**
     * 更新对手面板
     */
    private void updateOpponentPanel() {
        if (opponentPanel == null) return;

        opponentPanel.clear();

        int playerCount = gameStateManager.getPlayerCount();

        if (playerCount <= 2) {
            // 两人对战：显示单个对手
            Label titleLabel = FontUtils.createLabel(lang().get("opponent"), skin, fontSize(14), COLOR_SECONDARY);
            titleLabel.setAlignment(Align.center);
            opponentPanel.add(titleLabel).fillX().padBottom(h(10f)).row();

            // 对手信息
            for (PlayerScoresMessage.PlayerScore score : playerScores) {
                if (score.getPlayerIndex() != myPlayerIndex) {
                    Table infoTable = createOpponentInfoTable(score);
                    opponentPanel.add(infoTable).fillX().padBottom(h(10f)).row();
                    break;
                }
            }

            // 占位区域
            opponentPanel.add().height(h(450f));
        } else {
            // 多人对战：显示分数最高的两人（不包括自己）
            Label titleLabel = FontUtils.createLabel(lang().get("top.players"), skin, fontSize(14), COLOR_SECONDARY);
            titleLabel.setAlignment(Align.center);
            opponentPanel.add(titleLabel).fillX().padBottom(h(10f)).row();

            int displayed = 0;
            for (PlayerScoresMessage.PlayerScore score : playerScores) {
                if (score.getPlayerIndex() != myPlayerIndex && displayed < 2) {
                    Table infoTable = createOpponentInfoTable(score);
                    opponentPanel.add(infoTable).fillX().padBottom(h(10f)).row();
                    displayed++;
                }
            }

            // 填充剩余空间
            opponentPanel.add().expand().fill();
        }
    }

    private Table createOpponentInfoTable(PlayerScoresMessage.PlayerScore score) {
        Table table = new Table();
        table.setBackground(createPanelBackground(COLOR_BG));
        table.pad(w(8f));

        // 玩家名称
        Label nameLabel = FontUtils.createLabel(score.getPlayerName(), skin, fontSize(14), COLOR_TEXT);
        table.add(nameLabel).left().row();

        // 分数信息
        Table statsTable = new Table();

        String scoreText = lang().get("score.short") + ": " + score.getScore();
        Label scoreLabel = FontUtils.createLabel(scoreText, skin, fontSize(14), COLOR_TEXT_MUTED);
        statsTable.add(scoreLabel).left().padRight(w(10f));

        String linesText = lang().get("lines.short") + ": " + score.getLines();
        Label linesLabel = FontUtils.createLabel(linesText, skin, fontSize(14), COLOR_TEXT_MUTED);
        statsTable.add(linesLabel).left();

        table.add(statsTable).left().padTop(h(5f));

        return table;
    }

    private Table createPreviewPanel() {
        Table previewPanel = new Table();
        previewPanel.setBackground(createPanelBackground(COLOR_BG));
        previewPanel.pad(w(10f));

        Label previewTitle = FontUtils.createLabel(lang().get("next.piece"), skin, fontSize(16), COLOR_TEXT_MUTED);
        previewTitle.setAlignment(Align.center);

        previewPanel.add(previewTitle).fillX().padBottom(h(5f)).row();

        // 预览区域
        previewArea = new Table();
        previewArea.setBackground(createPanelBackground(COLOR_PANEL));
        previewPanel.add(previewArea).height(w(60f)).fillX();

        return previewPanel;
    }

    private void updatePreview() {
        if (previewArea == null) return;

        previewArea.clear();

        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        int nextPiece = gameLogic.getNextPiece();
        int[][] pieceShape = Tetromino.SHAPES[nextPiece];

        float cellSize = w(12f);
        int pieceSize = pieceShape.length;

        Table pieceTable = new Table();

        for (int y = 0; y < pieceSize; y++) {
            for (int x = 0; x < pieceSize; x++) {
                if (pieceShape[y][x] != 0) {
                    Table cell = new Table();
                    cell.setBackground(createColoredBackground(Tetromino.COLORS[nextPiece]));
                    pieceTable.add(cell).size(cellSize).pad(w(1f));
                } else {
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
        // 清除监听器
        gameStateManager.setPlayerScoresListener(null);
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

    private void checkGameOver() {
        // PVP模式下，检查自己是否游戏结束
        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        if (gameLogic.isGameOver() && !isGameOverShown) {
            isGameOverShown = true;
            // 不再显示单个玩家的弹窗，通过灰色遮罩表示失败
        }

        // 检查是否所有玩家都结束了（游戏真正结束）
        checkAllPlayersGameOver();
    }

    private boolean allPlayersGameOverChecked = false;

    private void checkAllPlayersGameOver() {
        if (allPlayersGameOverChecked || playerScores.isEmpty()) return;

        // 检查是否所有玩家都游戏结束
        boolean allGameOver = true;
        int activePlayers = 0;

        for (PlayerScoresMessage.PlayerScore score : playerScores) {
            if (!score.isGameOver()) {
                allGameOver = false;
                break;
            }
            activePlayers++;
        }

        // 只有所有玩家都结束且至少有两个玩家参与时才显示最终结算
        if (allGameOver && activePlayers >= 2 && !isGameOverShown) {
            allPlayersGameOverChecked = true;
            isGameOverShown = true;
            showFinalGameOverDialog();
        }
    }

    private void showFinalGameOverDialog() {
        // 计算最终排名
        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        int myScore = gameLogic.getScore();
        int rank = 1;

        for (PlayerScoresMessage.PlayerScore score : playerScores) {
            if (score.getPlayerIndex() != myPlayerIndex && score.getScore() > myScore) {
                rank++;
            }
        }

        // 构建最终排名信息
        StringBuilder rankInfo = new StringBuilder();
        rankInfo.append(lang().get("final.rankings")).append("\n\n");

        int currentRank = 1;
        for (PlayerScoresMessage.PlayerScore score : playerScores) {
            rankInfo.append("#").append(currentRank).append(" ")
                .append(score.getPlayerName());
            if (score.getPlayerIndex() == myPlayerIndex) {
                rankInfo.append(" ").append(lang().get("you.suffix"));
            }
            rankInfo.append(": ").append(score.getScore()).append("\n");
            currentRank++;
        }

        NotificationMessage message = new NotificationMessage();
        message.setNotificationType(NotificationMessage.NotificationType.INFO);
        message.setTitle(lang().get("game.over.final.title"));
        message.setMessage(rankInfo.toString());

        gameOverDialog = new NotificationDialog(skin);
        gameOverDialog.setNotification(message);
        gameOverDialog.setOnCloseAction(() -> {
            returnToPreviousScreen();
        });

        // 确保弹窗显示在屏幕中央
        gameOverDialog.show(stage);
    }

    private void returnToPreviousScreen() {
        if (uiManager.getNetworkManager() != null && uiManager.getNetworkManager().isConnected()) {
            uiManager.setScreen(new RoomLobbyState(uiManager, uiManager.getNetworkManager()));
        } else {
            uiManager.setScreen(new MainMenuState(uiManager));
        }
    }

    private void handleInput() {
        // 观战者模式下禁用输入
        if (spectatorMode) return;

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

    private void handleTouchInput() {
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
                if (!isDownKeyPressed) {
                    gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                    isDownKeyPressed = true;
                    downKeyPressTime = 0;
                    lastSoftDropTime = 0;
                }
                break;
            default:
                break;
        }

        // 处理触屏软降的持续下降（长按下半部分）
        if (touchInput.isSoftDropping()) {
            // 首次触发已经在上面处理了，这里只处理后续的持续下降
            if (isDownKeyPressed) {
                downKeyPressTime += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
                if (downKeyPressTime >= initialDelay) {
                    lastSoftDropTime += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
                    if (lastSoftDropTime >= softDropInterval) {
                        gameStateManager.handleInput(MoveMessage.MoveType.DOWN);
                        lastSoftDropTime = 0;
                    }
                }
            }
        } else if (!isInputPressed(TetrisSettings.downKey(), TetrisSettings.downKey2())) {
            // 如果键盘也没有按下，则重置软降状态
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
        updatePreview();
    }

    /**
     * 玩家分数更新回调（PVP模式）
     */
    @Override
    public void onPlayerScoresUpdated(List<PlayerScoresMessage.PlayerScore> scores, int yourIndex) {
        this.playerScores = scores;
        this.myPlayerIndex = yourIndex;
        updateLeaderboard();
        updateOpponentPanel();
    }

    public void renderGame(ShapeRenderer shapeRenderer) {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);

        // 更新游戏板位置（基于UI实际位置）
        updateBoardPositionsFromUI();

        // 渲染自己的游戏板
        renderSelfBoard(shapeRenderer);

        // 两人对战时渲染对手的游戏板
        if (gameStateManager.getPlayerCount() <= 2) {
            renderOpponentBoard(shapeRenderer);
        }
    }

    private void renderSelfBoard(ShapeRenderer shapeRenderer) {
        GameLogic gameLogic = gameStateManager.getSharedManager().getLocalGameLogic();
        int[][] board = gameLogic.getBoard();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 绘制游戏板背景
        shapeRenderer.setColor(COLOR_PANEL);
        shapeRenderer.rect(selfBoardX - 4, selfBoardY - 4, GameLogic.BOARD_WIDTH * selfCellSize + 8, GameLogic.BOARD_HEIGHT * selfCellSize + 8);

        // 绘制游戏板内部
        shapeRenderer.setColor(COLOR_BG);
        shapeRenderer.rect(selfBoardX, selfBoardY, GameLogic.BOARD_WIDTH * selfCellSize, GameLogic.BOARD_HEIGHT * selfCellSize);

        // 绘制网格线
        shapeRenderer.setColor(COLOR_PANEL_BORDER);
        for (int x = 0; x <= GameLogic.BOARD_WIDTH; x++) {
            shapeRenderer.rect(selfBoardX + x * selfCellSize, selfBoardY, 1, GameLogic.BOARD_HEIGHT * selfCellSize);
        }
        for (int y = 0; y <= GameLogic.BOARD_HEIGHT; y++) {
            shapeRenderer.rect(selfBoardX, selfBoardY + y * selfCellSize, GameLogic.BOARD_WIDTH * selfCellSize, 1);
        }

        // 绘制已固定的方块
        for (int y = 0; y < GameLogic.BOARD_HEIGHT; y++) {
            for (int x = 0; x < GameLogic.BOARD_WIDTH; x++) {
                int cell = board[y][x];
                if (cell != 0) {
                    float screenY = selfBoardY + (GameLogic.BOARD_HEIGHT - 1 - y) * selfCellSize;
                    Color cellColor = getColorForCell(cell - 1);
                    shapeRenderer.setColor(cellColor);
                    shapeRenderer.rect(selfBoardX + x * selfCellSize + 1, screenY + 1, selfCellSize - 2, selfCellSize - 2);

                    // 绘制高光效果
                    shapeRenderer.setColor(cellColor.cpy().add(0.2f, 0.2f, 0.2f, 0));
                    shapeRenderer.rect(selfBoardX + x * selfCellSize + 1, screenY + selfCellSize - 4, selfCellSize - 2, 3);
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
                        float screenY = selfBoardY + (GameLogic.BOARD_HEIGHT - 1 - boardYPos) * selfCellSize;
                        shapeRenderer.setColor(pieceColor);
                        shapeRenderer.rect(selfBoardX + boardXPos * selfCellSize + 1, screenY + 1, selfCellSize - 2, selfCellSize - 2);

                        // 高光效果
                        shapeRenderer.setColor(pieceColor.cpy().add(0.2f, 0.2f, 0.2f, 0));
                        shapeRenderer.rect(selfBoardX + boardXPos * selfCellSize + 1, screenY + selfCellSize - 4, selfCellSize - 2, 3);
                    }
                }
            }
        }

        shapeRenderer.end();
    }

    private void renderOpponentBoard(ShapeRenderer shapeRenderer) {
        // 获取对手的游戏逻辑（从remoteGameLogics中获取）
        GameLogic[] remoteLogics = gameStateManager.getSharedManager().getRemoteGameLogics();
        if (remoteLogics == null || remoteLogics.length == 0) return;

        // 找到第一个非自己的对手
        GameLogic opponentLogic = null;
        for (int i = 0; i < remoteLogics.length; i++) {
            if (i != myPlayerIndex && remoteLogics[i] != null) {
                opponentLogic = remoteLogics[i];
                break;
            }
        }

        if (opponentLogic == null) return;

        int[][] board = opponentLogic.getBoard();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 绘制游戏板背景
        shapeRenderer.setColor(COLOR_PANEL);
        shapeRenderer.rect(opponentBoardX - 4, opponentBoardY - 4, GameLogic.BOARD_WIDTH * opponentCellSize + 8, GameLogic.BOARD_HEIGHT * opponentCellSize + 8);

        // 绘制游戏板内部
        shapeRenderer.setColor(COLOR_BG);
        shapeRenderer.rect(opponentBoardX, opponentBoardY, GameLogic.BOARD_WIDTH * opponentCellSize, GameLogic.BOARD_HEIGHT * opponentCellSize);

        // 绘制网格线
        shapeRenderer.setColor(COLOR_PANEL_BORDER);
        for (int x = 0; x <= GameLogic.BOARD_WIDTH; x++) {
            shapeRenderer.rect(opponentBoardX + x * opponentCellSize, opponentBoardY, 1, GameLogic.BOARD_HEIGHT * opponentCellSize);
        }
        for (int y = 0; y <= GameLogic.BOARD_HEIGHT; y++) {
            shapeRenderer.rect(opponentBoardX, opponentBoardY + y * opponentCellSize, GameLogic.BOARD_WIDTH * opponentCellSize, 1);
        }

        // 绘制已固定的方块
        for (int y = 0; y < GameLogic.BOARD_HEIGHT; y++) {
            for (int x = 0; x < GameLogic.BOARD_WIDTH; x++) {
                int cell = board[y][x];
                if (cell != 0) {
                    float screenY = opponentBoardY + (GameLogic.BOARD_HEIGHT - 1 - y) * opponentCellSize;
                    Color cellColor = getColorForCell(cell - 1);
                    shapeRenderer.setColor(cellColor);
                    shapeRenderer.rect(opponentBoardX + x * opponentCellSize + 1, screenY + 1, opponentCellSize - 2, opponentCellSize - 2);

                    // 绘制高光效果
                    shapeRenderer.setColor(cellColor.cpy().add(0.2f, 0.2f, 0.2f, 0));
                    shapeRenderer.rect(opponentBoardX + x * opponentCellSize + 1, screenY + opponentCellSize - 4, opponentCellSize - 2, 3);
                }
            }
        }

        // 绘制当前方块
        int currentPiece = opponentLogic.getCurrentPiece();
        int currentPieceX = opponentLogic.getCurrentPieceX();
        int currentPieceY = opponentLogic.getCurrentPieceY();
        int currentPieceRotation = opponentLogic.getCurrentPieceRotation();

        int[][] pieceShape = opponentLogic.getPieceShape(currentPiece, currentPieceRotation);
        Color pieceColor = getColorForCell(currentPiece);

        for (int y = 0; y < pieceShape.length; y++) {
            for (int x = 0; x < pieceShape[y].length; x++) {
                if (pieceShape[y][x] != 0) {
                    int boardXPos = currentPieceX + x;
                    int boardYPos = currentPieceY + y;
                    if (boardXPos >= 0 && boardXPos < GameLogic.BOARD_WIDTH && boardYPos >= 0 && boardYPos < GameLogic.BOARD_HEIGHT) {
                        float screenY = opponentBoardY + (GameLogic.BOARD_HEIGHT - 1 - boardYPos) * opponentCellSize;
                        shapeRenderer.setColor(pieceColor);
                        shapeRenderer.rect(opponentBoardX + boardXPos * opponentCellSize + 1, screenY + 1, opponentCellSize - 2, opponentCellSize - 2);

                        // 高光效果
                        shapeRenderer.setColor(pieceColor.cpy().add(0.2f, 0.2f, 0.2f, 0));
                        shapeRenderer.rect(opponentBoardX + boardXPos * opponentCellSize + 1, screenY + opponentCellSize - 4, opponentCellSize - 2, 3);
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

    private void calculateBoardPositions() {
        // 计算自己的游戏板单元格大小
        float selfBaseCellSize = 22f;
        selfCellSize = selfBaseCellSize * scale();
        float maxSelfCellSize = h(28f);
        if (selfCellSize > maxSelfCellSize) {
            selfCellSize = maxSelfCellSize;
        }

        // 计算对手的游戏板单元格大小
        float opponentBaseCellSize = 18f;
        opponentCellSize = opponentBaseCellSize * scale();
        float maxOpponentCellSize = h(24f);
        if (opponentCellSize > maxOpponentCellSize) {
            opponentCellSize = maxOpponentCellSize;
        }
    }

    private void updateBoardPositionsFromUI() {
        // 使用UI元素的实际位置计算游戏板位置
        // 需要将局部坐标转换为舞台坐标（屏幕坐标）
        if (selfBoardArea != null) {
            com.badlogic.gdx.math.Vector2 selfPos = selfBoardArea.localToStageCoordinates(new com.badlogic.gdx.math.Vector2(0, 0));
            selfBoardX = selfPos.x + (selfBoardArea.getWidth() - GameLogic.BOARD_WIDTH * selfCellSize) / 2;
            selfBoardY = selfPos.y + (selfBoardArea.getHeight() - GameLogic.BOARD_HEIGHT * selfCellSize) / 2;
        }

        if (opponentBoardArea != null && gameStateManager.getPlayerCount() <= 2) {
            com.badlogic.gdx.math.Vector2 opponentPos = opponentBoardArea.localToStageCoordinates(new com.badlogic.gdx.math.Vector2(0, 0));
            opponentBoardX = opponentPos.x + (opponentBoardArea.getWidth() - GameLogic.BOARD_WIDTH * opponentCellSize) / 2;
            opponentBoardY = opponentPos.y + (opponentBoardArea.getHeight() - GameLogic.BOARD_HEIGHT * opponentCellSize) / 2;
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        calculateBoardPositions();
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
