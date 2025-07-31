# Changelog

All notable changes to TCC Diamond Economy will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.5.0] - 2025-07-31

### 🔍 Shop Validation System
- **Added** Automatic shop database integrity checking every 10 seconds
- **Added** Automatic cleanup of destroyed shops (removes shops from database when blocks are missing)
- **Added** Admin commands for shop management:
  - `/shopvalidate` - Manually trigger shop validation check (OP only)
  - `/shopvalidate stats` - View validation system statistics (OP only)
- **Enhanced** Help system with admin commands section for operators
- **Improved** Database integrity to ensure shop data matches actual world state

### 🏪 Offline Shop Sale Notifications
- **Added** Offline sale summary system for shop owners
- **Enhanced** NotificationManager to handle both transfer and shop sale notifications
- **Added** Comprehensive sale summaries when players log in after being offline
  - Shows all items sold while offline
  - Displays buyer names, quantities, and earnings
  - **Consolidated Sales**: Multiple purchases of the same item by the same buyer are combined into single lines
  - Total earnings summary for multiple sales
- **Improved** Shop owner experience with complete sale tracking

notable changes to TCC Diamond Economy will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.5.0] - 2025-07-31

### � Shop Validation System
- **Added** Automatic shop database integrity checking every 10 seconds
- **Added** Automatic cleanup of destroyed shops (removes shops from database when blocks are missing)
- **Added** Admin commands for shop management:
  - `/shopvalidate` - Manually trigger shop validation check (OP only)
  - `/shopvalidate stats` - View validation system statistics (OP only)
- **Enhanced** Help system with admin commands section for operators
- **Improved** Database integrity to ensure shop data matches actual world state

### 🔧 Technical Improvements
- **Added** `ShopValidationSystem` class with automatic validation logic
- **Added** `ShopValidateCommand` for admin shop management
- **Enhanced** Main mod initialization with validation system startup
- **Improved** Chunk-aware validation (skips unloaded chunks to prevent false positives)
- **Added** Comprehensive logging for shop cleanup operations
- **Enhanced** `NotificationManager` with support for shop sale notifications
- **Added** `PendingShopSale` class for offline sale tracking
- **Added** `NotificationData` class for improved JSON serialization

### � Documentation Updates
- **Updated** Protection claims to accurately reflect actual capabilities
- **Revised** README and help messages to remove false explosion protection claims
- **Clarified** That shops are protected from manual breaking, not environmental damage

## [1.4.0] - 2025-07-31

### 🛡️ Enhanced Shop Protection
- **Added** Block break protection against manual breaking attempts
- **Enhanced** Protection event handling with better error messages
- **Added** Owner-only shop removal system via `/removeshop` command
- **Improved** Protection feedback messages for clearer user understanding

### 🔧 Technical Improvements
- **Enhanced** Block break event handling for player-initiated damage
- **Added** Owner validation for shop modifications
- **Improved** Error messages with better formatting and clarity
- **Added** Logging for protection events

### ⚠️ Known Limitations
- Shops can still be destroyed by TNT, creepers, and other environmental damage
- Protection only works against manual block breaking by players
- Environmental protection was attempted but not successfully implemented

## [1.3.4] - 2025-07-31

### 🛡️ Basic Shop Protection
- **Added** Player block break protection for chest shops
- **Enhanced** Shop ownership validation system
- **Added** Protection messages when unauthorized players try to break shops
- **Improved** Error handling for shop access attempts

### 🔧 Technical Improvements
- **Added** Basic event-based protection using PlayerBlockBreakEvents
- **Enhanced** Owner verification in ChestShopEventHandler
- **Added** Protection logging for monitoring shop access attempts
- **Improved** Shop interaction feedback messages

### ⚠️ Known Limitations
- Only protects against manual player breaking
- TNT, creepers, and environmental damage can still destroy shops
- Advanced protection features were attempted but not successfully implemented

## [1.3.3] - 2025-07-31

