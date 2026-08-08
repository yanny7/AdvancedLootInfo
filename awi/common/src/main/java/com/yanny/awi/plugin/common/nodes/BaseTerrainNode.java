package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.language.Lang;
import com.yanny.awi.plugin.server.TooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yanny.aci.tooltip.TooltipBuilder.*;

public class BaseTerrainNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("base_layout");

    private final TooltipNode tooltip;

    public BaseTerrainNode(IServerUtils utils, Set<NodeUtils.BlockInfo> baseBlocks, Block defaultBlock, Fluid defaultFluid) {
        Set<Block> detectedBlocks = baseBlocks.stream().map(NodeUtils.BlockInfo::block).collect(Collectors.toSet());

        // The surface rule never places the generator's default block/fluid, so the scan cannot observe them — add them
        // explicitly, otherwise the bulk of the terrain would be missing from the list.
        if (!defaultBlock.defaultBlockState().isAir() && !detectedBlocks.contains(defaultBlock)) {
            addChildren(new BlockNode(utils, defaultBlock, keyOnly(Lang.BaseTerrain.DEFAULT_BLOCK).build()));
        }

        if (!defaultFluid.isSame(Fluids.EMPTY)) {
            Block fluidBlock = defaultFluid.defaultFluidState().createLegacyBlock().getBlock();

            if (!detectedBlocks.contains(fluidBlock)) {
                addChildren(new BlockNode(utils, fluidBlock, keyOnly(Lang.BaseTerrain.DEFAULT_FLUID).build()));
            }
        }

        baseBlocks.stream()
                .sorted(Comparator.comparing((info) -> BuiltInRegistries.BLOCK.getKey(info.block()).getPath()))
                .forEach((info) -> addChildren(new BlockNode(utils, info.block(), TooltipUtils.getBlockInfoTooltip(utils, info).build())));

        tooltip = array((b) -> b.add(value(translate(Lang.GenerationStep.BASE_TERRAIN.singular())).build(Lang.Value.GENERATION_STEP))).build();
    }

    public BaseTerrainNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
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
