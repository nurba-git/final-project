package com.narxoz.darkrealm.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.DarkRealmGame;

public class MainMenuScreen implements Screen {

    private final DarkRealmGame game;
    private float animTimer = 0f;

    public MainMenuScreen(DarkRealmGame game) { this.game = game; }

    @Override
    public void render(float delta) {
        animTimer += delta;
        Gdx.gl.glClearColor(0.04f, 0.02f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 24; i++) {
            float px = (float)((i * 37 + animTimer * 18) % 480);
            float py = (float)((i * 53 + animTimer * 10) % 320);
            float a  = 0.15f + 0.10f * (float)Math.sin(animTimer + i);
            game.sr.setColor(0.49f, 0.23f, 0.93f, a);
            game.sr.circle(px, py, 2f);
        }
        game.sr.setColor(0.49f, 0.23f, 0.93f, 0.7f);
        game.sr.rect(80, 250, 320, 2);
        game.sr.end();

        game.batch.begin();
        game.font.setColor(new Color(0.75f, 0.52f, 1.0f, 1f));
        game.font.draw(game.batch, "D A R K   R E A L M", 90, 290);
        game.fontSmall.setColor(new Color(0.55f, 0.40f, 0.80f, 1f));
        game.fontSmall.draw(game.batch, "2D Top-Down Action RPG", 155, 268);
        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, "CONTROLS:", 80, 220);
        game.fontSmall.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));
        game.fontSmall.draw(game.batch, "WASD / Arrows  -  Move", 80, 205);
        game.fontSmall.draw(game.batch, "Left Click / Z  -  Attack", 80, 192);
        game.fontSmall.draw(game.batch, "Right Click / X  -  Dodge (i-frames)", 80, 179);
        game.fontSmall.draw(game.batch, "Q  -  Use Skill (Shadow Strike / Void Shield / Soul Drain)", 80, 166);
        game.fontSmall.draw(game.batch, "E  -  Interact with NPC", 80, 153);
        game.fontSmall.draw(game.batch, "ESC  -  Pause", 80, 140);
        game.fontSmall.setColor(Color.WHITE);
        game.fontSmall.draw(game.batch, "STORY:", 80, 118);
        game.fontSmall.setColor(new Color(0.75f, 0.65f, 0.90f, 1f));
        game.fontSmall.draw(game.batch, "The world is consumed by shadow. You are the last warrior.", 80, 105);
        game.fontSmall.draw(game.batch, "Fight through 3 zones and defeat Malgrath, the Fallen God.", 80, 92);
        float blink = (float)Math.sin(animTimer * 3.5f);
        if (blink > 0) {
            game.font.setColor(new Color(0.98f, 0.85f, 0.30f, blink));
            game.font.draw(game.batch, "PRESS  ENTER  TO  BEGIN", 100, 55);
        }
        game.batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.justTouched()) {
            game.resetProgress();
            game.setScreen(new GameScreen(game, 1));
            dispose();
        }
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
