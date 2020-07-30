package uk.co.kring.mc.tileentities;

import net.minecraft.nbt.CompoundNBT;
import uk.co.kring.mc.calculationprovider.CalculationProviderTileEntity;

import static uk.co.kring.mc.JackoObjectHolder.tileEntitySigma;

public class SigmaTileEntity extends CalculationProviderTileEntity {

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
