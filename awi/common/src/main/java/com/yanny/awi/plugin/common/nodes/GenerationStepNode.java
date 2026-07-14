package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.*;

public class GenerationStepNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("generation_step");

    private final TooltipNode tooltip;
    private final int generationStep;

    public GenerationStepNode(IServerUtils utils, GenerationStep.Decoration step, HolderSet<PlacedFeature> features) {
        for (Holder<PlacedFeature> placedFeatureHolder : features) {
            addChildren(new PlacedFeatureNode(utils, placedFeatureHolder.value()));
        }

        tooltip = array((b) -> {
            switch (step) {
                case RAW_GENERATION -> b.add(value(translate(Lang.GenerationStep.RAW_GENERATION.singular())).build(Lang.Value.GENERATION_STEP));
                case LAKES -> b.add(value(translate(Lang.GenerationStep.LAKES.singular())).build(Lang.Value.GENERATION_STEP));
                case LOCAL_MODIFICATIONS -> b.add(value(translate(Lang.GenerationStep.LOCAL_MODIFICATIONS.singular())).build(Lang.Value.GENERATION_STEP));
                case UNDERGROUND_STRUCTURES -> b.add(value(translate(Lang.GenerationStep.UNDERGROUND_STRUCTURES.singular())).build(Lang.Value.GENERATION_STEP));
                case SURFACE_STRUCTURES -> b.add(value(translate(Lang.GenerationStep.SURFACE_STRUCTURES.singular())).build(Lang.Value.GENERATION_STEP));
                case STRONGHOLDS -> b.add(value(translate(Lang.GenerationStep.STRONGHOLDS.singular())).build(Lang.Value.GENERATION_STEP));
                case UNDERGROUND_ORES -> b.add(value(translate(Lang.GenerationStep.UNDERGROUND_ORES.singular())).build(Lang.Value.GENERATION_STEP));
                case UNDERGROUND_DECORATION -> b.add(value(translate(Lang.GenerationStep.UNDERGROUND_DECORATION.singular())).build(Lang.Value.GENERATION_STEP));
                case FLUID_SPRINGS -> b.add(value(translate(Lang.GenerationStep.FLUID_SPRINGS.singular())).build(Lang.Value.GENERATION_STEP));
                case VEGETAL_DECORATION -> b.add(value(translate(Lang.GenerationStep.VEGETAL_DECORATION.singular())).build(Lang.Value.GENERATION_STEP));
                case TOP_LAYER_MODIFICATION -> b.add(value(translate(Lang.GenerationStep.TOP_LAYER_MODIFICATION.singular())).build(Lang.Value.GENERATION_STEP));
            }
        }).build();

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
