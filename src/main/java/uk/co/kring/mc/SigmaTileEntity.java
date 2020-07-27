package uk.co.kring.mc;

import net.minecraft.nbt.CompoundNBT;

import static uk.co.kring.mc.Holder.tileEntitySigma;

public class SigmaTileEntity extends DelayTileEntity {

    public SigmaTileEntity() {
        super(tileEntitySigma);
    }

    //============================================================================
    // CALCULATION PROVIDER FOR REDSTONE
    //============================================================================
    public void pokeNBT(CompoundNBT tag) {

    }

    public void peekNBT(CompoundNBT tag) {

    }

    public int afterDelay() {
        return 0;
    }
}
