package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.network.LocalServerManager;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 联机菜单状态 - 使用新的简化缩放接口
 */
public class OnlineMenuState extends BaseUIState implements NetworkManager.NetworkListener {
    private Table mainTable;
    private Table connectionTable;
    private Table roomListTable;
    private ScrollPane roomListScrollPane;
    private TextField hostField;
    private TextField portField;
    private TextField playerNameField;
    private Label statusLabel;
    private BitmapFont titleFont;
    private final List<RoomMessage.RoomInfo> availableRooms;
    private TextButton selectedRoomButton;
    private ConnectionState currentState;

    public enum ConnectionState {
        INITIAL,       // 初始状态：显示连接设置和创建房间选项
        CONNECTED_TO_SERVER,  // 已连接到服务端：显示房间列表
        CONNECTED_TO_ROOM,    // 已连接到房间：准备进入游戏
        ERROR          // 错误状态：显示错误信息
    }

    public OnlineMenuState(UIManager uiManager) {
        super(uiManager);
        this.availableRooms = new ArrayList<>();
        this.currentState = ConnectionState.INITIAL;
    }

    @Override
    protected void createUI() {
        mainTable = new Table();
        mainTable.setPosition(offsetX(), offsetY());
        mainTable.setSize(displayWidth(), displayHeight());
        mainTable.center();
        mainTable.pad(h(20f));

        LanguageManager lang = LanguageManager.getInstance();

        // 生成适当大小的标题字体，考虑缩放比例
        titleFont = Main.platform.getFont(fontSize(24), lang.get("online.mode.title"), false, false);
        Label title;
        if (titleFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(titleFont, skin.getColor("font"));
            title = new Label(lang.get("online.mode.title"), labelStyle);
        } else {
            // 如果字体生成失败，使用默认字体
            title = new Label(lang.get("online.mode.title"), skin);
        }
        title.setAlignment(Align.center);

        // 状态标签
        statusLabel = new Label(lang.get("connection.status") + ": " + lang.get("disconnected"), skin);
        statusLabel.setColor(Color.RED);


        // 连接设置区域
        connectionTable = new Table();
        connectionTable.defaults().padBottom(h(10f));

        Label hostLabel = new Label(lang.get("host"), skin);
        hostField = new TextField("localhost", skin);

        Label portLabel = new Label(lang.get("port"), skin);
        portField = new TextField("8080", skin);

        Label playerNameLabel = new Label(lang.get("player.name"), skin);
        // 从TetrisSettings获取保存的玩家名称
        String savedPlayerName = TetrisSettings.playerName();
        playerNameField = new TextField(savedPlayerName, skin);

        connectionTable.add(hostLabel).right().padRight(w(10f));
        connectionTable.add(hostField).width(w(200f)).height(h(40f)).row();
        connectionTable.add(portLabel).right().padRight(w(10f));
        connectionTable.add(portField).width(w(200f)).height(h(40f)).row();
        connectionTable.add(playerNameLabel).right().padRight(w(10f));
        connectionTable.add(playerNameField).width(w(200f)).height(h(40f)).row();


        // 房间列表区域
        roomListTable = new Table();
        roomListTable.defaults().width(w(300f)).padBottom(h(5f));
        roomListTable.add(new Label(lang.get("no.rooms.available"), skin)).center().row();

        roomListScrollPane = new ScrollPane(roomListTable, skin);
        roomListScrollPane.setFadeScrollBars(false);
        roomListScrollPane.setScrollBarPositions(false, true);
        roomListScrollPane.setVisible(false); // 初始隐藏

        // 按钮区域
        Table buttonTable = new Table();
        buttonTable.defaults().width(w(200f)).height(h(50f)).padBottom(h(10f));

        TextButton connectServerButton = new TextButton(lang.get("connect"), skin);
        connectServerButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                connectToServer();
            }
            return true;
        });

        TextButton createRoomButton = new TextButton(lang.get("create.room"), skin);
        createRoomButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                createRoom();
            }
            return true;
        });

        TextButton refreshRoomsButton = new TextButton(lang.get("refresh.rooms"), skin);
        refreshRoomsButton.setVisible(false); // 初始隐藏
        refreshRoomsButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                refreshRoomList();
            }
            return true;
        });

        TextButton joinSelectedRoomButton = new TextButton(lang.get("join.selected.room"), skin);
        joinSelectedRoomButton.setVisible(false); // 初始隐藏
        joinSelectedRoomButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                joinSelectedRoom();
            }
            return true;
        });


        TextButton backButton = new TextButton(lang.get("back"), skin);
        backButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                uiManager.popState();
            }
            return true;
        });

        buttonTable.add(connectServerButton).row();
        buttonTable.add(createRoomButton).row();
        buttonTable.add(refreshRoomsButton).row();
        buttonTable.add(joinSelectedRoomButton).row();
        buttonTable.add(backButton).row();

        // 组装主表格
        mainTable.add(title).colspan(2).padBottom(h(20f)).row();
        mainTable.add(statusLabel).colspan(2).padBottom(h(10f)).row();
        mainTable.add(connectionTable).left().padRight(w(20f));
        mainTable.add(buttonTable).left().row();
        mainTable.add(roomListScrollPane).colspan(2).height(h(200f)).padTop(h(20f)).row();

        // 将当前实例添加为NetworkManager的监听器
        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager != null) {
            networkManager.addListener(this);
        }

        stage.addActor(mainTable);
    }

    @Override
    protected void clearUI() {
        // 将当前实例从NetworkManager的监听器中移除
        NetworkManager networkManager = uiManager.getNetworkManager();
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
        // 更新状态显示
        updateStatus();
    }

    @Override
    public void dispose() {
        // 释放生成的标题字体
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
    }


    /**
     * 连接到服务器
     */
    private void connectToServer() {
        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager == null) {
            setStatus(ConnectionState.ERROR, lang().get("network.manager.not.initialized"));
            return;
        }

        String host = hostField.getText();
        int port;
        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            setStatus(ConnectionState.ERROR, lang().get("invalid.port.number"));
            return;
        }
        String playerName = playerNameField.getText();

        if (playerName.isEmpty()) {
            setStatus(ConnectionState.ERROR, lang().get("player.name.cannot.be.empty"));
            return;
        }

        // 保存玩家名称到TetrisSettings
        TetrisSettings.playerName(playerName);

        setStatus(ConnectionState.INITIAL, lang().get("connecting.to.server"));

        if (networkManager.connect(host, port, playerName)) {
            setStatus(ConnectionState.CONNECTED_TO_SERVER, lang().get("connected.to.server").replace("%s", host + ":" + port));
            updateUIForState(ConnectionState.CONNECTED_TO_SERVER);
            // 自动获取房间列表
            refreshRoomList();
        } else {
            setStatus(ConnectionState.ERROR, lang().get("failed.to.connect.to.server").replace("%s", host + ":" + port));
        }
    }

    /**
     * 启动本地服务器
     */
    private int startLocalServer(int startPort) {
        try {
            // 直接使用UIManager的getLocalServerManager方法获取实例
            LocalServerManager localServerManager = uiManager.getLocalServerManager();

            if (localServerManager != null) {
                // 如果服务器已经在运行，先停止它
                if (localServerManager.isRunning()) {
                    localServerManager.stopServer();
                    // 等待服务器完全停止
                    Thread.sleep(500);
                }
                // 启动服务器（尝试多个端口）
                int actualPort = localServerManager.startServer(startPort);
                System.out.println("Local server started on port: " + actualPort);
                return actualPort;
            } else {
                System.err.println("LocalServerManager not found!");
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 创建房间
     */
    private void createRoom() {
        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager == null) {
            setStatus(ConnectionState.ERROR, lang().get("network.manager.not.initialized"));
            return;
        }

        // 如果未连接到服务器，启动本地服务器
        if (!networkManager.isConnected()) {
            setStatus(ConnectionState.INITIAL, lang().get("starting.local.server"));
            // 使用不常见的默认端口56148
            int actualPort = startLocalServer(56148);
            if (actualPort == -1) {
                setStatus(ConnectionState.ERROR, lang().get("failed.to.start.local.server"));
                return;
            }

            // 连接到本地服务器
            String playerName = playerNameField.getText();
            if (playerName.isEmpty()) {
                playerName = lang().get("default.player.name") + (int) (Math.random() * 1000);
            }

            // 保存玩家名称到TetrisSettings
            TetrisSettings.playerName(playerName);

            if (!networkManager.connect("localhost", actualPort, playerName)) {
                setStatus(ConnectionState.ERROR, lang().get("failed.to.connect.to.local.server"));
                return;
            }

            setStatus(ConnectionState.CONNECTED_TO_SERVER, lang().get("connected.to.local.server"));
            System.out.println("Connected to local server on port: " + actualPort);
        }

        // 创建房间
        String roomName = lang().get("default.room.name") + (int) (Math.random() * 1000);
        setStatus(ConnectionState.CONNECTED_TO_SERVER, lang().get("creating.room.short").replace("%s", roomName));
        networkManager.createRoom(roomName);
    }

    /**
     * 刷新房间列表
     */
    private void refreshRoomList() {
        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager == null || !networkManager.isConnected()) {
            setStatus(ConnectionState.ERROR, lang().get("not.connected.to.server"));
            return;
        }

        setStatus(ConnectionState.CONNECTED_TO_SERVER, lang().get("refreshing.room.list"));
        networkManager.listRooms();
    }

    /**
     * 加入选中的房间
     */
    private void joinSelectedRoom() {
        if (selectedRoomButton == null) {
            setStatus(ConnectionState.ERROR, lang().get("no.room.selected.short"));
            return;
        }

        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager == null || !networkManager.isConnected()) {
            setStatus(ConnectionState.ERROR, lang().get("not.connected.to.server"));
            return;
        }

        String roomId = selectedRoomButton.getUserObject().toString();
        setStatus(ConnectionState.CONNECTED_TO_SERVER, lang().get("joining.room.short"));
        networkManager.joinRoom(roomId);
    }


    /**
     * 更新状态
     */
    private void setStatus(ConnectionState state, String message) {
        this.currentState = state;
        statusLabel.setText(message);

        // 根据状态设置颜色
        switch (state) {
            case INITIAL:
                statusLabel.setColor(Color.WHITE);
                break;
            case CONNECTED_TO_SERVER:
                statusLabel.setColor(Color.GREEN);
                break;
            case CONNECTED_TO_ROOM:
                statusLabel.setColor(Color.BLUE);
                break;
            case ERROR:
                statusLabel.setColor(Color.RED);
                break;
        }
    }

    /**
     * 根据当前状态更新UI
     */
    private void updateUIForState(ConnectionState state) {
        // 获取所有按钮
        TextButton connectServerButton = null;
        TextButton createRoomButton = null;
        TextButton refreshRoomsButton = null;
        TextButton joinSelectedRoomButton = null;

        // 遍历按钮表格中的所有子元素
        for (int i = 0; i < mainTable.getChildren().size; i++) {
            if (mainTable.getChildren().get(i) instanceof Table) {
                Table buttonTable = (Table) mainTable.getChildren().get(i);
                for (int j = 0; j < buttonTable.getChildren().size; j++) {
                    if (buttonTable.getChildren().get(j) instanceof TextButton) {
                        TextButton button = (TextButton) buttonTable.getChildren().get(j);
                        if (button.getText().toString().equals(lang().get("connect"))) {
                            connectServerButton = button;
                        } else if (button.getText().toString().equals(lang().get("create.room"))) {
                            createRoomButton = button;
                        } else if (button.getText().toString().equals(lang().get("refresh.rooms"))) {
                            refreshRoomsButton = button;
                        } else if (button.getText().toString().equals(lang().get("join.selected.room"))) {
                            joinSelectedRoomButton = button;
                        }
                    }
                }
            }
        }

        // 根据状态显示/隐藏UI元素
        switch (state) {
            case INITIAL:
                connectionTable.setVisible(true);
                roomListScrollPane.setVisible(false);
                if (connectServerButton != null) connectServerButton.setVisible(true);
                if (createRoomButton != null) createRoomButton.setVisible(true); // 始终显示创建房间按钮
                if (refreshRoomsButton != null) refreshRoomsButton.setVisible(false);
                if (joinSelectedRoomButton != null) joinSelectedRoomButton.setVisible(false);
                break;
            case CONNECTED_TO_SERVER:
                connectionTable.setVisible(true);
                roomListScrollPane.setVisible(true);
                if (connectServerButton != null) connectServerButton.setVisible(false);
                if (createRoomButton != null) createRoomButton.setVisible(true);
                if (refreshRoomsButton != null) refreshRoomsButton.setVisible(true);
                if (joinSelectedRoomButton != null) joinSelectedRoomButton.setVisible(true);
                break;
            case CONNECTED_TO_ROOM:
                // 准备进入游戏
                break;
            case ERROR:
                // 保持当前UI状态，只显示错误信息
                break;
        }
    }

    /**
     * 更新连接状态
     */
    private void updateStatus() {
        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager != null) {
            if (networkManager.isConnected()) {
                if (currentState != ConnectionState.CONNECTED_TO_SERVER) {
                    setStatus(ConnectionState.CONNECTED_TO_SERVER, lang().get("connected.to.server.simple"));
                    updateUIForState(ConnectionState.CONNECTED_TO_SERVER);
                }
            } else {
                if (currentState != ConnectionState.INITIAL && currentState != ConnectionState.ERROR) {
                    setStatus(ConnectionState.INITIAL, lang().get("disconnected.from.server"));
                    updateUIForState(ConnectionState.INITIAL);
                }
            }
        }
    }

    /**
     * 更新房间列表
     */
    public void updateRoomList(List<RoomMessage.RoomInfo> rooms) {
        this.availableRooms.clear();
        if (rooms != null) {
            this.availableRooms.addAll(rooms);
        }

        // 清空房间列表表格
        roomListTable.clear();

        if (availableRooms.isEmpty()) {
            roomListTable.add(new Label(LanguageManager.getInstance().get("no.rooms.available"), skin)).center().row();
            selectedRoomButton = null;
        } else {
            for (RoomMessage.RoomInfo room : availableRooms) {
                TextButton roomButton = new TextButton(
                    room.getName() + " (" + room.getPlayerCount() + "/" + room.getMaxPlayers() + ")",
                    skin
                );
                roomButton.setUserObject(room.getId());
                roomButton.addListener(event -> {
                    if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                        // 选择房间
                        if (selectedRoomButton != null) {
                            selectedRoomButton.setColor(Color.WHITE);
                        }
                        selectedRoomButton = roomButton;
                        selectedRoomButton.setColor(Color.GREEN);
                        setStatus(ConnectionState.CONNECTED_TO_SERVER, lang().get("selected.room.short").replace("%s", room.getName()));
                    }
                    return true;
                });
                roomListTable.add(roomButton).row();
            }
        }

        if (roomListScrollPane != null) {
            roomListScrollPane.layout();
        }
    }

    // 辅助方法，获取LanguageManager实例
    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    @Override
    public void onConnectResponse(boolean success, String message, String clientId) {
        // 处理连接响应
        if (success) {
            setStatus(ConnectionState.CONNECTED_TO_SERVER, lang().get("connected.message").replace("%s", message));
            updateUIForState(ConnectionState.CONNECTED_TO_SERVER);
        } else {
            setStatus(ConnectionState.ERROR, lang().get("connection.failed").replace("%s", message));
        }
    }

    @Override
    public void onRoomResponse(RoomMessage message) {
        // 处理房间响应
        switch (message.getAction()) {
            case CREATE:
                if (message.isSuccess()) {
                    setStatus(ConnectionState.CONNECTED_TO_SERVER, lang().get("room.created").replace("%s", message.getMessage()));
                    // 加入创建的房间
                    if (message.getRoomId() != null) {
                        NetworkManager networkManager = uiManager.getNetworkManager();
                        if (networkManager != null && networkManager.isConnected()) {
                            networkManager.joinRoom(message.getRoomId());
                        } else {
                            System.err.println("NetworkManager is null or not connected when trying to join room");
                        }
                    }
                } else {
                    setStatus(ConnectionState.ERROR, lang().get("failed.to.create.room").replace("%s", message.getMessage()));
                }
                break;
            case JOIN:
                if (message.isSuccess()) {
                    setStatus(ConnectionState.CONNECTED_TO_ROOM, lang().get("joined.room").replace("%s", message.getMessage()));
                    // 进入房间大厅
                    NetworkManager networkManager = uiManager.getNetworkManager();
                    if (networkManager != null) {
                        RoomLobbyState roomLobbyState = new RoomLobbyState(uiManager, networkManager);
                        roomLobbyState.setRoomInfo(message.getRoomName() != null ? message.getRoomName() : lang().get("default.room.name"), 4, true); // 创建房间的人是房主
                        uiManager.pushState(roomLobbyState);
                    }
                } else {
                    setStatus(ConnectionState.ERROR, lang().get("failed.to.join.room").replace("%s", message.getMessage()));
                }
                break;
            case LEAVE:
                if (message.isSuccess()) {
                    setStatus(ConnectionState.CONNECTED_TO_SERVER, lang().get("left.room").replace("%s", message.getMessage()));
                } else {
                    setStatus(ConnectionState.ERROR, lang().get("failed.to.leave.room").replace("%s", message.getMessage()));
                }
                break;
            case LIST:
                // 处理房间列表
                if (message.isSuccess() && message.getRooms() != null) {
                    updateRoomList(message.getRooms());
                }
                break;
            case START:
                if (message.isSuccess()) {
                    setStatus(ConnectionState.CONNECTED_TO_ROOM, lang().get("game.starting").replace("%s", message.getMessage()));
                } else {
                    setStatus(ConnectionState.ERROR, lang().get("failed.to.start.game").replace("%s", message.getMessage()));
                }
                break;
            case STATUS:
                // 处理房间状态更新
                if (message.isSuccess()) {
                    // 房间状态更新，不需要特殊处理
                    // 这里可以添加更新房间信息的逻辑
                }
                break;
            case KICK:
                if (!message.isSuccess()) {
                    setStatus(ConnectionState.ERROR, lang().get("kick.failed").replace("%s", message.getMessage()));
                }
                break;
            case CHAT:
                // 聊天消息通常在RoomLobbyState中处理
                break;
        }
    }

    @Override
    public void onGameStart(GameStartMessage message) {
        // 处理游戏开始消息
        setStatus(ConnectionState.CONNECTED_TO_ROOM, lang().get("game.started"));
        // 进入游戏状态，使用uiManager中已经存在的GameStateManager实例
        if (uiManager.gameStateManager != null) {
            GameState gameState = new GameState(uiManager, uiManager.gameStateManager);
            uiManager.setScreen(gameState);
        }
    }

    @Override
    public void onGameStateUpdate(GameStateMessage message) {
        // 处理游戏状态更新消息
        // 游戏状态更新通常在GameState中处理
    }

    @Override
    public void onDisconnected() {
        // 处理断开连接消息
        setStatus(ConnectionState.INITIAL, lang().get("disconnected.from.server"));
        updateUIForState(ConnectionState.INITIAL);

        // 停止本地服务器
        LocalServerManager localServerManager = uiManager.getLocalServerManager();
        if (localServerManager != null && localServerManager.isRunning()) {
            localServerManager.stopServer();
        }
    }
}
