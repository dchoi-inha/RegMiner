package regminer.rtree;

import java.util.Comparator;

import regminer.util.Env;
import regminer.struct.Point;
import regminer.util.Debug;


public class Entry {
	public Pair x;
	public Pair y;
	public Node child;
	
	public double dist;
	
	public Entry(){
		x = new Pair();
		y = new Pair();
	}
	
	public Entry(double xl, double xh, double yl, double yh){
		x = new Pair();
		y = new Pair();
		x.l = xl;
		x.h = xh;
		y.l = yl;
		y.h = yh;
	}
	
	public boolean contains(Point p) {
		return (x.l <= p.x && x.h >= p.x && y.l <= p.y && y.h >= p.y);
	}
	public double distTo(Point p) { // mindist from p to mbr
		double dxl = x.l - p.x;
		double dyl = y.l - p.y;
		double dxh = x.h - p.x;
		double dyh = y.h - p.y;
		if (dxl == dxh && dyl == dyh) // if this is a point
			return Math.sqrt(Math.pow(dxl, 2) + Math.pow(dyl, 2));
		
		if (dxl > 0) {
			if (dyl > 0) return Math.sqrt(Math.pow(dxl, 2) + Math.pow(dyl, 2));
			else if (dyl <= 0 && dyh > 0) return Math.abs(dxl);
			else if (dyh <= 0) return Math.sqrt(Math.pow(dxl, 2) + Math.pow(dyh, 2));
		}
		else if (dxl <= 0 && dxh > 0) {
			if (dyl > 0) return Math.abs(dyl);
			else if (dyl <= 0 && dyh > 0) return 0;
			else if (dyh <= 0) return Math.abs(dyh);
		}
		else if (dxh <= 0) {
			if (dyl > 0) return Math.sqrt(Math.pow(dxh, 2) + Math.pow(dyl, 2));
			else if (dyl <= 0 && dyh > 0) return Math.abs(dxh);
			else if (dyh <= 0) return Math.sqrt(Math.pow(dxh, 2) + Math.pow(dyh, 2));
		}
		Debug._Error(this, "distTo function: No way, you cannot come here....");
		return -1;
	}
	
	public double area(){    
		return (x.h-x.l)*(y.h-y.l);
	}
	
	public double diffArea(Entry e){
		return child.diffArea(e);
	}
	public double overlap(Entry e){
		double xl, xh, yl, yh;
		
		xl = Math.max(x.l, e.x.l);
		xh = Math.min(x.h, e.x.h);
		yl = Math.max(y.l, e.y.l);
		yh = Math.min(y.h, e.y.h);
		
		if (xl > xh || yl > yh) return 0;
		else return (xh-xl)*(yh-yl);
		
	}
	public void adjust(){  
		double xl=Env.MaxCoord,xh=0,yl=Env.MaxCoord,yh=0;
		for (int i = 0; i<child.size(); i++){
			Entry e = (Entry) child.get(i);
			if (e.x.l<xl) xl = e.x.l;
			if (e.x.h>xh) xh = e.x.h;
			if (e.y.l<yl) yl = e.y.l;
			if (e.y.h>yh) yh = e.y.h;
		}
		x.l = xl;
		x.h = xh;
		y.l = yl;
		y.h = yh;

		child.x.l = xl;
		child.x.h = xh;
		child.y.l = yl;
		child.y.h = yh;
	}
	
	public String toString()
	{
		if (child != null)
			return "("+ this.x.l + "," + this.x.h + ")" + "("+ this.y.l + "," + this.y.h + ")";
		else
			return "("+ this.x.l + "," + this.y.l + ")";
	}
	
	public static Comparator<Entry> CompareX = new Comparator<Entry>() {
		public int compare(Entry e1, Entry e2) {
			if (e1.x.l == e2.x.l && e1.x.h != e2.x.h) return (e1.x.h - e2.x.h > 0? 1 : -1);
			else if (e1.x.l != e2.x.l) return (e1.x.l - e2.x.l > 0? 1: -1);
			else return 0;
		}
	};
	public static Comparator<Entry> CompareY = new Comparator<Entry>() {
		public int compare(Entry e1, Entry e2) {
			if (e1.y.l == e2.y.l && e1.y.h != e2.y.h) return (e1.y.h - e2.y.h > 0? 1 : -1);
			else if (e1.y.l != e2.y.l) return (e1.y.l - e2.y.l > 0? 1: -1);
			else return 0;
		}
	};
	public static Comparator<Entry> CompareDist = new Comparator<Entry>() {
		public int compare(Entry e1, Entry e2) {
			return (e1.dist > e2.dist ? 1: -1);
		}
	};
	
	public static int size = 8*4+4; // double * 4 + integer * 1
}
