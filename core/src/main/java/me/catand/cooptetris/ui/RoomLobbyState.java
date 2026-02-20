package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.shared.message.CountdownMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.shared.tetris.GameMode;
import me.catand.cooptetris.util.LanguageManager;

/**
 * 房间大厅状态 - 现代化暗色游戏UI风格
 */
public class RoomLobbyState extends BaseUIState implements NetworkManager.NetworkListener {
    private Table mainTable;
    private Table playerListTable;
    private final NetworkManager networkManager;
    private Label statusLabel;
    private Label roomNameLabel;
    private Label playerCountLabel;
    private Label gameModeLabel;
    private SelectBox<String> gameModeSelectBox;
    private TextButton startGameButton;
    private TextButton leaveRoomButton;
    private BitmapFont titleFont;
    private BitmapFont smallFont;
    private final List<String> playerNames;
    private final List<String> chatMessages;
    private String roomName;
    private int maxPlayers;
    private boolean isHost;
    private float countdownTimer;
    private boolean isCountingDown;
    private Table chatTable;
    private ScrollPane chatScrollPane;
    private TextField chatInputField;
    private TextButton sendChatButton;
    private NotificationDialog currentNotificationDialog;
    private GameMode currentGameMode;
    private final NetworkManager.ConnectionType connectionType;

    // UI颜色配置
    private static final Color COLOR_PANEL = new Color(0.12f, 0.14f, 0.17f, 0.95f);
    private static final Color COLOR_PANEL_HIGHLIGHT = new Color(0.15f, 0.17f, 0.21f, 0.95f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);
    private static final Color COLOR_SECONDARY = new Color(0.8f, 0.3f, 0.9f, 1f);
    private static final Color COLOR_SUCCESS = new Color(0.3f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_WARNING = new Color(1f, 0.7f, 0.2f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color TEXT_MUTED = new Color(0.5f, 0.52f, 0.55f, 1f);

    public RoomLobbyState(UIManager uiManager, NetworkManager networkManager) {
        this(uiManager, networkManager, false);
    }

    public RoomLobbyState(UIManager uiManager, NetworkManager networkManager, boolean isLocalServerMode) {
        super(uiManager);
        this.networkManager = networkManager;
        this.playerNames = new ArrayList<>();
        this.chatMessages = new ArrayList<>();
        this.roomName = lang().get("unknown.room");
        this.maxPlayers = 4;
        this.isHost = false;
        this.countdownTimer = 0;
        this.isCountingDown = false;
        this.currentGameMode = GameMode.COOP;
        this.connectionType = isLocalServerMode ? NetworkManager.ConnectionType.LOCAL_SERVER : NetworkManager.ConnectionType.EXTERNAL_SERVER;
    }

    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    public void setRoomInfo(String roomName, int maxPlayers, boolean isHost) {
        this.roomName = roomName;
        this.maxPlayers = maxPlayers;
        this.isHost = isHost;
        if (roomNameLabel != null) {
            roomNameLabel.setText(roomName);
        }
        if (playerCountLabel != null) {
            updatePlayerCountLabel();
        }
        if (startGameButton != null) {
            startGameButton.setDisabled(!isHost);
            startGameButton.setColor(isHost ? COLOR_SUCCESS : TEXT_MUTED);
        }
    }

    public void updatePlayerList(List<String> players) {
        this.playerNames.clear();
        if (players != null) {
            this.playerNames.addAll(players);
        }
        if (playerListTable != null) {
            updatePlayerListUI();
        }
        if (playerCountLabel != null) {
            updatePlayerCountLabel();
        }
    }

    private void updatePlayerCountLabel() {
        playerCountLabel.setText(playerNames.size() + "/" + maxPlayers + " " + lang().get("players.label"));
    }

    @Override
    protected void createUI() {
        // 创建字体
        titleFont = Main.platform.getFont(fontSize(28), lang().get("room.lobby"), false, false);
        smallFont = Main.platform.getFont(fontSize(14), "Players", false, false);

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        mainTable.pad(h(30f));

        // 创建主面板
        Table contentPanel = createMainPanel();

        mainTable.add(contentPanel).expand().center();
        stage.addActor(mainTable);

        // 注册网络监听器
        if (networkManager != null) {
            networkManager.addListener(this);
            RoomMessage statusMessage = new RoomMessage(RoomMessage.RoomAction.STATUS);
            networkManager.sendMessage(statusMessage);
        }
    }

    private Table createMainPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(30f));

        // 标题区域
        Table headerTable = createHeader();
        panel.add(headerTable).fillX().padBottom(h(20f)).row();

        // 中间内容区域（玩家列表 + 聊天）
        Table contentArea = new Table();

        // 左侧：玩家列表
        Table leftPanel = createPlayerListPanel();
        contentArea.add(leftPanel).width(w(280f)).fillY().padRight(w(20f));

        // 右侧：聊天区域
        Table rightPanel = createChatPanel();
        contentArea.add(rightPanel).width(w(380f)).fillY();

        panel.add(contentArea).fill().expand().padBottom(h(20f)).row();

        // 底部按钮区域
        Table buttonArea = createButtonArea();
        panel.add(buttonArea).fillX();

        return panel;
    }

