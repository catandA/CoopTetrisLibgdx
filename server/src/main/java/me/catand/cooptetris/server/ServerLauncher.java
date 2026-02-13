package me.catand.cooptetris.server;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.utils.Clipboard;
import me.catand.cooptetris.shared.server.ServerManager;

/**
 * Launches the server application.
 */
public class ServerLauncher {
    public static void main(String[] args) {
        // 启动服务器
        ServerManager serverManager = new ServerManager(8080);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serverManager.stop();
        }));
        
        System.out.println("ServerLauncher: 服务器启动完成，监听端口 8080");
    }
}
