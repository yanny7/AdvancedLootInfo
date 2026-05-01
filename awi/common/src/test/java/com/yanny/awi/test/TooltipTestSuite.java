package com.yanny.awi.test;

import com.mojang.logging.LogUtils;
import com.yanny.awi.api.IKeyTooltipNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.test.utils.TestUtils;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
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

        PluginManager.getInstance().registerCommonEvent();
        PluginManager.getInstance().registerClientEvent();
        PluginManager.getInstance().registerServerEvent();
        UTILS = new IServerUtils() {
            @Override
            public @Nullable ServerLevel getServerLevel() {
                return PluginManager.getInstance().serverRegistry.getServerLevel();
            }

            @Override
            public @Nullable HolderLookup.Provider lookupProvider() {
                return PluginManager.getInstance().serverRegistry.lookupProvider();
            }

            @Override
            public @NotNull ITooltipNode buildTooltip(IKeyTooltipNode keyTooltipNode) {
                return PluginManager.getInstance().serverRegistry.buildTooltip(keyTooltipNode);
            }

            @Override
            public @NotNull IKeyTooltipNode getBranchNode() {
                return PluginManager.getInstance().serverRegistry.getBranchNode();
            }

            @Override
            public @NotNull IKeyTooltipNode getBranchNode(boolean isAdvancedTooltip) {
                return PluginManager.getInstance().serverRegistry.getBranchNode(isAdvancedTooltip);
            }

            @Override
            public @NotNull IKeyTooltipNode getValueNode(Object... value) {
                return PluginManager.getInstance().serverRegistry.getValueNode(value);
            }

            @Override
            public @NotNull IKeyTooltipNode getKeyValueNode(Object key, Object value) {
                return PluginManager.getInstance().serverRegistry.getKeyValueNode(key, value);
            }

            @Override
            public @NotNull IKeyTooltipNode getComponentNode(Component... values) {
                return PluginManager.getInstance().serverRegistry.getComponentNode(values);
            }

            @Override
            public @NotNull ITooltipNode getLiteralNode(String translatable) {
                return PluginManager.getInstance().serverRegistry.getLiteralNode(translatable);
            }

            @Override
            public @NotNull IKeyTooltipNode getEmptyNode() {
                return PluginManager.getInstance().serverRegistry.getEmptyNode();
            }

            @Override
            public @NotNull IKeyTooltipNode getErrorNode(String error) {
                return PluginManager.getInstance().serverRegistry.getErrorNode(error);
            }

            @Override
            public @NotNull <T extends FeatureConfiguration> List<Item> getItemCollector(IServerUtils utils, T entry) {
                return PluginManager.getInstance().serverRegistry.getItemCollector(utils, entry);
            }

            @Override
            public @NotNull <T extends FeatureConfiguration> ITooltipNode getFeatureTooltip(IServerUtils utils, T entry) {
                return PluginManager.getInstance().serverRegistry.getFeatureTooltip(utils, entry);
            }

            @Override
            public @NotNull <T extends PlacementModifier> ITooltipNode getPlacementModifierTooltip(IServerUtils utils, T entry) {
                return PluginManager.getInstance().serverRegistry.getPlacementModifierTooltip(utils, entry);
            }

            @Override
            public @NotNull <T extends IntProvider> IKeyTooltipNode getIntProviderTooltip(IServerUtils utils, T entry) {
                return PluginManager.getInstance().serverRegistry.getIntProviderTooltip(utils, entry);
            }

            @Override
            public @NotNull <T extends RuleTest> IKeyTooltipNode getRuleTestTooltip(IServerUtils utils, T entry) {
                return PluginManager.getInstance().serverRegistry.getRuleTestTooltip(utils, entry);
            }

            @Override
            public @NotNull <T> IKeyTooltipNode getValueTooltip(IServerUtils utils, @Nullable T value) {
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
