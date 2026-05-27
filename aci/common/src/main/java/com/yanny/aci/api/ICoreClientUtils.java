package com.yanny.aci.api;

import com.yanny.aci.tooltip.TooltipNodePalette;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public interface ICoreClientUtils<
        TDataNode    extends ICoreDataNode<?>,
        TWidgetUtils extends ICoreWidgetUtils<?>,
        SELF         extends ICoreClientUtils<?, ?, ?>
        > {
    @NotNull
    List<IWidget> createWidgets(TWidgetUtils utils, List<TDataNode> entries, RelativeRect parent, int maxWidth);

    @NotNull
    BiFunction<SELF, RegistryFriendlyByteBuf, TDataNode> getDataNodeFactory(Identifier id);

    @Nullable
    String getTranslationKey(int index);

    @NotNull
    TooltipNodePalette getTooltipCache();
}
