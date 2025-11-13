package com.yanny.ali.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ITooltipNode {
    ChatFormatting TEXT_STYLE = ChatFormatting.GOLD; //TODO use these or add config
    ChatFormatting PARAM_STYLE = ChatFormatting.AQUA;

    static void encodeNode(IServerUtils utils, ITooltipNode node, RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(node.getId());
        node.encode(utils, buf);
    }

    static ITooltipNode decodeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        ResourceLocation name = buf.readResourceLocation();
        IClientRegistry.TooltipFactory<?> factory = utils.getTooltipNodeFactory(name);

        if (factory != null) {
            return factory.create(utils, buf);
        } else {
            throw new IllegalStateException("No factory defined for tooltip node " + name);
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
    static MutableComponent convertObject(@Nullable Object object) {
        if (object instanceof MutableComponent component) {
            return component;
        } else if (object != null) {
            return Component.literal(object.toString());
        } else {
            return Component.literal("null");
        }
    }

    List<Component> getComponents(int pad, boolean showAdvancedTooltip);

    void encode(IServerUtils utils, RegistryFriendlyByteBuf buf);

    ResourceLocation getId();
}
