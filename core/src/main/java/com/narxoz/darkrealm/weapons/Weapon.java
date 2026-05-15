package com.narxoz.darkrealm.weapons;

import com.narxoz.darkrealm.systems.Bullet;
import java.util.List;

public abstract class Weapon {
    public final String name;
    public final int damage;
    public final float fireRate;
    public final float range;
    protected float cooldown = 0f;

    public Weapon(String name, int damage, float fireRate, float range) {
        this.name = name;
        this.damage = damage;
        this.fireRate = fireRate;
        this.range = range;
    }

    public void update(float delta) {
        if (cooldown > 0) cooldown -= delta;
    }

    public boolean canFire() { return cooldown <= 0; }

    public abstract boolean fire(float ownerX, float ownerY, float targetX, float targetY, List<Bullet> bullets);
    public abstract void reload();
    public abstract String getHudLabel();
}
