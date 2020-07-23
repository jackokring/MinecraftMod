package uk.co.kring.mc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

import static uk.co.kring.mc.Jacko.tileEntityDataType;

/**
 * This TileEntity is used for two main purposes:
 *  1) to store the current power level.  This is necessary due to the way that the redstone signals propagate,
 *        e.g. getWeakPower() must retrieve a stored value and not calculate it from neighbours.
 *        see here for more information http://greyminecraftcoder.blogspot.com/2020/05/redstone-1152.html
 *  2) It's also used to flash the output at a defined rate using block tick scheduling.
 */
public class TileEntityCommon extends TileEntity {
    final private static int MIN_POWER_LEVEL = 0;
    final private static int MAX_POWER_LEVEL = 15;
    public enum SetBlockStateFlag {
        /**
         * Sets a block state into this world.Flags are as follows:
         * 1 will cause a block update.
         * 2 will send the change to clients.
         * 4 will prevent the block from being re-rendered.
         * 8 will force any re-renders to run on the main thread instead
         * 16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
         * 32 will prevent neighbor reactions from spawning drops.
         * 64 will signify the block is being moved.
         * Flags can be OR-ed
         */

        BLOCK_UPDATE(1),
        SEND_TO_CLIENTS(2),
        DO_NOT_RENDER(4),
        RUN_RENDER_ON_MAIN_THREAD(8),
        PREVENT_NEIGHBOUR_REACTIONS(16),
        NEIGHBOUR_REACTIONS_DONT_SPAWN_DROPS(32),
        BLOCK_IS_BEING_MOVED(64);

        public static int get(SetBlockStateFlag... flags) {
            int result = 0;
            for (SetBlockStateFlag flag : flags) {
                result |= flag.flagValue;
            }
            return result;
        }

        SetBlockStateFlag(int flagValue) {
            this.flagValue = flagValue;
        }
        private int flagValue;
    }

    public TileEntityCommon() {
        super(tileEntityDataType);
    }

    private int lastPowerLevel = -1;

    // -------- server side methods used to keep track of the current power level and alter the output signal state

    public boolean getOutputState() {
        return false; //scheduledTogglingOutput.isOn();
    }

    /** whenever a scheduled block update occurs, call this method
     *
     */
    public void onScheduledTick(ServerWorld world, BlockPos pos, Block block) {
        //scheduledTogglingOutput.onUpdateTick(world, pos, block);
    }

    /**
     *  Change the stored power level (and alter the flashing rate of the power output)
     */
    public void setPowerLevelServer(int newPowerLevel) {
        if (newPowerLevel == storedPowerLevel) return;
        storedPowerLevel = newPowerLevel;
        BlockState blockState = this.getBlockState();
        //setTogglingRateFromPowerLevel(true);
        // we've changed storedPowerLevel, so inform vanilla of the change to ensure it is sent to the client
        this.markDirty();
        int FLAGS = SetBlockStateFlag.get(SetBlockStateFlag.BLOCK_UPDATE, SetBlockStateFlag.SEND_TO_CLIENTS);
        this.getWorld().notifyBlockUpdate(this.getPos(), blockState, blockState, FLAGS);
    }

    private int storedPowerLevel;

    //---------- general TileEntity methods

    // When the world loads from disk, the server needs to send the TileEntity information to the client
    //  it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this:
    //  getUpdatePacket() and onDataPacket() are used for one-at-a-time TileEntity updates
    //  getUpdateTag() and handleUpdateTag() are used by vanilla to collate together into a single chunk update packet
    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT updateTagDescribingTileEntityState = getUpdateTag();
        int tileEntityType = 6;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new SUpdateTileEntityPacket(this.pos, tileEntityType, updateTagDescribingTileEntityState);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
        //updateNeedleFromPowerLevel();
    }

    /* Creates a tag containing the TileEntity information, used by vanilla to transmit from server to client
       Warning - although our getUpdatePacket() uses this method, vanilla also calls it directly, so don't remove it.
     */
    @Override
    public CompoundNBT getUpdateTag()  {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        return nbtTagCompound;
    }

    // This is where you save any data that you don't want to lose when the tile entity unloads
    @Override
    public CompoundNBT write(CompoundNBT parentNBTTagCompound) {
        super.write(parentNBTTagCompound); // The super call is required to save the tiles location
        parentNBTTagCompound.putInt("storedPowerLevel", storedPowerLevel);
        return parentNBTTagCompound;
    }

    // This is where you load the data that you saved in writeToNBT
    //@Override
    public void read(CompoundNBT parentNBTTagCompound) {
        //super.read(parentNBTTagCompound); // The super call is required to load the tiles location
        storedPowerLevel = parentNBTTagCompound.getInt("storedPowerLevel");  // defaults to 0 if not found
        if (storedPowerLevel < MIN_POWER_LEVEL ) storedPowerLevel = MIN_POWER_LEVEL;
        if (storedPowerLevel > MAX_POWER_LEVEL ) storedPowerLevel = MAX_POWER_LEVEL;
        //setTogglingRateFromPowerLevel(false);
    }

    /** Return an appropriate bounding box enclosing the TER
     * This method is used to control whether the TER should be rendered or not, depending on where the player is looking.
     * The default is the AABB for the parent block, which might be too small if the TER renders outside the borders of the
     *   parent block.
     * If you get the boundary too small, the TER may disappear when you aren't looking directly at it.
     * @return an appropriately size AABB for the TileEntity
     */
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // if your render should always be performed regardless of where the player is looking, use infinite
        AxisAlignedBB infiniteExample = INFINITE_EXTENT_AABB;

        // our needles are all on the block faces so our bounding box is from [x,y,z] to  [x+1, y+1, z+1]
        AxisAlignedBB aabb = new AxisAlignedBB(getPos(), getPos().add(1, 1, 1));
        return aabb;
    }

    /**
     * Don't render the needle if the player is too far away
     * @return the maximum distance squared at which the TER should render
     */
    @Override
    public double getMaxRenderDistanceSquared() {
        final int MAXIMUM_DISTANCE_IN_BLOCKS = 32;
        return MAXIMUM_DISTANCE_IN_BLOCKS * MAXIMUM_DISTANCE_IN_BLOCKS;
    }
}
