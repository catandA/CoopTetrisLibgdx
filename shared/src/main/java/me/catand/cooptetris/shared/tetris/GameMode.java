package me.catand.cooptetris.shared.tetris;

public enum GameMode {
    COOP("coop"),
    PVP("pvp");

    private final String value;

    GameMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GameMode fromString(String value) {
        for (GameMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return COOP;
    }
}
