package com.yanny.ali.test;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.test.utils.TestUtils;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Suite
@SelectClasses({
        GenericTooltipTest.class,
        RegistriesTooltipTest.class,
        ConditionTooltipTest.class,
        FunctionTooltipTest.class,
        EntryTooltipTest.class,
        IngredientTooltipTest.class,
        TooltipTest.class
})
public class TooltipTestSuite {
    public static IServerUtils UTILS;

    private static Set<String> UNUSED;
    private static final Logger LOGGER = LogUtils.getLogger();

    @BeforeSuite
    static void beforeAllTests() {
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        ResourceManager resourceManager = loadClientResources();
        Pair<Language, Set<String>> pair = TestUtils.loadDefaultLanguage(resourceManager);

        Language.inject(pair.getA());
        UNUSED = pair.getB();

        PluginManager.registerCommonEvent();
        PluginManager.registerClientEvent();
        PluginManager.registerServerEvent();
        UTILS = new IServerUtils() {
            @Override
            public List<Entity> createEntities(EntityType<?> type, Level level) {
                return PluginManager.SERVER_REGISTRY.createEntities(type, level);
            }

            @Override
            public List<LootPool> getLootPools(LootTable lootTable) {
                return PluginManager.SERVER_REGISTRY.getLootPools(lootTable);
            }

            @Override
            public <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry) {
                return PluginManager.SERVER_REGISTRY.collectItems(utils, entry);
            }

            @Override
            public <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function) {
                return PluginManager.SERVER_REGISTRY.collectItems(utils, items, function);
            }

            @Override
            public <T extends LootPoolEntryContainer> IServerRegistry.EntryFactory<T> getEntryFactory(IServerUtils utils, T type) {
                return PluginManager.SERVER_REGISTRY.getEntryFactory(utils, type);
            }

            @Override
            public <T extends LootItemFunction> ITooltipNode getFunctionTooltip(IServerUtils utils, T function) {
                return PluginManager.SERVER_REGISTRY.getFunctionTooltip(utils, function);
            }

            @Override
            public <T extends LootItemCondition> ITooltipNode getConditionTooltip(IServerUtils utils, T condition) {
                return PluginManager.SERVER_REGISTRY.getConditionTooltip(utils, condition);
            }

            @Override
            public <T extends Ingredient> ITooltipNode getIngredientTooltip(IServerUtils utils, T ingredient) {
                return PluginManager.SERVER_REGISTRY.getIngredientTooltip(utils, ingredient);
            }

            @Override
            public <T extends LootItemFunction> void applyCountModifier(IServerUtils utils, T function, Map<Enchantment, Map<Integer, RangeValue>> count) {
                PluginManager.SERVER_REGISTRY.applyCountModifier(utils, function, count);
            }

            @Override
            public <T extends LootItemCondition> void applyChanceModifier(IServerUtils utils, T condition, Map<Enchantment, Map<Integer, RangeValue>> chance) {
                PluginManager.SERVER_REGISTRY.applyChanceModifier(utils, condition, chance);
            }

            @Override
            public <T extends LootItemFunction> ItemStack applyItemStackModifier(IServerUtils utils, T function, ItemStack itemStack) {
                return PluginManager.SERVER_REGISTRY.applyItemStackModifier(utils, function, itemStack);
            }

            @Override
            public <T extends VillagerTrades.ItemListing> IDataNode getItemListing(IServerUtils utils, T entry, List<ITooltipNode> conditions) {
                return PluginManager.SERVER_REGISTRY.getItemListing(utils, entry, conditions);
            }

