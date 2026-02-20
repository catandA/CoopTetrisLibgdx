package me.catand.cooptetris.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * 触屏输入处理器 - 将屏幕分成四个区域进行控制
 *
 * 屏幕分区：
 * - 上半部分：旋转
 * - 下半部分：软降（向下移动）
 * - 左半部分：左移
 * - 右半部分：右移
 *
 * 手势：
 * - 下滑：硬降
 *
 * 注意：UI事件优先于游戏操作
 */
public class TouchInputProcessor extends InputAdapter {

    // 操作类型枚举
    public enum TouchAction {
        NONE,
        LEFT,           // 左移
        RIGHT,          // 右移
        ROTATE,         // 旋转
        SOFT_DROP,      // 软降
        HARD_DROP       // 硬降
    }

    // 屏幕分区比例（0.0 - 1.0）
    private static final float HORIZONTAL_SPLIT = 0.5f;  // 水平分割线（左右分界）
    private static final float VERTICAL_TOP_SPLIT = 0.35f;    // 垂直上部分割线
    private static final float VERTICAL_BOTTOM_SPLIT = 0.65f; // 垂直下部分割线

    // 滑动检测参数
    private static final float SWIPE_THRESHOLD = 50f;      // 滑动阈值（像素）
    private static final float SWIPE_DOWN_THRESHOLD = 80f; // 下滑阈值（需要更长距离避免误触）

    // 状态
    private Vector2 touchStartPos = new Vector2();
    private boolean isTouching = false;
    private boolean touchHandledByUI = false;
    private TouchAction currentAction = TouchAction.NONE;
    private boolean hardDropTriggered = false;

    // 动作触发控制（防止按帧重复触发）
    private boolean actionTriggered = false;  // 标记当前触摸是否已经触发过动作
    private TouchAction lastTriggeredAction = TouchAction.NONE; // 上次触发的动作

    // Stage 引用（用于检测UI事件）
    private Stage stage;

    // 软降持续状态
    private boolean isSoftDropping = false;

    public TouchInputProcessor() {
    }

    public TouchInputProcessor(Stage stage) {
        this.stage = stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0) return false; // 只处理第一个触摸点

        // 检查是否点击在UI上
        if (stage != null) {
            // 转换坐标到Stage坐标系
            Vector2 stageCoords = stage.screenToStageCoordinates(new Vector2(screenX, screenY));
            // 检查是否有Actor会处理这个事件
            if (stage.hit(stageCoords.x, stageCoords.y, true) != null) {
                touchHandledByUI = true;
                return false; // 让UI处理这个事件
            }
        }

        touchHandledByUI = false;
        isTouching = true;
        hardDropTriggered = false;
        actionTriggered = false; // 重置动作触发标记
        lastTriggeredAction = TouchAction.NONE;
        touchStartPos.set(screenX, screenY);

        // 立即检测区域并执行动作
        currentAction = detectActionArea(screenX, screenY);

        // 软降区域特殊处理：需要持续按住
        if (currentAction == TouchAction.SOFT_DROP) {
            isSoftDropping = true;
        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0) return false;

        isTouching = false;
        isSoftDropping = false;
        currentAction = TouchAction.NONE;
        actionTriggered = false;
        lastTriggeredAction = TouchAction.NONE;

        return !touchHandledByUI;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer != 0 || !isTouching || touchHandledByUI) return false;

        // 检测滑动手势
        float deltaY = screenY - touchStartPos.y;

        // 检测下滑（硬降）- 只有在非软降状态才能触发
        if (deltaY > SWIPE_DOWN_THRESHOLD && !hardDropTriggered && !isSoftDropping) {
            hardDropTriggered = true;
            currentAction = TouchAction.HARD_DROP;
            return true;
        }

        return true;
    }

    /**
     * 根据屏幕坐标检测操作区域
     * 注意：GDX屏幕坐标系Y轴向下（0在顶部）
     */
    private TouchAction detectActionArea(int screenX, int screenY) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        float normalizedX = screenX / screenWidth;
        // GDX screenY 是从顶部开始的，0在顶部，screenHeight在底部
        float normalizedY = screenY / screenHeight; // 0 = 顶部, 1 = 底部

        // 先检测上下区域
        if (normalizedY < VERTICAL_TOP_SPLIT) {
            return TouchAction.ROTATE;    // 上半部分（Y较小）- 旋转
        } else if (normalizedY > VERTICAL_BOTTOM_SPLIT) {
            return TouchAction.SOFT_DROP; // 下半部分（Y较大）- 软降
        } else {
            // 中间区域检测左右
            if (normalizedX < HORIZONTAL_SPLIT) {
                return TouchAction.LEFT;  // 左半部分 - 左移
            } else {
                return TouchAction.RIGHT; // 右半部分 - 右移
            }
        }
    }

    /**
     * 获取当前触屏操作（每帧调用一次）
     * 注意：每个动作只在触摸开始时触发一次
     */
    public TouchAction pollAction() {
        if (touchHandledByUI || !isTouching) {
            return TouchAction.NONE;
        }

        // 如果已经触发过动作，不再返回
        if (actionTriggered) {
            return TouchAction.NONE;
        }

        // 标记动作已触发
        actionTriggered = true;

        return currentAction;
    }

    /**
     * 检查是否正在软降（长按下半部分）
     */
    public boolean isSoftDropping() {
        return isSoftDropping && !touchHandledByUI;
    }

    /**
     * 检查是否正在触屏
     */
    public boolean isTouching() {
        return isTouching && !touchHandledByUI;
    }

    /**
     * 重置所有状态
     */
    public void reset() {
        isTouching = false;
        isSoftDropping = false;
        touchHandledByUI = false;
        currentAction = TouchAction.NONE;
        hardDropTriggered = false;
        actionTriggered = false;
        lastTriggeredAction = TouchAction.NONE;
    }

    /**
     * 获取当前操作区域的可读描述（用于调试）
     */
    public String getCurrentActionDescription() {
        switch (currentAction) {
            case LEFT: return "左移";
            case RIGHT: return "右移";
            case ROTATE: return "旋转";
            case SOFT_DROP: return "软降";
            case HARD_DROP: return "硬降";
            default: return "无";
        }
    }
}
