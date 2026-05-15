package com.narxoz.darkrealm.weapons;

import com.narxoz.darkrealm.entities.Enemy;
import com.narxoz.darkrealm.systems.Bullet;
import com.narxoz.darkrealm.systems.CollisionSystem;
import java.util.List;

public class SwordWeapon extends Weapon {
    private List<Enemy> enemies;

    public SwordWeapon() {
        super("Iron Sword", 28, 0.35f, 42f);
    }

    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
    }

    @Override
    public boolean fire(float ownerX, float ownerY, float targetX, float targetY, List<Bullet> bullets) {
        if (!canFire()) return false;
        if (enemies != null) {
            for (Enemy e : enemies) {
                if (e.isAlive() && CollisionSystem.dist(ownerX, ownerY, e.x, e.y) < range + e.radius) {
                    e.takeDamage(damage);
                }
            }
        }
        cooldown = fireRate;
        return true;
    }

    @Override public void reload() {}
    @Override public String getHudLabel() { return "IRON SWORD  ∞"; }
}
