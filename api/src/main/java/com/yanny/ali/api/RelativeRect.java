package com.yanny.ali.api;

public class RelativeRect {
    public static final RelativeRect ZERO = new RelativeRect(0, 0, 0, 0) {
        @Override
        public int getX() {
            return 0;
        }

        @Override
        public int getY() {
            return 0;
        }
    };

    public int offsetX;
    public int offsetY;
    public int width;
    public int height;
    public final RelativeRect parent;

    public RelativeRect(int offsetX, int offsetY) {
        this(offsetX, offsetY, 0, 0);
    }

    public RelativeRect(int offsetX, int offsetY, int width, int height) {
        this(offsetX, offsetY, width, height, ZERO);
    }

    public RelativeRect(int offsetX, int offsetY, int width, int height, RelativeRect parent) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
        this.parent = parent;
    }

    public int getX() {
        return parent.getX() + offsetX;
    }

    public int getY() {
        return parent.getY() + offsetY;
    }

    public int getRight() {
        return getX() + width;
    }

    public int getBottom() {
        return getY() + height;
    }

    public void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean contains(int x, int y) {
        int thisX = getX();
        int thisY = getY();

        return x >= thisX && x < thisX + width && y >= thisY && y < thisY + height;
    }
}
