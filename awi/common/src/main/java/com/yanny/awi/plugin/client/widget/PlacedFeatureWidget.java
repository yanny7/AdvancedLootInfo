package com.yanny.awi.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.api.ListWidget;
import com.yanny.awi.plugin.client.WidgetUtils;
import org.jetbrains.annotations.Nullable;

public class PlacedFeatureWidget extends ListWidget {
    public PlacedFeatureWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    @Nullable
    @Override
    public IWidget getGroupWidget(RelativeRect rect, IDataNode entry) {
        return WidgetUtils.getAllWidget(rect, entry);
    }
}
