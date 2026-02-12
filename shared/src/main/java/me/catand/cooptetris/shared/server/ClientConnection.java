package me.catand.cooptetris.shared.server;

import me.catand.cooptetris.shared.message.NetworkMessage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public class ClientConnection implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String clientId;
    private String playerName;
    private Room currentRoom;
    private ServerManager serverManager;
    private boolean connected;
    
    public ClientConnection(Socket socket, ServerManager serverManager) {
        this.socket = socket;
        this.serverManager = serverManager;
        this.clientId = UUID.randomUUID().toString();
        this.connected = true;
        
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }
    }
    
    @Override
    public void run() {
        while (connected) {
            try {
                NetworkMessage message = (NetworkMessage) in.readObject();
                serverManager.handleMessage(this, message);
            } catch (Exception e) {
                disconnect();
            }
        }
    }
    
    public void sendMessage(NetworkMessage message) {
        try {
            out.writeObject(message);
            out.flush();
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
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
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
}
