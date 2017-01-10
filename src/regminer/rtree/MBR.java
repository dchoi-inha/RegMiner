package regminer.rtree;


import regminer.struct.Point;
import regminer.util.Env;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class MBR
 * @date 3 Jan 2017
 *
 */
public class MBR {
	public Pair x;
	public Pair y;
	
	public MBR()
	{
		this.x = new Pair();
		this.y = new Pair();
		this.x.l = Double.MAX_VALUE;
		this.y.l = Double.MAX_VALUE;
		this.x.h = Double.MIN_VALUE;
		this.y.h = Double.MIN_VALUE;
	}
	
	public MBR(double xl, double xh, double yl, double yh) {
		this.x = new Pair();
		this.y = new Pair();
		this.x.l = xl;
		this.x.h = xh;
		this.y.l = yl;
		this.y.h = yh;
	}
	
	public void updateMBR(Point p) {

		this.x.l = Math.min(this.x.l, p.x);
		this.y.l = Math.min(this.y.l, p.y);
		this.x.h = Math.max(this.x.h, p.x);
		this.y.h = Math.max(this.y.h, p.y);
	}

	public void enlarge(double ep) {
		this.x.l = Math.max(0, this.x.l-ep);
		this.y.l = Math.max(0, this.y.l-ep);
		this.x.h = Math.min(Env.MaxCoord, this.x.h + ep);
		this.y.h = Math.min(Env.MaxCoord, this.y.h + ep);
	}
	
	public String toString() {
		return "x["	+String.format("%1$.3f", x.l)+","+String.format("%1$.3f", x.h)+"] y["
					+String.format("%1$.3f", y.l)+","+String.format("%1$.3f", y.h)+"]";	

	}
	
}
