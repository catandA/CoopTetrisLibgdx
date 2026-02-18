package me.catand.cooptetris.shared.server;

import com.esotericsoftware.kryonet.Connection;

import java.util.UUID;

import lombok.Data;
import me.catand.cooptetris.shared.message.NetworkMessage;

@Data
public class ClientConnection {
    private final Connection connection;
    private final String clientId;
    private String playerName;
    private String language; // 客户端语言设置
    private Room currentRoom;
    private final ServerManager serverManager;
    private boolean connected;
    private int gameLogicIndex; // 玩家对应的游戏逻辑索引（用于区分COOP/PVP模式）

    public ClientConnection(Connection connection, ServerManager serverManager) {
        this.connection = connection;
        this.serverManager = serverManager;
        this.clientId = UUID.randomUUID().toString();
        this.language = "en"; // 默认语言为英文
        this.connected = true;
        this.gameLogicIndex = -1;
    }

    public void sendMessage(NetworkMessage message) {
        try {
            connection.sendTCP(message);
        } catch (Exception e) {
            disconnect();
        }
    }

    public void disconnect() {
        if (connected) {
            connected = false;

            if (currentRoom != null) {
                currentRoom.removePlayer(this);
            }

            serverManager.removeClient(this);

            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
