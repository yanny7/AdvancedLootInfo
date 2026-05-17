package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class SlotNode implements IDataNode {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Utils.MOD_ID, "slot");

    private final TooltipNode tooltip;
    private final float chance;

    public SlotNode(float chance, TooltipNode tooltip) {
        this.chance = chance;
        this.tooltip = tooltip;
    }

    public SlotNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        tooltip = TooltipNode.decode(utils, buf);
        chance = buf.readFloat();
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
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
    public Identifier getId() {
        return ID;
    }

    @Override
    public float getChance() {
        return chance;
    }
}
