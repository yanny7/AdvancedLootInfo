package com.yanny.ali.plugin.mods.aether.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

@ClassAccessor("com.aetherteam.aether.loot.functions.WhirlwindSpawnEntity")
public class WhirlwindSpawnEntity extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private EntityTypePredicate entityType;
    @FieldAccessor
    private IntProvider count;

    public WhirlwindSpawnEntity(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.whirlwind_spawn_entity"));

        tooltip.add(getEntityTypePredicateTooltip(utils, "ali.property.branch.entity_types", entityType));
        tooltip.add(getStringTooltip(utils, "ali.property.value.count", RangeValue.rangeToString(new RangeValue(count.getMinValue()), new RangeValue(count.getMaxValue()))));
        tooltip.add(getSubConditionsTooltip(utils, predicates));

        return tooltip;
    }
}
