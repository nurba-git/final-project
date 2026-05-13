package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.interfaces.IDamageable;
import com.narxoz.darkrealm.interfaces.IUpdatable;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.TextureRegistry;

public abstract class Enemy implements IDamageable, IUpdatable {
    public float x, y;
    public int hp, maxHp;
    public float speed;
    public float radius;
    protected float facing = 0f;
    protected float hitFlash = 0f;
    protected Color color;
    protected CollisionSystem collision;
    protected BulletPool bulletPool;

    private float patrolAngle = (float)(Math.random() * Math.PI * 2);
    private float patrolTimer = 0f;

    public Enemy(float x, float y, int hp, float speed, float radius,
                 Color color, CollisionSystem collision, BulletPool bulletPool) {
        this.x = x;
        this.y = y;
        this.hp = this.maxHp = hp;
        this.speed = speed;
        this.radius = radius;
        this.color = new Color(color);
        this.collision = collision;
        this.bulletPool = bulletPool;
    }

    @Override
    public abstract void update(float delta);

    public abstract String getTextureKey();

    public void render(SpriteBatch batch) {
        Texture texture = TextureRegistry.get(getTextureKey());

        if (texture != null) {
            float size = radius * 5f;
            batch.draw(texture, x - size / 2f, y - size / 2f, size, size);
        }
    }

    public void drawHpBar(ShapeRenderer sr) {
        float barW = radius * 2.8f;
        float barX = x - barW / 2f;
        float barY = y + radius + 4;

        sr.setColor(0.12f, 0.08f, 0.12f, 0.9f);
        sr.rect(barX, barY, barW, 3);

        sr.setColor(color);
        sr.rect(barX, barY, barW * ((float)hp / maxHp), 3);
    }

    @Override
    public void takeDamage(int amount) {
        hp -= amount;
        if (hp < 0) hp = 0;
        hitFlash = 0.12f;
    }

    @Override public boolean isAlive() { return hp > 0; }
    @Override public int getHp() { return hp; }

    protected void move(float dx, float dy) {
        float[] pos = collision.moveWithCollision(x, y, dx, dy, radius);
        x = pos[0];
        y = pos[1];
    }

    protected void patrol(float delta) {
        patrolTimer += delta;
        if (patrolTimer > 1.0f) {
            patrolAngle = (float)(Math.random() * Math.PI * 2);
            patrolTimer = 0f;
        }

        move((float)Math.cos(patrolAngle) * speed * delta * 0.45f,
            (float)Math.sin(patrolAngle) * speed * delta * 0.45f);
    }
}
