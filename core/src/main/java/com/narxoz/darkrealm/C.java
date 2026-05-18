package com.narxoz.darkrealm;

public final class C {
    private C() {}

    public static final int W = 480;
    public static final int H = 320;

    public static final int TILE  = 16;
    public static final int COLS  = 30;
    public static final int ROWS  = 20;

    public static final int WALL   = 1;
    public static final int FLOOR  = 0;
    public static final int EXIT   = 2;
    public static final int HAZARD = 3;   // void pool (Zone 3)

    public static final float HAZARD_DPS = 5f;

    public static final float PLAYER_SPEED     = 3.0f * 60f;
    public static final float PLAYER_RADIUS    = 5f;
    public static final float DODGE_DURATION   = 0.25f;
    public static final float DODGE_SPEED_MULT = 3.5f;
    public static final float IFRAMES_DURATION = 0.5f;

    public static final float CD_SHADOW_STRIKE = 2f;
    public static final float CD_VOID_SHIELD   = 8f;
    public static final float CD_SOUL_DRAIN    = 6f;
    public static final float SHADOW_STRIKE_RANGE = 80f;
    public static final float SOUL_DRAIN_RANGE    = 60f;
    public static final float VOID_SHIELD_DURATION = 2f;

    public static final float MP_REGEN_INTERVAL = 0.3f;
    public static final int   MP_REGEN_AMOUNT   = 2;

    public static final float ATTACK_RANGE    = 40f;
    public static final float ATTACK_COOLDOWN = 0.4f;

    public static final float BULLET_SPEED  = 5.5f * 60f;
    public static final float BULLET_LIFE   = 2.5f;
    public static final float BULLET_RADIUS = 3f;

    public static final float EXP_SCALE = 1.5f;
}
