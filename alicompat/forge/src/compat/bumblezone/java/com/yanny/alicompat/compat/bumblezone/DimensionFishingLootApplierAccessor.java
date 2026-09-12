package com.yanny.alicompat.compat.bumblezone;

import com.telepathicgrunt.the_bumblezone.loot.NewLootInjectorApplier;
import com.telepathicgrunt.the_bumblezone.loot.forge.DimensionFishingLootApplier;
import com.telepathicgrunt.the_bumblezone.modinit.BzDimension;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DimensionFishingLootApplierAccessor extends BaseAccessor<DimensionFishingLootApplier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public DimensionFishingLootApplierAccessor(DimensionFishingLootApplier parent) {
        super(parent);
    }

    @NotNull
    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(conditions);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList, (c) -> {
            List<LootItemCondition> inDimension = new ArrayList<>(c);

            inDimension.add(LocationCheck.checkLocation(LocationPredicate.Builder.location().setDimension(BzDimension.BZ_WORLD_KEY)).build());
            return List.of(
                    new IOperation.AddOperation((itemStack) -> true,
                            GlmNodeUtils.referenceNode(utils, inDimension, NewLootInjectorApplier.BZ_DIMENSION_FISHING_LOOT_TABLE_RL)),
                    new IOperation.RemoveOperation((itemStack) -> true,
                            (src) -> GlmNodeUtils.keptNode(utils, inDimension, src)));
        });
    }
}
