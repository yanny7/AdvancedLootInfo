package com.yanny.ali.plugin.common.tooltip;

import com.yanny.aci.tooltip.CoreErrorTooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.yanny.aci.api.ICoreTooltipNode.pad;

public class ErrorTooltipNode extends CoreErrorTooltipNode<IServerUtils, ITooltipNode, IKeyTooltipNode> implements ITooltipNode, IKeyTooltipNode {
    public static final Identifier ID = Utils.modLoc("error");

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
    public Identifier getId() {
        return ID;
    }

    @NotNull
    public static ErrorTooltipNode.Builder error(String value) {
        return new ErrorTooltipNode.Builder(value);
    }

    @NotNull
    public static ErrorTooltipNode decode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        return decode(utils, buf, ErrorTooltipNode::new);
    }

    public static class Builder extends CoreErrorTooltipNode.Builder<IServerUtils, ITooltipNode, IKeyTooltipNode> implements IKeyTooltipNode {
        public Builder(String value) {
            super(value, ErrorTooltipNode::new);
        }
    }
}
