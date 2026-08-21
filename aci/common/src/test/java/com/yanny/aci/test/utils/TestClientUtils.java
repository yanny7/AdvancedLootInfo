package com.yanny.aci.test.utils;

import com.yanny.aci.api.ICoreWidgetUtils;
import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.tooltip.TooltipNodePalette;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class TestClientUtils implements ICoreWidgetUtils<Object, TestDataNode, TestClientUtils, TestClientUtils> {
    private static final String MOD_ID = "aci_test";

    private final TooltipNodePalette palette = new TooltipNodePalette(MOD_ID);
    private final Map<ResourceLocation, BiFunction<TestClientUtils, RegistryFriendlyByteBuf, TestDataNode>> factories = new HashMap<>();
    private final List<String> dictionary;

    @NotNull
    @Override
    public String getModId() {
        return MOD_ID;
    }

    public TestClientUtils(List<String> dictionary) {
        this.dictionary = dictionary;
        factories.put(TestLeafNode.ID, TestLeafNode::new);
        factories.put(TestListNode.ID, TestListNode::new);
    }

    @NotNull
    @Override
    public List<IWidget> createWidgets(TestClientUtils utils, List<TestDataNode> entries, RelativeRect parent, int maxWidth) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public BiFunction<TestClientUtils, RegistryFriendlyByteBuf, TestDataNode> getDataNodeFactory(ResourceLocation id) {
        BiFunction<TestClientUtils, RegistryFriendlyByteBuf, TestDataNode> factory = factories.get(id);

        if (factory == null) {
            throw new IllegalStateException("No data node factory registered for " + id);
        }

        return factory;
    }

    @Nullable
    @Override
    public String getTranslationKey(int index) {
        return index >= 0 && index < dictionary.size() ? dictionary.get(index) : null;
    }

    @NotNull
    @Override
    public TooltipNodePalette getTooltipCache() {
        return palette;
    }

    @Override
    public void addSlotWidget(Object entry, TestDataNode node, RelativeRect rect) {
        throw new UnsupportedOperationException();
    }
}
