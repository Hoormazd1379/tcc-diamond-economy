# TCC Diamond Economy

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.8-brightgreen.svg)](https://minecraft.net)
[![Fabric API](https://img.shields.io/badge/Fabric%20API-0.130.0-blue.svg)](https://fabricmc.net)
[![Java Version](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-CC0--1.0-lightgrey.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.6.1-red.svg)](https://github.com/Hoormazd1379/tcc-diamond-economy/releases)
[![Server Side](https://img.shields.io/badge/Side-Server-yellow.svg)]()

A comprehensive **server-side diamond-based economy mod** for Minecraft Fabric that allows players to manage their diamond wealth through a secure banking system with transfers, balance management, offline notifications, and chest shops for player-to-player trading.

## ✨ Features

### 💰 **Core Banking System**
- **Secure Balance Management**: UUID-based player accounts with persistent JSON storage
- **Fractional Support**: Full support for fractional diamond amounts (e.g., 1.5, 0.25, 2.75 diamonds)
- **Deposit System**: Convert physical diamonds to account balance
- **Withdrawal System**: Convert account balance back to physical diamonds with inventory space validation
- **Smart Balance Display**: Automatically formats balances (removes unnecessary decimal places)
- **Balance Checking**: View your current diamond wealth instantly

### 💎 **Transfer System**
- **Fractional Transfers**: Send precise amounts like `/wire Steve 2.75` or `/wire Alice 0.31`
- **Player-to-Player Transfers**: Send diamonds to any player, online or offline
- **Offline Notifications**: Recipients get notified of transfers when they log in
- **Transfer Validation**: Prevents self-transfers and validates player existence
- **Transaction Security**: All transfers are atomic and validated

### 📊 **Economy Analytics**
- **Server Statistics**: View economy-wide data with `/economystats` command
- **Shop Performance**: Individual shop analytics showing sales and transaction data
- **Economic Health**: Track total money circulation and economic activity
- **Leaderboards**: Top performing shops and wealthiest players
- **Real-time Insights**: Live statistics update with every transaction

### 📊 **Leaderboards & Statistics**
- **Baltop System**: View the top 10 richest players with beautiful rankings
- **Real-time Updates**: Leaderboards reflect current balances instantly
- **Visual Rankings**: Medal system (🥇🥈🥉) for top players

### 🏪 **Advanced Chest Shop System**
- **Named Shops**: Create shops with custom names like `/createshop 1.5 "My Food Shop"`
- **Fractional Pricing**: Create shops with decimal prices like `/createshop 1.5` or `/createshop 0.25`
- **Shop Creation**: Convert trapped chests into shops with custom per-item pricing
- **Custom Shopping GUI**: Dedicated customer interface showing shop name and owner
- **Shop Statistics**: Track total sales, items sold, and transaction history per shop
- **Analytics**: View detailed shop performance with `/shopstats` command
- **Enhanced Visual Effects**: Multiple particle types with orbital sparkles and ambient glows
- **Shop Protection**: Owner-only access for modifications; hopper protection prevents theft
- **Automatic Validation**: Shop database integrity checked every 10 seconds, removing destroyed shops
- **Smart Purchase System**: Left-click for single items, right-click for full stacks
- **Real-time Notifications**: Shop owners get detailed sale notifications with item names and earnings
- **Offline Sale Summaries**: Players receive comprehensive shop sale summaries when they log in after being offline
- **Inventory Synchronization**: Accurate item display with enchantment preservation

### 🛠️ **User Experience**
- **Comprehensive Help System**: `/tcchelp` with detailed command documentation
- **Smart Error Handling**: Clear error messages with helpful guidance
- **Color-coded Feedback**: Green for success, red for errors, gold for balances
- **Input Validation**: Prevents invalid amounts and edge cases
- **Rich Visual Effects**: Multiple particle systems for enhanced shop visibility
- **Intuitive Interface**: Custom GUIs with clear pricing and balance information

## 🎮 Commands

| Command | Description | Usage | Permission |
|---------|-------------|-------|------------|
| `/deposit <amount>` | Deposit diamonds from inventory to account | `/deposit 32` | All players |
| `/withdraw <amount>` | Withdraw diamonds from account to inventory | `/withdraw 10` | All players |
| `/balance` / `/bal` | Check your current diamond balance | `/balance` | All players |
| `/wiretransfer <player> <amount>` / `/wire <player> <amount>` | Send diamonds to another player | `/wire Steve 25` | All players |
| `/baltop` | View top 10 richest players | `/baltop` | All players |
| `/createshop <price> [name]` | Create a chest shop (look at trapped chest) | `/createshop 10 "Food Shop"` | All players |
| `/editshop price <amount>` | Change shop price per item (look at your shop) | `/editshop price 2.5` | Shop owners |
| `/editshop name <name>` | Change shop name (look at your shop) | `/editshop name "New Name"` | Shop owners |
| `/removeshop` | Remove your chest shop (look at shop) | `/removeshop` | All players |
| `/listshops` | List all your owned chest shops | `/listshops` | All players |
| `/shopstats` | View shop statistics (look at any shop) | `/shopstats` | All players |
| `/economystats` | View server economy statistics | `/economystats` | All players |
| `/tcchelp` | Show comprehensive help guide | `/tcchelp` | All players |
| `/shopvalidate` | Manually validate all shops (Admin) | `/shopvalidate` | OP Level 2+ |
| `/shopvalidate stats` | Show validation statistics (Admin) | `/shopvalidate stats` | OP Level 2+ |

## 🚀 Installation

### Requirements
- **Minecraft**: 1.21.8
- **Fabric Loader**: 0.16.14+
- **Fabric API**: 0.130.0+1.21.8
- **Java**: 21+

### Server Installation
1. Download the latest release from [Releases](https://github.com/Hoormazd1379/tcc-diamond-economy/releases)
2. Place `tcc-diamond-economy-1.6.1.jar` in your server's `mods/` folder
3. Ensure Fabric API is installed
4. Start your server
5. Players can immediately start using the economy system!

### Development Setup
```bash
git clone https://github.com/Hoormazd1379/tcc-diamond-economy.git
cd tcc-diamond-economy
./gradlew build
```

## 🔧 Configuration

The mod works out-of-the-box with no configuration required! Player data is automatically stored in:
- **Player Balances**: `server-folder/diamond_economy/[player-uuid].json`
- **Pending Notifications**: `server-folder/diamond_economy/pending_notifications.json`
- **Chest Shops**: `server-folder/diamond_economy/chest_shops.json`

## 📖 Usage Examples

### Basic Economy Operations
```
/deposit 64          # Deposit a stack of diamonds
/balance             # Check your balance (shows fractional amounts cleanly)
/withdraw 32         # Withdraw half a stack (still requires whole diamonds)
/baltop              # See who's the richest (displays fractional balances)
```

### Fractional Transfer System
```
/wire Steve 10.5     # Send 10.5 diamonds to Steve
/wire Alice 0.25     # Send a quarter diamond to Alice
/wire Bob 2.75       # Send fractional amounts with precision
```

### Fractional Chest Shop System
```
# Place a trapped chest, then:
/createshop 1.5 "Steve's Food Market"  # Named shop with fractional pricing
/createshop 0.1 "Cheap Items"          # Budget-friendly shop
/createshop 25.75                      # Premium pricing (unnamed shop)

# Edit your existing shops:
/editshop price 2.0                    # Change shop price (look at your shop)
/editshop name "Updated Shop Name"     # Change shop name (look at your shop)

# View and manage shops:
/listshops                             # View all your shops with names and sales
/shopstats                             # View detailed shop statistics (look at any shop)
/economystats                          # View server-wide economy statistics

# Shop GUI will show: "🏪 Steve's Food Market - Steve"
# Enhanced analytics track all sales and performance metrics
```

### Advanced Shop Features
```
# Shops support all item types including:
- Enchanted weapons and armor (enchantments preserved)
- Custom named items (names preserved)
- Potions and special items (all properties maintained)
- Any stackable or non-stackable items

# Visual effects include:
- Orbital sparkles around shops
- Ambient glow effects
- Golden shower effects for special occasions
- Multiple particle types for enhanced visibility
```

### Getting Help
```
/tcchelp             # View all commands and usage
```

## 🛡️ Security Features

- **Atomic Transactions**: All operations are validated before execution
- **Inventory Protection**: Withdrawals check available space to prevent diamond loss
- **Balance Validation**: Prevents negative balances and overdrafts
- **Player Verification**: Transfers validate target players exist
- **Thread-Safe Operations**: Concurrent access protection with ConcurrentHashMap
- **Shop Protection**: Owner-only access for modifications; protection from manual breaking
- **Shop Database Validation**: Automatic cleanup of destroyed shops every 10 seconds
- **Purchase Validation**: Advanced multi-layer validation for secure transactions
- **Custom GUI Security**: Separate customer interface prevents inventory manipulation
- **Enchantment Preservation**: All item properties including enchantments are preserved in transactions

## ⚠️ Known Limitations

### Shop Protection
- **Manual Breaking Protection**: ✅ Shops are protected from unauthorized manual breaking by players
- **Environmental Damage**: ❌ Shops can still be destroyed by TNT, creeper explosions, fire, lava, and other environmental damage
- **Database Cleanup**: ✅ The mod automatically detects and removes destroyed shops from the database every 10 seconds
- **Hopper Protection**: ✅ Hoppers cannot extract items from shop inventories

### Technical Limitations
- **Minecraft 1.21.8 Block Registration**: Custom indestructible blocks were attempted but couldn't be implemented due to new API restrictions
- **Event-Based Protection**: Current protection relies on player action events, which don't cover all damage sources
- **Future Improvements**: Full explosion protection may be possible with future Minecraft/Fabric API updates

## 📁 Data Storage

### Player Balance Files
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 1250
}
```

### Notification System
```json
{
  "transfers": {
    "550e8400-e29b-41d4-a716-446655440000": [
      {
        "senderName": "Steve",
        "amount": 25,
        "timestamp": 1642781234567
      }
    ]
  },
  "shopSales": {
    "550e8400-e29b-41d4-a716-446655440000": [
      {
        "buyerName": "Alice",
        "itemName": "Diamond Sword",
        "quantity": 1,
        "earnings": 50,
        "timestamp": 1642781234567
      }
    ]
  }
}
```

### Chest Shop System
```json
{
  "overworld:100:64:-200": {
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

## 🎯 Perfect For

- **Economy Servers**: Create diamond-based server economies with player-run shops
- **SMP Servers**: Enable player-to-player diamond trading and marketplace creation
- **Minigame Servers**: Reward players with stored diamond wealth and shopping systems
- **Community Servers**: Foster economic interactions between players with chest shops

## 📋 Version History

### Version 1.6.1 (Current)
**🔧 Quality of Life Update:**
- **Enhanced Wire Transfer Commands**: Added tab completion support for `/wiretransfer` and `/wire` commands
- **Tab Completion**: Press Tab when typing player names to see autocomplete suggestions for online players
- **Robust Offline Transfers**: Improved system for transferring to players who are currently offline

### Version 1.6.0
**🏪 Shop Management & Analytics:**
- **Named Shops**: Create shops with custom names for better identification
- **Shop Statistics**: Track sales, transactions, and performance metrics per shop
- **Economy Analytics**: Server-wide statistics with `/economystats` command
- **Enhanced Shop Lists**: Display shop names and total sales instead of creation dates
- **Shop Performance**: View detailed statistics for any shop with `/shopstats`
- **Smart GUI Titles**: Shop interfaces show both shop name and owner

### Version 1.5.2
**🎨 Visual & UX Improvements:**
- **Fixed** Baltop ranking colors - 1st place now bright yellow, 3rd place bronze, 4th-10th gray
- **Simplified** Particle effects by removing totem and end rod particles for cleaner visuals
- **Streamlined** Help command by removing lengthy notes section

### Version 1.5.1
**💰 Fractional Diamond Economy:**
- **Fractional Pricing**: Create shops with decimal prices like 1.5 or 0.1 diamonds per item
- **Fractional Transfers**: Send precise amounts like `/wire Steve 2.75` or `/wire Alice 0.31`
- **Smart Balance Display**: Automatically formats balances (125.5 shows as "125.5", 100.0 shows as "100")
- **BigDecimal Precision**: Upgraded from long to BigDecimal for accurate fractional calculations
- **Backward Compatibility**: Existing shops and balances automatically migrate to fractional system
- **Enhanced Visual Effects**: Improved shop particle effects with cleaner visuals and higher frequency

### Version 1.5.0
**🔍 Shop Validation System:**
- **Automatic Integrity Checking**: Shop database is now validated every 10 seconds
  - Automatically removes shops that have been destroyed by TNT, creepers, or other environmental damage
  - Keeps shop database clean and prevents phantom shop issues
  - Chunk-aware validation (skips unloaded chunks to prevent false positives)
- **Admin Commands**: New admin tools for shop management
  - `/shopvalidate` - Manually trigger shop validation check
  - `/shopvalidate stats` - View validation system statistics
- **Enhanced Help System**: Updated help command with admin section for operators
- **Offline Shop Sale Notifications**: Shop owners now receive sale summaries when they log in after being offline
  - Similar to wire transfer notifications
  - Shows all sales that occurred while offline
  - **Consolidated summaries**: Multiple purchases of same item by same buyer are combined
  - Displays total earnings from all offline sales
- **Documentation Cleanup**: Removed false claims about explosion protection, now accurately describes actual capabilities

### Version 1.4.0
**🛡️ Basic Shop Protection:**
- **Block Break Protection**: Shops are protected from manual block breaking by unauthorized players
- **Owner Validation**: Only shop owners can break their own shops
- **Enhanced Error Messages**: Clear protection messages when unauthorized access is attempted
- **Protection Logging**: All protection events are logged for server monitoring
- **Known Limitation**: Shops can still be destroyed by TNT, creepers, and environmental damage

### Version 1.3.4
**🛡️ Initial Protection System:**
- **Player Break Protection**: Basic protection against manual block breaking
- **Owner-Only Removal**: Shop owners must use `/removeshop` command to remove shops
- **Protection Feedback**: Clear messages when protection activates
- **Event-Based Protection**: Uses PlayerBlockBreakEvents for basic protection
- **Known Limitation**: Environmental damage (explosions, TNT, etc.) can still destroy shops

### Version 1.3.3
**✨ Enhanced Shop Experience:**
- **Fixed Item Display Bug**: Shop messages now show actual item names instead of "Air"
- **Enhanced Particle Effects**: More frequent and varied particle effects for better shop visibility
  - Increased particle frequency (every 0.5 seconds)
  - Added orbital sparkles around shops
  - Multiple particle types: composter, happy villager, effect, enchant particles
  - Ambient glow effects with END_ROD particles
  - Enhanced golden shower effects for special occasions
- **Improved Owner Notifications**: Bold, prominent sale notifications with actual item names and earnings display

### Version 1.3.2
**🔧 Critical Bug Fixes:**
- **Inventory Synchronization Fix**: Resolved issue where shop items appeared as "Air" in customer GUI
- **Enhanced Client Sync**: Added proper inventory synchronization to ensure accurate item display
- **Performance Optimization**: Improved GUI handling for better client-server communication

### Version 1.3.1
**🛡️ Security & Protection Enhancements:**
- **Enhanced Shop Protection**: Complete hopper protection preventing automated item extraction
- **Advanced Purchase Validation**: Multi-layer validation system for secure transactions
- **Improved Error Handling**: Better error messages for edge cases and invalid operations

### Version 1.3.0
**🎮 Custom Shopping Interface:**
- **Dedicated Customer GUI**: Separate shopping interface for customers (not chest inventory)
- **Enhanced Purchase System**: Left-click for single items, right-click for full stacks
- **Real-time Balance Display**: Shows customer balance and item pricing in shop interface
- **Transaction Security**: Bulletproof payment processing with inventory space validation

### Version 1.2.0
**🏪 Major Update - Chest Shop System:**
- Added complete chest shop functionality for player-to-player trading
- New commands: `/createshop`, `/removeshop`, `/listshops`
- Visual particle effects distinguish shops from regular chests
- Shop protection: only owners can break shops, hoppers cannot access shop inventories
- Custom shopping interface with automatic payment processing
- Real-time notifications for shop owners when items are sold

**🔧 Enhancements:**
- Updated help system with comprehensive shop documentation
- Enhanced error handling for shop operations
- Integrated shop system with existing balance management

### Version 1.1.1
**🔧 Command Rename:**
- Changed `/transfer` to `/wiretransfer` and `/wire` (shorthand) to avoid conflict with Minecraft's built-in command
- Updated all documentation and help text

**🐛 Bug Fixes:**
- Fixed command registration issue that prevented transfer functionality from working

### Version 1.1.0
**🆕 New Features:**
- Added `/wiretransfer <player> <amount>` and `/wire <player> <amount>` commands for player-to-player transfers
- Added `/tcchelp` command with comprehensive help system
- Implemented offline notification system for transfers
- Enhanced error handling with helpful guidance

**🔧 Improvements:**
- All commands now provide helpful error messages
- Added reference to `/tcchelp` in error messages
- Improved user experience with better feedback

**🐛 Bug Fixes:**
- Enhanced input validation for all commands
- Better edge case handling

### Version 1.0.0
**🎉 Initial Release:**
- Core banking system with deposit/withdraw
- Balance checking with `/balance` and `/bal` commands
- Top player leaderboard with `/baltop`
- UUID-based player data storage
- Inventory space validation for withdrawals
- Beautiful color-coded command feedback

📝 **See [CHANGELOG.md](CHANGELOG.md) for complete version history**

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Development Commands
```bash
./gradlew runServer    # Launch test server with mod
./gradlew build        # Build the mod
./gradlew clean build  # Clean build
```

## 📜 License

This project is licensed under the CC0-1.0 License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with [Fabric](https://fabricmc.net/) modding framework
- Thanks to the Minecraft modding community
- Special thanks to Fabric API contributors

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Hoormazd1379/tcc-diamond-economy/issues)
- **Testing Guide**: [TESTING_GUIDE.md](TESTING_GUIDE.md) - Comprehensive testing instructions
- **Version History**: [CHANGELOG.md](CHANGELOG.md) - Complete changelog with all versions
- **Discord**: TCC Community Server

---

**Made with ❤️ by [Hoormazd1379](https://github.com/Hoormazd1379)**
