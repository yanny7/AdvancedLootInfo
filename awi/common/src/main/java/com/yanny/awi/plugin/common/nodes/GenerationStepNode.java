package com.yanny.awi.plugin.common.nodes;

import com.yanny.awi.plugin.EnumTypes;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.language.Lang;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.yanny.aci.tooltip.TooltipBuilder.*;

public class GenerationStepNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("generation_step");

    private final TooltipNode tooltip;
    private final int generationStep;

    GenerationStepNode(GenerationStep.Decoration step, List<PlacedFeatureNode> features) {
        for (PlacedFeatureNode feature : features) {
            addChildren(feature);
        }

        tooltip = array((b) -> b.add(value(translate(EnumTypes.key(step))).build(Lang.Value.GENERATION_STEP))).build();

        generationStep = step.ordinal();
    }

    public GenerationStepNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        generationStep = buf.readVarInt();
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        buf.writeVarInt(generationStep);
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

    public int getGenerationStep() {
        return generationStep;
    }
}
