package com.uuz.fabrictestproj.mixin;

import com.uuz.fabrictestproj.manager.AllCanEatManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "finishUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"))
    private void onFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (AllCanEatManager.isEnabled() && user instanceof PlayerEntity player) {
            AllCanEatManager.onItemUseComplete(player, (ItemStack)(Object)this);
        }
    }

    @Inject(method = "getUseAction()Lnet/minecraft/util/UseAction;", at = @At("HEAD"), cancellable = true)
    private void onGetUseAction(CallbackInfoReturnable<UseAction> cir) {
        UseAction action = AllCanEatManager.getUseAction((ItemStack)(Object)this);
        if (action != UseAction.NONE) {
            cir.setReturnValue(action);
        }
    }

    @Inject(method = "getMaxUseTime()I", at = @At("HEAD"), cancellable = true)
    private void onGetMaxUseTime(CallbackInfoReturnable<Integer> cir) {
        int useTime = AllCanEatManager.getMaxUseTime((ItemStack)(Object)this);
        if (useTime > 0) {
            cir.setReturnValue(useTime);
        }
    }
} 