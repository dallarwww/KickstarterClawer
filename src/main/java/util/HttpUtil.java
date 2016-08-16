package util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class HttpUtil
{
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
	
	public static String curl(String url)
	{
		Document doc = null;
		try
		{
			Connection conn = Jsoup.connect(url).userAgent(USER_AGENT);
			conn.timeout(10000);// 10s
			doc = conn.get();
			String html = doc.html();
			return html;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
	
	@Test
	public void main() throws Exception
	{
		String linkClass = "bg-white block border category category-1 clip grey-dark hover-bg-color-art js-category rounded transition-all";
		int start = -1;
		int categoryId = 0;
		for (int i = 0, len = linkClass.length(); i < len; i++)
		{
			char c = linkClass.charAt(i);
			if (Character.isDigit(c))
				start = i;
			else if (start >= 0)
			{
				categoryId = Integer.valueOf(linkClass.substring(start, i));
				break;
			}
				
		}
		
		System.out.println(categoryId);
	}
}
