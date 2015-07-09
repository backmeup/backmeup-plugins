package facebook.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfLoader
{
	public static Properties getProperties()
	{
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(new File("properties.xml")))
		{
			props.loadFromXML(fis);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return props;
	}

	public static void genProperties()
	{
		Properties props = new Properties();
		props.put(PropertyOption.ACCESS_TOKEN.toString(), "yourToken");
		props.put(PropertyOption.DIRECTORY.toString(), System.getProperty("user.home") + "/bme-fb-output");
		props.put(PropertyOption.MAX_PHOTOS_PER_ALBUM.toString(), "-1");
		props.put(PropertyOption.SKIP_ALBUMS.toString(), ";");
		try (FileOutputStream fos = new FileOutputStream(new File("properties.xml")))
		{
			props.storeToXML(fos, "Default properties");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
