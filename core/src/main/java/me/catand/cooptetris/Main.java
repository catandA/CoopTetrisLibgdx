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
import me.catand.cooptetris.util.ConfigManager;
import me.catand.cooptetris.util.Config;

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
    private ConfigManager configManager;
    private String[] args;

    public Main() {
        this(new String[0]);
    }

    public Main(String[] args) {
        this.args = args;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        configManager = new ConfigManager();
        uiManager = new UIManager();
        gameStateManager = new GameStateManager();
        networkManager = new NetworkManager();
        localServerManager = new LocalServerManager();
        gameStateManager.setNetworkManager(networkManager);
        gameStateManager.setLocalServerManager(localServerManager);
        uiManager.setNetworkManager(networkManager);
        uiManager.setLocalServerManager(localServerManager);
        uiManager.gameStateManager = gameStateManager;
        uiManager.setConfigManager(configManager);

        // 处理启动参数
        handleStartupParameters();

        // 显示主菜单
        uiManager.setScreen(new MainMenuState(uiManager));
    }

    /**
     * 处理启动参数，支持通过命令行参数覆盖配置
     */
    private void handleStartupParameters() {
        if (args == null || args.length == 0) {
            return;
        }

        // 解析参数
        for (String arg : args) {
            if (arg.startsWith("--")) {
                // 处理长参数
                String[] parts = arg.substring(2).split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];
                    handleParameter(key, value);
                }
            } else if (arg.startsWith("-")) {
                // 处理短参数
                String[] parts = arg.substring(1).split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];
                    handleParameter(key, value);
                }
            }
        }
    }

    /**
     * 处理单个参数
     */
    private void handleParameter(String key, String value) {
        Config config = configManager.getConfig();
        switch (key) {
            case "playerName":
            case "name":
                config.setPlayerName(value);
                configManager.saveSettings(config);
                System.out.println("Main: 设置玩家名称: " + value);
                break;
            case "difficulty":
                try {
                    int difficulty = Integer.parseInt(value);
                    config.setDifficulty(difficulty);
                    configManager.saveSettings(config);
                    System.out.println("Main: 设置难度: " + difficulty);
                } catch (NumberFormatException e) {
                    System.err.println("Main: 无效的难度值: " + value);
                }
                break;
            case "language":
                config.setLanguage(value);
                configManager.saveSettings(config);
                System.out.println("Main: 设置语言: " + value);
                break;
            case "host":
                config.setDefaultHost(value);
                configManager.saveSettings(config);
                System.out.println("Main: 设置默认主机: " + value);
                break;
            case "port":
                try {
                    int port = Integer.parseInt(value);
                    config.setDefaultPort(port);
                    configManager.saveSettings(config);
                    System.out.println("Main: 设置默认端口: " + port);
                } catch (NumberFormatException e) {
                    System.err.println("Main: 无效的端口值: " + value);
                }
                break;
        }
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
