# TCC Diamond Economy Mod - Testing Guide

## 🎯 Features Implemented

Your diamond economy mod is **fully functional** with all requested features:

- ✅ `/deposit [amount]` - Deposit diamonds from inventory to account
- ✅ `/withdraw [amount]` - Withdraw diamonds from account to inventory  
- ✅ `/balance` or `/bal` - Check your diamond balance
- ✅ `/baltop` - View top 10 richest players
- ✅ `/wiretransfer [player] [amount]` / `/wire [player] [amount]` - Send diamonds to other players (online/offline)
- ✅ `/tcchelp` - Complete help system with all commands
- ✅ **NEW: `/createshop [price]`** - Create chest shops from trapped chests
- ✅ **NEW: `/removeshop`** - Remove your chest shops
- ✅ **NEW: `/listshops`** - List all your owned shops
- ✅ UUID-based player data storage (JSON files)
- ✅ Inventory space validation for withdrawals
- ✅ Offline player notification system
- ✅ **NEW: Chest shop system with visual particles**
- ✅ **NEW: Shop protection (owner-only breaking, hopper protection)**
- ✅ **NEW: Customer purchase interface with balance validation**
- ✅ Beautiful command feedback with colors and emojis
- ✅ Comprehensive error handling with help references

## 🚀 How to Test the Mod

### Quick Start for Development Testing

**Fastest way to test your mod:**

```bash
cd /home/hoormazdp/Documents/GitHub/tcc-diamond-economy
./gradlew runServer
```

**First time setup**: If you see "You need to agree to the EULA", stop the server and run:
```bash
echo "eula=true" > run/eula.txt
./gradlew runServer
```

This launches a test server with your mod automatically loaded! No manual setup needed.

- Server runs on `localhost:25565`
- Test world created in `run/` folder
- All your commands will work immediately
- Perfect for rapid iteration during development

**Available Gradle tasks:**
- `./gradlew runServer` - Launch test server with mod
- `./gradlew runClient` - Launch client with mod (for client-side testing)
- `./gradlew build` - Build the mod JAR file

### Full Server Setup (for production-like testing)

### 1. Server Setup

