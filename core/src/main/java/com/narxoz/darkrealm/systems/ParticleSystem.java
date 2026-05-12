package com.narxoz.darkrealm.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

/** Простая система частиц для hit-эффектов, лечения, смерти. */
public class ParticleSystem {

    private static class Particle {
        float x, y, vx, vy, life, maxLife, size;
        Color color = new Color();
        boolean alive;
    }

    private final List<Particle> pool   = new ArrayList<>();
    private final List<Particle> active = new ArrayList<>();

    public ParticleSystem() {
        for (int i = 0; i < 200; i++) pool.add(new Particle());
    }

    public void burst(float x, float y, Color col, int count, float speed, float life) {
        for (int i = 0; i < count; i++) {
            Particle p = pool.isEmpty() ? new Particle() : pool.remove(pool.size() - 1);
            double a = Math.random() * Math.PI * 2;
            float s = (float)(Math.random() * speed + speed * 0.3f);
            p.x = x; p.y = y;
            p.vx = (float)Math.cos(a) * s;
            p.vy = (float)Math.sin(a) * s;
            p.life = p.maxLife = life * (0.6f + (float)Math.random() * 0.8f);
            p.size = 1.5f + (float)Math.random() * 2f;
            p.color.set(col); p.alive = true;
            active.add(p);
        }
    }

    public void update(float dt) {
        for (int i = active.size() - 1; i >= 0; i--) {
            Particle p = active.get(i);
            p.x += p.vx * dt; p.y += p.vy * dt;
            p.vx *= 0.92f; p.vy *= 0.92f;
            p.life -= dt;
            if (p.life <= 0) { p.alive = false; pool.add(active.remove(i)); }
        }
    }

    public void draw(ShapeRenderer sr) {
        for (Particle p : active) {
            float alpha = p.life / p.maxLife;
            sr.setColor(p.color.r, p.color.g, p.color.b, alpha);
            sr.circle(p.x, p.y, p.size * alpha);
        }
    }
}
