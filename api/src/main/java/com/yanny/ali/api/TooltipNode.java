package com.yanny.ali.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class TooltipNode implements ITooltipNode {
    protected static final ChatFormatting TEXT_STYLE = ChatFormatting.GOLD; //TODO use these or add config
    protected static final ChatFormatting PARAM_STYLE = ChatFormatting.AQUA;

    private static final Map<Class<?>, Supplier<Function<FriendlyByteBuf, ITooltipNode>>> NODE_FACTORIES = new HashMap<>();

    private boolean advancedTooltip = false;

    public TooltipNode() {

    }

    public TooltipNode(FriendlyByteBuf buf) {
        advancedTooltip = buf.readBoolean();
    }

    public abstract void encodeData(FriendlyByteBuf buf);

    public TooltipNode setIsAdvancedTooltip() {
        advancedTooltip = true;
        return this;
    }

    @Override
    public boolean isAdvancedTooltip() {
        return advancedTooltip;
    }

    @Override
    public final void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(advancedTooltip);
        encodeData(buf);
    }

    protected final void decode(FriendlyByteBuf buf) {
        advancedTooltip = buf.readBoolean();
    }

    protected static <T extends TooltipNode> void registerFactory(Class<T> clazz, Supplier<Function<FriendlyByteBuf, T>> factory) {
        NODE_FACTORIES.put(clazz, () -> factory.get()::apply);
    }

    public static void encodeNode(ITooltipNode node, FriendlyByteBuf buf) {
        buf.writeUtf(node.getClass().getName());
        node.encode(buf);
    }

    public static ITooltipNode decodeNode(FriendlyByteBuf buf) {
        String name = buf.readUtf();

        try {
            Class<?> clazz = Class.forName(name);
            Function<FriendlyByteBuf, ITooltipNode> factory = NODE_FACTORIES.get(clazz).get();

            if (factory != null) {
                return factory.apply(buf);
            } else {
                throw new IllegalStateException("No factory defined for tooltip node " + clazz.getName());
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Invalid class name " + name, e);
        }
    }

    @NotNull
    static Component pair(Object value1, Object value2) {
        return Component.translatable("ali.util.advanced_loot_info.two_values_with_space", convertObject(value1), convertObject(value2));
    }

    @NotNull
    static Component pad(int count, Object arg) {
        if (count > 0) {
            return pair(Component.translatable("ali.util.advanced_loot_info.pad." + count), convertObject(arg));
        } else {
            return convertObject(arg);
        }
    }

    @NotNull
    private static MutableComponent convertObject(@Nullable Object object) {
        if (object instanceof MutableComponent component) {
            return component;
        } else if (object != null) {
            return Component.literal(object.toString());
        } else {
            return Component.literal("null");
        }
    }
}
