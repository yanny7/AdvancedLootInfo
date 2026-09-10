package com.yanny.alicompat.compat.hybridaquatic;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.IEntry;
import com.yanny.alicompat.accessor.IEntryTooltip;
import com.yanny.alicompat.accessor.SingletonContainer;
import dev.hybridlabs.aquatic.item.HybridAquaticItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageInABottleItemEntryAccessor extends SingletonContainer implements IEntry, IEntryTooltip {
    public MessageInABottleItemEntryAccessor(LootPoolSingletonContainer parent) {
        super(parent);
    }

    @Override
    public IDataNode create(IServerUtils utils, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return NodeUtils.getItemNode(utils, parent, (f) -> Either.left(TooltipUtils.getItemStack(utils, getItemStack(), f)), chance, sumWeight, functions, conditions);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, getItemStack().getItem()).build(Lang.Value.ITEM));
            b.add(TooltipBuilder.keyOnly(HybridAquaticLang.Value.RANDOM_SEA_MESSAGE));
            b.add(TooltipUtils.getWeightTooltip(weight));
            b.add(TooltipUtils.getQualityTooltip(quality));
            b.add(utils.getValueTooltip(utils, conditions).build(Lang.Branch.PREDICATES));
            b.add(utils.getValueTooltip(utils, functions).build(Lang.Branch.MODIFIERS));
        }, HybridAquaticLang.Entry.MESSAGE_IN_A_BOTTLE);
    }

    @NotNull
    private static ItemStack getItemStack() {
        return HybridAquaticItems.INSTANCE.getMESSAGE_IN_A_BOTTLE().getDefaultInstance();
    }
}
