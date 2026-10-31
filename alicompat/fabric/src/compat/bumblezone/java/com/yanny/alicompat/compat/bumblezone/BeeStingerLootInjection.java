package com.yanny.alicompat.compat.bumblezone;

import com.telepathicgrunt.the_bumblezone.loot.NewLootInjectorApplier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class BeeStingerLootInjection {
    @NotNull
    public IPageLootModifier getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, this, Collections.emptyList(),
                (page, c) -> Collections.singletonList(new IOperation.AddOperation((itemStack) -> true,
                        GlmNodeUtils.referenceNode(utils, c, NewLootInjectorApplier.STINGER_DROP_LOOT_TABLE_RL))));
    }

    @NotNull
    public static Verdict test(IServerUtils ignoredUtils, BeeStingerLootInjection ignoredInjection, LootPage page) {
        return GlobalLootModifierUtils.testEntityTypes(page, EntityType.BEE::equals, false);
    }
}
