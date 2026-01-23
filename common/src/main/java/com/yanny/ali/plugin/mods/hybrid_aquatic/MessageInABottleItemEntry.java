package com.yanny.ali.plugin.mods.hybrid_aquatic;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.mods.ClassAccessor;
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
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.List;

@ClassAccessor("dev.hybridlabs.aquatic.loot.entry.MessageInABottleItemEntry")
public class MessageInABottleItemEntry extends SingletonContainer implements IEntry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Item MSG_IN_BOTTLE;

    static {
        Item item = Items.AIR;
        try {
            Class<?> tradesClass = Class.forName("dev.hybridlabs.aquatic.item.HybridAquaticItems");
            Field typeMapField = tradesClass.getDeclaredField("MESSAGE_IN_A_BOTTLE");

            typeMapField.setAccessible(true);
            item = (Item) typeMapField.get(null);
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain item MESSAGE_IN_A_BOTTLE: {}", e.getMessage(), e);
        }

        MSG_IN_BOTTLE = item;
    }

    public MessageInABottleItemEntry(LootPoolSingletonContainer parent) {
        super(parent);
    }

    @Override
    public IDataNode create(IServerUtils utils, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return NodeUtils.getItemNode(utils, parent, (f) -> itemGetter(utils, f), rawChance, sumWeight, functions, conditions);
    }

    @NotNull
    private static Either<ItemStack, TagKey<? extends ItemLike>> itemGetter(IServerUtils utils, List<LootItemFunction> functions) {
        return Either.left(TooltipUtils.getItemStack(utils, MSG_IN_BOTTLE.getDefaultInstance(), functions));
    }
}
