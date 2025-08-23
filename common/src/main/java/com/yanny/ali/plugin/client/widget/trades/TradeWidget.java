package com.yanny.ali.plugin.client.widget.trades;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;
import org.jetbrains.annotations.Nullable;

public class TradeWidget extends ListWidget {
    public TradeWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    @Nullable
    @Override
    public IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        return WidgetUtils.getAllWidget(rect, entry);
    }
}
