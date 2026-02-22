package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.shared.message.CountdownMessage;
import me.catand.cooptetris.shared.message.NotificationMessage;
import me.catand.cooptetris.shared.message.PlayerSlotMessage;
import me.catand.cooptetris.shared.message.RoomMessage;
import me.catand.cooptetris.shared.tetris.CoopGameLogic;
import me.catand.cooptetris.shared.tetris.GameMode;
import me.catand.cooptetris.util.LanguageManager;

/**
 * 房间大厅状态 - 现代化暗色游戏UI风格
 * 简化设计：颜色直接绑定到槽位，0=蓝, 1=红, 2=绿, 3=黄
 */
public class RoomLobbyState extends BaseUIState implements NetworkManager.NetworkListener {
    private Table mainTable;
    private Table playerListTable;
    private final NetworkManager networkManager;
    private Label statusLabel;
    private Label roomNameLabel;
    private Label serverAddressLabel;
    private Label playerCountLabel;
    private Label gameModeLabel;
    private SelectBox<String> gameModeSelectBox;
    private TextButton startGameButton;
    private TextButton leaveRoomButton;
    private BitmapFont titleFont;
    private BitmapFont smallFont;
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

    // 玩家槽位数据
    private List<PlayerSlotMessage.SlotInfo> playerSlots;
    private int mySlotIndex;

    // 观战者数据
    private boolean spectatorLocked = false;
    private int spectatorCount = 0;
    private boolean isSpectator = false;

