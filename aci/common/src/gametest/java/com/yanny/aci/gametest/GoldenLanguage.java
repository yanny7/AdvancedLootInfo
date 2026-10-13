package com.yanny.aci.gametest;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class GoldenLanguage {
    public static void inject(Collection<String> namespaces) {
        Map<String, String> translations = new HashMap<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            for (String namespace : namespaces) {
                Enumeration<URL> resources = classLoader.getResources("assets/" + namespace + "/lang/" + Language.DEFAULT + ".json");

                while (resources.hasMoreElements()) {
                    try (InputStream stream = resources.nextElement().openStream()) {
                        Language.loadFromJson(stream, translations::putIfAbsent);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Language previous = Language.getInstance();

        Language.inject(new Language() {
            @NotNull
            @Override
            public String getOrDefault(String key, String fallback) {
                return previous.has(key) ? previous.getOrDefault(key, fallback) : translations.getOrDefault(key, fallback);
            }

            @Override
            public boolean has(String key) {
                return previous.has(key) || translations.containsKey(key);
            }

            @Override
            public boolean isDefaultRightToLeft() {
                return previous.isDefaultRightToLeft();
            }

            @NotNull
            @Override
            public FormattedCharSequence getVisualOrder(FormattedText text) {
                return previous.getVisualOrder(text);
            }
        });
    }
}
