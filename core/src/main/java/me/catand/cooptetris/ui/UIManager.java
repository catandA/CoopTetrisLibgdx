package me.catand.cooptetris.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Stack;

import lombok.Getter;
import lombok.Setter;
import me.catand.cooptetris.Main;
import me.catand.cooptetris.network.LocalServerManager;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.util.UIScaler;

public class UIManager {
    @Getter
    private final Stage stage;
    @Getter
    private final Skin skin;
    private final Stack<UIState> uiStates;
    @Setter
    @Getter
    private NetworkManager networkManager;
    @Setter
    @Getter
    private LocalServerManager localServerManager;
    public me.catand.cooptetris.tetris.GameStateManager gameStateManager;

    public UIManager() {
        // 初始化UIScaler
        UIScaler.getInstance().update();

        // 使用FitViewport保持16:9的宽高比
        stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(UIScaler.TARGET_WIDTH, UIScaler.TARGET_HEIGHT));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // 尝试加载支持中文的字体
        updateSkinFonts();

        uiStates = new Stack<>();
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * 更新skin中的字体，考虑当前的缩放比例
     */
    public void updateSkinFonts() {
        try {
            // 首先初始化字体生成器
            if (Main.platform != null) {
                Main.platform.setupFontGenerators(1024);
            }

            // 获取当前缩放比例
            float scale = UIScaler.getInstance().getScale();

            // 根据缩放比例计算字体大小
            int baseSize = 16;
            int scaledSize = (int) (baseSize * scale);

            BitmapFont font = Main.platform.getFont(scaledSize, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", false, false);
            if (font != null) {
                // 替换skin中的默认字体
                skin.add("default", font, BitmapFont.class);
                skin.add("font", font, BitmapFont.class);
                skin.add("list", font, BitmapFont.class);
                skin.add("subtitle", font, BitmapFont.class);
                skin.add("window", font, BitmapFont.class);

                // 同时更新所有样式中的字体
                skin.get(Label.LabelStyle.class).font = font;
                skin.get(TextButton.TextButtonStyle.class).font = font;
                skin.get(TextField.TextFieldStyle.class).font = font;
                skin.get(SelectBox.SelectBoxStyle.class).font = font;
            }
        } catch (Exception e) {
            // 字体加载失败，使用默认字体
            e.printStackTrace();
        }
    }

    public void pushState(UIState state) {
        if (!uiStates.isEmpty()) {
            uiStates.peek().hide();
        }
        uiStates.push(state);
        state.show(stage, skin);
    }

    public void popState() {
        if (!uiStates.isEmpty()) {
            UIState currentState = uiStates.pop();
            currentState.hide();
            currentState.dispose();
            if (!uiStates.isEmpty()) {
                uiStates.peek().show(stage, skin);
            }
        }
    }

    public void setScreen(UIState state) {
        while (!uiStates.isEmpty()) {
            UIState currentState = uiStates.pop();
            currentState.hide();
            currentState.dispose();
        }
        pushState(state);
    }

    public void update(float delta) {
        if (!uiStates.isEmpty()) {
            uiStates.peek().update(delta);
        }
    }

    public void render(SpriteBatch batch) {
        stage.act();
        stage.draw();
    }

    public void resize(int width, int height) {
        // 更新UIScaler
        UIScaler.getInstance().update();

        // 更新skin中的字体大小
        updateSkinFonts();

        // 使用FitViewport的update方法，保持宽高比
        stage.getViewport().update(width, height, true);
        if (!uiStates.isEmpty()) {
            uiStates.peek().resize(width, height);
        }
    }

    public void dispose() {
        while (!uiStates.isEmpty()) {
            uiStates.pop().dispose();
        }
        stage.dispose();
        skin.dispose();
    }

    public UIState getCurrentState() {
        return uiStates.isEmpty() ? null : uiStates.peek();
    }


}
