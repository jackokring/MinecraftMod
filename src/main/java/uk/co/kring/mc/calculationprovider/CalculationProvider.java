package uk.co.kring.mc.calculationprovider;

import net.minecraft.nbt.CompoundNBT;

public interface CalculationProvider {
    public void pokeNBT(CompoundNBT tag);
    public void peekNBT(CompoundNBT tag);
    public int afterDelay();
}
