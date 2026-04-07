package com.yanny.ali.forge.plugin.mods.deeper_and_darker_glm;

import com.yanny.ali.api.*;
import com.yanny.ali.forge.plugin.GlobalLootModifier;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierAccessor;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;

import java.util.*;

@ClassAccessor("com.kyanite.deeperdarker.content.loot.AddItemModifier")
public class AddItemModifier extends GlobalLootModifier implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private Item item;
    @FieldAccessor
    private int min;
    @FieldAccessor
    private int max;

    public AddItemModifier(LootModifier parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils, ILootTableIdConditionPredicate predicate) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);

        return GlobalLootModifierUtils.getLootModifier(conditionList, (c) -> {
            Map<Enchantment, Map<Integer, RangeValue>> chance = NodeUtils.getEnchantedChance(utils, c, 1);
            Map<Enchantment, Map<Integer, RangeValue>> count = NodeUtils.getEnchantedCount(utils, Collections.emptyList());
            ITooltipNode tooltip = EntryTooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, count, Collections.emptyList(), c);
            IDataNode node = new ItemNode(1, new RangeValue(min, max), item.getDefaultInstance(), tooltip, Collections.emptyList(), c);
            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        }, predicate);
    }
}
