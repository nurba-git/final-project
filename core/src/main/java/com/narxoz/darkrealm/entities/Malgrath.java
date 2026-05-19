package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.C;
import com.narxoz.darkrealm.systems.*;

import java.util.List;

/**
 * SOLID-O: Malgrath extends Enemy — не меняем базовый класс.
 * 3-phase boss. Phase меняется по HP threshold.
 */
public class Malgrath extends Enemy {

    public enum Phase { ONE, TWO, THREE }
    public Phase phase = Phase.ONE;

    private static final Color ARMOR = new Color(0.07f,0.01f,0.01f,1f);
    private static final Color RED   = new Color(0.93f,0.27f,0.27f,1f);
    private static final Color DARK  = new Color(0.12f,0.01f,0.01f,1f);

    private float slashTimer=0,orbTimer=0,novaTimer=0,summonTimer=0,blinkTimer=0;
    private List<Enemy> spawnList; // для добавления Shadow Wraith в Phase 2

    public Malgrath(float x,float y, CollisionSystem col, BulletPool bp, ParticleSystem fx){
        super(x,y,500,30,1.0f*60f,10f,ARMOR,col,bp,fx);
        type="malgrath"; expReward=1000;
    }

    public void setSpawnList(List<Enemy> list) { this.spawnList=list; }

    @Override public void update(float dt) {
        if (!isAlive()||target==null) return;
        if (hitFlash>0) hitFlash-=dt;
        updatePhase();

        float d = CollisionSystem.dist(x,y,target.x,target.y);
        facing = CollisionSystem.angle(x,y,target.x,target.y);

        float spd = phase==Phase.THREE ? speed*1.8f : speed;
        move((float)Math.cos(facing)*spd*dt,(float)Math.sin(facing)*spd*dt);

        slashTimer-=dt; orbTimer-=dt; novaTimer-=dt; summonTimer-=dt; blinkTimer-=dt;

        // Melee contact
        boolean los = CollisionSystem.hasLineOfSight(col.getMap(), x, y, target.x, target.y);
        if (d < radius+C.PLAYER_RADIUS+3 && !target.isInvincible() && los) target.takeDamage(damage);

        // Phase 1 — void slash
        if (slashTimer<=0) {
            int dmg = phase==Phase.THREE ? (int)(damage*1.5f) : damage;
            if (d<C.SHADOW_STRIKE_RANGE+radius && los) { target.takeDamage(dmg); slashTimer=1.8f; }
            else if (d>=C.SHADOW_STRIKE_RANGE+radius || !los) { slashTimer=0.5f; }
        }

        // All phases — orb burst
        if (orbTimer<=0) {
            int shots = phase==Phase.THREE?8:5;
            for(int i=0;i<shots;i++){
                float a=facing+(float)(i-shots/2)*0.28f;
                bp.spawn(x,y,a,4f*60f,(int)(damage*0.8f),false,true,RED);
            }
            orbTimer = phase==Phase.THREE?1.2f:2.0f;
        }

        // Phase 2 — summon Shadow Wraiths
        if (phase!=Phase.ONE && summonTimer<=0 && spawnList!=null) {
            for(int i=0;i<2;i++){
                float a=(float)(Math.random()*Math.PI*2);
                ShadowWraith sw=new ShadowWraith(x+(float)Math.cos(a)*50,y+(float)Math.sin(a)*50,col,bp,fx);
                sw.setTarget(target); spawnList.add(sw);
            }
            summonTimer=15f;
            if(fx!=null) fx.burst(x,y,RED,16,3f,0.7f);
        }

        // Phase 3 — void nova AoE
        if (phase==Phase.THREE && novaTimer<=0) {
            for(int i=0;i<12;i++){
                float a=(float)(i*Math.PI*2/12);
                bp.spawn(x,y,a,3.5f*60f,(int)(damage*1.2f),false,true,RED);
            }
            novaTimer=3f;
            if(fx!=null) fx.burst(x,y,new Color(0.9f,0.2f,0.2f,1f),20,4f,0.8f);
        }

        // Phase 3 blink
        if (phase==Phase.THREE && blinkTimer<=0 && d>100) {
            float ta=(float)(Math.random()*Math.PI*2);
            float nx=target.x+(float)Math.cos(ta)*30, ny=target.y+(float)Math.sin(ta)*30;
            if(!CollisionSystem.solid(col.getMap(),nx,ny)){ x=nx; y=ny; }
            blinkTimer=4f;
            if(fx!=null) fx.burst(x,y,RED,12,3f,0.6f);
        }
    }

