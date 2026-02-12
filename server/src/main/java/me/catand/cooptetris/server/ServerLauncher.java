package me.catand.cooptetris.server;

import me.catand.cooptetris.shared.server.ServerManager;

/**
 * Launches the server application.
 */
public class ServerLauncher {
    public static void main(String[] args) {
        ServerManager serverManager = new ServerManager(8080);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serverManager.stop();
        }));
    }
}
