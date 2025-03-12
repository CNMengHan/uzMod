package com.uuz.fabrictestproj.client;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class InfiniteArrowsManager {
    private static boolean enabled = false;

    public static boolean isEnabled() {
        return enabled;
    }
    
    public static boolean hasInfiniteArrows() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        InfiniteArrowsManager.enabled = enabled;
    }

    public static ItemStack getInfiniteArrow() {
        return new ItemStack(Items.ARROW);
    }
} 