    private void updatePhase(){
        if (hp<=150 && phase!=Phase.THREE){ phase=Phase.THREE; if(fx!=null) fx.burst(x,y,RED,24,5f,1f); }
        else if(hp<=300 && phase==Phase.ONE){ phase=Phase.TWO; summonTimer=3f; if(fx!=null) fx.burst(x,y,RED,16,4f,0.8f); }
    }

    @Override public void draw(ShapeRenderer sr) {
        if (!isAlive()) return;
        boolean f = hitFlash>0&&(int)(hitFlash*20)%2==0;
        float sx=x, sy=y;
        float rage = phase==Phase.THREE?1f:phase==Phase.TWO?0.5f:0f;

        // Outer aura (grows with phase)
        sr.setColor(0.74f,0.10f,0.10f,0.06f+rage*0.08f); sr.circle(sx,sy,56+rage*20);
        sr.setColor(0.74f,0.10f,0.10f,0.10f+rage*0.10f); sr.circle(sx,sy,40+rage*14);
        sr.setColor(0.74f,0.10f,0.10f,0.16f+rage*0.12f); sr.circle(sx,sy,28);

        // Demon wings
        sr.setColor(f?Color.WHITE:new Color(0.27f,0.04f,0.04f,0.95f));
        sr.triangle(sx-24,sy+4,sx-60,sy-30,sx-24,sy-4);
        sr.triangle(sx-24,sy-4,sx-50,sy-26,sx-24,sy-14);
        sr.triangle(sx-24,sy-14,sx-56,sy-18,sx-24,sy-22);
        sr.triangle(sx+24,sy+4,sx+60,sy-30,sx+24,sy-4);
        sr.triangle(sx+24,sy-4,sx+50,sy-26,sx+24,sy-14);
        sr.triangle(sx+24,sy-14,sx+56,sy-18,sx+24,sy-22);
        // Wing bone lines
        sr.setColor(f?Color.WHITE:new Color(0.50f,0.10f,0.10f,1f));
        sr.rectLine(sx-24,sy,sx-56,sy-20,1.5f);
        sr.rectLine(sx-24,sy,sx-46,sy-28,1.5f);
        sr.rectLine(sx+24,sy,sx+56,sy-20,1.5f);
        sr.rectLine(sx+24,sy,sx+46,sy-28,1.5f);

        // Cape
        sr.setColor(f?Color.WHITE:new Color(0.24f,0.01f,0.01f,0.95f));
        sr.triangle(sx-18,sy+8,sx-26,sy-36,sx+26,sy-36);
        sr.triangle(sx-18,sy+8,sx+18,sy+8,sx+26,sy-36);

        // Boots
        sr.setColor(f?Color.WHITE:new Color(0.08f,0.02f,0.02f,1f));
        sr.triangle(sx-14,sy-32,sx-14,sy-44,sx-4,sy-36);
        sr.triangle(sx+4,sy-32,sx+14,sy-44,sx+14,sy-36);
        // Legs
        sr.setColor(f?Color.WHITE:DARK);
        sr.rect(sx-13,sy-24,9,14); sr.rect(sx+4,sy-24,9,14);

        // Body armor
        sr.setColor(f?Color.WHITE:ARMOR);
        sr.rect(sx-18,sy+8,36,26);
        sr.setColor(f?Color.WHITE:DARK);
        sr.rect(sx-14,sy+10,28,22);
        // Rib lines
        sr.setColor(f?Color.WHITE:new Color(0.50f,0.08f,0.08f,1f));
        sr.rectLine(sx-11,sy+14,sx+11,sy+14,1f);
        sr.rectLine(sx-11,sy+20,sx+11,sy+20,1f);
        sr.rectLine(sx-11,sy+26,sx+11,sy+26,1f);
        // Soul gem (chest)
        sr.setColor(f?Color.WHITE:RED);
        sr.triangle(sx,sy+12,sx+6,sy+20,sx,sy+32);
        sr.triangle(sx,sy+12,sx-6,sy+20,sx,sy+32);
        sr.setColor(f?Color.WHITE:new Color(0.98f,0.64f,0.64f,1f));
        sr.triangle(sx,sy+16,sx+3,sy+21,sx,sy+29);
        sr.triangle(sx,sy+16,sx-3,sy+21,sx,sy+29);

        // Shoulder pads + spikes
        sr.setColor(f?Color.WHITE:DARK);
        sr.rect(sx-28,sy+8,12,16); sr.rect(sx+16,sy+8,12,16);
        sr.setColor(f?Color.WHITE:new Color(0.50f,0.08f,0.08f,1f));
        sr.triangle(sx-26,sy+24,sx-30,sy+34,sx-22,sy+24);
        sr.triangle(sx-20,sy+22,sx-20,sy+32,sx-14,sy+22);
        sr.triangle(sx+22,sy+24,sx+26,sy+34,sx+18,sy+24);
        sr.triangle(sx+20,sy+22,sx+20,sy+32,sx+14,sy+22);

        // Arms
        sr.setColor(f?Color.WHITE:DARK);
        sr.rect(sx-30,sy+2,12,26); sr.rect(sx+18,sy+2,12,26);

        // Left hand — dark blade
        sr.setColor(f?Color.WHITE:new Color(0.23f,0.25f,0.30f,1f));
        sr.rectLine(sx-24,sy-26,sx-24,sy+10,4f);
        sr.rectLine(sx-28,sy+4,sx-20,sy+4,3f); // guard
        sr.setColor(f?Color.WHITE:new Color(0.08f,0.10f,0.15f,1f));
        sr.triangle(sx-26,sy-26,sx-22,sy-26,sx-24,sy-40);
        // Blade rune glow
        sr.setColor(f?Color.WHITE:new Color(0.9f,0.1f,0.1f,0.7f+rage*0.3f));
        sr.rectLine(sx-24,sy-8,sx-24,sy+8,1.5f);

        // Right hand — void orb staff
        sr.setColor(f?Color.WHITE:new Color(0.50f,0.08f,0.08f,1f));
        sr.rectLine(sx+28,sy-40,sx+28,sy+28,3f);
        sr.setColor(f?Color.WHITE:DARK);
        sr.circle(sx+28,sy-44,12);
        sr.setColor(f?Color.WHITE:new Color(0.50f,0.08f,0.08f,1f));
        sr.circle(sx+28,sy-44,8);
        sr.setColor(f?Color.WHITE:RED);
        sr.circle(sx+28,sy-44,5);
        sr.setColor(f?Color.WHITE:new Color(0.98f,0.64f,0.64f,1f));
        sr.circle(sx+28,sy-44,2.5f);
        // Energy spikes on orb
        sr.setColor(f?Color.WHITE:RED);
        sr.rectLine(sx+28,sy-56,sx+28,sy-60,2f);
        sr.rectLine(sx+28,sy-32,sx+28,sy-28,2f);
        sr.rectLine(sx+16,sy-44,sx+12,sy-44,2f);
        sr.rectLine(sx+40,sy-44,sx+44,sy-44,2f);

        // Neck
        sr.setColor(f?Color.WHITE:DARK);
        sr.rect(sx-5,sy+32,10,10);

        // Helmet — elongated
        sr.setColor(f?Color.WHITE:ARMOR);
        sr.rect(sx-13,sy+42,26,24);
        sr.setColor(f?Color.WHITE:DARK);
        sr.rect(sx-10,sy+44,20,18);
        // Eye slits
        sr.setColor(f?Color.WHITE:RED);
        sr.rect(sx-9,sy+50,8,4); sr.rect(sx+1,sy+50,8,4);
        sr.setColor(f?Color.WHITE:new Color(0.98f,0.64f,0.64f,1f));
        sr.rect(sx-8,sy+51,6,2); sr.rect(sx+2,sy+51,6,2);

        // Crown spikes
        sr.setColor(f?Color.WHITE:new Color(0.50f,0.08f,0.08f,1f));
        sr.triangle(sx-11,sy+66,sx-13,sy+78,sx-7,sy+66);
        sr.triangle(sx-4,sy+66,sx-4,sy+82,sx+2,sy+66);
        sr.triangle(sx+4,sy+66,sx+6,sy+84,sx+10,sy+66);
        sr.triangle(sx+11,sy+66,sx+14,sy+76,sx+16,sy+66);
        // Crown gems
        sr.setColor(f?Color.WHITE:RED);
        sr.circle(sx-1,sy+80,3.5f);
        sr.setColor(f?Color.WHITE:new Color(0.98f,0.64f,0.64f,1f));
        sr.circle(sx+7,sy+82,2.5f);

        // Phase indicator ring
        Color pc = phase==Phase.THREE?new Color(1f,0.2f,0.2f,0.8f):
                   phase==Phase.TWO ?new Color(1f,0.5f,0.2f,0.6f):new Color(0.8f,0.3f,0.3f,0.4f);
        sr.setColor(pc); sr.circle(sx,sy,radius+4);

        drawHpBar(sr);
    }
}
