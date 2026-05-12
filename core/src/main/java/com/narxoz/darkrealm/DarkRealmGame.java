package com.narxoz.darkrealm;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.narxoz.darkrealm.screens.MainMenuScreen;
import com.narxoz.darkrealm.systems.SoundManager;

/**
 * DarkRealm — главный класс игры.
 * Screen State Machine: MainMenu → Game → Pause / GameOver → Victory → MainMenu
 * SOLID-S: только держит общие ресурсы и текущий Screen.
 */
public class DarkRealmGame extends Game {

    public SpriteBatch batch;
    public ShapeRenderer sr;
    public BitmapFont font;
    public BitmapFont fontSmall;
    public SoundManager sound;

    // Глобальное состояние прогресса (между экранами)
    public int currentZone = 1;
    public int totalKills  = 0;
    public int questsDone  = 0;

    @Override
    public void create() {
        batch     = new SpriteBatch();
        sr        = new ShapeRenderer();
        font      = new BitmapFont();
        fontSmall = new BitmapFont();
        font.getData().setScale(1.2f);
        fontSmall.getData().setScale(0.75f);
        sound     = new SoundManager();
        setScreen(new MainMenuScreen(this));
    }

    public void resetProgress() {
        currentZone = 1;
        totalKills  = 0;
        questsDone  = 0;
    }

    @Override
    public void dispose() {
        batch.dispose();
        sr.dispose();
        font.dispose();
        fontSmall.dispose();
        sound.dispose();
    }
}
