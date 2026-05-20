package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.C;
import com.narxoz.darkrealm.systems.*;
class ShadowWraith extends Enemy {

    private static final Color BODY = new Color(0.24f,0.04f,0.40f,1f);
    private static final Color EYES = new Color(0.87f,0.80f,1.00f,1f);
    private static final Color CLAW = new Color(0.49f,0.23f,0.93f,1f);

    private float zigTimer = 0f;
    private float bobTimer = 0f;
    private static final float DETECT = 140f, ATCK = 14f;

    public ShadowWraith(float x,float y, CollisionSystem col, BulletPool bp, ParticleSystem fx){
        super(x,y,40,8,1.8f*60f,6f,BODY,col,bp,fx);
        type="shadow_wraith"; expReward=30;
    }

    @Override public void update(float dt) {
        if (!isAlive()||target==null) return;
        if (hitFlash>0) hitFlash-=dt;
        bobTimer += dt;
        float d = CollisionSystem.dist(x,y,target.x,target.y);
        if (d < DETECT) {
            zigTimer += dt;
            float a = CollisionSystem.angle(x,y,target.x,target.y);
            float zz = (float)Math.sin(zigTimer*5)*0.5f;
            facing = a+zz;
            move((float)Math.cos(facing)*speed*dt,(float)Math.sin(facing)*speed*dt);
            if (d < ATCK+target.RADIUS && !target.isInvincible()
                    && CollisionSystem.hasLineOfSight(col.getMap(), x, y, target.x, target.y))
                target.takeDamage(damage);
        } else patrol(dt);
    }

    @Override public void draw(ShapeRenderer sr) {
        if (!isAlive()) return;
        boolean f = hitFlash>0&&(int)(hitFlash*20)%2==0;
        float bob = (float)Math.sin(bobTimer * 3f) * 2f;
        float sx=x, sy=y+bob;
        
        sr.setColor(f?Color.WHITE:BODY);
        sr.triangle(sx-6,sy-8, sx+6,sy-8, sx+8,sy+7);
        sr.triangle(sx-6,sy-8, sx-8,sy+7, sx+8,sy+7);
       
        sr.setColor(f?Color.WHITE:new Color(0.12f,0.02f,0.22f,1f));
        sr.triangle(sx-8,sy+7,sx-5,sy+11,sx-3,sy+7);
        sr.triangle(sx-3,sy+7, sx-1,sy+12, sx+2,sy+7);
        sr.triangle(sx+2,sy+7, sx+5,sy+11, sx+8,sy+7);

       
        sr.setColor(f?Color.WHITE:new Color(0.30f,0.05f,0.50f,1f));
        sr.circle(sx,sy-3,6);
        
        sr.setColor(f?Color.WHITE:new Color(0.18f,0.03f,0.30f,1f));
        sr.triangle(sx-6,sy-2,sx,sy-13,sx+6,sy-2);

   
        sr.setColor(f?Color.WHITE:EYES);
        sr.circle(sx-2,sy-3,2); sr.circle(sx+2,sy-3,2);
        sr.setColor(f?Color.WHITE:CLAW);
        sr.circle(sx-2,sy-3,1); sr.circle(sx+2,sy-3,1);

        sr.setColor(f?Color.WHITE:CLAW);
        sr.rectLine(sx-6,sy+2,sx-10,sy,1.2f);
        sr.rectLine(sx-6,sy+4,sx-11,sy+3,1.2f);
        sr.rectLine(sx+6,sy+2,sx+10,sy,1.2f);
        sr.rectLine(sx+6,sy+4,sx+11,sy+3,1.2f);

        drawHpBar(sr);
    }
}


class BoneArcher extends Enemy {

    private static final Color BONE  = new Color(0.83f,0.77f,0.60f,1f);
    private static final Color DARK  = new Color(0.54f,0.45f,0.33f,1f);
    private static final Color ARROW = new Color(0.43f,0.30f,0.12f,1f);

    private float fireTimer = 0f;
    private static final float DETECT=160f, MIN_RANGE=70f, FIRE_CD=1.1f;

    public BoneArcher(float x,float y, CollisionSystem col, BulletPool bp, ParticleSystem fx){
        super(x,y,30,6,1.1f*60f,5f,BONE,col,bp,fx);
        type="bone_archer"; expReward=25;
    }

    @Override public void update(float dt) {
        if (!isAlive()||target==null) return;
        if (hitFlash>0) hitFlash-=dt;
        float d = CollisionSystem.dist(x,y,target.x,target.y);
        facing = CollisionSystem.angle(x,y,target.x,target.y);
        if (d < DETECT) {
            if (d < MIN_RANGE) move(-(float)Math.cos(facing)*speed*dt,-(float)Math.sin(facing)*speed*dt);
            fireTimer += dt;
            if (fireTimer >= FIRE_CD && CollisionSystem.hasLineOfSight(col.getMap(), x, y, target.x, target.y)) {
                bp.spawn(x,y,facing+((float)Math.random()-0.5f)*0.25f,
                         3.8f*60f, damage, false, false, new Color(0.85f,0.70f,0.30f,1f));
                fireTimer = 0;
            } else if (fireTimer >= FIRE_CD) { fireTimer = 0; }
        } else patrol(dt);
    }

    @Override public void draw(ShapeRenderer sr) {
        if (!isAlive()) return;
        boolean f = hitFlash>0&&(int)(hitFlash*20)%2==0;
        float sx=x, sy=y;

       
        sr.setColor(f?Color.WHITE:BONE);
        sr.rectLine(sx-3,sy-8,sx-3,sy,1.5f); sr.rectLine(sx+3,sy-8,sx+3,sy,1.5f);

      
        sr.setColor(f?Color.WHITE:BONE);
        sr.rect(sx-5,sy,10,8);
        sr.setColor(f?Color.WHITE:DARK);
        for(int i=0;i<3;i++) sr.rectLine(sx-4,sy+1+i*2,sx+4,sy+1+i*2,0.8f);

     
        sr.setColor(f?Color.WHITE:BONE);
        sr.circle(sx,sy+14,6);
      
        sr.setColor(f?Color.WHITE:new Color(0.75f,0.70f,0.55f,1f));
        sr.rect(sx-4,sy+8,8,3);
        sr.setColor(f?Color.WHITE:DARK);
        for(int i=0;i<3;i++) sr.rectLine(sx-3+i*3,sy+8,sx-3+i*3,sy+10,1f);
      
        sr.setColor(f?Color.WHITE:new Color(0.1f,0.06f,0f,1f));
        sr.circle(sx-2,sy+14,2); sr.circle(sx+2,sy+14,2);

     
        float ba = facing;
        float bx2=sx+(float)Math.cos(ba)*8, by2=sy+(float)Math.sin(ba)*8;
        sr.setColor(f?Color.WHITE:ARROW);
        sr.arc(bx2,by2,4,-ba*57.3f-80,-ba*57.3f+80);
        sr.rectLine(bx2+(float)Math.cos(ba+Math.PI/2)*4,by2+(float)Math.sin(ba+Math.PI/2)*4,
                    bx2-(float)Math.cos(ba+Math.PI/2)*4,by2-(float)Math.sin(ba+Math.PI/2)*4,0.8f);
       
        sr.setColor(f?Color.WHITE:new Color(0.6f,0.4f,0.1f,1f));
        sr.rectLine(sx+(float)Math.cos(ba)*2,sy+(float)Math.sin(ba)*2,bx2,by2,1.5f);

        drawHpBar(sr);
    }
}
