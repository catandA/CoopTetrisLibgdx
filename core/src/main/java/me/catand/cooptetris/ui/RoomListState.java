package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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

import me.catand.cooptetris.Main;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.util.LanguageManager;

/**
 * 房间列表界面 - 现代化暗色游戏UI风格
 */
public class RoomListState extends BaseUIState implements NetworkManager.NetworkListener {

    private Table mainTable;
    private Table roomListTable;
    private Table roomListHeaderTable;
    private ScrollPane roomListScrollPane;
    private TextField newRoomNameField;
    private Label statusLabel;
    private Label serverInfoLabel;
    private BitmapFont titleFont;
    private BitmapFont smallFont;

    private TextButton createButton;
    private TextButton refreshButton;
    private TextButton disconnectButton;

    private final List<RoomMessage.RoomInfo> availableRooms;
    private final List<Table> roomRowTables;

    private boolean isProcessing = false;

    // UI颜色配置
    private static final Color COLOR_BG = new Color(0.08f, 0.09f, 0.11f, 1f);
    private static final Color COLOR_PANEL = new Color(0.12f, 0.14f, 0.17f, 0.95f);
    private static final Color COLOR_PANEL_HIGHLIGHT = new Color(0.15f, 0.17f, 0.21f, 0.95f);
    private static final Color COLOR_BORDER = new Color(0.25f, 0.28f, 0.35f, 1f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);
    private static final Color COLOR_SECONDARY = new Color(0.8f, 0.3f, 0.9f, 1f);
    private static final Color COLOR_SUCCESS = new Color(0.3f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_WARNING = new Color(1f, 0.7f, 0.2f, 1f);
    private static final Color COLOR_DANGER = new Color(1f, 0.3f, 0.3f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color TEXT_MUTED = new Color(0.5f, 0.52f, 0.55f, 1f);
    private static final Color COLOR_SELECTED = new Color(0.2f, 0.8f, 1f, 0.3f);

    public RoomListState(UIManager uiManager) {
        super(uiManager);
        this.availableRooms = new ArrayList<>();
        this.roomRowTables = new ArrayList<>();
    }

    private boolean needsRefresh = true;

    @Override
    public void show(Stage stage, Skin skin) {
        super.show(stage, skin);
    }

    @Override
    protected void recreateUI() {
        // resize时不需要刷新房间列表
        needsRefresh = false;
        super.recreateUI();
        needsRefresh = true;
    }

    @Override
    protected void createUI() {
        titleFont = Main.platform.getFont(fontSize(28), lang().get("room.list.title"), false, false);
        smallFont = Main.platform.getFont(fontSize(14), "Rooms", false, false);

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        mainTable.pad(h(30f));

        Table contentPanel = createMainPanel();
        mainTable.add(contentPanel).expand().center();

        stage.addActor(mainTable);

        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager != null) {
            networkManager.addListener(this);
            isProcessing = false;
            // 只在非resize情况下刷新房间列表
            if (needsRefresh) {
                refreshRoomList();
            } else {
                // resize时重新渲染已有的房间数据
                updateRoomListDisplay(new ArrayList<>(availableRooms));
            }
        }
    }

    private Table createMainPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(30f));

        // 标题区域
        Table header = createHeader();
        panel.add(header).fillX().padBottom(h(20f)).row();

        // 房间列表区域
        Table roomListPanel = createRoomListPanel();
        panel.add(roomListPanel).fill().expand().padBottom(h(20f)).row();

        // 创建房间输入区域
        Table createRoomPanel = createCreateRoomPanel();
        panel.add(createRoomPanel).fillX().padBottom(h(20f)).row();

        // 按钮区域
        Table buttonArea = createButtonArea();
        panel.add(buttonArea).fillX();

