package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReferenceNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("reference");

    private final TooltipNode tooltip;
    private final float chance;

    public ReferenceNode(List<IDataNode> children, float chance, TooltipNode tooltip) {
        children.forEach(this::addChildren);
        this.chance = chance;
        this.tooltip = tooltip;
    }

    public ReferenceNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = TooltipNode.decode(utils, buf);
        chance = buf.readFloat();
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        tooltip.encode(utils, buf);
        buf.writeFloat(chance);
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public float getChance() {
        return chance;
    }
}
