package regminer.struct;

import java.util.ArrayList;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class NeighborTset
 * @date 3 Jan 2017
 *
 */
public class NeighborTset extends Tset {

	public ArrayList<Double> ratios;
	private double density;
	
	public NeighborTset(Pattern pattern) {
		super(pattern);
		ratios = new ArrayList<Double>();
		density = 0.0;
	}

	public void add(Transition trn, double ratio) {
		super.add(trn);
		ratios.add(ratio);
		density += ratio*trn.weight();
	}
	
	public void mergeWith(NeighborTset nTset) {
		super.mergeWith(nTset);
		ratios.addAll(nTset.ratios);
		density += nTset.density;
	}
	
	public double density() {
		return density;
	}
	
	public String toString() {
		return super.pattern + "(size=" + super.size() + "  weight=" + super.weight()+ "  density=" + density + ")"; 
	}
	
}
