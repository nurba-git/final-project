package com.narxoz.darkrealm.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.C;
import com.narxoz.darkrealm.entities.Enemy;
import com.narxoz.darkrealm.entities.Malgrath;
import com.narxoz.darkrealm.entities.Player;

import java.util.List;

public class HUD {

    private final BitmapFont font, small;
    private int zone=1;
    private String questMsg="";
    private float questMsgTimer=0f;
    private boolean chestHint=false;

    public HUD(BitmapFont font, BitmapFont small){ this.font=font; this.small=small; }
    public void setZone(int z){ zone=z; }
    public void showQuestMsg(String m){ questMsg=m; questMsgTimer=3f; }
    public void setChestHint(boolean v){ chestHint=v; }

    public void update(float dt){ if(questMsgTimer>0) questMsgTimer-=dt; }

    public void draw(ShapeRenderer sr, SpriteBatch batch, Player p, int[][] map,
                     int kills, int level, boolean bossPhase, int bossHp, int bossMax,
                     List<float[]> chests, List<Enemy> enemies, Malgrath boss, boolean bossSpawned) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        bar(sr, 8, C.H-14, 100, 8, (float)p.hp/p.maxHp, new Color(0.85f,0.15f,0.15f,1f));
        bar(sr, 8, C.H-26, 80,  7, (float)p.mp/p.maxMp, new Color(0.30f,0.30f,0.90f,1f));

        skillIcon(sr, 8,  C.H-52, p.getCdSS(), C.CD_SHADOW_STRIKE, new Color(0.49f,0.23f,0.93f,1f));
        skillIcon(sr, 28, C.H-52, p.getCdVS(), C.CD_VOID_SHIELD,   new Color(0.30f,0.30f,0.90f,1f));
        skillIcon(sr, 48, C.H-52, p.getCdSD(), C.CD_SOUL_DRAIN,    new Color(0.85f,0.15f,0.15f,1f));

        minimap(sr, map, p, chests, enemies, boss, bossSpawned);

        if (bossPhase && bossMax>0) {
            sr.setColor(0.10f,0.04f,0.04f,0.85f);
            sr.rect(C.W/2f-100, 8, 200, 12);
            sr.setColor(0.85f,0.15f,0.15f,1f);
            sr.rect(C.W/2f-100, 8, 200f*bossHp/bossMax, 12);
        }

        sr.end();

        batch.begin();
        small.setColor(Color.WHITE);
        small.draw(batch,"HP "+p.hp+"/"+p.maxHp, 8, C.H-3);
        small.draw(batch,"MP "+p.mp+"/"+p.maxMp, 8, C.H-17);
        small.draw(batch,"LVL "+level+"  KILLS "+kills+"  ZONE "+zone, 8, 12);
        small.draw(batch,"[SS]  [VS]  [SD]  Q=use skill", 8, C.H-56);

        if (chestHint) {
            small.setColor(new Color(0.98f,0.85f,0.30f,1f));
            small.draw(batch,"[E] Open chest", C.W/2f-50, 26);
            small.setColor(Color.WHITE);
        }

        if (bossPhase && bossMax>0){
            small.setColor(new Color(0.98f,0.64f,0.64f,1f));
            small.draw(batch,"MALGRATH - THE FALLEN GOD", C.W/2f-70, 28);
        }
        if (questMsgTimer>0){
            font.setColor(new Color(0.98f,0.85f,0.30f,1f));
            font.draw(batch, questMsg, C.W/2f-100, C.H/2f+60);
        }

        small.setColor(new Color(0.6f,0.6f,0.6f,1f));
        small.draw(batch,"MAP", C.W-55, C.H-1);
        small.setColor(Color.WHITE);

