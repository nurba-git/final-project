package com.narxoz.darkrealm.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SoundManager — процедурные звуки без аудио-файлов.
 * Генерирует PCM-семплы и воспроизводит через AudioDevice LibGDX.
 * SOLID-S: только генерация и воспроизведение звуков.
 */
public class SoundManager {

    private final ExecutorService pool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "sound-thread");
        t.setDaemon(true);
        return t;
    });

    private float masterVolume = 0.6f;
    private boolean enabled = true;

    // Cooldowns чтобы не спамить звуками (в секундах от System.currentTimeMillis)
    private long lastHit    = 0;
    private long lastShoot  = 0;
    private long lastStep   = 0;
    private long lastDeath  = 0;
    private long lastLvlUp  = 0;

    private static final long HIT_CD   = 120;
    private static final long SHOOT_CD = 80;
    private static final long STEP_CD  = 220;
    private static final long DEATH_CD = 300;
    private static final long LVLUP_CD = 500;

    public SoundManager() {}

    public void setEnabled(boolean e) { this.enabled = e; }
    public boolean isEnabled() { return enabled; }
    public void setVolume(float v) { this.masterVolume = Math.max(0f, Math.min(1f, v)); }

    // ── Public API ────────────────────────────────────────────────

    /** Удар оружием по врагу */
    public void playHit() {
        long now = System.currentTimeMillis();
        if (!enabled || now - lastHit < HIT_CD) return;
        lastHit = now;
        final float vol = masterVolume;
        pool.execute(() -> playTone(new float[]{180f, 90f}, new float[]{0.04f, 0.06f},
                                   WaveType.NOISE_SQUARE, vol * 0.55f));
    }

    /** Выстрел (пуля или стрела врага) */
    public void playShoot() {
        long now = System.currentTimeMillis();
        if (!enabled || now - lastShoot < SHOOT_CD) return;
        lastShoot = now;
        final float vol = masterVolume;
        pool.execute(() -> playTone(new float[]{440f, 220f, 110f}, new float[]{0.02f, 0.03f, 0.02f},
                                   WaveType.SQUARE, vol * 0.35f));
    }

    /** Шаг игрока */
    public void playStep() {
        long now = System.currentTimeMillis();
        if (!enabled || now - lastStep < STEP_CD) return;
        lastStep = now;
        final float vol = masterVolume;
        pool.execute(() -> playTone(new float[]{60f, 45f}, new float[]{0.025f, 0.025f},
                                   WaveType.NOISE, vol * 0.25f));
    }

    /** Смерть врага */
    public void playEnemyDeath() {
        long now = System.currentTimeMillis();
        if (!enabled || now - lastDeath < DEATH_CD) return;
        lastDeath = now;
        final float vol = masterVolume;
        pool.execute(() -> playTone(new float[]{300f, 180f, 90f, 50f},
                                   new float[]{0.04f, 0.05f, 0.06f, 0.08f},
                                   WaveType.NOISE_SQUARE, vol * 0.5f));
    }

    /** Повышение уровня */
    public void playLevelUp() {
        long now = System.currentTimeMillis();
        if (!enabled || now - lastLvlUp < LVLUP_CD) return;
        lastLvlUp = now;
        final float vol = masterVolume;
        pool.execute(() -> {
            // Восходящий аккорд
            float[] freqs = {261.63f, 329.63f, 392f, 523.25f};
            float[] durs  = {0.08f,   0.08f,   0.08f, 0.18f};
            playTone(freqs, durs, WaveType.SINE, vol * 0.6f);
        });
    }

    /** Получение урона игроком */
    public void playPlayerHurt() {
        if (!enabled) return;
        final float vol = masterVolume;
        pool.execute(() -> playTone(new float[]{150f, 80f}, new float[]{0.06f, 0.08f},
                                   WaveType.NOISE_SQUARE, vol * 0.7f));
    }

    /** Подбор предмета / активация shrine */
    public void playPickup() {
        if (!enabled) return;
        final float vol = masterVolume;
        pool.execute(() -> playTone(new float[]{660f, 880f}, new float[]{0.05f, 0.1f},
                                   WaveType.SINE, vol * 0.5f));
    }

    // ── Внутренняя генерация PCM ──────────────────────────────────

    private enum WaveType { SINE, SQUARE, NOISE, NOISE_SQUARE }

    private void playTone(float[] freqs, float[] durations, WaveType wave, float amplitude) {
        try {
            int rate = 44100;
            // Считаем суммарную длину
            int totalSamples = 0;
            for (float d : durations) totalSamples += (int)(rate * d);
            short[] buf = new short[totalSamples];

            int pos = 0;
            for (int seg = 0; seg < freqs.length && seg < durations.length; seg++) {
                int segLen = (int)(rate * durations[seg]);
                float freq = freqs[seg];
                for (int i = 0; i < segLen && pos < buf.length; i++, pos++) {
                    float t = (float) i / rate;
                    float env = 1f - (float) i / segLen; // linear decay
                    float sample;
                    switch (wave) {
                        case SQUARE:
                            sample = (float)Math.sin(2 * Math.PI * freq * t) >= 0 ? 1f : -1f;
                            break;
                        case NOISE:
                            sample = (float)(Math.random() * 2 - 1);
                            break;
                        case NOISE_SQUARE:
                            float ns = (float)(Math.random() * 2 - 1);
                            float sq = (float)Math.sin(2 * Math.PI * freq * t) >= 0 ? 1f : -1f;
                            sample = (ns * 0.4f + sq * 0.6f);
                            break;
                        default: // SINE
                            sample = (float)Math.sin(2 * Math.PI * freq * t);
                            break;
                    }
                    buf[pos] = (short)(sample * env * amplitude * Short.MAX_VALUE);
                }
            }

            AudioDevice dev = Gdx.audio.newAudioDevice(rate, true);
            dev.writeSamples(buf, 0, buf.length);
            dev.dispose();
        } catch (Exception ignored) {
            // Аудио может быть недоступно в headless-режиме
        }
    }

    public void dispose() {
        pool.shutdownNow();
    }
}
