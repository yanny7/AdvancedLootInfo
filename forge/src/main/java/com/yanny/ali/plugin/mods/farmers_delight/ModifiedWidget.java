package com.yanny.ali.plugin.mods.farmers_delight;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;

public class ModifiedWidget extends ListWidget {
    public ModifiedWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    @Override
    public IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        return WidgetUtils.getAlternativesWidget(rect, entry);
    }
}
