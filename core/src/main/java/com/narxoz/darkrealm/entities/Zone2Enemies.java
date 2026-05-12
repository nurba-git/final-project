package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.C;
import com.narxoz.darkrealm.systems.*;

// ════════════════════════════════════════════════════════════════
// CURSED KNIGHT — щит блокирует фронт + фланг, зона 2
// ════════════════════════════════════════════════════════════════
class CursedKnight extends Enemy {

    private static final Color ARMOR  = new Color(0.22f,0.22f,0.28f,1f);
    private static final Color PLATE  = new Color(0.30f,0.30f,0.38f,1f);
    private static final Color SHIELD = new Color(0.12f,0.14f,0.35f,1f);
    private static final Color PURPLE = new Color(0.42f,0.14f,0.66f,1f);

    private float bashTimer = 0f;
    private static final float DETECT=160f, BASH_CD=2.2f, BASH_RANGE=28f;

    public CursedKnight(float x,float y, CollisionSystem col, BulletPool bp, ParticleSystem fx){
        super(x,y,80,14,1.3f*60f,9f,ARMOR,col,bp,fx);
        type="cursed_knight"; expReward=50;
    }

    @Override public void update(float dt) {
        if (!isAlive()||target==null) return;
        if (hitFlash>0) hitFlash-=dt; bashTimer-=dt;
        float d = CollisionSystem.dist(x,y,target.x,target.y);
        facing = CollisionSystem.angle(x,y,target.x,target.y);
        if (d < DETECT) {
            move((float)Math.cos(facing)*speed*dt*0.9f,(float)Math.sin(facing)*speed*dt*0.9f);
            if (d < C.PLAYER_RADIUS+radius+4 && !target.isInvincible())
                target.takeDamage(damage);
            if (d < BASH_RANGE && bashTimer <= 0 && !target.isInvincible()) {
                target.takeDamage(20); bashTimer=BASH_CD;
                if (fx!=null) fx.burst(target.x,target.y,PURPLE,8,2f,0.4f);
            }
        } else patrol(dt);
    }

    @Override public void draw(ShapeRenderer sr) {
        if (!isAlive()) return;
        boolean f = hitFlash>0&&(int)(hitFlash*20)%2==0;
        float sx=x, sy=y;

        // Boots
        sr.setColor(f?Color.WHITE:new Color(0.14f,0.14f,0.18f,1f));
        sr.rect(sx-9,sy-14,8,7); sr.rect(sx+1,sy-14,8,7);
        // Leg armor
        sr.setColor(f?Color.WHITE:ARMOR);
        sr.rect(sx-8,sy-7,7,10); sr.rect(sx+1,sy-7,7,10);
        // Torso
        sr.setColor(f?Color.WHITE:ARMOR);
        sr.rect(sx-12,sy+3,24,16);
        sr.setColor(f?Color.WHITE:PLATE);
        sr.rect(sx-9,sy+5,18,12);
        // Purple cross emblem
        sr.setColor(f?Color.WHITE:PURPLE);
        sr.rect(sx-1,sy+6,2,10); sr.rect(sx-5,sy+10,10,2);
        // Shoulders
        sr.setColor(f?Color.WHITE:PLATE);
        sr.rect(sx-18,sy+3,8,12); sr.rect(sx+10,sy+3,8,12);
        // Arms
        sr.setColor(f?Color.WHITE:ARMOR);
        sr.rect(sx-17,sy+2,7,14); sr.rect(sx+10,sy+2,7,14);
        // Helmet
        sr.setColor(f?Color.WHITE:ARMOR);
        sr.rect(sx-10,sy+19,20,18);
        sr.setColor(f?Color.WHITE:new Color(0.08f,0.08f,0.12f,1f));
        sr.rect(sx-8,sy+22,16,7); // visor
        sr.setColor(f?Color.WHITE:PURPLE);
        sr.rect(sx-7,sy+24,14,3); // visor glow slit
        // Helmet plume
        sr.setColor(f?Color.WHITE:new Color(0.49f,0.23f,0.93f,0.9f));
        sr.triangle(sx-3,sy+37, sx,sy+48, sx+3,sy+37);

        // Tower shield (left side relative to facing)
        float shA = facing + (float)Math.PI;
        float shX=sx+(float)Math.cos(shA)*15, shY=sy+(float)Math.sin(shA)*15;
        sr.setColor(f?Color.WHITE:SHIELD);
        sr.rect(shX-5,shY-10,10,20);
        sr.setColor(f?Color.WHITE:new Color(0.15f,0.38f,0.87f,0.7f));
        sr.rect(shX-4,shY-8,8,16);
        // Shield cross
        sr.setColor(f?Color.WHITE:new Color(0.38f,0.64f,0.98f,1f));
        sr.rect(shX-4,shY-1,8,2); sr.rect(shX-1,shY-7,2,14);

        // Sword (right of facing)
        float swX=sx+(float)Math.cos(facing)*8, swY=sy+(float)Math.sin(facing)*8;
        float swEx=sx+(float)Math.cos(facing)*24,swEy=sy+(float)Math.sin(facing)*24;
        sr.setColor(f?Color.WHITE:new Color(0.65f,0.65f,0.70f,1f));
        sr.rectLine(swX,swY,swEx,swEy,3f);

        drawHpBar(sr);
    }
}

