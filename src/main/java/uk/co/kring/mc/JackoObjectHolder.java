package uk.co.kring.mc;

import net.minecraft.block.Block;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;
import uk.co.kring.mc.tileentities.DeltaTileEntity;
import uk.co.kring.mc.tileentities.PiTileEntity;
import uk.co.kring.mc.tileentities.SigmaTileEntity;
import uk.co.kring.mc.tileentities.UpsilonTileEntity;

@ObjectHolder(value="jacko")
public class JackoObjectHolder {
    //version 1.0
    //hold references for use internal
    //public static Potion zerog;
    public static TileEntityType<SigmaTileEntity> tileEntitySigma;
    public static TileEntityType<DeltaTileEntity> tileEntityDelta;
    public static TileEntityType<UpsilonTileEntity> tileEntityUpsilon;
    public static TileEntityType<PiTileEntity> tileEntityPi;

    //these are filled in via the runtime to be the actual blocks
    //the registry name of any public static final field is
    //updated to be a reference which can be altered by other mods
    //being loaded. The final nature prevents interfering with
    //the mod loader and so conflicts of update are removed.
    public static final Block unlock = null;
    public static final Block sigma = null;
    public static final Block delta = null;
    public static final Block upsilon = null;
    public static final Block pi = null;

    //version 2.0
}
