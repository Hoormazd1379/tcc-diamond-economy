package net.thecubecollective.diamondeconomy;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.thecubecollective.diamondeconomy.commands.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tccdiamondeconomy implements DedicatedServerModInitializer {
	public static final String MOD_ID = "tcc-diamond-economy";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	private static BalanceManager balanceManager;
	private static NotificationManager notificationManager;
	private static ChestShopManager chestShopManager;
	private static MinecraftServer server;

	@Override
	public void onInitializeServer() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Initializing Diamond Economy mod!");
		
		// TODO: Custom blocks temporarily disabled due to Minecraft 1.21.8 registration issues
		// Register custom blocks
		// net.thecubecollective.diamondeconomy.blocks.ModBlocks.registerBlocks();
		
		// Register server lifecycle events
		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
		
		// Register player join event for pending notifications
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			notificationManager.checkAndSendPendingNotifications(handler.player);
		});
		
		// Register chest shop event handlers
		ChestShopEventHandler.register();
		
		// Register ULTIMATE shop protection system
		UltimateShopProtection.register();
		
		// Register continuous particle effects for shops
		ShopParticleManager.register();
		
		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LOGGER.info("Registering Diamond Economy commands...");
			DepositCommand.register(dispatcher, registryAccess, environment);
			WithdrawCommand.register(dispatcher, registryAccess, environment);
			BalanceCommand.register(dispatcher, registryAccess, environment);
			BaltopCommand.register(dispatcher, registryAccess, environment);
			TransferCommand.register(dispatcher, registryAccess, environment);
			HelpCommand.register(dispatcher, registryAccess, environment);
			CreateShopCommand.register(dispatcher, registryAccess, environment);
			RemoveShopCommand.register(dispatcher, registryAccess, environment);
			ListShopsCommand.register(dispatcher, registryAccess, environment);
			ShopValidateCommand.register(dispatcher, registryAccess, environment);
			LOGGER.info("All Diamond Economy commands registered successfully!");
		});
	}
	
	private void onServerStarting(MinecraftServer server) {
		Tccdiamondeconomy.server = server;
		balanceManager = new BalanceManager(server);
		notificationManager = new NotificationManager(server);
		chestShopManager = new ChestShopManager(server);
		
		// Start shop validation system
		ShopValidationSystem.start(server, chestShopManager);
		
		LOGGER.info("Diamond Economy balance manager, notification system, chest shop manager, and shop validation system initialized!");
	}
	
	private void onServerStopping(MinecraftServer server) {
		// Stop shop validation system
		ShopValidationSystem.stop();
		
		LOGGER.info("Diamond Economy shutting down!");
	}
	
	public static BalanceManager getBalanceManager() {
		return balanceManager;
	}
	
	public static NotificationManager getNotificationManager() {
		return notificationManager;
	}
	
	public static ChestShopManager getChestShopManager() {
		return chestShopManager;
	}
	
	public static MinecraftServer getServer() {
		return server;
	}
}