package com.yanny.ali.test.utils;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.datagen.LanguageHolder;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
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

    public static void assertTooltip(ITooltipNode tooltip, List<String> expected) {
        List<Component> components = NodeUtils.toComponents(tooltip, 0, true);
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

    public static void assertTooltip(List<ITooltipNode> tooltip, List<String> expected) {
        List<Component> components = NodeUtils.toComponents(tooltip, 0, true);
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

    public static void assertUnorderedTooltip(ITooltipNode tooltip, List<Object> expected) {
        List<Component> components = NodeUtils.toComponents(tooltip, 0, true);
        int cmpIndex = 0;
        int expIndex = 0;

        while (cmpIndex < components.size() && expIndex < expected.size()) {
            Component component = components.get(cmpIndex);
            Object object = expected.get(expIndex);

            if (object instanceof String string) {
                assertTooltip(component, string);
                cmpIndex++;
                expIndex++;
            } else if (object instanceof List list) {
                List<Object> mutableList = new LinkedList<Object>(list);

                for (Object obj : list) {
                    if (obj instanceof String) {
                        Component com = components.get(cmpIndex);
                        String cmp = componentToPlainString(com);

                        if (mutableList.contains(cmp)) {
                            assertTooltip(com, cmp);
                            mutableList.remove(cmp);
                            cmpIndex++;
                        } else {
                            Assertions.fail(String.format("String <%s> not found in expectation List %s", cmp, list));
                        }
                    } else {
                        throw new IllegalStateException("Expected String class");
                    }

                    if (cmpIndex >= components.size() && !mutableList.isEmpty()) {
                        Assertions.fail(String.format("More items than expected: %s", mutableList));
                    }
                }

                expIndex++;
            } else {
                throw new IllegalStateException("Unexpected class " + object.getClass().getName());
            }
        }
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
                    } catch (IOException $$5) {
                        LOGGER.warn("Failed to load translations for {} from pack {}", "en_us", resource.sourcePackId(), $$5);
                    }
                }
            } catch (Exception $$8) {
                LOGGER.warn("Skipped language file: {}:{} ({})", namespace, lang, $$8.toString());
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
