package com.yanny.ali.plugin.glm;

import com.yanny.ali.api.IServerUtils;
import net.minecraft.advancements.predicates.entity.EntitySubPredicate;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IEntitySubPredicateResolver<T extends EntitySubPredicate> {
    @Nullable
    Verdict test(IServerUtils utils, T predicate, LootPage page);
}
