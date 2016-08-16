package kickstarter;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Project
{
	public Category category;
	public int id;
	public String name;
	
	public Project(Category category, int id, String name)
	{
		super();
		this.category = category;
		this.id = id;
		this.name = name;
	}

	public boolean equals(Object o)
	{
		if (o instanceof Project)
		{
			Project c = (Project)o;
			return (c.id == this.id && c.category.equals(this.category));
		}
		return false;
	}
	
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("name", name)
				.append("category", category)
				.toString();
	}
}
