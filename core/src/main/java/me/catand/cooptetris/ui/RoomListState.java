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
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.shared.message.GameStartMessage;
import me.catand.cooptetris.shared.message.GameStateMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.util.LanguageManager;

/**
 * 房间列表界面 - 联机游戏的第二阶段（仅用于专有服务器）
 * <p>
 * 功能：
 * 1. 显示服务器上的可用房间列表
 * 2. 创建新房间
 * 3. 选择并加入现有房间
 * 4. 断开服务器连接返回
 */
public class RoomListState extends BaseUIState implements NetworkManager.NetworkListener {

    private Table mainTable;
    private Table roomListTable;
    private ScrollPane roomListScrollPane;
    private TextField newRoomNameField;
    private Label statusLabel;
    private Label serverInfoLabel;
    private BitmapFont titleFont;

    // UI 引用
    private TextButton joinButton;
    private TextButton createButton;
    private TextButton refreshButton;
    private TextButton disconnectButton;

    // 数据
    private final List<RoomMessage.RoomInfo> availableRooms;
    private RoomMessage.RoomInfo selectedRoom;
    private TextButton selectedRoomButton;

    // 状态
    private boolean isProcessing = false;

    public RoomListState(UIManager uiManager) {
        super(uiManager);
        this.availableRooms = new ArrayList<>();
    }

