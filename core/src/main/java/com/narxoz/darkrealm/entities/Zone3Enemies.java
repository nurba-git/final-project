package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.C;
import com.narxoz.darkrealm.systems.*;

// ════════════════════════════════════════════════════════════════
// VOID SORCERER — телепорт + AoE тройной выстрел, зона 3
// ════════════════════════════════════════════════════════════════
class VoidSorcerer extends Enemy {

    private static final Color ROBE  = new Color(0.06f,0.04f,0.16f,1f);
    private static final Color ORBC  = new Color(0.39f,0.40f,0.98f,1f);
    private static final Color MAGIC = new Color(0.50f,0.20f,0.95f,1f);

    private float fireTimer = 0f, teleTimer = 0f, pulseTimer = 0f;
    private static final float DETECT=190f, FIRE_CD=1.5f, TELE_CD=5f, MIN_RANGE=90f;

    public VoidSorcerer(float x,float y, CollisionSystem col, BulletPool bp, ParticleSystem fx){
        super(x,y,70,18,0.85f*60f,7f,ROBE,col,bp,fx);
        type="void_sorcerer"; expReward=60;
    }

    @Override public void update(float dt) {
        if (!isAlive()||target==null) return;
        if (hitFlash>0) hitFlash-=dt;
        fireTimer-=dt; teleTimer-=dt; pulseTimer+=dt;
        float d = CollisionSystem.dist(x,y,target.x,target.y);
        facing = CollisionSystem.angle(x,y,target.x,target.y);
        if (d < DETECT) {
            if (d < MIN_RANGE) move(-(float)Math.cos(facing)*speed*dt,-(float)Math.sin(facing)*speed*dt);
            // Triple spread shot
            if (fireTimer <= 0) {
                for (int i=-1;i<=1;i++)
                    bp.spawn(x,y,facing+i*0.35f,3.5f*60f,damage,false,true,MAGIC);
                fireTimer = FIRE_CD;
            }
            // Teleport
            if (teleTimer <= 0 && d < 60) {
                float ta = (float)(Math.random()*Math.PI*2);
                float nx=x+(float)Math.cos(ta)*80, ny=y+(float)Math.sin(ta)*80;
                if (!CollisionSystem.solid(col.getMap(),nx,ny)) { x=nx; y=ny; }
                if (fx!=null) { fx.burst(x,y,ORBC,10,2.5f,0.5f); fx.burst(nx,ny,ORBC,10,2.5f,0.5f); }
                teleTimer = TELE_CD;
            }
        } else patrol(dt);
    }

    @Override public void draw(ShapeRenderer sr) {
        if (!isAlive()) return;
        boolean f = hitFlash>0&&(int)(hitFlash*20)%2==0;
        float sx=x, sy=y;
        float orbPulse = (float)Math.sin(pulseTimer * 4f) * 1.5f;

        // Robe body (trapezoid via triangles)
        sr.setColor(f?Color.WHITE:ROBE);
        sr.triangle(sx-9,sy+6,sx+9,sy+6,sx+12,sy-20);
        sr.triangle(sx-9,sy+6,sx-12,sy-20,sx+12,sy-20);
        // Robe trim
        sr.setColor(f?Color.WHITE:new Color(0.27f,0.24f,0.62f,1f));
        sr.rectLine(sx-12,sy-20,sx-9,sy+6,1.5f);
        sr.rectLine(sx+12,sy-20,sx+9,sy+6,1.5f);

        // Body
        sr.setColor(f?Color.WHITE:new Color(0.12f,0.11f,0.28f,1f));
        sr.rect(sx-7,sy+4,14,8);

        // Head
        sr.setColor(f?Color.WHITE:new Color(0.12f,0.11f,0.28f,1f));
        sr.circle(sx,sy+18,10);
        // Face mask
        sr.setColor(f?Color.WHITE:new Color(0.06f,0.04f,0.16f,1f));
        sr.circle(sx,sy+19,7);
        // Eyes
        sr.setColor(f?Color.WHITE:ORBC);
        sr.circle(sx-3,sy+20,4); sr.circle(sx+3,sy+20,4);
        sr.setColor(f?Color.WHITE:new Color(0.88f,0.90f,1.0f,1f));
        sr.circle(sx-3,sy+20,2); sr.circle(sx+3,sy+20,2);

        // Tall pointed hat
        sr.setColor(f?Color.WHITE:new Color(0.06f,0.04f,0.16f,1f));
        sr.triangle(sx-8,sy+26,sx,sy+46,sx+8,sy+26);
        // Hat brim
        sr.setColor(f?Color.WHITE:new Color(0.12f,0.11f,0.28f,1f));
        sr.rect(sx-10,sy+24,20,4);
        // Star on hat
        sr.setColor(f?Color.WHITE:ORBC);
        for(int i=0;i<5;i++){
            double a=i*Math.PI*2/5-Math.PI/2;
            double a2=a+Math.PI/5;
            sr.triangle(sx+(float)Math.cos(a)*5,sy+36+(float)Math.sin(a)*5,
                         sx+(float)Math.cos(a2)*2,sy+36+(float)Math.sin(a2)*2,
                         sx+(float)Math.cos(a+2*Math.PI/5)*5,sy+36+(float)Math.sin(a+2*Math.PI/5)*5);
        }

        // Staff
        sr.setColor(f?Color.WHITE:new Color(0.27f,0.24f,0.62f,1f));
        sr.rectLine(sx-18,sy-26,sx-18,sy+6,2.5f);
        // Orb — pulsating
        sr.setColor(f?Color.WHITE:ORBC);
        sr.circle(sx-18,sy-30,7f+orbPulse);
        sr.setColor(f?Color.WHITE:new Color(0.75f,0.76f,1f,1f));
        sr.circle(sx-18,sy-30,4f+orbPulse*0.5f);
        sr.setColor(f?Color.WHITE:Color.WHITE);
        sr.circle(sx-18,sy-30,2);

        drawHpBar(sr);
    }
}

