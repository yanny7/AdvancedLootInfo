package com.yanny.ali.plugin.mods.supplementaries.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

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
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.curse_loot"));

        tooltip.add(getDoubleTooltip(utils, "ali.property.value.chance", chance));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.enchantments", "ali.property.value.null", CURSES, RegistriesTooltipUtils::getEnchantmentTooltip));
        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(predicates)));

        return tooltip;
    }
}
