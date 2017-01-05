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
	public NeighborTset neighbors;
	
	private MBR mbr;

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
	
	public void setDensity(double density) {
		this.density = density;
	}
	
//	public boolean contains(Transition trn) {
//		return (this.s <= trn.s && this.e >= trn.e);
//	}
	
	public String toString() {
		return "T"+ traj.uid+"["+s+":"+e+"]"+pattern.toString()+"(delta="+density+")";
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
		if (!this.traj.equals(o.traj)) 
			return 1;
		else 
			return (this.s - o.s);
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

}