// ════════════════════════════════════════════════════════════════
// GOLEM SENTINEL — медленный, высокая броня, оглушение, зона 3
// ════════════════════════════════════════════════════════════════
class GolemSentinel extends Enemy {

    private static final Color STONE  = new Color(0.27f,0.25f,0.24f,1f);
    private static final Color DARK   = new Color(0.18f,0.16f,0.15f,1f);
    private static final Color XTAL   = new Color(0.49f,0.23f,0.93f,1f);

    private float slamTimer = 0f, pulseTimer = 0f;
    private static final float DETECT=140f, SLAM_CD=3f, SLAM_RANGE=40f;

    public GolemSentinel(float x,float y, CollisionSystem col, BulletPool bp, ParticleSystem fx){
        super(x,y,120,20,0.6f*60f,12f,STONE,col,bp,fx);
        type="golem_sentinel"; expReward=80;
    }

    @Override public void update(float dt) {
        if (!isAlive()||target==null) return;
        if (hitFlash>0) hitFlash-=dt; slamTimer-=dt;
        pulseTimer += dt;
        float d = CollisionSystem.dist(x,y,target.x,target.y);
        facing = CollisionSystem.angle(x,y,target.x,target.y);
        if (d < DETECT) {
            move((float)Math.cos(facing)*speed*dt,(float)Math.sin(facing)*speed*dt);
            if (d < radius+C.PLAYER_RADIUS+4 && !target.isInvincible()) target.takeDamage(damage);
            // Ground slam
            if (d < SLAM_RANGE && slamTimer <= 0 && !target.isInvincible()) {
                target.takeDamage(30);
                slamTimer = SLAM_CD;
                if (fx!=null) fx.burst(x,y,STONE,14,3f,0.5f);
            }
        } else patrol(dt);
    }

    @Override public void draw(ShapeRenderer sr) {
        if (!isAlive()) return;
        boolean f = hitFlash>0&&(int)(hitFlash*20)%2==0;
        float sx=x, sy=y;
        float crystalPulse = (float)Math.sin(pulseTimer * 2.5f) * 2f;

        // Feet
        sr.setColor(f?Color.WHITE:DARK);
        sr.rect(sx-14,sy-20,12,8); sr.rect(sx+2,sy-20,12,8);
        // Legs
        sr.setColor(f?Color.WHITE:STONE);
        sr.rect(sx-13,sy-12,11,16); sr.rect(sx+2,sy-12,11,16);

        // Massive torso
        sr.setColor(f?Color.WHITE:STONE);
        sr.rect(sx-18,sy+4,36,26);
        sr.setColor(f?Color.WHITE:DARK);
        sr.rect(sx-15,sy+6,30,20);
        // Crack lines
        sr.setColor(f?Color.WHITE:new Color(0.12f,0.11f,0.10f,1f));
        sr.rectLine(sx-6,sy+8,sx-9,sy+18,1.5f);
        sr.rectLine(sx+6,sy+14,sx+8,sy+22,1.5f);
        // Chest crystal — pulsating
        sr.setColor(f?Color.WHITE:XTAL);
        sr.triangle(sx,sy+7-crystalPulse,sx+7+crystalPulse,sy+17,sx,sy+28+crystalPulse);
        sr.triangle(sx,sy+7-crystalPulse,sx-7-crystalPulse,sy+17,sx,sy+28+crystalPulse);
        sr.setColor(f?Color.WHITE:new Color(0.78f,0.70f,1f,1f));
        sr.triangle(sx,sy+11,sx+4,sy+17,sx,sy+24);
        sr.triangle(sx,sy+11,sx-4,sy+17,sx,sy+24);

        // Shoulders (huge)
        sr.setColor(f?Color.WHITE:DARK);
        sr.rect(sx-28,sy+4,12,18); sr.rect(sx+16,sy+4,12,18);
        // Shoulder spikes
        sr.setColor(f?Color.WHITE:STONE);
        sr.triangle(sx-26,sy+22,sx-30,sy+30,sx-22,sy+22);
        sr.triangle(sx+22,sy+22,sx+26,sy+30,sx+18,sy+22);

        // Arms (massive)
        sr.setColor(f?Color.WHITE:STONE);
        sr.rect(sx-28,sy-4,12,22); sr.rect(sx+16,sy-4,12,22);
        // Fists
        sr.setColor(f?Color.WHITE:DARK);
        sr.rect(sx-30,sy-10,14,10); sr.rect(sx+16,sy-10,14,10);

        // Square head
        sr.setColor(f?Color.WHITE:STONE);
        sr.rect(sx-12,sy+30,24,18);
        // Visor
        sr.setColor(f?Color.WHITE:new Color(0.08f,0.07f,0.07f,1f));
        sr.rect(sx-10,sy+34,20,8);
        // Eye crystals
        sr.setColor(f?Color.WHITE:XTAL);
        sr.rect(sx-8,sy+36,5,4); sr.rect(sx+3,sy+36,5,4);
        // Top knob
        sr.setColor(f?Color.WHITE:DARK);
        sr.rect(sx-4,sy+48,8,6);

        drawHpBar(sr);
    }
}
