package regminer.rtree;

import regminer.struct.Place;

/**
 * @author Dong-Wan Choi at SFU, CA
 * @class LEntry, this is the leaf entry class
 * @date 2015-08-26
 *
 */
public class LEntry extends Entry {

	public Place obj;
	
	public LEntry(double xl, double xh, double yl, double yh, Place o) {
		super(xl, xh, yl, yh);
		obj = o;
	}

}
