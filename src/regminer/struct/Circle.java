package regminer.struct;

import java.util.ArrayList;

/**
 * Circle.java
 *
 * The Circle class represents a circle in the two-dimensional Cartesian corrdinate space.
 * 
 * Created: Aug 15 2006
 *
 * Author: Xiumin Diao (xiumin@nmsu.edu)
 * 
 */

 
public class Circle  
{
	// The center of a circle
    public Point p;
    // The radius of a circle
    public double r;
    
    // The boundary points of a circle
    public Point bp[];
    

	// Construct a circle without any specification
	public Circle()
    {
		p = new Point(0,0);
		r = 0;
    }
	// Construct a circle with the specified circle
	public Circle(Circle circle)
    {
		p = new Point(circle.p);
		r = circle.r;
    }
	// Construct a circle with the specified center and radius
	public Circle(Point center, double radius)
    {
		p = new Point(center);
		r = radius;
    }
    // Construct a circle based on one point
	public Circle(Point center)    {
		p = new Point(center);
		r = 0;
    }
    // Construct a circle based on two points
    public Circle(Point p1, Point p2)
    {
		p = p1.midPoint(p2);
		r = p1.distance(p);
		bp = new Point[2];
		bp[0] = p1;
		bp[1] = p2;
    }
    // Construct a circle based on three points
    public Circle(Point p1, Point p2, Point p3)
    {
    	
    	double x = (p3.getX()*p3.getX() * (p1.getY()-p2.getY()) + (p1.getX()*p1.getX() + (p1.getY()-p2.getY())*(p1.getY()-p3.getY())) 
    			* (p2.getY()-p3.getY()) + p2.getX()*p2.getX() * (-p1.getY()+p3.getY())) 
    			/ (2 * (p3.getX() * (p1.getY()-p2.getY()) + p1.getX() * (p2.getY()-p3.getY()) + p2.getX() * (-p1.getY()+p3.getY())));

    	double y = 0;
    	if ( p2.getY() != p3.getY() )
    		y = (p2.getY()+p3.getY())/2 - (p3.getX() - p2.getX())/(p3.getY()-p2.getY()) * (x - (p2.getX() + p3.getX())/2);
    	else if ( p3.getY() != p1.getY()) 
    		y = (p1.getY()+p3.getY())/2 - (p3.getX() - p1.getX())/(p3.getY()-p1.getY()) * (x - (p1.getX() + p3.getX())/2);
    	else
    		y = Double.NaN;
    	p = new Point(x, y);
    	r = p.distance(p1);
    	bp = new Point[3];
    	bp[0] = p1;	bp[1] = p2;	bp[2] = p3;

    	// DEBUG
    	if ( Double.isNaN(r) )
    		System.out.println(p.toString() + p1.toString() + p2.toString() + p3.toString());
    	// DEBUG
    	
    }
    
	// Get the center
	public Point getCenter()
	{
		return p;
	}
	// Get the radius
	public double getRadius()
	{
		return r;
	}
	// Set the center
	public void setCenter(Point center)
	{
		p.translate(center);
	}
	// Set the radius
	public void setRadius(double radius)
	{
		r = radius;
	}
	
	// Translate the center of a circle to a specified point
    public void translate(Point newCenter)
    {
		p.translate(newCenter);
    }    
    // Offset a circle along its radius by dr
    public void offset(double dr)
    {
		r += dr;
    }
    // Scale a circle along its radius by a factor
    public void scale(double factor)
    {
		r *= factor;
    }
    // Calculate the diameter of a circle
    public double getDiameter()
    {
		return (2*r);
    }
    // Calculate the circumference of a circle
    public double getCircumference()
    {
		return (Math.PI*2*r);
    }
    // Calcualte the area of a circle
    public double getArea()
    {
		return (Math.PI*Math.pow(r, 2));
    }
	// Is a point in the circle
	public int contain(Point point)
	{
		int answer = 0;
		double d = p.distance(point);
		if (d > r)
		{
			answer = 1;		// The point is outside the circle
		}
		else if (d == r)
		{
			answer = 0;		// The point is on the circumference of the circle
		}
		else
		{
			answer = -1;	// The point is inside the circle
		}
		return answer;
	}
	
	public boolean contain(Circle circle)
	{
		return ( p.distance(circle.getCenter()) + circle.getRadius() <= r );
	}
	
	// Determine whether two points are equal
    public boolean equals(Circle circle)
    {
		return p.equals(circle.p) && (r == circle.r);
    }
    // Return a representation of a point as a string
    public String toString()
    {
		return "(" + p.getX() + ", " + p.getY() + "); " + 2*r;
    }
    
    public ArrayList<Point> getGridPoints(double radius) {
    	ArrayList<Point> gdPoints = new ArrayList<Point>();
    	
    	for ( double x = Math.max((int)Math.ceil(p.getX()-radius), 0); x <= p.getX()+radius ;x++ ) {
    		for ( double y = Math.max((int)Math.ceil(p.getY()-radius), 0); y <= p.getY()+radius ;y++ ) {
    			gdPoints.add(new Point(x, y));
    		}
    	}
    	return gdPoints;
    }
    
    
}
