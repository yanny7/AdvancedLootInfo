package com.yanny.awi.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.api.ListWidget;
import org.jetbrains.annotations.Nullable;

public class BiomeWidget extends ListWidget {
    public BiomeWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    @Override
    public @Nullable IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        return null;
    }
}
