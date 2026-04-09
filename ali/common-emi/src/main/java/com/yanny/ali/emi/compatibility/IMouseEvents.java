package com.yanny.ali.emi.compatibility;

public interface IMouseEvents {
    boolean onMouseScrolled(double mouseX, double mouseY, double scrollDeltaY);
    boolean onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
}
