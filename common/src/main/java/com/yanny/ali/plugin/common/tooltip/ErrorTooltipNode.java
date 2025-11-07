package com.yanny.ali.plugin.common.tooltip;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ErrorTooltipNode implements IKeyTooltipNode, ITooltipNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "error");

    private final String value;

    private ErrorTooltipNode(String value) {
        this.value = value;
    }

    @Override
    public IKeyTooltipNode key(String key) {
        return this;
    }

    @Override
    public IKeyTooltipNode add(ITooltipNode node) {
        return this;
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        return Collections.singletonList(ITooltipNode.pad(pad, Component.translatable("ali.util.advanced_loot_info.missing", value).withStyle(ChatFormatting.RED)));
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeUtf(value);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static ErrorTooltipNode error(String value) {
        return new ErrorTooltipNode(value);
    }

    @NotNull
    public static ErrorTooltipNode decode(IClientUtils ignoredUtils, FriendlyByteBuf buf) {
        String value = buf.readUtf();
        return new ErrorTooltipNode(value);
    }
}
