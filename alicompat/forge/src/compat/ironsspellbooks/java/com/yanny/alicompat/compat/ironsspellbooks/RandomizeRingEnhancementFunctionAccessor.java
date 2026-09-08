package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import io.redspace.ironsspellbooks.loot.RandomizeRingEnhancementFunction;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import org.jetbrains.annotations.NotNull;

public class RandomizeRingEnhancementFunctionAccessor extends BaseAccessor<RandomizeRingEnhancementFunction> implements IFunctionTooltip {
    @FieldAccessor
    private SpellFilter spellFilter;

    public RandomizeRingEnhancementFunctionAccessor(RandomizeRingEnhancementFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, spellFilter).build(IronsSpellbooksLang.Branch.SPELL_FILTER));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, IronsSpellbooksLang.Functions.RANDOMIZE_RING_ENHANCEMENT);
    }
}
