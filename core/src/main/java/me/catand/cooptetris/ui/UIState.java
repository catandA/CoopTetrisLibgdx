package me.catand.cooptetris.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public interface UIState {
    void show(Stage stage, Skin skin);

    void hide();

    void update(float delta);

    void resize(int width, int height);

    void dispose();
}
