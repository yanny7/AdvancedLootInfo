package com.yanny.ali.plugin.mods.snow_real_magic;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.IEntry;
import com.yanny.ali.plugin.mods.IEntryItemCollector;
import com.yanny.ali.plugin.mods.SingletonContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@ClassAccessor("snownee.snow.loot.NormalizeLoot")
public class NormalizeLoot extends SingletonContainer implements IEntry, IEntryItemCollector {
    public NormalizeLoot(LootPoolSingletonContainer parent) {
        super(parent);
    }

    @Override
    public IDataNode create(IServerUtils utils, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), Arrays.stream(this.conditions)).toList();
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), Arrays.stream(this.functions)).toList();
        float c = chance * weight / sumWeight;

        return new NormalizeNode(utils, c, quality, allFunctions, allConditions);
    }

    @Override
    public List<Item> collectItems(IServerUtils utils) {
        return List.of(Items.COMMAND_BLOCK);
    }
}