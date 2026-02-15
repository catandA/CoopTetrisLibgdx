package me.catand.cooptetris.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VersionManager {
    private static final String VERSION = "1.0.0";
    private static final long BUILD_TIME = System.currentTimeMillis();
    private static final String BUILD_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

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
        return VERSION;
    }

    public long getBuildTime() {
        return BUILD_TIME;
    }

    public String getFormattedBuildTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(BUILD_TIME_FORMAT);
        return sdf.format(new Date(BUILD_TIME));
    }

    public String getVersionInfo() {
        return "v" + VERSION + "\n" + getFormattedBuildTime();
    }
}
