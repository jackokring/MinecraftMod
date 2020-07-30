package uk.co.kring.mc.tileentities;

import net.minecraft.nbt.CompoundNBT;
import uk.co.kring.mc.calculationprovider.CalculationProviderTileEntity;

import static uk.co.kring.mc.JackoObjectHolder.tileEntityDelta;

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
