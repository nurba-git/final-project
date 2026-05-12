package com.narxoz.darkrealm.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.narxoz.darkrealm.DarkRealmGame;

public class VictoryScreen implements Screen {

    private final DarkRealmGame game;
    private final int kills;
    private float timer = 0f;

    public VictoryScreen(DarkRealmGame game, int kills) {
        this.game = game;
        this.kills = kills;
    }

    @Override
    public void render(float delta) {
        timer += delta;
        Gdx.gl.glClearColor(0.04f, 0.02f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.sr.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 30; i++) {
            float px = (float)((i * 41 + timer * 22) % 480);
            float py = (float)((i * 67 + timer * 14) % 320);
            float a  = 0.3f * (float)Math.abs(Math.sin(timer * 2 + i));
            game.sr.setColor(0.98f, 0.85f, 0.30f, a);
            game.sr.circle(px, py, 2f);
        }
        game.sr.setColor(0.98f, 0.85f, 0.30f, 0.7f);
        game.sr.rect(60, 258, 360, 2);
        game.sr.end();

        game.batch.begin();
        game.font.setColor(new Color(0.98f, 0.85f, 0.30f, 1f));
        game.font.draw(game.batch, "MALGRATH  DEFEATED!", 78, 285);
        game.fontSmall.setColor(new Color(0.75f, 0.52f, 1.0f, 1f));
        game.fontSmall.draw(game.batch, "The Dark Realm is saved.", 160, 248);
        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, "Total Kills     : " + game.totalKills,   140, 210);
        game.fontSmall.draw(game.batch, "Current Kills   : " + kills,             140, 196);
        game.fontSmall.draw(game.batch, "Quests Completed: " + game.questsDone,   140, 182);
        float blink = (float)Math.sin(timer * 3f);
        if (blink > 0) {
            game.fontSmall.setColor(new Color(0.98f, 0.85f, 0.30f, blink));
            game.fontSmall.draw(game.batch, "ENTER - Play Again    M - Main Menu", 105, 130);
        }
        game.batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.resetProgress();
            game.setScreen(new GameScreen(game, 1));
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
