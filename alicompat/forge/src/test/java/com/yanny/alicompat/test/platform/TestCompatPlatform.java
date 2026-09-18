package com.yanny.alicompat.test.platform;

import com.yanny.alicompat.platform.ICompatPlatform;

public class TestCompatPlatform implements ICompatPlatform {
    @Override
    public boolean isModLoaded(String modId) {
        return true;
    }
}
