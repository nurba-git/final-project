package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.TextureRegistry;

public class BoneArcher extends Enemy {

    private Player target;
    private float fireTimer = 0f;

    private static final Color ARROW =
        new Color(0.85f, 0.80f, 0.55f, 1f);

    public BoneArcher(float x,
                      float y,
                      CollisionSystem col,
                      BulletPool bp) {

        super(
            x,
            y,
            30,
            70f,
            5f,
            new Color(0.75f, 0.72f, 0.62f, 1f),
            col,
            bp
        );
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    @Override
    public String getTextureKey() {
        return TextureRegistry.BONE_ARCHER;
    }

    @Override
    public void update(float delta) {

        if (target == null)
            return;

        if (hitFlash > 0)
            hitFlash -= delta;

        float dist = CollisionSystem.dist(
            x,
            y,
            target.x,
            target.y
        );

        facing = CollisionSystem.angle(
            x,
            y,
            target.x,
            target.y
        );

        if (dist < 170f) {

            if (dist < 65f) {

                move(
                    -(float)Math.cos(facing) * speed * delta,
                    -(float)Math.sin(facing) * speed * delta
                );
            }

            fireTimer += delta;

            if (fireTimer >= 1.1f) {

                bulletPool.spawn(
                    x,
                    y,
                    facing,
                    230f,
                    6,
                    false,
                    false,
                    ARROW
                );

                fireTimer = 0f;
            }

        } else {

            patrol(delta);
        }
    }
}
