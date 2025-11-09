package com.yanny.ali.plugin.mods.the_bumblezone.predicates;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IEntitySubPredicateTooltip;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import org.slf4j.Logger;

import java.lang.reflect.Field;

@ClassAccessor("com.telepathicgrunt.the_bumblezone.entities.subpredicates.HoneySlimePredicate")
public class HoneySlimePredicate extends BaseAccessor<EntitySubPredicate> implements IEntitySubPredicateTooltip {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final MapCodec<HoneySlimePredicate> CODEC;

    @FieldAccessor
    private boolean isBaby;

    static {
        MapCodec<HoneySlimePredicate> codec = null;

        try {
            Class<?> tradesClass = Class.forName("com.telepathicgrunt.the_bumblezone.entities.subpredicates.HoneySlimePredicate");
            Field typeMapField = tradesClass.getDeclaredField("CODEC");

            typeMapField.setAccessible(true);
            //noinspection unchecked
            codec = (MapCodec<HoneySlimePredicate>) typeMapField.get(null);
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain MapCodec: {}", e.getMessage());
        }

        CODEC = codec;
    }

    public HoneySlimePredicate(EntitySubPredicate parent) {
        super(parent);
    }

    @Override
    public IKeyTooltipNode getTooltip(IServerUtils utils) {
        return  BranchTooltipNode.branch("ali.type.entity_sub_predicate.honey_slime")
                .add(utils.getValueTooltip(utils, isBaby).key("ali.property.value.is_baby"));
    }
}
