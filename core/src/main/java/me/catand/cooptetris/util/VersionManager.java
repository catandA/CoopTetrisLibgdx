package me.catand.cooptetris.util;

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
        String fullVersion = me.catand.cooptetris.Main.version;
        // 提取版本号部分（不包含构建时间）
        if (fullVersion != null && fullVersion.contains("-")) {
            return fullVersion.split("-", 2)[0];
        }
        return fullVersion;
    }

    public String getBuildTime() {
        String fullVersion = me.catand.cooptetris.Main.version;
        // 提取构建时间部分
        if (fullVersion != null && fullVersion.contains("-")) {
            return fullVersion.split("-", 2)[1];
        }
        return null;
    }

    public int getVersionCode() {
        return me.catand.cooptetris.Main.versionCode;
    }

    public String getVersionInfo() {
        String version = getVersion();
        String buildTime = getBuildTime();
        String versionInfo = "v" + version + " (" + getVersionCode() + ")";
        if (buildTime != null) {
            versionInfo += "\nBuild: " + buildTime;
        }
        return versionInfo;
    }
}
