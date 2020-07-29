package uk.co.kring.mc;

import net.minecraft.nbt.CompoundNBT;

import static uk.co.kring.mc.Holder.tileEntityDelta;

public class DeltaTileEntity extends CalculationProviderTileEntity {

    public DeltaTileEntity() {
        super(tileEntityDelta);
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
