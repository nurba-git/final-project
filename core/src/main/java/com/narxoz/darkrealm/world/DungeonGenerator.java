package com.narxoz.darkrealm.world;

import com.narxoz.darkrealm.C;
import java.util.*;

public class DungeonGenerator {

    private final Random rng;

    public DungeonGenerator(){ rng=new Random(); }
    public DungeonGenerator(long seed){ rng=new Random(seed); }

    public static class DungeonData {
        public int[][] map;
        public List<Room> rooms;
        public float spawnX, spawnY;
        public int exitTX, exitTY;
    }

    public DungeonData generate(int zone) {
        DungeonData d = new DungeonData();
        d.map = new int[C.ROWS][C.COLS];
        d.rooms = new ArrayList<>();

        for(int r=0;r<C.ROWS;r++) Arrays.fill(d.map[r], C.WALL);

        int target = 6 + zone;
        for(int att=0;att<300&&d.rooms.size()<target;att++){
            int rw=rng.nextInt(6)+4, rh=rng.nextInt(4)+3;
            int rx=rng.nextInt(C.COLS-rw-1)+1, ry=rng.nextInt(C.ROWS-rh-1)+1;
            Room cand=new Room(rx,ry,rw,rh);
            boolean ok=true;
            for(Room e:d.rooms) if(cand.overlaps(e)){ok=false;break;}
            if(ok){ carveRoom(d.map,cand); d.rooms.add(cand); }
        }

        for(int i=1;i<d.rooms.size();i++)
            corridor(d.map, d.rooms.get(i-1).centerX(), d.rooms.get(i-1).centerY(),
                d.rooms.get(i).centerX(),   d.rooms.get(i).centerY());

        if(zone==3) addHazards(d.map);

        Room last=d.rooms.get(d.rooms.size()-1);
        d.exitTX=last.centerX(); d.exitTY=last.centerY();
        d.map[d.exitTY][d.exitTX]=C.EXIT;

        Room first=d.rooms.get(0);
        d.spawnX=first.centerX()*C.TILE+C.TILE/2f;
        d.spawnY=first.centerY()*C.TILE+C.TILE/2f;

        return d;
    }

    private void carveRoom(int[][] map,Room r){
        for(int y=r.y;y<r.y+r.h;y++) for(int x=r.x;x<r.x+r.w;x++) map[y][x]=C.FLOOR;
    }

    private void corridor(int[][] map,int x1,int y1,int x2,int y2){
        int x=x1,y=y1;
        while(x!=x2){if(x>=0&&x<C.COLS&&y>=0&&y<C.ROWS) map[y][x]=C.FLOOR; x+=x<x2?1:-1;}
        while(y!=y2){if(x>=0&&x<C.COLS&&y>=0&&y<C.ROWS) map[y][x]=C.FLOOR; y+=y<y2?1:-1;}
        if(x>=0&&x<C.COLS&&y>=0&&y<C.ROWS) map[y][x]=C.FLOOR;
    }

    private void addHazards(int[][] map){
        for(int r=1;r<C.ROWS-1;r++) for(int c=1;c<C.COLS-1;c++)
            if(map[r][c]==C.FLOOR && rng.nextFloat()<0.06f) map[r][c]=C.HAZARD;
    }
}
