package com.narxoz.darkrealm.systems;

import com.narxoz.darkrealm.C;

public class CollisionSystem {

    private int[][] map;

    public CollisionSystem(int[][] map) { this.map = map; }
    public void setMap(int[][] map)     { this.map = map; }
    public int[][] getMap()             { return map; }

    public float[] move(float x, float y, float dx, float dy, float r) {
        float nx = x + dx, ny = y + dy, rc = r - 0.5f;
        if (!solidBox(nx, y, rc)) x = nx;
        if (!solidBox(x, ny, rc)) y = ny;
        return new float[]{x, y};
    }

    private boolean solidBox(float cx, float cy, float r) {
        return solid(cx - r, cy - r) || solid(cx + r, cy - r)
            || solid(cx - r, cy + r) || solid(cx + r, cy + r);
    }

    public static boolean solid(int[][] map, float px, float py) {
        int tx = (int)(px / C.TILE), ty = (int)(py / C.TILE);
        if (tx < 0 || ty < 0 || tx >= C.COLS || ty >= C.ROWS) return true;
        return map[ty][tx] == C.WALL;
    }

    private boolean solid(float px, float py) { return solid(map, px, py); }

    public static int tileAt(int[][] map, float px, float py) {
        int tx = (int)(px / C.TILE), ty = (int)(py / C.TILE);
        if (tx < 0 || ty < 0 || tx >= C.COLS || ty >= C.ROWS) return C.WALL;
        return map[ty][tx];
    }

    public static boolean circles(float ax, float ay, float ar, float bx, float by, float br) {
        float dx = ax - bx, dy = ay - by;
        float sum = ar + br;
        return dx * dx + dy * dy < sum * sum;
    }

    public static float dist(float ax, float ay, float bx, float by) {
        return (float) Math.hypot(ax - bx, ay - by);
    }

    public static float angle(float ax, float ay, float bx, float by) {
        return (float) Math.atan2(by - ay, bx - ax);
    }

    public static boolean hasLineOfSight(int[][] map, float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay;
        float dist = (float) Math.hypot(dx, dy);
        if (dist < 1f) return true;
        int steps = (int)(dist / (C.TILE * 0.5f)) + 2;
        for (int i = 0; i <= steps; i++) {
            float t = (float)i / steps;
            float px = ax + dx * t, py = ay + dy * t;
            if (solid(map, px, py)) return false;
        }
        return true;
    }
}
