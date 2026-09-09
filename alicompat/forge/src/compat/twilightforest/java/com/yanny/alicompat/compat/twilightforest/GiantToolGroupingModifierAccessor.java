package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import twilightforest.loot.modifiers.GiantToolGroupingModifier;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class GiantToolGroupingModifierAccessor extends BaseAccessor<GiantToolGroupingModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public GiantToolGroupingModifierAccessor(GiantToolGroupingModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        Map<Block, Item> conversions = Map.copyOf(GiantToolGroupingModifier.CONVERSIONS);

        if (conversions.isEmpty()) {
            return Optional.empty();
        }

        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (c) -> conversions.entrySet().stream()
                .map((entry) -> (IOperation) new IOperation.ReplaceOperation(
                        (itemStack) -> itemStack.getItem().equals(entry.getKey().asItem()),
                        (src) -> GlmNodeUtils.replacedNode(utils, c, src, entry.getValue().getDefaultInstance(), new RangeValue(1))))
                .toList());
    }
}
