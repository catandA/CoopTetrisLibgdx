package me.catand.cooptetris.ui;

import com.badlogic.gdx.graphics.Color;
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
 * 设置界面状态 - 现代化卡片式设计
 * <p>
 * 设计原则：
 * 1. 使用卡片式布局，每个设置类别一个卡片
 * 2. 易于扩展，添加新设置只需新增卡片
 * 3. 语言切换即时生效，自动保存
 * 4. 键位设置独立页面
 * <p>
 * 布局规范：
 * - 每个卡片使用统一的两列布局
 * - 左侧列宽 150f 放置标签
 * - 右侧列放置操作控件
 * - 确保所有操作按钮/控件在同一垂直线上
 */
public class SettingsState extends BaseUIState {

    private Table rootTable;
    private BitmapFont titleFont;
    private BitmapFont cardTitleFont;

    // 语言选择框
    private com.badlogic.gdx.scenes.scene2d.ui.SelectBox<String> languageBox;

    // 统一的标签列宽度
    private static final float LABEL_WIDTH = 150f;
    // 统一的操作控件宽度
    private static final float CONTROL_WIDTH = 180f;
    // 统一的行高
    private static final float ROW_HEIGHT = 45f;

    public SettingsState(UIManager uiManager) {
        super(uiManager);
    }

    @Override
    protected void createUI() {
        LanguageManager lang = LanguageManager.getInstance();

        // 生成字体
        titleFont = Main.platform.getFont(fontSize(32), lang.get("settings.title"), false, false);
        cardTitleFont = Main.platform.getFont(fontSize(18), "Card Title", false, false);

        // 创建根表格
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.align(Align.top);
        rootTable.padTop(h(40f));

        // 创建标题
        Label titleLabel = createTitleLabel(lang.get("settings.title"));

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
        rootTable.add(titleLabel).padBottom(h(30f)).row();
        rootTable.add(cardsTable).expandX().padBottom(h(30f)).row();
        rootTable.add(buttonTable);

        stage.addActor(rootTable);
    }

    /**
     * 创建显示设置卡片
     */
    private Table createDisplayCard() {
        LanguageManager lang = LanguageManager.getInstance();

        Table card = createCardBase();

        // 卡片标题
        Label cardTitle = createCardTitle(lang.get("settings.display.title"));

        // 全屏设置行 - 使用 TextButton 作为开关，使用统一的 createSettingRow 方法
        Table rowTable = createSettingRow(
            lang.get("settings.fullscreen"),
            () -> {
                // 使用 TextButton 模拟开关，可以完全控制缩放
                boolean isFullscreen = TetrisSettings.fullscreen();
                TextButton toggleButton = new TextButton(
                    isFullscreen ? lang.get("settings.on") : lang.get("settings.off"),
                    skin
                );
                addCyanHoverEffect(toggleButton);
                toggleButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                        boolean newFullscreenState = !TetrisSettings.fullscreen();
                        TetrisSettings.fullscreen(newFullscreenState);
                        toggleButton.setText(newFullscreenState ? lang.get("settings.on") : lang.get("settings.off"));
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

    /**
     * 创建语言设置卡片
     */
    private Table createLanguageCard() {
        LanguageManager lang = LanguageManager.getInstance();

        Table card = createCardBase();

        // 卡片标题
        Label cardTitle = createCardTitle(lang.get("settings.language.title"));

        // 语言设置行 - 使用统一的两列布局
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

    /**
     * 创建键位设置卡片
     */
    private Table createControlsCard() {
        LanguageManager lang = LanguageManager.getInstance();

        Table card = createCardBase();

        // 卡片标题
        Label cardTitle = createCardTitle(lang.get("settings.controls.title"));

        // 键位设置行 - 使用统一的两列布局
        // 描述文本放在按钮的悬停提示上
        Table rowTable = createSettingRow(
            lang.get("settings.controls"),
            () -> {
                TextButton controlsButton = new TextButton(lang.get("settings.controls.configure"), skin);
                addCyanHoverEffect(controlsButton);
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

    /**
     * 创建统一的设置行布局
     * 左侧标签列，右侧操作控件列
     *
     * @param labelText 标签文本
     * @param controlSupplier 操作控件提供者
     * @return 设置行表格
     */
    private Table createSettingRow(String labelText, java.util.function.Supplier<com.badlogic.gdx.scenes.scene2d.Actor> controlSupplier) {
        Table rowTable = new Table();

        // 左侧标签
        if (!labelText.isEmpty()) {
            Label label = new Label(labelText, skin);
            label.setColor(Color.LIGHT_GRAY);
            rowTable.add(label).left().width(w(LABEL_WIDTH)).padRight(w(15f));
        } else {
            // 空标签占位，保持对齐
            rowTable.add().left().width(w(LABEL_WIDTH)).padRight(w(15f));
        }

        // 右侧操作控件 - 所有控件统一使用 w() 和 h() 进行缩放
        com.badlogic.gdx.scenes.scene2d.Actor control = controlSupplier.get();
        rowTable.add(control).left().width(w(CONTROL_WIDTH)).height(h(ROW_HEIGHT));

        return rowTable;
    }

    /**
     * 创建卡片基础样式
     */
    private Table createCardBase() {
        Table card = new Table();
        card.setBackground(skin.newDrawable("white", new Color(0.18f, 0.18f, 0.22f, 0.9f)));
        card.pad(w(25f), h(20f), w(25f), h(20f));
        return card;
    }

    /**
     * 创建底部按钮区域
     */
    private Table createBottomButtons() {
        LanguageManager lang = LanguageManager.getInstance();

        Table buttonTable = new Table();

        // 返回按钮
        TextButton backButton = new TextButton(lang.get("back"), skin);
        addCyanHoverEffect(backButton);
        backButton.addListener(event -> {
            if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown) {
                uiManager.popState();
            }
            return true;
        });

        buttonTable.add(backButton).width(w(150f)).height(h(50f));

        return buttonTable;
    }

    /**
     * 创建标题标签
     */
    private Label createTitleLabel(String text) {
        if (titleFont != null) {
            Label.LabelStyle style = new Label.LabelStyle(titleFont, Color.WHITE);
            return new Label(text, style);
        }
        return new Label(text, skin);
    }

    /**
     * 创建卡片标题标签
     */
    private Label createCardTitle(String text) {
        if (cardTitleFont != null) {
            Label.LabelStyle style = new Label.LabelStyle(cardTitleFont, new Color(0.6f, 0.8f, 1f, 1f));
            return new Label(text, style);
        }
        Label label = new Label(text, skin);
        label.setColor(new Color(0.6f, 0.8f, 1f, 1f));
        return label;
    }

    @Override
    protected void clearUI() {
        if (rootTable != null) {
            rootTable.remove();
            rootTable = null;
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
