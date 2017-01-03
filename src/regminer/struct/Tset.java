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
	public HashSet<Place> places;
	
	
	public Tset(Pattern pattern)
	{
		this.pattern = pattern;
		this.trns = new ArrayList<Transition>();
		this.places = new HashSet<Place>();
	}
	
	public void add(Transition trn) {
		trns.add(trn);
		places.addAll(trn.computePOIs());
	}
	
	public Transition get(int i) {
		return trns.get(i);
	}
	
	public void mergeWith(Tset tSet) {
		trns.addAll(tSet.trns);
		places.addAll(tSet.places);
	}
	
	public int size(){
		return trns.size();
	}

	public String toString() {
		return pattern.toString() + " : " + trns.toString();
	}

	@Override
	public Iterator<Transition> iterator() {
		return trns.iterator();
	}

}
