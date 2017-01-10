package regminer.struct;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class Trajectory
 * @date 21 Dec 2016
 *
 */
public class Trajectory implements Iterable<Visit>{

	public long uid;
	public ArrayList<Visit> visits;
	public Pattern pattern;
	
	public Trajectory(long uid)
	{
		this.uid = uid;
		this.visits = new ArrayList<Visit>();
		this.pattern = new Pattern();
	}
	
	public void add(Visit visit)
	{
		this.visits.add(visit);
		this.pattern.add(visit.place.category);
	}
	
	
	public String toString()
	{
		return uid+"";
	}
	
	public int length()
	{
		return visits.size();
	}
	
	public Place placeAt(int i) {
		return visits.get(i).place;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (uid ^ (uid >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trajectory other = (Trajectory) obj;
		if (uid != other.uid)
			return false;
		return true;
	}

	
	@Override
	public Iterator<Visit> iterator() {
		return (Iterator<Visit>) visits.iterator();
	}


	
}
