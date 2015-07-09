package facebook.utils;

public class ConsoleDrawerTester
{

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		System.out.print("esf");
		for(int i = 0; i<=10;i++)
		{
			ConsoleDrawer.drawProgress(10, i,i==0);
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
