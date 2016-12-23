package regminer.struct;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import regminer.util.Env;

public class Visit {

	public Date time;
	public Place place;
	
	public Visit(String timestamp, String pid) throws ParseException
	{
		DateFormat format = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss", Locale.ENGLISH);
		this.time = format.parse(timestamp);
		this.place = Env.Place_Map.get(pid);
	}
	
	public String toString()
	{
		return place.toString();
	}
}
