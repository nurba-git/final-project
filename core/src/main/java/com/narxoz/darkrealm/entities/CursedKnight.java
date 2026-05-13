package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.TextureRegistry;

public class CursedKnight extends Enemy {
    private Player target;
    private float bashTimer = 0f;

    public CursedKnight(float x, float y, CollisionSystem col, BulletPool bp) {
        super(x, y, 80, 80f, 8f, new Color(0.35f, 0.25f, 0.30f, 1f), col, bp);
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    @Override
    public String getTextureKey() {
        return TextureRegistry.CURSED_KNIGHT;
    }

    @Override
    public void update(float delta) {
        if (target == null) return;

        if (hitFlash > 0) hitFlash -= delta;
        bashTimer -= delta;

        float dist = CollisionSystem.dist(x, y, target.x, target.y);
        facing = CollisionSystem.angle(x, y, target.x, target.y);

        if (dist < 170f) {
            move(
                (float)Math.cos(facing) * speed * delta * 0.9f,
                (float)Math.sin(facing) * speed * delta * 0.9f
            );

            if (dist < 17f + Player.RADIUS && !target.isInvincible()) {
                target.takeDamage(14);
            }

            if (dist < 32f && bashTimer <= 0 && !target.isInvincible()) {
                target.takeDamage(20);
                bashTimer = 2f;
            }
        } else {
            patrol(delta);
        }
    }
}
