# Changelog

All notable changes to TCC Diamond Economy will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

- [ ] Interest system for stored diamonds
- [ ] Transaction history and logs
- [ ] Admin commands for balance management
- [ ] Economy statistics and analytics
- [ ] Integration with other economy mods
- [ ] Bank fees and transaction costs
- [ ] Multi-currency support
