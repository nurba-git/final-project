package com.narxoz.darkrealm.weapons;

import com.badlogic.gdx.graphics.Color;
import com.narxoz.darkrealm.systems.Bullet;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import java.util.List;

public class GunWeapon extends Weapon {
    private final BulletPool pool;
    private int ammo = 30;
    private final int maxAmmo = 30;
    private static final Color COLOR = new Color(0.9f, 0.65f, 0.15f, 1f);

    public GunWeapon(BulletPool pool) {
        super("Crossbow", 14, 0.25f, 220f);
        this.pool = pool;
    }

    @Override
    public boolean fire(float ownerX, float ownerY, float targetX, float targetY, List<Bullet> bullets) {
        if (!canFire() || ammo <= 0) return false;
        float angle = CollisionSystem.angle(ownerX, ownerY, targetX, targetY);
        pool.spawn(ownerX, ownerY, angle, 330f, damage, true, false, COLOR);
        ammo--;
        cooldown = fireRate;
        return true;
    }

    @Override public void reload() { ammo = maxAmmo; }
    @Override public String getHudLabel() { return "CROSSBOW " + ammo + "/" + maxAmmo; }
}
