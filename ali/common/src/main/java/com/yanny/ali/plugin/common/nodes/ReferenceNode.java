package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReferenceNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("reference");

    private final ITooltipNode tooltip;
    private final float chance;

    public ReferenceNode(List<IDataNode> children, float chance, ITooltipNode tooltip) {
        children.forEach(this::addChildren);
        this.chance = chance;
        this.tooltip = tooltip;
    }

    public ReferenceNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = ITooltipNode.decodeNode(utils, buf);
        chance = buf.readFloat();
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        ITooltipNode.encodeNode(utils, tooltip, buf);
        buf.writeFloat(chance);
    }

    @NotNull
    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public float getChance() {
        return chance;
    }
}
