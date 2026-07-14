package com.yanny.ali.plugin.client.widget.trades;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.ListWidget;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.common.trades.TradeLevelNode;
import org.jetbrains.annotations.Nullable;

public class TradeLevelWidget extends ListWidget {
    public TradeLevelWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    @Nullable
    @Override
    public IWidget getGroupWidget(RelativeRect rect, IDataNode entry) {
        return WidgetUtils.getLevelWidget(rect, entry, ((TradeLevelNode) entry).level);
    }
}
