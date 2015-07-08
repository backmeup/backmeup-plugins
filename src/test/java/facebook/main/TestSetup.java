package facebook.main;

import java.util.Properties;

import org.junit.Before;

import facebook.files.ConfLoader;
import facebook.files.PropertyOption;
import facebook.htmlgenerator.MainGenerator;
import facebook.storage.Serializer;

public class TestSetup
{
	private static final boolean GEN_PROPERTIES = true;

	@Before
	public void setUp() throws Exception
	{
		if (GEN_PROPERTIES)
			ConfLoader.genProperties();
		Properties props = ConfLoader.getProperties();
		Serializer.PATH = props.getProperty(PropertyOption.DIRECTORY.toString());
		MainGenerator.DATA_PATH = Serializer.PATH;
	}
}
