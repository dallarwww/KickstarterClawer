package kickstarter;

public enum TURL
{
	CATEGORIES("分类", "https://www.kickstarter.com/discover?ref=nav"),
	PROJECTS("项目", "https://www.kickstarter.com/discover/advanced?state=live&category_id=%d&woe_id=0&sort=popularity&page=%d"),
	/**	1st parameter is projectId, 2nd parameter is projectName	*/
	COMMENTS("评论", "https://www.kickstarter.com/projects/%d/%s/comments"),
	COMMENTS_MORE("更多评论", "https://www.kickstarter.com/projects/%d/%s/comments?cursor=%d"),
	;
	
	private String url;
	@SuppressWarnings("unused")
	private String name;
	
	private TURL(String name, String url)
	{
		this.name = name;
		this.url = url;
	}
	
	public String url()
	{
		return this.url;
	}
}