    @Override
    protected void createUI() {
        mainTable = new Table();
        mainTable.setPosition(offsetX(), offsetY());
        mainTable.setSize(displayWidth(), displayHeight());
        mainTable.center();
        mainTable.pad(h(20f));

        LanguageManager lang = LanguageManager.getInstance();
        NetworkManager networkManager = uiManager.getNetworkManager();

        // 标题
        titleFont = Main.platform.getFont(fontSize(26), lang.get("room.list.title"), false, false);
        Label title = createTitleLabel(lang.get("room.list.title"));
        title.setAlignment(Align.center);

        // 服务器信息
        String serverInfo = networkManager != null ?
            lang.get("connected.to") + ": " + networkManager.getPlayerName() : lang.get("unknown.server");
        serverInfoLabel = new Label(serverInfo, skin);
        serverInfoLabel.setAlignment(Align.center);
        serverInfoLabel.setColor(Color.CYAN);

        // 状态标签
        statusLabel = new Label(lang.get("status.loading.rooms"), skin);
        statusLabel.setAlignment(Align.center);
        statusLabel.setColor(Color.GRAY);

        // 房间列表区域
        roomListTable = new Table();
        roomListTable.defaults().width(w(360f)).padBottom(h(8f));

        roomListScrollPane = new ScrollPane(roomListTable, skin);
        roomListScrollPane.setFadeScrollBars(false);
        roomListScrollPane.setScrollBarPositions(false, true);
        roomListScrollPane.setScrollingDisabled(true, false);

        // 创建房间输入区域
        Table createRoomTable = new Table();
        createRoomTable.defaults().padRight(w(10f));

        Label roomNameLabel = new Label(lang.get("new.room.name"), skin);
        newRoomNameField = new TextField(lang.get("default.room.name") + (int)(Math.random() * 1000), skin);
        newRoomNameField.setMessageText(lang.get("enter.room.name"));

        createRoomTable.add(roomNameLabel);
        createRoomTable.add(newRoomNameField).width(w(220f)).height(h(40f));

        // 按钮区域
        Table buttonTable = new Table();
        buttonTable.defaults().width(w(180f)).height(h(50f)).padBottom(h(10f)).padRight(w(10f));

        // 加入房间按钮
        joinButton = new TextButton(lang.get("join.room"), skin);
        joinButton.setDisabled(true); // 初始禁用，需要选择房间
        addCyanHoverEffect(joinButton);
        joinButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                joinSelectedRoom();
            }
            return true;
        });

        // 创建房间按钮
        createButton = new TextButton(lang.get("create.room"), skin);
        addCyanHoverEffect(createButton);
        createButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                createNewRoom();
            }
            return true;
        });

        // 刷新按钮
        refreshButton = new TextButton(lang.get("refresh"), skin);
        addCyanHoverEffect(refreshButton);
        refreshButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                refreshRoomList();
            }
            return true;
        });

        // 断开连接按钮
        disconnectButton = new TextButton(lang.get("disconnect"), skin);
        addCyanHoverEffect(disconnectButton);
        disconnectButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                disconnectFromServer();
            }
            return true;
        });

        // 第一行按钮
        buttonTable.add(joinButton);
        buttonTable.add(createButton).row();

        // 第二行按钮
        buttonTable.add(refreshButton);
        buttonTable.add(disconnectButton).row();

        // 组装主表格
        mainTable.add(title).padBottom(h(15f)).row();
        mainTable.add(serverInfoLabel).padBottom(h(10f)).row();
        mainTable.add(statusLabel).padBottom(h(15f)).row();
        mainTable.add(roomListScrollPane).height(h(250f)).width(w(400f)).padBottom(h(15f)).row();
        mainTable.add(createRoomTable).padBottom(h(15f)).row();
        mainTable.add(buttonTable).row();

        // 注册网络监听器
        if (networkManager != null) {
            System.out.println("[RoomListState] Adding network listener and requesting room list");
            networkManager.addListener(this);
            // 重置处理状态，确保可以刷新房间列表
            isProcessing = false;
            // 自动请求房间列表
            refreshRoomList();
        } else {
            System.out.println("[RoomListState] NetworkManager is null!");
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
        System.out.println("[RoomListState] clearUI called");
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
        // 可以添加加载动画等
    }

    // ==================== 核心功能方法 ====================

    /**
     * 刷新房间列表
     */
    private void refreshRoomList() {
        if (isProcessing) return;

        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager == null || !networkManager.isConnected()) {
            setStatus(lang().get("error.not.connected"), Color.RED);
            return;
        }

        setStatus(lang().get("status.loading.rooms"), Color.YELLOW);
        networkManager.listRooms();
    }

    /**
     * 创建新房间
     */
    private void createNewRoom() {
        if (isProcessing) return;

        String roomName = newRoomNameField.getText().trim();
        if (roomName.isEmpty()) {
            setStatus(lang().get("error.room.name.empty"), Color.RED);
            return;
        }
        if (roomName.length() > 30) {
            setStatus(lang().get("error.room.name.too.long"), Color.RED);
            return;
        }

        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager == null || !networkManager.isConnected()) {
            setStatus(lang().get("error.not.connected"), Color.RED);
            return;
        }

        isProcessing = true;
        setButtonsEnabled(false);
        setStatus(lang().get("status.creating.room"), Color.YELLOW);

        networkManager.createRoom(roomName);
    }

    /**
     * 加入选中的房间
     */
    private void joinSelectedRoom() {
        if (isProcessing) return;
        if (selectedRoom == null) {
            setStatus(lang().get("error.no.room.selected"), Color.RED);
            return;
        }

        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager == null || !networkManager.isConnected()) {
            setStatus(lang().get("error.not.connected"), Color.RED);
            return;
        }

        isProcessing = true;
        setButtonsEnabled(false);
        setStatus(lang().get("status.joining.room"), Color.YELLOW);

        networkManager.joinRoom(selectedRoom.getId());
    }

    /**
     * 断开服务器连接
     */
    private void disconnectFromServer() {
        NetworkManager networkManager = uiManager.getNetworkManager();
        if (networkManager != null) {
            networkManager.disconnect();
        }
        uiManager.popState();
    }

    /**
     * 更新房间列表显示
     */
    private void updateRoomListDisplay(List<RoomMessage.RoomInfo> rooms) {
        availableRooms.clear();
        selectedRoom = null;
        selectedRoomButton = null;
        joinButton.setDisabled(true);

        roomListTable.clear();

        if (rooms == null || rooms.isEmpty()) {
            roomListTable.add(new Label(lang().get("no.rooms.available"), skin)).center().row();
            setStatus(lang().get("status.no.rooms"), Color.GRAY);
            return;
        }

        availableRooms.addAll(rooms);

        // 添加表头
        Table headerTable = new Table();
        headerTable.defaults().padBottom(h(5f));
        Label headerLabel = new Label(lang().get("room.list.header"), skin);
        headerLabel.setColor(Color.CYAN);
        roomListTable.add(headerLabel).left().padBottom(h(10f)).row();

        // 添加房间按钮
        for (RoomMessage.RoomInfo room : availableRooms) {
            String buttonText = String.format("%s (%d/%d)",
                room.getName(), room.getPlayerCount(), room.getMaxPlayers());

            TextButton roomButton = new TextButton(buttonText, skin);
            roomButton.setUserObject(room);
            addCyanHoverEffect(roomButton);

            // 如果房间已满或游戏已开始，禁用按钮
            boolean isFull = room.getPlayerCount() >= room.getMaxPlayers();
            boolean isStarted = room.isStarted();
            roomButton.setDisabled(isFull || isStarted);

            if (isFull) {
                roomButton.setText(buttonText + " " + lang().get("room.full"));
            } else if (isStarted) {
                roomButton.setText(buttonText + " " + lang().get("room.in.game"));
            }

            roomButton.addListener(event -> {
                if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                    selectRoom(room, roomButton);
                }
                return true;
            });

            roomListTable.add(roomButton).padBottom(h(5f)).row();
        }

        setStatus(lang().get("status.rooms.loaded").replace("%d", String.valueOf(availableRooms.size())), Color.GREEN);

        // 刷新滚动面板
        roomListScrollPane.layout();
        roomListScrollPane.setScrollPercentY(0);
    }

    /**
     * 选择房间
     */
    private void selectRoom(RoomMessage.RoomInfo room, TextButton button) {
        // 恢复之前选中按钮的颜色
        if (selectedRoomButton != null) {
            selectedRoomButton.setColor(Color.WHITE);
        }

        selectedRoom = room;
        selectedRoomButton = button;
        selectedRoomButton.setColor(Color.GREEN);

        joinButton.setDisabled(false);
        setStatus(lang().get("room.selected").replace("%s", room.getName()), Color.CYAN);
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
        joinButton.setDisabled(!enabled || selectedRoom == null);
        createButton.setDisabled(!enabled);
        refreshButton.setDisabled(!enabled);
        disconnectButton.setDisabled(!enabled);
    }

    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    // ==================== NetworkListener 回调 ====================

    @Override
    public void onConnectResponse(boolean success, String message, String clientId) {
        // 在此界面不处理连接响应
    }

    @Override
    public void onRoomResponse(RoomMessage message) {
        System.out.println("[RoomListState] onRoomResponse called, action: " + message.getAction());

        switch (message.getAction()) {
            case LIST:
                // 房间列表更新
                if (message.isSuccess()) {
                    updateRoomListDisplay(message.getRooms());
                } else {
                    setStatus(lang().get("error.failed.to.load.rooms"), Color.RED);
                }
                isProcessing = false;
                setButtonsEnabled(true);
                break;

            case CREATE:
                // 创建房间响应
                System.out.println("[RoomListState] CREATE response received, success: " + message.isSuccess());
                if (message.isSuccess()) {
                    setStatus(lang().get("status.room.created"), Color.GREEN);
                    // 进入房间大厅（false 表示不是本地服务器模式）
                    NetworkManager networkManager = uiManager.getNetworkManager();
                    RoomLobbyState roomLobby = new RoomLobbyState(uiManager, networkManager, false);
                    roomLobby.setRoomInfo(
                        message.getRoomName() != null ? message.getRoomName() : lang().get("default.room.name"),
                        4,
                        true // 创建者是房主
                    );
                    uiManager.pushState(roomLobby);
                } else {
                    setStatus(lang().get("error.failed.to.create.room").replace("%s", message.getMessage()), Color.RED);
                    isProcessing = false;
                    setButtonsEnabled(true);
                }
                break;

            case JOIN:
                // 加入房间响应
                System.out.println("[RoomListState] JOIN response received, success: " + message.isSuccess());
                if (message.isSuccess()) {
                    setStatus(lang().get("status.joined.room"), Color.GREEN);
                    // 进入房间大厅（false 表示不是本地服务器模式）
                    NetworkManager networkManager = uiManager.getNetworkManager();
                    RoomLobbyState roomLobby = new RoomLobbyState(uiManager, networkManager, false);
                    roomLobby.setRoomInfo(
                        message.getRoomName() != null ? message.getRoomName() : lang().get("default.room.name"),
                        4,
                        message.isHost()
                    );
                    uiManager.pushState(roomLobby);
                } else {
                    setStatus(lang().get("error.failed.to.join.room").replace("%s", message.getMessage()), Color.RED);
                    isProcessing = false;
                    setButtonsEnabled(true);
                }
                break;

            default:
                break;
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
        // 返回上一级
        uiManager.popState();
    }

    @Override
    public void onNotification(NotificationMessage message) {
        // 处理通知
        switch (message.getNotificationType()) {
            case DISCONNECTED:
                setStatus(lang().get("status.disconnected.server"), Color.RED);
                uiManager.popState();
                break;
            default:
                if (message.getMessage() != null) {
                    setStatus(message.getMessage(), Color.ORANGE);
                }
                break;
        }
    }
}
