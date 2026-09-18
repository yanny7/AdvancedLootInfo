package com.yanny.alicompat.compat.aether;

import com.aetherteam.aether.loot.functions.WhirlwindSpawnEntity;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.util.valueproviders.IntProvider;
import org.jetbrains.annotations.NotNull;

public class WhirlwindSpawnEntityAccessor extends BaseAccessor<WhirlwindSpawnEntity> implements IFunctionTooltip {
    @FieldAccessor
    private EntityTypePredicate entityType;
    @FieldAccessor
    private IntProvider count;

    public WhirlwindSpawnEntityAccessor(WhirlwindSpawnEntity parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, entityType).build(Lang.Branch.ENTITY_TYPES));
            b.add(utils.getValueTooltip(utils, count).build(Lang.Value.COUNT));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, AetherLang.Functions.WHIRLWIND_SPAWN_ENTITY);
    }
}