            @Override
            public <T extends VillagerTrades.ItemListing> Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, T entry) {
                return PluginManager.SERVER_REGISTRY.collectItems(utils, entry);
            }

            @Override
            public RangeValue convertNumber(IServerUtils utils, @Nullable NumberProvider numberProvider) {
                return PluginManager.SERVER_REGISTRY.convertNumber(utils, numberProvider);
            }

            @Override
            public @Nullable ServerLevel getServerLevel() {
                return PluginManager.SERVER_REGISTRY.getServerLevel();
            }

            @Override
            public LootContext getLootContext() {
                return PluginManager.SERVER_REGISTRY.getLootContext();
            }

            @Override
            public LootTable getLootTable(ResourceLocation location) {
                return PluginManager.SERVER_REGISTRY.getLootTable(location);
            }
        };

        LOGGER.info("----- Translation keys ({}) -----", UNUSED.size());
    }

    @AfterSuite
    static void afterAllTests() {
        LOGGER.info("----- Unused translation keys ({}) -----", UNUSED.size());
        UNUSED.stream().sorted().forEach(LOGGER::info);
    }

    @NotNull
    private static ResourceManager loadClientResources() {
        LanguageManager languageManager = new LanguageManager(LanguageManager.DEFAULT_LANGUAGE_CODE);
        ReloadableResourceManager resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);

        resourceManager.registerReloadListener(languageManager);

        Path resourcePackDirectory = new File("src/test/resources").toPath();
        ClientPackSource clientpacksource = new ClientPackSource(resourcePackDirectory.resolve("assets"));
        PackRepository resourcePackRepository = new PackRepository(clientpacksource);

        resourcePackRepository.reload();
        resourcePackRepository.setSelected(List.of("vanilla"));

        List<PackResources> list = resourcePackRepository.openAllSelected();
        ReloadInstance reloadinstance = resourceManager.createReload(Util.backgroundExecutor(), Util.backgroundExecutor(), CompletableFuture.completedFuture(Unit.INSTANCE), list);

        CompletableFuture<?> completableFuture = reloadinstance.done();

        try {
            completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to load resources for test setup", e);
        }

        return resourceManager;
    }

//    private void loadServerResources() {
//        // 1. Set up ServerPacksSource and PackRepository
//        ServerPacksSource serverPacksSource = new ServerPacksSource();
//
//        // 1. Create a FolderRepositorySource for test resources
//        FolderRepositorySource folderRepositorySource = new FolderRepositorySource(new File("src/test/resources").toPath(), PackType.CLIENT_RESOURCES, PackSource.DEFAULT); // Use FolderRepositorySource
//
//        // 2. Create PackRepository with FolderRepositorySource and ServerPacksSource
//        PackRepository packRepository = new PackRepository(serverPacksSource, folderRepositorySource); // Pass RepositorySources to constructor
//        packRepository.reload(); // Reload packs
//
//        // 2. Create PackConfig
//        WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(
//                packRepository,
//                new WorldDataConfiguration(DataPackConfig.DEFAULT, FeatureFlags.DEFAULT_FLAGS), // Use default data pack config and feature flags for test
//                false, // safeMode = false
//                false  // initMode = false
//        );
//
//        WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(
//                packConfig,
//                Commands.CommandSelection.ALL,
//                2
//        );
//
//        AtomicReference<LootDataManager> manager = new AtomicReference<>();
//
//        // 5. Load resources using WorldLoader.load() (no changes needed here)
//        CompletableFuture<WorldLoader.DataLoadOutput<Void>> loadFuture = WorldLoader.load(
//                initConfig,
//                (dataLoadContext) -> {
//                    return new WorldLoader.DataLoadOutput<>(null, RegistryAccess.Frozen.EMPTY);
//                },
//                (resourceManager, serverResources, layeredRegistryAccess, unused) -> {
//                    manager.set(serverResources.getLootData());
//                    return null;
//                },
//                Util.backgroundExecutor(),
//                Runnable::run
//        );
//
//        // 6. Wait for resource loading to complete (no changes needed here)
//        try {
//            loadFuture.get();
//        } catch (InterruptedException | ExecutionException e) {
//            // serverResourceManager.close(); // No close() method on WorldLoader. Let GC handle resources.
//            throw new RuntimeException("Failed to load resources for test setup", e);
//        }
//    }
}
