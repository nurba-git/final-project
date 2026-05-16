package com.narxoz.darkrealm.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.narxoz.darkrealm.interfaces.IInputHandler;

public class KeyboardInputHandler implements IInputHandler {

    private final Camera cam;
    private float mx, my, moveX, moveY;
    private boolean attack, dodge, interact, skill, inventory, pause;

    public KeyboardInputHandler(Camera cam) { this.cam = cam; }

    @Override
    public void update() {
        moveX = 0; moveY = 0;
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT))  moveX -= 1;
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) moveX += 1;
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN))  moveY -= 1;
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP))    moveY += 1;
        if (moveX != 0 && moveY != 0) { moveX *= 0.707f; moveY *= 0.707f; }

        Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        cam.unproject(m);
        mx = m.x; my = m.y;

        attack    = Gdx.input.isButtonPressed(0) || Gdx.input.isKeyPressed(Keys.Z);
        dodge     = Gdx.input.isButtonJustPressed(1) || Gdx.input.isKeyJustPressed(Keys.X);
        interact  = Gdx.input.isKeyJustPressed(Keys.E);
        skill     = Gdx.input.isKeyJustPressed(Keys.Q);
        inventory = Gdx.input.isKeyJustPressed(Keys.I);
        pause     = Gdx.input.isKeyJustPressed(Keys.ESCAPE);
    }

    @Override public float getMoveX()       { return moveX; }
    @Override public float getMoveY()       { return moveY; }
    @Override public float getAimX()        { return mx; }
    @Override public float getAimY()        { return my; }
    @Override public boolean isAttack()     { return attack; }
    @Override public boolean isDodge()      { return dodge; }
    @Override public boolean isInteract()   { return interact; }
    @Override public boolean isSkill()      { return skill; }
    @Override public boolean isInventory()  { return inventory; }
    @Override public boolean isPause()      { return pause; }
}