        batch.end();
    }

    public void draw(ShapeRenderer sr, SpriteBatch batch, Player p, int[][] map,
                     int kills, int level, boolean bossPhase, int bossHp, int bossMax) {
        draw(sr, batch, p, map, kills, level, bossPhase, bossHp, bossMax, null, null, null, false);
    }

    private void bar(ShapeRenderer sr,float x,float y,float w,float h,float fill,Color col){
        sr.setColor(0.12f,0.12f,0.12f,0.8f); sr.rect(x,y,w,h);
        sr.setColor(col); sr.rect(x,y,Math.max(0,w*fill),h);
    }

    private void skillIcon(ShapeRenderer sr,float x,float y,float cd,float max,Color col){
        sr.setColor(0.10f,0.10f,0.10f,0.8f); sr.rect(x,y,16,16);
        float fill = max > 0 ? (1f - cd / max) : 1f;
        sr.setColor(col); sr.rect(x,y,16,16*fill);
        sr.setColor(Color.WHITE); // border
        sr.rectLine(x,y,x+16,y,1); sr.rectLine(x,y+16,x+16,y+16,1);
        sr.rectLine(x,y,x,y+16,1); sr.rectLine(x+16,y,x+16,y+16,1);
    }

    private void minimap(ShapeRenderer sr, int[][] map, Player p,
                         List<float[]> chests, List<Enemy> enemies,
                         Malgrath boss, boolean bossSpawned) {
        float mmX=C.W-62, mmY=6, sc=2f;

        sr.setColor(0f,0f,0f,0.70f);
        sr.rect(mmX-2, mmY-2, C.COLS*sc+4, C.ROWS*sc+4);

        for(int r=0;r<C.ROWS;r++) for(int c=0;c<C.COLS;c++){
            int t=map[r][c]; if(t==C.WALL) continue;
            if(t==C.EXIT)   sr.setColor(0.20f,0.83f,0.36f,1f);
            else if(t==C.HAZARD) sr.setColor(0.52f,0.80f,0.09f,1f);
            else            sr.setColor(0.22f,0.22f,0.30f,1f);
            sr.rect(mmX+c*sc, mmY+r*sc, sc, sc);
        }

        if (chests != null) {
            for (float[] ch : chests) {
                int cx = (int)(ch[0]/C.TILE), cy = (int)(ch[1]/C.TILE);
                if (ch[2] == 1) sr.setColor(0.35f, 0.25f, 0.10f, 0.8f);
                else            sr.setColor(0.95f, 0.75f, 0.10f, 1f);
                sr.rect(mmX + cx*sc - 0.5f, mmY + cy*sc - 0.5f, sc+1, sc+1);
            }
        }

        if (enemies != null) {
            sr.setColor(0.90f, 0.15f, 0.15f, 0.9f);
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                int ex = (int)(e.x/C.TILE), ey = (int)(e.y/C.TILE);
                sr.rect(mmX + ex*sc, mmY + ey*sc, sc, sc);
            }
        }

        if (boss != null && bossSpawned && boss.isAlive()) {
            int bx = (int)(boss.x/C.TILE), by = (int)(boss.y/C.TILE);
            sr.setColor(0.95f, 0.20f, 0.85f, 1f);
            sr.rect(mmX + bx*sc - 1, mmY + by*sc - 1, sc+2, sc+2);
        }

        int px=(int)(p.x/C.TILE), py=(int)(p.y/C.TILE);
        sr.setColor(0.35f,0.95f,0.50f,1f);
        sr.rect(mmX+px*sc, mmY+py*sc, sc+1, sc+1);

        sr.setColor(0.4f,0.4f,0.5f,0.8f);
        float bw=C.COLS*sc+4, bh=C.ROWS*sc+4;
        sr.rectLine(mmX-2, mmY-2, mmX-2+bw, mmY-2, 1);
        sr.rectLine(mmX-2, mmY-2+bh, mmX-2+bw, mmY-2+bh, 1);
        sr.rectLine(mmX-2, mmY-2, mmX-2, mmY-2+bh, 1);
        sr.rectLine(mmX-2+bw, mmY-2, mmX-2+bw, mmY-2+bh, 1);
    }
}
