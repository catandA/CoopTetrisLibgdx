package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.util.LanguageManager;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * 设置界面状态 - 现代化暗色游戏UI风格
 */
public class SettingsState extends BaseUIState {

    private Table mainTable;
    private BitmapFont titleFont;
    private BitmapFont cardTitleFont;

    private com.badlogic.gdx.scenes.scene2d.ui.SelectBox<String> languageBox;

    // UI颜色配置
    private static final Color COLOR_BG = new Color(0.08f, 0.09f, 0.11f, 1f);
    private static final Color COLOR_PANEL = new Color(0.12f, 0.14f, 0.17f, 0.95f);
    private static final Color COLOR_PANEL_HIGHLIGHT = new Color(0.15f, 0.17f, 0.21f, 0.95f);
    private static final Color COLOR_PRIMARY = new Color(0.2f, 0.8f, 1f, 1f);
    private static final Color COLOR_SECONDARY = new Color(0.8f, 0.3f, 0.9f, 1f);
    private static final Color COLOR_SUCCESS = new Color(0.3f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_TEXT = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color COLOR_TEXT_MUTED = new Color(0.5f, 0.52f, 0.55f, 1f);

    // 统一的尺寸
    private static final float LABEL_WIDTH = 150f;
    private static final float CONTROL_WIDTH = 180f;
    private static final float ROW_HEIGHT = 45f;

    public SettingsState(UIManager uiManager) {
        super(uiManager);
    }

    @Override
    protected void createUI() {
        LanguageManager lang = LanguageManager.getInstance();

        titleFont = Main.platform.getFont(fontSize(32), lang.get("settings.title"), false, false);
        cardTitleFont = Main.platform.getFont(fontSize(18), "Card Title", false, false);

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        mainTable.pad(h(40f));

        // 创建主面板
        Table contentPanel = createMainPanel();
        mainTable.add(contentPanel).expand().center();

        stage.addActor(mainTable);
    }

    private Table createMainPanel() {
        LanguageManager lang = LanguageManager.getInstance();

        Table panel = new Table();
        panel.setBackground(createPanelBackground(COLOR_PANEL));
        panel.pad(w(40f));

        // 标题
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, COLOR_PRIMARY);
        Label titleLabel = new Label(lang.get("settings.title"), titleStyle);
        titleLabel.setAlignment(Align.center);

        // 创建设置卡片容器
        Table cardsTable = new Table();
        cardsTable.defaults().padBottom(h(20f));

        // 添加各个设置卡片
        cardsTable.add(createDisplayCard()).row();
        cardsTable.add(createLanguageCard()).row();
        cardsTable.add(createControlsCard()).row();

        // 创建底部按钮区域
        Table buttonTable = createBottomButtons();

        // 组装主界面
        panel.add(titleLabel).padBottom(h(30f)).row();
        panel.add(cardsTable).expandX().padBottom(h(30f)).row();
        panel.add(buttonTable);

        return panel;
    }

