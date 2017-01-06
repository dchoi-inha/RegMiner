package regminer.struct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class TSet: the set of transitions of a pattern 'pattern'
 * @date 21 Dec 2016
 *
 */
public class Tset implements Iterable<Transition>{

	public Pattern pattern;
	public ArrayList<Transition> trns;
	
	private double weight;
	
	
	public Tset(Pattern pattern)
	{
		this.pattern = pattern;
		this.trns = new ArrayList<Transition>();
		this.weight = 0;
	}
	
	public void add(Transition trn) {
		trns.add(trn);
		weight += trn.weight();
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
	
	public void mergeWith(Tset tSet) {
		trns.addAll(tSet.trns);
		weight += tSet.weight();
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

	@Override
	public Iterator<Transition> iterator() {
		return trns.iterator();
	}

}
