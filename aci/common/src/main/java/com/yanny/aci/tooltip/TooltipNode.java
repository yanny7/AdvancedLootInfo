package com.yanny.aci.tooltip;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.ICoreClientUtils;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.language.CoreLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TooltipNode {
    /** @deprecated use {@link TooltipStyle#text()} of the style the tooltip is rendered with */
    @Deprecated(forRemoval = true, since = "1.1.0")
    public static final ChatFormatting TEXT_STYLE = ChatFormatting.GOLD;
    /** @deprecated use {@link TooltipStyle#value()} of the style the tooltip is rendered with */
    @Deprecated(forRemoval = true, since = "1.1.0")
    public static final ChatFormatting VALUE_STYLE = ChatFormatting.AQUA;
    /** @deprecated use {@link TooltipStyle#error()} of the style the tooltip is rendered with */
    @Deprecated(forRemoval = true, since = "1.1.0")
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

    private @Nullable final String key;
    private final String @Nullable[] values;
    private @Nullable final Component component;
    private final short flags;
    private final List<TooltipNode> children;

    TooltipNode(CacheKey cacheKey) {
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

    /**
     * Number of lines this node contributes to its parent's indent level. Nodes without a key/value/component are
     * transparent when rendered - they emit no line of their own and splice their children into the parent instead
     * (see {@link #getComponents}), so a single node can span multiple lines.
     */
    public int lineSpan() {
        if (is(FLAG_EMPTY)) {
            return 0;
        }

        if (hasKey() || is(FLAG_HAS_VALUE) || is(FLAG_COMPONENT)) {
            return 1;
        }

        int span = 0;

        for (TooltipNode child : children) {
            span += child.lineSpan();
        }

        return span;
    }

    public boolean isEmpty(boolean isAdvanced) {
        if (is(FLAG_EMPTY)) {
            return true;
        }

        return is(FLAG_ADVANCED) && !isAdvanced;
    }

    /**
     * Whether this node renders no line at all at the given advanced-tooltip level, its whole subtree included. A node
     * that only carries a key (a header) is blank once every child below it got filtered out.
     */
    public boolean isBlank(boolean isAdvanced) {
        if (isEmpty(isAdvanced)) {
            return true;
        }

        for (TooltipNode child : children) {
            if (!child.isBlank(isAdvanced)) {
                return false;
            }
        }

        boolean hasOwnValue = is(FLAG_HAS_VALUE) || is(FLAG_COMPONENT) || is(FLAG_ERROR);

        if (hasOwnValue) {
            return false;
        }

        return !is(FLAG_HAS_KEY) || !children.isEmpty();
    }

    /** @deprecated use {@link #getComponents(int, boolean, TooltipStyle)} */
    @Deprecated(forRemoval = true, since = "1.1.0")
    public List<Component> getComponents(int indentLevel, boolean isAdvanced) {
        return getComponents(indentLevel, isAdvanced, TooltipStyle.DEFAULT);
    }

    public List<Component> getComponents(int indentLevel, boolean isAdvanced, TooltipStyle style) {
        if (isBlank(isAdvanced)) {
            return List.of();
        }

        List<Component> lines = new ArrayList<>();
        MutableComponent currentLine = indent(indentLevel);
        boolean hasContent = is(FLAG_HAS_KEY) || is(FLAG_HAS_VALUE) || is(FLAG_COMPONENT);
        boolean isBranching = is(FLAG_ARRAY) || !children.isEmpty() || (hasContent && indentLevel > 0);

        if (indentLevel > 0 && isBranching) {
            currentLine.append(Component.literal("-> ").withStyle(style.branch()));
        }

        if (is(FLAG_HAS_KEY)) {
            assert key != null;

            if (is(FLAG_RAW_KEY)) {
                appendRawKey(currentLine, style);
                appendValuesAndComponentWithColon(currentLine, style);
            } else {
                appendTranslatableKeyWithValuesAndComponent(currentLine, style);
            }
        } else {
            appendValuesAncComponentDirectly(currentLine, style);
        }

        String rawText = currentLine.getString().replace("->", "").trim();

        if (!rawText.isEmpty() || is(FLAG_ERROR)) {
            lines.add(currentLine);
        }

        int childIndent = (is(FLAG_HAS_KEY) || is(FLAG_HAS_VALUE) || is(FLAG_COMPONENT)) ? indentLevel + 1 : indentLevel;

        for (TooltipNode child : children) {
            lines.addAll(child.getComponents(childIndent, isAdvanced, style));
        }

        return lines;
    }

    private boolean is(short flag) {
        return (flags & flag) != 0;
    }

    private void appendRawKey(MutableComponent line, TooltipStyle style) {
        assert key != null;

        if (!key.isEmpty() && key.charAt(0) == TooltipBuilder.TRANSLATE_MARKER) {
            line.append(Component.translatable(key.substring(1)).withStyle(style.text()));
        } else {
            line.append(Component.literal(key).withStyle(style.text()));

            if ((!children.isEmpty() && !is(FLAG_HAS_VALUE)) || is(FLAG_ARRAY)) {
                line.append(Component.literal(":").withStyle(style.text()));
            }
        }
    }

    private void appendTranslatableKeyWithValuesAndComponent(MutableComponent line, TooltipStyle style) {
        assert key != null;

        if (is(FLAG_HAS_VALUE)) {
            assert values != null;
            Object[] valArgs = new Object[values.length];

            for (int i = 0; i < values.length; i++) {
                valArgs[i] = formatValue(values[i], style);
            }

            line.append(Component.translatable(key, valArgs).withStyle(style.text()));
        } else if (is(FLAG_COMPONENT)) {
            assert component != null;
            line.append(Component.translatable(key, component.copy().withStyle(style.value())).withStyle(style.text()));
        } else {
            line.append(Component.translatable(key).withStyle(style.text()));
        }
    }

    private void appendValuesAndComponentWithColon(MutableComponent line, TooltipStyle style) {
        if (is(FLAG_HAS_VALUE)) {
            assert values != null;
            line.append(Component.literal(": ").withStyle(style.text()));

            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    line.append(Component.literal(" "));
                }

                line.append(formatValue(values[i], style));
            }
        }

        if (is(FLAG_COMPONENT)) {
            assert component != null;
            line.append(Component.literal(": ").withStyle(style.text()));
            line.append(component.copy().withStyle(style.value()));
        }
    }

    private void appendValuesAncComponentDirectly(MutableComponent line, TooltipStyle style) {
        if (is(FLAG_HAS_VALUE)) {
            assert values != null;
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    line.append(Component.literal(" "));
                }

                line.append(formatValue(values[i], style));
            }
        } else if (is(FLAG_COMPONENT)) {
            assert component != null;
            line.append(component.copy().withStyle(style.value()));
        }
    }

    @NotNull
    private MutableComponent indent(int level) {
        return Component.literal("  ".repeat(level));
    }

    @NotNull
    private MutableComponent formatValue(String value, TooltipStyle style) {
        MutableComponent comp;

        if (!value.isEmpty() && value.charAt(0) == TooltipBuilder.TRANSLATE_MARKER) {
            comp = Component.translatable(value.substring(1));
        } else {
            comp = Component.literal(value);
        }

        return is(FLAG_ERROR) ? comp.withStyle(style.error()) : comp.withStyle(style.value());
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
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, component);
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
            buf.writeVarInt(utils.getTooltipCache().getNodeId(child));
        }
    }

    @NotNull
    public static RawTooltipNode decodeRaw(ICoreClientUtils<?, ?, ?> utils, RegistryFriendlyByteBuf buf) {
        short flags = buf.readShort();
        String key = null;
        String[] values = null;
        Component component = null;

        if ((flags & FLAG_EMPTY) != 0) {
            return new RawTooltipNode(null, null, null, flags, List.of());
        }

        if ((flags & FLAG_HAS_KEY) != 0) {
            if ((flags & FLAG_INDEX_KEY) != 0) {
                key = utils.getTranslationKey(buf.readVarInt());

                if (key == null) {
                    CommonLogUtils.getLogger(utils.getModId()).warn("Unable to decode indexed key! Version mismatch!");
                    key = CoreLang.Utils.NOT_IMPLEMENTED.singular();
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
        List<Integer> children = new ArrayList<>(childCount);

        for (int i = 0; i < childCount; i++) {
            children.add(buf.readVarInt());
        }

        return new RawTooltipNode(key, values, component, flags, children);
    }

    @NotNull
    public static TooltipNode empty() {
        return getOrCreate(TooltipContext.getPalette(), null, null, null, FLAG_EMPTY, Collections.emptyList());
    }

    @NotNull
    public static TooltipNode getOrCreate(TooltipNodePalette cache, @Nullable String key, String @Nullable[] values, @Nullable Component component, short flags, List<TooltipNode> children) {
        if ((flags & FLAG_EMPTY) != 0) {
            cache.getOrCreate(new CacheKey(null, null, null, FLAG_EMPTY, Collections.emptyList()));
        }

        List<String> valList = null;

        if ((flags & FLAG_HAS_VALUE) != 0) {
            assert values != null;
            valList = new ArrayList<>(values.length);

            for (String value : values) {
                valList.add(value.intern());
            }
        }

        return cache.getOrCreate(new CacheKey(key != null ? key.intern() : null, valList, component, flags, List.copyOf(children)));
    }
}