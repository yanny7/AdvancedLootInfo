package com.yanny.awi.test;

import com.mojang.logging.LogUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNodePalette;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.test.utils.TestUtils;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.locale.Language;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.storage.LevelStorageSource;
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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Suite
@SelectClasses({
        FeatureConfigurationTooltipTest.class
})
public class TooltipTestSuite {
    public static IServerUtils UTILS;
    public static HolderLookup.Provider LOOKUP;

    private static Set<String> UNUSED;
    private static final Logger LOGGER = LogUtils.getLogger();

    @BeforeSuite
    static void beforeAllTests() throws NoSuchFieldException, IllegalAccessException {
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();

        ResourceManager resourceManager = loadClientResources();
        Pair<Language, Set<String>> pair = TestUtils.loadDefaultLanguage(resourceManager);

        Language.inject(pair.getA());
        UNUSED = pair.getB();
        LOOKUP = VanillaRegistries.createLookup();

        PluginManager.getInstance().registerCommonEvent();
        PluginManager.getInstance().registerClientEvent();
        PluginManager.getInstance().registerServerEvent(null);
        UTILS = new IServerUtils() {
            @Override
            public @NotNull ServerLevel getServerLevel() {
                return PluginManager.getInstance().serverRegistry.getServerLevel();
            }

            @Override
            public @NotNull TooltipNodePalette getTooltipCache() {
                return PluginManager.getInstance().serverRegistry.getTooltipCache();
            }

            @Override
            public @NotNull HolderLookup.Provider lookupProvider() {
                return LOOKUP;
            }

            @Override
            public int getTranslationKeyIndex(String key) {
                return PluginManager.getInstance().serverRegistry.getTranslationKeyIndex(key);
            }

            @Override
            public @NotNull <T extends FeatureConfiguration> List<Block> collectBlocks(IServerUtils utils, T entry) {
                return PluginManager.getInstance().serverRegistry.collectBlocks(utils, entry);
            }

            @NotNull
            @Override
            public <T extends BlockStateProvider> List<Block> collectBlocks(IServerUtils utils, T entry) {
                return List.of();
            }

            @Override
            public @NotNull <T extends FeatureConfiguration> TooltipBuilder getFeatureTooltip(IServerUtils utils, T entry) {
                return PluginManager.getInstance().serverRegistry.getFeatureTooltip(utils, entry);
            }

            @Override
            public @NotNull <T extends PlacementModifier> TooltipBuilder getPlacementModifierTooltip(IServerUtils utils, T entry) {
                return PluginManager.getInstance().serverRegistry.getPlacementModifierTooltip(utils, entry);
            }

            @Override
            public @NotNull <T extends IntProvider> TooltipBuilder getIntProviderTooltip(IServerUtils utils, T entry) {
                return PluginManager.getInstance().serverRegistry.getIntProviderTooltip(utils, entry);
            }

            @Override
            public @NotNull <T extends RuleTest> TooltipBuilder getRuleTestTooltip(IServerUtils utils, T entry) {
                return PluginManager.getInstance().serverRegistry.getRuleTestTooltip(utils, entry);
            }

            @Override
            public @NotNull <T> TooltipBuilder getValueTooltip(IServerUtils utils, @Nullable T value) {
                return PluginManager.getInstance().serverRegistry.getValueTooltip(utils, value);
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
        LanguageManager languageManager = new LanguageManager("en_us", (lang) -> {});
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
}
