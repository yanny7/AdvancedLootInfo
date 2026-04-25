package com.yanny.aci.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICoreTooltipNode<SU extends ICoreServerUtils> {
    ChatFormatting TEXT_STYLE = ChatFormatting.GOLD; //TODO use these or add config
    ChatFormatting PARAM_STYLE = ChatFormatting.AQUA;

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

    @NotNull
    List<Component> getComponents(int pad, boolean showAdvancedTooltip);

    void encode(SU utils, FriendlyByteBuf buf);

    @NotNull
    ResourceLocation getId();
}
