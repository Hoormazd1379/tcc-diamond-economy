package net.thecubecollective.diamondeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.thecubecollective.diamondeconomy.BalanceManager;
import net.thecubecollective.diamondeconomy.ChestShopManager;
import net.thecubecollective.diamondeconomy.Tccdiamondeconomy;
import net.thecubecollective.diamondeconomy.TrappedChestUtils;

import java.util.List;
import java.util.UUID;

public class ShoptopCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("shoptop")
                .executes(ShoptopCommand::execute));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        
        ChestShopManager shopManager = Tccdiamondeconomy.getChestShopManager();
        List<ChestShopManager.ChestShop> topShops = shopManager.getTopShops(10);
        
        if (topShops.isEmpty()) {
            player.sendMessage(Text.literal("No shops have made any sales yet!")
                    .formatted(Formatting.YELLOW), false);
            return 1;
        }
        
        player.sendMessage(Text.literal("=== Top 10 Shops by Sales ===")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        
        for (int i = 0; i < topShops.size(); i++) {
            ChestShopManager.ChestShop shop = topShops.get(i);
            
            // Get shop display name
            String shopDisplayName = getShopDisplayName(shop);
            
            // Get shop owner name
            String ownerName = getPlayerName(shop.ownerUUID);
            
            // Get chest type for the shop
            String chestType = getChestTypeForShop(shop);
            
            Formatting rankColor;
            String medal = "";
            
            switch (i) {
                case 0:
                    rankColor = Formatting.YELLOW; // Bright gold for 1st place
                    medal = "🥇 ";
                    break;
                case 1:
                    rankColor = Formatting.WHITE; // White for 2nd place  
                    medal = "🥈 ";
                    break;
                case 2:
                    rankColor = Formatting.GOLD; // Bronze/darker gold for 3rd place
                    medal = "🥉 ";
                    break;
                default:
                    rankColor = Formatting.GRAY; // Light gray for 4-10th place
                    medal = "";
                    break;
            }
            
            // Format: "🥇 1. Steve's Food Market (Steve) [Double] - 150.75💎"
            player.sendMessage(Text.literal(medal + (i + 1) + ". " + shopDisplayName + 
                    " (" + ownerName + ") [" + chestType + "] - " + 
                    BalanceManager.formatBalance(shop.totalSales) + "💎")
                    .formatted(rankColor), false);
        }
        
        return 1;
    }
    
    private static String getShopDisplayName(ChestShopManager.ChestShop shop) {
        if (shop.shopName != null && !shop.shopName.trim().isEmpty()) {
            return shop.shopName;
        }
        return "Unnamed Shop";
    }
    
    private static String getPlayerName(UUID playerUUID) {
        ServerPlayerEntity player = Tccdiamondeconomy.getServer().getPlayerManager().getPlayer(playerUUID);
        if (player != null) {
            return player.getName().getString();
        }
        
        // If player is offline, try to get cached name
        String cachedName = Tccdiamondeconomy.getServer().getUserCache().getByUuid(playerUUID)
                .map(profile -> profile.getName())
                .orElse("Unknown Player");
        
        return cachedName;
    }
    
    private static String getChestTypeForShop(ChestShopManager.ChestShop shop) {
        try {
            // Try to get the world and check the chest type
            net.minecraft.server.world.ServerWorld world = null;
            for (net.minecraft.server.world.ServerWorld serverWorld : Tccdiamondeconomy.getServer().getWorlds()) {
                if (serverWorld.getRegistryKey().getValue().toString().equals(shop.worldName)) {
                    world = serverWorld;
                    break;
                }
            }
            
            if (world != null) {
                net.minecraft.util.math.BlockPos pos = shop.getBlockPos();
                return TrappedChestUtils.getChestType(pos, world);
            }
        } catch (Exception e) {
            // If we can't determine the type, default to Single
        }
        
        return "Single";
    }
}
