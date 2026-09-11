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
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Suite
@SelectPackages("com.yanny.alicompat.compat")
@IncludeClassNamePatterns(".*TooltipTest")
public class CompatTooltipSuite {
    public static IServerUtils UTILS;

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Set<String> UNUSED;

    @BeforeSuite
    public static void beforeAllTests() {
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        bootStrapWithoutForge();
        unfreezeRegistries();
        installModList();

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

    // A target mod's loot types register from its class initializer, long after Bootstrap froze the registries
    private static void unfreezeRegistries() {
        try {
            Field frozen = MappedRegistry.class.getDeclaredField("frozen");

            frozen.setAccessible(true);

            for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
                if (registry instanceof MappedRegistry<?> mappedRegistry) {
                    frozen.set(mappedRegistry, false);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to unfreeze registries", e);
        }
    }

    // Target mods ask ModList whether a mod is present; without an FML launch ModList.get() is null
    private static void installModList() {
        ModList modList = Mockito.mock(ModList.class);

        Mockito.when(modList.isLoaded(Mockito.anyString())).thenReturn(true);

        try {
            Field instance = ModList.class.getDeclaredField("INSTANCE");

            instance.setAccessible(true);
            instance.set(null, modList);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to install test ModList", e);
        }
    }

    // Forge's bootStrap tail calls NetworkHooks#init, needing an ASM transformer only an FML launch installs
    private static void bootStrapWithoutForge() {
        try {
            Bootstrap.bootStrap();
        } catch (Throwable e) {
            LOGGER.info("Forge part of bootstrap failed as expected: {}", e.toString());
        }
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
        ReloadInstance reloadInstance = resourceManager.createReload(Util.backgroundExecutor(), Util.backgroundExecutor(), CompletableFuture.completedFuture(Unit.INSTANCE), list);

        try {
            reloadInstance.done().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to load resources for test setup", e);
        }

        return resourceManager;
    }
}
