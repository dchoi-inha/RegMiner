package regminer.struct;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import regminer.rtree.MBR;
import regminer.util.Env;

public class Visit {

	public Date time;
	public Place place;
	
	// to represent the MBR from this visit to the end of the trajectory
	public MBR embr;
	
	public Visit(String timestamp, String pid) throws ParseException
	{
		DateFormat format = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss", Locale.ENGLISH);
		this.time = format.parse(timestamp);
		this.place = Env.Place_Map.get(pid);
	}
	
	public Visit(String pid) throws ParseException
	{
		this.time = null;
		this.place = Env.Place_Map.get(pid);
	}
	
	public String toString()
	{
		return place.toString();
	}
}
