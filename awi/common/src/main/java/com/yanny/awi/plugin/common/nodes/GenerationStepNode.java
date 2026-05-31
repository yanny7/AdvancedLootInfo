package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;

public class GenerationStepNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("generation_step");

    private final TooltipNode tooltip;

    public GenerationStepNode(IServerUtils utils, GenerationStep.Decoration step, HolderSet<PlacedFeature> features) {
        for (Holder<PlacedFeature> placedFeatureHolder : features) {
            addChildren(new PlacedFeatureNode(utils, placedFeatureHolder.value()));

            //todo step
        }
        tooltip = TooltipNode.empty();
    }

    public GenerationStepNode(IClientUtils utils, FriendlyByteBuf buf) {
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
