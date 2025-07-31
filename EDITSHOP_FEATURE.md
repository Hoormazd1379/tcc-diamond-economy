# EditShop Command Feature Summary

## 🔧 Overview
The `/editshop` command has been added to TCC Diamond Economy v1.6.0, allowing shop owners to modify their existing shops without losing valuable statistics data.

## 🎯 Key Features

### ✅ Shop Price Editing
- **Command**: `/editshop price <amount>`
- **Example**: `/editshop price 2.5`
- **Function**: Changes the price per item for the shop
- **Minimum**: 0.01 diamonds per item
- **Precision**: Supports fractional pricing (BigDecimal precision)

### ✅ Shop Name Editing  
- **Command**: `/editshop name <new_name>`
- **Example**: `/editshop name "Updated Food Store"`
- **Function**: Changes the display name of the shop
- **Support**: Supports spaces and special characters in names

## 🔒 Security & Permissions

### Owner-Only Access
- Only the shop owner can edit their shops
- UUID-based ownership verification
- Clear error messages for unauthorized attempts
- Shows original owner name when access is denied

### Visual Feedback
- **Success Messages**: Clear confirmation of changes with before/after values
- **Error Handling**: Helpful guidance for common mistakes
- **Statistics Preservation**: Explicit confirmation that stats are maintained

## 🎮 User Experience

### Intuitive Usage
1. **Look at your shop**: Aim crosshair at your trapped chest shop
2. **Choose edit type**: Either `price` or `name`
3. **Specify new value**: Enter the new price or name
4. **Confirmation**: Receive detailed feedback about the change

### Smart Validation
- **Shop Detection**: Automatically detects if you're looking at a shop
- **Ownership Check**: Verifies you own the shop before allowing edits
- **Input Validation**: Ensures price is above minimum threshold
- **Block Type Check**: Confirms you're looking at a trapped chest

## 📊 Statistics Preservation

### Complete Data Retention
- **Total Sales Revenue**: Preserved across all edits
- **Items Sold Count**: Maintained unchanged
- **Transaction History**: Complete transaction count preserved
- **Creation Date**: Original shop creation timestamp kept
- **Last Sale**: Most recent sale timestamp retained

### Why This Matters
- Players can adjust pricing strategies without losing progress
- Shop analytics remain accurate for decision-making
- Economic data integrity maintained across the server
- Historical performance tracking continues uninterrupted

## 🔗 Integration

### Command Registration
- Properly registered in main mod initialization
- Added to `/tcchelp` command documentation
- Included in all README and documentation updates

### Technical Implementation
- Uses existing ChestShopManager infrastructure
- Follows established error handling patterns
- Maintains code consistency with other commands
- Proper logging for admin monitoring

## 🚀 Benefits

### For Players
- **Flexibility**: Adapt to market conditions without starting over
- **Data Continuity**: Keep valuable performance statistics
- **Professional Management**: Rebrand shops as business grows
- **No Downtime**: Edit shops while they remain operational

### For Server Economy
- **Market Dynamics**: Encourages price competition and adjustment
- **Business Evolution**: Supports shop rebranding and growth
- **Data Integrity**: Maintains accurate economic statistics
- **User Retention**: Reduces frustration from starting over

## 📝 Usage Examples

```bash
# Change shop price for market competition
/editshop price 1.75

# Rebrand shop for seasonal items
/editshop name "Halloween Treats"

# Adjust pricing for premium items
/editshop price 25.50

# Update shop name after partnership
/editshop name "Steve & Alex's Market"
```

## 🎯 Future Considerations

This feature sets the foundation for additional shop management capabilities while maintaining the core principle of preserving valuable player data and statistics.
