package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class DynamicNode implements IDataNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "dynamic");

    private final ITooltipNode tooltip;
    private final float chance;

    public DynamicNode(float chance, ITooltipNode tooltip) {
        this.chance = chance;
        this.tooltip = tooltip;
    }

    public DynamicNode(IClientUtils utils, FriendlyByteBuf buf) {
        tooltip = ITooltipNode.decodeNode(utils, buf);
        chance = buf.readFloat();
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
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