1. **Create a test server folder** (if you don't have one):
   ```bash
   mkdir ~/minecraft-test-server
   cd ~/minecraft-test-server
   ```

2. **Download Minecraft Server 1.21.8**:
   - Get the server JAR from [minecraft.net](https://minecraft.net/download/server)
   - Save it as `server.jar`

3. **Download Fabric Server Loader**:
   - Go to [fabricmc.net](https://fabricmc.net/use/server)
   - Download the installer for Minecraft 1.21.8
   - Run: `java -jar fabric-installer-x.x.x.jar server -mcversion 1.21.8 -loader 0.16.14`

4. **Install Fabric API**:
   - Download Fabric API 0.130.0+1.21.8 from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
   - Put it in the `mods/` folder

5. **Install your mod**:
   ```bash
   cp /home/hoormazdp/Documents/GitHub/tcc-diamond-economy/build/libs/tcc-diamond-economy-1.2.1.jar ~/minecraft-test-server/mods/
   ```

6. **Start the server**:
   ```bash
   java -Xmx2G -Xms1G -jar fabric-server-launch.jar nogui
   ```

### Alternative: Quick Testing with Gradle

For quick testing during development, you can use Gradle to launch a test server:

```bash
cd /home/hoormazdp/Documents/GitHub/tcc-diamond-economy
./gradlew runServer
```

This will:
- Automatically set up a test server environment
- Load your mod without needing manual installation
- Create a temporary world in `run/` folder
- Perfect for quick testing during development

To connect to this test server:
1. Launch Minecraft 1.21.8 with Fabric
2. Go to Multiplayer → Direct Connect
3. Server Address: `localhost:25565`

### 2. Testing Commands

Once the server is running and you've joined:

#### Basic Balance Operations
```
/tcchelp              # View all available commands and usage
/balance              # Check your current balance (should be 0)
/deposit 10           # Try without diamonds (should fail)
```

#### Get Some Diamonds for Testing
```
/give @s diamond 64    # Give yourself diamonds
/deposit 32            # Deposit 32 diamonds
/balance               # Should show 32 diamonds
/withdraw 10           # Withdraw 10 diamonds
/balance               # Should show 22 diamonds
```

#### Test Transfer System
```
/wire [playername] 5        # Send 5 diamonds to another player
/wiretransfer Steve 10      # Send to offline player (gets notification on login)
/wire @s 5                  # Try self-transfer (should fail)
/wire NonExistentPlayer 5   # Try invalid player (should fail)
```

#### Test Edge Cases
```
/deposit 1000         # Try depositing more than you have
/withdraw 1000        # Try withdrawing more than you have
/wire Steve 1000      # Try transferring more than you have
/deposit 0            # Invalid amount (should fail)
/withdraw -5          # Invalid amount (should fail)
/wire                 # Wrong command usage (should show help)
```

#### Test Help System
```
/tcchelp              # View comprehensive help with all commands
```

#### Test Error Messages
All commands now provide helpful error messages and refer to `/tcchelp` when used incorrectly.

#### Test Inventory Space
1. Fill your inventory completely
2. Try `/withdraw 64` (should warn about space)
3. Clear some space and try again

#### Test Baltop
```
/baltop              # View richest players
```

#### Test Chest Shop System
```
# Basic shop creation
/give @s trapped_chest 5    # Get trapped chests
# Place a trapped chest, look at it, then:
/createshop 10              # Create shop with 10 diamonds per item
/listshops                  # View your shops
/removeshop                 # Remove shop (look at it first)

# Advanced shop testing
/give @s diamond_sword 1    # Get items to sell
# Put the sword in your shop chest
# Have another player open the chest and try to buy the sword
# Verify continuous particle effects appear around shop chests (golden + green sparkles)
# Verify GUI shows "Shop owned by [YourName]" for customers
# Test hopper protection by placing hopper under shop
```

### 3. Multi-Player Testing

1. **Join with multiple accounts** (or use friends)
2. **Give each player different amounts**:
   ```
   /give player1 diamond 100
   /give player2 diamond 50
   /give player3 diamond 200
   ```
3. **Have each player deposit different amounts**
4. **Test transfers between players**:
   ```
   /wire player2 25          # Send diamonds between players
   /wiretransfer player3 50  # Test larger amounts
   ```
5. **Check `/baltop`** to see the leaderboard
6. **Test offline notifications**:
   - Have one player disconnect
   - Transfer diamonds to the offline player
   - When they reconnect, they should see notification messages
7. **Test chest shop marketplace**:
   - Have Player 1 create shops with different items and prices
   - Have Player 2 browse shops and make purchases
   - Verify shop owners get notified of sales
   - Test that only shop owners can break their shops
   - Verify particle effects are visible to all players

### 4. Data Persistence Testing

1. **Make some deposits/withdrawals**
2. **Stop the server**
3. **Check the data files**:
   ```bash
   ls diamond_economy/
   cat diamond_economy/[your-uuid].json
   cat diamond_economy/chest_shops.json
   ```
4. **Restart server and check balances persist**

## 📁 Data Storage

Player balances are stored in: `server-folder/diamond_economy/[player-uuid].json`

Example balance file:
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 150
}
```

Pending offline notifications are stored in: `server-folder/diamond_economy/pending_notifications.json`

Example notifications file:
```json
{
  "550e8400-e29b-41d4-a716-446655440000": [
    {
      "senderName": "Steve",
      "amount": 25,
      "timestamp": 1642781234567
    }
  ]
}
```

Chest shops are stored in: `server-folder/diamond_economy/chest_shops.json`

Example chest shops file:
```json
{
  "minecraft:overworld:100:64:-200": {
    "ownerUUID": "550e8400-e29b-41d4-a716-446655440000",
    "ownerName": "Steve",
    "worldName": "minecraft:overworld",
    "x": 100,
    "y": 64,
    "z": -200,
    "pricePerItem": 10,
    "createdTime": 1642781234567
  }
}
```

## 🐛 Troubleshooting

### Common Issues:

1. **"Unknown command" error**:
   - Make sure the mod is in the `mods/` folder
   - Check server logs for loading errors
   - Ensure Fabric API is installed

2. **Commands not working**:
   - Verify you're using the correct Minecraft version (1.21.8)
   - Check that you have operator permissions
   - Look for errors in server console

3. **Mod not loading**:
   - Ensure Java 21+ is being used
   - Check mod compatibility with Fabric Loader version
   - Review server startup logs

### Debug Commands:
```bash
# Check if mod is loaded
/fabric mods

# Check server logs
tail -f logs/latest.log
```

## 🎮 Expected Behavior

- **Deposits**: Remove diamonds from inventory, add to balance
- **Withdrawals**: Remove from balance, add diamonds to inventory  
- **Transfers**: Send diamonds between players (works offline!)
- **Balance**: Shows current diamond count with gold formatting
- **Baltop**: Shows top 10 players with medals (🥇🥈🥉) and colors
- **Chest Shops**: Trapped chests with golden particles, custom shopping interface
- **Shop Protection**: Only owners can break shops, hoppers cannot access shop inventories
- **Shop Purchases**: Automatic payment processing with balance validation
- **Help System**: `/tcchelp` shows comprehensive command guide
- **Error Messages**: Clear red messages for insufficient funds/space with help references
- **Success Messages**: Green confirmation messages with new balances
- **Offline Notifications**: Players get notified of received transfers when they log in

## 🚀 Production Deployment

For a production server:

1. **Backup your world first!**
2. **Test on a copy of your world**
3. **Set appropriate permissions** if using a permissions plugin
4. **Monitor server performance** (the mod is lightweight but check with large player counts)
5. **Regular backups** of the `diamond_economy/` folder

The mod is now ready for production use! 🎉
