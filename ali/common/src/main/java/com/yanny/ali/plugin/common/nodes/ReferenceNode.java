package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ReferenceNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "reference");

    private final ITooltipNode tooltip;
    private final float chance;

    public ReferenceNode(List<IDataNode> children, float chance, ITooltipNode tooltip) {
        children.forEach(this::addChildren);
        this.chance = chance;
        this.tooltip = tooltip;
    }

    public ReferenceNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = ITooltipNode.decodeNode(utils, buf);
        chance = buf.readFloat();
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        ITooltipNode.encodeNode(utils, tooltip, buf);
        buf.writeFloat(chance);
    }

    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public float getChance() {
        return chance;
    }
}
