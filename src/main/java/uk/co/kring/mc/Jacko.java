package uk.co.kring.mc;

import net.minecraft.block.*;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.command.Commands;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
//import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
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

//IMC multiple IPC combine to list received values of sent evaluations of closure results
import java.util.stream.Collectors;

//command arguments
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.FloatArgumentType.*;

//ObjectHolder
import static uk.co.kring.mc.Blocks.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("jacko")
public class Jacko {

    //public static TileEntityType<TileEntityCommon> tileEntityDataType;
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public Jacko() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}",
                event.getMinecraftSupplier().get().gameSettings);
        //IMC supplier and field
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("jacko", "hello_world",
                () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
        //binding element manufacture to supply
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.getMessageSupplier().get()).
                collect(Collectors.toList()));
        //collect all supplied as a list
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
        event.getCommandDispatcher().register(
                Commands.literal("foo")
                        .requires(source -> source.hasPermissionLevel(4))
                        .then(
                                Commands.argument("bar", integer())
                                        .executes(context -> {
                                            System.out.println("Bar is " + getInteger(context, "bar"));
                                            return 1;
                                        })
                        )
                        .executes(context -> {
                            System.out.println("Called foo with no arguments");
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

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            Block unlock = new Block(Block.Properties.create(Material.MISCELLANEOUS)
                    .hardnessAndResistance(3f, 3f) //emerald level
                    .sound(SoundType.STONE)
                    .slipperiness(0.5f)
                    .harvestTool(ToolType.PICKAXE)
            );
            unlock.setRegistryName("jacko", "unlock");
            //see @ObjectHolder in uk.co.kring.mc.Blocks field import static
            event.getRegistry().register(unlock);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            Item.Properties itemP = new Item.Properties().group(ItemGroup.MATERIALS);
            BlockItem unlockItem = new BlockItem(unlock, itemP);
            unlockItem.setRegistryName(unlock.getRegistryName());
            event.getRegistry().register(unlockItem);
            itemP = new Item.Properties().group(ItemGroup.MISC);
            Item bookItem = new Item(itemP);
            bookItem.setRegistryName("jacko", "book");
            event.getRegistry().register(bookItem);
            itemP = new Item.Properties().group(ItemGroup.BREWING);
            Item potion = new Item(itemP);
            potion.setRegistryName("jacko", "zerog");
            event.getRegistry().register(potion);
        }

        /* @SubscribeEvent
        public static void registerTileEntityTypes(final RegistryEvent.Register<TileEntityType<?>> event) {
            tileEntityDataType = TileEntityType.Builder
                    .create(TileEntityCommon::new, blockRedstoneMeter).build(null);
            // you probably don't need a datafixer --> null should be fine
            tileEntityDataType.setRegistryName("minecraftbyexample:mbe06_tile_entity_type_registry_name");
            event.getRegistry().register(tileEntityDataType);
        } */
    }
}
