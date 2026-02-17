package me.catand.cooptetris.util;

import com.badlogic.gdx.Gdx;

import lombok.Getter;

/**
 * UI缩放工具类 - 简化版
 * <p>
 * 设计思路：
 * 1. 以1280x720为设计基准分辨率
 * 2. 所有UI元素按设计分辨率定位
 * 3. 运行时根据实际屏幕尺寸进行等比例缩放
 * 4. 不再计算黑边，UI直接铺满屏幕
 */
public class UIScaler {
    // 设计基准分辨率（16:9）
    public static final int DESIGN_WIDTH = 1280;
    public static final int DESIGN_HEIGHT = 720;

    // 当前屏幕尺寸
    @Getter
    private int screenWidth;
    @Getter
    private int screenHeight;

    // 缩放比例（以宽度为基准，保持UI元素比例）
    @Getter
    private float scaleX;

    // 缩放比例（以高度为基准）
    @Getter
    private float scaleY;

    // 统一缩放比例（取较小值确保UI完整显示）
    @Getter
    private float scale;

    private static UIScaler instance;

    private UIScaler() {
        update();
    }

    public static UIScaler getInstance() {
        if (instance == null) {
            instance = new UIScaler();
        }
        return instance;
    }

    /**
     * 更新缩放信息，当屏幕尺寸变化时调用
     */
    public void update() {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        // 计算X和Y方向的缩放比例
        scaleX = (float) screenWidth / DESIGN_WIDTH;
        scaleY = (float) screenHeight / DESIGN_HEIGHT;

        // 使用统一缩放比例（取较小值确保UI完整显示在屏幕内）
        scale = Math.min(scaleX, scaleY);
    }

    /**
     * 将设计时的X坐标转换为实际屏幕X坐标
     * 居中显示：在宽屏上左右留白，在窄屏上铺满
     */
    public float toScreenX(float designX) {
        float displayWidth = DESIGN_WIDTH * scale;
        float offsetX = (screenWidth - displayWidth) / 2;
        return offsetX + designX * scale;
    }

    /**
     * 将设计时的Y坐标转换为实际屏幕Y坐标
     * 居中显示：在超宽屏上上下留白，在窄屏上铺满
     */
    public float toScreenY(float designY) {
        float displayHeight = DESIGN_HEIGHT * scale;
        float offsetY = (screenHeight - displayHeight) / 2;
        return offsetY + designY * scale;
    }

    /**
     * 将设计时的宽度转换为实际屏幕宽度
     */
    public float toScreenWidth(float designWidth) {
        return designWidth * scale;
    }

    /**
     * 将设计时的高度转换为实际屏幕高度
     */
    public float toScreenHeight(float designHeight) {
        return designHeight * scale;
    }

    /**
     * 将设计时的字体大小转换为实际字体大小
     */
    public int toFontSize(int designFontSize) {
        return Math.round(designFontSize * scale);
    }

    /**
     * 获取实际显示区域的宽度（缩放后的设计宽度）
     */
    public float getDisplayWidth() {
        return DESIGN_WIDTH * scale;
    }

    /**
     * 获取实际显示区域的高度（缩放后的设计高度）
     */
    public float getDisplayHeight() {
        return DESIGN_HEIGHT * scale;
    }

    /**
     * 获取水平方向的偏移量（用于居中）
     */
    public float getOffsetX() {
        return (screenWidth - getDisplayWidth()) / 2;
    }

    /**
     * 获取垂直方向的偏移量（用于居中）
     */
    public float getOffsetY() {
        return (screenHeight - getDisplayHeight()) / 2;
    }
}
