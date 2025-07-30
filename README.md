# TCC Diamond Economy

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.8-brightgreen.svg)](https://minecraft.net)
[![Fabric API](https://img.shields.io/badge/Fabric%20API-0.130.0-blue.svg)](https://fabricmc.net)
[![Java Version](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-CC0--1.0-lightgrey.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.1.1-red.svg)](https://github.com/Hoormazd1379/tcc-diamond-economy/releases)
[![Server Side](https://img.shields.io/badge/Side-Server-yellow.svg)]()

A comprehensive **server-side diamond-based economy mod** for Minecraft Fabric that allows players to manage their diamond wealth through a secure banking system with transfers, balance management, and offline notifications.

## ✨ Features

### 💰 **Core Banking System**
- **Secure Balance Management**: UUID-based player accounts with persistent JSON storage
- **Deposit System**: Convert physical diamonds to account balance
- **Withdrawal System**: Convert account balance back to physical diamonds with inventory space validation
- **Balance Checking**: View your current diamond wealth instantly

### 💎 **Transfer System**
- **Player-to-Player Transfers**: Send diamonds to any player, online or offline
- **Offline Notifications**: Recipients get notified of transfers when they log in
- **Transfer Validation**: Prevents self-transfers and validates player existence
- **Transaction Security**: All transfers are atomic and validated

### 📊 **Leaderboards & Statistics**
- **Baltop System**: View the top 10 richest players with beautiful rankings
- **Real-time Updates**: Leaderboards reflect current balances instantly
- **Visual Rankings**: Medal system (🥇🥈🥉) for top players

### 🛠️ **User Experience**
- **Comprehensive Help System**: `/tcchelp` with detailed command documentation
- **Smart Error Handling**: Clear error messages with helpful guidance
- **Color-coded Feedback**: Green for success, red for errors, gold for balances
- **Input Validation**: Prevents invalid amounts and edge cases

## 🎮 Commands

| Command | Description | Usage | Permission |
|---------|-------------|-------|------------|
| `/deposit <amount>` | Deposit diamonds from inventory to account | `/deposit 32` | All players |
| `/withdraw <amount>` | Withdraw diamonds from account to inventory | `/withdraw 10` | All players |
| `/balance` / `/bal` | Check your current diamond balance | `/balance` | All players |
| `/wiretransfer <player> <amount>` / `/wire <player> <amount>` | Send diamonds to another player | `/wire Steve 25` | All players |
| `/baltop` | View top 10 richest players | `/baltop` | All players |
| `/tcchelp` | Show comprehensive help guide | `/tcchelp` | All players |

## 🚀 Installation

### Requirements
- **Minecraft**: 1.21.8
- **Fabric Loader**: 0.16.14+
- **Fabric API**: 0.130.0+1.21.8
- **Java**: 21+

### Server Installation
1. Download the latest release from [Releases](https://github.com/Hoormazd1379/tcc-diamond-economy/releases)
2. Place `tcc-diamond-economy-1.1.0.jar` in your server's `mods/` folder
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

## 📖 Usage Examples

### Basic Economy Operations
```
/deposit 64          # Deposit a stack of diamonds
/balance             # Check your balance
/withdraw 32         # Withdraw half a stack
/baltop              # See who's the richest
```

### Transfer System
```
/wire Steve 10       # Send 10 diamonds to Steve
/wire Alice 50       # Send diamonds to offline player
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
  "550e8400-e29b-41d4-a716-446655440000": [
    {
      "senderName": "Steve",
      "amount": 25,
      "timestamp": 1642781234567
    }
  ]
}
```

## 🎯 Perfect For

- **Economy Servers**: Create diamond-based server economies
- **SMP Servers**: Enable player-to-player diamond trading
- **Minigame Servers**: Reward players with stored diamond wealth
- **Community Servers**: Foster economic interactions between players

## 📋 Version History

### Version 1.1.1 (Current)
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
