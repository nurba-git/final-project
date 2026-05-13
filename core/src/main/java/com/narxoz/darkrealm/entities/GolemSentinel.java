package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.TextureRegistry;

public class GolemSentinel extends Enemy {
    private Player target;
    private float slamTimer = 0f;

    public GolemSentinel(float x, float y, CollisionSystem col, BulletPool bp) {
        super(x, y, 120, 45f, 12f, new Color(0.42f, 0.38f, 0.45f, 1f), col, bp);
    }

    public void setTarget(Player target) { this.target = target; }
    @Override
    public String getTextureKey() {
        return TextureRegistry.GOLEM_SENTINEL;
    }
    @Override
    public void update(float delta) {
        if (target == null) return;
        if (hitFlash > 0) hitFlash -= delta;
        slamTimer -= delta;
        float dist = CollisionSystem.dist(x, y, target.x, target.y);
        facing = CollisionSystem.angle(x, y, target.x, target.y);
        if (dist < 190f) {
            move((float)Math.cos(facing) * speed * delta,
                 (float)Math.sin(facing) * speed * delta);
            if (dist < 24f + target.RADIUS && !target.isInvincible()) target.takeDamage(20);
            if (dist < 45f && slamTimer <= 0 && !target.isInvincible()) {
                target.takeDamage(28);
                slamTimer = 2.5f;
            }
        } else patrol(delta);
    }
}
