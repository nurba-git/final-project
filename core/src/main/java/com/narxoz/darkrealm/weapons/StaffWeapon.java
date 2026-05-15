package com.narxoz.darkrealm.weapons;

import com.badlogic.gdx.graphics.Color;
import com.narxoz.darkrealm.systems.Bullet;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class StaffWeapon extends Weapon {
    private final BulletPool pool;
    private final IntSupplier getMana;
    private final IntConsumer useMana;
    private final int manaCost = 18;
    private static final Color COLOR = new Color(0.7f, 0.2f, 1f, 1f);

    public StaffWeapon(BulletPool pool, IntSupplier getMana, IntConsumer useMana) {
        super("Void Staff", 22, 0.45f, 180f);
        this.pool = pool;
        this.getMana = getMana;
        this.useMana = useMana;
    }

    @Override
    public boolean fire(float ownerX, float ownerY, float targetX, float targetY, List<Bullet> bullets) {
        if (!canFire() || getMana.getAsInt() < manaCost) return false;
        float angle = CollisionSystem.angle(ownerX, ownerY, targetX, targetY);
        pool.spawn(ownerX, ownerY, angle, 300f, damage, true, true, COLOR);
        useMana.accept(manaCost);
        cooldown = fireRate;
        return true;
    }

    @Override public void reload() {}
    @Override public String getHudLabel() { return "VOID STAFF  MP"; }
}
