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
	public double sumRatio;
	
	public NeighborTset(Pattern pattern) {
		super(pattern);
		ratios = new ArrayList<Double>();
		sumRatio = 0.0;
	}

	public void add(Transition trn, double ratio) {
		super.add(trn);
		ratios.add(ratio);
		sumRatio += ratio;
	}
	
	public void mergeWith(NeighborTset nTset) {
		super.mergeWith(nTset);
		ratios.addAll(nTset.ratios);
		sumRatio += nTset.sumRatio;
	}
	
	
}
