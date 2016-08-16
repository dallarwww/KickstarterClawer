package kickstarter;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class MailBox
{
	public String address;
	public Project project;

	public MailBox(String address, Project project)
	{
		this.address = address;
		this.project = project;
	}

	public boolean equals(Object o)
	{
		if (o instanceof MailBox)
		{
			MailBox c = (MailBox)o;
			return c.address == this.address;
		}
		return false;
	}
	
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("address", address)
				.append("project", project)
				.toString();
	}
}
