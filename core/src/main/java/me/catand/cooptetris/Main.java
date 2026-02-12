package me.catand.cooptetris;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import me.catand.cooptetris.network.LocalServerManager;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.tetris.GameStateManager;
import me.catand.cooptetris.ui.GameState;
import me.catand.cooptetris.ui.MainMenuState;
import me.catand.cooptetris.ui.UIManager;
import me.catand.cooptetris.ui.UIState;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private UIManager uiManager;
    private GameStateManager gameStateManager;
    private NetworkManager networkManager;
    private LocalServerManager localServerManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        uiManager = new UIManager();
        gameStateManager = new GameStateManager();
        networkManager = new NetworkManager();
        localServerManager = new LocalServerManager();
        gameStateManager.setNetworkManager(networkManager);
        gameStateManager.setLocalServerManager(localServerManager);
        uiManager.setNetworkManager(networkManager);
        uiManager.setLocalServerManager(localServerManager);
        uiManager.gameStateManager = gameStateManager;

        // 显示主菜单
        uiManager.setScreen(new MainMenuState(uiManager));
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // 更新UI
        uiManager.update(Gdx.graphics.getDeltaTime());

        // 渲染游戏
        UIState currentState = uiManager.getCurrentState();
        if (currentState instanceof GameState) {
            GameState gameState = (GameState) currentState;
            gameState.renderGame(shapeRenderer);
        }

        // 渲染UI
        batch.begin();
        uiManager.render(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        uiManager.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        uiManager.dispose();
        networkManager.disconnect();
        if (localServerManager != null) {
            localServerManager.stopServer();
        }
    }
}
