package me.catand.cooptetris.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import me.catand.cooptetris.util.UIScaler;

/**
 * UI状态基类 - 简化版
 * <p>
 * 提供统一的缩放处理方法，所有子类按1280x720设计分辨率布局，
 * 通过UIScaler自动转换为实际屏幕坐标
 */
public abstract class BaseUIState implements UIState {
    protected Stage stage;
    protected Skin skin;
    protected UIManager uiManager;
    protected UIScaler scaler;

    public BaseUIState(UIManager uiManager) {
        this.uiManager = uiManager;
        this.scaler = UIScaler.getInstance();
    }

    @Override
    public void show(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
        // 确保UIScaler已更新
        scaler.update();

        // 创建UI元素
        createUI();
    }

    /**
     * 创建UI元素 - 子类实现
     * 使用设计分辨率（1280x720）进行布局
     */
    protected abstract void createUI();

    /**
     * 重新创建UI - 在resize时调用
     */
    protected void recreateUI() {
        clearUI();
        createUI();
    }

    /**
     * 清除UI元素 - 子类可覆盖
     */
    protected void clearUI() {
        // 默认实现为空，子类根据需要覆盖
    }

    @Override
    public void hide() {
        clearUI();
    }

    @Override
    public void update(float delta) {
        // 子类实现
    }

    @Override
    public void resize(int width, int height) {
        // 更新UIScaler
        scaler.update();
        // 重新创建UI元素，确保正确缩放
        recreateUI();
    }

    @Override
    public void dispose() {
        // 子类实现
    }

    // ============ 缩放辅助方法 ============

    /**
     * 将设计时的X坐标转换为实际屏幕X坐标
     */
    protected float x(float designX) {
        return scaler.toScreenX(designX);
    }

    /**
     * 将设计时的Y坐标转换为实际屏幕Y坐标
     */
    protected float y(float designY) {
        return scaler.toScreenY(designY);
    }

    /**
     * 将设计时的宽度转换为实际屏幕宽度
     */
    protected float w(float designWidth) {
        return scaler.toScreenWidth(designWidth);
    }

    /**
     * 将设计时的高度转换为实际屏幕高度
     */
    protected float h(float designHeight) {
        return scaler.toScreenHeight(designHeight);
    }

    /**
     * 将设计时的字体大小转换为实际字体大小
     */
    protected int fontSize(int designFontSize) {
        return scaler.toFontSize(designFontSize);
    }

    /**
     * 获取当前的统一缩放比例
     */
    protected float scale() {
        return scaler.getScale();
    }

    /**
     * 获取设计基准宽度
     */
    protected float designWidth() {
        return UIScaler.DESIGN_WIDTH;
    }

    /**
     * 获取设计基准高度
     */
    protected float designHeight() {
        return UIScaler.DESIGN_HEIGHT;
    }

    /**
     * 获取实际显示区域宽度
     */
    protected float displayWidth() {
        return scaler.getDisplayWidth();
    }

    /**
     * 获取实际显示区域高度
     */
    protected float displayHeight() {
        return scaler.getDisplayHeight();
    }

    /**
     * 获取水平居中偏移量
     */
    protected float offsetX() {
        return scaler.getOffsetX();
    }

    /**
     * 获取垂直居中偏移量
     */
    protected float offsetY() {
        return scaler.getOffsetY();
    }
}
