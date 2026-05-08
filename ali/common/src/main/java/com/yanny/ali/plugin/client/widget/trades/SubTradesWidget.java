package com.yanny.ali.plugin.client.widget.trades;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.ListWidget;
import com.yanny.ali.plugin.client.WidgetUtils;
import net.minecraft.core.HolderLookup;

public class SubTradesWidget extends ListWidget {
    private final HolderLookup.Provider provider;

    public SubTradesWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
        provider = utils.lookupProvider();
    }

    @Override
    public IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        return WidgetUtils.getRandomWidget(provider, rect, entry);
    }
}
