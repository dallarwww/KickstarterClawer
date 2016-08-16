package main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kickstarter.CommentDigger;
import kickstarter.KickStarter;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import util.HttpUtil;

/**
 * 
 * @author yuhongyong
 * @date Aug 13, 2016 4:36:30 PM
 */
public class Clawer
{
	public static final ExecutorService POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
	
	public static void main(String[] args)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				new KickStarter();
				Clawer.POOL.shutdown();
			}
		}.start();
	}
	
	public static String curl(String url)
	{
		Document doc = null;
		try
		{
			doc = Jsoup.connect(url).get();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// get all links
		Elements links = doc.select("a[href]");
		for (Element link : links)
		{
			// get the value from href attribute
			System.out.println("\nlink : " + link.attr("href"));
			System.out.println("text : " + link.text());

		}
		
		return "";
	}

	@Test
	public void main() throws Exception
	{
		String url = "https://www.kickstarter.com/projects/1852466551/shiba-inu-plush/comments";
		String html = HttpUtil.curl(url);
		if (StringUtils.isBlank(html))
		{
			KickStarter.LOG.severe("[ERROR]:access url got nothing.URL:"+url);
			return;
		}
		CommentDigger digger = new CommentDigger(null, null);
		digger.parseComments(html);
	}
	
	@Test
	public void parseMail() throws Exception
	{
		String html = "ladfjlasj,123@qq.com,456@eclipse.org 789@gmail.com";
		int index = 0;
		while (index >= 0)
		{
			int pointer = html.indexOf("@", index);
			if (pointer == 0)
			{
				index = 1;
				continue;
			}
			index = pointer;
			String mailPrefix = "";
			// pointer backward until whitespace
			for (int i = pointer-1; i >= 0; i--)
			{
				char c = html.charAt(i);
				if (Character.isWhitespace(c) || c==',')
				{
					mailPrefix = html.substring(i+1, pointer);
					break;
				}
			}
			String mailSuffix = "";
			// pointer forward until whitespace
			for (int i = pointer+1, len = html.length(); i < len; i++)
			{
				char c = html.charAt(i);
				if (Character.isWhitespace(c) || c==',')
				{
					mailSuffix = html.substring(pointer + 1, i);
					index = i;
					break;
				}
				if (i == len - 1)
				{
					mailSuffix = html.substring(pointer + 1);
					index = -1;
				}
			}
			System.out.println(mailPrefix+"@"+mailSuffix);
		}
	}
}
