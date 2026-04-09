package com.yanny.ali.plugin.mods.supplementaries.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

@ClassAccessor("net.mehvahdjukaar.supplementaries.common.items.loot.RandomEnchantFunction")
public class RandomEnchantFunction extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private HolderSet<Enchantment> curses;

    @FieldAccessor
    private double chance;

    public RandomEnchantFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, chance).build("ali.property.value.chance"))
                .add(utils.getValueTooltip(utils, curses).build("ali.property.branch.enchantments"))
                .add(getSubConditionsTooltip(utils, predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.curse_loot");
    }
}
