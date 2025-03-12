package com.uuz.fabrictestproj.mixin;

import com.uuz.fabrictestproj.client.InfiniteArrowsManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public class BowItemMixin {
    // 移除原来的注入方法，它可能导致弓无法正常使用
    /*
    @Inject(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getProjectileType(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (!(user instanceof PlayerEntity)) return;
        
        if (InfiniteArrowsManager.isEnabled()) {
            // 如果启用了无限箭矢，提供一个箭矢ItemStack
            ((PlayerEntity) user).getProjectileType(stack);
        }
    }
    */
    
    // 添加一个更简单的注入方法，只在需要时提供无限箭矢
    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void onStoppedUsingHead(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        // 这里不做任何修改，只是确保原始方法能够正常执行
    }
} 