package com.yanny.ali.plugin.mods.hybrid_aquatic;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.IEntry;
import com.yanny.ali.plugin.mods.SingletonContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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
            e.printStackTrace();
            LOGGER.warn("Unable to obtain item MESSAGE_IN_A_BOTTLE: {}", e.getMessage());
        }

        MSG_IN_BOTTLE = item;
    }

    public MessageInABottleItemEntry(LootPoolSingletonContainer parent) {
        super(parent);
    }

    @Override
    public IDataNode create(IServerUtils utils, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return new ItemNode(utils, (LootItem) LootItem.lootTableItem(MSG_IN_BOTTLE).build(), chance, sumWeight, functions, conditions);
    }
}
