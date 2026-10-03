package com.yanny.alicompat.compat.spellengine;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.spell_engine.spellbinding.SpellBindRandomlyLootFunction;

public class SpellBindRandomlyLootFunctionAccessor extends BaseAccessor<SpellBindRandomlyLootFunction> implements IFunctionTooltip {
    @FieldAccessor
    private Holder<ContextIntProvider> tier;

    @FieldAccessor
    private String pool;

    @FieldAccessor
    private Holder<ContextIntProvider> count;

    public SpellBindRandomlyLootFunctionAccessor(SpellBindRandomlyLootFunction parent) {
        super(parent);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipUtils.getIntNumberTooltip(utils, tier).build(SpellEngineLang.Value.TIER));
            b.add(utils.getValueTooltip(utils, pool).build(SpellEngineLang.Value.SPELL_POOL));
            b.add(TooltipUtils.getIntNumberTooltip(utils, count).build(Lang.Value.COUNT));
            b.add(utils.getValueTooltip(utils, parent.condition).build(Lang.Branch.PREDICATES));
        }, SpellEngineLang.Functions.SPELL_BIND_RANDOMLY);
    }
}
