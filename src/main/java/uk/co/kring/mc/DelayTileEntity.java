package uk.co.kring.mc;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import static uk.co.kring.mc.Jacko.tileEntityDataType;
import static uk.co.kring.mc.Sigma.ON;

public class DelayTileEntity extends TileEntity implements ITickableTileEntity {
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
    //warning! tick must sync setting of powerOut else recursive problems with notification for feedback loops
    public void tick() {
        if (!hasWorld()) return;//unloaded?
        int oldPower = powerOut;
        //process to find new powerOut
        if (oldPower != powerOut) {
            markDirty();//send client updates?
            world.setBlockState(pos, getBlockState().with(ON, powerOut != 0));
            world.notifyNeighborsOfStateChange(pos, getBlockState().getBlock());
        }
    }
}
