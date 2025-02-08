package com.yanny.advanced_loot_info.plugin.widget;

import com.yanny.advanced_loot_info.api.IWidget;
import com.yanny.advanced_loot_info.api.Rect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

public class TextureWidget implements IWidget {
    protected final ResourceLocation texture;
    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;
    protected final int u;
    protected final int v;
    protected final int regionWidth;
    protected final int regionHeight;
    protected final int textureWidth;
    protected final int textureHeight;
    private final List<Component> components = new LinkedList<>();
    private final Rect rect;

    public TextureWidget(ResourceLocation texture, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        rect = new Rect(x, y, width, height);
    }

    public TextureWidget(ResourceLocation texture, int x, int y, int width, int height, int u, int v) {
        this(texture, x, y, width, height, u, v, width, height, 256, 256);
    }

    @Override
    public Rect getRect() {
        return rect;
    }

    public void tooltipText(List<Component> components) {
        this.components.addAll(components);
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return components;
    }

    @Override
    public void render(GuiGraphics draw, int mouseX, int mouseY) {
        draw.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }
}
