package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;
import org.jetbrains.annotations.Nullable;

public class SingletonWidget extends ListWidget {
    public SingletonWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    @Override
    public @Nullable IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        return WidgetUtils.getAllWidget(rect, entry);
    }
}
