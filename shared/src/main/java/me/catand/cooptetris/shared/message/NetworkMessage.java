package me.catand.cooptetris.shared.message;

import java.io.Serializable;

public abstract class NetworkMessage implements Serializable {
    private String type;
    
    public NetworkMessage(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
}
