package uk.co.kring.mc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;

import static net.minecraft.util.Direction.*;

public class Sigma extends Block {
    final static DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    final static BooleanProperty ON = BooleanProperty.create("on");

    public Sigma() {
        super(Block.Properties.create(Material.MISCELLANEOUS).doesNotBlockMovement());
        BlockState defaultBlockState = stateContainer.getBaseState().with(FACING, Direction.NORTH)
                .with(ON, false);
        setDefaultState(defaultBlockState);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new DelayTileEntity();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, ON);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
                                             Hand handIn, BlockRayTraceResult hit) {
        if(worldIn.isRemote) {
            //server only world update
            if (state.get(ON)) {
                worldIn.setBlockState(pos, state.with(ON, false));
            } else {
                worldIn.setBlockState(pos, state.with(ON, true));
            }
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

        BlockState blockState = getDefaultState().with(FACING, direction)
                .with(ON, false);
        return blockState;
    }

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
        return 0;//no weak power emitted
    }

    final Direction[] driving = new Direction[]{ WEST, EAST, NORTH, SOUTH };
    final Direction[] me = new Direction[]{ EAST, WEST, SOUTH, NORTH };
    final Direction[] left = new Direction[]{ NORTH, SOUTH, EAST, WEST };
    final Direction[] right = new Direction[]{ SOUTH, NORTH, WEST, EAST };

    /**
     * Strong power to any other block.
     * @param worldIn
     * @param pos the position of this block
     * @param state the blockstate of this block
     * @param directionFromNeighborToThis eg EAST means that this is to the EAST of the block which is asking for strong power
     * @return The power provided [0 - 15]
     */

    @Override
    public int getStrongPower(BlockState state, IBlockReader worldIn, BlockPos pos,
                              Direction directionFromNeighborToThis) {
        for(int i = 0; i < driving.length; ++i) {
            if(driving[i] == directionFromNeighborToThis) {
                if(state.get(FACING) == me[i]) {
                    TileEntity d = worldIn.getTileEntity(pos);
                    if(d instanceof DelayTileEntity) {
                        return ((DelayTileEntity)d).powerOut;//return delayed calculated power
                    } else return 0;
                }
            }
        }
        return 0;//else no power
    }

    // Retrieve the current input power levels - sides EAST, WEST, NORTH, SOUTH
    private void calculatePowerInput(World world, BlockPos pos, BlockState state) {

    // int powerLevel = world.getRedstonePowerFromNeighbors(pos);
    // if input can come from any side, use this line
        for(int i = 0; i < me.length; ++i) {
            if(me[i] == state.get(FACING)) {
                TileEntity d = world.getTileEntity(pos);
                DelayTileEntity dd;
                if(d instanceof DelayTileEntity) {
                    dd = (DelayTileEntity)d;
                } else return;
                BlockPos neighborPos = pos.offset(driving[i]);//main
                dd.powerIn = world.getRedstonePower(neighborPos, driving[i]);
                neighborPos = pos.offset(left[i]);//left
                dd.powerLeft = world.getRedstonePower(neighborPos, left[i]);
                neighborPos = pos.offset(right[i]);//right
                dd.powerRight = world.getRedstonePower(neighborPos, right[i]);
                return;
            }
        }
    }

    // ------ various block methods that react to changes and are responsible for updating the redstone power information

    // Called when a neighbouring block changes.
    // Only called on the server side- so it doesn't help us alter rendering on the client side.
    @Override
    public void neighborChanged(BlockState currentState, World world, BlockPos pos, Block blockIn,
                                BlockPos fromPos, boolean isMoving) {
        calculatePowerInput(world, pos, currentState);
    }

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

    // for this model, we're making the shape match the block model exactly
    // see assets\minecraftbyexample\models\block\mbe02_block_partial_model.json
    private static final Vector3d BASE_MIN_CORNER = new Vector3d(0.0, 0.0, 0.0);
    private static final Vector3d BASE_MAX_CORNER = new Vector3d(16.0, 2.0, 16.0);
    private static final Vector3d PILLAR_MIN_CORNER = new Vector3d(7.0, 2.0, 7.0);
    private static final Vector3d PILLAR_MAX_CORNER = new Vector3d(9.0, 8.0, 9.0);

    private static final VoxelShape BASE = Block.makeCuboidShape(BASE_MIN_CORNER.getX(), BASE_MIN_CORNER.getY(), BASE_MIN_CORNER.getZ(),
            BASE_MAX_CORNER.getX(), BASE_MAX_CORNER.getY(), BASE_MAX_CORNER.getZ());
    private static final VoxelShape PILLAR = Block.makeCuboidShape(PILLAR_MIN_CORNER.getX(), PILLAR_MIN_CORNER.getY(), PILLAR_MIN_CORNER.getZ(),
            PILLAR_MAX_CORNER.getX(), PILLAR_MAX_CORNER.getY(), PILLAR_MAX_CORNER.getZ());

    private static VoxelShape COMBINED_SHAPE = VoxelShapes.or(BASE, PILLAR);  // use this method to add two shapes together

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return COMBINED_SHAPE;
    }
}