    // UI颜色配置
    private static final Color COLOR_PANEL = new Color(0.12f, 0.14f, 0.17f, 0.95f);
    private static final Color COLOR_PANEL_HIGHLIGHT = new Color(0.15f, 0.17f, 0.21f, 0.95f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);
    private static final Color COLOR_SECONDARY = new Color(0.8f, 0.3f, 0.9f, 1f);
    private static final Color COLOR_SUCCESS = new Color(0.3f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_WARNING = new Color(1f, 0.7f, 0.2f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color TEXT_MUTED = new Color(0.5f, 0.52f, 0.55f, 1f);
    private static final Color COLOR_LOCKED = new Color(0.3f, 0.3f, 0.3f, 1f);
    private static final Color COLOR_GRAYED_OUT = new Color(0.4f, 0.4f, 0.4f, 1f);

    // 玩家可选择的颜色：蓝、红、绿、黄
    private static final Color[] PLAYER_COLORS = {
        new Color(0.2f, 0.5f, 1.0f, 1.0f),    // 0 - 蓝色
        new Color(1.0f, 0.2f, 0.2f, 1.0f),    // 1 - 红色
        new Color(0.2f, 0.8f, 0.2f, 1.0f),    // 2 - 绿色
        new Color(1.0f, 0.9f, 0.2f, 1.0f),    // 3 - 黄色
    };

    private String[] getPositionNames() {
        return new String[]{
            lang().get("slot.position.p1"),
            lang().get("slot.position.p2"),
            lang().get("slot.position.p3"),
            lang().get("slot.position.p4")
        };
    }

    public RoomLobbyState(UIManager uiManager, NetworkManager networkManager) {
        this(uiManager, networkManager, false);
    }

    public RoomLobbyState(UIManager uiManager, NetworkManager networkManager, boolean isLocalServerMode) {
        super(uiManager);
        this.networkManager = networkManager;
        this.chatMessages = new ArrayList<>();
        this.roomName = lang().get("unknown.room");
        this.maxPlayers = 4;
        this.isHost = false;
        this.countdownTimer = 0;
        this.isCountingDown = false;
        this.currentGameMode = GameMode.COOP;
        this.connectionType = isLocalServerMode ? NetworkManager.ConnectionType.LOCAL_SERVER : NetworkManager.ConnectionType.EXTERNAL_SERVER;
        this.playerSlots = new ArrayList<>();
        this.mySlotIndex = -1;
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
        if (playerCountLabel != null) {
            updatePlayerCountLabel();
        }
    }

    private void updatePlayerCountLabel() {
        int actualCount = 0;
        int displayCount = 0;
        for (PlayerSlotMessage.SlotInfo slot : playerSlots) {
            if (!slot.getPlayerName().isEmpty()) {
                actualCount++;
            }
            if (!slot.getPlayerName().isEmpty() || slot.isLocked()) {
                displayCount++;
            }
        }
        playerCountLabel.setText(displayCount + "/" + maxPlayers + " " + lang().get("players.label"));
    }

    public void updatePlayerSlots(List<PlayerSlotMessage.SlotInfo> slots, int mySlotIndex, boolean isHost) {
        this.playerSlots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
        this.mySlotIndex = mySlotIndex;
        this.isHost = isHost;

        if (playerListTable != null) {
            updatePlayerListUI();
        }
        if (playerCountLabel != null) {
            updatePlayerCountLabel();
        }
        if (startGameButton != null) {
            startGameButton.setDisabled(!isHost);
            startGameButton.setColor(isHost ? COLOR_SUCCESS : TEXT_MUTED);
        }
        if (gameModeSelectBox != null) {
            gameModeSelectBox.setDisabled(!isHost);
        }
    }

    public void updatePlayerSlots(List<PlayerSlotMessage.SlotInfo> slots, int mySlotIndex, boolean isHost,
                                   boolean spectatorLocked, int spectatorCount, boolean isSpectator) {
        this.playerSlots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
        this.mySlotIndex = mySlotIndex;
        this.isHost = isHost;
        this.spectatorLocked = spectatorLocked;
        this.spectatorCount = spectatorCount;
        this.isSpectator = isSpectator;

        if (playerListTable != null) {
            updatePlayerListUI();
        }
        if (playerCountLabel != null) {
            updatePlayerCountLabel();
        }
        if (startGameButton != null) {
            startGameButton.setDisabled(!isHost);
            startGameButton.setColor(isHost ? COLOR_SUCCESS : TEXT_MUTED);
        }
        if (gameModeSelectBox != null) {
            gameModeSelectBox.setDisabled(!isHost);
        }
    }

    private boolean needsRefresh = true;

    @Override
    public void show(Stage stage, Skin skin) {
        super.show(stage, skin);
    }

    @Override
    protected void recreateUI() {
        // resize时不需要发送状态请求
        needsRefresh = false;
        super.recreateUI();
        needsRefresh = true;
    }

    @Override
    protected void createUI() {
        titleFont = Main.platform.getFont(fontSize(28), lang().get("room.lobby"), false, false);
        smallFont = Main.platform.getFont(fontSize(14), "Players", false, false);

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        mainTable.pad(h(30f));

        Table contentPanel = createMainPanel();

        mainTable.add(contentPanel).expand().center();
        stage.addActor(mainTable);

        if (networkManager != null) {
            networkManager.addListener(this);
            // 只在非resize情况下发送状态请求
            if (needsRefresh) {
                RoomMessage statusMessage = new RoomMessage(RoomMessage.RoomAction.STATUS);
                networkManager.sendMessage(statusMessage);
            }
        }
    }

    private Table createMainPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(30f));

        Table headerTable = createHeader();
        panel.add(headerTable).fillX().padBottom(h(20f)).row();

        Table contentArea = new Table();

        Table leftPanel = createPlayerListPanel();
        contentArea.add(leftPanel).width(w(420f)).fillY().padRight(w(20f));

        Table rightPanel = createChatPanel();
        contentArea.add(rightPanel).width(w(280f)).fillY();

        panel.add(contentArea).fill().expand().padBottom(h(20f)).row();

        Table buttonArea = createButtonArea();
        panel.add(buttonArea).fillX();

        return panel;
    }

    private Table createHeader() {
        Table header = new Table();

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, COLOR_PRIMARY);
        roomNameLabel = new Label(roomName, titleStyle);
        roomNameLabel.setAlignment(Align.left);

        // 服务器地址标签（显示在房间名下方，与准备状态文字同高度）
        String serverAddressText = getServerAddressText();
        serverAddressLabel = FontUtils.createLabel(serverAddressText, skin, fontSize(14), TEXT_MUTED);
        serverAddressLabel.setAlignment(Align.left);

        statusLabel = FontUtils.createLabel(lang().get("waiting.players"), skin, fontSize(16), COLOR_WARNING);
        statusLabel.setAlignment(Align.right);

