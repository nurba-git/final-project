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
        super(x,y,80,14,1.3f*60f,6f,ARMOR,col,bp,fx);
        type="cursed_knight"; expReward=50;
    }

    @Override public void update(float dt) {
        if (!isAlive()||target==null) return;
        if (hitFlash>0) hitFlash-=dt; bashTimer-=dt;
        float d = CollisionSystem.dist(x,y,target.x,target.y);
        facing = CollisionSystem.angle(x,y,target.x,target.y);
        if (d < DETECT) {
            move((float)Math.cos(facing)*speed*dt*0.9f,(float)Math.sin(facing)*speed*dt*0.9f);
            boolean los = CollisionSystem.hasLineOfSight(col.getMap(), x, y, target.x, target.y);
            if (d < C.PLAYER_RADIUS+radius+4 && !target.isInvincible() && los)
                target.takeDamage(damage);
            if (d < BASH_RANGE && bashTimer <= 0 && !target.isInvincible() && los) {
                target.takeDamage(20); bashTimer=BASH_CD;
                if (fx!=null) fx.burst(target.x,target.y,PURPLE,8,2f,0.4f);
            }
        } else patrol(dt);
    }

    @Override public void draw(ShapeRenderer sr) {
        if (!isAlive()) return;
        boolean f = hitFlash>0&&(int)(hitFlash*20)%2==0;
        float sx=x, sy=y;

        // Boots (уменьшены)
        sr.setColor(f?Color.WHITE:new Color(0.14f,0.14f,0.18f,1f));
        sr.rect(sx-5,sy-8,5,4); sr.rect(sx+1,sy-8,5,4);
        // Leg armor
        sr.setColor(f?Color.WHITE:ARMOR);
        sr.rect(sx-5,sy-4,4,6); sr.rect(sx+1,sy-4,4,6);
        // Torso
        sr.setColor(f?Color.WHITE:ARMOR);
        sr.rect(sx-7,sy+2,14,9);
        sr.setColor(f?Color.WHITE:PLATE);
        sr.rect(sx-5,sy+3,10,7);
        // Purple cross emblem
        sr.setColor(f?Color.WHITE:PURPLE);
        sr.rect(sx-1,sy+4,2,6); sr.rect(sx-3,sy+6,6,2);
        // Shoulders
        sr.setColor(f?Color.WHITE:PLATE);
        sr.rect(sx-10,sy+2,5,7); sr.rect(sx+5,sy+2,5,7);
        // Arms
        sr.setColor(f?Color.WHITE:ARMOR);
        sr.rect(sx-10,sy+1,4,8); sr.rect(sx+6,sy+1,4,8);
        // Helmet
        sr.setColor(f?Color.WHITE:ARMOR);
        sr.rect(sx-6,sy+11,12,10);
        sr.setColor(f?Color.WHITE:new Color(0.08f,0.08f,0.12f,1f));
        sr.rect(sx-5,sy+13,10,4); // visor
        sr.setColor(f?Color.WHITE:PURPLE);
        sr.rect(sx-4,sy+14,8,2); // visor glow slit
        // Helmet plume
        sr.setColor(f?Color.WHITE:new Color(0.49f,0.23f,0.93f,0.9f));
        sr.triangle(sx-2,sy+21, sx,sy+27, sx+2,sy+21);

        // Tower shield
        float shA = facing + (float)Math.PI;
        float shX=sx+(float)Math.cos(shA)*9, shY=sy+(float)Math.sin(shA)*9;
        sr.setColor(f?Color.WHITE:SHIELD);
        sr.rect(shX-3,shY-6,6,12);
        sr.setColor(f?Color.WHITE:new Color(0.15f,0.38f,0.87f,0.7f));
        sr.rect(shX-2,shY-5,5,10);
        sr.setColor(f?Color.WHITE:new Color(0.38f,0.64f,0.98f,1f));
        sr.rect(shX-2,shY-1,5,2); sr.rect(shX-1,shY-4,2,8);

        // Sword
        float swX=sx+(float)Math.cos(facing)*5, swY=sy+(float)Math.sin(facing)*5;
        float swEx=sx+(float)Math.cos(facing)*14,swEy=sy+(float)Math.sin(facing)*14;
        sr.setColor(f?Color.WHITE:new Color(0.65f,0.65f,0.70f,1f));
        sr.rectLine(swX,swY,swEx,swEy,2f);

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
        super(x,y,50,10,2.0f*60f,5f,BODY,col,bp,fx);
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
            if (d < radius+C.PLAYER_RADIUS+3 && !target.isInvincible()
                    && CollisionSystem.hasLineOfSight(col.getMap(), x, y, target.x, target.y)) {
                target.takeDamage(damage);
                if (fx!=null) fx.burst(target.x,target.y,POISON,4,1.5f,0.25f);
            }
        } else patrol(dt);
    }

    @Override public void draw(ShapeRenderer sr) {
        if (!isAlive()) return;
        boolean f = hitFlash>0&&(int)(hitFlash*20)%2==0;
        float sx=x, sy=y;
        float legSwing = (float)Math.sin(walkTimer * 8f) * 2f;

        // Body (уменьшен)
        sr.setColor(f?Color.WHITE:BODY);
        sr.ellipse(sx-8,sy-4,16,8);
        // Legs
        sr.setColor(f?Color.WHITE:new Color(0.18f,0.33f,0.06f,1f));
        sr.rectLine(sx-6,sy-4,sx-7+legSwing,sy-9,2f);
        sr.rectLine(sx-2,sy-4,sx-3-legSwing,sy-9,2f);
        sr.rectLine(sx+2,sy-4,sx+3+legSwing,sy-9,2f);
        sr.rectLine(sx+6,sy-4,sx+7-legSwing,sy-9,2f);
        // Tail
        sr.setColor(f?Color.WHITE:BODY);
        sr.rectLine(sx+8,sy,sx+13,sy+3,2f);

        // Head
        sr.setColor(f?Color.WHITE:new Color(0.25f,0.46f,0.10f,1f));
        sr.ellipse(sx-14,sy-1,10,8);
        // Snout
        sr.setColor(f?Color.WHITE:BODY);
        sr.ellipse(sx-18,sy+1,7,5);
        // Nose
        sr.setColor(f?Color.WHITE:new Color(0.10f,0.18f,0.03f,1f));
        sr.circle(sx-19,sy+3,1.5f);
        // Eyes
        sr.setColor(f?Color.WHITE:new Color(0.95f,0.90f,0.20f,1f));
        sr.circle(sx-11,sy+2,2); sr.circle(sx-8,sy+2,2);
        sr.setColor(f?Color.WHITE:new Color(0.06f,0.10f,0.02f,1f));
        sr.circle(sx-11,sy+2,1f); sr.circle(sx-8,sy+2,1f);
        // Poison drip
        sr.setColor(f?Color.WHITE:POISON);
        sr.circle(sx-18,sy-1,1.5f);

        drawHpBar(sr);
    }
}
