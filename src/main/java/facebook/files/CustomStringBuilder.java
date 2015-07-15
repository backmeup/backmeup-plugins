package facebook.files;

public class CustomStringBuilder
{
	private StringBuilder sb;
	private String regex;

	public CustomStringBuilder(String regex)
	{
		if (regex == null)
			regex = "|";
		this.regex = regex;
		sb = new StringBuilder();
	}

	public void append(String string)
	{
		sb.append(string);
		sb.append(getRegex());
	}

	public String getRegex()
	{
		return regex;
	}

	public void empty()
	{
		sb = new StringBuilder();
	}

	@Override
	public String toString()
	{
		/*if (sb.length() > 0)
			sb.delete(sb.length()-1, sb.length());*/
		return sb.toString();
	}
}