        return panel;
    }

    private Table createHeader() {
        Table header = new Table();

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, COLOR_PRIMARY);
        Label title = new Label(lang().get("room.list.title"), titleStyle);
        title.setAlignment(Align.left);

        NetworkManager networkManager = uiManager.getNetworkManager();
        String serverInfo = networkManager != null ?
            lang().get("connected.to") + ": " + networkManager.getPlayerName() : lang().get("unknown.server");
        serverInfoLabel = FontUtils.createLabel(serverInfo, skin, fontSize(16), COLOR_SECONDARY);
        serverInfoLabel.setAlignment(Align.right);

        statusLabel = FontUtils.createLabel(lang().get("status.loading.rooms"), skin, fontSize(16), TEXT_MUTED);
        statusLabel.setAlignment(Align.center);

        header.add(title).left().expandX();
        header.add(serverInfoLabel).right().row();
        header.add(statusLabel).colspan(2).center().padTop(h(10f));

        return header;
    }

    private Table createRoomListPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL_HIGHLIGHT));
        panel.pad(w(15f));

        // 列表标题 - 使用动态字体
        String listTitleText = lang().get("available.rooms");
        Label listTitle = FontUtils.createLabel(listTitleText, skin, fontSize(14), COLOR_SECONDARY);
        panel.add(listTitle).left().padBottom(h(10f)).row();

        // 表头
        roomListHeaderTable = createRoomListHeader();
        panel.add(roomListHeaderTable).fillX().padBottom(h(5f)).row();

        // 房间列表
        roomListTable = new Table();
        roomListTable.top().left();

        roomListScrollPane = new ScrollPane(roomListTable, skin);
        roomListScrollPane.setFadeScrollBars(false);
        roomListScrollPane.setScrollingDisabled(true, false);

        panel.add(roomListScrollPane).height(h(250f)).fillX();

        return panel;
    }

    private Table createRoomListHeader() {
        Table header = new Table();
        header.setBackground(createPanelBackground(new Color(0.18f, 0.20f, 0.24f, 1f)));
        header.pad(w(8f));

        // 列标题
        Label nameHeader = FontUtils.createLabel(lang().get("room.header.name"), skin, fontSize(14), TEXT_MUTED);
        Label playersHeader = FontUtils.createLabel(lang().get("room.header.players"), skin, fontSize(14), TEXT_MUTED);
        Label statusHeader = FontUtils.createLabel(lang().get("room.header.status"), skin, fontSize(14), TEXT_MUTED);
        Label actionHeader = FontUtils.createLabel(lang().get("room.header.action"), skin, fontSize(14), TEXT_MUTED);

        header.add(nameHeader).left().width(w(180f)).padRight(w(10f));
        header.add(playersHeader).center().width(w(80f)).padRight(w(10f));
        header.add(statusHeader).center().width(w(100f)).padRight(w(10f));
        header.add(actionHeader).center().width(w(100f));

        return header;
    }

    private Table createCreateRoomPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL_HIGHLIGHT));
        panel.pad(w(15f));

        Label roomNameLabel = FontUtils.createLabel(lang().get("new.room.name"), skin, fontSize(16), TEXT_MUTED);

        // 使用动态字体创建TextField，包含房间名可能用到的字符
        String roomNameChars = lang().get("default.room.name") + lang().get("enter.room.name");
        newRoomNameField = FontUtils.createTextField(skin, fontSize(16), lang().get("enter.room.name"), roomNameChars);
        // 使用玩家名字生成默认房间名
        NetworkManager networkManager = uiManager.getNetworkManager();
        String playerName = networkManager != null ? networkManager.getPlayerName() : lang().get("default.player.name");
        newRoomNameField.setText(playerName + lang().get("default.room.name.suffix"));

        panel.add(roomNameLabel).padRight(w(10f));
        panel.add(newRoomNameField).width(w(250f)).height(h(40f)).expandX();

        return panel;
    }

    private Table createButtonArea() {
        Table buttonArea = new Table();

        createButton = FontUtils.createTextButton(lang().get("create.room"), skin, fontSize(18), COLOR_SUCCESS);
        createButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                createNewRoom();
            }
            return true;
        });

        refreshButton = FontUtils.createTextButton(lang().get("refresh"), skin, fontSize(18), COLOR_SECONDARY);
        refreshButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                refreshRoomList();
            }
            return true;
        });

        disconnectButton = FontUtils.createTextButton(lang().get("disconnect"), skin, fontSize(18), COLOR_DANGER);
        disconnectButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                disconnectFromServer();
            }
            return true;
        });

        buttonArea.add(createButton).width(w(130f)).height(h(45f)).padRight(w(10f));
        buttonArea.add(refreshButton).width(w(130f)).height(h(45f)).padRight(w(10f));
        buttonArea.add(disconnectButton).width(w(130f)).height(h(45f));

        return buttonArea;
    }

    private void updateRoomListDisplay(List<RoomMessage.RoomInfo> rooms) {
        roomRowTables.clear();

        roomListTable.clear();

        if (rooms == null || rooms.isEmpty()) {
            Label emptyLabel = FontUtils.createLabel(lang().get("no.rooms.available"), skin, fontSize(16), TEXT_MUTED);
            roomListTable.add(emptyLabel).center().padTop(h(50f)).row();
            setStatus(lang().get("status.no.rooms"), TEXT_MUTED);
            return;
        }

        for (RoomMessage.RoomInfo room : rooms) {
            Table roomRow = createRoomRow(room);
            roomRowTables.add(roomRow);
            roomListTable.add(roomRow).fillX().padBottom(h(4f)).row();
        }

        setStatus(lang().get("status.rooms.loaded").replace("%d", String.valueOf(rooms.size())), COLOR_SUCCESS);

        roomListScrollPane.layout();
        roomListScrollPane.setScrollPercentY(0);
    }

    private Table createRoomRow(RoomMessage.RoomInfo room) {
        Table row = new Table();
        row.setBackground(createPanelBackground(new Color(0.13f, 0.15f, 0.18f, 1f)));
        row.pad(w(8f));

        // 使用显示的玩家数量（包含锁定的槽位）
        int displayCount = room.getDisplayPlayerCount() > 0 ? room.getDisplayPlayerCount() : room.getPlayerCount();
        int maxPlayers = room.getMaxPlayers();
        boolean isFull = displayCount >= maxPlayers;
        boolean isStarted = room.isStarted();
        boolean spectatorLocked = room.isSpectatorLocked();

        // 判断加入状态
        boolean canJoinAsPlayer = !isFull && !isStarted;
        boolean canJoinAsSpectator = !spectatorLocked && (isFull || isStarted);
        boolean canJoin = canJoinAsPlayer || canJoinAsSpectator;

        // 第一列：房间名
        Label nameLabel = FontUtils.createLabel(room.getName(), skin, fontSize(14), COLOR_TEXT);
        nameLabel.setEllipsis(true);
        row.add(nameLabel).left().width(w(180f)).padRight(w(10f));

        // 第二列：人数
        // 满人但可观战时显示黄色，满人且不可观战时显示红色，未满时显示绿色
        String playersText = displayCount + "/" + maxPlayers;
        Color playersColor;
        if (isFull) {
            playersColor = canJoinAsSpectator ? COLOR_WARNING : COLOR_DANGER;
        } else {
            playersColor = COLOR_SUCCESS;
        }
        Label playersLabel = FontUtils.createLabel(playersText, skin, fontSize(14), playersColor);
        row.add(playersLabel).center().width(w(80f)).padRight(w(10f));

        // 第三列：状态
        String statusText;
        Color statusColor;
        if (isStarted) {
            if (canJoinAsSpectator) {
                statusText = lang().get("room.status.spectator.available");
                statusColor = COLOR_WARNING;
            } else {
                statusText = lang().get("room.status.in.game");
                statusColor = COLOR_DANGER;
            }
        } else if (isFull) {
            if (canJoinAsSpectator) {
                statusText = lang().get("room.status.spectator.available");
                statusColor = COLOR_WARNING;
            } else {
                statusText = lang().get("room.status.full");
                statusColor = COLOR_DANGER;
            }
        } else {
            statusText = lang().get("room.status.waiting");
            statusColor = COLOR_SUCCESS;
        }
        Label statusLabel = FontUtils.createLabel(statusText, skin, fontSize(14), statusColor);
        row.add(statusLabel).center().width(w(100f)).padRight(w(10f));

        // 第四列：加入按钮
        String buttonText;
        Color buttonColor;
        if (canJoinAsPlayer) {
            buttonText = lang().get("room.action.join");
            buttonColor = COLOR_PRIMARY;
        } else if (canJoinAsSpectator) {
            buttonText = lang().get("room.action.spectate");
            buttonColor = COLOR_WARNING;
        } else {
            buttonText = lang().get("room.action.join");
            buttonColor = TEXT_MUTED;
        }

        TextButton joinBtn = FontUtils.createTextButton(buttonText, skin, fontSize(14), buttonColor);
        joinBtn.setDisabled(!canJoin);
        joinBtn.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                if (canJoin) {
                    joinRoom(room);
                }
            }
            return true;
        });
        row.add(joinBtn).center().width(w(100f)).height(h(35f));

        return row;
    }

    // ==================== 按钮样式 ====================

    private void stylePrimaryButton(TextButton button) {
        button.setColor(COLOR_PRIMARY);
    }

    private void styleSuccessButton(TextButton button) {
        button.setColor(COLOR_SUCCESS);
    }

    private void styleSecondaryButton(TextButton button) {
        button.setColor(COLOR_SECONDARY);
    }

    private void styleDangerButton(TextButton button) {
        button.setColor(COLOR_DANGER);
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

    private void refreshRoomList() {
        if (isProcessing) return;

        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager == null || !networkManager.isConnected()) {
            setStatus(lang().get("error.not.connected"), COLOR_DANGER);
            return;
        }

        setStatus(lang().get("status.loading.rooms"), COLOR_WARNING);
        networkManager.listRooms();
    }

    private void createNewRoom() {
        if (isProcessing) return;

        String roomName = newRoomNameField.getText().trim();
        if (roomName.isEmpty()) {
            setStatus(lang().get("error.room.name.empty"), COLOR_DANGER);
            return;
        }
        if (roomName.length() > 30) {
            setStatus(lang().get("error.room.name.too.long"), COLOR_DANGER);
            return;
        }

        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager == null || !networkManager.isConnected()) {
            setStatus(lang().get("error.not.connected"), COLOR_DANGER);
            return;
        }

        isProcessing = true;
        setButtonsEnabled(false);
        setStatus(lang().get("status.creating.room"), COLOR_WARNING);

        networkManager.createRoom(roomName);
    }

    private void joinRoom(RoomMessage.RoomInfo room) {
        if (isProcessing) return;
        if (room == null) {
            setStatus(lang().get("error.no.room.selected"), COLOR_DANGER);
            return;
        }

        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager == null || !networkManager.isConnected()) {
            setStatus(lang().get("error.not.connected"), COLOR_DANGER);
            return;
        }

        isProcessing = true;
        setButtonsEnabled(false);
        setStatus(lang().get("status.joining.room"), COLOR_WARNING);

        networkManager.joinRoom(room.getId());
    }

    private void disconnectFromServer() {
        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager != null) {
            networkManager.disconnect();
        }
        uiManager.popState();
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setColor(color);
    }

    private void setButtonsEnabled(boolean enabled) {
        createButton.setDisabled(!enabled);
        refreshButton.setDisabled(!enabled);
        disconnectButton.setDisabled(!enabled);
    }

    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    // ==================== 生命周期方法 ====================

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
        if (smallFont != null) {
            smallFont.dispose();
            smallFont = null;
        }
    }

    @Override
    public void update(float delta) {
        // 可以添加加载动画等
    }

    // ==================== 网络回调 ====================

    @Override
    public void onConnectResponse(boolean success, String message, String clientId) {}

    @Override
    public void onRoomResponse(RoomMessage message) {
        switch (message.getAction()) {
            case LIST:
                if (message.isSuccess()) {
                    // 更新房间数据并刷新显示
                    availableRooms.clear();
                    if (message.getRooms() != null) {
                        availableRooms.addAll(message.getRooms());
                    }
                    updateRoomListDisplay(availableRooms);
                } else {
                    setStatus(lang().get("error.failed.to.load.rooms"), COLOR_DANGER);
                }
                isProcessing = false;
                setButtonsEnabled(true);
                break;

            case CREATE:
                if (message.isSuccess()) {
                    setStatus(lang().get("status.room.created"), COLOR_SUCCESS);
                    NetworkManager networkManager = uiManager.getNetworkManager();
                    RoomLobbyState roomLobby = new RoomLobbyState(uiManager, networkManager, false);
                    roomLobby.setRoomInfo(
                        message.getRoomName() != null ? message.getRoomName() : lang().get("default.room.name"),
                        4,
                        true
                    );
                    uiManager.pushState(roomLobby);
                } else {
                    setStatus(lang().get("error.failed.to.create.room").replace("%s", message.getMessage()), COLOR_DANGER);
                    isProcessing = false;
                    setButtonsEnabled(true);
                }
                break;

            case JOIN:
                if (message.isSuccess()) {
                    setStatus(lang().get("status.joined.room"), COLOR_SUCCESS);
                    NetworkManager networkManager = uiManager.getNetworkManager();
                    RoomLobbyState roomLobby = new RoomLobbyState(uiManager, networkManager, false);
                    roomLobby.setRoomInfo(
                        message.getRoomName() != null ? message.getRoomName() : lang().get("default.room.name"),
                        4,
                        message.isHost()
                    );
                    uiManager.pushState(roomLobby);
                } else {
                    setStatus(lang().get("error.failed.to.join.room").replace("%s", message.getMessage()), COLOR_DANGER);
                    isProcessing = false;
                    setButtonsEnabled(true);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onGameStart(GameStartMessage message) {}

    @Override
    public void onGameStateUpdate(GameStateMessage message) {}

    @Override
    public void onDisconnected() {
        // 显示断开连接弹窗
        LanguageManager lang = LanguageManager.getInstance();
        NotificationMessage message = new NotificationMessage();
        message.setNotificationType(NotificationMessage.NotificationType.DISCONNECTED);
        message.setTitle(lang.get("notification.title.disconnected"));
        message.setMessage(lang.get("error.connection.lost"));

        NotificationDialog dialog = new NotificationDialog(skin);
        dialog.setNotification(message);
        dialog.setOnCloseAction(() -> {
            // 返回主菜单
            uiManager.setScreen(new MainMenuState(uiManager));
        });
        dialog.show(stage);
    }

    @Override
    public void onNotification(NotificationMessage message) {
        switch (message.getNotificationType()) {
            case DISCONNECTED:
                setStatus(lang().get("status.disconnected.server"), COLOR_DANGER);
                uiManager.popState();
                break;
            default:
                if (message.getMessage() != null) {
                    setStatus(message.getMessage(), COLOR_WARNING);
                }
                break;
        }
    }
}
