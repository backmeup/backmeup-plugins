/**
 * System depended Operators:
 * provides OS depended information
 * 
 * Helps to provide 
 * @author richard
 * 
 */

package facebook.storage;

public enum SDO
{
	SLASH(System.getProperty("file.separator") + ""),
	NEW_LINE(System.getProperty("line.separator") + "");

	private String operator;

	private SDO(String operator)
	{
		this.operator = operator;
	}

	public String getOperator()
	{
		return operator;
	}

	@Override
	public String toString()
	{
		return getOperator();
	}
}
