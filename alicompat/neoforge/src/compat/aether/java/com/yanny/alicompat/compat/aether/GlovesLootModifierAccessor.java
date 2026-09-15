package com.yanny.alicompat.compat.aether;

import com.aetherteam.aether.loot.modifiers.GlovesLootModifier;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GlovesLootModifierAccessor extends BaseAccessor<GlovesLootModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public GlovesLootModifierAccessor(GlovesLootModifier parent) {
        super(parent);
    }

    @NotNull
    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(conditions);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList,
                (c) -> Collections.singletonList(new IOperation.ReplaceOperation(
                        (itemStack) -> itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial().equals(parent.armorMaterial),
                        (src) -> GlmNodeUtils.replacedNode(utils, AetherNodeUtils.withChance(c, 0.25F), src, parent.glovesStack, new RangeValue(1)))));
    }
}
