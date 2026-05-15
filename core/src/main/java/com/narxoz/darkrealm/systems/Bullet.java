package com.narxoz.darkrealm.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.C;

import java.util.ArrayList;
import java.util.List;

public class Bullet {
    public float x, y, vx, vy, life;
    public int   damage;
    public boolean fromPlayer, alive, magic;
    public Color color = new Color();

    public void init(float x, float y, float angle, float speed,
                     int dmg, boolean fp, boolean magic, Color col) {
        this.x = x; this.y = y;
        this.vx = (float)Math.cos(angle) * speed;
        this.vy = (float)Math.sin(angle) * speed;
        this.damage = dmg; this.fromPlayer = fp;
        this.magic = magic; this.life = C.BULLET_LIFE;
        this.alive = true; this.color.set(col);
    }

    public void update(float dt) {
        x += vx * dt; y += vy * dt; life -= dt;
        if (life <= 0) alive = false;
    }

    public void draw(ShapeRenderer sr) {
        if (!alive) return;
        if (magic) { sr.setColor(color.r, color.g, color.b, 0.3f); sr.circle(x, y, C.BULLET_RADIUS + 3); }
        sr.setColor(color);
        sr.circle(x, y, C.BULLET_RADIUS);
    }
}


