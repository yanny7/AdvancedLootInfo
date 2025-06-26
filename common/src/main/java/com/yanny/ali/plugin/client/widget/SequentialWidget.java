package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.plugin.client.WidgetUtils;

public class SequentialWidget extends CompositeWidget {
    public SequentialWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        super(utils, entry, x, y, maxWidth);
        widgets.add(WidgetUtils.getSequentialWidget(x, y, entry));
    }
}
