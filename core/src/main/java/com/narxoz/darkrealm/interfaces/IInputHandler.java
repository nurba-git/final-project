package com.narxoz.darkrealm.interfaces;


public interface IInputHandler {
    float getMoveX();
    float getMoveY();
    float getAimX();
    float getAimY();
    boolean isAttack();
    boolean isDodge();
    boolean isInteract();
    boolean isSkill();
    boolean isInventory();
    boolean isPause();
    void update();
}
