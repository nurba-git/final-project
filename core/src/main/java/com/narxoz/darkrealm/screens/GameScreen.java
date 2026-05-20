package com.narxoz.darkrealm.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.*;
import com.narxoz.darkrealm.entities.*;
import com.narxoz.darkrealm.interfaces.IInputHandler;
import com.narxoz.darkrealm.systems.*;
import com.narxoz.darkrealm.world.*;

import com.narxoz.darkrealm.screens.VictoryScreen;
import com.narxoz.darkrealm.screens.PauseScreen;
import com.narxoz.darkrealm.screens.GameOverScreen;
import com.narxoz.darkrealm.screens.MainMenuScreen;

import java.util.ArrayList;
import java.util.List;


public class GameScreen implements Screen {

    private final DarkRealmGame game;
    private final IInputHandler input;

    private OrthographicCamera cam, hudCam;
    private ShapeRenderer sr;

    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private Malgrath boss = null;
    private boolean bossSpawned = false;

    private BulletPool bp;
    private CollisionSystem col;
    private ParticleSystem fx;
    private QuestManager qm;
    private HUD hud;

    private DungeonGenerator.DungeonData dungeon;
    private int zone;
    private int kills = 0;
    private int lastPlayerLevel = 1;

    private List<float[]> shrines = new ArrayList<>();   // {x,y,used}
    private List<float[]> chests  = new ArrayList<>();   // {x,y,opened}

    private VillageElder npc;
    private String dialogLine = null;
    private float dialogTimer = 0f;

    private float darkAlpha = 0f;

    private final List<Enemy> toRemove = new ArrayList<>();

    public GameScreen(DarkRealmGame game, int zone) {
        this.game = game;
        this.zone = zone;
        game.currentZone = zone;

        cam    = new OrthographicCamera(C.W, C.H);
        hudCam = new OrthographicCamera(C.W, C.H);
        hudCam.setToOrtho(false, C.W, C.H);
        hudCam.update();

        sr = game.sr;
        input = new KeyboardInputHandler(cam);

        loadZone(zone);
    }

