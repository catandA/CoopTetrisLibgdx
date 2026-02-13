package me.catand.cooptetris.network;

import me.catand.cooptetris.shared.server.ServerManager;

public class LocalServerManager {
    private ServerManager serverManager;
    private Thread serverThread;
    private boolean running;
    private String roomId;
    private int actualPort;

    public int startServer(int startPort) {
        if (!running) {
            // 尝试从startPort开始的多个端口
            for (int port = startPort; port < startPort + 10; port++) {
                try {
                    final int finalPort = port;
                    serverThread = new Thread(() -> {
                        try {
                            // 创建本地服务器，使用LOCAL_SERVER类型
                            serverManager = new ServerManager(finalPort, ServerManager.ServerType.LOCAL_SERVER);
                            running = true;
                            actualPort = finalPort;
                            // 本地服务器启动后，默认创建一个房间
                            // 这个房间将作为唯一的游戏房间
                        } catch (Exception e) {
                            running = false;
                            serverManager = null;
                        }
                    });
                    serverThread.start();
                    // 等待服务器启动
                    Thread.sleep(1000);

                    if (running && serverManager != null) {
                        System.out.println("LocalServerManager: 服务器成功启动，端口: " + port);
                        return port;
                    }
                } catch (Exception e) {
                    System.out.println("LocalServerManager: 尝试端口 " + port + " 失败: " + e.getMessage());
                }
            }
            return -1; // 所有端口都失败
        }
        return actualPort;
    }

    public int getActualPort() {
        return actualPort;
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
