package uk.co.kring.mc;

import net.minecraft.block.*;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.command.Commands;
import net.minecraft.item.*;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.kring.mc.calculationprovider.CalculationProviderRedstoneBlock;
import uk.co.kring.mc.tileentities.DeltaTileEntity;
import uk.co.kring.mc.tileentities.PiTileEntity;
import uk.co.kring.mc.tileentities.SigmaTileEntity;
import uk.co.kring.mc.tileentities.UpsilonTileEntity;

//IMC multiple IPC combine to list received values of sent evaluations of closure results
import java.util.function.Supplier;
import java.util.stream.Collectors;

//command arguments

//ObjectHolder
import static uk.co.kring.mc.JackoObjectHolder.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Jacko.MOD_ID)
public class Jacko {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "jacko";

    public Jacko() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void fromMod(String toShow, Object extra) {
        if(extra == null) {
            LOGGER.info("[" + Jacko.MOD_ID + "]: " + toShow);
        } else {
            LOGGER.info("[" + Jacko.MOD_ID + "]: " + toShow, extra);
        }
    }

    private void fromMod(String toShow) {
        fromMod(toShow, null);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        fromMod("setup()");
        //LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void clientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        fromMod("clientStuff()");
        /*fromMod("Got game settings {}",
                event.getMinecraftSupplier().get().gameSettings); */
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo(Jacko.MOD_ID, "enqueueIMC",
                () -> { fromMod("enqueueIMC()"); return "IMC TEST OK!";});
        //binding element manufacture to supply
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        fromMod("processIMC() -> Got IMC {}", event.getIMCStream().
                map(m -> m.getMessageSupplier().get()).
                collect(Collectors.toList()));
        //collect all supplied as a list
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        fromMod("onServerStarting()");
        event.getServer().getCommandManager().getDispatcher().register(
                Commands.literal(Jacko.MOD_ID)
                        .requires(source -> source.hasPermissionLevel(1)) // can be up to 4 (1 => low pri)
                        /* .then(
                                Commands.argument("foo_bar", integer())
                                        .executes(context -> {
                                            System.out.println("Bar is " + getInteger(context, "bar"));
                                            return 1;
                                        })
                        ) */
                        .executes(context -> {
                            System.out.println("Hi, mod " + Jacko.MOD_ID + " is loaded.");
                            return 1;
                        })
        );
    }

    public static class RangedRocker {
        public int count, bottomOffset, topOffset, max;
        public RangedRocker(int countSet, int bottomOffsetSet, int topOffsetSet, int maxSet) {
            count = countSet;
            bottomOffset = bottomOffsetSet;
            topOffset = topOffsetSet;
            max = maxSet;
        }
    }

    //count = iterative attempts to match random location with that to be replaced for spawn (commonality)
    //bottomOffset/topOffset = bottom and top offsets. 0 - 255 range squish
    //max = maximum cascade iterates
    public static RangedRocker[] ranges = {
        new RangedRocker(12, 5, 5, 80),
        new RangedRocker(18, 3, 5, 80),
        new RangedRocker(15, 8, 5, 50)
    };

    public static class BiomeRocker {
        Biome biome;
        OreFeatureConfig.FillerBlockType find;
        Block replace;
        RangedRocker range;
        Biome.Category subFilter;
        int size;
        boolean terminal;

        public BiomeRocker(OreFeatureConfig.FillerBlockType findSet, Block replaceSet, RangedRocker rangeSet,
                           int sizeSet, boolean terminalSet,
                           Biome.Category subFilterSet, Biome biomeSet) {
            biome = biomeSet;
            find = findSet;
            replace = replaceSet;
            range = rangeSet;
            subFilter = subFilterSet;
            size = sizeSet;
            terminal = terminalSet;
        }
    }

    //Seems to be no End Stone Ore type
    public static OreFeatureConfig.FillerBlockType END_STONE =
            OreFeatureConfig.FillerBlockType.create("END_STONE","end_stone",
                    new BlockMatcher(Blocks.END_STONE));

    //filters to iterate
    //more generic later as only applied per biome until terminalSet = true
    //sizeSet is vein size
    public static BiomeRocker[] biomes = {
        new BiomeRocker(OreFeatureConfig.FillerBlockType.NETHERRACK, unlock, ranges[0], 4, true,
                Biome.Category.NETHER, null),
        new BiomeRocker(END_STONE, unlock, ranges[1], 12, true,
                Biome.Category.THEEND, null),
        new BiomeRocker(OreFeatureConfig.FillerBlockType.NATURAL_STONE, unlock, ranges[2], 6, true,
                null, null)
    };

