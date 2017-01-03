package regminer.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;

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
	
	public static void convertToXY(ArrayList<Place> POIs)
	{
		double minLat = Double.MAX_VALUE;
		double minLon = Double.MAX_VALUE;
		for (Place poi: POIs) {
			minLat = Math.min(minLat, poi.lat);
			minLon = Math.min(minLon, poi.lon);
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
		
		for (Place poi: POIs) {
			double x = (poi.loc.getX() - minX) * Env.MaxCoord / (maxX-minX);
			double y = (poi.loc.getY() - minY) * Env.MaxCoord / (maxY-minY);
			poi.setLoc(x, y);
		}
		
	}
	private static double getDistLatLon(double lat1, double lon1, double lat2, double lon2)
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
		double dist = p1.distance(p2);
		if (dist > 0) {
		
			double doubleArea = Math.abs((p2.y-p1.y)*p.x - (p2.x-p1.x)*p.y + p2.x*p1.y - p2.y*p1.x);
			dist = doubleArea/dist;

			dist = Math.min(dist, p.distance(p1));
			dist = Math.min(dist, p.distance(p2));
		}
		
		Debug._TestDouble(dist);
		return dist;
	}
	
	
	public static long getCpuTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported()?
				bean.getCurrentThreadCpuTime(): 0L;
	}

}