    private void loadZone(int z) {
        DungeonGenerator gen = new DungeonGenerator();
        dungeon = gen.generate(z);

        col = new CollisionSystem(dungeon.map);
        bp  = new BulletPool(dungeon.map);
        fx  = new ParticleSystem();
        qm  = new QuestManager();
        qm.activateZoneQuests(z);
        hud = new HUD(game.font, game.fontSmall);
        hud.setZone(z);

        player = new Player(dungeon.spawnX, dungeon.spawnY, col, fx);

        enemies = EnemyFactory.spawnForZone(dungeon.rooms, z, col, bp, fx);
        enemies.forEach(e -> e.setTarget(player));

        if (z == 3) {
            boss = EnemyFactory.spawnBoss(
                dungeon.rooms.get(dungeon.rooms.size()-1), col, bp, fx);
            boss.setTarget(player);
            boss.setSpawnList(enemies);
            bossSpawned = false;
        }

        Room r0 = dungeon.rooms.get(0);
        shrines.add(new float[]{r0.centerX()*C.TILE+C.TILE/2f+20, r0.centerY()*C.TILE+C.TILE/2f, 0});

        for (int i = 1; i < dungeon.rooms.size()-1; i++) {
            if (Math.random() < 0.5f) {
                Room rc = dungeon.rooms.get(i);
                chests.add(new float[]{rc.centerX()*C.TILE+C.TILE/2f, rc.centerY()*C.TILE+C.TILE/2f, 0});
            }
        }
        if (z <= 2 && dungeon.rooms.size() > 2) {
            Room nr = dungeon.rooms.get(1);
            npc = new VillageElder(nr.centerX()*C.TILE+C.TILE/2f+24,
                nr.centerY()*C.TILE+C.TILE/2f, z);
        }

        darkAlpha = z == 3 ? 0.55f : 0f;
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f);
        update(delta);
        draw();
    }

    private void update(float dt) {
        input.update();
        player.update(dt, input);

        cam.position.set(player.x, player.y, 0);
        cam.update();

        bp.update(dt);
        fx.update(dt);
        hud.update(dt);

        if (dialogTimer > 0) dialogTimer -= dt;

        if (input.isSkill()) {
            if (!player.tryShadowStrike(enemies))
                if (!player.tryVoidShield())
                    player.trySoulDrain(enemies);
        }

        if (input.isAttack() && player.tryAttack()) {
            game.sound.playHit();
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                if (CollisionSystem.dist(player.x, player.y, e.x, e.y)
                    < C.ATTACK_RANGE + e.radius
                    && CollisionSystem.hasLineOfSight(col.getMap(), player.x, player.y, e.x, e.y))
                    e.takeDamage(player.atk);
            }
            if (boss != null && boss.isAlive()) {
                if (CollisionSystem.dist(player.x, player.y, boss.x, boss.y)
                    < C.ATTACK_RANGE + boss.radius
                    && CollisionSystem.hasLineOfSight(col.getMap(), player.x, player.y, boss.x, boss.y))
                    boss.takeDamage(player.atk);
            }
        }

        for (Enemy e : enemies) {
            if (!e.isAlive()) { toRemove.add(e); continue; }
            e.update(dt);
        }
        for (Enemy e : toRemove) {
            player.gainExp(e.expReward);
            qm.notifyKill(e.type);
            kills++; game.totalKills++;
            game.sound.playEnemyDeath();
        }
        enemies.removeAll(toRemove);
        toRemove.clear();

        if (zone == 3 && !bossSpawned &&
            enemies.isEmpty() &&
            CollisionSystem.dist(player.x, player.y,
                dungeon.exitTX * C.TILE + C.TILE/2f,
                dungeon.exitTY * C.TILE + C.TILE/2f) < 40) {
            bossSpawned = true;
        }
        if (boss != null && bossSpawned) {
            if (boss.isAlive()) boss.update(dt);
            else if (!boss.isAlive() && kills > 0) {
                // Boss dead → victory
                qm.notifyKill("malgrath");
                game.questsDone = qm.countDone();
                game.setScreen(new VictoryScreen(game, kills));
            }
        }

        for (Bullet b : new ArrayList<>(bp.getActive())) {
            if (!b.fromPlayer || !b.alive) continue;
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                if (CollisionSystem.circles(b.x,b.y,C.BULLET_RADIUS,e.x,e.y,e.radius)) {
                    e.takeDamage(b.damage); b.alive=false; break;
                }
            }
            if (b.alive && boss!=null && boss.isAlive()
                && CollisionSystem.circles(b.x,b.y,C.BULLET_RADIUS,boss.x,boss.y,boss.radius)) {
                boss.takeDamage(b.damage); b.alive=false;
            }
        }

        for (Bullet b : new ArrayList<>(bp.getActive())) {
            if (b.fromPlayer || !b.alive) continue;
            if (CollisionSystem.circles(b.x,b.y,C.BULLET_RADIUS,player.x,player.y,C.PLAYER_RADIUS)) {
                player.takeDamage(b.damage); b.alive=false;
                game.sound.playPlayerHurt();
            }
        }

        if (zone == 3) {
            int t = CollisionSystem.tileAt(dungeon.map, player.x, player.y);
            if (t == C.HAZARD) player.takeDamage((int)(C.HAZARD_DPS * dt));
        }

        for (float[] s : shrines) {
            if (s[2]==1) continue;
            if (CollisionSystem.dist(player.x,player.y,s[0],s[1]) < 16) {
                player.heal(30); s[2]=1;
                fx.burst(s[0],s[1],new Color(0.2f,0.9f,0.4f,1f),10,2f,0.5f);
                hud.showQuestMsg("Shrine — +30 HP");
                game.sound.playPickup();
            }
        }

        if (input.isInteract()) {
            for (float[] ch : chests) {
                if (ch[2] == 1) continue;
                if (CollisionSystem.dist(player.x, player.y, ch[0], ch[1]) < 20) {
                    ch[2] = 1;
                    game.sound.playPickup();
                    fx.burst(ch[0], ch[1], new Color(0.83f, 0.63f, 0.09f, 1f), 14, 2.5f, 0.6f);
                    int roll = (int)(Math.random() * 3);
                    if (roll == 0) {
                        int heal = 20 + (int)(Math.random() * 31); // 20-50
                        player.heal(heal);
                        hud.showQuestMsg("Chest opened! +" + heal + " HP");
                    } else if (roll == 1) {
                        player.gainExp(40 + (int)(Math.random() * 61)); // 40-100 EXP
                        hud.showQuestMsg("Chest opened! +EXP");
                    } else {
                        player.atk += 2;
                        hud.showQuestMsg("Chest opened! +2 ATK");
                    }
                    break;
                }
            }
        }

        if (npc != null && input.isInteract() && npc.isNear(player.x,player.y)) {
            dialogLine = npc.interact();
            dialogTimer = 3f;
            if (dialogLine.contains("Quest:")) {
                qm.activateZoneQuests(zone);
                hud.showQuestMsg(dialogLine);
            }
        }

        for (QuestManager.Quest q : qm.getJustCompleted()) {
            player.gainExp(q.expReward);
            hud.showQuestMsg("Quest done: "+q.name+"! +"+q.expReward+" EXP");
            game.questsDone++;
        }

        boolean cleared = enemies.isEmpty() && (zone!=3 || (boss!=null && !boss.isAlive()));
        float ex=dungeon.exitTX*C.TILE+C.TILE/2f, ey=dungeon.exitTY*C.TILE+C.TILE/2f;
        if (cleared && CollisionSystem.dist(player.x,player.y,ex,ey)<16) {
            if (zone < 3) game.setScreen(new GameScreen(game, zone+1));
        }

        if (player.level > lastPlayerLevel) {
            lastPlayerLevel = player.level;
            game.sound.playLevelUp();
        }

        if (input.isPause()) game.setScreen(new PauseScreen(game, this));

        if (!player.isAlive()) game.setScreen(new GameOverScreen(game, zone, kills));
    }

    private void draw() {
        Gdx.gl.glClearColor(0.04f, 0.02f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sr.setProjectionMatrix(cam.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        drawTiles();
        drawInteractables();
        if (npc != null) npc.draw(sr);
        for (Enemy e : enemies) e.draw(sr);
        if (boss != null && bossSpawned && boss.isAlive()) boss.draw(sr);
        bp.draw(sr);
        fx.draw(sr);
        player.draw(sr);

        sr.end();

        if (darkAlpha > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0f,0f,0f,darkAlpha);
            sr.rect(player.x-C.W,player.y-C.H, C.W*2, C.H*2);
            sr.setColor(0f,0f,0f,0f); sr.circle(player.x,player.y,72);
            sr.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        if (dialogTimer > 0 && dialogLine != null) {
            sr.setProjectionMatrix(hudCam.combined);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0f,0f,0f,0.75f); sr.rect(C.W/2f-150,C.H-60,300,36);
            sr.end();
            game.batch.setProjectionMatrix(hudCam.combined);
            game.batch.begin();
            game.fontSmall.setColor(Color.WHITE);
            game.fontSmall.draw(game.batch, dialogLine, C.W/2f-140, C.H-34);
            game.batch.end();
        }

        boolean nearChest = false;
        for (float[] ch : chests) {
            if (ch[2] == 1) continue;
            if (CollisionSystem.dist(player.x, player.y, ch[0], ch[1]) < 20) {
                nearChest = true;
                break;
            }
        }
        hud.setChestHint(nearChest);

        sr.setProjectionMatrix(hudCam.combined);
        game.batch.setProjectionMatrix(hudCam.combined);
        boolean bossActive = bossSpawned && boss!=null && boss.isAlive();
        int bHp = bossActive ? boss.getHp() : 0;
        int bMax = bossActive ? boss.getMaxHp() : 0;
        hud.draw(sr, game.batch, player, dungeon.map, kills, player.level, bossActive, bHp, bMax,
            chests, enemies, boss, bossSpawned);
    }

    private void drawTiles() {
        int sx0=Math.max(0,(int)((cam.position.x-C.W/2f)/C.TILE));
        int ex0=Math.min(C.COLS,sx0+C.W/C.TILE+2);
        int sy0=Math.max(0,(int)((cam.position.y-C.H/2f)/C.TILE));
        int ey0=Math.min(C.ROWS,sy0+C.H/C.TILE+2);

        boolean cleared = enemies.isEmpty();

        for (int r=sy0;r<ey0;r++) for (int c=sx0;c<ex0;c++) {
            int t=dungeon.map[r][c];
            float tx=c*C.TILE, ty=r*C.TILE;
            if (t==C.WALL) {
                sr.setColor(0.07f,0.04f,0.11f,1f); sr.rect(tx,ty,C.TILE,C.TILE);
                sr.setColor(0.11f,0.06f,0.18f,1f); sr.rect(tx,ty+C.TILE-2,C.TILE,2);
            } else if (t==C.HAZARD) {
                sr.setColor(0.09f,0.18f,0.04f,1f); sr.rect(tx,ty,C.TILE,C.TILE);
                sr.setColor(0.25f,0.50f,0.05f,0.6f); sr.circle(tx+8,ty+8,5);
            } else if (t==C.EXIT) {
                sr.setColor((c+r)%2==0?0.10f:0.11f,0.05f,0.16f,1f); sr.rect(tx,ty,C.TILE,C.TILE);
                if (cleared) {
                    sr.setColor(0.04f,0.31f,0.14f,1f); sr.rect(tx+2,ty+2,C.TILE-4,C.TILE-4);
                    sr.setColor(0.20f,0.83f,0.36f,1f); sr.rect(tx+4,ty+4,C.TILE-8,C.TILE-8);
                }
            } else {
                float sh=(c+r)%2==0?0.08f:0.09f;
                sr.setColor(sh,sh*0.7f,sh*1.3f,1f); sr.rect(tx,ty,C.TILE,C.TILE);
            }
        }
    }

    private void drawInteractables() {
        for (float[] s : shrines) {
            if (s[2]==1) { sr.setColor(0.2f,0.2f,0.2f,0.5f); sr.circle(s[0],s[1],6); continue; }
            sr.setColor(0.2f,0.9f,0.4f,0.9f); sr.circle(s[0],s[1],6);
            sr.setColor(0.6f,1f,0.7f,0.4f); sr.circle(s[0],s[1],11);
        }
        for (float[] ch : chests) {
            if (ch[2]==1) { sr.setColor(0.3f,0.2f,0.1f,0.5f); sr.rect(ch[0]-6,ch[1]-5,12,10); continue; }
            sr.setColor(0.5f,0.35f,0.08f,1f); sr.rect(ch[0]-7,ch[1]-6,14,12);
            sr.setColor(0.72f,0.52f,0.14f,1f); sr.rect(ch[0]-5,ch[1]-4,10,8);
            sr.setColor(0.83f,0.63f,0.09f,1f); sr.rect(ch[0]-2,ch[1]-2,4,4);
        }
    }

    public void resume() { /* nothing needed */ }

    @Override public void resize(int w,int h) {}
    @Override public void pause() {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
