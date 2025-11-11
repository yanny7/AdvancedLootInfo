package com.yanny.ali.plugin.mods.supplementaries.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("net.mehvahdjukaar.supplementaries.common.items.loot.CurseLootFunction")
public class CurseLootFunction extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private List<Enchantment> CURSES;

    @FieldAccessor
    private double chance;

    public CurseLootFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, chance).build("ali.property.value.chance"))
                .add(utils.getValueTooltip(utils, CURSES).build("ali.property.branch.enchantments"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.curse_loot");
    }
}
