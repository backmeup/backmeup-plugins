package facebook.main;

import java.io.File;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import facebook.files.ConfLoader;
import facebook.files.PropertyOption;
import facebook.htmlgenerator.HTMLGenerator;
import facebook.storage.SDO;
import facebook.utils.FileUtils;

public class HTMLTester
{
	private HTMLGenerator mainGen;
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
			mainGen = new HTMLGenerator();
			target = new File(props.getProperty(PropertyOption.HTML_DIR.toString()));
			if (!target.exists())
				target.mkdirs();
			FileUtils.exctractFromJar("/facebook/htmlgenerator/css/main.css", new File("" + target + SDO.SLASH + "main.css"), HTMLGenerator.class);
			FileUtils.exctractFromJar("/facebook/htmlgenerator/css/menu.css", new File("" + target + SDO.SLASH + "menu.css"), HTMLGenerator.class);
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

	public static void main(String[] args)
	{
		JUnitCore.runClasses(HTMLTester.class);
	}

}
