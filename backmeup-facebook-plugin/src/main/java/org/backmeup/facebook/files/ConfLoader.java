package org.backmeup.facebook.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfLoader {
    public static Properties getProperties() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(new File("properties.xml"))) {
            props.loadFromXML(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    public static boolean reducedInfos() {
        return Boolean.parseBoolean(getProperties().getProperty(PropertyOption.REDUCED_INFOS.toString()));
    }

    public static void genProperties() {
        Properties props = new Properties();
        props.setProperty(PropertyOption.ACCESS_TOKEN.toString(), "yourToken");
        props.setProperty(PropertyOption.DIRECTORY.toString(), "./data-out/.core.xml");
        props.setProperty(PropertyOption.MAX_PHOTOS_PER_ALBUM.toString(), "-1");
        props.setProperty(PropertyOption.SKIP_ALBUMS.toString(), ";");
        props.setProperty(PropertyOption.HTML_DIR.toString(), "./html-out");
        props.setProperty(PropertyOption.REDUCED_INFOS.toString(), Boolean.TRUE.toString());
        
        try (FileOutputStream fos = new FileOutputStream(new File("properties.xml"))) {
            props.storeToXML(fos, "Default properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean confExists() {
        return new File("properties.xml").exists();
    }
}
