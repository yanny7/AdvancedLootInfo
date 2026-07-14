package com.yanny.aci.api;

public interface ICoreWidgetUtils<
        Entry,
        TDataNode    extends ICoreDataNode<?>,
        TWidgetUtils extends ICoreWidgetUtils<?, ?, ?, ?>,
        TClientUtils extends ICoreClientUtils<?, ?, ?>
        > extends ICoreClientUtils<TDataNode, TWidgetUtils, TClientUtils> {
    void addSlotWidget(Entry entry, TDataNode node, RelativeRect rect);
}
