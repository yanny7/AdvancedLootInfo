package com.yanny.awi.test.utils;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.datagen.LanguageHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiFunction;

public class TestUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void assertTooltip(TooltipNode tooltip, List<String> expected) {
        List<Component> components = CoreTooltipUtils.toComponents(tooltip, 0, true);
        List<Executable> executables = new LinkedList<>();

        executables.add(() -> Assertions.assertEquals(expected.size(), components.size()));

        for (int i = 0; i < components.size(); i++) {
            int index = i;

            if (i < expected.size()) {
                executables.add(() -> assertTooltip(components.get(index), expected.get(index), String.format("Index: %d", index)));
            } else {
                executables.add(() -> {
                    throw new AssertionFailedError(String.format("Index: %d ==> expected: <> but was: <%s>", index, componentToPlainString(components.get(index))));
                });
            }
        }

        Assertions.assertAll(executables);
    }

    public static void assertTooltip(List<TooltipNode> tooltip, List<String> expected) {
        List<Component> components = CoreTooltipUtils.toComponents(tooltip, 0, true);
        List<Executable> executables = new LinkedList<>();

        executables.add(() -> Assertions.assertEquals(expected.size(), components.size()));

        for (int i = 0; i < components.size(); i++) {
            int index = i;

            if (i < expected.size()) {
                executables.add(() -> assertTooltip(components.get(index), expected.get(index), String.format("Index: %d", index)));
            }
        }

        Assertions.assertAll(executables);
    }

    public static void assertTooltip(Component component, String expected, String message) {
        String translated = componentToPlainString(component);

        Assertions.assertEquals(expected, translated, message);
    }

    public static void assertTooltip(Component component, String expected) {
        String translated = componentToPlainString(component);

        Assertions.assertEquals(expected, translated);
    }

    @NotNull
    public static String componentToPlainString(Component component) {
        return componentToString(component, (style, text) -> text);
    }

    @NotNull
    public static String componentToStyleString(Component component) {
        return componentToString(component, (style, text) -> {
            StringBuilder sb = new StringBuilder();
            StringBuilder pre = new StringBuilder();
            StringBuilder post = new StringBuilder();

            if (style.getColor() != null) {
                switch (style.getColor().toString()) {
                    case "gold":
                        pre.append("[");
                        post.append("]");
                        break;
                    case "aqua":
                        pre.append("<");
                        post.append(">");
                        break;
                }
            }

            if (style.isBold()) {
                pre.append("*");
                post.append("*");
            }

            return sb.append(pre).append(text).append(post.reverse()).toString();
        });
    }

    /**
     * {@link net.minecraft.server.Bootstrap#bootStrap()} only fills the built-in registries with their elements - tags are
     * datapack-driven and stay empty until {@code TagManager} runs, which never happens in tests. Since
     * {@code VanillaRegistries.createLookup()} exposes built-in registries through {@code MappedRegistry#asLookup()} (a live
     * view of {@code MappedRegistry#tags}), every {@code lookupOrThrow(Registries.BLOCK).getOrThrow(someTag)} fails.
     * This loads the vanilla datapack and binds its tags into the built-in registries, mirroring what {@code TagManager} does.
     */
    public static void bindVanillaTags() {
        PackRepository packRepository = ServerPacksSource.createVanillaTrustedRepository();

        packRepository.reload();
        packRepository.setSelected(List.of("vanilla"));

        try (CloseableResourceManager resourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, packRepository.openAllSelected())) {
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).registries().forEach((entry) -> bindTags(resourceManager, entry));
        }
    }

    private static <T> void bindTags(ResourceManager resourceManager, RegistryAccess.RegistryEntry<T> entry) {
        ResourceKey<? extends Registry<T>> registryKey = entry.key();
        Registry<T> registry = entry.value();
        TagLoader<Holder<T>> tagLoader = new TagLoader<>((location) -> registry.getHolder(ResourceKey.create(registryKey, location)), Registries.tagsDirPath(registryKey));
        Map<TagKey<T>, List<Holder<T>>> tags = new HashMap<>();

        tagLoader.loadAndBuild(resourceManager).forEach((location, holders) -> tags.put(TagKey.create(registryKey, location), List.copyOf(holders)));

        if (!tags.isEmpty()) {
            registry.bindTags(tags);
        }
    }

    @NotNull
    public static Pair<Language, Set<String>> loadDefaultLanguage(ResourceManager resourceManager) {
        ImmutableMap.Builder<String, String> stringBuilder = ImmutableMap.builder();
        LanguageHolder.TRANSLATION_MAP.forEach(stringBuilder::put);
        Set<String> notUsed = new HashSet<>(LanguageHolder.TRANSLATION_MAP.keySet());
        String lang = String.format(Locale.ROOT, "lang/%s.json", "en_us");

        for(String namespace : resourceManager.getNamespaces()) {
            try {
                ResourceLocation langLocation = ResourceLocation.fromNamespaceAndPath(namespace, lang);

                for(Resource resource : resourceManager.getResourceStack(langLocation)) {
                    try (InputStream inputStream = resource.open()) {
                        Language.loadFromJson(inputStream, stringBuilder::put);
                    } catch (IOException e) {
                        LOGGER.warn("Failed to load translations for {} from pack {}", "en_us", resource.sourcePackId(), e);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Skipped language file: {}:{} ({})", namespace, lang, e.toString(), e);
            }
        }

        final Map<String, String> languageMap = stringBuilder.build();

        Language language = new Language() {
            @NotNull
            public String getOrDefault(String key, String value) {
                notUsed.remove(key);
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

        return new Pair<>(language, notUsed);
    }

    @NotNull
    private static String componentToString(Component component, BiFunction<Style, String, String> formatter) {
        StringBuilder builder = new StringBuilder();

        component.visit((style, text) -> {
            if (style.isEmpty()) {
                builder.append(text);
            } else {
                builder.append(formatter.apply(style, text));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return builder.toString();
    }
}
