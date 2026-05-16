package com.narxoz.darkrealm.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SoundManager {

    private Sound sndHit;
    private Sound sndDeath;
    private Sound sndHurt;
    private Sound sndLevelUp;
    private Sound sndPickup;

    private float masterVolume = 0.55f;
    private boolean enabled    = false; // выставляется в true если инициализация прошла

    private long lastHit = 0, lastDeath = 0, lastHurt = 0;
    private long lastLvl = 0, lastPickup = 0;
    private static final long CD_HIT = 120, CD_DEATH = 250, CD_HURT = 180;
    private static final long CD_LVL = 600, CD_PICKUP = 200;

    public SoundManager() {
        try {
            sndHit    = makeSound(new float[]{180f, 90f},  new float[]{0.04f, 0.06f}, Wave.NOISE_SQ, 0.55f);
            sndDeath  = makeSound(new float[]{260f,140f,70f,35f}, new float[]{0.04f,0.05f,0.06f,0.08f}, Wave.NOISE_SQ, 0.50f);
            sndHurt   = makeSound(new float[]{130f, 65f},  new float[]{0.06f, 0.09f}, Wave.NOISE_SQ, 0.65f);
            sndLevelUp= makeSound(new float[]{261f,329f,392f,523f}, new float[]{0.08f,0.08f,0.08f,0.20f}, Wave.SINE, 0.55f);
            sndPickup = makeSound(new float[]{660f, 880f},  new float[]{0.05f, 0.12f}, Wave.SINE,     0.45f);
            enabled = true;
        } catch (Exception e) {

        }
    }

    public void playHit()         { play(sndHit,    lastHit,   CD_HIT);   lastHit    = System.currentTimeMillis(); }
    public void playEnemyDeath()  { play(sndDeath,  lastDeath, CD_DEATH); lastDeath  = System.currentTimeMillis(); }
    public void playPlayerHurt()  { play(sndHurt,   lastHurt,  CD_HURT);  lastHurt   = System.currentTimeMillis(); }
    public void playLevelUp()     { play(sndLevelUp,lastLvl,   CD_LVL);   lastLvl    = System.currentTimeMillis(); }
    public void playPickup()      { play(sndPickup, lastPickup,CD_PICKUP); lastPickup = System.currentTimeMillis(); }

    public void setVolume(float v) { masterVolume = Math.max(0f, Math.min(1f, v)); }
    public void setEnabled(boolean e) { this.enabled = e; }
    public boolean isEnabled() { return enabled; }

    private void play(Sound s, long last, long cd) {
        if (!enabled || s == null) return;
        if (System.currentTimeMillis() - last < cd) return;
        try { s.play(masterVolume); } catch (Exception ignored) {}
    }

    public void dispose() {
        disposeSound(sndHit);
        disposeSound(sndDeath);
        disposeSound(sndHurt);
        disposeSound(sndLevelUp);
        disposeSound(sndPickup);
    }

    private void disposeSound(Sound s) {
        try { if (s != null) s.dispose(); } catch (Exception ignored) {}
    }

    private enum Wave { SINE, NOISE_SQ }

    private Sound makeSound(float[] freqs, float[] durs, Wave wave, float amp) throws IOException {
        final int RATE = 22050; // 22 kHz достаточно для игровых звуков
        final int CH   = 1;
        final int BITS = 16;


        int totalSamples = 0;
        for (float d : durs) totalSamples += Math.max(1, (int)(RATE * d));

        short[] pcm = new short[totalSamples];
        int pos = 0;

        for (int seg = 0; seg < freqs.length && seg < durs.length; seg++) {
            int len = Math.max(1, (int)(RATE * durs[seg]));
            float f = freqs[seg];
            for (int i = 0; i < len && pos < pcm.length; i++, pos++) {
                float t   = (float) i / RATE;
                float env = 1f - (float) i / len; // linear decay
                float s;
                if (wave == Wave.SINE) {
                    s = (float) Math.sin(2.0 * Math.PI * f * t);
                } else {
                    // NOISE_SQ: mix of noise and square wave
                    float sq = (float) Math.sin(2.0 * Math.PI * f * t) >= 0 ? 1f : -1f;
                    float ns = (float)(Math.random() * 2 - 1);
                    s = sq * 0.65f + ns * 0.35f;
                }
                pcm[pos] = (short)(s * env * amp * 32767f);
            }
        }

        byte[] wav = pcmToWav(pcm, RATE, CH, BITS);
        FileHandle fh = new WavFileHandle(wav);
        return Gdx.audio.newSound(fh);
    }


    private static byte[] pcmToWav(short[] pcm, int rate, int ch, int bits) throws IOException {
        int byteRate   = rate * ch * bits / 8;
        int blockAlign = ch * bits / 8;
        int dataSize   = pcm.length * blockAlign;

        ByteArrayOutputStream bos = new ByteArrayOutputStream(44 + dataSize);
        DataOutputStream dos = new DataOutputStream(bos);

        dos.writeBytes("RIFF");
        writeInt32LE(dos, 36 + dataSize);
        dos.writeBytes("WAVE");

        dos.writeBytes("fmt ");
        writeInt32LE(dos, 16);
        writeInt16LE(dos, 1);
        writeInt16LE(dos, ch);
        writeInt32LE(dos, rate);
        writeInt32LE(dos, byteRate);
        writeInt16LE(dos, blockAlign);
        writeInt16LE(dos, bits);

        dos.writeBytes("data");
        writeInt32LE(dos, dataSize);
        for (short s : pcm) writeInt16LE(dos, s);

        dos.flush();
        return bos.toByteArray();
    }

    private static void writeInt32LE(DataOutputStream d, int v) throws IOException {
        d.write(v & 0xFF); d.write((v >> 8) & 0xFF);
        d.write((v >> 16) & 0xFF); d.write((v >> 24) & 0xFF);
    }

    private static void writeInt16LE(DataOutputStream d, int v) throws IOException {
        d.write(v & 0xFF); d.write((v >> 8) & 0xFF);
    }


    private static class WavFileHandle extends FileHandle {
        private final byte[] data;

        WavFileHandle(byte[] data) {
            super("");
            this.data = data;
        }

        @Override public java.io.InputStream read() {
            return new java.io.ByteArrayInputStream(data);
        }

        @Override public long length() { return data.length; }

        @Override public String extension() { return "wav"; }

        @Override public String name() { return "sound.wav"; }

        @Override public String path() { return "sound.wav"; }

        @Override public String toString() { return "sound.wav"; }

        @Override public boolean exists() { return true; }
    }
}
