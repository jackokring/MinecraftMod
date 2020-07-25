package uk.co.kring.mc;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import static uk.co.kring.mc.Jacko.tileEntityDataType;

public class DelayTileEntity extends TileEntity implements ITickableTileEntity {
    public DelayTileEntity() {
        super(tileEntityDataType);
    }

    // This is where you save any data that you don't want to lose when the tile entity unloads
    //SAVE
    @Override
    public CompoundNBT write(CompoundNBT nbt) {

        //markDirty();//essential to trigger this
        return super.write(nbt);
    }

    //Creates a tag containing the TileEntity information, used by vanilla to transmit from server to client
    //Warning - although our getUpdatePacket() uses this method, vanilla also calls it directly, so don't remove it.
    //SEND
    @Override
    public CompoundNBT getUpdateTag()  {
        CompoundNBT nbt = new CompoundNBT();
        write(nbt);//save defaults NO! leads to retransmit
        //but this is sender so needs to be written with server side data
        return nbt;
    }

    // This is where you load the data that you saved in writeToNBT
    //LOAD
    public void read(CompoundNBT nbt) {
        //storedPowerLevel = parentNBTTagCompound.getInt("storedPowerLevel");  // defaults to 0 if not found
    }

    //can't seem to read anymore. So one wonders how this triggers
    //RECEIVE
    @Override
    public void handleUpdateTag(BlockState bs, CompoundNBT tag) {
        read(tag);
        //updateNeedleFromPowerLevel();
    }

    @Override
    public void tick() {

    }

    //---------- general TileEntity methods
    // When the world loads from disk, the server needs to send the TileEntity information to the client
    //  it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this:
    //// NO!  getUpdatePacket() and onDataPacket() are used for one-at-a-time TileEntity updates
    //  getUpdateTag() and handleUpdateTag() are used by vanilla to collate together into a single chunk update packet
}
