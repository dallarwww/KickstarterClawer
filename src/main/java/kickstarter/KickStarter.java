package kickstarter;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import main.Clawer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.HttpUtil;
import util.Misc;

public class KickStarter
{
	public static final Logger LOG = Logger.getLogger(KickStarter.class.getName());
	
	List<Category> categories = new ArrayList<>();
	List<Project> projects = new ArrayList<>();
	
	AtomicInteger finishedProjectNum = new AtomicInteger();
	ConcurrentLinkedQueue<MailBox> mails = new ConcurrentLinkedQueue<>();
	
	public static final int PROJECTS_ONE_PAGE = 20;
	public static final int COMMENTS_ONE_PAGE = 50;
	
	public KickStarter()
	{
		LOG.info("====START KickStarter====");
		// search categories
		searchCategories();
		// search projects 
		searchProjects();
		
		while (finishedProjectNum.get() > 0)
		{
			Thread.yield();
		}
		// write mail
		writeMails();
		LOG.info("====END KickStarter====");
	}
	
	private void searchCategories()
	{
		// FIXME TEST CODE
//		Category category = new Category("dance", 6, 27);
//		categories.add(category);
//		finishedProjectNum.addAndGet(26);
		
		String url = TURL.CATEGORIES.url();
		String html = HttpUtil.curl(url);
		Document doc = Jsoup.parse(html);
		categories.addAll(parseCategories(doc));
	}
	
	private List<Category> parseCategories(Document doc)
	{
		List<Category> result = new ArrayList<>();
		Elements rows = doc.select("div[class=row]");
		// traverse div[class=row] elements
		for (Element row : rows)
		{
			Elements categories = row.select("div[class^=category-container]");
			for (Element categoryHtml : categories)
			{
				if (categoryHtml == null)
					continue;
				
				result.add(parseCategory(categoryHtml));
			}
		}
		
		return result;
	}
	
	private Category parseCategory(Element categoryHtml)
	{
		Elements links = categoryHtml.select("a[href^=/discover/categories/]");
		Element link = links.get(0);
		// category id
		String linkClass = link.attr("class");
		int start = -1;
		int categoryId = 0;
		for (int i = 0, len = linkClass.length(); i < len; i++)
		{
			char c = linkClass.charAt(i);
			if (Character.isDigit(c))
			{
				if (start == -1)
					start = i;
			}
			else if (start >= 0)
			{
				categoryId = Integer.valueOf(linkClass.substring(start, i));
				break;
			}
		}
		// category name
		String href = link.attr("href");
		String[] parts = href.split("/");
		String name = StringUtils.substringBefore(parts[3], "?");
		// category projectsNum
		String html = categoryHtml.html();
		int index = html.indexOf(" live projects");
		start = -1;
		int projects = 0;
		for (int i = index-1; i > 0; i--)
		{
			char c = html.charAt(i);
			if (Character.isDigit(c))
				start = i;
			else if (start >= 0)
			{
				projects = Integer.valueOf(html.substring(i+1, index));
				break;
			}
		}
		Category category = new Category(name, categoryId, projects);
		finishedProjectNum.addAndGet(projects-1);
		LOG.info(category.toString());
		return category;
	}
	
	private void searchProjects()
	{
		for (Category category : categories)
		{
			if (category == null)
				continue;
			List<Project> _projects = searchProjects(category);
			if (_projects.isEmpty())
				continue;
			
			projects.addAll(_projects);
		}
	}
	
	private List<Project> searchProjects(Category category)
	{
		List<Project> result = new ArrayList<>();
		
		int pages = Misc.calcPages(category.projects, PROJECTS_ONE_PAGE);
		for (int i = 0, len = pages; i < len; i++)
		{
			int pageNo = i+1;
			String url = String.format(TURL.PROJECTS.url(), category.id, pageNo);
			String html = HttpUtil.curl(url);
			if (StringUtils.isBlank(html))
			{
				LOG.severe("access url got nothing.URL:"+url);
				continue;
			}
			List<Project> _projects = null;
			try
			{
				_projects = parseProjects(category, html);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (_projects == null || _projects.isEmpty())
				continue;
			
			result.addAll(_projects);
			LOG.fine("category:"+category+" pageNo:"+pageNo+" projects:"+_projects.size()+" url:"+url);
			
		}
		LOG.fine("search projects:"+result.size()+" by "+category);
		return result;
	}
	
	private List<Project> parseProjects(Category category, String html)throws Exception
	{
		List<Project> result = new ArrayList<>();
		
		Document doc = Jsoup.parse(html);
		Element element = doc.getElementById("projects_list");
		// get all projects in this page
		Elements rows = element.children();
		// traverse div[class=row] elements
		for (Element row : rows)
		{
			Elements projects = row.children();
			for (Element projectHtml : projects)
			{
				if (projectHtml == null)
					continue;
				Project project = parseProject(category, projectHtml);
				result.add(project);
				// search comments
				Clawer.POOL.submit(new CommentDigger(project, this));
			}
		}
		return result;
	}
	
	private Project parseProject(Category category, Element e)throws Exception
	{
		Elements links = e.select("a[class=project-thumbnail-wrap]");
		Element link = links.get(0);
		int projectId = Integer.valueOf(link.attr("data-pid"));
		String href = link.attr("href");
		String[] parts = href.split("/");
		String name = StringUtils.substringBefore(parts[3], "?");
		
		Project project = new Project(category, projectId, name);
		LOG.info(project.toString());
		return project;
	}
	
	private void writeMails()
	{
		String timestamp = DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd_HHmmss");
		try(FileOutputStream out = new FileOutputStream("kickstarter_mail_"+timestamp+".txt");)
		{
			for (MailBox mailBox : mails)
			{
				if (mailBox == null)
					continue;
				IOUtils.write(mailBox.toString()+"\n", out, "UTF-8");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
