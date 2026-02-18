package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.network.LocalServerManager;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 服务器连接界面 - 联机游戏的第一阶段
 * <p>
 * 功能：
 * 1. 输入服务器地址、端口、玩家名称
 * 2. 选择模式：创建本地服务器 或 连接到远程服务器
 * 3. 连接成功后根据服务器类型决定下一步：
 *    - 本地服务器：直接进入房间大厅（创建者成为房主）
 *    - 远程服务器：进入房间列表界面
 */
public class ServerConnectionState extends BaseUIState implements NetworkManager.NetworkListener {

    private Table mainTable;
    private TextField hostField;
    private TextField portField;
    private TextField playerNameField;
    private Label statusLabel;
    private BitmapFont titleFont;

    // UI 引用（用于状态更新）
    private TextButton connectButton;
    private TextButton localServerButton;
    private TextButton backButton;

    // 连接状态
    private boolean isConnecting = false;

    public ServerConnectionState(UIManager uiManager) {
        super(uiManager);
    }

    @Override
    protected void createUI() {
        mainTable = new Table();
        mainTable.setPosition(offsetX(), offsetY());
        mainTable.setSize(displayWidth(), displayHeight());
        mainTable.center();
        mainTable.pad(h(20f));

        LanguageManager lang = LanguageManager.getInstance();

        // 标题
        titleFont = Main.platform.getFont(fontSize(28), lang.get("online.mode.title"), false, false);
        Label title = createTitleLabel(lang.get("online.mode.title"));
        title.setAlignment(Align.center);

        // 状态标签
        statusLabel = new Label(lang.get("status.ready"), skin);
        statusLabel.setColor(Color.GRAY);
        statusLabel.setAlignment(Align.center);

        // 输入区域
        Table inputTable = new Table();
        inputTable.defaults().padBottom(h(15f));

        // 玩家名称
        Label nameLabel = new Label(lang.get("player.name"), skin);
        String savedPlayerName = TetrisSettings.playerName();
        playerNameField = new TextField(savedPlayerName, skin);
        playerNameField.setMessageText(lang.get("enter.player.name"));

        inputTable.add(nameLabel).right().padRight(w(15f));
        inputTable.add(playerNameField).width(w(280f)).height(h(45f)).row();

        // 服务器地址
        Label hostLabel = new Label(lang.get("server.host"), skin);
        hostField = new TextField("localhost", skin);
        hostField.setMessageText(lang.get("enter.host"));

        inputTable.add(hostLabel).right().padRight(w(15f));
        inputTable.add(hostField).width(w(280f)).height(h(45f)).row();

        // 端口
        Label portLabel = new Label(lang.get("server.port"), skin);
        portField = new TextField("8080", skin);
        portField.setMessageText(lang.get("enter.port"));

        inputTable.add(portLabel).right().padRight(w(15f));
        inputTable.add(portField).width(w(280f)).height(h(45f)).row();

        // 按钮区域
        Table buttonTable = new Table();
        buttonTable.defaults().width(w(260f)).height(h(55f)).padBottom(h(12f));

        // 创建本地服务器按钮
        localServerButton = new TextButton(lang.get("create.local.server"), skin);
        localServerButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                createLocalServerAndConnect();
            }
            return true;
        });

        // 连接到服务器按钮
        connectButton = new TextButton(lang.get("connect.to.server"), skin);
        connectButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                connectToRemoteServer();
            }
            return true;
        });

        // 返回按钮
        backButton = new TextButton(lang.get("back"), skin);
        backButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                uiManager.popState();
            }
            return true;
        });

        buttonTable.add(localServerButton).row();
        buttonTable.add(connectButton).row();
        buttonTable.add(backButton).row();

        // 组装主表格
        mainTable.add(title).padBottom(h(30f)).row();
        mainTable.add(statusLabel).padBottom(h(25f)).row();
        mainTable.add(inputTable).padBottom(h(20f)).row();
        mainTable.add(buttonTable).row();

        // 注册网络监听器
        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager != null) {
            networkManager.addListener(this);
        }

        stage.addActor(mainTable);
    }

    private Label createTitleLabel(String text) {
        if (titleFont != null) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(titleFont, skin.getColor("font"));
            return new Label(text, labelStyle);
        } else {
            return new Label(text, skin);
        }
    }

    @Override
    protected void clearUI() {
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
    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
    }

    @Override
    public void update(float delta) {
        // 可以在这里添加连接超时检测等逻辑
    }

    // ==================== 核心功能方法 ====================

    /**
     * 创建本地服务器并连接
     */
    private void createLocalServerAndConnect() {
        if (isConnecting) return;

        String playerName = getValidatedPlayerName();
        if (playerName == null) return;

        isConnecting = true;
        isLocalServerMode = true; // 标记为本地服务器模式
        setStatus(lang().get("status.starting.local.server"), Color.YELLOW);
        setButtonsEnabled(false);

        // 启动本地服务器
        LocalServerManager localServerManager = uiManager.getLocalServerManager();
        if (localServerManager == null) {
            setStatus(lang().get("error.local.server.not.available"), Color.RED);
            isConnecting = false;
            setButtonsEnabled(true);
            return;
        }

        // 如果服务器已在运行，先停止
        if (localServerManager.isRunning()) {
            localServerManager.stopServer();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 启动服务器
        int startPort = 56148;
        int actualPort = localServerManager.startServer(startPort);

        if (actualPort == -1) {
            setStatus(lang().get("error.failed.to.start.local.server"), Color.RED);
            isConnecting = false;
            setButtonsEnabled(true);
            return;
        }

        // 连接到本地服务器
        setStatus(lang().get("status.connecting.to.local.server"), Color.YELLOW);
        NetworkManager networkManager = uiManager.getNetworkManager();

        if (networkManager.connect("localhost", actualPort, playerName)) {
            // 连接成功，等待服务器响应
            // onConnectResponse 会处理后续逻辑
        } else {
            setStatus(lang().get("error.failed.to.connect.local.server"), Color.RED);
            isConnecting = false;
            setButtonsEnabled(true);
            localServerManager.stopServer();
        }
    }

    /**
     * 连接到远程服务器
     */
    private void connectToRemoteServer() {
        if (isConnecting) return;

        String playerName = getValidatedPlayerName();
        if (playerName == null) return;

        String host = hostField.getText().trim();
        if (host.isEmpty()) {
            setStatus(lang().get("error.host.empty"), Color.RED);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
            if (port < 1 || port > 65535) {
                setStatus(lang().get("error.port.invalid"), Color.RED);
                return;
            }
        } catch (NumberFormatException e) {
            setStatus(lang().get("error.port.number"), Color.RED);
            return;
        }

        isConnecting = true;
        setStatus(lang().get("status.connecting").replace("%s", host + ":" + port), Color.YELLOW);
        setButtonsEnabled(false);

        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager.connect(host, port, playerName)) {
            // 连接成功，等待服务器响应
            // onConnectResponse 会处理后续逻辑
        } else {
            setStatus(lang().get("error.failed.to.connect").replace("%s", host + ":" + port), Color.RED);
            isConnecting = false;
            setButtonsEnabled(true);
        }
    }

    /**
     * 获取并验证玩家名称
     */
    private String getValidatedPlayerName() {
        String playerName = playerNameField.getText().trim();
        if (playerName.isEmpty()) {
            setStatus(lang().get("error.player.name.empty"), Color.RED);
            return null;
        }
        if (playerName.length() > 20) {
            setStatus(lang().get("error.player.name.too.long"), Color.RED);
            return null;
        }
        // 保存玩家名称
        TetrisSettings.playerName(playerName);
        return playerName;
    }

    /**
     * 设置状态文本
     */
    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setColor(color);
    }

    /**
     * 设置按钮启用状态
     */
    private void setButtonsEnabled(boolean enabled) {
        localServerButton.setDisabled(!enabled);
        connectButton.setDisabled(!enabled);
        backButton.setDisabled(!enabled);
    }

    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    // ==================== NetworkListener 回调 ====================

    // 标记是否是本地服务器模式（由 createLocalServerAndConnect 设置）
    private boolean isLocalServerMode = false;

    @Override
    public void onConnectResponse(boolean success, String message, String clientId) {
        System.out.println("[ServerConnectionState] onConnectResponse called, success: " + success + ", message: " + message + ", isLocalServerMode: " + isLocalServerMode);

        if (!success) {
            setStatus(lang().get("error.connection.failed").replace("%s", message), Color.RED);
            isConnecting = false;
            setButtonsEnabled(true);
            return;
        }

        // 连接成功
        NetworkManager networkManager = uiManager.getNetworkManager();

        if (isLocalServerMode) {
            // 本地服务器模式：创建房间并进入
            setStatus(lang().get("status.connected.local.server"), Color.GREEN);

            // 本地服务器创建者自动创建一个房间
            String roomName = lang().get("default.room.name") + (int)(Math.random() * 1000);
            System.out.println("[ServerConnectionState] Creating room for local server: " + roomName);
            networkManager.createRoom(roomName);
            // 等待服务器响应，在 onRoomResponse 中处理

        } else {
            // 连接到远程服务器模式：进入房间列表
            System.out.println("[ServerConnectionState] Pushing RoomListState for remote server");
            setStatus(lang().get("status.connected.remote.server"), Color.GREEN);
            uiManager.pushState(new RoomListState(uiManager));
            isConnecting = false;
        }
    }

    @Override
    public void onRoomResponse(RoomMessage message) {
        // 处理创建房间响应（仅本地服务器流程）
        // 只有本地服务器才在这里处理 CREATE 响应
        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager == null) return;

        // 使用 isLocalServerMode 而不是 getCurrentConnectionType()
        // 因为用户可能通过"连接到服务器"按钮连接到 localhost
        System.out.println("[ServerConnectionState] onRoomResponse called, action: " + message.getAction() + ", isLocalServerMode: " + isLocalServerMode);

        if (!isLocalServerMode) {
            // 远程服务器的 CREATE 响应不应该在这里处理
            // 它应该在 RoomListState 中处理
            System.out.println("[ServerConnectionState] Ignoring room response for non-local server mode");
            return;
        }

        if (message.getAction() == RoomMessage.RoomAction.CREATE) {
            if (message.isSuccess()) {
                // 房间创建成功，进入房间大厅
                System.out.println("[ServerConnectionState] Room created successfully, entering lobby");
                // 传入 isLocalServerMode，确保 RoomLobbyState 知道正确的连接类型
                RoomLobbyState roomLobby = new RoomLobbyState(uiManager, networkManager, isLocalServerMode);
                roomLobby.setRoomInfo(
                    message.getRoomName() != null ? message.getRoomName() : lang().get("default.room.name"),
                    4,
                    true // 本地服务器创建者是房主
                );
                uiManager.pushState(roomLobby);
                isConnecting = false;
            } else {
                // 房间创建失败
                System.out.println("[ServerConnectionState] Room creation failed: " + message.getMessage());
                setStatus(lang().get("error.failed.to.create.room").replace("%s", message.getMessage()), Color.RED);
                isConnecting = false;
                setButtonsEnabled(true);
            }
        }
    }

    @Override
    public void onGameStart(GameStartMessage message) {
        // 在此界面不处理游戏开始消息
    }

    @Override
    public void onGameStateUpdate(GameStateMessage message) {
        // 在此界面不处理游戏状态消息
    }

    @Override
    public void onDisconnected() {
        setStatus(lang().get("status.disconnected"), Color.RED);
        isConnecting = false;
        setButtonsEnabled(true);

        // 停止本地服务器（如果正在运行）
        LocalServerManager localServerManager = uiManager.getLocalServerManager();
        if (localServerManager != null && localServerManager.isRunning()) {
            localServerManager.stopServer();
        }
    }

    @Override
    public void onNotification(NotificationMessage message) {
        // 处理通知消息
        switch (message.getNotificationType()) {
            case DISCONNECTED:
                setStatus(lang().get("status.disconnected.server"), Color.RED);
                isConnecting = false;
                setButtonsEnabled(true);
                break;
            default:
                // 其他通知类型可以显示在状态栏
                if (message.getMessage() != null) {
                    setStatus(message.getMessage(), Color.ORANGE);
                }
                break;
        }
    }
}
