package com.yanny.alicompat.compat.kaleidoscopecookery;

import com.github.ysbbbbbb.kaleidoscopecookery.loot.AdvanceEntityMatchTool;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class AdvanceEntityMatchToolAccessor extends BaseAccessor<AdvanceEntityMatchTool> implements IConditionTooltip {
    @FieldAccessor
    private EquipmentSlot slot;

    @FieldAccessor
    private ItemPredicate predicate;

    public AdvanceEntityMatchToolAccessor(AdvanceEntityMatchTool parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, slot).build(Lang.Value.EQUIPMENT_SLOT));
            b.add(utils.getValueTooltip(utils, predicate));
        }, KaleidoscopeCookeryLang.Conditions.ADVANCE_ENTITY_MATCH_TOOL);
    }
}
