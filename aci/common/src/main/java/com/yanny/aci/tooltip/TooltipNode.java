package com.yanny.aci.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TooltipNode {
    public static final ChatFormatting TEXT_STYLE = ChatFormatting.GOLD;
    public static final ChatFormatting VALUE_STYLE = ChatFormatting.AQUA;
    public static final ChatFormatting ERROR_STYLE = ChatFormatting.RED;

    public static final short FLAG_ARRAY     = 1;
    public static final short FLAG_ADVANCED  = 1 << 1;
    public static final short FLAG_ERROR     = 1 << 2;
    public static final short FLAG_EMPTY     = 1 << 3;
    public static final short FLAG_RAW_KEY   = 1 << 4;
    public static final short FLAG_HAS_KEY   = 1 << 5;
    public static final short FLAG_HAS_VALUE = 1 << 6;
    public static final short FLAG_COMPONENT = 1 << 7;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LoadingCache<CacheKey, TooltipNode> CACHE = CacheBuilder.newBuilder()
            .recordStats()
            .build(CacheLoader.from(TooltipNode::new));

    public static final TooltipNode EMPTY_INSTANCE = getOrCreate(null, null, null, FLAG_EMPTY, List.of());

    private @Nullable final String key;
    private @Nullable final String[] values;
    private @Nullable final Component component;
    private final short flags;
    private final List<TooltipNode> children;

    private TooltipNode(CacheKey cacheKey) {
        this.key = cacheKey.key();
        this.values = cacheKey.values() != null ? cacheKey.values().toArray(new String[0]) : null;
        this.component = cacheKey.componentValue();
        this.flags = cacheKey.flags();
        this.children = cacheKey.children();
    }

    @Nullable
    public String[] getValues() {
        return values;
    }

    public boolean isComplex() {
        return (flags & FLAG_ARRAY) != 0 || children.size() > 1 || (children.size() == 1 && children.getFirst().isComplex());
    }

    @NotNull
    private MutableComponent indent(int level) {
        return Component.literal("  ".repeat(level));
    }

    public boolean isEmpty(boolean isAdvanced) {
        if ((flags & FLAG_EMPTY) != 0) {
            return true;
        }

        return (flags & FLAG_ADVANCED) != 0 && !isAdvanced;
    }

    public List<Component> getComponents(HolderLookup.Provider provider, int indentLevel, boolean isAdvanced) {
        if (isEmpty(isAdvanced)) {
            return List.of();
        }

        List<Component> lines = new ArrayList<>();
        MutableComponent currentLine = indent(indentLevel);
        boolean hasContent = (key != null || component != null || (values != null && values.length > 0));
        boolean isBranching = (flags & FLAG_ARRAY) != 0 || !children.isEmpty() || (hasContent && indentLevel > 0);

        if (indentLevel > 0 && isBranching) {
            currentLine.append(Component.literal("-> ").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (key != null) {
            if ((flags & FLAG_RAW_KEY) != 0) {
                if (!key.isEmpty() && key.charAt(0) == TooltipBuilder.TRANSLATE_MARKER) {
                    currentLine.append(Component.translatable(key.substring(1)).withStyle(TEXT_STYLE));
                } else {
                    currentLine.append(Component.literal(key).withStyle(TEXT_STYLE));

                    if ((!children.isEmpty() || (flags & FLAG_ARRAY) != 0) && values == null) {
                        currentLine.append(Component.literal(":").withStyle(TEXT_STYLE));
                    }
                }

                if (values != null && values.length > 0) {
                    currentLine.append(Component.literal(": ").withStyle(TEXT_STYLE));

                    for (int i = 0; i < values.length; i++) {
                        String value = values[i];

                        if (i > 0) {
                            currentLine.append(Component.literal(" "));
                        }

                        if (value != null) {
                            currentLine.append(formatValue(provider, value));
                        }
                    }
                }

                if (component != null) {
                    currentLine.append(Component.literal(": ").withStyle(TEXT_STYLE));
                    currentLine.append(component).withStyle(VALUE_STYLE);
                }
            } else {
                if (values != null && values.length > 0) {
                    Object[] valArgs = new Object[values.length];

                    for (int i = 0; i < values.length; i++) {
                        String value = values[i];

                        if (value != null) {
                            valArgs[i] = formatValue(provider, value);
                        }
                    }

                    currentLine.append(Component.translatable(key, valArgs).withStyle(TEXT_STYLE));
                } else if (component != null) {
                    currentLine.append(Component.translatable(key, component.copy().withStyle(VALUE_STYLE)).withStyle(TEXT_STYLE));
                } else {
                    currentLine.append(Component.translatable(key).withStyle(TEXT_STYLE));
                }
            }
        } else if (values != null && values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                String value = values[i];

                if (i > 0) {
                    currentLine.append(Component.literal(" "));
                }

                if (value != null) {
                    currentLine.append(formatValue(provider, value));
                }
            }
        } else if (component != null) {
            currentLine.append(component.copy().withStyle(VALUE_STYLE));
        }

        String rawText = currentLine.getString().replace("->", "").trim();

        if (!rawText.isEmpty() || (flags & FLAG_ERROR) != 0) {
            lines.add(currentLine);
        }

        int childIndent = (key != null || values != null) ? indentLevel + 1 : indentLevel;

        for (TooltipNode child : children) {
            lines.addAll(child.getComponents(provider, childIndent, isAdvanced));
        }

        return lines;
    }

    @NotNull
    private MutableComponent formatValue(HolderLookup.Provider provider, String value) {
        MutableComponent comp;

        if (!value.isEmpty() && value.charAt(0) == TooltipBuilder.TRANSLATE_MARKER) {
            comp = Component.translatable(value.substring(1));
        } else {
            comp = Component.literal(value);
        }

        return (flags & FLAG_ERROR) != 0 ? comp.withStyle(ERROR_STYLE) : comp.withStyle(VALUE_STYLE);
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeShort(flags);

        if ((flags & FLAG_EMPTY) != 0) {
            return;
        }

        if ((flags & FLAG_HAS_KEY) != 0 && key != null) {
            buf.writeUtf(key);
        }

        if ((flags & FLAG_HAS_VALUE) != 0 && values != null) {
            if ((flags & FLAG_COMPONENT) != 0) {
                ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, component != null ? component : Component.empty());
            } else {
                buf.writeVarInt(values.length);

                for (String value : values) {
                    assert value != null;
                    buf.writeUtf(value);
                }
            }
        }

        buf.writeVarInt(children.size());

        for (TooltipNode child : children) {
            child.encode(buf);
        }
    }

    public static TooltipNode decode(RegistryFriendlyByteBuf buf) {
        short flags = buf.readShort();
        String key = null;
        String[] values = null;
        Component component = null;

        if ((flags & FLAG_EMPTY) != 0) {
            return EMPTY_INSTANCE;
        }

        if ((flags & FLAG_HAS_KEY) != 0) {
            key = buf.readUtf();
        }

        if ((flags & FLAG_HAS_VALUE) != 0) {
            if ((flags & FLAG_COMPONENT) != 0) {
                component = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
            } else {
                int valCount = buf.readVarInt();

                values = new String[valCount];

                for (int i = 0; i < valCount; i++) {
                    values[i] = buf.readUtf();
                }
            }
        }

        int childCount = buf.readVarInt();
        List<TooltipNode> children = new ArrayList<>(childCount);

        for (int i = 0; i < childCount; i++) {
            children.add(decode(buf));
        }

        return getOrCreate(key, values, component, flags, children);
    }

    @NotNull
    public static TooltipNode getOrCreate(@Nullable String key, String @Nullable[] values, @Nullable Component component, short flags, List<TooltipNode> children) {
        if ((flags & FLAG_EMPTY) != 0) {
            return getFromCache(CACHE, new CacheKey(null, null, null, FLAG_EMPTY, List.of()));
        }

        List<String> valList = null;

        if (values != null) {
            valList = new ArrayList<>(values.length);

            for (String value : values) {
                valList.add(value != null ? value.intern() : null);
            }
        }

        CacheKey cacheKey = new CacheKey(
                key != null ? key.intern() : null,
                valList,
                component,
                flags,
                List.copyOf(children)
        );
        return getFromCache(CACHE, cacheKey);
    }

    public static void logCacheStatistics() {
        CacheStats stats = CACHE.stats();

        LOGGER.info("Node Statistics:");
        LOGGER.info("Total Requests: {}", stats.requestCount());
        LOGGER.info("Hits (Reused):  {} ({})", stats.hitCount(), String.format("%.2f%%", stats.hitRate() * 100));
        LOGGER.info("Misses (New):   {} ({})", stats.missCount(), String.format("%.2f%%", stats.missRate() * 100));
        LOGGER.info("Load Penalty:   {}ms (avg)", stats.averageLoadPenalty() / 1_000_000.0);
        LOGGER.info("Evictions:      {}", stats.evictionCount());
    }

    @NotNull
    public static <K, V> V getFromCache(LoadingCache<K, V> cache, K key) {
        try {
            return cache.get(key);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to retrieve node from cache", e);
        }
    }

    record CacheKey(@Nullable String key, @Nullable List<String> values, @Nullable Component componentValue, short flags, List<TooltipNode> children) {}
}