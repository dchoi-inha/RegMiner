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
	public PRouteSet pRoutes;
	
	public PRegion(PRouteSet prtSet) {
		P = prtSet.computePOIs();
		S = prtSet.pattern;
		this.pRoutes = prtSet;
	}
	
	public String toString() {
		return "Region" + S.toString() +"(size="+pRoutes.size()+")" ;//+ "\n" + trnsStr();
//		return S.toString() + "(" +trns.size()+" core transitions, " + P.size() + " POIs)";
	}
	
	public String trnsStr() {
		String str = "";
		
		for (PRoute trn: pRoutes) {
			str += trn.toString() + "(density=" + trn.density() + ")\n";
		}
		
		return str;
	}
		
}
