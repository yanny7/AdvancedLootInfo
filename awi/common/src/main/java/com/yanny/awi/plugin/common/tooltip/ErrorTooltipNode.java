package com.yanny.awi.plugin.common.tooltip;

import com.yanny.aci.tooltip.CoreErrorTooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IKeyTooltipNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.yanny.aci.api.ICoreTooltipNode.pad;

public class ErrorTooltipNode extends CoreErrorTooltipNode<IServerUtils, ITooltipNode, IKeyTooltipNode> implements ITooltipNode, IKeyTooltipNode {
    public static final ResourceLocation ID = Utils.modLoc("error");

    private ErrorTooltipNode(String value) {
        super(value);
    }

    @NotNull
    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        return Collections.singletonList(pad(pad, Component.translatable("aci.util.missing", getValue()).withStyle(ChatFormatting.RED)));
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static ErrorTooltipNode.Builder error(String value) {
        return new Builder(value);
    }

    @NotNull
    public static ErrorTooltipNode decode(IClientUtils utils, FriendlyByteBuf buf) {
        return decode(utils, buf, ErrorTooltipNode::new);
    }

    public static class Builder extends CoreErrorTooltipNode.Builder<IServerUtils, ITooltipNode, IKeyTooltipNode> implements IKeyTooltipNode {
        public Builder(String value) {
            super(value, ErrorTooltipNode::new);
        }
    }
}
