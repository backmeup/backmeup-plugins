package facebook.utils;

public class Mathematics
{
	public static double roundDouble(double number, int digitAfterKomma)
	{
		double komma = number - (int) number;
		double cutaway = ((int) ((komma + 5 / Math.pow(10, digitAfterKomma + 1)) * Math.pow(10, digitAfterKomma))) / Math.pow(10, digitAfterKomma);
		return ((int) number) + cutaway;
	}
}
