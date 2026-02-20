package me.catand.cooptetris;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import me.catand.cooptetris.network.LocalServerManager;
import me.catand.cooptetris.network.NetworkManager;
import me.catand.cooptetris.tetris.GameStateManager;
import me.catand.cooptetris.ui.CoopGameState;
import me.catand.cooptetris.ui.GameState;
import me.catand.cooptetris.ui.MainMenuState;
import me.catand.cooptetris.ui.PVPGameState;
import me.catand.cooptetris.ui.UIManager;
import me.catand.cooptetris.ui.UIState;
import me.catand.cooptetris.util.PlatformSupport;
import me.catand.cooptetris.util.TetrisSettings;
import me.catand.cooptetris.util.UIScaler;

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

        // 先渲染UI（作为背景层）
        batch.begin();
        uiManager.render(batch);
        batch.end();

        // 再渲染游戏板（显示在UI之上）
        UIState currentState = uiManager.getCurrentState();
        if (currentState instanceof GameState) {
            GameState gameState = (GameState) currentState;
            gameState.renderGame(shapeRenderer);
        } else if (currentState instanceof PVPGameState) {
            PVPGameState pvpGameState = (PVPGameState) currentState;
            pvpGameState.renderGame(shapeRenderer);
        } else if (currentState instanceof CoopGameState) {
            CoopGameState coopGameState = (CoopGameState) currentState;
            coopGameState.renderGame(shapeRenderer);
        }

        // 在16:9渲染区域外绘制黑色边框
        renderBlackBars();
    }

    // 黑色纹理缓存
    private com.badlogic.gdx.graphics.Texture blackTexture;

    /**
     * 在16:9渲染区域外绘制黑色边框
     */
    private void renderBlackBars() {
        UIScaler scaler = UIScaler.getInstance();
        float offsetX = scaler.getOffsetX();
        float offsetY = scaler.getOffsetY();
        float displayWidth = scaler.getDisplayWidth();
        float displayHeight = scaler.getDisplayHeight();
        float screenWidth = scaler.getScreenWidth();
        float screenHeight = scaler.getScreenHeight();

        // 如果没有黑边（屏幕正好是16:9），则不绘制
        if (offsetX <= 0 && offsetY <= 0) {
            return;
        }

        // 延迟初始化黑色纹理
        if (blackTexture == null) {
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(0f, 0f, 0f, 1f);
            pixmap.fill();
            blackTexture = new com.badlogic.gdx.graphics.Texture(pixmap);
            pixmap.dispose();
        }

        // 设置SpriteBatch为屏幕坐标系
        batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
        batch.begin();

        // 绘制左右黑边（当屏幕比16:9宽时）
        if (offsetX > 0) {
            // 左边黑边
            batch.draw(blackTexture, 0, 0, offsetX, screenHeight);
            // 右边黑边
            batch.draw(blackTexture, screenWidth - offsetX, 0, offsetX, screenHeight);
        }

        // 绘制上下黑边（当屏幕比16:9高时）
        if (offsetY > 0) {
            // 上边黑边
            batch.draw(blackTexture, 0, screenHeight - offsetY, screenWidth, offsetY);
            // 下边黑边
            batch.draw(blackTexture, 0, 0, screenWidth, offsetY);
        }

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
        platform.updateDisplaySize();
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
