package com.uuz.fabrictestproj.handler;

import net.minecraft.entity.ai.goal.GoalSelector;

public interface MobEntityAccessor {
    GoalSelector getGoalSelector();
    GoalSelector getTargetSelector();
} 