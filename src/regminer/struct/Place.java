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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Place)) {
			return false;
		}
		Place other = (Place) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
	
	
}
