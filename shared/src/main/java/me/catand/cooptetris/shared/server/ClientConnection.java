package me.catand.cooptetris.shared.server;

import com.esotericsoftware.kryonet.Connection;

import java.util.UUID;

import me.catand.cooptetris.shared.message.NetworkMessage;

public class ClientConnection {
    private final Connection connection;
    private final String clientId;
    private String playerName;
    private Room currentRoom;
    private final ServerManager serverManager;
    private boolean connected;

    public ClientConnection(Connection connection, ServerManager serverManager) {
        this.connection = connection;
        this.serverManager = serverManager;
        this.clientId = UUID.randomUUID().toString();
        this.connected = true;
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

    public String getClientId() {
        return clientId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public boolean isConnected() {
        return connected;
    }

    public Connection getConnection() {
        return connection;
    }
}
