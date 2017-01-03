package regminer.struct;

import java.util.Objects;

import regminer.util.Util;

/**
 * @author Dong-Wan Choi at SFU, CA
 * @class Edge
 * @date Mar 24, 2016
 *
 */
public class Edge {

	public Place src;
	public Place dst;
	
	public Edge (Place src, Place dst)
	{
		this.src = src;
		this.dst = dst;
	}
	
	//TODO: implement this!
	public double distance(Place p)
	{
		return Util.distPointLinesegment(src.loc, dst.loc, p.loc);
	}

	@Override
	public int hashCode() {
		return Objects.hash(src, dst);
	}

	@Override
	public boolean equals(Object obj) {
		Edge e = (Edge) obj;
		return (src.equals(e.src) && dst.equals(e.dst));
	}
	
	public String toString() {
		return "<" + src.toString() + ", " + dst.toString() + ">";
	}
	
	
}
