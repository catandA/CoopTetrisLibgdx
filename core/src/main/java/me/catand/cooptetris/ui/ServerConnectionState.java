package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.ui.FontUtils;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 服务器连接界面 - 现代化暗色游戏UI风格
 */
public class ServerConnectionState extends BaseUIState implements NetworkManager.NetworkListener {

    private Table mainTable;
    private BitmapFont titleFont;
    private BitmapFont sectionFont;

    private TextField playerNameField;
    private TextField serverAddressField;
    private TextField serverPortField;
    private Label statusLabel;

    private boolean isConnecting = false;
    private boolean isCreatingLocalServer = false;

    // UI颜色配置
    private static final Color COLOR_BG = new Color(0.08f, 0.09f, 0.11f, 1f);
    private static final Color COLOR_PANEL = new Color(0.12f, 0.14f, 0.17f, 0.95f);
    private static final Color COLOR_PANEL_HIGHLIGHT = new Color(0.15f, 0.17f, 0.21f, 0.95f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);
    private static final Color COLOR_SECONDARY = new Color(0.8f, 0.3f, 0.9f, 1f);
    private static final Color COLOR_SUCCESS = new Color(0.3f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_WARNING = new Color(1f, 0.7f, 0.2f, 1f);
    private static final Color COLOR_DANGER = new Color(1f, 0.3f, 0.3f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color COLOR_TEXT_MUTED = new Color(0.5f, 0.52f, 0.55f, 1f);

    // 统一的尺寸
    private static final float LABEL_WIDTH = 120f;
    private static final float INPUT_WIDTH = 200f;
    private static final float ROW_HEIGHT = 40f;

    public ServerConnectionState(UIManager uiManager) {
        super(uiManager);
    }

    @Override
    protected void createUI() {
        LanguageManager lang = LanguageManager.getInstance();

        titleFont = Main.platform.getFont(fontSize(32), lang.get("server.connection.title"), false, false);
        sectionFont = Main.platform.getFont(fontSize(18), "Section", false, false);

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        mainTable.pad(h(40f));

        // 创建主面板
        Table contentPanel = createMainPanel();
        mainTable.add(contentPanel).expand().center();

        stage.addActor(mainTable);

        // 注册网络监听器
        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager != null) {
            networkManager.addListener(this);
        }

        // 初始化输入字段默认值
        initializeDefaultValues();
    }

    private Table createMainPanel() {
        LanguageManager lang = LanguageManager.getInstance();

        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(40f));

        // 标题
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, COLOR_PRIMARY);
        Label titleLabel = new Label(lang.get("server.connection.title"), titleStyle);
        titleLabel.setAlignment(Align.center);

        // 创建连接信息卡片
        Table connectionCard = createConnectionCard();

        // 创建操作按钮区域
        Table buttonArea = createButtonArea();

        // 状态标签
        statusLabel = FontUtils.createLabel("", skin, fontSize(16), COLOR_TEXT);
        statusLabel.setAlignment(Align.center);

        // 组装主界面
        panel.add(titleLabel).padBottom(h(30f)).row();
        panel.add(connectionCard).expandX().padBottom(h(30f)).row();
        panel.add(buttonArea).padBottom(h(15f)).row();
        panel.add(statusLabel);

