package util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * CSV util
 * @author yuhongyong
 * @date Oct 10, 2013 4:07:11 PM
 */
public class CSVUtils
{
	private static final String	TMP_DIR = System.getProperty("java.io.tmpdir");
	
	/**
	 * 1st row(not parse) is title
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static List<List<String>> parse(InputStream input) throws IOException
	{
		// validate argument
		if (null==input)
			return null;
		
		List<List<String>> result = new LinkedList<List<String>>();
		List<String> lines = IOUtils.readLines(input, "UTF-8");
		for (int i = 0, size = lines.size(); i < size; i++)
		{
			String line = lines.get(i);
			if (i == 0)// 1st row is title,not to parse
				continue;
			if (StringUtils.isBlank(line))
				continue;
			
			List<String> cellsOfRow = new ArrayList<String>();
			line = line.replace("，", ",");
			String[] parts = StringUtils.split(line, ",");
			for (int j = 0, len = parts.length; j < len; j++)
			{
				String cell = parts[j];
				if (cell == null || cell.trim().length() <= 0)
					continue;
				// make the list.size reach current size and fill the list with ""
				if (cellsOfRow.size()<j+1)
				{
					int currentSize = cellsOfRow.size();
					for (int k = 0; k < j+1-currentSize; k++)
						cellsOfRow.add("");
				}
				cellsOfRow.set(j, cell.trim());
			}
			if (!cellsOfRow.isEmpty())
				result.add(cellsOfRow);
		}
		return result;
	}
	
	/**
	 * generate CSV file
	 * 
	 * @param fileName
	 * @param header
	 * @param rows
	 * @return
	 * @throws IOException 
	 */
	public static File makeCSV(String fileName,List<String> header,List<List<String>> rows)
	{
		// validate argument
		if (rows==null||rows.isEmpty())
			return null;
		
		// file path
		String file = TMP_DIR +"/" + fileName+"_"+ DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd-HH-mm-ss") 
				+ ".csv";
		try(OutputStream out = Files.newOutputStream(Paths.get(file), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);)
		{
			// write header
			if (header != null && !header.isEmpty())
			{
				String headerLine = StringUtils.join(header, ",");
				IOUtils.write(headerLine, out, "UTF-8");
			}
			
			// write rows
			for (List<String> row : rows)
			{
				if (row == null || row.isEmpty())
					continue;
				
				String rowLine = StringUtils.join(row, ",");
				IOUtils.write(rowLine, out, "UTF-8");
			}
		}
		catch (Exception e){}
		
		return new File(file);
	}
}