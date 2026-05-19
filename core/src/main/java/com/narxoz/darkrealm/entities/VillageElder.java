package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.systems.CollisionSystem;

/** NPC — квест-гивер. Не является Enemy. SOLID-S: только диалог + рендер. */
public class VillageElder {

    public float x, y;
    private static final float RADIUS = 10f;
    private static final Color ROBE   = new Color(0.23f,0.17f,0.10f,1f);
    private static final Color GOLD   = new Color(0.83f,0.63f,0.09f,1f);
    private static final Color SKIN   = new Color(0.36f,0.24f,0.12f,1f);

    private String[] lines;
    private boolean talking = false;
    private int lineIdx = 0;

    public VillageElder(float x, float y, int zone) {
        this.x = x; this.y = y;
        if (zone == 1) lines = new String[]{
            "Welcome, warrior. Dark times have come.",
            "Shadow Wraiths roam the Ashen Plains.",
            "Defeat 5 of them — I'll reward you well.",
            "[Quest: The First Shadow accepted]"
        };
        else lines = new String[]{
            "The Cursed Knights guard the citadel gates.",
            "Destroy 3 barricades to open the path.",
            "[Quest: Shattered Ramparts accepted]"
        };
    }

    public boolean isNear(float px, float py) {
        return CollisionSystem.dist(px,py,x,y) < RADIUS + 20f;
    }

    public String interact() {
        talking = true;
        String l = lines[lineIdx];
        lineIdx = (lineIdx + 1) % lines.length;
        return l;
    }

    public void draw(ShapeRenderer sr) {
        float sx=x, sy=y;

        // Shadow
        sr.setColor(0f,0f,0f,0.2f); sr.ellipse(sx-8,sy-16,16,5);

        // Robe
        sr.setColor(ROBE);
        sr.triangle(sx-8,sy+6,sx+8,sy+6,sx+10,sy-18);
        sr.triangle(sx-8,sy+6,sx-10,sy-18,sx+10,sy-18);
        // Gold sash trim
        sr.setColor(GOLD);
        sr.rect(sx-8,sy+0,16,3);

        // Arms
        sr.setColor(ROBE);
        sr.rectLine(sx-8,sy+2,sx-14,sy-12,6f);
        sr.rectLine(sx+8,sy+2,sx+14,sy-12,6f);

        // Hands
        sr.setColor(SKIN);
        sr.circle(sx-14,sy-13,4); sr.circle(sx+14,sy-13,4);

        // Staff
        sr.setColor(new Color(0.36f,0.24f,0.12f,1f));
        sr.rectLine(sx-14,sy-13,sx-16,sy+28,2.5f);
        sr.setColor(GOLD);
        sr.circle(sx-16,sy+30,5);

        // Body
        sr.setColor(new Color(0.18f,0.13f,0.08f,1f));
        sr.rect(sx-6,sy+4,12,10);

        // Head
        sr.setColor(SKIN);
        sr.circle(sx+1,sy+18,9);
        // White beard
        sr.setColor(new Color(0.9f,0.9f,0.9f,1f));
        sr.triangle(sx-6,sy+12,sx-5,sy+24,sx+7,sy+24);
        sr.triangle(sx-5,sy+24,sx+7,sy+24,sx+6,sy+32);
        // Mustache
        sr.setColor(new Color(0.85f,0.85f,0.85f,1f));
        sr.rectLine(sx-5,sy+14,sx+7,sy+14,1.5f);
        // Eyebrows (old bushy)
        sr.setColor(new Color(0.85f,0.85f,0.85f,1f));
        sr.rectLine(sx-5,sy+21,sx-1,sy+22,2f);
        sr.rectLine(sx+2,sy+22,sx+6,sy+21,2f);
        // Eyes
        sr.setColor(new Color(0.12f,0.23f,0.50f,1f));
        sr.circle(sx-3,sy+18,2.5f); sr.circle(sx+5,sy+18,2.5f);
        // Hood
        sr.setColor(ROBE);
        sr.triangle(sx-8,sy+14,sx+1,sy+30,sx+10,sy+14);
        sr.triangle(sx-9,sy+12,sx+1,sy+28,sx-8,sy+14);

        // Interaction indicator
        sr.setColor(GOLD);
        sr.circle(sx+1,sy+36,3);
    }
}
