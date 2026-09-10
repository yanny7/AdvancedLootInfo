package com.yanny.alicompat.compat.moonlight;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IEntry;
import com.yanny.alicompat.accessor.IEntryTooltip;
import net.mehvahdjukaar.moonlight.core.loot.OptionalItemPool;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class OptionalItemPoolAccessor extends BaseAccessor<OptionalItemPool> implements IEntry, IEntryTooltip {
    @FieldAccessor
    private Item item;

    @FieldAccessor
    private String res;

    public OptionalItemPoolAccessor(OptionalItemPool parent) {
        super(parent);
    }

    @Override
    public IDataNode create(IServerUtils utils, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return NodeUtils.getItemNode(utils, parent, (f) -> Either.left(TooltipUtils.getItemStack(utils, getItemStack(), f)), chance, sumWeight, functions, conditions);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, item).build(Lang.Value.ITEM));
            b.add(utils.getValueTooltip(utils, res).build(Lang.Value.NAME));
            b.add(TooltipUtils.getWeightTooltip(parent.weight));
            b.add(TooltipUtils.getQualityTooltip(parent.quality));
            b.add(utils.getValueTooltip(utils, parent.conditions).build(Lang.Branch.PREDICATES));
            b.add(utils.getValueTooltip(utils, parent.functions).build(Lang.Branch.MODIFIERS));
        }, MoonlightLang.Entry.OPTIONAL_ITEM);
    }

    private ItemStack getItemStack() {
        return item == null ? ItemStack.EMPTY : item.getDefaultInstance();
    }
}
