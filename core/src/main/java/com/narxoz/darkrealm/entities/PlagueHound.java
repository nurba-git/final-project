package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.TextureRegistry;

public class PlagueHound extends Enemy {
    private Player target;

    public PlagueHound(float x, float y, CollisionSystem col, BulletPool bp) {
        super(x, y, 50, 145f, 6f, new Color(0.32f, 0.62f, 0.18f, 1f), col, bp);
    }

    public void setTarget(Player target) { this.target = target; }
    @Override
    public String getTextureKey() {
        return TextureRegistry.PLAGUE_HOUND;
    }
    @Override
    public void update(float delta) {
        if (target == null) return;
        if (hitFlash > 0) hitFlash -= delta;
        float dist = CollisionSystem.dist(x, y, target.x, target.y);
        facing = CollisionSystem.angle(x, y, target.x, target.y);
        if (dist < 180f) {
            move((float)Math.cos(facing) * speed * delta,
                 (float)Math.sin(facing) * speed * delta);
            if (dist < 16f + target.RADIUS && !target.isInvincible()) target.takeDamage(10);
        } else patrol(delta);
    }
}
