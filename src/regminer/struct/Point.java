package regminer.struct;


public class Point implements Comparable<Point> {
		
	public double x; 
	public double y;
	
	public Point ( int x, int y ) {
		this.x = x;
		this.y = y;
	}

	// Construct a point with the same location as the specified point
	public Point(Point point) 
	{
		x = point.x;
		y = point.y;
	}

	public Point (double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}



	public String toString() {
//		return ""+x+"\t"+y+"";
		return "( "+String.format("%1$.3f", x)+", "+String.format("%1$.3f",y)+ " )";
	}

	@Override
	public int compareTo(Point o) {
		int xDiff = (int)(this.x-o.x);
		int yDiff = (int)(this.y-o.y)*-1;

		if ( yDiff == 0 ) return xDiff;
		else return yDiff;
	}

	public boolean equals(Point p) {
		return (this.x == p.x && this.y == p.y);
	}
	
	public int hashCode() {
		return ((int)x * 31) ^ (int)y;
	}
	 
	 public boolean equals(Object o) {
		 if (o instanceof Point) {
			 Point other = (Point) o;
			 return (x == other.x && y == other.y);
		 }
		 return false;
	 }

	public double distance(Point point)
	{
		double dx = x - point.x;
		double dy = y - point.y;
		
		return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
	}

	
	
	public Point midPoint(Point p2) {
		return new Point((x+p2.x)/2, (y+p2.y)/2);
	}
	
    // Translate a point to the specified location
    public void translate(Point point)
    {
		translate(point.x, point.y);
    }
    // Translate a point to the specified location (newX, newY)
    public void translate(double newX, double newY)
    {
		x = newX;
		y = newY;
    }

}
