package com.yanny.ali.plugin.mods.moonlight;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IEntry;
import com.yanny.ali.plugin.mods.SingletonContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ClassAccessor("net.mehvahdjukaar.moonlight.core.loot.OptionalItemPool")
public class OptionalItemPool extends SingletonContainer implements IEntry {
    @FieldAccessor("item")
    @Nullable
    private Item item;

    public OptionalItemPool(LootPoolSingletonContainer entry) {
        super(entry);
    }

    @Override
    public IDataNode create(IServerUtils utils, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return new ItemNode(utils, (LootItem) LootItem.lootTableItem(item != null ? item : Items.AIR).build(), chance, sumWeight, functions, conditions);
    }
}
