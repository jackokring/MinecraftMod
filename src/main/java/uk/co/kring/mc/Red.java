package uk.co.kring.mc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;

import static net.minecraft.util.Direction.*;

/**
 * User: The Grey Ghost
 * Date: 27/11/2015
 *
 * BlockRedstoneMeter is a simple block with an associated TileEntity to render the block's power level.
 * It gets weak power from all directions except UP.
 * The meter provides weak power to the block UP - if a lamp is placed on top of the meter, it will flash
 *   at a speed related to the input power.
 * We use a TileEntity because our block needs to store the input power level, for later use when others call the getWeakPower().
 *    for the reason why, see http://greyminecraftcoder.blogspot.com/2020/05/redstone-1152.html
 */
public class Red extends Block {
    final static DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    public Red() {
        super(Block.Properties.create(Material.MISCELLANEOUS));//.doesNotBlockMovement());
        BlockState defaultBlockState = stateContainer.getBaseState().with(FACING, Direction.NORTH);
        setDefaultState(defaultBlockState);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
                                             Hand handIn, BlockRayTraceResult hit) {
        //stateContainer.getProperty("facing") ...
        //stateContainer.getProperty("facing") ... can't write
        if(worldIn.isRemote) {

        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext blockItemUseContext) {
        //World world = blockItemUseContext.getWorld();
        //BlockPos blockPos = blockItemUseContext.getPos();

        Direction direction = blockItemUseContext.getPlacementHorizontalFacing();  // north, east, south, or west
        //float playerFacingDirectionAngle = blockItemUseContext.getPlacementYaw(); //if you want more directions than just NESW, you can use the yaw instead.
        // likewise the pitch is also available for up/down placement.

        BlockState blockState = getDefaultState().with(FACING, direction);
        return blockState;
    }

    // ------ methods relevant to redstone
    //  The methods below are used to provide power to neighbours.

    /**
     * This block can provide power
     * @return
     */
    @Override
    public boolean canProvidePower(BlockState iBlockState) {
        return true;
    }

    @Override
    public boolean getWeakChanges(BlockState state, IWorldReader world, BlockPos pos) {
        return true;//comparator style weak inputs yes
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;//output side
    }

    /** How much weak power does this block provide to the adjacent block?
     * The meter provides weak power to the block above it.
     * The meter flashes the power according to how strong the input signals are
     * See https://greyminecraftcoder.blogspot.com/2020/05/redstone-1152.html for more information
     * @param blockReader
     * @param pos the position of this block
     * @param state the blockstate of this block
     * @param directionFromNeighborToThis eg EAST means that this is to the EAST of the block which is asking for weak power
     * @return The power provided [0 - 15]
     */
    @Override
    public int getWeakPower(BlockState state, IBlockReader blockReader, BlockPos pos,
                            Direction directionFromNeighborToThis) {
        if (directionFromNeighborToThis != DOWN) {
            return 0;
        }

        boolean isOutputOn = false;
        TileEntity tileentity = blockReader.getTileEntity(pos);
        /* if (tileentity instanceof TileEntityCommon) { // prevent a crash if not the right type, or is null
            TileEntityCommon tileEntityRedstoneMeter = (TileEntityCommon) tileentity;
            isOutputOn = tileEntityRedstoneMeter.getOutputState();
        } */

        final int OUTPUT_POWER_WHEN_ON = 15;
        return isOutputOn ? OUTPUT_POWER_WHEN_ON : 0;
    }

    /**
     *  The redstone meter doesn't provide strong power to any other block.
     * @param worldIn
     * @param pos the position of this block
     * @param state the blockstate of this block
     * @param directionFromNeighborToThis eg EAST means that this is to the EAST of the block which is asking for strong power
     * @return The power provided [0 - 15]
     */

    @Override
    public int getStrongPower(BlockState state, IBlockReader worldIn, BlockPos pos,
                              Direction directionFromNeighborToThis) {
        return 0;
    }

    // Retrieve the current input power level of the meter - the maximum of the five sides EAST, WEST, NORTH, SOUTH, DOWN
    //   Don't look UP
    private int getPowerLevelInputFromNeighbours(World world, BlockPos pos) {

//    int powerLevel = world.getRedstonePowerFromNeighbors(pos);  // if input can come from any side, use this line

        int maxPowerFound = 0;
        Direction [] directions = new Direction[]{DOWN, WEST, EAST, NORTH, SOUTH};

        for (Direction whichFace : directions) {
            BlockPos neighborPos = pos.offset(whichFace);
            int powerLevel = world.getRedstonePower(neighborPos, whichFace);
            maxPowerFound = Math.max(powerLevel, maxPowerFound);
        }

        return maxPowerFound;
    }

    // ------ various block methods that react to changes and are responsible for updating the redstone power information

    // Called when a neighbouring block changes.
    // Only called on the server side- so it doesn't help us alter rendering on the client side.
    @Override
    public void neighborChanged(BlockState currentState, World world, BlockPos pos, Block blockIn,
                                BlockPos fromPos, boolean isMoving) {
        calculatePowerInputAndNotifyNeighbors(world, pos);
    }

    // Our flashing output uses scheduled ticks to toggle the output.
    //  Scheduling of ticks is by calling  world.scheduleTick(pos, block, numberOfTicksToDelay);  see ScheduledTogglingOutput
    //
    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
         /* TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityCommon) {
            TileEntityCommon tileEntityCommon = (TileEntityCommon) te;

            boolean currentOutputState = tileEntityCommon.getOutputState();
            tileEntityCommon.onScheduledTick(world, pos, state.getBlock());
            boolean newOutputState = tileEntityCommon.getOutputState();

            if (newOutputState != currentOutputState) {
                world.notifyNeighborsOfStateChange(pos, this);
            }
        } */
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        // not needed here
    }

    private void calculatePowerInputAndNotifyNeighbors(World world, BlockPos pos) {
        // calculate the power level from neighbours and store in our TileEntity for later use in getWeakPower()
        int powerLevel = getPowerLevelInputFromNeighbours(world, pos);
        /* TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof TileEntityCommon) { // prevent a crash if not the right type, or is null
            TileEntityCommon tileEntityCommon = (TileEntityCommon) tileentity;

            boolean currentOutputState = tileEntityCommon.getOutputState();
            tileEntityCommon.setPowerLevelServer(powerLevel);
            // this method will also schedule the next tick using call world.scheduleTick(pos, block, delay);

            if (currentOutputState != tileEntityCommon.getOutputState()) {
                world.notifyNeighborsOfStateChange(pos, this);
            }
        } */
    }
}