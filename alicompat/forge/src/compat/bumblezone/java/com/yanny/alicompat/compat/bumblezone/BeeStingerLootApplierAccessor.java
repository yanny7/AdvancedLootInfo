package com.yanny.alicompat.compat.bumblezone;

import com.telepathicgrunt.the_bumblezone.loot.NewLootInjectorApplier;
import com.telepathicgrunt.the_bumblezone.loot.forge.BeeStingerLootApplier;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IDestination;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BeeStingerLootApplierAccessor extends BaseAccessor<BeeStingerLootApplier> implements IGlobalLootModifierAccessor, IDestination {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public BeeStingerLootApplierAccessor(BeeStingerLootApplier parent) {
        super(parent);
    }

    @NotNull
    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(conditions);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList,
                (c) -> Collections.singletonList(new IOperation.AddOperation((itemStack) -> true,
                        GlmNodeUtils.referenceNode(utils, c, NewLootInjectorApplier.STINGER_DROP_LOOT_TABLE_RL))));
    }

    @NotNull
    @Override
    public Destination getDestination(IServerUtils ignoredUtils) {
        return new Destination.Entities(EntityTypePredicate.of(EntityType.BEE), false);
    }
}
