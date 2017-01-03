package regminer.struct;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
	
	public double density;

	public Transition(Trajectory traj, Pattern pattern, int s, int e)
	{
		this.traj = traj;
		this.visits = traj.visits.subList(s, e+1);
		this.pattern = pattern;
		this.s = s;
		this.e = e;
		
		this.density = -1;
	}
	
	public HashSet<Place> computePOIs() {
		HashSet<Place> places = new HashSet<Place>();
		
		for (Visit visit: visits) {
			places.add(visit.place);
		}

		return places;
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
	
	public double contRatio(Transition other, double ep) {
		int cnt = 0;
		for (Visit v: other) {
			if (this.distance(v.place) <= ep) {
				cnt++;
			}
		}
		return (double) cnt / (double) other.length();
	}
	
	
//	public boolean contains(Transition trn) {
//		return (this.s <= trn.s && this.e >= trn.e);
//	}
	

	@Override
	public Iterator<Visit> iterator() {
		return (Iterator<Visit>) visits.iterator();
	}
	
	public String toString() {
		return traj.uid+"["+s+":"+e+"]"+pattern.toString()+"(delta="+density+")";
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

	public void computePdensity(double sumRatio) {
		double penalty = (double) this.pattern.length() / (double) this.length();
		this.density = 	sumRatio * penalty;
	}

}
