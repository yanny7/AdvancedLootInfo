package com.yanny.ali.api;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IClientUtils extends ICommonUtils {
    Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<IDataNode> entries, int x, int y, int maxWidth);

    <T extends IDataNode> IClientRegistry.NodeFactory<T> getNodeFactory(ResourceLocation id);

    Rect getBounds(IClientUtils registry, List<IDataNode> entries, int x, int y, int maxWidth);

    @Nullable
    WidgetDirection getWidgetDirection(ResourceLocation id);

    List<Item> getItems(ResourceLocation location);
}
