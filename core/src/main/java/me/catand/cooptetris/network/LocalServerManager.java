package me.catand.cooptetris.network;

import me.catand.cooptetris.shared.server.ServerManager;

public class LocalServerManager {
    private ServerManager serverManager;
    private Thread serverThread;
    private boolean running;
    private String roomId;

    public boolean startServer(int port) {
        if (!running) {
            try {
                serverThread = new Thread(() -> {
                    // 创建本地服务器，使用LOCAL_SERVER类型
                    serverManager = new ServerManager(port, ServerManager.ServerType.LOCAL_SERVER);
                    running = true;
                    // 本地服务器启动后，默认创建一个房间
                    // 这个房间将作为唯一的游戏房间
                });
                serverThread.start();
                // 等待服务器启动
                Thread.sleep(1000);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public void stopServer() {
        if (running) {
            running = false;
            if (serverManager != null) {
                serverManager.stop();
            }
            if (serverThread != null) {
                try {
                    serverThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 重置所有变量，确保可以重新启动服务器
            serverManager = null;
            serverThread = null;
            roomId = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public String getRoomId() {
        return roomId;
    }
}
