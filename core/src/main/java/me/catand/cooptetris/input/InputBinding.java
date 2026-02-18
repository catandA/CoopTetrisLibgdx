package me.catand.cooptetris.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.HashMap;
import java.util.Map;

/**
 * 输入绑定枚举 - 支持键盘按键和鼠标按钮
 */
public enum InputBinding {
    // ========== 键盘按键 ==========
    // 方向键
    LEFT(Input.Keys.LEFT, "Left", "←", BindingType.KEYBOARD),
    RIGHT(Input.Keys.RIGHT, "Right", "→", BindingType.KEYBOARD),
    UP(Input.Keys.UP, "Up", "↑", BindingType.KEYBOARD),
    DOWN(Input.Keys.DOWN, "Down", "↓", BindingType.KEYBOARD),

    // 字母键
    A(Input.Keys.A, "A", "A", BindingType.KEYBOARD),
    B(Input.Keys.B, "B", "B", BindingType.KEYBOARD),
    C(Input.Keys.C, "C", "C", BindingType.KEYBOARD),
    D(Input.Keys.D, "D", "D", BindingType.KEYBOARD),
    E(Input.Keys.E, "E", "E", BindingType.KEYBOARD),
    F(Input.Keys.F, "F", "F", BindingType.KEYBOARD),
    G(Input.Keys.G, "G", "G", BindingType.KEYBOARD),
    H(Input.Keys.H, "H", "H", BindingType.KEYBOARD),
    I(Input.Keys.I, "I", "I", BindingType.KEYBOARD),
    J(Input.Keys.J, "J", "J", BindingType.KEYBOARD),
    K(Input.Keys.K, "K", "K", BindingType.KEYBOARD),
    L(Input.Keys.L, "L", "L", BindingType.KEYBOARD),
    M(Input.Keys.M, "M", "M", BindingType.KEYBOARD),
    N(Input.Keys.N, "N", "N", BindingType.KEYBOARD),
    O(Input.Keys.O, "O", "O", BindingType.KEYBOARD),
    P(Input.Keys.P, "P", "P", BindingType.KEYBOARD),
    Q(Input.Keys.Q, "Q", "Q", BindingType.KEYBOARD),
    R(Input.Keys.R, "R", "R", BindingType.KEYBOARD),
    S(Input.Keys.S, "S", "S", BindingType.KEYBOARD),
    T(Input.Keys.T, "T", "T", BindingType.KEYBOARD),
    U(Input.Keys.U, "U", "U", BindingType.KEYBOARD),
    V(Input.Keys.V, "V", "V", BindingType.KEYBOARD),
    W(Input.Keys.W, "W", "W", BindingType.KEYBOARD),
    X(Input.Keys.X, "X", "X", BindingType.KEYBOARD),
    Y(Input.Keys.Y, "Y", "Y", BindingType.KEYBOARD),
    Z(Input.Keys.Z, "Z", "Z", BindingType.KEYBOARD),

    // 数字键
    NUM_0(Input.Keys.NUM_0, "0", "0", BindingType.KEYBOARD),
    NUM_1(Input.Keys.NUM_1, "1", "1", BindingType.KEYBOARD),
    NUM_2(Input.Keys.NUM_2, "2", "2", BindingType.KEYBOARD),
    NUM_3(Input.Keys.NUM_3, "3", "3", BindingType.KEYBOARD),
    NUM_4(Input.Keys.NUM_4, "4", "4", BindingType.KEYBOARD),
    NUM_5(Input.Keys.NUM_5, "5", "5", BindingType.KEYBOARD),
    NUM_6(Input.Keys.NUM_6, "6", "6", BindingType.KEYBOARD),
    NUM_7(Input.Keys.NUM_7, "7", "7", BindingType.KEYBOARD),
    NUM_8(Input.Keys.NUM_8, "8", "8", BindingType.KEYBOARD),
    NUM_9(Input.Keys.NUM_9, "9", "9", BindingType.KEYBOARD),

    // 功能键
    F1(Input.Keys.F1, "F1", "F1", BindingType.KEYBOARD),
    F2(Input.Keys.F2, "F2", "F2", BindingType.KEYBOARD),
    F3(Input.Keys.F3, "F3", "F3", BindingType.KEYBOARD),
    F4(Input.Keys.F4, "F4", "F4", BindingType.KEYBOARD),
    F5(Input.Keys.F5, "F5", "F5", BindingType.KEYBOARD),
    F6(Input.Keys.F6, "F6", "F6", BindingType.KEYBOARD),
    F7(Input.Keys.F7, "F7", "F7", BindingType.KEYBOARD),
    F8(Input.Keys.F8, "F8", "F8", BindingType.KEYBOARD),
    F9(Input.Keys.F9, "F9", "F9", BindingType.KEYBOARD),
    F10(Input.Keys.F10, "F10", "F10", BindingType.KEYBOARD),
    F11(Input.Keys.F11, "F11", "F11", BindingType.KEYBOARD),
    F12(Input.Keys.F12, "F12", "F12", BindingType.KEYBOARD),

