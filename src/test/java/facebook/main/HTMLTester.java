package facebook.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import facebook.files.ConfLoader;
import facebook.files.PropertyOption;
import facebook.htmlgenerator.MainGenerator;

public class HTMLTester
{
	private MainGenerator mainGen;
	private boolean genConfig = false;
	private int circles = 0;

	@Before
	public void forceJunit()
	{
		if (circles == 0 && genConfig)
			ConfLoader.genProperties();
		Properties props = ConfLoader.getProperties();
		String path = props.getProperty(PropertyOption.DIRECTORY.toString());
		mainGen = new MainGenerator(path);
		MainTester.CURRENT_ACCESSTOKEN = props.getProperty(PropertyOption.ACCESS_TOKEN.toString());
		File mainCss = new File("main.css");
		File menuCss = new File("menu.css");
		try
		{
			File target = new File(path);
			if (!target.exists())
				target.mkdirs();
			Files.copy(mainCss.toPath(), new FileOutputStream(new File("" + target + "/out/main.css")));
			Files.copy(menuCss.toPath(), new FileOutputStream(new File("" + target + "/out/menu.css")));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		JUnitCore.runClasses(MainTester.class);
	}

	@After
	public void after()
	{
		circles++;
	}

	@Test
	public void testGenOverview()
	{
		mainGen.genOverview();
	}

}
