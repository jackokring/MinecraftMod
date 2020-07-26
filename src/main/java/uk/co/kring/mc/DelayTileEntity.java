package uk.co.kring.mc;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import javax.annotation.Nullable;

import static uk.co.kring.mc.Sigma.ON;

public class DelayTileEntity extends TileEntity implements ITickableTileEntity {
    public static TileEntityType<DelayTileEntity> tileEntityDataType;
    int powerOut = 0;
    int powerIn = 0;
    int powerLeft = 0;
    int powerRight = 0;

    public DelayTileEntity() {
        super(tileEntityDataType);
    }

    // This is where you save any data that you don't want to lose when the tile entity unloads
    //SAVE
    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag = super.write(tag);//writes location etc ...
        //markDirty();//essential to trigger this
        //save data
        tag.putInt("out", powerOut);
        return tag;
    }

    //SEND
    @Override
    public CompoundNBT getUpdateTag()  {
        CompoundNBT tag = new CompoundNBT();//blank
        tag = write(tag);//extra use functionality, as does not save, just makes net packet
        //server side fill in non saved?
        return tag;//filled in complete
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT updateTagDescribingTileEntityState = getUpdateTag();
        final int METADATA = 44; // arbitrary.
        return new SUpdateTileEntityPacket(pos, METADATA, updateTagDescribingTileEntityState);
    }

    //RECEIVE
    @Override
    public void handleUpdateTag(BlockState bs, CompoundNBT tag) {
        //there is assumption this is called when loading phase happens to load server via updates?
        super.handleUpdateTag(bs, tag);//some docs say this does the readNBT
        //client side process
        powerOut = tag.getInt("out");
        //updateNeedleFromPowerLevel();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        handleUpdateTag(getBlockState(), pkt.getNbtCompound());//pass down
    }

    @Override
    //warning! tick must sync setting of powerOut else recursive problems with notification for feedback loops
    public void tick() {
        if (!hasWorld()) return;//unloaded?
        int oldPower = powerOut;
        //process to find new powerOut
        if (oldPower != powerOut) {
            markDirty();//send client updates?
            final int FLAGS = SetBlockStateFlag.get(SetBlockStateFlag.BLOCK_UPDATE, SetBlockStateFlag.SEND_TO_CLIENTS);
            world.setBlockState(pos, getBlockState().with(ON, powerOut != 0), FLAGS);
            world.notifyNeighborsOfStateChange(pos, getBlockState().getBlock());
        }
    }
}
