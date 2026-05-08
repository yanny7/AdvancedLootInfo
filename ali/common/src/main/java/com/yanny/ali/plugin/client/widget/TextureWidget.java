package com.yanny.ali.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.plugin.client.WidgetUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class TextureWidget implements IWidget {
    protected final ResourceLocation texture;
    protected final RelativeRect rect;
    protected final int u;
    protected final int v;
    protected final int regionWidth;
    protected final int regionHeight;
    protected final int textureWidth;
    protected final int textureHeight;
    private final HolderLookup.Provider provider;
    private final List<Component> components = new LinkedList<>();

    public TextureWidget(HolderLookup.Provider provider, ResourceLocation texture, RelativeRect rect, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        this.provider = provider;
        this.texture = texture;
        this.rect = rect;
        this.u = u;
        this.v = v;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        rect.setDimensions(rect.getWidth(), rect.getHeight());
    }

    public TextureWidget(HolderLookup.Provider provider, ResourceLocation texture, RelativeRect rect, int u, int v) {
        this(provider, texture, rect, u, v, rect.getWidth(), rect.getHeight(), 256, 256);
    }

    @NotNull
    @Override
    public RelativeRect getRect() {
        return rect;
    }

    @NotNull
    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.VERTICAL;
    }

    public void tooltipText(TooltipNode tooltip) {
        this.components.addAll(CoreTooltipUtils.toComponents(provider, tooltip, 0, Minecraft.getInstance().options.advancedItemTooltips));
    }

    @NotNull
    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return components;
    }

    @Override
    public void render(GuiGraphics draw, int mouseX, int mouseY) {
        draw.blit(RenderPipelines.GUI_TEXTURED, texture, rect.getX(), rect.getY(), u, v, rect.getWidth(), rect.getHeight(), textureWidth, textureHeight);
    }
}