    @SubscribeEvent
    public static void generateOres(FMLLoadCompleteEvent event) {
        for (Biome biome : ForgeRegistries.BIOMES) {
            for(BiomeRocker br : biomes) {
                if(br.biome == null || biome == br.biome) {
                    if(br.subFilter == null || biome.getCategory() == br.subFilter) {
                        RangedRocker rr = br.range;
                        CountRangeConfig range = new CountRangeConfig(rr.count, rr.bottomOffset, rr.topOffset, rr.max);
                        OreFeatureConfig feature = new OreFeatureConfig(br.find, br.replace.getDefaultState(), br.size);
                        ConfiguredPlacement config = Placement.COUNT_RANGE.configure(range);
                        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE.
                                withConfiguration(feature).withPlacement(config));
                        if(br.terminal) break;//exit all applied per biome
                    }
                }
            }
        }
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        public static void regBlockCP(String name, RegistryEvent.Register<Block> event) {
            Block b = new CalculationProviderRedstoneBlock();//needs TileEntity registering with same "name"
            b.setRegistryName(Jacko.MOD_ID, name);
            event.getRegistry().register(b);
            //return b;//via ObjectHolder
        }

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            Block u = new Block(Block.Properties.create(Material.MISCELLANEOUS)
                    //.hardnessAndResistance(3f, 3f) //emerald level
                    .sound(SoundType.SCAFFOLDING)
                    //.slipperiness(0.5f)
            );
            u.setRegistryName(Jacko.MOD_ID, "unlock");
            //see @ObjectHolder in uk.co.kring.mc.Blocks field import static
            event.getRegistry().register(u);

            regBlockCP("sigma", event);
            regBlockCP("delta", event);
            regBlockCP("upsilon", event);
            regBlockCP("pi", event);
        }

        final static int MAXIMUM_STACK_SIZE = 64;
        final static int POTION_STACK_SIZE = 1;
        final static int BOOK_STACK_SIZE = 16;
        final static int OPTIMAL_HOPPER_SORT_SIZE = 13;
        // 1/13th of a stack for each of the four filter items
        // Hopper feedback delay = 2 items at max 5 items per second
        // needs 1/14th of a stack
        //final int OPTIMAL_HOPPER_SORT_SIZE = 14;
        // if slow feed then fine at 1/13th

        static ItemGroup customItemGroup;

        static void regBlockItemCP(Block name, RegistryEvent.Register<Item> event) {
            Item.Properties itemP = new BlockItem.Properties().group(customItemGroup);
                    //.maxStackSize(MAXIMUM_STACK_SIZE);
            BlockItem item = new BlockItem(name, itemP);
            item.setRegistryName(name.getRegistryName());
            event.getRegistry().register(item);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
             customItemGroup = new ItemGroup("jacko_item_group") {
                @Override
                public ItemStack createIcon() {
                    return new ItemStack(Items.EMERALD);
                }
            };

            Item.Properties itemP = new BlockItem.Properties().group(customItemGroup)
                    .maxStackSize(OPTIMAL_HOPPER_SORT_SIZE);//for extra utility
            BlockItem unlockItem = new BlockItem(unlock, itemP);
            unlockItem.setRegistryName(unlock.getRegistryName());
            event.getRegistry().register(unlockItem);

            itemP = new WrittenBookItem.Properties().group(customItemGroup);
                    //.maxStackSize(BOOK_STACK_SIZE);
            WrittenBookItem bookItem = new WrittenBookItem(itemP);
            //the following 2 look like a read and write function pair
            //bookItem.readShareTag();
            //bookItem.getShareTag();
            //perhaps it's bool false and a place where tags are for following 2 methods ...
            //bookItem.shouldSyncTag();
            //bookItem.getTags();
            bookItem.setRegistryName(Jacko.MOD_ID, "book");
            event.getRegistry().register(bookItem);

            itemP = new PotionItem.Properties().group(customItemGroup);
                    //.maxStackSize(POTION_STACK_SIZE);
            Item potionItem = new PotionItem(itemP);
            potionItem.setRegistryName(Jacko.MOD_ID, "zerog");
            event.getRegistry().register(potionItem);

            regBlockItemCP(sigma, event);
            regBlockItemCP(delta, event);
            regBlockItemCP(upsilon, event);
            regBlockItemCP(pi, event);
        }

        public static TileEntityType regTileEntityCP(Block name, Supplier<TileEntity> tet,
                                                 RegistryEvent.Register<TileEntityType<?>> event) {
            TileEntityType te = TileEntityType.Builder
                    .create(tet, name).build(null);
            //technically type is null and used in constructor before assignment (pokey pointer variant generic?)
            // you probably don't need a datafixer --> null should be fine
            te.setRegistryName(name.getRegistryName());
            event.getRegistry().register(te);
            return te;
        }

        @SubscribeEvent
        public static void registerTileEntity(final RegistryEvent.Register<TileEntityType<?>> event) {
            tileEntitySigma = regTileEntityCP(sigma, SigmaTileEntity::new, event);
            tileEntityDelta = regTileEntityCP(delta, DeltaTileEntity::new, event);
            tileEntityUpsilon = regTileEntityCP(upsilon, UpsilonTileEntity::new, event);
            tileEntityPi = regTileEntityCP(pi, PiTileEntity::new, event);
        }

        @SubscribeEvent
        public static void registerEffects(final RegistryEvent.Register<Effect> event) {

        }

        @SubscribeEvent
        public static void registerPotions(final RegistryEvent.Register<Potion> event) {
            zerog = new Potion();
            zerog.setRegistryName(Jacko.MOD_ID, "zerog");
            event.getRegistry().register(zerog);//might need registering
        }
    }
}
