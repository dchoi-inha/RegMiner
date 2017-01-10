package regminer.slist;


import regminer.struct.Transition;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class Item
 * @date 9 Jan 2017
 *
 */
public class Item implements Comparable<Item> {

	public double coord;
	public Transition trn;
	public int sgn; // 1 or -1
	
	public Item next;
	public Item prev;
	
	public Item cloneData() {
		Item item = new Item(this.coord, this.trn, this.sgn);
		return item;
	}
	
	@Override
 	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(coord);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + sgn;
		result = prime * result + ((trn == null) ? 0 : trn.hashCode());
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
		Item other = (Item) obj;
		if (Double.doubleToLongBits(coord) != Double
				.doubleToLongBits(other.coord))
			return false;
		if (sgn != other.sgn)
			return false;
		if (trn == null) {
			if (other.trn != null)
				return false;
		} else if (!trn.equals(other.trn))
			return false;
		return true;
	}



	public Item (double coord, Transition trn, int sgn) {
		this.coord = coord;
		this.trn = trn;
		this.sgn = sgn;
	}
	
	public String toString() {
		return "(" + trn.toString() + "," + String.format("%1$.3f", coord) + "," + sgn + ")";
	}
	


	@Override
	public int compareTo(Item other) {
		if (this.coord == other.coord) {
			if (this.trn.equals(other.trn)) {
				return (-1)*sgn; // (+) first, (-) last
			}
			else if (this.sgn * other.sgn < 0){ // sgn's are differ
				return (-1)*sgn; // (+) first, (-) last
			}
			else {
				return this.sgn+other.sgn;
			}
		}
		else {
			if (this.coord - other.coord > 0) return 1;
			else return -1;
		}
	}
	
	
}
