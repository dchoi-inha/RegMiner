package regminer.struct;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import regminer.rtree.MBR;
import regminer.util.Env;

public class Visit {

	public double timestamp;
	public Place place;

	// to represent the MBR from this visit to the end of the trajectory
	public MBR embr;

	public Visit(String timestamp, String pid) throws ParseException 
	{
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		DateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH); // 07-Apr-2012 23:33:08
		DateFormat format3 = new SimpleDateFormat("EE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);// Wed Jan 09 18:03:42 2013

		if (timestamp.matches("([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})"))
			this.timestamp = format1.parse(timestamp).getTime()/(1000*60);
		else if (timestamp.matches("([0-9]{2})-([a-zA-Z]{3})-([0-9]{4}) ([0-9]{2}):([0-9]{2}):([0-9]{2})"))
			this.timestamp = format2.parse(timestamp).getTime()/(1000*60);
		else
			this.timestamp = format3.parse(timestamp).getTime()/(1000*60);
		this.place = Env.Place_Map.get(pid);
	}

	public String toString()
	{
		return place.toString();
	}
}
