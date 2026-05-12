package com.narxoz.darkrealm.interfaces;

/**
 * SOLID-D: GameScreen зависит от IInputHandler, а не от KeyboardInputHandler.
 * Можно легко заменить на AIInputHandler или MockInputHandler для тестов.
 */
public interface IInputHandler {
    float getMoveX();       // -1..1
    float getMoveY();       // -1..1
    float getAimX();        // world X
    float getAimY();        // world Y
    boolean isAttack();
    boolean isDodge();
    boolean isInteract();
    boolean isSkill();
    boolean isInventory();
    boolean isPause();
    void update();
}
