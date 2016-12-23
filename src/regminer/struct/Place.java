package regminer.struct;


/**
 * @author Dong-Wan Choi at SFU, CA
 * @class Place
 * @date Mar 23, 2016
 *
 */
public class Place {

	public String id;
	public double lat;
	public double lon;
	public Point loc;
	public String category;
	
	public Place (String id, double lat, double lon, String category)
	{
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.category = category;
	}
	public void setLoc(double x, double y)
	{
		if (this.loc != null) {
			this.loc.setX(x);
			this.loc.setY(y);
		}
		else this.loc = new Point(x, y);
	}
	
	
	
	public String toString()
	{
		return id+"("+category+")"+loc.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || this == null) return false;
		else return id.equals(((Place)obj).id);
	}
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
