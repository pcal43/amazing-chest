package net.pcal.amazingchest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.pcal.amazingchest.AcService.CacheInvalidationPolicy;
import net.pcal.amazingchest.network.LockPacket;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static net.minecraft.util.registry.Registry.register;
import static net.pcal.amazingchest.AcIdentifiers.*;

public class AcInitializer implements ModInitializer, ClientModInitializer {

    // ===================================================================================
    // Constants

    private static final String CONFIG_FILENAME = "amazingchest.properties";
    private static final String DEFAULT_CONFIG_FILENAME = "default-amazingchest.properties";
    private static final String LOGGER_NAME = "net.pcal.amazingchest.AcService";
    private static final String POLYMER_REGISTRAR_CLASS = "net.pcal.amazingchest.polymer.PolymerRegistrar";

    // ===================================================================================
    // Initializer implementation

    @Override
    public void onInitialize() {
        new ExactlyOnceServiceInitializer();
    }

    @Override
    public void onInitializeClient() {
        new ExactlyOnceServiceInitializer();
        // client stuff
        ScreenRegistry.register(AcIdentifiers.getScreenHandlerType(), AcScreen::new);
        BlockEntityType<AmazingChestBlockEntity> entityType = AcIdentifiers.getAcBlockEntityType();
        BlockEntityRendererRegistry.register(entityType, ChestBlockEntityRenderer::new);
        ClientSpriteRegistryCallback.event(TexturedRenderLayers.CHEST_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            registry.register(AC_TEXTURE);
            registry.register(AC_TEXTURE_RIGHT);
            registry.register(AC_TEXTURE_LEFT);
        });
        AmazingChestBlock acBlock = AcIdentifiers.getAcBlock();
        BuiltinItemRendererRegistry.INSTANCE.register(acBlock, (stack, mode, matrices, vertexConsumers, light, overlay) -> {
            MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(entityType.
                    instantiate(BlockPos.ORIGIN, acBlock.getDefaultState()), matrices, vertexConsumers, light, overlay);
        });
    }
    // ===================================================================================
    // Private

    private static class ExactlyOnceServiceInitializer {
        static {
            initializeService();
        }
    }

    /**
     * Initialize
     */
    private static void initializeService() {
        final Logger logger = LogManager.getLogger(AcService.class.getName());
        try {
            final Properties config;
            final Path configFilePath = Paths.get("config", CONFIG_FILENAME);
            final File configFile = configFilePath.toFile();
            //
            // write out default config file if none exists
            if (!configFile.exists()) {
                try (InputStream in = AcInitializer.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILENAME)) {
                    if (in == null) {
                        throw new IllegalStateException("Unable to load " + DEFAULT_CONFIG_FILENAME);
                    }
                    // I guess config might not yet exist on fresh install
                    java.nio.file.Files.createDirectories(configFilePath.getParent());
                    java.nio.file.Files.copy(in, configFilePath);
                    logger.info(LOG_PREFIX + "Wrote default configuration to " + configFilePath);
                }
            }
            //
            // load the configuration
            try (final InputStream in = new FileInputStream(configFilePath.toFile())) {
                Properties newProps = new Properties();
                newProps.load(in);
                config = newProps;
            }
            //
            // get the caching policy
            final String configuredCachePolicy = config.getProperty("cache-policy");
            final CacheInvalidationPolicy cachePolicy;
            if (configuredCachePolicy == null) {
                cachePolicy = CacheInvalidationPolicy.DEFAULT;
            } else {
                cachePolicy = CacheInvalidationPolicy.valueOf(configuredCachePolicy);
                logger.info(LOG_PREFIX + "cachePolicy set to " + cachePolicy);
            }
            AcService.initialize(cachePolicy, logger);

            LockPacket.registerReceivePacket();

            //
            // register blocks
            final String polymerEnabled = config.getProperty("polymer-enabled");
            if ("true".equals(polymerEnabled)) {
                logger.info("Initializing polymer.");
                ((Runnable) Class.forName(POLYMER_REGISTRAR_CLASS).getDeclaredConstructor().newInstance()).run();
            } else {
                doStandardRegistrations();
            }
            PlayerBlockBreakEvents.AFTER.register(AcService.getInstance());
            logger.info(LOG_PREFIX + "Initialized");
        } catch (Exception e) {
            logger.catching(Level.ERROR, e);
            logger.error(LOG_PREFIX + "Failed to initialize");
        }
    }

    /**
     * Create and register all of our blocks and items for non-polymer mode.
     */
    private static void doStandardRegistrations() {
        ScreenHandlerRegistry.registerSimple(AC_SCREEN_ID, AcScreenHandler::createForRegistration);
        final AmazingChestBlock acBlock = new AmazingChestBlock();
        final BlockItem acItem = new BlockItem(acBlock, new Item.Settings().group(ItemGroup.REDSTONE));
        //acItem.appendBlocks(Item.BLOCK_ITEMS, acItem); // wat
        register(Registry.BLOCK_ENTITY_TYPE, AC_BLOCK_ENTITY_TYPE_ID,
                FabricBlockEntityTypeBuilder.create(AmazingChestBlockEntity::new, acBlock).build(null));
        register(Registry.ITEM, AC_ITEM_ID, acItem);
        register(Registry.BLOCK, AC_BLOCK_ID, acBlock);
    }
}
