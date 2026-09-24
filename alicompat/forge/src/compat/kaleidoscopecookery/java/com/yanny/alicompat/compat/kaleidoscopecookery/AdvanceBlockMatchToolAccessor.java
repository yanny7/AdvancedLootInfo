package com.yanny.alicompat.compat.kaleidoscopecookery;

import com.github.ysbbbbbb.kaleidoscopecookery.loot.AdvanceBlockMatchTool;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class AdvanceBlockMatchToolAccessor extends BaseAccessor<AdvanceBlockMatchTool> implements IConditionTooltip {
    @FieldAccessor
    private EquipmentSlot slot;

    @FieldAccessor
    private ItemPredicate predicate;

    public AdvanceBlockMatchToolAccessor(AdvanceBlockMatchTool parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, slot).build(Lang.Value.EQUIPMENT_SLOT));
            b.add(utils.getValueTooltip(utils, predicate));
        }, KaleidoscopeCookeryLang.Conditions.ADVANCE_BLOCK_MATCH_TOOL);
    }
}
