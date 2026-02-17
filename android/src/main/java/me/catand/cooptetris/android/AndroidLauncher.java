package me.catand.cooptetris.android;

import android.content.pm.PackageManager;
import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import me.catand.cooptetris.Main;
import me.catand.cooptetris.util.TetrisSettings;

/**
 * Launches the Android application.
 */
public class AndroidLauncher extends AndroidApplication {
    public static AndroidApplication instance;
    private static AndroidPlatformSupport support;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //there are some things we only need to set up on first launch
        if (instance == null) {

            instance = this;

            // 获取版本信息
            try {
                Main.version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Main.version = "???";
            }
            try {
                Main.versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                Main.versionCode = 0;
            }

            Gdx.app = this;

            // grab preferences directly using our instance first
            // so that we don't need to rely on Gdx.app, which isn't initialized yet.
            // Note that we use a different prefs name on android for legacy purposes,
            // this is the default prefs filename given to an android app (.xml is automatically added to it)
            TetrisSettings.set(instance.getPreferences("coop_tetris_settings"));
        } else {
            instance = this;
        }

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = false;
        configuration.useCompass = false;
        configuration.useAccelerometer = false;

        if (support == null) {
            support = new AndroidPlatformSupport();
        } else {
            support.reloadGenerators();
        }
        support.updateSystemUI();

        initialize(new Main(null, support), configuration);
    }
}
