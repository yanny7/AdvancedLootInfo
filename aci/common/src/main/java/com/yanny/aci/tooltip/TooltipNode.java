package com.yanny.aci.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import com.yanny.aci.api.ICoreClientUtils;
import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.ChatFormatting;
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
    public static final short FLAG_INDEX_KEY = 1 << 8;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LoadingCache<CacheKey, TooltipNode> CACHE = CacheBuilder.newBuilder()
            .recordStats()
            .build(CacheLoader.from(TooltipNode::new));

    public static final TooltipNode EMPTY_INSTANCE = getOrCreate(null, null, null, FLAG_EMPTY, List.of());

    private @Nullable final String key;
    private final String @Nullable[] values;
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

    @Nullable
    public Component getComponent() {
        return component;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean hasKey() {
        return (flags & FLAG_HAS_KEY) != 0;
    }

    public boolean isEmpty(boolean isAdvanced) {
        if (is(FLAG_EMPTY)) {
            return true;
        }

        return is(FLAG_ADVANCED) && !isAdvanced;
    }

    public List<Component> getComponents(int indentLevel, boolean isAdvanced) {
        if (isEmpty(isAdvanced)) {
            return List.of();
        }

        List<Component> lines = new ArrayList<>();
        MutableComponent currentLine = indent(indentLevel);
        boolean hasContent = is(FLAG_HAS_KEY) || is(FLAG_HAS_VALUE) || is(FLAG_COMPONENT);
        boolean isBranching = is(FLAG_ARRAY) || !children.isEmpty() || (hasContent && indentLevel > 0);

        if (indentLevel > 0 && isBranching) {
            currentLine.append(Component.literal("-> ").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (is(FLAG_HAS_KEY)) {
            assert key != null;

            if (is(FLAG_RAW_KEY)) {
                appendRawKey(currentLine);
                appendValuesAndComponentWithColon(currentLine);
            } else {
                appendTranslatableKeyWithValuesAndComponent(currentLine);
            }
        } else {
            appendValuesAncComponentDirectly(currentLine);
        }

        String rawText = currentLine.getString().replace("->", "").trim();

        if (!rawText.isEmpty() || is(FLAG_ERROR)) {
            lines.add(currentLine);
        }

        int childIndent = (is(FLAG_HAS_KEY) || is(FLAG_HAS_VALUE) || is(FLAG_COMPONENT)) ? indentLevel + 1 : indentLevel;

        for (TooltipNode child : children) {
            lines.addAll(child.getComponents(childIndent, isAdvanced));
        }

        return lines;
    }

    private boolean is(short flag) {
        return (flags & flag) != 0;
    }

    private void appendRawKey(MutableComponent line) {
        assert key != null;

        if (!key.isEmpty() && key.charAt(0) == TooltipBuilder.TRANSLATE_MARKER) {
            line.append(Component.translatable(key.substring(1)).withStyle(TEXT_STYLE));
        } else {
            line.append(Component.literal(key).withStyle(TEXT_STYLE));

            if ((!children.isEmpty() && !is(FLAG_HAS_VALUE)) || is(FLAG_ARRAY)) {
                line.append(Component.literal(":").withStyle(TEXT_STYLE));
            }
        }
    }

    private void appendTranslatableKeyWithValuesAndComponent(MutableComponent line) {
        assert key != null;

        if (is(FLAG_HAS_VALUE)) {
            assert values != null;
            Object[] valArgs = new Object[values.length];

            for (int i = 0; i < values.length; i++) {
                valArgs[i] = formatValue(values[i]);
            }

            line.append(Component.translatable(key, valArgs).withStyle(TEXT_STYLE));
        } else if (is(FLAG_COMPONENT)) {
            assert component != null;
            line.append(Component.translatable(key, component.copy().withStyle(VALUE_STYLE)).withStyle(TEXT_STYLE));
        } else {
            line.append(Component.translatable(key).withStyle(TEXT_STYLE));
        }
    }

    private void appendValuesAndComponentWithColon(MutableComponent line) {
        if (is(FLAG_HAS_VALUE)) {
            assert values != null;
            line.append(Component.literal(": ").withStyle(TEXT_STYLE));

            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    line.append(Component.literal(" "));
                }

                line.append(formatValue(values[i]));
            }
        }

        if (is(FLAG_COMPONENT)) {
            assert component != null;
            line.append(Component.literal(": ").withStyle(TEXT_STYLE));
            line.append(component).withStyle(VALUE_STYLE);
        }
    }

    private void appendValuesAncComponentDirectly(MutableComponent line) {
        if (is(FLAG_HAS_VALUE)) {
            assert values != null;
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    line.append(Component.literal(" "));
                }

                line.append(formatValue(values[i]));
            }
        } else if (is(FLAG_COMPONENT)) {
            assert component != null;
            line.append(component.copy().withStyle(VALUE_STYLE));
        }
    }

    @NotNull
    private MutableComponent indent(int level) {
        return Component.literal("  ".repeat(level));
    }

    @NotNull
    private MutableComponent formatValue(String value) {
        MutableComponent comp;

        if (!value.isEmpty() && value.charAt(0) == TooltipBuilder.TRANSLATE_MARKER) {
            comp = Component.translatable(value.substring(1));
        } else {
            comp = Component.literal(value);
        }

        return is(FLAG_ERROR) ? comp.withStyle(ERROR_STYLE) : comp.withStyle(VALUE_STYLE);
    }

    public void encode(ICoreServerUtils<?> utils, RegistryFriendlyByteBuf buf) {
        int keyIndex = -1;
        short flags = this.flags;

        if (key != null) {
            keyIndex = utils.getTranslationKeyIndex(key);

            if (keyIndex != -1) {
                flags |= FLAG_INDEX_KEY;
            }
        }

        buf.writeShort(flags);

        if (is(FLAG_EMPTY)) {
            return;
        }

        if (is(FLAG_HAS_KEY)) {
            if (keyIndex >= 0) {
                buf.writeVarInt(keyIndex);
            } else {
                assert key != null;
                buf.writeUtf(key);
            }
        }

        if (is(FLAG_COMPONENT)) {
            assert component != null;
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, component != null ? component : Component.empty());
        }

        if (is(FLAG_HAS_VALUE)) {
            assert values != null;
            buf.writeVarInt(values.length);

            for (String value : values) {
                buf.writeUtf(value);
            }
        }

        buf.writeVarInt(children.size());

        for (TooltipNode child : children) {
            child.encode(utils, buf);
        }
    }

    public static TooltipNode decode(ICoreClientUtils<?, ?, ?> utils, RegistryFriendlyByteBuf buf) {
        short flags = buf.readShort();
        String key = null;
        String[] values = null;
        Component component = null;

        if ((flags & FLAG_EMPTY) != 0) {
            return EMPTY_INSTANCE;
        }

        if ((flags & FLAG_HAS_KEY) != 0) {
            if ((flags & FLAG_INDEX_KEY) != 0) {
                key = utils.getTranslationKey(buf.readVarInt());

                if (key == null) {
                    LOGGER.warn("Unable to decode indexed key! Version mismatch!");
                    key = "aci.util.missing";
                }
            } else {
                key = buf.readUtf();
            }
        }

        if ((flags & FLAG_COMPONENT) != 0) {
            component = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
        }

        if ((flags & FLAG_HAS_VALUE) != 0) {
            int valCount = buf.readVarInt();

            values = new String[valCount];

            for (int i = 0; i < valCount; i++) {
                values[i] = buf.readUtf();
            }
        }

        int childCount = buf.readVarInt();
        List<TooltipNode> children = new ArrayList<>(childCount);

        for (int i = 0; i < childCount; i++) {
            children.add(decode(utils, buf));
        }

        return getOrCreate(key, values, component, flags, children);
    }

    @NotNull
    public static TooltipNode getOrCreate(@Nullable String key, String @Nullable[] values, @Nullable Component component, short flags, List<TooltipNode> children) {
        if ((flags & FLAG_EMPTY) != 0) {
            return getFromCache(CACHE, new CacheKey(null, null, null, FLAG_EMPTY, List.of()));
        }

        List<String> valList = null;

        if ((flags & FLAG_HAS_VALUE) != 0) {
            assert values != null;
            valList = new ArrayList<>(values.length);

            for (String value : values) {
                valList.add(value.intern());
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