package com.yanny.ali.lootjs.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.ListWidget;
import com.yanny.ali.lootjs.node.AddLootNode;
import com.yanny.ali.plugin.client.WidgetUtils;

public class AddLootWidget extends ListWidget {
    public AddLootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    @Override
    public IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        return switch (((AddLootNode) entry).getAddType()) {
            case DEFAULT -> WidgetUtils.getAllWidget(rect, entry);
            case SEQUENCE -> WidgetUtils.getSequentialWidget(rect, entry);
            case ALTERNATIVES -> WidgetUtils.getAlternativesWidget(rect, entry);
        };
    }
}
