package com.narxoz.darkrealm.interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

// ── IRenderable ──────────────────────────────────────────────────
/** SOLID-I: только рендер. Статичные тайлы реализуют только это. */
public interface IRenderable {
    void render(ShapeRenderer sr, SpriteBatch batch);
}
