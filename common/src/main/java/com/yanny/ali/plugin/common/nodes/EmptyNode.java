package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class EmptyNode implements IDataNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Utils.MOD_ID, "empty");

    private final ITooltipNode tooltip;
    private final float chance;

    public EmptyNode(float chance, ITooltipNode tooltip) {
        this.chance = chance;
        this.tooltip = tooltip;
    }

    public EmptyNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        tooltip = ITooltipNode.decodeNode(utils, buf);
        chance = buf.readFloat();
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
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
