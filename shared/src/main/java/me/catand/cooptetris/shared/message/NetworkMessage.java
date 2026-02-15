package me.catand.cooptetris.shared.message;

import java.io.Serializable;
import lombok.Getter;

public abstract class NetworkMessage implements Serializable {
    @Getter
    private final String type;

    public NetworkMessage(String type) {
        this.type = type;
    }
}
