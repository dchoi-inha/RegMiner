package regminer.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Set;

import regminer.struct.Circle;
import regminer.struct.Place;
import regminer.struct.Point;


public class Util {
	
//	public static Point convertToXY(double width, double height, double lat, double lon) {
//		
//		double x = (lon * height / 180.0) + (height / 2);
//		double y = (lat* width / 360.0) + (width / 2);
//		
//		Point point = new Point(x, y);
//		
//	    return point;
//	}
	
	public static double convertToXY(ArrayList<Place> POIs)
	{
		double minLat = Double.MAX_VALUE;
		double minLon = Double.MAX_VALUE;
		double maxLat = -Double.MAX_VALUE;
		double maxLon = -Double.MAX_VALUE;
		for (Place poi: POIs) {
			minLat = Math.min(minLat, poi.lat);
			minLon = Math.min(minLon, poi.lon);
			maxLat = Math.max(maxLat, poi.lat);
			maxLon = Math.max(maxLon, poi.lon);
		}
		
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		for (Place poi: POIs) {
			double x = getDistLatLon(minLat, minLon, minLat, poi.lon);
			double y = getDistLatLon(minLat, minLon, poi.lat, minLon);
			
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);
			poi.setLoc(x, y);
		}
		
		double coordLen = Math.max(maxX-minX, maxY-minY);
		
		for (Place poi: POIs) {
			double x = (poi.loc.getX() - minX) * Env.MaxCoord / (coordLen);
			double y = (poi.loc.getY() - minY) * Env.MaxCoord / (coordLen);
			poi.setLoc(x, y);
		}
		
		return Env.MaxCoord/coordLen;
		
	}
	private static double getDistLatLon(double lat1, double lon1, double lat2, double lon2) // return distance in kilometers
	{
		double R = 6371.0;
		double dLat = Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(lon2-lon1);
		
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		double a = Math.pow(Math.sin(dLat/2.0),2) + Math.pow(Math.sin(dLon/2.0), 2)*Math.cos(lat1)*Math.cos(lat2); 
		double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1.0-a));
		double d = R * c;
		
		return d;
	}
	
	
	public static double distPointLinesegment(Point p1, Point p2, Point p) {
		double dist;
		
		if (isProjectedPointOnLineSegment(p1, p2, p)) {
			dist =  projectedDist(p1, p2, p);
		}
		else
			dist =  Math.min(p.distance(p2), p.distance(p1));
		
		
		Debug._TestDouble(dist);
		return dist;
	}
	
	public static double projectedDist(Point p1, Point p2, Point p) {
		double dist = p1.distance(p2);
		if (dist <= 0) {
			return Math.min(p.distance(p2), p.distance(p1));
		}
		else {
			double doubleArea = Math.abs((p2.y-p1.y)*p.x - (p2.x-p1.x)*p.y + p2.x*p1.y - p2.y*p1.x);
			return doubleArea/dist;
		}
	}
	
	public static boolean isProjectedPointOnLineSegment(Point v1, Point v2, Point p)
	{
	  Point e1 = new Point(v2.x - v1.x, v2.y - v1.y);
	  double recArea = dotProduct(e1, e1);
	  // dot product of |e1| * |e2|
	  Point e2 = new Point(p.x - v1.x, p.y - v1.y);
	  double val = dotProduct(e1, e2);
	  return (val > 0 && val < recArea);
	}
	
	public static double dotProduct(Point v1, Point v2) {
		return ((v1.x*v2.x) + (v1.y*v2.y));
	}
	
	
	public static long getCpuTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported()?
				bean.getCurrentThreadCpuTime(): 0L;
	}
	
	public static Circle findSec( Set<Point> locs ) 
	{
	    Point[] p = new Point[locs.size()];	// points input by the user
	    int n = locs.size();					// Number of points 
	    Point[] b = new Point[3];				// Points on the boundary of the circle

	    int i = 0;
	    for ( Point loc: locs ) {
	    	p[i++] = new Point(loc.getX(), loc.getY());
	    }
	    
	    return findSec(n, p, 0, b);
	}
	
	// Compute the Smallest Enclosing Circle of the n points in p, 
	// such that the m points in B lie on the boundary of the circle.
	private static Circle findSec(int n, Point[] p, int m, Point[] b)
	{
		Circle sec = new Circle();
		
		// Compute the Smallest Enclosing Circle defined by B
		if(m == 1)
		{
			sec = new Circle(b[0]);
		}
		else if(m == 2)
		{
			sec = new Circle(b[0], b[1]);
		}
		else if(m == 3)
		{
			return new Circle( b[0], b[1], b[2]);
		}
	
		// Check if all the points in p are enclosed
		for(int i=0; i<n; i++)
		{
			if(sec.contain(p[i]) == 1)
			{
				// Compute B <--- B union P[i].
				b[m] = new Point(p[i]);	
				// Recurse
				sec = findSec(i, p, m+1, b);
			}
		}
		
		return sec;
	}
	
	
	public static <T extends Object> T getCopy(T obj) throws IOException, ClassNotFoundException{

		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutStream = new ObjectOutputStream (byteOutStream);
		objectOutStream.writeObject(obj);
		objectOutStream.flush();
		objectOutStream.close();
		byteOutStream.close();
		byte[] byteData = byteOutStream.toByteArray();

		ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
		T copy = (T) new ObjectInputStream(bais).readObject();

		return copy;
	}

}
