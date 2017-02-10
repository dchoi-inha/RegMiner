package regminer.struct;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import regminer.rtree.MBR;
import regminer.util.Env;
import regminer.util.Util;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class Transition
 * @date 21 Dec 2016
 *
 */
public class Transition implements Iterable<Visit>, Comparable<Transition>{
	public Trajectory traj;
	public List<Visit> visits;
	public Pattern pattern;
	public int s, e; // [s:e]
	
	private double density;
	private NeighborTset neighbors;
	
	private MBR mbr;
	private MBR embr;
	
	private double distSum;
	

	public Transition(Trajectory traj, Pattern pattern, int s, int e)
	{
		this.traj = traj;
		this.visits = traj.visits.subList(s, e+1);
		this.pattern = pattern;
		this.s = s;
		this.e = e;
		
		this.density = -1;
		this.neighbors = null;
		this.mbr = null;
		
//		this.embr = traj.visits.get(s).embr;
//		if (this.embr != null) this.embr.enlarge(Env.ep);
		
		this.distSum = -1;
	}
	
	
	public HashSet<Place> computePOIs() {
		HashSet<Place> places = new HashSet<Place>();
		
		for (Visit visit: visits) {
			places.add(visit.place);
		}

		return places;
	}
	
	public void delPostings() {
		for (Visit visit: visits) {
			visit.place.delPostingTrn(this);
		}
	}
	
	public void addPostings() {
		for (Visit visit: visits) {
			visit.place.addPostingTrn(this);
		}
	}
	
	public int nextPos(String cate) {
		return traj.pattern.indexOf(cate, e+1);
	}
	
	public void setInterval(int s, int e) {
		this.s = s;
		this.e = e;
		this.visits = traj.visits.subList(s, e+1);
		
//		this.embr = traj.visits.get(s).embr;
//		if (this.embr != null)
//			this.embr.enlarge(Env.ep);
		
//		this.mbr = null; // to update the MBR
		this.distSum = -1; // to update distSum
	}
	
	public int length() {
		return visits.size();
	}
	
	public Place placeAt(int i) {
		return visits.get(i).place;
	}
	
	public double distance(Place p) {
		double dist = Double.MAX_VALUE;
		
		if (visits.size() == 1) 
			return visits.get(0).place.loc.distance(p.loc);
		
		for (int i=0; i < visits.size()-1; i++) {
			Place src = visits.get(i).place;
			Place dst = visits.get(i+1).place;
			dist = Math.min(dist, Util.distPointLinesegment(src.loc, dst.loc, p.loc));
			
		}
		
		return dist;
	}
	
	public double weight() {
		return 1.0/ (double) ((length()-pattern.length()) + 1.0);
	}
	
	public double density() {
		return density;
	}
	
	public double distSum() {
		if (distSum == -1) {
			this.distSum = 0;
			
			for (int i=0; i < this.length()-1; i++) {
				Place p1 = this.visits.get(i).place;
				Place p2 = this.visits.get(i+1).place;
				this.distSum = p1.loc.distance(p2.loc);
			}
		}
		return distSum;
	}
	
	public void setDensity(double density) {
//		double penalty = Math.min(Env.ep/this.distSum, 1);		
		double penalty = (double) this.pattern.length() / (double) this.length();
//		double penalty = 1.0 / (double) this.length();		
		
		this.density = density*penalty;
	}
	
	public void setNeighbors(NeighborTset neighborTrns) {
		this.neighbors = neighborTrns;
	}
	
	public NeighborTset neighbors() {
		return this.neighbors;
	}
	
//	public boolean contains(Transition trn) {
//		return (this.s <= trn.s && this.e >= trn.e);
//	}
	
	public String toString() {
		String str = "T"+ traj.uid+"["+s+":"+e+"]<"+pattern.toString()+">(density="+density+")"; 
//		String str = "T"+ traj.uid+"["+s+":"+e+"]"+pattern.toString()+"(delta="+density+")"; 
//		str += "\n" + strPOIs();
		
		return str;
	}
	
	public String strPOIs() {
		String str = "";
		
		for (Visit v: visits) {
			str += v.place.loc.toString()+" ";
		}
		return str;
	}

	@Override
	public Iterator<Visit> iterator() {
		return (Iterator<Visit>) visits.iterator();
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + e;
		result = prime * result + s;
		result = prime * result + ((traj == null) ? 0 : traj.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Transition)) {
			return false;
		}
		Transition other = (Transition) obj;
		if (e != other.e) {
			return false;
		}
		if (s != other.s) {
			return false;
		}
		if (traj == null) {
			if (other.traj != null) {
				return false;
			}
		} else if (!traj.equals(other.traj)) {
			return false;
		}
		return true;
	}


	@Override
	public int compareTo(Transition o) {
		
		if (this.traj.equals(o.traj))
			return this.s - o.s;
		else {
			long idDiff = this.traj.uid - o.traj.uid;
			if (idDiff > 0) return 1;
			else if (idDiff < 0) return -1;
			else return 0;
		}
		
	}
	
	public double computeRatio(Transition other, double ep) {
		int cnt = 0;
		for (Visit v: other) {
			if (this.distance(v.place) <= ep) {
				cnt++;
			}
		}
		return (double) cnt / (double) other.length();
	}

	public MBR getMBR() {
		if (this.mbr == null)
		{
			this.mbr = new MBR();
			for (Visit visit: visits) {
				this.mbr.updateMBR(visit.place.loc);
			}
			this.mbr.enlarge(Env.ep);
		}
			
		return this.mbr;
	}
	
	public MBR eMBR() {
		return this.embr;
	}


	
	
}
