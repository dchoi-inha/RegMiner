package regminer.struct;

import java.util.ArrayList;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class NeighborTset
 * @date 3 Jan 2017
 *
 */
public class NeighborTset extends Tset {

//	public ArrayList<Double> ratios;
	private double sumRatios;
	
	public NeighborTset(Pattern pattern) {
		super(pattern);
//		ratios = new ArrayList<Double>();
		sumRatios = 0.0;
	}

	public void add(Transition trn, double ratio) {
		super.add(trn);
//		ratios.add(ratio);
		sumRatios += ratio*trn.weight();
	}
	
//	public void mergeWith(NeighborTset nTset) {
//		super.mergeWith(nTset);
////		ratios.addAll(nTset.ratios);
//	}
	
	public double sumRatios() {
		return sumRatios;
	}
	
	public String toString() {
		String str = super.pattern + "(size=" + super.size() + "  weight=" + super.weight()+ "  density=" + sumRatios + ")";
//		str += strTrns();
		return  str;
	}
	
	public String strTrns() {
		String str = "";
		for (int i=0; i < super.trns.size(); i++) {
			Transition trn = super.trns.get(i);
//			double ratio = this.ratios.get(i);
//			str += "\n" + trn.toString() +" ratio:" + ratio;
		}
		return str;
	}
	
}
