package net.thecubecollective.diamondeconomy;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling trapped chest operations, including double chest detection
 */
public class TrappedChestUtils {
    
    /**
     * Gets all block positions that are part of the same chest structure (single or double)
     * @param pos The position to check
     * @param world The world
     * @return List of all positions that are part of this chest structure
     */
    public static List<BlockPos> getChestPositions(BlockPos pos, World world) {
        List<BlockPos> positions = new ArrayList<>();
        BlockState state = world.getBlockState(pos);
        
        if (!state.isOf(Blocks.TRAPPED_CHEST)) {
            return positions;
        }
        
        positions.add(pos);
        
        // Check if this is a double chest
        if (state.getBlock() instanceof ChestBlock) {
            ChestType chestType = state.get(ChestBlock.CHEST_TYPE);
            
            if (chestType != ChestType.SINGLE) {
                // This is part of a double chest, find the other half
                Direction facing = state.get(ChestBlock.FACING);
                BlockPos otherPos = getOtherChestPos(pos, chestType, facing);
                
                if (otherPos != null && world.getBlockState(otherPos).isOf(Blocks.TRAPPED_CHEST)) {
                    positions.add(otherPos);
                }
            }
        }
        
        return positions;
    }
    
    /**
     * Checks if a trapped chest can be expanded by placing another trapped chest next to it
     * @param existingPos The position of the existing trapped chest
     * @param newPos The position where a new trapped chest will be placed
     * @param world The world
     * @return true if the placement would create a valid double chest
     */
    public static boolean canExpandChest(BlockPos existingPos, BlockPos newPos, World world) {
        BlockState existingState = world.getBlockState(existingPos);
        
        if (!existingState.isOf(Blocks.TRAPPED_CHEST)) {
            return false;
        }
        
        // Check if existing chest is already part of a double chest
        if (existingState.getBlock() instanceof ChestBlock) {
            ChestType chestType = existingState.get(ChestBlock.CHEST_TYPE);
            if (chestType != ChestType.SINGLE) {
                return false; // Already a double chest
            }
        }
        
        // Check if the positions are adjacent and aligned properly for a double chest
        return isValidDoubleChestPlacement(existingPos, newPos);
    }
    
    /**
     * Checks if two positions can form a valid double chest
     */
    private static boolean isValidDoubleChestPlacement(BlockPos pos1, BlockPos pos2) {
        // Must be on same Y level
        if (pos1.getY() != pos2.getY()) {
            return false;
        }
        
        // Must be exactly one block apart
        int dx = Math.abs(pos1.getX() - pos2.getX());
        int dz = Math.abs(pos1.getZ() - pos2.getZ());
        
        // Valid double chest placement: either (dx=1, dz=0) or (dx=0, dz=1)
        return (dx == 1 && dz == 0) || (dx == 0 && dz == 1);
    }
    
    /**
     * Gets the position of the other half of a double chest
     */
    private static BlockPos getOtherChestPos(BlockPos pos, ChestType chestType, Direction facing) {
        Direction direction;
        
        switch (chestType) {
            case LEFT:
                direction = facing.rotateYClockwise();
                break;
            case RIGHT:
                direction = facing.rotateYCounterclockwise();
                break;
            default:
                return null;
        }
        
        return pos.offset(direction);
    }
    
    /**
     * Gets the main position for a chest structure (for single chests, returns the same position;
     * for double chests, returns the "left" chest position when viewed from the front)
     * @param pos Any position that's part of the chest structure
     * @param world The world
     * @return The main position for this chest structure
     */
    public static BlockPos getMainChestPosition(BlockPos pos, World world) {
        List<BlockPos> allPositions = getChestPositions(pos, world);
        
        if (allPositions.size() <= 1) {
            return pos; // Single chest
        }
        
        // For double chests, return the position with smaller coordinates
        BlockPos pos1 = allPositions.get(0);
        BlockPos pos2 = allPositions.get(1);
        
        // Prioritize by X coordinate, then Z coordinate
        if (pos1.getX() < pos2.getX() || (pos1.getX() == pos2.getX() && pos1.getZ() < pos2.getZ())) {
            return pos1;
        } else {
            return pos2;
        }
    }
    
    /**
     * Checks if a position is part of a double trapped chest
     * @param pos The position to check
     * @param world The world
     * @return true if this position is part of a double trapped chest
     */
    public static boolean isDoubleChest(BlockPos pos, World world) {
        return getChestPositions(pos, world).size() > 1;
    }
    
    /**
     * Gets the type of chest structure
     * @param pos The position to check
     * @param world The world
     * @return "Single" or "Double" chest type
     */
    public static String getChestType(BlockPos pos, World world) {
        return isDoubleChest(pos, world) ? "Double" : "Single";
    }
}
