package com.uuz.fabrictestproj;

import com.uuz.fabrictestproj.command.TakeNearbyPackCommand;
import com.uuz.fabrictestproj.command.TakeLostItemsCommand;
import com.uuz.fabrictestproj.command.InfiniteArrowsCommand;
import com.uuz.fabrictestproj.command.ExplosionArrowCommand;
import com.uuz.fabrictestproj.command.DebugHUDCommand;
import com.uuz.fabrictestproj.command.AllCanEatCommand;
import com.uuz.fabrictestproj.command.UnbreakingCommand;
import com.uuz.fabrictestproj.command.TeleportCommand;
import com.uuz.fabrictestproj.command.HomeCommand;
import com.uuz.fabrictestproj.command.MineralHUDCommand;
import com.uuz.fabrictestproj.command.RuleCommand;
import com.uuz.fabrictestproj.command.PingCommand;
import com.uuz.fabrictestproj.command.SystemCommand;
import com.uuz.fabrictestproj.command.MobComeToMeCommand;
import com.uuz.fabrictestproj.command.ChatCommand;
import com.uuz.fabrictestproj.command.SummonCommand;
import com.uuz.fabrictestproj.command.BoatFlyCommand;
import com.uuz.fabrictestproj.command.VillagerFoodCommand;
import com.uuz.fabrictestproj.command.FindChunkDoorAndVillagerCommand;
import com.uuz.fabrictestproj.handler.EnderTeleportHandler;
import com.uuz.fabrictestproj.item.ModItems;
import com.uuz.fabrictestproj.network.CatDropPacket;
import com.uuz.fabrictestproj.network.BlockRandomizePacket;
import com.uuz.fabrictestproj.network.BoatFlyInputPacket;
import com.uuz.fabrictestproj.manager.AllCanEatManager;
import com.uuz.fabrictestproj.manager.UnbreakingManager;
import com.uuz.fabrictestproj.manager.HomeManager;
import com.uuz.fabrictestproj.manager.LowHealthManager;
import com.uuz.fabrictestproj.manager.AfkManager;
import com.uuz.fabrictestproj.manager.VillagerGiftManager;
import com.uuz.fabrictestproj.manager.VillagerFoodManager;
import com.uuz.fabrictestproj.manager.BoatFlyManager;
import com.uuz.fabrictestproj.world.SkyIslandCommand;
import com.uuz.fabrictestproj.world.SkyIslandWorldType;
import com.uuz.fabrictestproj.handler.MobEquipmentHandler;
import com.uuz.fabrictestproj.handler.PlayerAttributeHandler;
import com.uuz.fabrictestproj.handler.BlockBreakHandler;
import com.uuz.fabrictestproj.enchantment.FireballEnchantment;
import com.uuz.fabrictestproj.handler.FireballHandler;
import com.uuz.fabrictestproj.handler.MobDropHandler;
import com.uuz.fabrictestproj.handler.InfiniteArrowsHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UuzFabricTestProj implements ModInitializer {
	public static final String MOD_ID = "uuzfabrictestproj";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			TakeNearbyPackCommand.register(dispatcher);
			TakeLostItemsCommand.register(dispatcher);
			InfiniteArrowsCommand.register(dispatcher);
			ExplosionArrowCommand.register(dispatcher);
			DebugHUDCommand.register(dispatcher);
			AllCanEatCommand.register(dispatcher);
			UnbreakingCommand.register(dispatcher);
			TeleportCommand.register(dispatcher);
			HomeCommand.register(dispatcher);
			MineralHUDCommand.register(dispatcher);
			SkyIslandCommand.register(dispatcher);
			RuleCommand.register(dispatcher);
			PingCommand.register(dispatcher);
			SystemCommand.register(dispatcher);
			MobComeToMeCommand.register(dispatcher, registryAccess);
			ChatCommand.register(dispatcher);
			SummonCommand.register(dispatcher, registryAccess);
			BoatFlyCommand.register(dispatcher);
			VillagerFoodCommand.register(dispatcher);
			FindChunkDoorAndVillagerCommand.register(dispatcher);
		});

		// 注册网络包
		CatDropPacket.register();
		BlockRandomizePacket.register();
		BoatFlyInputPacket.register();

		// 注册方块破坏事件处理器
		BlockBreakHandler.register();
		
		// 初始化全物品可食用功能
		AllCanEatManager.initialize();
		
		// 初始化不消耗耐久度功能
		UnbreakingManager.initialize();
		
		// 初始化濒死通知系统
		LowHealthManager.initialize();
		
		// 初始化家系统
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			HomeManager.initialize(server);
		});
		
		// 初始化AFK提示系统
		AfkManager.initialize();
		
		// 初始化村民礼物系统
		VillagerGiftManager.initialize();
		
		// 初始化村民食物系统
		VillagerFoodManager.initialize();
		
		// 初始化末影人传送功能
		EnderTeleportHandler.registerServer();
		
		// 注册空岛世界类型
		SkyIslandWorldType.register();
		
		// 注册生物装备处理器
		MobEquipmentHandler.register();
		
		// 注册玩家属性处理器
		PlayerAttributeHandler.register();
		
		// 初始化模组物品
		ModItems.initialize();
		
		// 初始化BoatFly功能
		BoatFlyManager.initialize();
		
		// 注册附魔
		FireballEnchantment.register();
		
		// 注册事件处理器
		FireballHandler.register();
		
		// 注册生物掉落处理器
		MobDropHandler.register();
		
		// 注册无限箭矢处理器
		InfiniteArrowsHandler.register();
	}
}