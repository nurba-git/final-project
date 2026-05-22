package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.C;
import com.narxoz.darkrealm.interfaces.IDamageable;
import com.narxoz.darkrealm.interfaces.IInputHandler;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.ParticleSystem;

import java.util.List;

public class Player implements IDamageable {


    public float x, y;
    public int hp, maxHp, mp, maxMp;
    public int atk, def, level, exp, expToNext;
    public int statPoints = 0;


    private float invTimer   = 0f;
    private float hitFlash   = 0f;
    private float attackTimer= 0f;
    private float dodgeTimer = 0f;
    private boolean dodging  = false;
    private float facing     = 0f;          // radians
    private float mpRegenTimer = 0f;


    private float cdShadowStrike = 0f;
    private float cdVoidShield   = 0f;
    private float cdSoulDrain    = 0f;
    private float voidShieldTimer= 0f;     // active shield
    private boolean shieldActive = false;


    private final CollisionSystem col;
    private final ParticleSystem  fx;


    private static final Color COL_ARMOR  = new Color(0.11f,0.11f,0.15f,1f);
    private static final Color COL_PLATE  = new Color(0.16f,0.16f,0.22f,1f);
    private static final Color COL_GOLD   = new Color(0.83f,0.63f,0.09f,1f);
    private static final Color COL_SHIELD = new Color(0.12f,0.23f,0.37f,1f);
    private static final Color COL_BLADE  = new Color(0.60f,0.65f,0.98f,1f);
    private static final Color COL_SKIN   = new Color(0.29f,0.21f,0.13f,1f);
    private static final Color COL_HAIR   = new Color(0.11f,0.06f,0.03f,1f);
    private static final Color COL_PURPLE = new Color(0.49f,0.23f,0.93f,0.35f);
    public static final float RADIUS = C.PLAYER_RADIUS;

    public Player(float x, float y, CollisionSystem col, ParticleSystem fx) {
        this.x = x; this.y = y;
        this.col = col; this.fx = fx;
        hp = maxHp = 1000; mp = maxMp = 60;
        atk = 10; def = 5; level = 1; exp = 0; expToNext = 100;
    }


    public void update(float dt, IInputHandler input) {

        if (invTimer   > 0) invTimer   -= dt;
        if (hitFlash   > 0) hitFlash   -= dt;
        if (attackTimer> 0) attackTimer -= dt;
        if (dodgeTimer > 0) dodgeTimer  -= dt; else dodging = false;
        if (cdShadowStrike > 0) cdShadowStrike -= dt;
        if (cdVoidShield   > 0) cdVoidShield   -= dt;
        if (cdSoulDrain    > 0) cdSoulDrain    -= dt;
        if (voidShieldTimer > 0) { voidShieldTimer -= dt; shieldActive = true; }
        else shieldActive = false;


        mpRegenTimer += dt;
        if (mpRegenTimer >= C.MP_REGEN_INTERVAL) {
            mp = Math.min(maxMp, mp + C.MP_REGEN_AMOUNT);
            mpRegenTimer = 0;
        }


        facing = CollisionSystem.angle(x, y, input.getAimX(), input.getAimY());


        if (input.isDodge() && !dodging && dodgeTimer <= 0) {
            dodging = true;
            dodgeTimer = C.DODGE_DURATION;
            invTimer = C.DODGE_DURATION;
            fx.burst(x, y, new Color(0.6f,0.6f,1f,1f), 6, 1.5f, 0.3f);
        }


        float spd = dodging ? C.PLAYER_SPEED * C.DODGE_SPEED_MULT : C.PLAYER_SPEED;
        float dx = input.getMoveX() * spd * dt;
        float dy = input.getMoveY() * spd * dt;
        if (dodging) { dx = (float)Math.cos(facing)*spd*dt; dy = (float)Math.sin(facing)*spd*dt; }
        float[] pos = col.move(x, y, dx, dy, C.PLAYER_RADIUS);
        x = pos[0]; y = pos[1];
    }


    public boolean tryAttack() {
        if (attackTimer > 0) return false;
        attackTimer = C.ATTACK_COOLDOWN;
        fx.burst(x + (float)Math.cos(facing)*20, y + (float)Math.sin(facing)*20,
                 COL_BLADE, 4, 2f, 0.2f);
        return true;
    }



