package org.backmeup.zip.utils;

import org.junit.Assert;
import org.junit.Test;


public class PropertiesUtilTest {

    @Test
    public void testGetExistingProperty() {
        String key = "actionId";
        String expectedValue = "org.backmeup.test.zip";
        String actualValue = PropertiesUtil.getInstance().getProperty(key);
        Assert.assertEquals(expectedValue, actualValue);
    }
    
    @Test
    public void testGetMissingProperty() {
        String key = "id";
        String actualValue = PropertiesUtil.getInstance().getProperty(key);
        Assert.assertNull(actualValue);
    }
}
