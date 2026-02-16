package me.catand.cooptetris.util;

import com.badlogic.gdx.Input;

import java.util.LinkedHashMap;

public class TetrisAction extends GameAction {

    protected TetrisAction(String name) {
        super(name);
    }

    //--New references to existing actions from GameAction
    public static final GameAction NONE = GameAction.NONE;
    public static final GameAction BACK = GameAction.BACK;
    //--

    public static final GameAction LEFT = new TetrisAction("left");
    public static final GameAction RIGHT = new TetrisAction("right");
    public static final GameAction DOWN = new TetrisAction("down");
    public static final GameAction ROTATE_CLOCKWISE = new TetrisAction("rotate_clockwise");
    public static final GameAction DROP = new TetrisAction("drop");

    private static final LinkedHashMap<Integer, GameAction> defaultBindings = new LinkedHashMap<>();

    static {
        // 第一套控制键位
        defaultBindings.put(Input.Keys.LEFT, TetrisAction.LEFT);
        defaultBindings.put(Input.Keys.RIGHT, TetrisAction.RIGHT);
        defaultBindings.put(Input.Keys.DOWN, TetrisAction.DOWN);
        defaultBindings.put(Input.Keys.UP, TetrisAction.ROTATE_CLOCKWISE);
        defaultBindings.put(Input.Keys.SPACE, TetrisAction.DROP);

        // 第二套控制键位
        defaultBindings.put(Input.Keys.A, TetrisAction.LEFT);
        defaultBindings.put(Input.Keys.D, TetrisAction.RIGHT);
        defaultBindings.put(Input.Keys.S, TetrisAction.DOWN);
        defaultBindings.put(Input.Keys.W, TetrisAction.ROTATE_CLOCKWISE);
        defaultBindings.put(Input.Keys.SPACE, TetrisAction.DROP);
    }

    public static LinkedHashMap<Integer, GameAction> getDefaults() {
        return new LinkedHashMap<>(defaultBindings);
    }

}
