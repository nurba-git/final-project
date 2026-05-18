package com.narxoz.darkrealm.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.DarkRealmGame;
import com.narxoz.darkrealm.screens.GameScreen;
import com.narxoz.darkrealm.screens.MainMenuScreen;

public class PauseScreen implements Screen {

    private final DarkRealmGame game;
    private final GameScreen prev;

    public PauseScreen(DarkRealmGame game, GameScreen prev) {
        this.game = game;
        this.prev = prev;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.04f, 0.02f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.sr.begin(ShapeRenderer.ShapeType.Filled);
        game.sr.setColor(0f, 0f, 0f, 0.75f);
        game.sr.rect(120, 80, 240, 160);
        game.sr.setColor(0.49f, 0.23f, 0.93f, 0.9f);
        game.sr.rect(120, 236, 240, 3);
        game.sr.end();

        game.batch.begin();
        game.font.setColor(new Color(0.75f, 0.52f, 1.0f, 1f));
        game.font.draw(game.batch, "PAUSED", 185, 250);
        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, "ESC / R  -  Resume", 165, 210);
        game.fontSmall.draw(game.batch, "M        -  Main Menu", 165, 190);
        game.batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.setScreen(prev);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
