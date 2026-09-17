package com.yanny.ali.plugin.glm;

import com.yanny.ali.api.IOperation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IPageLootModifier {
    @NotNull
    PageMatch test(LootPage page);

    @NotNull
    List<IOperation> getOperations(LootPage page, PageMatch match);
}