// ════════════════════════════════════════════════════════════════
// PLAGUE HOUND — пак по 3, прыжок + яд, зона 2
// ════════════════════════════════════════════════════════════════
class PlagueHound extends Enemy {

    private static final Color BODY   = new Color(0.21f,0.39f,0.08f,1f);
    private static final Color POISON = new Color(0.52f,0.80f,0.09f,1f);

    private float poisonTimer = 0f;
    private float walkTimer = 0f;
    private static final float DETECT=130f, POISON_CD=2f, POISON_DUR=3f;

    public PlagueHound(float x,float y, CollisionSystem col, BulletPool bp, ParticleSystem fx){
        super(x,y,50,10,2.0f*60f,7f,BODY,col,bp,fx);
        type="plague_hound"; expReward=40;
    }

    @Override public void update(float dt) {
        if (!isAlive()||target==null) return;
        if (hitFlash>0) hitFlash-=dt; poisonTimer-=dt;
        walkTimer += dt;
        float d = CollisionSystem.dist(x,y,target.x,target.y);
        facing = CollisionSystem.angle(x,y,target.x,target.y);
        if (d < DETECT) {
            move((float)Math.cos(facing)*speed*dt,(float)Math.sin(facing)*speed*dt);
            if (d < radius+C.PLAYER_RADIUS+3 && !target.isInvincible()) {
                target.takeDamage(damage);
                // poison drip burst
                if (fx!=null) fx.burst(target.x,target.y,POISON,4,1.5f,0.25f);
            }
        } else patrol(dt);
    }

    @Override public void draw(ShapeRenderer sr) {
        if (!isAlive()) return;
        boolean f = hitFlash>0&&(int)(hitFlash*20)%2==0;
        float sx=x, sy=y;
        float legSwing = (float)Math.sin(walkTimer * 8f) * 3f;

        // Body
        sr.setColor(f?Color.WHITE:BODY);
        sr.ellipse(sx-14,sy-6,28,14);
        // Legs — animated
        sr.setColor(f?Color.WHITE:new Color(0.18f,0.33f,0.06f,1f));
        sr.rectLine(sx-10,sy-6,sx-12+legSwing,sy-14,3f);
        sr.rectLine(sx-4, sy-6,sx-5-legSwing, sy-14,3f);
        sr.rectLine(sx+4, sy-6,sx+5+legSwing, sy-14,3f);
        sr.rectLine(sx+10,sy-6,sx+12-legSwing,sy-14,3f);
        // Tail
        sr.setColor(f?Color.WHITE:BODY);
        sr.rectLine(sx+14,sy,sx+22,sy+5,3f);
        sr.rectLine(sx+22,sy+5,sx+24,sy-2,2f);

        // Head
        sr.setColor(f?Color.WHITE:new Color(0.25f,0.46f,0.10f,1f));
        sr.ellipse(sx-22,sy-2,18,14);
        // Snout
        sr.setColor(f?Color.WHITE:BODY);
        sr.ellipse(sx-30,sy,12,8);
        // Nose
        sr.setColor(f?Color.WHITE:new Color(0.10f,0.18f,0.03f,1f));
        sr.circle(sx-32,sy+3,2);
        // Fangs
        sr.setColor(f?Color.WHITE:new Color(0.90f,0.90f,0.80f,1f));
        sr.triangle(sx-29,sy+6,sx-31,sy+11,sx-27,sy+6);
        sr.triangle(sx-25,sy+7,sx-26,sy+12,sx-23,sy+7);
        // Eyes
        sr.setColor(f?Color.WHITE:new Color(0.95f,0.90f,0.20f,1f));
        sr.circle(sx-19,sy+3,3); sr.circle(sx-14,sy+3,3);
        sr.setColor(f?Color.WHITE:new Color(0.06f,0.10f,0.02f,1f));
        sr.circle(sx-19,sy+3,1.5f); sr.circle(sx-14,sy+3,1.5f);
        // Ears
        sr.setColor(f?Color.WHITE:new Color(0.18f,0.33f,0.06f,1f));
        sr.triangle(sx-20,sy+7,sx-24,sy+14,sx-16,sy+7);
        sr.triangle(sx-14,sy+7,sx-12,sy+14,sx-8, sy+7);
        // Poison drip
        sr.setColor(f?Color.WHITE:POISON);
        sr.circle(sx-30,sy-2,2); sr.circle(sx-31,sy-7,1.5f);

        drawHpBar(sr);
    }
}
