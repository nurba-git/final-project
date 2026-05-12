package com.narxoz.darkrealm.interfaces;

public interface IDamageable {
    void takeDamage(int amount);
    boolean isAlive();
    int getHp();
    int getMaxHp();
}
