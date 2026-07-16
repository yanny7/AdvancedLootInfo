package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.language.Lang;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.yanny.aci.tooltip.TooltipBuilder.*;

public class BaseTerrainNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("base_layout");

    private final TooltipNode tooltip;

    public BaseTerrainNode(IServerUtils utils, Set<Block> baseBlocks) {
        for (Block block : baseBlocks) {
            addChildren(new BlockNode(utils, block));
        }

        tooltip = array((b) -> b.add(value(translate(Lang.GenerationStep.BASE_TERRAIN.singular())).build(Lang.Value.GENERATION_STEP))).build();
    }

    public BaseTerrainNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
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
}
