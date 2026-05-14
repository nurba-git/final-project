package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.interfaces.IDamageable;
import com.narxoz.darkrealm.interfaces.IInputHandler;
import com.narxoz.darkrealm.interfaces.IRenderable;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.TextureRegistry;
import com.narxoz.darkrealm.weapons.Weapon;

public class Player implements IDamageable, IRenderable {
    public float x, y;
    public int hp, maxHp;
    public int mana, maxMana;
    public float speed;
    public static final float RADIUS = 7f;

    private Weapon weapon;
    private float facing = 0f;
    private float invincibleTimer = 0f;
    private float manaRegenTimer = 0f;
    private float hitFlash = 0f;

    private Texture texture;

    private static final float INVINCIBLE_DURATION = 0.5f;
    private static final float MANA_REGEN_INTERVAL = 0.2f;

    private final CollisionSystem collision;
    private final BulletPool bulletPool;

    public Player(float x, float y, CollisionSystem collision, BulletPool bulletPool) {
        this.x = x;
        this.y = y;
        this.collision = collision;
        this.bulletPool = bulletPool;
        this.hp = this.maxHp = 100;
        this.mana = this.maxMana = 60;
        this.speed = 140f;

        texture = TextureRegistry.get(TextureRegistry.PLAYER);
    }

    public void update(float delta, IInputHandler input) {
        float dx = input.getMoveX() * speed * delta;
        float dy = input.getMoveY() * speed * delta;
        float[] pos = collision.moveWithCollision(x, y, dx, dy, RADIUS);
        x = pos[0];
        y = pos[1];

        facing = CollisionSystem.angle(x, y, input.getAimX(), input.getAimY());

        if (invincibleTimer > 0) invincibleTimer -= delta;
        if (hitFlash > 0) hitFlash -= delta;

        manaRegenTimer += delta;
        if (manaRegenTimer >= MANA_REGEN_INTERVAL) {
            mana = Math.min(maxMana, mana + 1);
            manaRegenTimer = 0f;
        }

        if (weapon != null) {
            weapon.update(delta);
            if (input.isAttacking() && weapon.canFire()) {
                weapon.fire(x, y, input.getAimX(), input.getAimY(), bulletPool.getActive());
            }
            if (input.isReload()) weapon.reload();
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x - 16, y - 16, 32, 32);
        }
    }

    public void drawAim(ShapeRenderer sr) {
        sr.setColor(0.95f, 0.85f, 0.45f, 1f);
        sr.rectLine(
            x,
            y,
            x + (float)Math.cos(facing) * 13,
            y + (float)Math.sin(facing) * 13,
            1.5f
        );
    }

    @Override
    public void takeDamage(int amount) {
        if (invincibleTimer > 0) return;
        hp -= amount;
        if (hp < 0) hp = 0;
        invincibleTimer = INVINCIBLE_DURATION;
        hitFlash = 0.15f;
    }

    @Override public boolean isAlive() { return hp > 0; }
    @Override public int getHp() { return hp; }

    public void heal(int amount) { hp = Math.min(maxHp, hp + amount); }
    public void setWeapon(Weapon weapon) { this.weapon = weapon; }
    public Weapon getWeapon() { return weapon; }
    public boolean isInvincible() { return invincibleTimer > 0; }
}
