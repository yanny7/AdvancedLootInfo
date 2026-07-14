package com.yanny.ali.api;

import com.yanny.aci.api.CoreListWidget;
import com.yanny.aci.api.RelativeRect;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class ListWidget extends CoreListWidget<IDataNode, IWidgetUtils, IClientUtils> {
    public static final ResourceLocation TEXTURE_LOC = ResourceLocation.fromNamespaceAndPath("ali", "textures/gui/gui.png");

    public ListWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth, entry);
    }

    public ListWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth, IDataNode tooltipNode) {
        super(utils, entry, rect, maxWidth, tooltipNode);
    }

    @NotNull
    @Override
    public ResourceLocation getTexture() {
        return TEXTURE_LOC;
    }
}
