package com.yanny.ali.test;

import com.google.common.collect.ImmutableMap;
import com.yanny.ali.datagen.LanguageHolder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class TestTest {

//    @BeforeAll
//    public static void setup() {
//        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
//        Bootstrap.bootStrap();
//
//        // 1. Set up ServerPacksSource and PackRepository
//        ServerPacksSource serverPacksSource = new ServerPacksSource();
//
//        // 1. Create a FolderRepositorySource for test resources
//        FolderRepositorySource folderRepositorySource = new FolderRepositorySource(new File("src/test/resources").toPath(), PackType.SERVER_DATA, PackSource.DEFAULT); // Use FolderRepositorySource
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
//
//        ServerLevel serverLevel = mock(ServerLevel.class);
//        Objenesis objenesis = new ObjenesisStd(); // or ObjenesisSerializer
//        ObjectInstantiator<FakeServer> instantiator = objenesis.getInstantiatorOf(FakeServer.class);
//        FakeServer minecraftServer = instantiator.newInstance();
//
//        minecraftServer.manager = manager.get();
//        when(serverLevel.getServer()).thenReturn(minecraftServer);
//        when(serverLevel.getRandomSequence(null)).thenReturn(new RandomSequences(42L).get(new ResourceLocation("test")));
//
//
//        manager.get().getKeys(LootDataType.TABLE).forEach((location) -> {
//            LootTable table = manager.get().getElement(LootDataType.TABLE, location);
//
//            if (table != null && table != LootTable.EMPTY) {
//                LootParams lootParams = (new LootParams.Builder(serverLevel)).create(LootContextParamSets.EMPTY);
//                LootContext context = new LootContext.Builder(lootParams).create(null);
//                AliContext aliContext = new AliContext(context, PluginManager.COMMON_REGISTRY, manager.get());
//                LootTableEntry lootTableEntry = new LootTableEntry(aliContext, table);
//            }
//        });
//    }

    @NotNull
    public static String componentToString(Component component, Style baseStyle, BiFunction<Style, String, String> formatter) {
        StringBuilder builder = new StringBuilder();

        component.visit((style, text) -> {
            if (style.isEmpty()) {
                builder.append(text);
            } else {
                builder.append(formatter.apply(style, text));
            }
            return Optional.empty();
        }, baseStyle);
        return builder.toString();
    }

    @NotNull
    public static String componentToPlainString(Component component) {
        return componentToString(component, Style.EMPTY, (style, text) -> text);
    }

    @NotNull
    private static Language loadDefault() {
        ImmutableMap.Builder<String, String> stringBuilder = ImmutableMap.builder();
        BiConsumer<String, String> biConsumer = stringBuilder::put;
        LanguageHolder.TRANSLATION_MAP.forEach(biConsumer);
        final Map<String, String> languageMap = stringBuilder.build();
        
        return new Language() {
            @NotNull
            public String getOrDefault(String key, String value) {
                return Objects.requireNonNull(languageMap.getOrDefault(key, value));
            }

            public boolean has(String key) {
                return languageMap.containsKey(key);
            }

            public boolean isDefaultRightToLeft() {
                return false;
            }

            @NotNull
            public FormattedCharSequence getVisualOrder(FormattedText formattedText) {
                return (charSink) -> 
                        formattedText.visit((style, text) -> 
                                StringDecomposer.iterateFormatted(text, style, charSink) ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY).isPresent();
            }
        };
    }
}
