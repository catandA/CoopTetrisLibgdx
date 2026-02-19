package me.catand.cooptetris.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.VersionManager;

/**
 * 主菜单状态 - 现代化暗色游戏UI风格
 */
public class MainMenuState extends BaseUIState {
    private Table mainTable;
    private BitmapFont titleFont;
    private BitmapFont subtitleFont;
    private Label versionLabel;

    // UI颜色配置
    private static final Color COLOR_BG = new Color(0.06f, 0.07f, 0.09f, 1f);
    private static final Color COLOR_PANEL = new Color(0.1f, 0.12f, 0.15f, 0.95f);
    private static final Color COLOR_PANEL_HIGHLIGHT = new Color(0.15f, 0.17f, 0.21f, 0.95f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);
    private static final Color COLOR_SECONDARY = new Color(0.8f, 0.3f, 0.9f, 1f);
    private static final Color COLOR_SUCCESS = new Color(0.3f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_WARNING = new Color(1f, 0.7f, 0.2f, 1f);
    private static final Color COLOR_DANGER = new Color(1f, 0.3f, 0.3f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color COLOR_TEXT_MUTED = new Color(0.5f, 0.52f, 0.55f, 1f);

    public MainMenuState(UIManager uiManager) {
        super(uiManager);
    }

    @Override
    protected void createUI() {
        // 创建字体
        titleFont = Main.platform.getFont(fontSize(48), lang().get("title"), false, false);
        subtitleFont = Main.platform.getFont(fontSize(16), "Cooperative Edition", false, false);

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        // 创建主面板
        Table contentPanel = createContentPanel();
        mainTable.add(contentPanel).center();

        stage.addActor(mainTable);

        // 添加版本信息
        addVersionLabel();
    }

    private Table createContentPanel() {
        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(50f));
        panel.center();

        // 游戏标题
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, COLOR_PRIMARY);
        Label titleLabel = new Label(lang().get("title"), titleStyle);
        titleLabel.setAlignment(Align.center);

        // 副标题
        Label.LabelStyle subtitleStyle = new Label.LabelStyle(subtitleFont, COLOR_SECONDARY);
        Label subtitleLabel = new Label("Cooperative Edition", subtitleStyle);
        subtitleLabel.setAlignment(Align.center);

        // 按钮区域
        Table buttonTable = new Table();
        buttonTable.center();

        // 开始游戏按钮
        TextButton startButton = createStyledButton(lang().get("start.game"), COLOR_SUCCESS);
        startButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                if (uiManager.gameStateManager != null) {
                    GameState gameState = new GameState(uiManager, uiManager.gameStateManager);
                    uiManager.setScreen(gameState);
                    uiManager.gameStateManager.startSinglePlayer();
                }
            }
            return true;
        });

        // 联机模式按钮
        TextButton onlineButton = createStyledButton(lang().get("online.mode"), COLOR_PRIMARY);
        onlineButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                uiManager.pushState(new ServerConnectionState(uiManager));
            }
            return true;
        });

        // 设置按钮
        TextButton settingsButton = createStyledButton(lang().get("settings"), COLOR_WARNING);
        settingsButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                uiManager.pushState(new SettingsState(uiManager));
            }
            return true;
        });

        // 退出按钮
        TextButton exitButton = createStyledButton(lang().get("exit"), COLOR_DANGER);
        exitButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                Gdx.app.exit();
            }
            return true;
        });

        // 组装按钮
        float buttonWidth = w(220f);
        float buttonHeight = h(55f);
        float buttonSpacing = h(15f);

        buttonTable.add(startButton).width(buttonWidth).height(buttonHeight).padBottom(buttonSpacing).row();
        buttonTable.add(onlineButton).width(buttonWidth).height(buttonHeight).padBottom(buttonSpacing).row();
        buttonTable.add(settingsButton).width(buttonWidth).height(buttonHeight).padBottom(buttonSpacing).row();
        buttonTable.add(exitButton).width(buttonWidth).height(buttonHeight);

        // 组装主面板
        panel.add(titleLabel).padBottom(h(5f)).row();
        panel.add(subtitleLabel).padBottom(h(40f)).row();
        panel.add(buttonTable).center();

        return panel;
    }

    private TextButton createStyledButton(String text, Color color) {
        TextButton button = new TextButton(text, skin);
        button.setColor(color);
        return button;
    }

    private void addVersionLabel() {
        VersionManager versionManager = VersionManager.getInstance();
        String versionInfo = versionManager.getVersionInfo();
        versionLabel = new Label(versionInfo, skin);
        versionLabel.setColor(COLOR_TEXT_MUTED);
        versionLabel.setAlignment(Align.bottomRight);
        versionLabel.setPosition(
            offsetX() + displayWidth() - versionLabel.getWidth() - w(20f),
            offsetY() + h(20f)
        );
        stage.addActor(versionLabel);
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createPanelBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(texture);
    }

    private LanguageManager lang() {
        return LanguageManager.getInstance();
    }

    @Override
    protected void clearUI() {
        if (mainTable != null) {
            mainTable.remove();
            mainTable = null;
        }
        if (versionLabel != null) {
            versionLabel.remove();
            versionLabel = null;
        }
    }

    @Override
    public void update(float delta) {
        // 主菜单不需要更新逻辑
    }

    @Override
    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
        if (subtitleFont != null) {
            subtitleFont.dispose();
            subtitleFont = null;
        }
    }
}
