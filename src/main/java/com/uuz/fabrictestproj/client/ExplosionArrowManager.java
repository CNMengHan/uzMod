package com.uuz.fabrictestproj.client;

public class ExplosionArrowManager {
    private static boolean enabled = false;
    public static final float EXPLOSION_POWER = 3.0F; // 苦力怕爆炸威力

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        ExplosionArrowManager.enabled = enabled;
    }
} 