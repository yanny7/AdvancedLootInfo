package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.IDataNode;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public record TradeLootType(Set<Block> pois, Set<Item> accepts, IDataNode entry, String id, List<ItemStack> inputs, List<ItemStack> outputs) implements IType {
    public TradeLootType(VillagerProfession profession, IDataNode entry, String id, List<ItemStack> inputs, List<ItemStack> outputs) {
        this(GenericUtils.getJobSites(profession), GenericUtils.getRequestedItems(profession), entry, id, inputs, outputs);
    }
}
