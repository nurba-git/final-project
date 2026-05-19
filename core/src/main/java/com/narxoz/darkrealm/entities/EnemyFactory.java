package com.narxoz.darkrealm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.narxoz.darkrealm.systems.*;
import com.narxoz.darkrealm.world.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyFactory {

    private static final Random rng = new Random();

    public static List<Enemy> spawnForZone(List<Room> rooms, int zone,
            CollisionSystem col, BulletPool bp, ParticleSystem fx) {
        List<Enemy> list = new ArrayList<>();
    
        for (int i = 1; i < rooms.size() - 1; i++) {
            Room r = rooms.get(i);
            int count = 2 + Math.min(rng.nextInt(2), zone - 1);
            for (int j = 0; j < count; j++) {
                float ex = (r.x + 1 + rng.nextInt(Math.max(1,r.w-2))) * 16f + 8;
                float ey = (r.y + 1 + rng.nextInt(Math.max(1,r.h-2))) * 16f + 8;
                Enemy e = pick(zone, ex, ey, col, bp, fx);
                list.add(e);
            }
        }
        return list;
    }

    public static Malgrath spawnBoss(Room bossRoom, CollisionSystem col,
                                     BulletPool bp, ParticleSystem fx) {
        float bx = bossRoom.centerX() * 16f + 8;
        float by = bossRoom.centerY() * 16f + 8;
        return new Malgrath(bx, by, col, bp, fx);
    }

    private static Enemy pick(int zone, float x, float y,
                               CollisionSystem col, BulletPool bp, ParticleSystem fx) {
        int r = rng.nextInt(100);
        switch (zone) {
            case 1: return r < 60 ? new ShadowWraith(x,y,col,bp,fx) : new BoneArcher(x,y,col,bp,fx);
            case 2: if (r < 35) return new CursedKnight(x,y,col,bp,fx);
                    if (r < 70) return new PlagueHound(x,y,col,bp,fx);
                    return new ShadowWraith(x,y,col,bp,fx);
            default:if (r < 40) return new VoidSorcerer(x,y,col,bp,fx);
                    if (r < 75) return new GolemSentinel(x,y,col,bp,fx);
                    return new CursedKnight(x,y,col,bp,fx);
        }
    }
}