### ✨ Enhanced Shop Experience
- **Fixed** Item display bug where shop messages showed "Air" instead of actual item names
- **Enhanced** Particle effects for better shop visibility:
  - Increased particle frequency from 1 second to 0.5 seconds
  - Added 4 different particle types per cycle (composter, happy villager, effect, enchant)
  - Added orbital sparkles (WAX_ON particles) around shops
  - Added ambient glow effects with END_ROD particles
  - Enhanced golden shower effects (TOTEM_OF_UNDYING + ENCHANT combo)
- **Improved** Shop owner notifications:
  - Bold, prominent sale notifications
  - Shows actual item names that were sold
  - Displays exact earnings from each sale
  - Example: "💰 SALE! Steve bought 64x Dirt from your shop for 640 diamonds!"

### 🔧 Technical Improvements
- **Fixed** ItemStack name preservation in purchase messages
- **Added** Original item name capture before inventory modifications
- **Enhanced** Visual feedback for all shop-related messages

## [1.3.2] - 2025-07-31

### 🔧 Critical Bug Fixes
- **Fixed** Major inventory synchronization issue where shop items appeared as "Air" in customer GUI
- **Added** Proper client-server inventory synchronization with `sendContentUpdates()`
- **Improved** GUI constructor to use empty inventory and populate after initialization
- **Enhanced** Display inventory management for better client communication

### 🚀 Performance
- **Optimized** Inventory handling for custom GUI
- **Improved** Client-server synchronization efficiency

## [1.3.1] - 2025-07-31

### 🛡️ Security & Protection Enhancements
- **Added** Complete hopper protection preventing automated item extraction from shops
- **Enhanced** Shop protection with comprehensive access control
- **Improved** Purchase validation system with multi-layer security
- **Added** Advanced error handling for edge cases

### 🔧 Technical Improvements
- **Added** HopperBlockEntityMixin for hopper interaction prevention
- **Enhanced** Block access validation in shop systems
- **Improved** Error messages for invalid operations

## [1.3.0] - 2025-07-31

### 🎮 Custom Shopping Interface
- **Added** Dedicated customer GUI separate from chest inventory
- **Implemented** ShopBrowserScreenHandler for enhanced shopping experience
- **Added** Purchase system: Left-click for single items, right-click for full stacks
- **Added** Real-time balance display in shop interface
- **Added** Item pricing display with visual formatting

### 🛡️ Transaction Security
- **Implemented** Bulletproof payment processing system
- **Added** Inventory space validation before purchases
- **Enhanced** Transaction atomicity and rollback capabilities
- **Added** Comprehensive purchase validation checks

### 🎨 User Experience
- **Added** Visual item display with pricing information
- **Enhanced** Customer interaction feedback
- **Improved** Shop browsing experience with custom GUI
- **Added** Color-coded pricing and balance information

## [1.2.1] - 2025-07-31

### Fixed
- **Customer Access**: Fixed issue where customers couldn't open chest shops to purchase items
- **Particle Effects**: Implemented continuous particle spawning around all chest shops (every second)
- **GUI Title**: Shop chests now display "Shop owned by [Owner]" instead of generic "Chest"
- **Visual Enhancement**: Shops now have consistent golden sparkle and happy villager particles for easy identification

### Enhanced
- **Shop Identification**: Continuous particle effects make shops easily distinguishable from regular trapped chests
- **User Experience**: Improved shop interface with proper titles and customer access
- **Performance**: Optimized particle spawning system with server tick events

## [1.2.0] - 2025-07-31

### Added
- **Chest Shop System**: Complete chest shop functionality for creating player-to-player marketplaces
- **Shop Commands**: 
  - `/createshop <price>` - Create a chest shop while looking at a trapped chest
  - `/removeshop` - Remove your chest shop while looking at it
  - `/listshops` - List all your owned chest shops with locations and details
