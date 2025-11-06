package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class LiteralTooltipNode extends TooltipNode {
    static {
        registerFactory(LiteralTooltipNode.class, () -> LiteralTooltipNode::new);
    }

    private final String text;
    private final boolean translatable;

    private LiteralTooltipNode(String text, boolean translatable) {
        this.text = text;
        this.translatable = translatable;
    }

    public LiteralTooltipNode(FriendlyByteBuf buf) {
        super(buf);
        text = buf.readUtf();
        translatable = buf.readBoolean();
    }

    @Override
    public void encodeData(FriendlyByteBuf buf) {
        buf.writeUtf(text);
        buf.writeBoolean(translatable);
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (isAdvancedTooltip() && !showAdvancedTooltip) {
            return Collections.emptyList();
        }

        if (translatable) {
            return Collections.singletonList(pad(pad, Component.translatable(text).withStyle(TEXT_STYLE)));
        } else {
            return Collections.singletonList(pad(pad, Component.literal(text).withStyle(TEXT_STYLE)));
        }
    }

    @NotNull
    public static LiteralTooltipNode translatable(String text) {
        return new LiteralTooltipNode(text, true);
    }
}
