package com.yanny.aci.tooltip;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class TooltipBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();

    static final char TRANSLATE_MARKER = '\uE000';

    private MultiKey translatableKey;
    private String rawKey;
    private String[] values;
    private Component componentValue;

    private boolean isArray = false;
    private boolean isAdvancedOnly = false;
    private boolean isError = false;
    private boolean isEmptyForced = false;
    private boolean isComponent = false;
    private boolean forceVisible = false;

    private final List<TooltipNode> children = new ArrayList<>();

    @NotNull
    public static MultiKey multi(String s, String p) {
        return new MultiKey(s, p);
    }

    @NotNull
    public static String translate(String key) {
        return TRANSLATE_MARKER + key;
    }

    @NotNull
    public static TooltipBuilder empty() {
        TooltipBuilder builder = new TooltipBuilder();

        builder.isEmptyForced = true;
        return builder;
    }

    @NotNull
    public static TooltipBuilder array(Consumer<TooltipBuilder> logic) {
        TooltipBuilder builder = new TooltipBuilder();

        builder.isArray = true;
        logic.accept(builder);
        return builder;
    }

    @NotNull
    public static TooltipBuilder branch(Consumer<TooltipBuilder> logic) {
        TooltipBuilder builder = new TooltipBuilder();

        logic.accept(builder);
        return builder;
    }

    @NotNull
    public static TooltipBuilder value(Object... v) {
        TooltipBuilder builder = new TooltipBuilder();

        builder.values = new String[v.length];

        for (int i = 0; i < v.length; i++) {
            builder.values[i] = String.valueOf(v[i]);
        }

        return builder;
    }

    @NotNull
    public static TooltipBuilder keyOnly(String key) {
        TooltipBuilder builder = new TooltipBuilder();

        builder.key(key);
        builder.forceVisible = true;
        return builder;
    }

    @NotNull
    public static TooltipBuilder keyValue(String k, Object... v) {
        TooltipBuilder builder = new TooltipBuilder();

        builder.rawKey = k;
        builder.values = new String[v.length];

        for (int i = 0; i < v.length; i++) {
            builder.values[i] = String.valueOf(v[i]);
        }

        return builder;
    }

    @NotNull
    public static TooltipBuilder error(String msg) {
        TooltipBuilder builder = new TooltipBuilder();

        builder.isError = true;
        builder.values = new String[]{msg};
        return builder;
    }

    @NotNull
    public static TooltipBuilder component(Component component) {
        TooltipBuilder builder = new TooltipBuilder();

        builder.isComponent = true;
        builder.componentValue = component;
        return builder;
    }

    private TooltipBuilder() {}

    public TooltipBuilder isAdvancedTooltip() {
        this.isAdvancedOnly = true;
        return this;
    }

    public TooltipBuilder add(TooltipBuilder builder) {
        TooltipNode node = builder.build();

        if (node != TooltipNode.EMPTY_INSTANCE) {
            this.children.add(node);
        }

        return this;
    }

    public TooltipBuilder add(TooltipNode node) {
        if (node != TooltipNode.EMPTY_INSTANCE) {
            this.children.add(node);
        }

        return this;
    }

    public TooltipNode build(String k) {
        return this.key(k).build();
    }

    public TooltipNode build(MultiKey mk) {
        return this.key(mk).build();
    }

    public TooltipBuilder key(String k) {
        this.translatableKey = new MultiKey(k, k);
        return this;
    }

    public TooltipBuilder key(MultiKey mk) {
        this.translatableKey = mk;
        return this;
    }

    public TooltipBuilder rawKey(String k) {
        this.rawKey = k;
        return this;
    }

    public TooltipBuilder showEmpty() {
        this.forceVisible = true;
        return this;
    }

    public boolean hasKey() {
        return rawKey != null || translatableKey != null;
    }

    public TooltipNode build() {
        if (isEmptyForced) {
            return TooltipNode.EMPTY_INSTANCE;
        }

        if (!forceVisible && values == null && componentValue == null && children.isEmpty()) {
            return TooltipNode.EMPTY_INSTANCE;
        }

        String finalKeyStr = rawKey;
        String[] finalValues = values;
        Component finalComponent = componentValue;
        short finalFlags = getFlags();
        List<TooltipNode> finalChildren = new ArrayList<>(children);

        if (translatableKey != null && rawKey == null) {
            boolean isSingleSimpleChild = !finalChildren.isEmpty() && !finalChildren.get(0).hasChildren() && !finalChildren.get(0).hasKey();
            boolean parentIsEligible = finalValues == null && finalComponent == null && !isError;
            boolean potentiallyMergeable = parentIsEligible && isSingleSimpleChild;
            boolean canBeMerged = potentiallyMergeable && !isArray && finalChildren.size() == 1;
            boolean hasMultiKey = !Objects.equals(translatableKey.plural, translatableKey.single);

            if (canBeMerged && hasMultiKey) {
                TooltipNode child = finalChildren.get(0);
                finalKeyStr = translatableKey.single();
                finalValues = child.getValues();
                finalComponent = child.getComponent();
                finalChildren.clear();
            } else {
                if (potentiallyMergeable) {
                    if (!hasMultiKey) {
                        LOGGER.info("Tooltip node {} could be merged if defined singular form", translatableKey.plural);
                    }

                    if (isArray) {
                        LOGGER.info("Tooltip node {} could be merged if it wasn't forced to be an array", translatableKey.plural);
                    }
                }

                finalKeyStr = translatableKey.plural();
            }
        }

        return createNode(finalKeyStr, finalValues, finalComponent, finalFlags, finalChildren);
    }

    private short getFlags() {
        short flags = 0;

        if (isArray) flags |= TooltipNode.FLAG_ARRAY;
        if (isAdvancedOnly) flags |= TooltipNode.FLAG_ADVANCED;
        if (isError) flags |= TooltipNode.FLAG_ERROR;
        if (rawKey != null) flags |= TooltipNode.FLAG_RAW_KEY;
        if (translatableKey != null || rawKey != null) flags |= TooltipNode.FLAG_HAS_KEY;
        if ((values != null && values.length > 0) || componentValue != null) flags |= TooltipNode.FLAG_HAS_VALUE;
        if (isComponent) flags |= TooltipNode.FLAG_COMPONENT;

        return flags;
    }

    @NotNull
    private TooltipNode createNode(@Nullable String key, @Nullable String @Nullable[] values, @Nullable Component component, short flags, List<TooltipNode> children) {
        return TooltipNode.getOrCreate(key, values, component, flags, children);
    }

    public record MultiKey(String single, String plural) {}
}