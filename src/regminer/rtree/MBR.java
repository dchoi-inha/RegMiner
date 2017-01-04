package regminer.rtree;

import regminer.struct.Point;

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
	
	public void updateMBR(Point p) {

		this.x.l = Math.min(this.x.l, p.x);
		this.y.l = Math.min(this.y.l, p.y);
		this.x.h = Math.max(this.x.h, p.x);
		this.y.h = Math.max(this.y.h, p.y);
	}

	public void enlarge(double ep) {
		this.x.l -= ep;
		this.y.l -= ep;
		this.x.h += ep;
		this.y.h += ep;
	}
}
