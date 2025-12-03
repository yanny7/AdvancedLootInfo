package com.yanny.ali.plugin.mods.moonlight;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IEntry;
import com.yanny.ali.plugin.mods.SingletonContainer;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ClassAccessor("net.mehvahdjukaar.moonlight.core.loot.OptionalItemPool")
public class OptionalItemPool extends SingletonContainer implements IEntry {
    @FieldAccessor
    @Nullable
    private Item item;

    public OptionalItemPool(LootPoolSingletonContainer entry) {
        super(entry);
    }

    @Override
    public IDataNode create(IServerUtils utils, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return NodeUtils.getItemNode(utils, parent, (f) -> itemGetter(utils, f), rawChance, sumWeight, functions, conditions);
    }

    @NotNull
    private Either<ItemStack, TagKey<? extends ItemLike>> itemGetter(IServerUtils utils, List<LootItemFunction> functions) {
        return Either.left(TooltipUtils.getItemStack(utils, (item != null ? item : Items.AIR).getDefaultInstance(), functions));
    }
}
