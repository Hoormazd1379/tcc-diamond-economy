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
	private static MinecraftServer server;

	@Override
	public void onInitializeServer() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Initializing Diamond Economy mod!");
		
		// Register server lifecycle events
		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
		
		// Register player join event for pending notifications
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			notificationManager.checkAndSendPendingNotifications(handler.player);
		});
		
		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LOGGER.info("Registering Diamond Economy commands...");
			DepositCommand.register(dispatcher, registryAccess, environment);
			WithdrawCommand.register(dispatcher, registryAccess, environment);
			BalanceCommand.register(dispatcher, registryAccess, environment);
			BaltopCommand.register(dispatcher, registryAccess, environment);
			TransferCommand.register(dispatcher, registryAccess, environment);
			HelpCommand.register(dispatcher, registryAccess, environment);
			LOGGER.info("All Diamond Economy commands registered successfully!");
		});
	}
	
	private void onServerStarting(MinecraftServer server) {
		Tccdiamondeconomy.server = server;
		balanceManager = new BalanceManager(server);
		notificationManager = new NotificationManager(server);
		LOGGER.info("Diamond Economy balance manager and notification system initialized!");
	}
	
	private void onServerStopping(MinecraftServer server) {
		LOGGER.info("Diamond Economy shutting down!");
	}
	
	public static BalanceManager getBalanceManager() {
		return balanceManager;
	}
	
	public static NotificationManager getNotificationManager() {
		return notificationManager;
	}
	
	public static MinecraftServer getServer() {
		return server;
	}
}