package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

import java.util.*;

public class AliClientRegistry implements IClientRegistry, IClientUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PADDING = 2;

    private final Map<ResourceLocation, IWidgetFactory> widgetMap = new HashMap<>();
    private final Map<ResourceLocation, NodeFactory<?>> nodeFactoryMap = new HashMap<>();
    private final Map<ResourceLocation, List<Item>> lootItemMap = new HashMap<>();
    private final Map<ResourceLocation, IDataNode> lootNodeMap = new HashMap<>();

    public void addLootData(ResourceLocation resourceLocation, IDataNode node, List<Item> items) {
        lootItemMap.put(resourceLocation, items);
        lootNodeMap.put(resourceLocation, node);
    }

    public Map<ResourceLocation, IDataNode> getLootData() {
        return lootNodeMap;
    }

    public void clearLootData() {
        lootNodeMap.clear();
        lootItemMap.clear();
    }

    @Override
    public void registerWidget(ResourceLocation id, IWidgetFactory factory) {
        widgetMap.put(id, factory);
    }

    @Override
    public <T extends IDataNode> void registerNode(ResourceLocation id, NodeFactory<T> nodeFactory) {
        nodeFactoryMap.put(id, nodeFactory);
    }

    @Override
    public List<IWidget> createWidgets(IWidgetUtils utils, List<IDataNode> entries, RelativeRect parent, int maxWidth) {
        int posX = 0, posY = 0;
        int newLines = 0;
        List<IWidget> widgets = new LinkedList<>();
        WidgetDirection lastDirection = null;

        for (IDataNode entry : entries) {
            IWidgetFactory widgetFactory = widgetMap.getOrDefault(entry.getId(), widgetMap.get(MissingNode.ID));
            IWidget widget = widgetFactory.create(utils, entry, new RelativeRect(posX, posY, parent.width - posX, 0, parent), maxWidth);
            RelativeRect bounds = widget.getRect();
            WidgetDirection direction = widget.getDirection();

            if (lastDirection == null) {
                if (direction == WidgetDirection.HORIZONTAL) {
                    posX += bounds.width;
                } else {
                    posY += bounds.height + PADDING;
                }
            } else {
                if (lastDirection == WidgetDirection.HORIZONTAL && direction == WidgetDirection.HORIZONTAL) {
                    if (bounds.getRight() <= maxWidth) {
                        posX += bounds.width;
                    } else {
                        posX = bounds.width;
                        posY += widgets.get(widgets.size() - 1).getRect().height;
                        bounds.setOffset(0, posY);
                    }
                } else {
                    posX = 0;
                    newLines++;

                    if (direction == lastDirection) {
                        posY += bounds.height + PADDING;
                    } else {
                        posY += widgets.get(widgets.size() - 1).getRect().height + bounds.height + PADDING;
                        bounds.setOffset(0, widgets.get(widgets.size() - 1).getRect().height);
                    }
                }
            }

            widgets.add(widget);
            lastDirection = direction;
        }

        int w = 0, h = 0;

        for (IWidget widget : widgets) {
            RelativeRect rect = widget.getRect();

            w = Math.max(w, rect.offsetX + rect.width);
            h = Math.max(h, rect.offsetY + rect.height);
        }

        parent.setDimensions(w, h);
        return widgets;
    }

    @Override
    public <T extends IDataNode> NodeFactory<T> getNodeFactory(ResourceLocation id) {
        //noinspection unchecked
        NodeFactory<T> nodeFactory = (NodeFactory<T>) nodeFactoryMap.get(id);

        return Objects.requireNonNullElseGet(nodeFactory, () -> {
            throw new IllegalStateException(String.format("Failed to construct node - node {%s} was not registered!", id));
        });
    }

    @Override
    public List<Item> getItems(ResourceLocation location) {
        return lootItemMap.getOrDefault(location, List.of());
    }

    @Override
    public List<LootPool> getLootPools(LootTable lootTable) {
        return Services.PLATFORM.getLootPools(lootTable);
    }

    public void printClientInfo() {
        LOGGER.info("Registered {} widgets", widgetMap.size());
        LOGGER.info("Registered {} node factories", nodeFactoryMap.size());
    }
}
