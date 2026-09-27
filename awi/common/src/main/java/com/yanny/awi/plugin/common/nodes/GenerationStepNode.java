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

    GenerationStepNode(int step, List<PlacedFeatureNode> features) {
        GenerationStep.Decoration[] steps = GenerationStep.Decoration.values();

        for (PlacedFeatureNode feature : features) {
            addChildren(feature);
        }

        if (step < steps.length) {
            tooltip = array((b) -> b.add(value(translate(EnumTypes.key(steps[step]))).build(Lang.Value.GENERATION_STEP))).build();
        } else {
            tooltip = array((b) -> b.add(value(step - steps.length + 1).build(Lang.Value.EXTRA_GENERATION_STEP))).build();
        }

        generationStep = step;
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
