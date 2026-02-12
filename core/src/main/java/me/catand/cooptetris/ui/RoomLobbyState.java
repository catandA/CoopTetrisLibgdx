package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;

import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.util.LanguageManager;

public class RoomLobbyState implements UIState, NetworkManager.NetworkListener {
    private Stage stage;
    private Skin skin;
    private Table mainTable;
    private Table playerListTable;
    private final UIManager uiManager;
    private final NetworkManager networkManager;
    private Label statusLabel;
    private Label roomNameLabel;
    private Label playerCountLabel;
    private TextButton startGameButton;
    private TextButton leaveRoomButton;
    private BitmapFont titleFont;
    private List<String> playerNames;
    private List<String> chatMessages;
    private String roomName;
    private int maxPlayers;
    private boolean isHost;
    private float countdownTimer;
    private boolean isCountingDown;
    private Table chatTable;
    private ScrollPane chatScrollPane;
    private TextField chatInputField;
    private TextButton sendChatButton;

    public RoomLobbyState(UIManager uiManager, NetworkManager networkManager) {
        this.uiManager = uiManager;
        this.networkManager = networkManager;
        this.playerNames = new ArrayList<>();
        this.chatMessages = new ArrayList<>();
        this.roomName = lang().get("unknown.room");
        this.maxPlayers = 4;
        this.isHost = false;
        this.countdownTimer = 0;
        this.isCountingDown = false;
    }