- **Shop Protection**: 
  - Only shop owners can break their chest shops
  - Hopper protection prevents automated item extraction from shops
  - Visual particle effects (golden sparkles) distinguish shops from regular chests
- **Shop Interactions**: 
  - Non-owners see shop interface with prices when opening shop chests
  - Automatic payment processing when customers purchase items
  - Real-time balance updates for both buyers and sellers
  - Online notifications for shop owners when items are sold
- **Data Persistence**: Shop data stored in `chest_shops.json` with location tracking and ownership

### Enhanced
- **Help System**: Updated `/tcchelp` to include all new chest shop commands
- **Error Handling**: Comprehensive validation for shop creation and management
- **User Experience**: Rich feedback messages with emojis and color coding

### Technical
- Added `ChestShopManager` for shop data management and persistence
- Added `ChestShopEventHandler` for block interaction and particle effects
- Added `HopperBlockEntityMixin` to prevent automated item extraction
- Enhanced event system with block break protection
- Integrated shop system with existing balance management

## [1.1.1] - 2025-07-31

### Changed
- **Command Rename**: Changed `/transfer` to `/wiretransfer` and `/wire` to avoid conflict with Minecraft's built-in `/transfer` command
- **Documentation**: Updated all documentation to reflect new command names

### Fixed
- **Command Registration**: Fixed issue where transfer command wasn't recognized due to name conflict

## [1.1.0] - 2025-07-31

### Added
- **Transfer System**: New `/transfer <player> <amount>` command for player-to-player diamond transfers
- **Offline Notifications**: Players receive notifications when they log in if they received transfers while offline
- **Help System**: Comprehensive `/tcchelp` command with detailed documentation of all features
- **Enhanced Error Handling**: All commands now provide helpful error messages with guidance
- **Player Join Events**: Automatic notification delivery system when players connect
- **NotificationManager**: New system for handling offline player notifications with persistent storage

### Changed
- **Error Messages**: All commands now refer users to `/tcchelp` for assistance when used incorrectly
- **User Experience**: Improved feedback with more descriptive error messages and help references
- **Command Validation**: Enhanced input validation across all commands

### Technical
- Added `NotificationManager` class for handling offline notifications
- Implemented `ServerPlayConnectionEvents.JOIN` for automatic notification delivery
- Added `pending_notifications.json` for persistent notification storage
- Enhanced main mod class with notification system integration

## [1.0.0] - 2025-07-31

### Added
- **Core Banking System**: 
  - `/deposit <amount>` - Deposit diamonds from inventory to account
  - `/withdraw <amount>` - Withdraw diamonds from account to inventory
  - `/balance` / `/bal` - Check current diamond balance
- **Leaderboard System**: `/baltop` - View top 10 richest players with medals and rankings
- **Data Persistence**: UUID-based player data storage in JSON format
- **Inventory Validation**: Smart space checking prevents diamond loss during withdrawals
- **Beautiful UI**: Color-coded messages (green for success, red for errors, gold for balances)
- **Security Features**: 
  - Atomic transactions
  - Balance validation
  - Thread-safe operations with ConcurrentHashMap
- **Input Validation**: Prevents negative amounts, zero values, and invalid inputs

### Technical
- Built with Fabric 1.21.8 and Fabric API 0.130.0
- Requires Java 21+
- Server-side only mod (no client installation required)
- Efficient JSON-based data storage system
- Comprehensive error handling and edge case management

---

## Version Numbering

- **Major (X.0.0)**: Breaking changes, major feature overhauls
- **Minor (0.X.0)**: New features, significant improvements
- **Patch (0.0.X)**: Bug fixes, minor improvements

## Future Planned Features

- [ ] Enhanced shop protection (when Minecraft/Fabric APIs provide better explosion event handling)
- [ ] Interest system for stored diamonds
- [ ] Transaction history and logs
- [ ] Admin commands for balance management
- [ ] Economy statistics and analytics
- [ ] Integration with other economy mods
- [ ] Bank fees and transaction costs
- [ ] Multi-currency support
