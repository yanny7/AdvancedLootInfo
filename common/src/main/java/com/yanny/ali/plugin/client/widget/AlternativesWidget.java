package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.plugin.client.WidgetUtils;

public class AlternativesWidget extends CompositeWidget {
    public AlternativesWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        super(utils, entry, x, y, maxWidth);
        widgets.add(WidgetUtils.getAlternativesWidget(x, y, entry));
    }
}