        String playerCountText = "0/" + maxPlayers + " " + lang().get("players.label");
        playerCountLabel = FontUtils.createLabel(playerCountText, skin, fontSize(16), TEXT_MUTED);
        playerCountLabel.setAlignment(Align.right);

        Table modeTable = new Table();
        gameModeLabel = FontUtils.createLabel(lang().get("game.mode.label"), skin, fontSize(16), TEXT_MUTED);

        String coopMode = lang().get("game.mode.coop");
        String pvpMode = lang().get("game.mode.pvp");
        gameModeSelectBox = FontUtils.createSelectBox(skin, fontSize(16), COLOR_TEXT, coopMode, pvpMode);
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
        modeTable.add(gameModeSelectBox).width(w(120f)).height(h(35f));

        // 左侧：房间名和服务器地址
        Table leftTable = new Table();
        leftTable.add(roomNameLabel).left().row();
        leftTable.add(serverAddressLabel).left().padTop(h(5f));

        header.add(leftTable).left().expandX();
        header.add(modeTable).padRight(w(20f));
        header.add(playerCountLabel).width(w(120f)).row();
        header.add(statusLabel).right().colspan(3).padTop(h(5f));

        return header;
    }

    /**
     * 获取服务器地址显示文本
     */
    private String getServerAddressText() {
        if (networkManager != null && networkManager.isConnected()) {
            String host = networkManager.getConnectedHost();
            int port = networkManager.getConnectedPort();
            if (host != null && port > 0) {
                return host + ":" + port;
            }
        }
        return "";
    }

    private Table createPlayerListPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL_HIGHLIGHT));
        panel.pad(w(15f));

        String playersTitle = lang().get("players.title");
        Label sectionTitle = FontUtils.createLabel(playersTitle, skin, fontSize(14), COLOR_SECONDARY);
        panel.add(sectionTitle).left().padBottom(h(10f)).row();

        // 表头
        Table headerTable = new Table();
        Label nameHeader = FontUtils.createLabel(lang().get("slot.header.player"), skin, fontSize(12), TEXT_MUTED);
        Label colorHeader = FontUtils.createLabel(lang().get("slot.header.color"), skin, fontSize(12), TEXT_MUTED);
        Label posHeader = FontUtils.createLabel(lang().get("slot.header.position"), skin, fontSize(12), TEXT_MUTED);

        headerTable.add(nameHeader).width(w(150f)).left().padRight(w(10f));
        headerTable.add(colorHeader).width(w(100f)).left().padRight(w(10f));
        headerTable.add(posHeader).width(w(120f)).left();

        panel.add(headerTable).fillX().padBottom(h(5f)).row();

        playerListTable = new Table();
        playerListTable.top().left();

        ScrollPane scrollPane = new ScrollPane(playerListTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        panel.add(scrollPane).fill().expand();

        updatePlayerListUI();

        return panel;
    }

    private void updatePlayerListUI() {
        playerListTable.clear();

        while (playerSlots.size() < 4) {
            playerSlots.add(new PlayerSlotMessage.SlotInfo(
                playerSlots.size(), "", "", -1, false, true
            ));
        }

        for (int i = 0; i < 4; i++) {
            PlayerSlotMessage.SlotInfo slot = playerSlots.get(i);
            Table slotRow = createSlotRow(slot, i);
            playerListTable.add(slotRow).fillX().padBottom(h(6f)).row();
        }

        // 添加观战者行
        Table spectatorRow = createSpectatorRow();
        playerListTable.add(spectatorRow).fillX().padTop(h(10f)).row();
    }

    private Table createSlotRow(PlayerSlotMessage.SlotInfo slot, int index) {
        Table row = new Table();
        boolean isLocked = slot.isLocked();
        boolean hasPlayer = !slot.getPlayerName().isEmpty();
        boolean isMySlot = (index == mySlotIndex);

        // 背景色
        if (isLocked) {
            row.setBackground(createPanelBackground(COLOR_LOCKED));
        } else if (hasPlayer) {
            row.setBackground(createPanelBackground(COLOR_PANEL));
        } else {
            row.setBackground(createPanelBackground(COLOR_PANEL_HIGHLIGHT));
        }

        row.pad(w(6f));

        // ========== 第一列：名字下拉菜单 ==========
        SelectBox<String> nameSelectBox = createNameSelectBox(slot, index);
        row.add(nameSelectBox).width(w(150f)).height(h(32f)).padRight(w(10f));

        // ========== 第二列：颜色选择下拉菜单 ==========
        if (hasPlayer) {
            SelectBox<String> colorSelectBox = createColorSelectBox(slot, index);
            boolean canChangeColor = !isLocked && (isHost || isMySlot);
            colorSelectBox.setDisabled(!canChangeColor);
            if (!canChangeColor) {
                colorSelectBox.setColor(COLOR_GRAYED_OUT);
            }
            row.add(colorSelectBox).width(w(100f)).height(h(32f)).padRight(w(10f));
        } else {
            // 没有玩家时，颜色列显示为空
            row.add().width(w(100f)).height(h(32f)).padRight(w(10f));
        }

        // ========== 第三列：位置下拉菜单 ==========
        SelectBox<String> positionSelectBox = createPositionSelectBox(slot, index);
        boolean canChangePosition = !isLocked && hasPlayer && (isHost || isMySlot);
        positionSelectBox.setDisabled(!canChangePosition);
        if (!canChangePosition) {
            positionSelectBox.setColor(COLOR_GRAYED_OUT);
        }
        row.add(positionSelectBox).width(w(120f)).height(h(32f));

        return row;
    }

    /**
     * 创建观战者行
     * 第一列：观战者标签（房主可锁定/解锁）
     * 第二列：当前观战人数
     * 第三列：加入/退出观战按钮
     */
    private Table createSpectatorRow() {
        Table row = new Table();
        // 根据锁定状态设置背景色
        if (spectatorLocked) {
            row.setBackground(createPanelBackground(COLOR_LOCKED));
        } else {
            row.setBackground(createPanelBackground(COLOR_PANEL_HIGHLIGHT));
        }
        row.pad(w(6f));

        // ========== 第一列：观战者标签（房主可锁定/解锁）==========
        SelectBox<String> spectatorLabelBox = createSpectatorLabelBox();
        row.add(spectatorLabelBox).width(w(150f)).height(h(32f)).padRight(w(10f));

        // ========== 第二列：观战人数 ==========
        String countText = String.format(lang().get("spectator.count.format"), spectatorCount);
        Label countLabel = FontUtils.createLabel(countText, skin, fontSize(14), spectatorLocked ? TEXT_MUTED : COLOR_TEXT);
        row.add(countLabel).width(w(100f)).height(h(32f)).padRight(w(10f));

        // ========== 第三列：加入/退出观战按钮 ==========
        TextButton spectatorButton = createSpectatorButton();
        row.add(spectatorButton).width(w(120f)).height(h(32f));

        return row;
    }

    /**
     * 创建加入/退出观战按钮
     */
    private TextButton createSpectatorButton() {
        String buttonText;
        Color buttonColor;

        if (isSpectator) {
            // 当前是观战者，显示退出观战按钮
            buttonText = lang().get("spectator.action.leave");
            buttonColor = COLOR_WARNING;
        } else {
            // 当前是普通玩家，显示加入观战按钮
            buttonText = lang().get("spectator.action.join");
            buttonColor = COLOR_PRIMARY;
        }

        TextButton button = FontUtils.createTextButton(buttonText, skin, fontSize(14), buttonColor);

        // 设置按钮是否可用
        boolean canClick = !spectatorLocked || isSpectator;
        button.setDisabled(!canClick);
        if (!canClick) {
            button.setColor(TEXT_MUTED);
        }

        button.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                if (isSpectator) {
                    // 退出观战，回到普通玩家位置
                    requestSpectator(false);
                } else {
                    // 加入观战
                    requestSpectator(true);
                }
            }
            return true;
        });

        return button;
    }

    /**
     * 创建观战者标签下拉框（房主可以锁定/解锁观战功能）
     */
    private SelectBox<String> createSpectatorLabelBox() {
        List<String> options = new ArrayList<>();

        // 判断房主自己是否是观战者
        boolean hostIsSpectator = isHost && isSpectator;

        // 显示文本（根据锁定状态显示不同文字）
        String displayText;
        if (hostIsSpectator) {
            displayText = lang().get("spectator.host.cannot.lock");
        } else if (spectatorLocked) {
            displayText = lang().get("spectator.status.locked");
        } else {
            displayText = lang().get("spectator.label");
        }

        // 添加选项
        if (isHost) {
            options.add(displayText);
            // 只有房主不是观战者时才能锁定/解锁
            if (!hostIsSpectator) {
                if (spectatorLocked) {
                    options.add(lang().get("spectator.action.unlock"));
                } else {
                    options.add(lang().get("spectator.action.lock"));
                }
            }
        } else {
            options.add(displayText);
        }

        String[] optionsArray = options.toArray(new String[0]);
        SelectBox<String> selectBox = FontUtils.createSelectBox(skin, fontSize(14), COLOR_TEXT, optionsArray);
        selectBox.setSelected(displayText);

        // 设置颜色
        if (hostIsSpectator) {
            selectBox.setColor(COLOR_WARNING);
        } else if (spectatorLocked) {
            selectBox.setColor(TEXT_MUTED);
        } else {
            selectBox.setColor(COLOR_SECONDARY);
        }

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = selectBox.getSelected();
                handleSpectatorLabelSelection(selected);
            }
        });

        return selectBox;
    }

    private void handleSpectatorLabelSelection(String selected) {
        if (isHost) {
            if (lang().get("spectator.action.lock").equals(selected) ||
                lang().get("spectator.action.unlock").equals(selected)) {
                requestSpectatorLockToggle();
            }
        }
    }

    private void requestSpectator(boolean becomeSpectator) {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.requestSpectator(becomeSpectator);
        }
    }

    private void requestSpectatorLockToggle() {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.requestSpectatorLockToggle();
        }
    }

    /**
     * 创建SelectBox样式
     */
    private SelectBox.SelectBoxStyle createSelectBoxStyle() {
        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle(skin.get(SelectBox.SelectBoxStyle.class));
        style.font = smallFont;
        style.listStyle.font = smallFont;
        return style;
    }

    private SelectBox<String> createColorSelectBox(PlayerSlotMessage.SlotInfo slot, int index) {
        // 颜色选项
        String[] colorNames = {
            lang().get("color.blue"),
            lang().get("color.red"),
            lang().get("color.green"),
            lang().get("color.yellow")
        };

        SelectBox<String> selectBox = new SelectBox<>(createSelectBoxStyle());
        selectBox.setItems(colorNames);

        // 设置当前选中的颜色
        int colorIndex = slot.getColorIndex();
        if (colorIndex < 0 || colorIndex >= 4) {
            colorIndex = index; // 默认使用槽位索引作为颜色
        }
        selectBox.setSelectedIndex(colorIndex);

        // 设置选中项的颜色
        selectBox.getStyle().fontColor = PLAYER_COLORS[colorIndex];

        // 添加监听器
        final int slotIdx = index;
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int newColorIndex = selectBox.getSelectedIndex();
                // 发送颜色更改请求到服务器
                if (networkManager != null) {
                    networkManager.requestColorChange(slotIdx, newColorIndex);
                }
                // 更新选中项的颜色
                selectBox.getStyle().fontColor = PLAYER_COLORS[newColorIndex];
            }
        });

        return selectBox;
    }

    private SelectBox<String> createNameSelectBox(PlayerSlotMessage.SlotInfo slot, int index) {
        List<String> options = new ArrayList<>();
        boolean isLocked = slot.isLocked();
        boolean hasPlayer = !slot.getPlayerName().isEmpty();
        boolean isMySlot = (index == mySlotIndex);

        // 构建显示文本
        String displayText;
        if (isLocked) {
            displayText = lang().get("slot.status.locked");
        } else if (hasPlayer) {
            displayText = slot.getPlayerName();
            if (isMySlot) {
                displayText += " " + lang().get("you.suffix");
            }
        } else {
            displayText = lang().get("slot.status.empty");
        }

        // 添加选项
        if (isHost) {
            // 房主选项
            if (isLocked) {
                options.add(displayText);
                options.add(lang().get("slot.action.unlock"));
            } else if (hasPlayer) {
                options.add(displayText);
                if (!isMySlot) {
                    options.add(lang().get("slot.action.kick"));
                    options.add(lang().get("slot.action.lock"));
                }
            } else {
                // 空槽位
                options.add(displayText);
                options.add(lang().get("slot.action.lock.slot"));
            }
        } else {
            // 非房主选项
            if (isLocked) {
                options.add(lang().get("slot.status.locked"));
            } else if (hasPlayer) {
                options.add(displayText);
            } else {
                // 空槽位
                options.add(lang().get("slot.status.empty"));
            }
        }

        String[] optionsArray = options.toArray(new String[0]);
        SelectBox<String> selectBox = FontUtils.createSelectBox(skin, fontSize(14), COLOR_TEXT, optionsArray);
        selectBox.setSelected(displayText);

        // 设置颜色
        if (isLocked) {
            selectBox.setColor(TEXT_MUTED);
        } else if (hasPlayer) {
            selectBox.setColor(isMySlot ? COLOR_PRIMARY : COLOR_TEXT);
        } else {
            selectBox.setColor(TEXT_MUTED);
        }

        final int slotIndex = index;
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = selectBox.getSelected();
                handleNameSelection(slotIndex, selected);
            }
        });

        return selectBox;
    }

    private void handleNameSelection(int slotIndex, String selected) {
        PlayerSlotMessage.SlotInfo slot = playerSlots.get(slotIndex);
        boolean isLocked = slot.isLocked();
        boolean hasPlayer = !slot.getPlayerName().isEmpty();

        if (isHost) {
            if (lang().get("slot.action.unlock").equals(selected)) {
                requestLockToggle(slotIndex);
            } else if (lang().get("slot.action.kick").equals(selected)) {
                requestKick(slotIndex);
            } else if (lang().get("slot.action.lock").equals(selected) || lang().get("slot.action.lock.slot").equals(selected)) {
                requestLockToggle(slotIndex);
            }
        }
    }

    private SelectBox<String> createPositionSelectBox(PlayerSlotMessage.SlotInfo slot, int index) {
        List<String> options = new ArrayList<>();
        boolean isLocked = slot.isLocked();
        boolean hasPlayer = !slot.getPlayerName().isEmpty();
        boolean isMySlot = (index == mySlotIndex);

        // 构建位置选项
        String[] positionNames = getPositionNames();
        for (int i = 0; i < 4; i++) {
            PlayerSlotMessage.SlotInfo targetSlot = playerSlots.get(i);
            boolean targetHasPlayer = !targetSlot.getPlayerName().isEmpty();
            boolean targetIsLocked = targetSlot.isLocked();

            String prefix = (i == index) ? "● " : "○ ";
            String option = prefix + positionNames[i];

            if (i == index) {
                // 当前位置
                options.add(option);
            } else if (targetIsLocked) {
                // 锁定位置不能选择
                if (isHost) {
                    options.add(prefix + positionNames[i] + " " + lang().get("slot.status.locked"));
                }
            } else if (targetHasPlayer) {
                // 有玩家的位置
                if (isHost) {
                    // 房主可以交换
                    String swapText = String.format(lang().get("slot.action.swap"), targetSlot.getPlayerName());
                    options.add(option + " (" + swapText + ")");
                } else if (isMySlot) {
                    // 非房主只能移动到空位
                    // 不添加此选项
                }
            } else {
                // 空位置
                if (isHost || isMySlot) {
                    options.add(option + " " + lang().get("slot.status.empty"));
                }
            }
        }

        if (options.isEmpty()) {
            options.add("● " + positionNames[index]);
        }

        String[] optionsArray = options.toArray(new String[0]);
        SelectBox<String> selectBox = FontUtils.createSelectBox(skin, fontSize(12), COLOR_TEXT, optionsArray);
        selectBox.setSelected("● " + positionNames[index]);

        final int slotIndex = index;
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = selectBox.getSelected();
                int newPosition = extractPositionIndex(selected);
                if (newPosition >= 0 && newPosition != slotIndex) {
                    requestSlotChange(newPosition);
                }
            }
        });

        return selectBox;
    }

    private int extractPositionIndex(String selected) {
        String[] positionNames = getPositionNames();
        for (int i = 0; i < positionNames.length; i++) {
            if (selected.contains(positionNames[i])) {
                return i;
            }
        }
        return -1;
    }

    private void requestSlotChange(int targetSlotIndex) {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.requestSlotChange(targetSlotIndex);
        }
    }

    private void requestLockToggle(int slotIndex) {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.requestLockToggle(slotIndex);
        }
    }

    private void requestKick(int slotIndex) {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.requestKick(slotIndex);
        }
    }

    private Table createChatPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL_HIGHLIGHT));
        panel.pad(w(15f));

        String chatTitleText = lang().get("chat.label");
        Label chatTitle = FontUtils.createLabel(chatTitleText, skin, fontSize(14), COLOR_PRIMARY);
        panel.add(chatTitle).left().padBottom(h(10f)).row();

        chatTable = new Table();
        chatTable.top().left();

        chatScrollPane = new ScrollPane(chatTable, skin);
        chatScrollPane.setFadeScrollBars(false);
        chatScrollPane.setScrollingDisabled(true, false);

        panel.add(chatScrollPane).height(h(200f)).fillX().padBottom(h(10f)).row();

        Table inputArea = new Table();

        chatInputField = FontUtils.createTextField(skin, fontSize(16), lang().get("type.message"), null);
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

        sendChatButton = FontUtils.createTextButton(lang().get("send.button"), skin, fontSize(16), COLOR_PRIMARY);
        sendChatButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                sendChatMessage();
            }
            return true;
        });

        inputArea.add(chatInputField).width(w(170f)).height(h(40f)).padRight(w(10f));
        inputArea.add(sendChatButton).width(w(70f)).height(h(40f));

        panel.add(inputArea).fillX();

        initChatMessages();

        return panel;
    }

    private Table createButtonArea() {
        Table buttonArea = new Table();

        startGameButton = FontUtils.createTextButton(lang().get("start.game.button"), skin, fontSize(18), isHost ? COLOR_SUCCESS : TEXT_MUTED);
        startGameButton.setDisabled(!isHost);
        if (!isHost) {
            startGameButton.setColor(TEXT_MUTED);
        }
        startGameButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                startGame();
            }
            return true;
        });

        leaveRoomButton = FontUtils.createTextButton(lang().get("leave.room.button"), skin, fontSize(18), new Color(1f, 0.3f, 0.3f, 1f));
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
        Label messageLabel = FontUtils.createLabel(message, skin, fontSize(14), COLOR_TEXT);
        messageLabel.setWrap(true);
        chatTable.add(messageLabel).left().width(w(250f)).padBottom(h(4f)).row();
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

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createPanelBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(texture);
    }

    private void startGame() {
        if (!isHost) {
            return;
        }

        if (networkManager != null && networkManager.isConnected()) {
            networkManager.startGame();
        }
    }

    private void leaveRoom() {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.leaveRoom();
            if (connectionType == NetworkManager.ConnectionType.LOCAL_SERVER) {
                networkManager.disconnect();
                if (uiManager.getLocalServerManager() != null && uiManager.getLocalServerManager().isRunning()) {
                    uiManager.getLocalServerManager().stopServer();
                }
                uiManager.setScreen(new MainMenuState(uiManager));
            } else {
                uiManager.popState();
            }
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
            if (startGameButton != null) {
                startGameButton.setText(lang().get("starting.countdown").replace("%d", String.valueOf((int) countdownTimer)));
            }
            if (statusLabel != null) {
                statusLabel.setText(lang().get("starting.game"));
                statusLabel.setColor(COLOR_PRIMARY);
            }
        } else {
            if (statusLabel != null) {
                if (playerSlots.isEmpty() || getActualPlayerCount() == 0) {
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

    private int getActualPlayerCount() {
        int count = 0;
        for (PlayerSlotMessage.SlotInfo slot : playerSlots) {
            if (!slot.getPlayerName().isEmpty()) {
                count++;
            }
        }
        return count;
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
    public void onPlayerSlotUpdate(PlayerSlotMessage message) {
        switch (message.getAction()) {
            case UPDATE_SLOTS:
                updatePlayerSlots(message.getSlots(), message.getMySlotIndex(), message.isHost(),
                    message.isSpectatorLocked(), message.getSpectatorCount(), message.isSpectator());
                break;
            case SLOT_ASSIGNED:
            case LOCK_CHANGED:
            case SPECTATOR_CHANGED:
                break;
        }
    }

    @Override
    public void onCountdownUpdate(CountdownMessage message) {
        if (message.isStarting()) {
            isCountingDown = true;
            countdownTimer = message.getCountdownSeconds();
            if (startGameButton != null) {
                startGameButton.setDisabled(true);
            }
        } else {
            isCountingDown = false;
            countdownTimer = 0;
        }
    }

    @Override
    public void onGameStart(me.catand.cooptetris.shared.message.GameStartMessage message) {
        isCountingDown = false;
        countdownTimer = 0;

        if (uiManager != null && uiManager.gameStateManager != null) {
            int playerIndex = message.getYourIndex();
            boolean isSpectatorMode = (playerIndex == -1);

            // 保存是否是观战者状态
            this.isSpectator = isSpectatorMode;

            if (message.getGameMode() == GameMode.COOP) {
                // 合作模式：观战者使用 playerIndex 0 观看，但禁用输入
                int actualPlayerIndex = isSpectatorMode ? 0 : playerIndex;
                uiManager.gameStateManager.startCoopMode(message.getPlayerCount(), actualPlayerIndex, message.getSeed());
                CoopGameState coopGameState = new CoopGameState(uiManager, uiManager.gameStateManager);
                // 如果是观战者，禁用输入
                if (isSpectatorMode) {
                    coopGameState.setSpectatorMode(true);
                }
                uiManager.setScreen(coopGameState);
            } else if (message.getGameMode() == GameMode.PVP) {
                // PVP模式：观战者使用 playerIndex 0 观看，但禁用输入
                int actualPlayerIndex = isSpectatorMode ? 0 : playerIndex;
                uiManager.gameStateManager.startMultiplayer(message.getPlayerCount(), actualPlayerIndex, message.getSeed());
                PVPGameState pvpGameState = new PVPGameState(uiManager, uiManager.gameStateManager);
                // 如果是观战者，禁用输入
                if (isSpectatorMode) {
                    pvpGameState.setSpectatorMode(true);
                }
                uiManager.setScreen(pvpGameState);
            } else {
                uiManager.gameStateManager.startMultiplayer(message.getPlayerCount(), playerIndex, message.getSeed());
                GameState gameState = new GameState(uiManager, uiManager.gameStateManager);
                uiManager.setScreen(gameState);
            }
        }
    }

    @Override
    public void onGameStateUpdate(me.catand.cooptetris.shared.message.GameStateMessage message) {}

    @Override
    public void onDisconnected() {
        // 显示断开连接弹窗
        if (currentNotificationDialog != null && currentNotificationDialog.isVisible()) {
            currentNotificationDialog.hide();
        }

        LanguageManager lang = LanguageManager.getInstance();
        NotificationMessage message = new NotificationMessage();
        message.setNotificationType(NotificationMessage.NotificationType.DISCONNECTED);
        message.setTitle(lang.get("notification.title.disconnected"));
        message.setMessage(lang.get("error.connection.lost"));

        currentNotificationDialog = new NotificationDialog(skin);
        currentNotificationDialog.setNotification(message);
        currentNotificationDialog.setOnCloseAction(() -> {
            currentNotificationDialog = null;
            // 停止本地服务器（如果是本地服务器模式）
            if (connectionType == NetworkManager.ConnectionType.LOCAL_SERVER) {
                if (uiManager.getLocalServerManager() != null && uiManager.getLocalServerManager().isRunning()) {
                    uiManager.getLocalServerManager().stopServer();
                }
            }
            // 断开网络连接
            if (networkManager != null) {
                networkManager.disconnect();
            }
            // 返回主菜单
            uiManager.setScreen(new MainMenuState(uiManager));
        });
        currentNotificationDialog.show(stage);
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
