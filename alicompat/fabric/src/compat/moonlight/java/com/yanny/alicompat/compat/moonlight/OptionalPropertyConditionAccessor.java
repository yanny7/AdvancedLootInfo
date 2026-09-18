package com.yanny.alicompat.compat.moonlight;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import com.yanny.alicompat.accessor.IPageResolverAccessor;
import net.mehvahdjukaar.moonlight.core.loot.OptionalPropertyCondition;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class OptionalPropertyConditionAccessor extends BaseAccessor<OptionalPropertyCondition> implements IConditionTooltip, IPageResolverAccessor {
    @FieldAccessor
    private Block block;

    @FieldAccessor
    private Optional<StatePropertiesPredicate> properties;

    @FieldAccessor
    private ResourceLocation blockId;

    public OptionalPropertyConditionAccessor(OptionalPropertyCondition parent) {
        super(parent);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, blockId).build(Lang.Value.BLOCK));
            b.add(utils.getValueTooltip(utils, properties).build(Lang.Branch.PROPERTIES));
        }, MoonlightLang.Conditions.OPTIONAL_BLOCK_STATE_PROPERTY);
    }

    @Nullable
    @Override
    public Verdict test(IServerUtils utils, LootPage page) {
        if (block == null || page.blocks().isEmpty()) {
            return null;
        }

        return GlobalLootModifierUtils.testBlocks(page, block::equals, properties.map((p) -> p.properties().isEmpty()).orElse(true));
    }
}
