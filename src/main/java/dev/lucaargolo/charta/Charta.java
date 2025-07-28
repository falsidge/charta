package dev.lucaargolo.charta;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.entity.ModEntityTypes;
import dev.lucaargolo.charta.entity.ModPoiTypes;
import dev.lucaargolo.charta.entity.ModVillagerProfessions;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.game.Rank;
import dev.lucaargolo.charta.game.Suit;
import dev.lucaargolo.charta.item.ModCreativeTabs;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.menu.ModMenus;
import dev.lucaargolo.charta.network.*;
import dev.lucaargolo.charta.resources.CardImageResource;
import dev.lucaargolo.charta.resources.DeckResource;
import dev.lucaargolo.charta.resources.SuitImageResource;
import dev.lucaargolo.charta.sound.ModSounds;
import dev.lucaargolo.charta.utils.PlayerOptionData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

import java.util.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Charta.MOD_ID)
public class Charta {
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace("empty"));
    public static final Set<Suit> DEFAULT_SUITS = Set.of(Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS);
    public static final Set<Rank> DEFAULT_RANKS = Set.of(Rank.ACE, Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING);

    public static final String MOD_ID = "charta";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Style SYMBOLS = Style.EMPTY.withFont(Charta.id("symbols"));
    public static final Style MINERCRAFTORY = Style.EMPTY.withFont(Charta.id("minercraftory"));

    public static final ResourceLocation MISSING_DECK = Charta.id("missing_deck");
    public static final ResourceLocation MISSING_SUIT = Charta.id("missing_suit");
    public static final ResourceLocation MISSING_CARD = Charta.id("missing_card");
    public static final ResourceLocation MISSING_GAME = Charta.id("missing_game");

    public static final SuitImageResource SUIT_IMAGES = new SuitImageResource();
    public static final CardImageResource CARD_IMAGES = new CardImageResource("card");
    public static final CardImageResource DECK_IMAGES = new CardImageResource("deck");
    public static final DeckResource CARD_DECKS = new DeckResource();
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            id("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public static EntityDataAccessor<Boolean> MOB_IRON_LEASH;

    // Define mod id in a common place for everything to reference
    // Directly reference a slf4j logger
    // Create a Deferred Register to hold Blocks which will all be registered under the "chartalegacy" namespace
//    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
//    // Create a Deferred Register to hold Items which will all be registered under the "chartalegacy" namespace
//    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
//    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "chartalegacy" namespace
//    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
//
//    // Creates a new Block with the id "chartalegacy:example_block", combining the namespace and path
//    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
//    // Creates a new BlockItem with the id "chartalegacy:example_block", combining the namespace and path
//    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));
//
//    // Creates a new food item with the id "chartalegacy:example_id", nutrition 1 and saturation 2
//    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(1).saturationMod(2f).build())));
//
//    // Creates a creative tab with the id "chartalegacy:example_tab" for the example item, that is placed after the combat tab
//    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> EXAMPLE_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
//        output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
//    }).build());

    public Charta() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
//        modEventBus.addListener(this::commonSetup);

//        // Register the Deferred Register to the mod event bus so blocks get registered
//        BLOCKS.register(modEventBus);
//        // Register the Deferred Register to the mod event bus so items get registered
//        ITEMS.register(modEventBus);
//        // Register the Deferred Register to the mod event bus so tabs get registered
//        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        registerMessages();
        // Register the item to a creative tab
//        modEventBus.addListener(this::addCreative);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModPoiTypes.register(modEventBus);
        ModVillagerProfessions.register(modEventBus);
        ModBlockEntityTypes.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
//        ModDataComponentTypes.register(modEventBus);
        ModSounds.register(modEventBus);
        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
//        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Charta.MOD_ID, path);
    }



    public static void registerMessages() {
        int id = 0;
        INSTANCE.registerMessage(id++, ImagesPayload.class, ImagesPayload::toBytes, ImagesPayload::fromBytes, ImagesPayload::handleClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(id++, CardDecksPayload.class, CardDecksPayload::toBytes, CardDecksPayload::fromBytes, CardDecksPayload::handleClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(id++, UpdateCardContainerSlotPayload.class, UpdateCardContainerSlotPayload::toBytes, UpdateCardContainerSlotPayload::fromBytes, UpdateCardContainerSlotPayload::handleClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(id++, UpdateCardContainerCarriedPayload.class, UpdateCardContainerCarriedPayload::toBytes, UpdateCardContainerCarriedPayload::fromBytes, UpdateCardContainerCarriedPayload::handleClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(id++, TableScreenPayload.class, TableScreenPayload::toBytes, TableScreenPayload::fromBytes, TableScreenPayload::handleClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(id++, GameSlotCompletePayload.class, GameSlotCompletePayload::toBytes, GameSlotCompletePayload::fromBytes, GameSlotCompletePayload::handleClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(id++, GameSlotPositionPayload.class, GameSlotPositionPayload::toBytes, GameSlotPositionPayload::fromBytes, GameSlotPositionPayload::handleClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(id++, GameSlotResetPayload.class, GameSlotResetPayload::toBytes, GameSlotResetPayload::fromBytes, GameSlotResetPayload::handleClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(id++, GameStartPayload.class, GameStartPayload::toBytes, GameStartPayload::fromBytes, GameStartPayload::handleClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(id++, CardPlayPayload.class, CardPlayPayload::toBytes, CardPlayPayload::fromBytes, CardPlayPayload::handleClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));


        INSTANCE.registerMessage(id++, CardContainerSlotClickPayload.class, CardContainerSlotClickPayload::toBytes, CardContainerSlotClickPayload::fromBytes, CardContainerSlotClickPayload::handleServer, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(id++, CardTableSelectGamePayload.class, CardTableSelectGamePayload::toBytes, CardTableSelectGamePayload::fromBytes, CardTableSelectGamePayload::handleServer, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(id++, RestoreSolitairePayload.class, RestoreSolitairePayload::toBytes, RestoreSolitairePayload::fromBytes, RestoreSolitairePayload::handleServer, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        INSTANCE.registerMessage(id++, LastFunPayload.class, LastFunPayload::toBytes, LastFunPayload::fromBytes, LastFunPayload::handleBoth);
        INSTANCE.registerMessage(id++, PlayerOptionsPayload.class, PlayerOptionsPayload::toBytes, PlayerOptionsPayload::fromBytes, PlayerOptionsPayload::handleBoth);
        INSTANCE.registerMessage(id++, GameLeavePayload.class, GameLeavePayload::toBytes, GameLeavePayload::fromBytes, GameLeavePayload::handleBoth);
    }
//    private void commonSetup(final FMLCommonSetupEvent event) {
//        // Some common setup code
////        LOGGER.info("HELLO FROM COMMON SETUP");
////        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
////
////        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
////
////        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
////
////        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
//    }

//    // Add the example block item to the building blocks tab
//    private void addCreative(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM);
//    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
//    @SubscribeEvent
//    public void onServerStarting(ServerStartingEvent event) {
//        // Do something when the server starts
//        LOGGER.info("HELLO from server starting");
//    }

//    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
//    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
//    public static class ClientModEvents {
//
//        @SubscribeEvent
//        public static void onClientSetup(FMLClientSetupEvent event) {
//            // Some client setup code
//            LOGGER.info("HELLO FROM CLIENT SETUP");
//            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
//        }
//    }

    @Mod.EventBusSubscriber(modid = Charta.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class GameEvents {

        @SubscribeEvent
        public static void serverAboutToStart(final ServerAboutToStartEvent event) {
            MinecraftServer server = event.getServer();
            RegistryAccess registryAccess = server.registryAccess();
            Registry<StructureTemplatePool> templatePoolRegistry = registryAccess.registry(Registries.TEMPLATE_POOL).orElseThrow();
            Registry<StructureProcessorList> processorListRegistry = registryAccess.registry(Registries.PROCESSOR_LIST).orElseThrow();

//            addBuildingToPool(templatePoolRegistry, processorListRegistry,
//                    ResourceLocation.parse("minecraft:village/plains/houses"),
//                    "charta:plains_card_bar", 50);
//
//            addBuildingToPool(templatePoolRegistry, processorListRegistry,
//                    ResourceLocation.parse("minecraft:village/desert/houses"),
//                    "charta:desert_card_bar", 50);
//
//            addBuildingToPool(templatePoolRegistry, processorListRegistry,
//                    ResourceLocation.parse("minecraft:village/taiga/houses"),
//                    "charta:taiga_card_bar", 40);
//
//            addBuildingToPool(templatePoolRegistry, processorListRegistry,
//                    ResourceLocation.parse("minecraft:village/savanna/houses"),
//                    "charta:savanna_card_bar", 60);
        }

        @SubscribeEvent
        public static void addReloadListeners(final AddReloadListenerEvent event) {
            event.addListener(SUIT_IMAGES);
            event.addListener(CARD_IMAGES);
            event.addListener(DECK_IMAGES);
            event.addListener(CARD_DECKS);
        }

        @SubscribeEvent
        public static void onChunkSent(final ChunkWatchEvent.Watch event) {
            LevelChunk chunk = event.getChunk();
            chunk.getBlockEntities().forEach((pos, blockEntity) -> {
                if(blockEntity instanceof CardTableBlockEntity cardTable) {
                    int count = cardTable.getSlotCount();
                    for(int i = 0; i < count; i++) {
                        GameSlot slot = cardTable.getSlot(i);
                        GameSlotCompletePayload payload = new GameSlotCompletePayload(pos, i, slot);
//                        PacketDistributor.sendToPlayer(event.getPlayer(), payload);
                        INSTANCE.send(PacketDistributor.PLAYER.with(event::getPlayer), payload);
                    }
                }
            });

        }

        @SubscribeEvent
        public static void onPlayerJoined(final PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            if(player instanceof ServerPlayer serverPlayer) {
                INSTANCE.send(PacketDistributor.PLAYER.with(()->serverPlayer), new ImagesPayload(
                        new HashMap<>(Charta.SUIT_IMAGES.getImages()),
                        new HashMap<>(Charta.CARD_IMAGES.getImages()),
                        new HashMap<>(Charta.DECK_IMAGES.getImages())
                ));
                INSTANCE.send(PacketDistributor.PLAYER.with(()->serverPlayer), new CardDecksPayload(new LinkedHashMap<>(Charta.CARD_DECKS.getDecks())));
                PlayerOptionData data = serverPlayer.server.overworld().getDataStorage().computeIfAbsent(PlayerOptionData::load, PlayerOptionData::new,"charta_player_options");
                INSTANCE.send(PacketDistributor.PLAYER.with(()->serverPlayer), new PlayerOptionsPayload(data.getPlayerOptions(serverPlayer)));
            }
        }

        @SubscribeEvent
        public static void onDatapackReload(final OnDatapackSyncEvent event) {
            INSTANCE.send(PacketDistributor.ALL.noArg(),new ImagesPayload(
                    new HashMap<>(Charta.SUIT_IMAGES.getImages()),
                    new HashMap<>(Charta.CARD_IMAGES.getImages()),
                    new HashMap<>(Charta.DECK_IMAGES.getImages())
            ));
            INSTANCE.send(PacketDistributor.ALL.noArg(),new CardDecksPayload(new LinkedHashMap<>(Charta.CARD_DECKS.getDecks())));
        }

    }
    private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry, ResourceLocation poolRL, String nbtPieceRL, int weight) {
        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);
        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceRL, emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }

}
