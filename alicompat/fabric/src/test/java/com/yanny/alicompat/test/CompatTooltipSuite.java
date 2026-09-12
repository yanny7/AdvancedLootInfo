package com.yanny.alicompat.test;

import com.mojang.logging.LogUtils;
import com.yanny.aci.test.utils.TestUtils;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.datagen.LanguageHolder;
import com.yanny.ali.manager.PluginManager;
import com.yanny.alicompat.ModCompatManager;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.locale.Language;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Suite(failIfNoTests = false)
@SelectPackages("com.yanny.alicompat.compat")
@IncludeClassNamePatterns(".*TooltipTest")
public class CompatTooltipSuite {
    public static IServerUtils UTILS;

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Set<String> UNUSED;

    @BeforeSuite
    public static void beforeAllTests() {
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();

        ResourceManager resourceManager = loadClientResources();
        Map<String, String> translations = new HashMap<>(LanguageHolder.TRANSLATION_MAP);

        translations.putAll(ModCompatManager.collectTranslations(false));

        TestUtils.LoadedLanguage loadedLanguage = TestUtils.loadDefaultLanguage(resourceManager, translations);

        Language.inject(loadedLanguage.language());
        TestUtils.bindVanillaTags();
        UNUSED = loadedLanguage.unusedKeys();

        PluginManager.getInstance().registerCommonEvent();
        PluginManager.getInstance().registerClientEvent();
        PluginManager.getInstance().registerServerEvent(null);
        TooltipContext.setPalette(PluginManager.getInstance().serverRegistry.getTooltipCache());

        AliConfig config = PluginManager.getInstance().serverRegistry.getConfiguration();

        config.showInGameNames = false;
        UTILS = PluginManager.getInstance().serverRegistry;
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
        ReloadInstance reloadInstance = resourceManager.createReload(Util.backgroundExecutor(), Util.backgroundExecutor(), CompletableFuture.completedFuture(Unit.INSTANCE), list);

        try {
            reloadInstance.done().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to load resources for test setup", e);
        }

        return resourceManager;
    }
}
