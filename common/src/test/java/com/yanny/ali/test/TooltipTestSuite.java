package com.yanny.ali.test;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.Rect;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.Utils;
import com.yanny.ali.test.utils.TestUtils;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.locale.Language;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Suite
@SelectClasses({
        ConditionTooltipTest.class,
        FunctionTooltipTest.class,
        GenericTooltipTest.class,
})
public class TooltipTestSuite {
    public static Utils UTILS;

    private static Set<String> UNUSED;
    public static HolderLookup.Provider LOOKUP;

    @BeforeSuite
    static void beforeAllTests() {
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        ResourceManager resourceManager = loadClientResources();
        Pair<Language, Set<String>> pair = TestUtils.loadDefaultLanguage(resourceManager);

        Language.inject(pair.getFirst());
        UNUSED = pair.getSecond();
        LOOKUP = VanillaRegistries.createLookup();

        PluginManager.registerCommonEvent();
        PluginManager.registerClientEvent();
        UTILS = new Utils() {
            @Override
            public Rect addSlotWidget(Item item, LootPoolEntryContainer entry, int x, int y, RangeValue chance, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance, RangeValue count, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                return null;
            }

            @Override
            public Rect addSlotWidget(TagKey<Item> item, LootPoolEntryContainer entry, int x, int y, RangeValue chance, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance, RangeValue count, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                return null;
            }
        };

        System.out.printf("----- Translation keys (%d) -----\n", UNUSED.size());
    }

    @AfterSuite
    static void afterAllTests() {
        System.out.printf("----- Unused translation keys (%d) -----\n", UNUSED.size());
        UNUSED.stream().sorted().forEach(System.out::println);
    }

    @NotNull
    private static ResourceManager loadClientResources() {
        LanguageManager languageManager = new LanguageManager("en_us");
        ReloadableResourceManager resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);

        resourceManager.registerReloadListener(languageManager);

        Path resourcePackDirectory = new File("src/test/resources").toPath();
        ClientPackSource clientpacksource = new ClientPackSource(resourcePackDirectory.resolve("assets"), LevelStorageSource.parseValidator(resourcePackDirectory.resolve("allowed_symlinks.txt")));
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
