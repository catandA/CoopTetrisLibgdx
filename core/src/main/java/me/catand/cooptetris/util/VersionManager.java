package me.catand.cooptetris.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VersionManager {

    private static VersionManager instance;

    private VersionManager() {
    }

    public static VersionManager getInstance() {
        if (instance == null) {
            instance = new VersionManager();
        }
        return instance;
    }

    public String getVersion() {
        return me.catand.cooptetris.Main.version;
    }

    public int getVersionCode() {
        return me.catand.cooptetris.Main.versionCode;
    }

    public String getVersionInfo() {
        return "v" + getVersion() + " (" + getVersionCode() + ")";
    }
}