        return panel;
    }

    private Table createConnectionCard() {
        LanguageManager lang = LanguageManager.getInstance();

        Table card = new Table();
        card.setBackground(createPanelBackground(COLOR_PANEL_HIGHLIGHT));
        card.pad(w(25f));

        // 卡片标题 - 使用动态字体
        String titleText = lang.get("connection.info");
        Label cardTitle = FontUtils.createLabel(titleText, skin, fontSize(18), COLOR_SECONDARY);

        // 玩家名称输入 - 使用动态字体，支持中文输入
        Table nameRow = createInputRow(
            lang.get("player.name"),
            () -> {
                playerNameField = FontUtils.createTextField(skin, fontSize(16), lang.get("enter.player.name"), null);
                return playerNameField;
            }
        );

        // 服务器地址输入 - 使用动态字体
        Table addressRow = createInputRow(
            lang.get("server.address"),
            () -> {
                serverAddressField = FontUtils.createTextField(skin, fontSize(16), "127.0.0.1", "0123456789.");
                return serverAddressField;
            }
        );

        // 端口输入 - 使用动态字体，只允许数字
        Table portRow = createInputRow(
            lang.get("server.port"),
            () -> {
                serverPortField = FontUtils.createTextField(skin, fontSize(16), "8080", "0123456789");
                serverPortField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
                return serverPortField;
            }
        );

        card.add(cardTitle).left().padBottom(h(20f)).row();
        card.add(nameRow).fillX().padBottom(h(12f)).row();
        card.add(addressRow).fillX().padBottom(h(12f)).row();
        card.add(portRow).fillX();

        return card;
    }

    private Table createInputRow(String labelText, java.util.function.Supplier<TextField> fieldSupplier) {
        Table rowTable = new Table();

        // 左侧标签
        Label label = FontUtils.createLabel(labelText, skin, fontSize(16), COLOR_TEXT_MUTED);
        rowTable.add(label).left().width(w(LABEL_WIDTH)).padRight(w(15f));

        // 右侧输入框
        TextField field = fieldSupplier.get();
        rowTable.add(field).left().width(w(INPUT_WIDTH)).height(h(ROW_HEIGHT));

        return rowTable;
    }

    private Table createButtonArea() {
        LanguageManager lang = LanguageManager.getInstance();

        Table buttonTable = new Table();

        // 创建本地服务器按钮
        TextButton localServerButton = FontUtils.createTextButton(lang.get("create.local.server"), skin, fontSize(24), COLOR_SUCCESS);
        localServerButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                createLocalServer();
            }
            return true;
        });

        // 连接按钮
        TextButton connectButton = FontUtils.createTextButton(lang.get("connect"), skin, fontSize(24), COLOR_PRIMARY);
        connectButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                connectToServer();
            }
            return true;
        });

        // 返回按钮
        TextButton backButton = FontUtils.createTextButton(lang.get("back"), skin, fontSize(24), COLOR_TEXT_MUTED);
        backButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                uiManager.popState();
            }
            return true;
        });

        buttonTable.add(localServerButton).width(w(160f)).height(h(45f)).padRight(w(10f));
        buttonTable.add(connectButton).width(w(120f)).height(h(45f)).padRight(w(10f));
        buttonTable.add(backButton).width(w(100f)).height(h(45f));

        return buttonTable;
    }

    private void initializeDefaultValues() {
        // 从设置中读取默认值
        String savedName = TetrisSettings.playerName();
        if (savedName != null && !savedName.isEmpty()) {
            playerNameField.setText(savedName);
        }

        String savedAddress = TetrisSettings.defaultHost();
        if (savedAddress != null && !savedAddress.isEmpty()) {
            serverAddressField.setText(savedAddress);
        }

        int savedPort = TetrisSettings.defaultPort();
        if (savedPort > 0) {
            serverPortField.setText(String.valueOf(savedPort));
        }
    }

    private void createLocalServer() {
        if (isConnecting) return;

        LanguageManager lang = LanguageManager.getInstance();
        String playerName = playerNameField.getText().trim();

        if (playerName.isEmpty()) {
            setStatus(lang.get("error.player.name.empty"), COLOR_DANGER);
            return;
        }

        isConnecting = true;
        isCreatingLocalServer = true;
        setStatus(lang.get("status.creating.server"), COLOR_WARNING);

        // 启动本地服务器（使用与单人游戏相同的端口逻辑，从56148开始）
        if (uiManager.getLocalServerManager() != null) {
            int port = uiManager.getLocalServerManager().startServer(56148);
            if (port == -1) {
                // 服务器启动失败
                isConnecting = false;
                isCreatingLocalServer = false;
                setStatus(lang.get("error.local.server.failed"), COLOR_DANGER);
                return;
            }
            // 使用实际启动的端口连接
            connect("127.0.0.1", port, playerName);
        } else {
            isConnecting = false;
            isCreatingLocalServer = false;
            setStatus(lang.get("error.local.server.manager.null"), COLOR_DANGER);
        }
    }

    private void connectToServer() {
        if (isConnecting) return;

        LanguageManager lang = LanguageManager.getInstance();
        String playerName = playerNameField.getText().trim();
        String address = serverAddressField.getText().trim();
        String portText = serverPortField.getText().trim();

        if (playerName.isEmpty()) {
            setStatus(lang.get("error.player.name.empty"), COLOR_DANGER);
            return;
        }
        if (address.isEmpty()) {
            setStatus(lang.get("error.server.address.empty"), COLOR_DANGER);
            return;
        }
        if (portText.isEmpty()) {
            setStatus(lang.get("error.server.port.empty"), COLOR_DANGER);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                setStatus(lang.get("error.server.port.invalid"), COLOR_DANGER);
                return;
            }
        } catch (NumberFormatException e) {
            setStatus(lang.get("error.server.port.invalid"), COLOR_DANGER);
            return;
        }

        isConnecting = true;
        isCreatingLocalServer = false;
        setStatus(lang.get("status.connecting"), COLOR_WARNING);

        connect(address, port, playerName);
    }

    private void connect(String address, int port, String playerName) {
        // 保存设置（仅在非本地服务器模式下保存端口）
        TetrisSettings.playerName(playerName);
        if (!isCreatingLocalServer) {
            // 只有连接外部服务器时才保存地址和端口设置
            TetrisSettings.defaultHost(address);
            TetrisSettings.defaultPort(port);
        }

        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager != null) {
            boolean success = networkManager.connect(address, port, playerName);
            if (!success) {
                // 连接失败，重置状态
                isConnecting = false;
                isCreatingLocalServer = false;
                LanguageManager lang = LanguageManager.getInstance();
                setStatus(lang.get("error.connection.failed").replace("%s", lang.get("error.connection.refused")), COLOR_DANGER);
            }
        } else {
            // NetworkManager 为空，重置状态
            isConnecting = false;
            isCreatingLocalServer = false;
            LanguageManager lang = LanguageManager.getInstance();
            setStatus(lang.get("error.connection.failed").replace("%s", "NetworkManager is null"), COLOR_DANGER);
        }
    }

    private void setStatus(String message, Color color) {
        // 动态获取字体以支持中文显示
        BitmapFont font = Main.platform.getFont(fontSize(16), message, false, false);
        Label.LabelStyle style = new Label.LabelStyle(statusLabel.getStyle());
        style.font = font;
        statusLabel.setStyle(style);
        statusLabel.setText(message);
        statusLabel.setColor(color);
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
        // 连接界面不需要更新逻辑
    }

    @Override
    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
        if (sectionFont != null) {
            sectionFont.dispose();
            sectionFont = null;
        }
    }

    // ==================== 网络回调 ====================

    @Override
    public void onConnectResponse(boolean success, String message, String clientId) {
        LanguageManager lang = LanguageManager.getInstance();

        if (success) {
            setStatus(lang.get("status.connected"), COLOR_SUCCESS);
            
            if (isCreatingLocalServer) {
                // 本地服务器：直接创建房间并进入房间大厅
                NetworkManager networkManager = uiManager.getNetworkManager();
                if (networkManager != null) {
                    // 创建房间
                    String roomName = playerNameField.getText().trim() + "'s Room";
                    networkManager.createRoom(roomName);
                    // 进入房间大厅（标记为本地服务器模式）
                    RoomLobbyState roomLobby = new RoomLobbyState(uiManager, networkManager, true);
                    roomLobby.setRoomInfo(roomName, 4, true);
                    // 使用setScreen替换当前状态，避免栈中残留ServerConnectionState
                    uiManager.setScreen(roomLobby);
                }
            } else {
                // 外部服务器：显示房间列表
                uiManager.pushState(new RoomListState(uiManager));
            }
        } else {
            isConnecting = false;
            isCreatingLocalServer = false;
            setStatus(lang.get("error.connection.failed").replace("%s", message), COLOR_DANGER);
        }
    }

    @Override
    public void onRoomResponse(RoomMessage message) {}

    @Override
    public void onGameStart(GameStartMessage message) {}

    @Override
    public void onGameStateUpdate(GameStateMessage message) {}

    @Override
    public void onDisconnected() {
        isConnecting = false;
        isCreatingLocalServer = false;
        LanguageManager lang = LanguageManager.getInstance();
        setStatus(lang.get("status.disconnected"), COLOR_DANGER);
    }

    @Override
    public void onNotification(NotificationMessage message) {
        if (message.getMessage() != null) {
            setStatus(message.getMessage(), COLOR_WARNING);
        }
    }
}
