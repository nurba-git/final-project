package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.TextureRegistry;

public class Malgrath extends Enemy {
    private Player target;
    private float attackTimer = 0f;
    private static final Color BOSS_MAGIC = new Color(0.95f, 0.12f, 1f, 1f);

    public Malgrath(float x, float y, CollisionSystem col, BulletPool bp) {
        super(x, y, 500, 55f, 15f, new Color(0.22f, 0.02f, 0.32f, 1f), col, bp);
    }

    public void setTarget(Player target) { this.target = target; }
    @Override
    public String getTextureKey() {
        return TextureRegistry.MALGRATH;
    }
    @Override
    public void update(float delta) {
        if (target == null) return;
        if (hitFlash > 0) hitFlash -= delta;
        attackTimer += delta;
        float dist = CollisionSystem.dist(x, y, target.x, target.y);
        facing = CollisionSystem.angle(x, y, target.x, target.y);

        float rage = hp < maxHp * 0.3f ? 1.6f : 1f;
        move((float)Math.cos(facing) * speed * rage * delta,
             (float)Math.sin(facing) * speed * rage * delta);

        if (dist < 28f + target.RADIUS && !target.isInvincible()) target.takeDamage(30);

        if (attackTimer >= 1.2f) {
            int count = hp < maxHp * 0.5f ? 8 : 4;
            for (int i = 0; i < count; i++) {
                float angle = (float)(Math.PI * 2 * i / count);
                bulletPool.spawn(x, y, angle, 210f, 20, false, true, BOSS_MAGIC);
            }
            attackTimer = 0f;
        }
    }

}
