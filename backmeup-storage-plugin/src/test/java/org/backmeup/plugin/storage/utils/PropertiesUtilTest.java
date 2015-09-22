package org.backmeup.plugin.storage.utils;

import org.junit.Assert;
import org.junit.Test;


public class PropertiesUtilTest {

    @Test
    public void testGetExistingProperty() {
        String key = "storage.url";
        String expectedValue = "http://example:8080/service/";
        String actualValue = PropertiesUtil.getInstance().getProperty(key);
        Assert.assertEquals(expectedValue, actualValue);
    }
    
    @Test
    public void testGetExistingRecursiveProperty() {
        String key = "storage.downloadBase";
        String expectedValue = "http://example:8080/service/download/";
        String actualValue = PropertiesUtil.getInstance().getProperty(key);
        Assert.assertEquals(expectedValue, actualValue);
    }
    
    @Test
    public void testGetMissingProperty() {
        String key = "url";
        String actualValue = PropertiesUtil.getInstance().getProperty(key);
        Assert.assertNull(actualValue);
    }
}
