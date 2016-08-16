package kickstarter;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.HttpUtil;

public class CommentDigger implements Runnable
{
	Project project;
	KickStarter maker;
	int parsedCommentNum;

	public CommentDigger(Project project, KickStarter maker)
	{
		this.project = project;
		this.maker = maker;
	}

	@Override
	public void run()
	{
		try
		{
			String url = String.format(TURL.COMMENTS.url(), project.id, project.name);
			String html = HttpUtil.curl(url);
			if (StringUtils.isBlank(html))
			{
				KickStarter.LOG.severe("[ERROR]:access url got nothing.URL:"+url);
				return;
			}
			
			parseComments(html);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (maker != null)
			{
				maker.finishedProjectNum.decrementAndGet();
//				System.out.println(rest);
			}
		}
	}
	
	public void parseComments(String html)
	{
		Document doc = Jsoup.parse(html);
		// comments count
		Elements links = doc.select("a[data-content=comments]");
		Element link = links.get(0);
		int commentNum = Integer.valueOf(link.attr("data-comments-count"));
		// get all comments
		Elements comments = doc.select("li[id^=comment-]");
		Element lastComment = comments.last();
		if (lastComment == null)
			return;
		String idStr = lastComment.attr("id");
		if (StringUtils.isBlank(idStr))
			return;
		idStr = StringUtils.substringAfter(idStr, "-");
		if (StringUtils.isBlank(idStr))
			return;
		int lastCommentId = Integer.valueOf(idStr);
		int commentsInPage = comments.size();
		for (Element e : comments)
		{
			if (e == null)
				continue;
			parseComment(e);
		}
		
		parsedCommentNum += commentsInPage;
		while (parsedCommentNum < commentNum)
		{
			String url = String.format(TURL.COMMENTS_MORE.url(), project.id, project.name, lastCommentId);
			String _html = HttpUtil.curl(url);
			parseComments(_html);
		}
	}

	private void parseComment(Element e)
	{
		String html = e.html();
		int index = 0;
		while (index >= 0)
		{
			int pointer = html.indexOf("@", index);
			if (pointer == 0)
			{
				index = 1;
				continue;
			}
			else if (pointer < 0)
				return;
			
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
			MailBox mailBox = new MailBox(mailPrefix+"@"+mailSuffix, project);
			KickStarter.LOG.info(mailBox.toString());
			if (maker != null)
				maker.mails.add(mailBox);
		}
	}
}
