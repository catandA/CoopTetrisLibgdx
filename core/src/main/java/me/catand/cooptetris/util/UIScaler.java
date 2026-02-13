package me.catand.cooptetris.util;

import com.badlogic.gdx.Gdx;

public class UIScaler {
    // 目标分辨率（16:9）
    public static final int TARGET_WIDTH = 1280;
    public static final int TARGET_HEIGHT = 720;
    
    // 实际屏幕尺寸
    private int screenWidth;
    private int screenHeight;
    
    // 缩放比例
    private float scale;
    
    // 实际显示区域的偏移
    private float offsetX;
    private float offsetY;
    
    // 实际显示区域的大小
    private float displayWidth;
    private float displayHeight;
    
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
        
        // 计算屏幕宽高比
        float screenAspectRatio = (float) screenWidth / screenHeight;
        float targetAspectRatio = (float) TARGET_WIDTH / TARGET_HEIGHT; // 16:9 = 1.777...
        
        if (screenAspectRatio > targetAspectRatio) {
            // 屏幕比目标更宽，以高度为基准缩放
            scale = (float) screenHeight / TARGET_HEIGHT;
            displayHeight = screenHeight;
            displayWidth = TARGET_WIDTH * scale;
            offsetX = (screenWidth - displayWidth) / 2;
            offsetY = 0;
        } else {
            // 屏幕比目标更高，以宽度为基准缩放
            scale = (float) screenWidth / TARGET_WIDTH;
            displayWidth = screenWidth;
            displayHeight = TARGET_HEIGHT * scale;
            offsetX = 0;
            offsetY = (screenHeight - displayHeight) / 2;
        }
    }
    
    /**
     * 将设计时的X坐标转换为实际屏幕X坐标
     */
    public float toScreenX(float designX) {
        return offsetX + designX * scale;
    }
    
    /**
     * 将设计时的Y坐标转换为实际屏幕Y坐标
     */
    public float toScreenY(float designY) {
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
     * 获取缩放比例
     */
    public float getScale() {
        return scale;
    }
    
    /**
     * 获取实际显示区域的宽度
     */
    public float getDisplayWidth() {
        return displayWidth;
    }
    
    /**
     * 获取实际显示区域的高度
     */
    public float getDisplayHeight() {
        return displayHeight;
    }
    
    /**
     * 获取X轴偏移量
     */
    public float getOffsetX() {
        return offsetX;
    }
    
    /**
     * 获取Y轴偏移量
     */
    public float getOffsetY() {
        return offsetY;
    }
    
    /**
     * 检查点是否在实际显示区域内
     */
    public boolean isInDisplayArea(float screenX, float screenY) {
        return screenX >= offsetX && screenX <= offsetX + displayWidth &&
               screenY >= offsetY && screenY <= offsetY + displayHeight;
    }
}
