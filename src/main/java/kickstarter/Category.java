package kickstarter;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Category
{
	public String name ="";
	public int id = 0;
	public int projects = 0;
	
	public Category(String name, int id, int projects)
	{
		this.name = name;
		this.id = id;
		this.projects = projects;
	}

	public boolean equals(Object o)
	{
		if (o instanceof Category)
		{
			Category c = (Category)o;
			return c.id == this.id;
		}
		return false;
	}
	
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("name", name)
				.append("projects", projects)
				.toString();
	}
}
