package com.yanny.alicompat.compat.aether;

import com.aetherteam.aether.loot.modifiers.EnchantedGrassModifier;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EnchantedGrassModifierAccessor extends BaseAccessor<EnchantedGrassModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public EnchantedGrassModifierAccessor(EnchantedGrassModifier parent) {
        super(parent);
    }

    @NotNull
    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(conditions);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList,
                (c) -> Collections.singletonList(new IOperation.ReplaceOperation(
                        (itemStack) -> itemStack.is(parent.item.getItem()),
                        (src) -> AetherNodeUtils.countedNode(utils, AetherNodeUtils.withChance(c, 0.5F), src,
                                new RangeValue(((IItemNode) src).getCount()).add(1)))));
    }
}
