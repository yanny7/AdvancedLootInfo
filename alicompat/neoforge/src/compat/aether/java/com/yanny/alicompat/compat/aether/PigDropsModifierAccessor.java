package com.yanny.alicompat.compat.aether;

import com.aetherteam.aether.AetherTags;
import com.aetherteam.aether.loot.modifiers.PigDropsModifier;
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

public class PigDropsModifierAccessor extends BaseAccessor<PigDropsModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public PigDropsModifierAccessor(PigDropsModifier parent) {
        super(parent);
    }

    @NotNull
    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(conditions);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList,
                (c) -> Collections.singletonList(new IOperation.ReplaceOperation(
                        (itemStack) -> itemStack.is(AetherTags.Items.PIG_DROPS),
                        (src) -> AetherNodeUtils.countedNode(utils, AetherNodeUtils.withChance(c, 0.25F), src,
                                new RangeValue(((IItemNode) src).getCount()).multiply(2)))));
    }
}
