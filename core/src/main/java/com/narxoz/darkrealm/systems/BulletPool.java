package com.narxoz.darkrealm.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class BulletPool {
    private final List<Bullet> pool   = new ArrayList<>();
    private final List<Bullet> active = new ArrayList<>();
    private int[][] map;

    public BulletPool(int[][] map) {
        this.map = map;
        for (int i = 0; i < 80; i++) pool.add(new Bullet());
    }

    public void setMap(int[][] m) { map = m; }

    public void spawn(float x, float y, float angle, float speed,
                      int dmg, boolean fp, boolean magic, Color col) {
        Bullet b = pool.isEmpty() ? new Bullet() : pool.remove(pool.size() - 1);
        b.init(x, y, angle, speed, dmg, fp, magic, col);
        active.add(b);
    }

    public void spawnSpread(float x, float y, float base, int count, float spread,
                             float speed, int dmg, boolean fp, Color col) {
        for (int i = 0; i < count; i++) {
            float a = base + (float)(Math.random() - 0.5) * spread;
            spawn(x, y, a, speed, dmg, fp, false, col);
        }
    }

    public void update(float dt) {
        for (int i = active.size() - 1; i >= 0; i--) {
            Bullet b = active.get(i);
            b.update(dt);
            if (!b.alive || CollisionSystem.solid(map, b.x, b.y)) {
                b.alive = false; pool.add(active.remove(i));
            }
        }
    }

    public void draw(ShapeRenderer sr) { for (Bullet b : active) b.draw(sr); }

    public List<Bullet> getActive() { return active; }

    public void clear() { pool.addAll(active); active.clear(); }
}
