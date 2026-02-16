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
import me.catand.cooptetris.util.PlatformSupport;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends ApplicationAdapter {
    public static String version;
    public static int versionCode;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private UIManager uiManager;
    private GameStateManager gameStateManager;
    private NetworkManager networkManager;
    private LocalServerManager localServerManager;
    private final String[] args;
    public static PlatformSupport platform;

    // Size of the EGL surface view
    public static int width;
    public static int height;

    public Main(PlatformSupport platform) {
        this(new String[0], platform);
    }

    public Main(String[] args, PlatformSupport platform) {
        this.args = args;
        Main.platform = platform;
    }

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

        // 处理启动参数
        handleStartupParameters();

        // 显示主菜单
        uiManager.setScreen(new MainMenuState(uiManager));
    }

    /**
     * 处理启动参数，支持通过命令行参数覆盖配置
     */
    private void handleStartupParameters() {
        if (args == null) {
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
        switch (key) {
            case "playerName":
            case "name":
                TetrisSettings.playerName(value);
                System.out.println("Main: 设置玩家名称: " + value);
                break;
            case "difficulty":
                try {
                    int difficulty = Integer.parseInt(value);
                    TetrisSettings.difficulty(difficulty);
                    System.out.println("Main: 设置难度: " + difficulty);
                } catch (NumberFormatException e) {
                    System.err.println("Main: 无效的难度值: " + value);
                }
                break;
            case "language":
                TetrisSettings.language(value);
                System.out.println("Main: 设置语言: " + value);
                break;
            case "host":
                TetrisSettings.defaultHost(value);
                System.out.println("Main: 设置默认主机: " + value);
                break;
            case "port":
                try {
                    int port = Integer.parseInt(value);
                    TetrisSettings.defaultPort(port);
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
        if (width == 0 || height == 0) {
            return;
        }

        if (height != Main.height || width != Main.width) {
            Main.width = width;
            Main.height = height;
        }
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
