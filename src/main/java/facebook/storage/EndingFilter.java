package facebook.storage;

import java.io.File;
import java.io.FilenameFilter;

public class EndingFilter implements FilenameFilter
{
	private String ending;

	public EndingFilter(String ending)
	{
		setEnding(ending);
	}

	@Override
	public boolean accept(File dir, String name)
	{
		if (name.length() < ending.length() + 1)
			return false;
		String endingWithDot = "." + getEnding();
		if (name.substring(name.length() - endingWithDot.length(), name.length()).equalsIgnoreCase(endingWithDot))
			return true;
		return false;
	}

	public String getEnding()
	{
		return ending;
	}

	public void setEnding(String ending)
	{
		this.ending = ending;
	}

}
