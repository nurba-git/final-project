package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.C;
import com.narxoz.darkrealm.interfaces.IDamageable;
import com.narxoz.darkrealm.interfaces.IInputHandler;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.ParticleSystem;

import java.util.List;

/**
 * SOLID-S: только состояние игрока + движение + скиллы.
 * Ввод читает IInputHandler (SOLID-D).
 * Рисует себя пиксельным макетом через ShapeRenderer.
 */
public class Player implements IDamageable {

    // ── Stats ──
    public float x, y;
    public int hp, maxHp, mp, maxMp;
    public int atk, def, level, exp, expToNext;
    public int statPoints = 0;

    // ── State ──
    private float invTimer   = 0f;
    private float hitFlash   = 0f;
    private float attackTimer= 0f;
    private float dodgeTimer = 0f;
    private boolean dodging  = false;
    private float facing     = 0f;          // radians
    private float mpRegenTimer = 0f;

    // ── Skills ──
    private float cdShadowStrike = 0f;
    private float cdVoidShield   = 0f;
    private float cdSoulDrain    = 0f;
    private float voidShieldTimer= 0f;     // active shield
    private boolean shieldActive = false;

    // ── References ──
    private final CollisionSystem col;
    private final ParticleSystem  fx;

    // ── Colors ──
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
        hp = maxHp = 100; mp = maxMp = 60;
        atk = 10; def = 5; level = 1; exp = 0; expToNext = 100;
    }

    // ── UPDATE ────────────────────────────────────────────────────
    public void update(float dt, IInputHandler input) {
        // Timers
        if (invTimer   > 0) invTimer   -= dt;
        if (hitFlash   > 0) hitFlash   -= dt;
        if (attackTimer> 0) attackTimer -= dt;
        if (dodgeTimer > 0) dodgeTimer  -= dt; else dodging = false;
        if (cdShadowStrike > 0) cdShadowStrike -= dt;
        if (cdVoidShield   > 0) cdVoidShield   -= dt;
        if (cdSoulDrain    > 0) cdSoulDrain    -= dt;
        if (voidShieldTimer > 0) { voidShieldTimer -= dt; shieldActive = true; }
        else shieldActive = false;

        // Mana regen
        mpRegenTimer += dt;
        if (mpRegenTimer >= C.MP_REGEN_INTERVAL) {
            mp = Math.min(maxMp, mp + C.MP_REGEN_AMOUNT);
            mpRegenTimer = 0;
        }

        // Facing
        facing = CollisionSystem.angle(x, y, input.getAimX(), input.getAimY());

        // Dodge
        if (input.isDodge() && !dodging && dodgeTimer <= 0) {
            dodging = true;
            dodgeTimer = C.DODGE_DURATION;
            invTimer = C.DODGE_DURATION;
            fx.burst(x, y, new Color(0.6f,0.6f,1f,1f), 6, 1.5f, 0.3f);
        }

        // Movement
        float spd = dodging ? C.PLAYER_SPEED * C.DODGE_SPEED_MULT : C.PLAYER_SPEED;
        float dx = input.getMoveX() * spd * dt;
        float dy = input.getMoveY() * spd * dt;
        if (dodging) { dx = (float)Math.cos(facing)*spd*dt; dy = (float)Math.sin(facing)*spd*dt; }
        float[] pos = col.move(x, y, dx, dy, C.PLAYER_RADIUS);
        x = pos[0]; y = pos[1];
    }

    // ── ATTACK ───────────────────────────────────────────────────
    public boolean tryAttack() {
        if (attackTimer > 0) return false;
        attackTimer = C.ATTACK_COOLDOWN;
        fx.burst(x + (float)Math.cos(facing)*20, y + (float)Math.sin(facing)*20,
                 COL_BLADE, 4, 2f, 0.2f);
        return true;
    }

    // ── SKILLS ───────────────────────────────────────────────────
    /** Shadow Strike — dash forward + deal 3× ATK in range. Returns true if fired. */
    public boolean tryShadowStrike(List<Enemy> enemies) {
        if (cdShadowStrike > 0 || mp < 20) return false;
        mp -= 20; cdShadowStrike = C.CD_SHADOW_STRIKE;
        // Dash
        x += (float)Math.cos(facing) * 48;
        y += (float)Math.sin(facing) * 48;
        float[] pos = col.move(x, y, 0, 0, C.PLAYER_RADIUS);
        x = pos[0]; y = pos[1];
        // Damage
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            if (CollisionSystem.dist(x,y,e.x,e.y) < C.SHADOW_STRIKE_RANGE + e.radius)
                e.takeDamage(atk * 3);
        }
        invTimer = 0.3f;
        fx.burst(x, y, new Color(0.49f,0.23f,0.93f,1f), 12, 3f, 0.4f);
        return true;
    }

    /** Void Shield — block all damage for 2s. */
    public boolean tryVoidShield() {
        if (cdVoidShield > 0 || mp < 30) return false;
        mp -= 30; cdVoidShield = C.CD_VOID_SHIELD;
        voidShieldTimer = C.VOID_SHIELD_DURATION;
        fx.burst(x, y, new Color(0.4f,0.4f,1f,1f), 10, 2f, 0.5f);
        return true;
    }

    /** Soul Drain — steal 20 HP from nearest enemy. */
    public boolean trySoulDrain(List<Enemy> enemies) {
        if (cdSoulDrain > 0 || mp < 25) return false;
        Enemy nearest = null; float minD = C.SOUL_DRAIN_RANGE;
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            float d = CollisionSystem.dist(x,y,e.x,e.y);
            if (d < minD) { minD = d; nearest = e; }
        }
        if (nearest == null) return false;
        mp -= 25; cdSoulDrain = C.CD_SOUL_DRAIN;
        nearest.takeDamage(20);
        hp = Math.min(maxHp, hp + 20);
        fx.burst(nearest.x, nearest.y, new Color(0.8f,0.2f,0.2f,1f), 8, 2f, 0.5f);
        fx.burst(x, y, new Color(0.2f,0.9f,0.4f,1f), 8, 2f, 0.5f);
        return true;
    }

    // ── DAMAGE ───────────────────────────────────────────────────
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

    // ── EXP / LEVEL ──────────────────────────────────────────────
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

    // ── Getters for cooldowns ────────────────────────────────────
    public float getCdSS()  { return cdShadowStrike; }
    public float getCdVS()  { return cdVoidShield; }
    public float getCdSD()  { return cdSoulDrain; }
    public boolean isShieldActive() { return shieldActive; }
    public boolean isDodging()      { return dodging; }
    public float getFacing()        { return facing; }
    public boolean isInvincible()   { return invTimer > 0; }

    // ── DRAW (pixel-art макет) ────────────────────────────────────
    public void draw(ShapeRenderer sr) {
        float sx = x, sy = y;
        boolean flash = hitFlash > 0 && (int)(hitFlash * 20) % 2 == 0;

        if (flash) { sr.setColor(Color.WHITE); }

        // Shadow
        sr.setColor(0f,0f,0f,0.25f); sr.ellipse(sx-9,sy-14,18,5);

        // ── Boots ──
        sr.setColor(flash?Color.WHITE:new Color(0.16f,0.13f,0.11f,1f));
        sr.rect(sx-9,sy-12,7,6); sr.rect(sx+2,sy-12,7,6);

        // ── Leg armor ──
        sr.setColor(flash?Color.WHITE:COL_ARMOR);
        sr.rect(sx-8,sy-6,6,10); sr.rect(sx+2,sy-6,6,10);

        // ── Belt with gold buckle ──
        sr.setColor(flash?Color.WHITE:COL_PLATE);
        sr.rect(sx-10,sy+4,20,4);
        sr.setColor(flash?Color.WHITE:COL_GOLD);
        sr.rect(sx-2,sy+3,4,6);

        // ── Torso plate ──
        sr.setColor(flash?Color.WHITE:COL_ARMOR);
        sr.rect(sx-11,sy+8,22,14);
        sr.setColor(flash?Color.WHITE:COL_PLATE);
        sr.rect(sx-8,sy+10,16,10);

        // ── Chest emblem (gold diamond) ──
        sr.setColor(flash?Color.WHITE:COL_GOLD);
        sr.triangle(sx,sy+11, sx+4,sy+16, sx,sy+21);
        sr.triangle(sx,sy+11, sx-4,sy+16, sx,sy+21);

        // ── Pauldrons ──
        sr.setColor(flash?Color.WHITE:COL_PLATE);
        sr.rect(sx-16,sy+8,7,10); sr.rect(sx+9,sy+8,7,10);

        // ── Arms ──
        sr.setColor(flash?Color.WHITE:COL_ARMOR);
        sr.rect(sx-15,sy+6,6,14); sr.rect(sx+9,sy+6,6,14);

        // ── Gloves ──
        sr.setColor(flash?Color.WHITE:COL_PLATE);
        sr.rect(sx-16,sy,8,7); sr.rect(sx+8,sy,8,7);

        // ── Shield (left) ──
        if (!flash) sr.setColor(COL_SHIELD); else sr.setColor(Color.WHITE);
        float shA = facing + (float)Math.PI;
        float shX = sx + (float)Math.cos(shA)*14, shY = sy + (float)Math.sin(shA)*14;
        sr.rect(shX-4,shY-8,8,16);
        sr.setColor(flash?Color.WHITE:new Color(0.15f,0.38f,0.87f,0.7f));
        sr.rect(shX-3,shY-6,6,12);
        // Shield cross
        sr.setColor(flash?Color.WHITE:new Color(0.38f,0.64f,0.98f,1f));
        sr.rect(shX-3,shY-1,6,2); sr.rect(shX-1,shY-5,2,10);

        // ── Sword (right, shadow-strike glow) ──
        float swA = facing;
        float swBx=sx+(float)Math.cos(swA)*10, swBy=sy+(float)Math.sin(swA)*10;
        float swEx=sx+(float)Math.cos(swA)*28, swEy=sy+(float)Math.sin(swA)*28;
        sr.setColor(flash?Color.WHITE:new Color(0.6f,0.65f,0.98f,1f));
        sr.rectLine(swBx,swBy,swEx,swEy,3f);
        // guard
        sr.setColor(flash?Color.WHITE:new Color(0.42f,0.45f,0.55f,1f));
        float gx=sx+(float)Math.cos(swA)*13, gy=sy+(float)Math.sin(swA)*13;
        float perpA=swA+(float)Math.PI/2;
        sr.rectLine(gx+(float)Math.cos(perpA)*5,gy+(float)Math.sin(perpA)*5,
                    gx-(float)Math.cos(perpA)*5,gy-(float)Math.sin(perpA)*5,2f);
        // purple glow when cd=0
        if (cdShadowStrike <= 0) {
            sr.setColor(0.49f,0.23f,0.93f,0.3f);
            sr.rectLine(swBx,swBy,swEx,swEy,7f);
        }

        // ── Neck ──
        sr.setColor(flash?Color.WHITE:COL_PLATE);
        sr.rect(sx-4,sy+22,8,8);

        // ── Head ──
        sr.setColor(flash?Color.WHITE:COL_SKIN);
        sr.ellipse(sx-9,sy+22,18,18);

        // ── Hair ──
        sr.setColor(flash?Color.WHITE:COL_HAIR);
        sr.ellipse(sx-9,sy+30,18,10);
        sr.rect(sx-10,sy+25,4,12);

        // ── Eyes ──
        sr.setColor(flash?Color.WHITE:Color.WHITE);
        sr.ellipse(sx-7,sy+28,5,5); sr.ellipse(sx+2,sy+28,5,5);
        sr.setColor(flash?Color.WHITE:new Color(0.12f,0.23f,0.5f,1f));
        sr.ellipse(sx-6,sy+29,3,3); sr.ellipse(sx+3,sy+29,3,3);

        // ── Scar ──
        if (!flash) {
            sr.setColor(new Color(0.42f,0.23f,0.12f,1f));
            sr.rectLine(sx+5,sy+36,sx+8,sy+29,1f);
        }

        // ── Void Shield aura ──
        if (shieldActive) {
            sr.setColor(0.4f,0.4f,1f,0.25f + (float)Math.sin(System.currentTimeMillis()*0.005f)*0.1f);
            sr.circle(sx, sy+12, 22);
        }
    }
}
