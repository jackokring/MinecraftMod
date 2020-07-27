package uk.co.kring.mc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
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

import javax.annotation.Nullable;

import static net.minecraft.util.Direction.*;

public class Sigma extends Block {
    final static DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    final static BooleanProperty ON = BooleanProperty.create("on");
    final static BooleanProperty POWERED = BooleanProperty.create("powered");

    public Sigma() {
        super(Block.Properties.create(Material.MISCELLANEOUS).doesNotBlockMovement());
        BlockState defaultBlockState = stateContainer.getBaseState().with(FACING, Direction.NORTH)
                .with(ON, false).with(POWERED, false);
        setDefaultState(defaultBlockState);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state,
                                       IBlockReader world) {
        //TODO: behaviour alteration
        return new SigmaTileEntity();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, ON);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext blockItemUseContext) {
        //World world = blockItemUseContext.getWorld();
        //BlockPos blockPos = blockItemUseContext.getPos();

        Direction direction = blockItemUseContext.getPlacementHorizontalFacing();  // north, east, south, or west
        //float playerFacingDirectionAngle = blockItemUseContext.getPlacementYaw();
        // if you want more directions than just N, E, S and W, you can use the yaw instead.
        // likewise the pitch is also available for up/down placement.

        BlockState blockState = getDefaultState().with(FACING, direction);
        return blockState;
    }

    @Override
    public boolean getWeakChanges(BlockState state, IWorldReader world, BlockPos pos) {
        return true;//comparator style weak inputs yes
    }

    //outputs only
    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        if(side != null) {
            return state.get(FACING).getOpposite() == side;//output side
        } else {
            return false;//no stepped connection
        }
    }

    // Retrieve the current input power levels - sides EAST, WEST, NORTH, SOUTH
    // server only
    protected void calculatePowerInput(World world, BlockPos pos, BlockState state) {

        // int powerLevel = world.getRedstonePowerFromNeighbors(pos);
        // if input can come from any side, use this line
        TileEntity d = world.getTileEntity(pos);
        DelayTileEntity dd;
        if (d instanceof DelayTileEntity) {
            dd = (DelayTileEntity) d;
        } else return;
        BlockPos neighborPos = pos.offset(state.get(FACING).getOpposite());//main
        //int oldPower = dd.powerIn;
        dd.powerIn = world.getRedstonePower(neighborPos, state.get(FACING).getOpposite());
        //if(dd.powerIn != oldPower) dd.markDirty();
        neighborPos = pos.offset(state.get(FACING).rotateYCCW());//left
        //oldPower = dd.powerLeft;
        dd.powerLeft = world.getRedstonePower(neighborPos, state.get(FACING).rotateYCCW());
        //if(dd.powerLeft != oldPower) dd.markDirty();
        neighborPos = pos.offset(state.get(FACING).rotateY());//right
        //oldPower = dd.powerRight;
        dd.powerRight = world.getRedstonePower(neighborPos, state.get(FACING).rotateY());
        //if(dd.powerRight != oldPower) dd.markDirty();
        return;
    }

    //================================================================
    // DEPRECATED FUNCTIONS 1.16
    //================================================================
    // Using block state construction via properties gives factory paradigm.
    // This makes overriding block state methods kind of difficult.
    // So the intent is allow overrides but proxy when calling them via IBlockProperties
    // This is perhaps to allow extension of code base to things other than standard block massive method set OK

    /**
     * What happens when a piston block pushes this?
     * @param state
     * @return
     */
    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    /**
     * Can be placed here?  i.e. on top
     * @param state
     * @param worldIn
     * @param pos
     * @return
     */
    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction direction = DOWN;
        return Block.hasEnoughSolidSide(worldIn, pos.offset(direction), direction.getOpposite());
    }

    @Override
    public boolean canProvidePower(BlockState iBlockState) {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
                                             Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {//is local I.E. SERVER
            //server only world update
            final int FLAGS = SetBlockStateFlag.get(SetBlockStateFlag.BLOCK_UPDATE, SetBlockStateFlag.SEND_TO_CLIENTS);
            if (state.get(ON)) {
                worldIn.setBlockState(pos, state.with(ON, false), FLAGS);
            } else {
                worldIn.setBlockState(pos, state.with(ON, true), FLAGS);
            }
        } else {
            //client only
        }
        return ActionResultType.SUCCESS;
    }

    /**
     * How much weak power does this block provide to the adjacent block?
     * See https://greyminecraftcoder.blogspot.com/2020/05/redstone-1152.html for more information
     *
     * @param blockReader
     * @param pos                         the position of this block
     * @param state                       the blockstate of this block
     * @param directionFromNeighborToThis eg EAST means that this is to the EAST of the block which is asking for weak power
     * @return The power provided [0 - 15]
     */
    @Override
    public int getWeakPower(BlockState state, IBlockReader blockReader, BlockPos pos,
                            Direction directionFromNeighborToThis) {
        return 0;//no weak power emitted
    }

    /**
     * Strong power to any other block.
     *
     * @param worldIn
     * @param pos                         the position of this block
     * @param state                       the blockstate of this block
     * @param directionFromNeighborToThis eg EAST means that this is to the EAST of the block which is asking for strong power
     * @return The power provided [0 - 15]
     */

    @Override
    public int getStrongPower(BlockState state, IBlockReader worldIn, BlockPos pos,
                              Direction directionFromNeighborToThis) {
        if (state.get(FACING) == directionFromNeighborToThis.getOpposite()) {
            TileEntity d = worldIn.getTileEntity(pos);
            if (d instanceof DelayTileEntity) {
                return ((DelayTileEntity) d).powerOut;//return delayed calculated power
            } else return 0;
        }
        return 0;//else no power
    }

    // ------ various block methods that react to changes and are responsible for updating the redstone power information

    // Called when a neighbouring block changes.
    // Only called on the server side- so it doesn't help us alter rendering on the client side.
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn,
                                BlockPos fromPos, boolean isMoving) {
        if (state.isValidPosition(world, pos)) {
            calculatePowerInput(world, pos, state);
        } else {
            spawnDrops(state, world, pos);
            world.removeBlock(pos, false);
        }
    }

    // for this model, we're making the shape match the block model exactly
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