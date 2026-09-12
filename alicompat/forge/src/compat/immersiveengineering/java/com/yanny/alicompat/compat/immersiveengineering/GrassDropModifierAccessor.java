package com.yanny.alicompat.compat.immersiveengineering;

import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.loot.GrassDropModifier;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
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

public class GrassDropModifierAccessor extends BaseAccessor<GrassDropModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public GrassDropModifierAccessor(GrassDropModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(conditions);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList,
                (c) -> Collections.singletonList(new IOperation.AddOperation((itemStack) -> true,
                        GlmNodeUtils.addedNode(utils, c, new ItemStack(Misc.HEMP_SEEDS), 1, new RangeValue(1)))));
    }
}