    private Table createHeader() {
        Table header = new Table();

        // 房间标题
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, COLOR_PRIMARY);
        roomNameLabel = new Label(roomName, titleStyle);
        roomNameLabel.setAlignment(Align.left);

        // 状态标签
        statusLabel = new Label(lang().get("waiting.players"), skin);
        statusLabel.setColor(COLOR_WARNING);
        statusLabel.setAlignment(Align.right);

        // 玩家数量
        playerCountLabel = new Label("0/" + maxPlayers + " " + lang().get("players.label"), skin);
        playerCountLabel.setColor(TEXT_MUTED);
        playerCountLabel.setAlignment(Align.right);

        // 游戏模式选择
        Table modeTable = new Table();
        gameModeLabel = new Label(lang().get("game.mode.label"), skin);
        gameModeLabel.setColor(TEXT_MUTED);

        gameModeSelectBox = new SelectBox<>(skin);
        Array<String> gameModeItems = new Array<>();
        gameModeItems.add(lang().get("game.mode.coop"));
        gameModeItems.add(lang().get("game.mode.pvp"));
        gameModeSelectBox.setItems(gameModeItems);
        gameModeSelectBox.setSelected(lang().get("game.mode.coop"));
        gameModeSelectBox.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                return true;
            }
            if (isHost && !isCountingDown) {
                String selected = gameModeSelectBox.getSelected();
                GameMode newMode = selected.equals(lang().get("game.mode.coop")) ? GameMode.COOP : GameMode.PVP;
                if (newMode != currentGameMode) {
                    setGameMode(newMode);
                }
            }
            return true;
        });
        gameModeSelectBox.setDisabled(!isHost);

        modeTable.add(gameModeLabel).padRight(w(10f));
        modeTable.add(gameModeSelectBox).width(w(120f));

        // 组装头部
        header.add(roomNameLabel).left().expandX();
        header.add(modeTable).padRight(w(20f));
        header.add(playerCountLabel).width(w(120f)).row();
        header.add(statusLabel).right().colspan(3).padTop(h(5f));

        return header;
    }

    private Table createPlayerListPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL_HIGHLIGHT));
        panel.pad(w(15f));

        // 标题
        Label.LabelStyle sectionTitleStyle = new Label.LabelStyle(smallFont, COLOR_SECONDARY);
        Label sectionTitle = new Label(lang().get("players.title"), sectionTitleStyle);
        panel.add(sectionTitle).left().padBottom(h(15f)).row();

        // 玩家列表
        playerListTable = new Table();
        playerListTable.top().left();

        ScrollPane scrollPane = new ScrollPane(playerListTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        panel.add(scrollPane).fill().expand();

        updatePlayerListUI();

        return panel;
    }

    private Table createChatPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL_HIGHLIGHT));
        panel.pad(w(15f));

        // 聊天标题
        Label.LabelStyle chatTitleStyle = new Label.LabelStyle(smallFont, COLOR_PRIMARY);
        Label chatTitle = new Label(lang().get("chat.label"), chatTitleStyle);
        panel.add(chatTitle).left().padBottom(h(10f)).row();

        // 聊天消息区域
        chatTable = new Table();
        chatTable.top().left();

        chatScrollPane = new ScrollPane(chatTable, skin);
        chatScrollPane.setFadeScrollBars(false);
        chatScrollPane.setScrollingDisabled(true, false);

        panel.add(chatScrollPane).height(h(200f)).fillX().padBottom(h(10f)).row();

        // 输入区域
        Table inputArea = new Table();

        chatInputField = new TextField("", skin);
        chatInputField.setMessageText(lang().get("type.message"));
        chatInputField.addListener(event -> {
            if (event instanceof InputEvent) {
                InputEvent inputEvent = (InputEvent) event;
                int keyCode = inputEvent.getKeyCode();
                if (inputEvent.getType() == InputEvent.Type.keyDown &&
                    (keyCode == com.badlogic.gdx.Input.Keys.ENTER || keyCode == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER)) {
                    sendChatMessage();
                }
            }
            return true;
        });

        sendChatButton = new TextButton(lang().get("send.button"), skin);
        stylePrimaryButton(sendChatButton);
        sendChatButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                sendChatMessage();
            }
            return true;
        });

        inputArea.add(chatInputField).fillX().expandX().height(h(40f)).padRight(w(10f));
        inputArea.add(sendChatButton).width(w(70f)).height(h(40f));

        panel.add(inputArea).fillX();

        initChatMessages();

        return panel;
    }

    private Table createButtonArea() {
        Table buttonArea = new Table();

        startGameButton = new TextButton(lang().get("start.game.button"), skin);
        startGameButton.setDisabled(!isHost);
        styleSuccessButton(startGameButton);
        if (!isHost) {
            startGameButton.setColor(TEXT_MUTED);
        }
        startGameButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                startGame();
            }
            return true;
        });

        leaveRoomButton = new TextButton(lang().get("leave.room.button"), skin);
        styleDangerButton(leaveRoomButton);
        leaveRoomButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                leaveRoom();
            }
            return true;
        });

        buttonArea.add(startGameButton).width(w(160f)).height(h(45f)).padRight(w(15f));
        buttonArea.add(leaveRoomButton).width(w(160f)).height(h(45f));

        return buttonArea;
    }

    private void updatePlayerListUI() {
        playerListTable.clear();

        if (playerNames.isEmpty()) {
            Label emptyLabel = new Label(lang().get("no.players"), skin);
            emptyLabel.setColor(TEXT_MUTED);
            playerListTable.add(emptyLabel).left().padBottom(h(8f)).row();
        } else {
            for (int i = 0; i < playerNames.size(); i++) {
                String playerName = playerNames.get(i);
                boolean isPlayerHost = (i == 0);

                Table playerRow = new Table();
                playerRow.padBottom(h(8f));

                // 玩家指示器（圆点）
                Label indicator = new Label(isPlayerHost ? "★" : "●", skin);
                indicator.setColor(isPlayerHost ? COLOR_WARNING : COLOR_SUCCESS);
                playerRow.add(indicator).width(w(20f)).left();

                // 玩家名称
                Label nameLabel = new Label(playerName + (isPlayerHost ? " " + lang().get("host.suffix") : ""), skin);
                nameLabel.setColor(COLOR_TEXT);
                playerRow.add(nameLabel).left().expandX();

                // 踢出按钮（仅房主可见）
                if (isHost && !isPlayerHost) {
                    TextButton kickButton = new TextButton(lang().get("kick.button"), skin);
                    kickButton.setColor(new Color(1f, 0.3f, 0.3f, 1f));
                    final String targetPlayerName = playerName;
                    kickButton.addListener(event -> {
                        if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                            kickPlayer(targetPlayerName);
                        }
                        return true;
                    });
                    playerRow.add(kickButton).width(w(50f)).height(h(28f));
                }

                playerListTable.add(playerRow).fillX().row();
            }
        }
    }

    private void initChatMessages() {
        if (chatTable != null) {
            chatTable.clear();

            if (chatMessages.isEmpty()) {
                Label emptyLabel = new Label("", skin);
                emptyLabel.setHeight(h(180f));
                chatTable.add(emptyLabel).left().row();
            } else {
                for (String msg : chatMessages) {
                    addChatMessageToUI(msg);
                }
            }

            chatScrollPane.layout();
            chatScrollPane.setScrollPercentY(1f);
        }
    }

    private void addChatMessageToUI(String message) {
        Label messageLabel = new Label(message, skin);
        messageLabel.setWrap(true);
        messageLabel.setColor(COLOR_TEXT);
        chatTable.add(messageLabel).left().width(w(340f)).padBottom(h(4f)).row();
    }

    public void addChatMessage(String message) {
        chatMessages.add(message);
        if (chatTable != null) {
            if (chatMessages.size() == 1) {
                chatTable.clear();
            }
            addChatMessageToUI(message);
            chatScrollPane.layout();
            chatScrollPane.setScrollPercentY(1f);
        }
    }

    // ==================== 按钮样式 ====================

    private void stylePrimaryButton(TextButton button) {
        button.setColor(COLOR_PRIMARY);
    }

    private void styleSuccessButton(TextButton button) {
        button.setColor(COLOR_SUCCESS);
    }

    private void styleDangerButton(TextButton button) {
        button.setColor(new Color(1f, 0.3f, 0.3f, 1f));
    }

    // ==================== 背景创建 ====================

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createPanelBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(texture);
    }

    // ==================== 功能方法 ====================

    private void startGame() {
        // 只有房主才能开始游戏
        if (!isHost) {
            return;
        }

        if (networkManager != null && networkManager.isConnected()) {
            // 发送开始游戏请求，服务器会广播倒计时
            networkManager.startGame();
        }
    }

    private void leaveRoom() {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.leaveRoom();
            if (connectionType == NetworkManager.ConnectionType.LOCAL_SERVER) {
                // 本地服务器模式：断开连接并停止服务器，返回主菜单
                networkManager.disconnect();
                if (uiManager.getLocalServerManager() != null && uiManager.getLocalServerManager().isRunning()) {
                    uiManager.getLocalServerManager().stopServer();
                }
                // 使用setScreen返回主菜单，因为ServerConnectionState已经被替换
                uiManager.setScreen(new MainMenuState(uiManager));
            } else {
                // 外部服务器模式：返回房间列表
                uiManager.popState();
            }
        }
    }

    private void kickPlayer(String playerName) {
        if (networkManager != null && networkManager.isConnected() && isHost) {
            networkManager.kickPlayer(playerName);
        }
    }

    private void sendChatMessage() {
        String message = chatInputField.getText();
        if (!message.isEmpty() && networkManager != null && networkManager.isConnected()) {
            networkManager.sendChatMessage(message);
            chatInputField.setText("");
        }
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
        if (startGameButton != null) {
            startGameButton.setDisabled(!isHost);
            startGameButton.setColor(isHost ? COLOR_SUCCESS : TEXT_MUTED);
        }
        if (gameModeSelectBox != null) {
            gameModeSelectBox.setDisabled(!isHost);
        }
        if (playerListTable != null) {
            updatePlayerListUI();
        }
    }

    public void setGameMode(GameMode mode) {
        this.currentGameMode = mode;
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.setGameMode(mode);
        }
    }

    public void updateGameMode(GameMode mode) {
        this.currentGameMode = mode;
        if (gameModeSelectBox != null) {
            String modeText = mode == GameMode.COOP ? lang().get("game.mode.coop") : lang().get("game.mode.pvp");
            gameModeSelectBox.setSelected(modeText);
        }
    }

    // ==================== 生命周期方法 ====================

    @Override
    protected void clearUI() {
        if (networkManager != null) {
            networkManager.removeListener(this);
        }
        if (mainTable != null) {
            mainTable.remove();
            mainTable = null;
        }
    }

    @Override
    public void update(float delta) {
        if (playerCountLabel != null) {
            updatePlayerCountLabel();
        }

        if (isCountingDown) {
            // 倒计时由服务器同步，这里只更新UI显示
            if (startGameButton != null) {
                startGameButton.setText(lang().get("starting.countdown").replace("%d", String.valueOf((int) countdownTimer)));
            }
            if (statusLabel != null) {
                statusLabel.setText(lang().get("starting.game"));
                statusLabel.setColor(COLOR_PRIMARY);
            }
        } else {
            if (statusLabel != null) {
                if (playerNames.isEmpty()) {
                    statusLabel.setText(lang().get("waiting.players"));
                    statusLabel.setColor(COLOR_WARNING);
                    if (startGameButton != null) {
                        startGameButton.setDisabled(true);
                    }
                } else {
                    statusLabel.setText(lang().get("ready.start") + (isHost ? lang().get("click.start") : ""));
                    statusLabel.setColor(COLOR_SUCCESS);
                    if (startGameButton != null && isHost) {
                        startGameButton.setDisabled(false);
                        startGameButton.setText(lang().get("start.game.button"));
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
        if (smallFont != null) {
            smallFont.dispose();
            smallFont = null;
        }
    }

    // ==================== 网络回调 ====================

    @Override
    public void onConnectResponse(boolean success, String message, String clientId) {}

    @Override
    public void onRoomResponse(RoomMessage message) {
        if (message.getAction() == RoomMessage.RoomAction.CHAT) {
            if (message.getMessage() != null) {
                addChatMessage(message.getMessage());
            }
        } else if (message.getAction() == RoomMessage.RoomAction.STATUS) {
            if (message.getPlayers() != null) {
                updatePlayerList(message.getPlayers());
            }
            if (message.isHost() != this.isHost) {
                setHost(message.isHost());
            }
            if (message.getGameMode() != null) {
                updateGameMode(message.getGameMode());
            }
        } else if (message.getAction() == RoomMessage.RoomAction.SET_GAME_MODE) {
            if (message.getGameMode() != null && message.isSuccess()) {
                updateGameMode(message.getGameMode());
            }
        }
    }

    @Override
    public void onCountdownUpdate(CountdownMessage message) {
        if (message.isStarting()) {
            // 开始倒计时
            isCountingDown = true;
            countdownTimer = message.getCountdownSeconds();
            if (startGameButton != null) {
                startGameButton.setDisabled(true);
            }
        } else {
            // 倒计时结束
            isCountingDown = false;
            countdownTimer = 0;
        }
    }

    @Override
    public void onGameStart(me.catand.cooptetris.shared.message.GameStartMessage message) {
        // 重置倒计时状态
        isCountingDown = false;
        countdownTimer = 0;

        if (uiManager != null && uiManager.gameStateManager != null) {
            // 根据游戏模式启动不同的游戏
            if (message.getGameMode() == GameMode.COOP) {
                // 合作模式
                uiManager.gameStateManager.startCoopMode(message.getPlayerCount(), message.getYourIndex(), message.getSeed());
                CoopGameState coopGameState = new CoopGameState(uiManager, uiManager.gameStateManager);
                uiManager.setScreen(coopGameState);
            } else if (message.getGameMode() == GameMode.PVP) {
                // PVP模式
                uiManager.gameStateManager.startMultiplayer(message.getPlayerCount(), message.getYourIndex(), message.getSeed());
                PVPGameState pvpGameState = new PVPGameState(uiManager, uiManager.gameStateManager);
                uiManager.setScreen(pvpGameState);
            } else {
                // 单人模式（兼容旧版本）
                uiManager.gameStateManager.startMultiplayer(message.getPlayerCount(), message.getYourIndex(), message.getSeed());
                GameState gameState = new GameState(uiManager, uiManager.gameStateManager);
                uiManager.setScreen(gameState);
            }
        }
    }

    @Override
    public void onGameStateUpdate(me.catand.cooptetris.shared.message.GameStateMessage message) {}

    @Override
    public void onDisconnected() {
        uiManager.popState();
    }

    @Override
    public void onNotification(NotificationMessage message) {
        if (currentNotificationDialog != null && currentNotificationDialog.isVisible()) {
            currentNotificationDialog.hide();
        }

        currentNotificationDialog = new NotificationDialog(skin);
        currentNotificationDialog.setNotification(message);

        switch (message.getNotificationType()) {
            case KICKED:
                currentNotificationDialog.setOnCloseAction(() -> {
                    currentNotificationDialog = null;
                    uiManager.popState();
                });
                break;
            case DISCONNECTED:
                currentNotificationDialog.setOnCloseAction(() -> {
                    currentNotificationDialog = null;
                    uiManager.popState();
                });
                break;
            case BANNED:
                currentNotificationDialog.setOnCloseAction(() -> {
                    currentNotificationDialog = null;
                    if (networkManager != null) {
                        networkManager.disconnect();
                    }
                    uiManager.popState();
                });
                break;
            default:
                currentNotificationDialog.setOnCloseAction(() -> {
                    currentNotificationDialog = null;
                });
                break;
        }

        currentNotificationDialog.show(stage);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (currentNotificationDialog != null && currentNotificationDialog.isVisible()) {
            currentNotificationDialog.onResize(stage);
        }
    }
}
