package com.narxoz.darkrealm.interfaces;

public interface IInputHandler {
    float getMoveX();
    float getMoveY();
    float getAimX();
    float getAimY();
    boolean isAttacking();
    boolean isPickup();
    boolean isReload();
    void update();
}
