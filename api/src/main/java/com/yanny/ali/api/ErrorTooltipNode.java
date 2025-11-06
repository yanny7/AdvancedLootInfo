package com.yanny.ali.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ErrorTooltipNode extends TooltipNode implements IKeyTooltipNode {
    static {
        registerFactory(ErrorTooltipNode.class, () -> ErrorTooltipNode::new);
    }

    private final String value;

    private ErrorTooltipNode(String value) {
        this.value = value;
    }

    public ErrorTooltipNode(FriendlyByteBuf buf) {
        super(buf);
        value = buf.readUtf();
    }

    @Override
    public void encodeData(FriendlyByteBuf buf) {
        buf.writeUtf(value);
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
        if (isAdvancedTooltip() && !showAdvancedTooltip) {
            return Collections.emptyList();
        }

        return Collections.singletonList(pad(pad, Component.translatable("ali.util.advanced_loot_info.missing", value).withStyle(ChatFormatting.RED)));
    }

    @NotNull
    public static ErrorTooltipNode error(String value) {
        return new ErrorTooltipNode(value);
    }
}