    public boolean tryShadowStrike(List<Enemy> enemies) {
        if (cdShadowStrike > 0 || mp < 20) return false;
        mp -= 20; cdShadowStrike = C.CD_SHADOW_STRIKE;

        float dashDist = 48f;
        float stepSize = C.PLAYER_RADIUS;
        int steps = (int)(dashDist / stepSize) + 1;
        for (int i = 0; i < steps; i++) {
            float[] pos = col.move(x, y, (float)Math.cos(facing)*stepSize, (float)Math.sin(facing)*stepSize, C.PLAYER_RADIUS);
            if (pos[0] == x && pos[1] == y) break; // уперся в стену
            x = pos[0]; y = pos[1];
        }

        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            if (CollisionSystem.dist(x,y,e.x,e.y) < C.SHADOW_STRIKE_RANGE + e.radius)
                e.takeDamage(atk * 3);
        }
        invTimer = 0.3f;
        fx.burst(x, y, new Color(0.49f,0.23f,0.93f,1f), 12, 3f, 0.4f);
        return true;
    }


    public boolean tryVoidShield() {
        if (cdVoidShield > 0 || mp < 30) return false;
        mp -= 30; cdVoidShield = C.CD_VOID_SHIELD;
        voidShieldTimer = C.VOID_SHIELD_DURATION;
        fx.burst(x, y, new Color(0.4f,0.4f,1f,1f), 10, 2f, 0.5f);
        return true;
    }


    public boolean trySoulDrain(List<Enemy> enemies) {
        if (cdSoulDrain > 0 || mp < 25) return false;
        Enemy nearest = null; float minD = C.SOUL_DRAIN_RANGE;
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            float d = CollisionSystem.dist(x,y,e.x,e.y);
            if (d < minD && CollisionSystem.hasLineOfSight(col.getMap(), x, y, e.x, e.y))
                { minD = d; nearest = e; }
        }
        if (nearest == null) return false;
        mp -= 25; cdSoulDrain = C.CD_SOUL_DRAIN;
        nearest.takeDamage(20);
        hp = Math.min(maxHp, hp + 20);
        fx.burst(nearest.x, nearest.y, new Color(0.8f,0.2f,0.2f,1f), 8, 2f, 0.5f);
        fx.burst(x, y, new Color(0.2f,0.9f,0.4f,1f), 8, 2f, 0.5f);
        return true;
    }


    @Override
    public void takeDamage(int amount) {
        if (invTimer > 0 || shieldActive) return;
        int dmg = Math.max(1, amount - def);
        hp -= dmg; if (hp < 0) hp = 0;
        invTimer = C.IFRAMES_DURATION;
        hitFlash = 0.15f;
        fx.burst(x, y, new Color(0.9f,0.1f,0.1f,1f), 6, 2f, 0.3f);
    }

    @Override public boolean isAlive()  { return hp > 0; }
    @Override public int getHp()        { return hp; }
    @Override public int getMaxHp()     { return maxHp; }

    public void heal(int amount) { hp = Math.min(maxHp, hp + amount); }


    public void gainExp(int amount) {
        exp += amount;
        while (exp >= expToNext) {
            exp -= expToNext;
            level++;
            expToNext = (int)(expToNext * C.EXP_SCALE);
            statPoints += 3;
            maxHp += 10; hp = maxHp;
            maxMp += 5;  mp = maxMp;
        }
    }


    public float getCdSS()  { return cdShadowStrike; }
    public float getCdVS()  { return cdVoidShield; }
    public float getCdSD()  { return cdSoulDrain; }
    public boolean isShieldActive() { return shieldActive; }
    public boolean isDodging()      { return dodging; }
    public float getFacing()        { return facing; }
    public boolean isInvincible()   { return invTimer > 0; }


    public void draw(ShapeRenderer sr) {
        float sx = x, sy = y;
        boolean flash = hitFlash > 0 && (int)(hitFlash * 20) % 2 == 0;

        if (flash) { sr.setColor(Color.WHITE); }


        sr.setColor(0f,0f,0f,0.25f); sr.ellipse(sx-5,sy-8,10,3);


        sr.setColor(flash?Color.WHITE:new Color(0.16f,0.13f,0.11f,1f));
        sr.rect(sx-5,sy-7,4,4); sr.rect(sx+1,sy-7,4,4);


        sr.setColor(flash?Color.WHITE:COL_ARMOR);
        sr.rect(sx-5,sy-3,4,6); sr.rect(sx+1,sy-3,4,6);


        sr.setColor(flash?Color.WHITE:COL_PLATE);
        sr.rect(sx-6,sy+3,12,3);
        sr.setColor(flash?Color.WHITE:COL_GOLD);
        sr.rect(sx-1,sy+2,3,4);

        sr.setColor(flash?Color.WHITE:COL_ARMOR);
        sr.rect(sx-6,sy+6,12,8);
        sr.setColor(flash?Color.WHITE:COL_PLATE);
        sr.rect(sx-5,sy+7,10,6);


        sr.setColor(flash?Color.WHITE:COL_GOLD);
        sr.triangle(sx,sy+7, sx+2,sy+10, sx,sy+13);
        sr.triangle(sx,sy+7, sx-2,sy+10, sx,sy+13);


        sr.setColor(flash?Color.WHITE:COL_PLATE);
        sr.rect(sx-9,sy+6,4,6); sr.rect(sx+5,sy+6,4,6);


        sr.setColor(flash?Color.WHITE:COL_ARMOR);
        sr.rect(sx-8,sy+4,3,8); sr.rect(sx+5,sy+4,3,8);


        sr.setColor(flash?Color.WHITE:COL_PLATE);
        sr.rect(sx-9,sy,5,4); sr.rect(sx+4,sy,5,4);


        if (!flash) sr.setColor(COL_SHIELD); else sr.setColor(Color.WHITE);
        float shA = facing + (float)Math.PI;
        float shX = sx + (float)Math.cos(shA)*8, shY = sy + (float)Math.sin(shA)*8;
        sr.rect(shX-2,shY-5,5,10);
        sr.setColor(flash?Color.WHITE:new Color(0.15f,0.38f,0.87f,0.7f));
        sr.rect(shX-2,shY-4,4,8);

        sr.setColor(flash?Color.WHITE:new Color(0.38f,0.64f,0.98f,1f));
        sr.rect(shX-2,shY-1,4,2); sr.rect(shX-1,shY-3,2,6);


        float swA = facing;
        float swBx=sx+(float)Math.cos(swA)*6, swBy=sy+(float)Math.sin(swA)*6;
        float swEx=sx+(float)Math.cos(swA)*16, swEy=sy+(float)Math.sin(swA)*16;
        sr.setColor(flash?Color.WHITE:new Color(0.6f,0.65f,0.98f,1f));
        sr.rectLine(swBx,swBy,swEx,swEy,2f);

        sr.setColor(flash?Color.WHITE:new Color(0.42f,0.45f,0.55f,1f));
        float gx=sx+(float)Math.cos(swA)*8, gy=sy+(float)Math.sin(swA)*8;
        float perpA=swA+(float)Math.PI/2;
        sr.rectLine(gx+(float)Math.cos(perpA)*3,gy+(float)Math.sin(perpA)*3,
                    gx-(float)Math.cos(perpA)*3,gy-(float)Math.sin(perpA)*3,1.5f);
        if (cdShadowStrike <= 0) {
            sr.setColor(0.49f,0.23f,0.93f,0.3f);
            sr.rectLine(swBx,swBy,swEx,swEy,5f);
        }


        sr.setColor(flash?Color.WHITE:COL_PLATE);
        sr.rect(sx-2,sy+14,4,4);


        sr.setColor(flash?Color.WHITE:COL_SKIN);
        sr.ellipse(sx-5,sy+14,10,10);


        sr.setColor(flash?Color.WHITE:COL_HAIR);
        sr.ellipse(sx-5,sy+18,10,6);
        sr.rect(sx-6,sy+15,2,7);


        sr.setColor(flash?Color.WHITE:Color.WHITE);
        sr.ellipse(sx-4,sy+17,3,3); sr.ellipse(sx+1,sy+17,3,3);
        sr.setColor(flash?Color.WHITE:new Color(0.12f,0.23f,0.5f,1f));
        sr.ellipse(sx-4,sy+17,2,2); sr.ellipse(sx+1,sy+17,2,2);


        if (!flash) {
            sr.setColor(new Color(0.42f,0.23f,0.12f,1f));
            sr.rectLine(sx+3,sy+22,sx+5,sy+17,1f);
        }


        if (shieldActive) {
            sr.setColor(0.4f,0.4f,1f,0.25f + (float)Math.sin(System.currentTimeMillis()*0.005f)*0.1f);
            sr.circle(sx, sy+10, 14);
        }
    }
}
