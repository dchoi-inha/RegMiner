package regminer.struct;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
	
	public Transition(Trajectory traj, Pattern pattern, int s, int e)
	{
		this.traj = traj;
		this.visits = traj.visits.subList(s, e+1);
		this.pattern = pattern;
		this.s = s;
		this.e = e;
	}
	
	public HashSet<Place> getPOIs() {
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
	
//	public boolean contains(Transition trn) {
//		return (this.s <= trn.s && this.e >= trn.e);
//	}
	

	@Override
	public Iterator<Visit> iterator() {
		return (Iterator<Visit>) visits.iterator();
	}
	
	public String toString() {
		return traj.uid+"["+s+":"+e+"]"+pattern.toString();
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

}
