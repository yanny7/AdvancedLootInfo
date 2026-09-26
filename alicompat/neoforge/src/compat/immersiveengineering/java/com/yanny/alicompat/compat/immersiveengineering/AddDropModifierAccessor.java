package com.yanny.alicompat.compat.immersiveengineering;

import blusunrize.immersiveengineering.common.util.loot.AddDropModifier;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AddDropModifierAccessor extends BaseAccessor<AddDropModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private Supplier<ItemStack> item;

    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AddDropModifierAccessor(AddDropModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(conditions);
        ItemStack stack = item.get();

        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList,
                (page, c) -> Collections.singletonList(new IOperation.AddOperation((itemStack) -> true,
                        GlmNodeUtils.addedNode(utils, c, stack.copy(), 1, new RangeValue(stack.getCount()))))));
    }
}
