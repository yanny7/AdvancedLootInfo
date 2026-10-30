package com.yanny.alicompat.compat.bumblezone;

import com.telepathicgrunt.the_bumblezone.loot.NewLootInjectorApplier;
import com.telepathicgrunt.the_bumblezone.modinit.BzDimension;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DimensionFishingLootInjection {
    @NotNull
    public IPageLootModifier getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, this, Collections.emptyList(), (page, c) -> {
            List<LootItemCondition> inDimension = new ArrayList<>(c);

            inDimension.add(LocationCheck.checkLocation(LocationPredicate.Builder.location().setDimension(BzDimension.BZ_WORLD_KEY)).build());
            return List.of(
                    new IOperation.AddOperation((itemStack) -> true,
                            GlmNodeUtils.referenceNode(utils, inDimension, NewLootInjectorApplier.BZ_DIMENSION_FISHING_LOOT_TABLE_RL)),
                    new IOperation.RemoveOperation((itemStack) -> true,
                            (src) -> GlmNodeUtils.keptNode(utils, inDimension, src)));
        });
    }

    @NotNull
    public static Verdict test(IServerUtils ignoredUtils, DimensionFishingLootInjection ignoredInjection, LootPage page) {
        return GlobalLootModifierUtils.testTable(page, NewLootInjectorApplier.VANILLA_FISHING_LOOT_TABLE_RL::equals, false);
    }
}
