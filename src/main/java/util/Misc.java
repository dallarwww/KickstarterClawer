package util;

public class Misc
{
	public static int calcPages(int all, int onePage)
	{
		int multiple = all/onePage;
		int delta = all%onePage;
		return (delta != 0)?multiple + 1:multiple;
	}
}
