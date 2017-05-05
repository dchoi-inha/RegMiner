package regminer.struct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import regminer.rtree.RTree;
import regminer.util.Util;

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
	
	public double frequency() {
		if (this.size() == 0) return 0;
		
		if (pRoutes.get(0).neighbors() != null) return occurrences();
		else return this.size();
	}
	
	private int size() {
		return pRoutes.size();
	}
	
	private double occurrences() {
		Set<PRoute> allNeighborSet = new HashSet<PRoute>();
		for (PRoute route: pRoutes) {
			allNeighborSet.addAll(route.neighbors().routes);
		}
		
		double sumRatio = 0.0;
		Circle circle = sec();
		for (PRoute neighbor: allNeighborSet) {
			int inCnt = 0;
			for (Visit visit: neighbor.visits) {
				if (circle.contain(visit.place.loc) < 1) inCnt++;
			}
			sumRatio += (double) inCnt / (double) neighbor.length();
		}
		
		return sumRatio;
	}
	
	private RTree rt;
	public double diameter() {
		if (this.rt == null) {
			this.rt = new RTree();
			for (Place p: this.P) {
				this.rt.insert(p);
			}
		}
		
		double dist = 0.0;
		
		
		for (Place p: this.P) {
			dist = Math.max(dist, this.rt.farthestDist(p.loc));
		}
		
		return dist;
	}
	
	public Circle sec() {
		Set<Point> locs = new HashSet<>();
		for (Place place: this.P) {
			locs.add(place.loc);
		}
		return Util.findSec(locs);
	}
		
}
