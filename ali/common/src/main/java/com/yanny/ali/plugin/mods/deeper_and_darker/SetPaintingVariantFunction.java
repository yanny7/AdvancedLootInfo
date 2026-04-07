package com.yanny.ali.plugin.mods.deeper_and_darker;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;
import java.util.Optional;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("com.kyanite.deeperdarker.util.SetPaintingVariantFunction")
public class SetPaintingVariantFunction extends ConditionalFunction implements IFunctionTooltip {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @FieldAccessor
    private Optional<TagKey<PaintingVariant>> validPaintings;

    public SetPaintingVariantFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch();

        validPaintings.ifPresent((tagKey) -> tooltip.add(utils.getValueTooltip(utils, tagKey).build("ali.property.value.tag")));
        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(predicates)).build("ali.property.branch.conditions"));

        return tooltip.build("ali.type.function.set_painting_variant");
    }
}
