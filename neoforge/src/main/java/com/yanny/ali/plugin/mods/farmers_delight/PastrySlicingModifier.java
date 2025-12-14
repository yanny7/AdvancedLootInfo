package com.yanny.ali.plugin.mods.farmers_delight;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.GlobalLootModifier;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierAccessor;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.*;

@ClassAccessor("vectorwing.farmersdelight.common.loot.modifier.PastrySlicingModifier")
public class PastrySlicingModifier extends GlobalLootModifier implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private Item pastrySlice;

    public PastrySlicingModifier(LootModifier parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils, ILootTableIdConditionPredicate predicate) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);

        return GlobalLootModifierUtils.getLootModifier(conditionList, (c) -> {
            Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance = NodeUtils.getEnchantedChance(utils, c, 1);
            Map<Holder<Enchantment>, Map<Integer, RangeValue>> count = new HashMap<>();

            count.put(null, Map.of(0, new RangeValue(1, 7)));

            ITooltipNode tooltip = EntryTooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, count, Collections.emptyList(), c);
            IDataNode node = new ItemNode(1, new RangeValue(1, 7), pastrySlice.getDefaultInstance(), tooltip, Collections.emptyList(), c);

            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        }, predicate);
    }
}
