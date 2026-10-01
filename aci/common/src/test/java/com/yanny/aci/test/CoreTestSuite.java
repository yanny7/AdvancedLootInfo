package com.yanny.aci.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        CoreConfigUtilsTest.class,
        NodeCodecTest.class,
        NumberExprTest.class,
        NumberTooltipTest.class
})
public class CoreTestSuite {
}
