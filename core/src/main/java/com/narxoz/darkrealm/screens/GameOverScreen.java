package com.narxoz.darkrealm.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.narxoz.darkrealm.DarkRealmGame;

public class GameOverScreen implements Screen {

    private final DarkRealmGame game;
    private final int zone, kills;
    private float timer = 0f;

    public GameOverScreen(DarkRealmGame game, int zone, int kills) {
        this.game = game;
        this.zone = zone;
        this.kills = kills;
    }

    @Override
    public void render(float delta) {
        timer += delta;
        Gdx.gl.glClearColor(0.08f, 0.01f, 0.01f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.sr.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        float a = 0.04f + 0.03f * (float)Math.sin(timer * 2.5f);
        game.sr.setColor(0.8f, 0.1f, 0.1f, a);
        game.sr.rect(0, 0, 480, 320);
        game.sr.setColor(0.75f, 0.10f, 0.10f, 0.8f);
        game.sr.rect(60, 242, 360, 2);
        game.sr.end();

        game.batch.begin();
        game.font.setColor(new Color(0.93f, 0.27f, 0.27f, 1f));
        game.font.draw(game.batch, "YOU  DIED", 155, 270);
        game.fontSmall.setColor(new Color(0.85f, 0.60f, 0.60f, 1f));
        game.fontSmall.draw(game.batch, "Zone reached : " + zone + " / 3",     140, 225);
        game.fontSmall.draw(game.batch, "Total kills  : " + kills,             140, 210);
        game.fontSmall.draw(game.batch, "Total kills (all) : " + game.totalKills, 140, 195);
        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, "ENTER  -  Retry from Zone " + zone,   120, 155);
        game.fontSmall.draw(game.batch, "M      -  Main Menu",                 120, 138);
        game.batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game, zone));
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