    // 小键盘
    NUMPAD_0(Input.Keys.NUMPAD_0, "Numpad 0", "Num0", BindingType.KEYBOARD),
    NUMPAD_1(Input.Keys.NUMPAD_1, "Numpad 1", "Num1", BindingType.KEYBOARD),
    NUMPAD_2(Input.Keys.NUMPAD_2, "Numpad 2", "Num2", BindingType.KEYBOARD),
    NUMPAD_3(Input.Keys.NUMPAD_3, "Numpad 3", "Num3", BindingType.KEYBOARD),
    NUMPAD_4(Input.Keys.NUMPAD_4, "Numpad 4", "Num4", BindingType.KEYBOARD),
    NUMPAD_5(Input.Keys.NUMPAD_5, "Numpad 5", "Num5", BindingType.KEYBOARD),
    NUMPAD_6(Input.Keys.NUMPAD_6, "Numpad 6", "Num6", BindingType.KEYBOARD),
    NUMPAD_7(Input.Keys.NUMPAD_7, "Numpad 7", "Num7", BindingType.KEYBOARD),
    NUMPAD_8(Input.Keys.NUMPAD_8, "Numpad 8", "Num8", BindingType.KEYBOARD),
    NUMPAD_9(Input.Keys.NUMPAD_9, "Numpad 9", "Num9", BindingType.KEYBOARD),

    // 特殊键
    SPACE(Input.Keys.SPACE, "Space", "Space", BindingType.KEYBOARD),
    ENTER(Input.Keys.ENTER, "Enter", "Enter", BindingType.KEYBOARD),
    ESCAPE(Input.Keys.ESCAPE, "Escape", "Esc", BindingType.KEYBOARD),
    TAB(Input.Keys.TAB, "Tab", "Tab", BindingType.KEYBOARD),
    BACKSPACE(Input.Keys.BACKSPACE, "Backspace", "Bksp", BindingType.KEYBOARD),
    DELETE(Input.Keys.FORWARD_DEL, "Delete", "Del", BindingType.KEYBOARD),
    INSERT(Input.Keys.INSERT, "Insert", "Ins", BindingType.KEYBOARD),
    HOME(Input.Keys.HOME, "Home", "Home", BindingType.KEYBOARD),
    END(Input.Keys.END, "End", "End", BindingType.KEYBOARD),
    PAGE_UP(Input.Keys.PAGE_UP, "Page Up", "PgUp", BindingType.KEYBOARD),
    PAGE_DOWN(Input.Keys.PAGE_DOWN, "Page Down", "PgDn", BindingType.KEYBOARD),

    // 修饰键
    SHIFT_LEFT(Input.Keys.SHIFT_LEFT, "L-Shift", "LShift", BindingType.KEYBOARD),
    SHIFT_RIGHT(Input.Keys.SHIFT_RIGHT, "R-Shift", "RShift", BindingType.KEYBOARD),
    CONTROL_LEFT(Input.Keys.CONTROL_LEFT, "L-Ctrl", "LCtrl", BindingType.KEYBOARD),
    CONTROL_RIGHT(Input.Keys.CONTROL_RIGHT, "R-Ctrl", "RCtrl", BindingType.KEYBOARD),
    ALT_LEFT(Input.Keys.ALT_LEFT, "L-Alt", "LAlt", BindingType.KEYBOARD),
    ALT_RIGHT(Input.Keys.ALT_RIGHT, "R-Alt", "RAlt", BindingType.KEYBOARD),

