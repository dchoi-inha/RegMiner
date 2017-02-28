package regminer.struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import regminer.rtree.MBR;
import regminer.slist.Item;
import regminer.slist.SLlist;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class TSet: the set of transitions of a pattern 'pattern'
 * @date 21 Dec 2016
 *
 */
public class PRouteSet implements Iterable<PRoute>, Comparable<PRouteSet>{
	public Pattern pattern;
	public ArrayList<PRoute> routes;
	
	private double weight;
	
	public SLlist listX;
	public SLlist listY;
	
	public HashSet<PRoute> set;
	
	
	public PRouteSet(Pattern pattern)
	{
		this.pattern = pattern;
		this.routes = new ArrayList<PRoute>();
		this.weight = 0;
		
		this.listX = new SLlist();
		this.listY = new SLlist();
		
		this.set = new HashSet<PRoute>();
	}
	
	public void add(PRoute trn) {
		routes.add(trn);
		weight += trn.weight();
		
		set.add(trn);
	}
	
	
	public PRoute get(int i) {
		return routes.get(i);
	}
	
	public HashSet<Place> computePOIs() {
		HashSet<Place> places = new HashSet<Place>();
		for (PRoute rt: routes) {
			places.addAll(rt.computePOIs());
		}
		return places;
	}
	
//	public void mergeWith(Tset tSet) {
//		trns.addAll(tSet.trns);
//		weight += tSet.weight();
//	}
	
	public boolean contains(PRoute trn) {
		return set.contains(trn);
	}
	
	public int size(){
		return routes.size();
	}
	
	public double weight() {
		return weight;
	}

	public String toString() {
		return pattern.toString() + " : " + routes.toString();
	}
	public String embrStr() {
		return "("+String.format("%1$.3f", this.listX.head.coord)+","+
	String.format("%1$.3f", this.listX.tail.coord)+")" +" "+
				"("+String.format("%1$.3f", this.listY.head.coord)+","+
	String.format("%1$.3f", this.listY.tail.coord)+")";
	}

	@Override
	public Iterator<PRoute> iterator() {
		return routes.iterator();
	}

	public void clear() {
		this.routes.clear();
		this.weight = 0;
		this.set.clear();
		
	}

	public void decWeight(double decWeight) {
		this.weight -= decWeight;		
	}

	public void incWeight(double incWeight) {
		this.weight += incWeight;		
	}

	@Override
	public int compareTo(PRouteSet o) {
		return Double.compare(this.weight(), o.weight());
	}
	
	
	
	public double avgDensity() {
		return routes.stream().mapToDouble(val -> val.density()).average().getAsDouble();
	}
}
