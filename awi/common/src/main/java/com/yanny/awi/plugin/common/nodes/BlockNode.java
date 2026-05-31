package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class BlockNode implements IDataNode {
    public static ResourceLocation ID = Utils.modLoc("block");

    private final Block block;
    private final TooltipNode tooltip;

    public BlockNode(IServerUtils utils, Block block) {
        this.block = block;
        tooltip = TooltipNode.empty();
    }

    public BlockNode(IClientUtils utils, FriendlyByteBuf buf) {
        block = BuiltInRegistries.BLOCK.get(buf.readResourceLocation());
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
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
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(block));
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
    }
}
