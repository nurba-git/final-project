package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.TextureRegistry;

public class VoidSorcerer extends Enemy {
    private Player target;
    private float fireTimer = 0f;
    private float teleportTimer = 0f;
    private static final Color MAGIC = new Color(0.75f, 0.1f, 1f, 1f);

    public VoidSorcerer(float x, float y, CollisionSystem col, BulletPool bp) {
        super(x, y, 70, 60f, 7f, new Color(0.4f, 0.05f, 0.68f, 1f), col, bp);
    }

    public void setTarget(Player target) { this.target = target; }
    @Override
    public String getTextureKey() {
        return TextureRegistry.VOID_SORCERER;
    }
    @Override
    public void update(float delta) {
        if (target == null) return;
        if (hitFlash > 0) hitFlash -= delta;
        fireTimer += delta;
        teleportTimer += delta;
        float dist = CollisionSystem.dist(x, y, target.x, target.y);
        facing = CollisionSystem.angle(x, y, target.x, target.y);

        if (dist < 210f) {
            if (teleportTimer >= 3.2f) {
                x += (float)(Math.random() - 0.5f) * 80f;
                y += (float)(Math.random() - 0.5f) * 80f;
                teleportTimer = 0f;
            }
            if (fireTimer >= 1.4f) {
                for (int i = -1; i <= 1; i++) {
                    bulletPool.spawn(x, y, facing + i * 0.35f, 220f, 18, false, true, MAGIC);
                }
                fireTimer = 0f;
            }
        } else patrol(delta);
    }
}
