package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class BlockNode implements IDataNode {
    public static ResourceLocation ID = Utils.modLoc("block");

    public BlockNode(IServerUtils utils, Block block) {

    }

    @Override
    public @NotNull TooltipNode getTooltip() {
        return null;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {

    }
}
