package me.catand.cooptetris.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Stack;

import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.network.LocalServerManager;
import me.catand.cooptetris.util.LanguageManager;

public class UIManager {
    private final Stage stage;
    private final Skin skin;
    private final Stack<UIState> uiStates;
    private NetworkManager networkManager;
    private LocalServerManager localServerManager;
    public me.catand.cooptetris.tetris.GameStateManager gameStateManager;

    public UIManager() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // 尝试加载支持中文的字体
        try {
            // 检查是否有默认字体文件
            boolean fontLoaded = false;

            if (Gdx.files.internal("fonts/NotoSansSC-Regular.ttf").exists()) {
                // 使用高分辨率字体，提高显示质量
                BitmapFont font = generateHighResolutionFont(16, 2);
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
            }
        } catch (Exception e) {
            // 字体加载失败，使用默认字体
            e.printStackTrace();
        }

        uiStates = new Stack<>();
        Gdx.input.setInputProcessor(stage);
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

    public Stage getStage() {
        return stage;
    }

    public Skin getSkin() {
        return skin;
    }

    public UIState getCurrentState() {
        return uiStates.isEmpty() ? null : uiStates.peek();
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void setLocalServerManager(LocalServerManager localServerManager) {
        this.localServerManager = localServerManager;
    }

    public LocalServerManager getLocalServerManager() {
        return localServerManager;
    }

    /**
     * 生成指定大小的字体
     * @param size 字体大小
     * @return 生成的BitmapFont对象，如果失败则返回null
     */
    public BitmapFont generateFont(int size) {
        try {
            if (Gdx.files.internal("fonts/NotoSansSC-Regular.ttf").exists()) {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/NotoSansSC-Regular.ttf"));
                FreeTypeFontParameter parameter = new FreeTypeFontParameter();
                parameter.size = size;
                parameter.characters = LanguageManager.getAllCharacters();

                // 优化字体渲染质量
                parameter.hinting = FreeTypeFontGenerator.Hinting.Full; // 完整的字体微调
                parameter.genMipMaps = true; // 生成mipmap，提高缩放时的质量
                parameter.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear; // 缩小过滤
                parameter.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear; // 放大过滤

                BitmapFont font = generator.generateFont(parameter);
                generator.dispose();
                return font;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成高分辨率字体，用于在小尺寸下显示时保持清晰
     * @param targetSize 目标显示大小
     * @param resolutionMultiplier 分辨率倍率，建议为2或3
     * @return 生成的高分辨率BitmapFont对象，如果失败则返回null
     */
    public BitmapFont generateHighResolutionFont(int targetSize, int resolutionMultiplier) {
        try {
            if (Gdx.files.internal("fonts/NotoSansSC-Regular.ttf").exists()) {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/NotoSansSC-Regular.ttf"));
                FreeTypeFontParameter parameter = new FreeTypeFontParameter();

                // 生成更高分辨率的字体
                int actualSize = targetSize * resolutionMultiplier;
                parameter.size = actualSize;
                parameter.characters = LanguageManager.getAllCharacters();

                // 优化字体渲染质量
                parameter.hinting = FreeTypeFontGenerator.Hinting.Full;
                parameter.genMipMaps = true;
                parameter.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
                parameter.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;

                BitmapFont font = generator.generateFont(parameter);
                generator.dispose();

                // 设置字体缩放到目标大小
                font.getData().setScale(1.0f / resolutionMultiplier);

                return font;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
