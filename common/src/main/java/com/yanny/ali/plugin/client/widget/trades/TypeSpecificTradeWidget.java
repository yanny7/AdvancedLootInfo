package com.yanny.ali.plugin.client.widget.trades;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;

public class TypeSpecificTradeWidget extends ListWidget {
    public TypeSpecificTradeWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    @Override
    public IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        return WidgetUtils.getRandomWidget(rect, entry);
    }
}