    // 符号键
    COMMA(Input.Keys.COMMA, ",", ",", BindingType.KEYBOARD),
    PERIOD(Input.Keys.PERIOD, ".", ".", BindingType.KEYBOARD),
    SLASH(Input.Keys.SLASH, "/", "/", BindingType.KEYBOARD),
    SEMICOLON(Input.Keys.SEMICOLON, ";", ";", BindingType.KEYBOARD),
    APOSTROPHE(Input.Keys.APOSTROPHE, "'", "'", BindingType.KEYBOARD),
    MINUS(Input.Keys.MINUS, "-", "-", BindingType.KEYBOARD),
    EQUALS(Input.Keys.EQUALS, "=", "=", BindingType.KEYBOARD),
    LEFT_BRACKET(Input.Keys.LEFT_BRACKET, "[", "[", BindingType.KEYBOARD),
    RIGHT_BRACKET(Input.Keys.RIGHT_BRACKET, "]", "]", BindingType.KEYBOARD),
    BACKSLASH(Input.Keys.BACKSLASH, "\\", "\\", BindingType.KEYBOARD),
    GRAVE(Input.Keys.GRAVE, "`", "`", BindingType.KEYBOARD),

    // ========== 鼠标按钮 ==========
    MOUSE_LEFT(-1, "Mouse Left", "MLeft", BindingType.MOUSE),
    MOUSE_RIGHT(-2, "Mouse Right", "MRight", BindingType.MOUSE),
    MOUSE_MIDDLE(-3, "Mouse Middle", "MMid", BindingType.MOUSE),
    MOUSE_BACK(-4, "Mouse Back", "MBack", BindingType.MOUSE),
    MOUSE_FORWARD(-5, "Mouse Forward", "MFwd", BindingType.MOUSE);

    private final int code;
    private final String displayName;
    private final String shortName;
    private final BindingType type;

    InputBinding(int code, String displayName, String shortName, BindingType type) {
        this.code = code;
        this.displayName = displayName;
        this.shortName = shortName;
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortName() {
        return shortName;
    }

    public BindingType getType() {
        return type;
    }

    public boolean isKeyboard() {
        return type == BindingType.KEYBOARD;
    }

    public boolean isMouse() {
        return type == BindingType.MOUSE;
    }

    /**
     * 检查此输入是否被按下
     */
    public boolean isPressed() {
        if (type == BindingType.KEYBOARD) {
            return Gdx.input.isKeyPressed(code);
        } else {
            return Gdx.input.isButtonPressed(getMouseButtonCode());
        }
    }

    /**
     * 检查此输入是否刚被按下（用于单次触发）
     */
    public boolean isJustPressed() {
        if (type == BindingType.KEYBOARD) {
            return Gdx.input.isKeyJustPressed(code);
        } else {
            // 鼠标按钮没有 isButtonJustPressed，需要手动检测
            // 这里简化处理，实际使用时可能需要在游戏循环中跟踪状态
            return Gdx.input.isButtonJustPressed(getMouseButtonCode());
        }
    }

    private int getMouseButtonCode() {
        switch (this) {
            case MOUSE_LEFT: return Input.Buttons.LEFT;
            case MOUSE_RIGHT: return Input.Buttons.RIGHT;
            case MOUSE_MIDDLE: return Input.Buttons.MIDDLE;
            case MOUSE_BACK: return Input.Buttons.BACK;
            case MOUSE_FORWARD: return Input.Buttons.FORWARD;
            default: return -1;
        }
    }

    // 静态查找映射
    private static final Map<Integer, InputBinding> BY_KEYCODE = new HashMap<>();
    private static final Map<String, InputBinding> BY_NAME = new HashMap<>();

    static {
        for (InputBinding binding : values()) {
            if (binding.type == BindingType.KEYBOARD) {
                BY_KEYCODE.put(binding.code, binding);
            }
            BY_NAME.put(binding.name(), binding);
        }
    }

    /**
     * 通过 keyCode 查找键盘按键绑定
     */
    public static InputBinding fromKeyCode(int keyCode) {
        return BY_KEYCODE.get(keyCode);
    }

    /**
     * 通过鼠标按钮代码查找鼠标绑定
     */
    public static InputBinding fromMouseButton(int buttonCode) {
        switch (buttonCode) {
            case Input.Buttons.LEFT: return MOUSE_LEFT;
            case Input.Buttons.RIGHT: return MOUSE_RIGHT;
            case Input.Buttons.MIDDLE: return MOUSE_MIDDLE;
            case Input.Buttons.BACK: return MOUSE_BACK;
            case Input.Buttons.FORWARD: return MOUSE_FORWARD;
            default: return null;
        }
    }

    /**
     * 通过枚举名称查找 InputBinding
     */
    public static InputBinding fromName(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return InputBinding.valueOf(name);
        } catch (IllegalArgumentException e) {
            return BY_NAME.get(name.toUpperCase());
        }
    }

    public enum BindingType {
        KEYBOARD,
        MOUSE
    }
}
