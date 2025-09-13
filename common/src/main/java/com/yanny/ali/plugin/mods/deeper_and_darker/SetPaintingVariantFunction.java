package com.yanny.ali.plugin.mods.deeper_and_darker;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;
import java.util.Optional;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

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
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_painting_variant"));

        validPaintings.ifPresent((tagKey) -> tooltip.add(getTagKeyTooltip(utils, "ali.property.value.tag", tagKey)));
        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(predicates)));

        return tooltip;
    }
}
