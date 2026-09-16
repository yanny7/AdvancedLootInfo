package com.yanny.alicompat.accessor;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import org.jetbrains.annotations.Nullable;

public interface IEntitySubPredicateResolverAccessor {
    @Nullable
    Verdict test(IServerUtils utils, LootPage page);
}
