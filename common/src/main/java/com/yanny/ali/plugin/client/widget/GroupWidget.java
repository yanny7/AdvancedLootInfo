package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.plugin.client.WidgetUtils;

public class GroupWidget extends CompositeWidget {
    public GroupWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        super(utils, entry, x, y, maxWidth);
        widgets.add(WidgetUtils.getGroupWidget(x, y, entry));
    }
}
