package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.TextureRegistry;

public class ShadowWraith extends Enemy {
    private Player target;

    public ShadowWraith(float x, float y, CollisionSystem col, BulletPool bp) {
        super(x, y, 40, 105f, 6f, new Color(0.45f, 0.12f, 0.65f, 1f), col, bp);
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    @Override
    public String getTextureKey() {
        return TextureRegistry.SHADOW_WRAITH;
    }

    @Override
    public void update(float delta) {
        if (target == null) return;

        if (hitFlash > 0) hitFlash -= delta;

        float dist = CollisionSystem.dist(x, y, target.x, target.y);
        facing = CollisionSystem.angle(x, y, target.x, target.y);

        if (dist < 150f) {
            move(
                (float)Math.cos(facing) * speed * delta,
                (float)Math.sin(facing) * speed * delta
            );

            if (dist < 15f + Player.RADIUS && !target.isInvincible()) {
                target.takeDamage(8);
            }
        } else {
            patrol(delta);
        }
    }
}
