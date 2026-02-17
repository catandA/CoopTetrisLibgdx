package me.catand.cooptetris.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import me.catand.cooptetris.util.UIScaler;

/**
 * UI状态基类，提供统一的缩放处理
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
    }

    @Override
    public void hide() {
        // 子类实现
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
        if (stage != null && skin != null) {
            hide();
            show(stage, skin);
        }
    }

    @Override
    public void dispose() {
        // 子类实现
    }

    /**
     * 将设计时的X坐标转换为实际屏幕X坐标
     */
    protected float toScreenX(float designX) {
        return scaler.toScreenX(designX);
    }

    /**
     * 将设计时的Y坐标转换为实际屏幕Y坐标
     */
    protected float toScreenY(float designY) {
        return scaler.toScreenY(designY);
    }

    /**
     * 将设计时的宽度转换为实际屏幕宽度
     */
    protected float toScreenWidth(float designWidth) {
        return scaler.toScreenWidth(designWidth);
    }

    /**
     * 将设计时的高度转换为实际屏幕高度
     */
    protected float toScreenHeight(float designHeight) {
        return scaler.toScreenHeight(designHeight);
    }

    /**
     * 获取当前的缩放比例
     */
    protected float getScale() {
        return scaler.getScale();
    }
}
