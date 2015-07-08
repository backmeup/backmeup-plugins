package facebook.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import facebook.files.ConfLoader;
import facebook.files.PropertyOption;
import facebook.htmlgenerator.MainGenerator;
import facebook.storage.Serializer;

public class HTMLTester
{
	@Before
	public void forceJunit()
	{
		Properties props = ConfLoader.getProperties();
		Serializer.PATH = props.getProperty(PropertyOption.DIRECTORY.toString());
		MainGenerator.DATA_PATH = Serializer.PATH;
		MainTester.CURRENT_ACCESSTOKEN = props.getProperty(PropertyOption.ACCESS_TOKEN.toString());
		File mainCss = new File("main.css");
		File menuCss = new File("menu.css");
		try
		{
			File target = new File(Serializer.PATH);
			if (!target.exists())
				target.mkdirs();
			Files.copy(mainCss.toPath(), new FileOutputStream(new File("" + target + "/main.css")));
			Files.copy(menuCss.toPath(), new FileOutputStream(new File("" + target + "/menu.css")));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		JUnitCore.runClasses(MainTester.class);
		System.out.println(Serializer.PATH);
	}

	@Test
	public void testGenOverview()
	{
		MainGenerator.genOverview();
	}

}
