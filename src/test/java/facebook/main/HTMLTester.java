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
	private int circles = 0;
	private String path;
	private File target;

	@Before
	public void forceJunit()
	{
		if (circles == 0)
		{
			if (!ConfLoader.confExists())
				ConfLoader.genProperties();
			Properties props = ConfLoader.getProperties();
			path = props.getProperty(PropertyOption.DIRECTORY.toString());
			mainGen = new MainGenerator();
			File mainCss = new File("main.css");
			File menuCss = new File("menu.css");
			try
			{
				target = new File(props.getProperty(PropertyOption.HTML_DIR.toString()));
				if (!target.exists())
					target.mkdirs();
				Files.copy(mainCss.toPath(), new FileOutputStream(new File("" + target + "/main.css")));
				Files.copy(menuCss.toPath(), new FileOutputStream(new File("" + target + "/menu.css")));
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		JUnitCore.runClasses(DownloadTester.class);
	}

	@After
	public void after()
	{
		circles++;
	}

	@Test
	public void testGenOverview()
	{
		mainGen.genOverview(target, new File(path));
	}

}