    private Table createDisplayCard() {
        LanguageManager lang = LanguageManager.getInstance();

        Table card = createCardBase();

        // 卡片标题
        Label.LabelStyle cardTitleStyle = new Label.LabelStyle(cardTitleFont, COLOR_SECONDARY);
        Label cardTitle = new Label(lang.get("settings.display.title"), cardTitleStyle);

        // 全屏设置行
        Table rowTable = createSettingRow(
            lang.get("settings.fullscreen"),
            () -> {
                boolean isFullscreen = TetrisSettings.fullscreen();
                TextButton toggleButton = new TextButton(
                    isFullscreen ? lang.get("settings.on") : lang.get("settings.off"),
                    skin
                );
                toggleButton.setColor(isFullscreen ? COLOR_SUCCESS : COLOR_TEXT_MUTED);
                toggleButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                        boolean newFullscreenState = !TetrisSettings.fullscreen();
                        TetrisSettings.fullscreen(newFullscreenState);
                        toggleButton.setText(newFullscreenState ? lang.get("settings.on") : lang.get("settings.off"));
                        toggleButton.setColor(newFullscreenState ? COLOR_SUCCESS : COLOR_TEXT_MUTED);
                        Main.platform.updateSystemUI();
                    }
                });
                return toggleButton;
            }
        );

        card.add(cardTitle).left().padBottom(h(15f)).row();
        card.add(rowTable).left().row();

        return card;
    }

    private Table createLanguageCard() {
        LanguageManager lang = LanguageManager.getInstance();

        Table card = createCardBase();

        // 卡片标题
        Label.LabelStyle cardTitleStyle = new Label.LabelStyle(cardTitleFont, COLOR_SECONDARY);
        Label cardTitle = new Label(lang.get("settings.language.title"), cardTitleStyle);

        // 语言设置行
        Table rowTable = createSettingRow(
            lang.get("language"),
            () -> {
                languageBox = new com.badlogic.gdx.scenes.scene2d.ui.SelectBox<>(skin);
                String[] languages = {lang.get("en"), lang.get("zh")};
                languageBox.setItems(languages);

                if (lang.getCurrentLanguageCode().equals("zh")) {
                    languageBox.setSelectedIndex(1);
                } else {
                    languageBox.setSelectedIndex(0);
                }

                languageBox.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                        int selectedIndex = languageBox.getSelectedIndex();
                        String newLang = (selectedIndex == 0) ? "en" : "zh";
                        lang.setLanguage(newLang);
                        TetrisSettings.language(newLang);
                        recreateUI();
                    }
                });
                return languageBox;
            }
        );

        card.add(cardTitle).left().padBottom(h(15f)).row();
        card.add(rowTable).left().row();

        return card;
    }

    private Table createControlsCard() {
        LanguageManager lang = LanguageManager.getInstance();

        Table card = createCardBase();

        // 卡片标题
        Label.LabelStyle cardTitleStyle = new Label.LabelStyle(cardTitleFont, COLOR_SECONDARY);
        Label cardTitle = new Label(lang.get("settings.controls.title"), cardTitleStyle);

        // 键位设置行
        Table rowTable = createSettingRow(
            lang.get("settings.controls"),
            () -> {
                TextButton controlsButton = new TextButton(lang.get("settings.controls.configure"), skin);
                controlsButton.setColor(COLOR_PRIMARY);
                controlsButton.addListener(event -> {
                    if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                        uiManager.pushState(new ControlsSettingsState(uiManager));
                    }
                    return true;
                });
                return controlsButton;
            }
        );

        card.add(cardTitle).left().padBottom(h(15f)).row();
        card.add(rowTable).left().row();

        return card;
    }

    private Table createSettingRow(String labelText, java.util.function.Supplier<com.badlogic.gdx.scenes.scene2d.Actor> controlSupplier) {
        Table rowTable = new Table();

        // 左侧标签
        if (!labelText.isEmpty()) {
            Label label = new Label(labelText, skin);
            label.setColor(COLOR_TEXT_MUTED);
            rowTable.add(label).left().width(w(LABEL_WIDTH)).padRight(w(15f));
        } else {
            rowTable.add().left().width(w(LABEL_WIDTH)).padRight(w(15f));
        }

        // 右侧操作控件
        com.badlogic.gdx.scenes.scene2d.Actor control = controlSupplier.get();
        rowTable.add(control).left().width(w(CONTROL_WIDTH)).height(h(ROW_HEIGHT));

        return rowTable;
    }

    private Table createCardBase() {
        Table card = new Table();
        card.setBackground(createPanelBackground(COLOR_PANEL_HIGHLIGHT));
        card.pad(w(25f), h(20f), w(25f), h(20f));
        return card;
    }

    private Table createBottomButtons() {
        LanguageManager lang = LanguageManager.getInstance();

        Table buttonTable = new Table();

        // 返回按钮
        TextButton backButton = new TextButton(lang.get("back"), skin);
        backButton.setColor(COLOR_PRIMARY);
        backButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                uiManager.popState();
            }
            return true;
        });

        buttonTable.add(backButton).width(w(150f)).height(h(50f));

        return buttonTable;
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
        if (mainTable != null) {
            mainTable.remove();
            mainTable = null;
        }
    }

    @Override
    public void update(float delta) {
        // 设置界面不需要更新逻辑
    }

    @Override
    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
        if (cardTitleFont != null) {
            cardTitleFont.dispose();
            cardTitleFont = null;
        }
    }
}
