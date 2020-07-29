package uk.co.kring.mc;

import net.minecraft.nbt.CompoundNBT;

import static uk.co.kring.mc.Holder.tileEntityPi;

public class PiTileEntity extends CalculationProviderTileEntity {

    public PiTileEntity() {
        super(tileEntityPi);
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
