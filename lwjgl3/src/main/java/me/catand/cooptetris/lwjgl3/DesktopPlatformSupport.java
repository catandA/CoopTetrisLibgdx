package me.catand.cooptetris.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.catand.cooptetris.util.PlatformSupport;
import me.catand.cooptetris.util.Point;

public class DesktopPlatformSupport extends PlatformSupport {

    //we recall previous window sizes as a workaround to not save maximized size to settings
    //have to do this as updateDisplaySize is called before maximized is set =S
    protected static Point[] previousSizes = null;

    @Override
    public void updateDisplaySize() {
        // 读取和保存窗口分辨率配置
        if (previousSizes == null){
            previousSizes = new Point[2];
            previousSizes[1] = me.catand.cooptetris.util.TetrisSettings.windowResolution();
        } else {
            previousSizes[1] = previousSizes[0];
        }
        previousSizes[0] = new Point(me.catand.cooptetris.Main.width, me.catand.cooptetris.Main.height);
        // 只在非全屏模式下保存窗口分辨率
        if (!me.catand.cooptetris.util.TetrisSettings.fullscreen()) {
            me.catand.cooptetris.util.TetrisSettings.windowResolution(previousSizes[0].x, previousSizes[0].y);
        }
    }

    private static boolean first = true;

    @Override
    public void updateSystemUI() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                // 读取全屏配置
                boolean isFullscreen = me.catand.cooptetris.util.TetrisSettings.fullscreen();
                if (isFullscreen){
                    int monitorNum = 0;
                    if (!first){
                        com.badlogic.gdx.Graphics.Monitor[] monitors = Gdx.graphics.getMonitors();
                        for (int i = 0; i < monitors.length; i++){
                            if (((com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3Monitor)Gdx.graphics.getMonitor()).getMonitorHandle()
                                    == ((com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3Monitor)monitors[i]).getMonitorHandle()) {
                                monitorNum = i;
                            }
                        }
                    }
                    
                    com.badlogic.gdx.Graphics.Monitor[] monitors = Gdx.graphics.getMonitors();
                    if (monitors.length <= monitorNum) {
                        monitorNum = 0;
                    }
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode(monitors[monitorNum]));
                } else {
                    me.catand.cooptetris.util.Point p = me.catand.cooptetris.util.TetrisSettings.windowResolution();
                    Gdx.graphics.setWindowedMode(p.x, p.y);
                }
                first = false;
            }
        });
    }

    @Override
    public boolean connectedToUnmeteredNetwork() {
        return true; //no easy way to check this in desktop, just assume user doesn't care
    }

    /* FONT SUPPORT */

    //custom pixel font, for use with Latin and Cyrillic languages
    private static FreeTypeFontGenerator basicFontGenerator;
    //droid sans fallback, for asian fonts
    private static FreeTypeFontGenerator asianFontGenerator;

    @Override
    public void setupFontGenerators(int pageSize) {
        //don't bother doing anything if nothing has changed
        if (fonts != null && this.pageSize == pageSize) {
            return;
        }
        this.pageSize = pageSize;

        resetGenerators(false);
        fonts = new HashMap<>();

        basicFontGenerator = asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));

        fonts.put(basicFontGenerator, new HashMap<>());
        fonts.put(asianFontGenerator, new HashMap<>());

        packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
    }

    private static final Matcher asianMatcher = Pattern.compile("\\p{InHangul_Syllables}|" +
        "\\p{InCJK_Unified_Ideographs}|\\p{InCJK_Symbols_and_Punctuation}|\\p{InHalfwidth_and_Fullwidth_Forms}|" +
        "\\p{InHiragana}|\\p{InKatakana}").matcher("");

    @Override
    protected FreeTypeFontGenerator getGeneratorForString(String input) {
        if (asianMatcher.reset(input).find()) {
            return asianFontGenerator;
        } else {
            return basicFontGenerator;
        }
    }

    //splits on newline (for layout), chinese/japanese (for font choice), and '_'/'**' (for highlighting)
    private final Pattern regularsplitter = Pattern.compile(
        "(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|" +
            "(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
            "(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
            "(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
            "(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");

    //additionally splits on spaces, so that each word can be laid out individually
    private final Pattern regularsplitterMultiline = Pattern.compile(
        "(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|" +
            "(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
            "(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
            "(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
            "(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");

    @Override
    public String[] splitforTextBlock(String text, boolean multiline) {
        if (multiline) {
            return regularsplitterMultiline.split(text);
        } else {
            return regularsplitter.split(text);
        }
    }
}