    // 辅助方法，获取LanguageManager实例
    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    public void setRoomInfo(String roomName, int maxPlayers, boolean isHost) {
        this.roomName = roomName;
        this.maxPlayers = maxPlayers;
        this.isHost = isHost;
        // 更新UI显示
        if (roomNameLabel != null) {
            roomNameLabel.setText(lang().get("room.label") + " " + roomName);
        }
        if (playerCountLabel != null) {
            playerCountLabel.setText(lang().get("players.label") + " " + playerNames.size() + "/" + maxPlayers);
        }
        if (startGameButton != null) {
            startGameButton.setVisible(isHost);
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
        // 更新玩家数量显示
        if (playerCountLabel != null) {
            playerCountLabel.setText(lang().get("players.label") + " " + playerNames.size() + "/" + maxPlayers);
        }
    }

    @Override
    public void show(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        mainTable.pad(20f);

        LanguageManager lang = LanguageManager.getInstance();

        // 生成适当大小的标题字体
        titleFont = uiManager.generateFont(24);
        Label title;
        if (titleFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(titleFont, skin.getColor("font"));
            title = new Label(lang().get("room.lobby"), labelStyle);
        } else {
            // 如果字体生成失败，使用默认字体
            title = new Label(lang().get("room.lobby"), skin);
        }
        title.setAlignment(Align.center);

        // 状态标签
        statusLabel = new Label(lang().get("waiting.players"), skin);
        statusLabel.setColor(Color.YELLOW);

        // 房间信息
        roomNameLabel = new Label(lang().get("room.label") + " " + roomName, skin);
        playerCountLabel = new Label(lang().get("players.label") + " 0/" + maxPlayers, skin);

        // 玩家列表
        playerListTable = new Table();
        playerListTable.defaults().padBottom(5f);
        playerListTable.add(new Label(lang().get("players.title"), skin)).left().padBottom(10f).row();
        updatePlayerListUI();

        // 聊天区域
        Table chatContainer = new Table();
        chatContainer.defaults().width(400f).padBottom(10f);

        chatTable = new Table();
        chatTable.defaults().left().padBottom(5f).width(380f);

        // 创建一个带有滚动条的滚动窗
        chatScrollPane = new ScrollPane(chatTable, skin);
        chatScrollPane.setHeight(200f);
        chatScrollPane.setWidth(392f);
        chatScrollPane.setScrollingDisabled(false, true);
        chatScrollPane.setFlickScroll(true);
        chatScrollPane.setSmoothScrolling(true);

        // 为滚动窗添加外边框
        Table borderTable = new Table(skin);
        borderTable.setWidth(400f);
        borderTable.setHeight(200f);
        // 添加聊天滚动窗
        borderTable.add(chatScrollPane).expand().fill().pad(4f);
        // 尝试使用skin中的默认背景作为边框
        try {
            borderTable.setBackground(skin.getDrawable("default"));
        } catch (Exception e) {
            // 如果默认背景不存在，忽略错误
        }

        Table chatInputContainer = new Table();
        chatInputContainer.defaults().padRight(5f);

        chatInputField = new TextField("", skin);
        chatInputField.setWidth(320f);
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
        sendChatButton.setWidth(70f);
        sendChatButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                sendChatMessage();
            }
            return true;
        });

        chatInputContainer.add(chatInputField).fillX().expandX();
        chatInputContainer.add(sendChatButton).width(70f);

        chatContainer.add(borderTable).row();
        chatContainer.add(chatInputContainer).row();

        // 初始化聊天消息
        initChatMessages();

        // 按钮区域
        Table buttonTable = new Table();
        buttonTable.defaults().width(200f).height(50f).padBottom(10f);

        startGameButton = new TextButton(lang().get("start.game.button"), skin);
        startGameButton.setVisible(isHost); // 只有房主可以开始游戏
        startGameButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                startGame();
            }
            return true;
        });

        leaveRoomButton = new TextButton(lang().get("leave.room.button"), skin);
        leaveRoomButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                leaveRoom();
            }
            return true;
        });

        buttonTable.add(startGameButton).row();
        buttonTable.add(leaveRoomButton).row();

        // 组装主表格
        mainTable.add(title).colspan(2).padBottom(20f).row();
        mainTable.add(statusLabel).colspan(2).padBottom(10f).row();
        mainTable.add(roomNameLabel).colspan(2).padBottom(5f).row();
        mainTable.add(playerCountLabel).colspan(2).padBottom(20f).row();
        mainTable.add(playerListTable).width(200f).padRight(20f).left();
        mainTable.add(chatContainer).fillX().expandX().right();
        mainTable.row();
        mainTable.add(buttonTable).center().colspan(2).padTop(20f).row();

        stage.addActor(mainTable);

        // 注册为NetworkManager的监听器
        if (networkManager != null) {
            networkManager.addListener(this);
        }
    }

    @Override
    public void hide() {
        // 从NetworkManager的监听器中移除
        if (networkManager != null) {
            networkManager.removeListener(this);
        }
        mainTable.remove();
    }

    @Override
    public void update(float delta) {
        // 更新玩家数量显示
        if (playerCountLabel != null) {
            playerCountLabel.setText(lang().get("players.label") + " " + playerNames.size() + "/" + maxPlayers);
        }

        // 处理倒计时
        if (isCountingDown) {
            countdownTimer -= delta;
            if (countdownTimer > 0) {
                if (startGameButton != null) {
                    startGameButton.setText(lang().get("starting.countdown").replace("%d", String.valueOf((int) countdownTimer)));
                }
            } else {
                isCountingDown = false;
                if (startGameButton != null) {
                    startGameButton.setText(lang().get("start.game.button"));
                }
            }
        } else {
            // 更新状态信息
            if (statusLabel != null) {
                if (playerNames.isEmpty()) {
                    statusLabel.setText(lang().get("waiting.players"));
                    statusLabel.setColor(Color.YELLOW);
                    if (startGameButton != null) {
                        startGameButton.setDisabled(true);
                    }
                } else {
                    statusLabel.setText(lang().get("ready.start") + (isHost ? lang().get("click.start") : ""));
                    statusLabel.setColor(Color.GREEN);
                    if (startGameButton != null && isHost) {
                        startGameButton.setDisabled(false);
                    }
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // 自动处理大小调整
    }

    @Override
    public void dispose() {
        // 释放生成的标题字体
        if (titleFont != null) {
            titleFont.dispose();
        }
    }

    /**
     * 更新玩家列表UI
     */
    private void updatePlayerListUI() {
        // 清空玩家列表表格
        playerListTable.clear();
        playerListTable.add(new Label(lang().get("players.title"), skin)).left().padBottom(10f).row();

        if (playerNames.isEmpty()) {
            playerListTable.add(new Label(lang().get("no.players"), skin)).left().row();
        } else {
            for (int i = 0; i < playerNames.size(); i++) {
                String playerName = playerNames.get(i);
                // 只在当前用户是房主时显示房主标记
                String displayName = playerName + (isHost && i == 0 ? lang().get("host.suffix") : "");
                Label playerLabel = new Label(displayName, skin);

                if (isHost && i > 0) { // 房主可以踢出其他玩家，但不能踢自己
                    Table playerRow = new Table();
                    playerRow.defaults().padRight(10f);
                    playerRow.add(playerLabel).left().expandX();

                    TextButton kickButton = new TextButton(lang().get("kick.button"), skin);
                    kickButton.setWidth(60f);
                    final String targetPlayerName = playerName;
                    kickButton.addListener(event -> {
                        if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                            kickPlayer(targetPlayerName);
                        }
                        return true;
                    });

                    playerRow.add(kickButton);
                    playerListTable.add(playerRow).left().row();
                } else {
                    playerListTable.add(playerLabel).left().row();
                }
            }
        }
    }

    /**
     * 开始游戏
     */
    private void startGame() {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.startGame();
            statusLabel.setText(lang().get("starting.game"));
            statusLabel.setColor(Color.BLUE);
            startGameButton.setDisabled(true);
            isCountingDown = true;
            countdownTimer = 3;
        }
    }

    /**
     * 离开房间
     */
    private void leaveRoom() {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.leaveRoom();
            uiManager.popState();
        }
    }

    /**
     * 踢出玩家
     */
    private void kickPlayer(String playerName) {
        if (networkManager != null && networkManager.isConnected() && isHost) {
            networkManager.kickPlayer(playerName);
        }
    }

    /**
     * 发送聊天消息
     */
    private void sendChatMessage() {
        String message = chatInputField.getText();
        if (!message.isEmpty() && networkManager != null && networkManager.isConnected()) {
            networkManager.sendChatMessage(message);
            chatInputField.setText("");
        }
    }

    /**
     * 初始化聊天消息
     */
    private void initChatMessages() {
        if (chatTable != null) {
            // 清空并添加标题行
            chatTable.clear();
            chatTable.add(new Label(lang().get("chat.label"), skin)).left().padBottom(10f).row();

            // 添加所有聊天消息
            if (chatMessages.isEmpty()) {
                // 添加空消息占位符，确保聊天框保持最小高度
                Label emptyLabel = new Label("", skin);
                emptyLabel.setHeight(180f);
                chatTable.add(emptyLabel).left().row();
            } else {
                for (String msg : chatMessages) {
                    Label messageLabel = new Label(msg, skin);
                    messageLabel.setWrap(true);
                    chatTable.add(messageLabel).left().row();
                }
            }

            // 滚动到底部
            chatScrollPane.layout();
            chatScrollPane.setScrollPercentY(1f);
        }
    }

    /**
     * 添加聊天消息
     */
    public void addChatMessage(String message) {
        chatMessages.add(message);
        if (chatTable != null) {
            // 移除空消息占位符（如果存在）
            if (chatMessages.size() == 1 && chatTable.getChildren().size > 1) {
                chatTable.clear();
                chatTable.add(new Label(lang().get("chat.label"), skin)).left().padBottom(10f).row();
            }

            // 添加新消息
            Label messageLabel = new Label(message, skin);
            messageLabel.setWrap(true);
            chatTable.add(messageLabel).left().row();

            // 滚动到底部
            chatScrollPane.layout();
            chatScrollPane.setScrollPercentY(1f);
        }
    }

    /**
     * 设置是否为房主
     */
    public void setHost(boolean isHost) {
        this.isHost = isHost;
        if (startGameButton != null) {
            startGameButton.setVisible(isHost);
        }
        if (playerListTable != null) {
            updatePlayerListUI();
        }
    }

    @Override
    public void onConnectResponse(boolean success, String message, String clientId) {
        // 不需要处理
    }

    @Override
    public void onRoomResponse(RoomMessage message) {
        if (message.getAction() == RoomMessage.RoomAction.CHAT) {
            // 处理聊天消息
            if (message.getMessage() != null) {
                addChatMessage(message.getMessage());
            }
        } else if (message.getAction() == RoomMessage.RoomAction.STATUS) {
            // 处理房间状态更新
            if (message.getPlayers() != null) {
                updatePlayerList(message.getPlayers());
            }
        }
    }

    @Override
    public void onGameStart(me.catand.cooptetris.shared.message.GameStartMessage message) {
        // 处理游戏开始消息
        if (uiManager != null && uiManager.gameStateManager != null) {
            // 启动多人游戏
            uiManager.gameStateManager.startMultiplayer(message.getPlayerCount(), message.getYourIndex());
            // 创建游戏状态并切换到游戏界面
            GameState gameState = new GameState(uiManager, uiManager.gameStateManager);
            uiManager.setScreen(gameState);
        }
    }

    @Override
    public void onGameStateUpdate(me.catand.cooptetris.shared.message.GameStateMessage message) {
        // 不需要处理
    }

    @Override
    public void onDisconnected() {
        // 处理断开连接
        uiManager.popState();
    }
}
