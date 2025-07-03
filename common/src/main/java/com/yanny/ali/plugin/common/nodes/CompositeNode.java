package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class CompositeNode extends ListNode {
    public CompositeNode(List<ILootModifier<?>> modifiers, IServerUtils utils, CompositeEntryBase entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), Arrays.stream(entry.conditions)).toList();

        for (LootPoolEntryContainer child : entry.children) {
            addChildren(utils.getEntryFactory(utils, child).create(modifiers, utils, child, chance, sumWeight, functions, allConditions));
        }
    }

    public CompositeNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
    }
}
