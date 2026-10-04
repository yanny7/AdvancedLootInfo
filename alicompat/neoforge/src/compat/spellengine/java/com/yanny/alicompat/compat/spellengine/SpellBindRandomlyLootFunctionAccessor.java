package com.yanny.alicompat.compat.spellengine;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.spellbinding.SpellBindRandomlyLootFunction;

public class SpellBindRandomlyLootFunctionAccessor extends BaseAccessor<SpellBindRandomlyLootFunction> implements IFunctionTooltip {
    @FieldAccessor
    private NumberProvider tier;

    @FieldAccessor
    private String pool;

    @FieldAccessor
    private NumberProvider count;

    public SpellBindRandomlyLootFunctionAccessor(SpellBindRandomlyLootFunction parent) {
        super(parent);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, tier).build(SpellEngineLang.Value.TIER));

            if (pool != null && !pool.isEmpty()) {
                b.add(utils.getValueTooltip(utils, TagKey.create(SpellRegistry.KEY, Identifier.parse(pool.startsWith("#") ? pool.substring(1) : pool))).build(SpellEngineLang.Value.SPELL_POOL));
            }

            b.add(utils.getValueTooltip(utils, count).build(Lang.Value.COUNT));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, SpellEngineLang.Functions.SPELL_BIND_RANDOMLY);
    }
}
