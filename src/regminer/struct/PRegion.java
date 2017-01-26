package regminer.struct;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class PRegion
 * @date 21 Dec 2016
 *
 */
public class PRegion {
	public HashSet<Place> P;
	public Pattern S;
	public Tset trns;
	
	public PRegion(Tset trns) {
		P = trns.computePOIs();
		S = trns.pattern;
		this.trns = trns;
	}
	
	public String toString() {
		return "Region" + S.toString() +"(size="+trns.size()+")" ;//+ "\n" + trnsStr();
//		return S.toString() + "(" +trns.size()+" core transitions, " + P.size() + " POIs)";
	}
	
	public String trnsStr() {
		String str = "";
		
		for (Transition trn: trns) {
			str += trn.toString() + "(density=" + trn.density() + ")\n";
		}
		
		return str;
	}
		
}
