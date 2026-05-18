package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.interfaces.IDamageable;
import com.narxoz.darkrealm.interfaces.IUpdatable;
import com.narxoz.darkrealm.systems.BulletPool;
import com.narxoz.darkrealm.systems.CollisionSystem;
import com.narxoz.darkrealm.systems.ParticleSystem;


public abstract class Enemy implements IDamageable, IUpdatable {

    public float x, y;
    public int hp, maxHp, damage;
    public float speed, radius;
    public boolean alive = true;
    public int expReward;
    public String type;

    protected float facing = 0f;
    protected float hitFlash = 0f;
    protected Color bodyColor;

    protected CollisionSystem col;
    protected BulletPool      bp;
    protected ParticleSystem  fx;
    protected Player          target;


    private float patrolAngle = (float)(Math.random() * Math.PI * 2);
    private float patrolTimer = 0f;

    public Enemy(float x, float y, int hp, int damage, float speed, float radius,
                 Color color, CollisionSystem col, BulletPool bp, ParticleSystem fx) {
        this.x=x; this.y=y; this.hp=this.maxHp=hp; this.damage=damage;
        this.speed=speed; this.radius=radius; this.bodyColor=new Color(color);
        this.col=col; this.bp=bp; this.fx=fx;
    }

    public void setTarget(Player p) { this.target = p; }

    @Override
    public void takeDamage(int amount) {
        hp -= amount; if (hp < 0) hp = 0;
        hitFlash = 0.12f;
        if (hp <= 0) {
            alive = false;
            if (fx != null) fx.burst(x, y, bodyColor, 12, 2.5f, 0.6f);
        }
    }

    @Override public boolean isAlive()  { return alive && hp > 0; }
    @Override public int getHp()        { return hp; }
    @Override public int getMaxHp()     { return maxHp; }

    protected void move(float dx, float dy) {
        float[] p = col.move(x, y, dx, dy, radius);
        x = p[0]; y = p[1];
    }

    protected void patrol(float dt) {
        patrolTimer += dt;
        if (patrolTimer > 1.2f) { patrolAngle=(float)(Math.random()*Math.PI*2); patrolTimer=0; }
        move((float)Math.cos(patrolAngle)*speed*dt*0.4f,
             (float)Math.sin(patrolAngle)*speed*dt*0.4f);
    }


    protected void drawHpBar(ShapeRenderer sr) {
        float bw = radius * 2.8f;
        float bx = x - bw/2, by = y + radius + 3;
        sr.setColor(0.15f,0.15f,0.15f,0.8f); sr.rect(bx,by,bw,3);
        float f = (float)hp/maxHp;
        Color c = f > 0.5f ? new Color(0.2f,0.85f,0.3f,1f) : f > 0.25f ?
                  new Color(0.9f,0.7f,0.1f,1f) : new Color(0.9f,0.15f,0.15f,1f);
        sr.setColor(c); sr.rect(bx,by,bw*f,3);
    }

    @Override
    public abstract void update(float dt);

    public void draw(ShapeRenderer sr) {
        if (!isAlive()) return;
        if (hitFlash > 0) hitFlash -= 0.016f;
        boolean flash = hitFlash > 0 && (int)(hitFlash*20)%2==0;
        sr.setColor(flash ? Color.WHITE : bodyColor);
        sr.circle(x, y, radius);
        drawHpBar(sr);
    }
}
