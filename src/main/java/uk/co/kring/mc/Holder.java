package uk.co.kring.mc;

import net.minecraft.block.Block;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(value="jacko")
public class Holder {
    public static TileEntityType<SigmaTileEntity> tileEntitySigma;
    public static TileEntityType<DeltaTileEntity> tileEntityDelta;
    public static TileEntityType<UpsilonTileEntity> tileEntityUpsilon;
    public static TileEntityType<PiTileEntity> tileEntityPi;

    //version 1.0
    public static Block unlock;
    public static Block sigma;
    public static Block delta;
    public static Block upsilon;
    public static Block pi;

    public static Potion zerog;
    //version 2.0
}
