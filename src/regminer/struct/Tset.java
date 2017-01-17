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
public class Tset implements Iterable<Transition> {
	public Pattern pattern;
	public ArrayList<Transition> trns;
	
	private double weight;
	
	public SLlist listX;
	public SLlist listY;
	
	public HashSet<Transition> set;
	
	
	public Tset(Pattern pattern)
	{
		this.pattern = pattern;
		this.trns = new ArrayList<Transition>();
		this.weight = 0;
		
		this.listX = new SLlist();
		this.listY = new SLlist();
		
		this.set = new HashSet<Transition>();
	}
	
	public void add(Transition trn) {
		trns.add(trn);
		weight += trn.weight();
		
		set.add(trn);
	}
	
	
	public Transition get(int i) {
		return trns.get(i);
	}
	
	public HashSet<Place> computePOIs() {
		HashSet<Place> places = new HashSet<Place>();
		for (Transition trn: trns) {
			places.addAll(trn.computePOIs());
		}
		return places;
	}
	
//	public void mergeWith(Tset tSet) {
//		trns.addAll(tSet.trns);
//		weight += tSet.weight();
//	}
	
	public boolean contains(Transition trn) {
		return set.contains(trn);
	}
	
	public int size(){
		return trns.size();
	}
	
	public double weight() {
		return weight;
	}

	public String toString() {
		return pattern.toString() + " : " + trns.toString();
	}
	public String embrStr() {
		return "("+String.format("%1$.3f", this.listX.head.coord)+","+
	String.format("%1$.3f", this.listX.tail.coord)+")" +" "+
				"("+String.format("%1$.3f", this.listY.head.coord)+","+
	String.format("%1$.3f", this.listY.tail.coord)+")";
	}

	@Override
	public Iterator<Transition> iterator() {
		return trns.iterator();
	}

	public void clear() {
		this.trns.clear();
		this.weight = 0;
		this.set.clear();
		
	}

	public void decWeight(double decWeight) {
		this.weight -= decWeight;		
	}

	public void incWeight(double incWeight) {
		this.weight += incWeight;		
	}
	
	
	
}
