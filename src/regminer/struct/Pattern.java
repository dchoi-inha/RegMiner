package regminer.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Pattern implements Iterable<String> {
	public ArrayList<String> seq;
	
	public Pattern()
	{	
		seq = new ArrayList<String>();
	}
	
	public Pattern(Collection<?extends String> collection)
	{
		seq = new ArrayList<String>(collection);
	}
	
	public Pattern(String cate) {
		seq = new ArrayList<String>();
		seq.add(cate);
	}
	
	public Pattern(String[] strings) {
		seq = new ArrayList<String>();
		for (int i=0; i < strings.length; i++) {
			seq.add(strings[i]);
		}
	}

	public void add(String cate) {
		seq.add(cate);
	}
	
	/**
	 * find the index of occurrence 'cate' after idx (inclusive)
	 */
	public int indexOf(String cate, int idx) {
		if (idx >= seq.size()) return -1;
		for (int i=idx; i < seq.size(); i++) {
			if (seq.get(i).equals(cate))
				return i;
		}
		return -1;
	}
		
	public int length() {
		return seq.size();
	}
	
	public String toString() {
		return seq.toString();
	}
	
	public Pattern grow(String cate) {
		Pattern pattern = new Pattern(this.seq);
		pattern.add(cate);
		
		return pattern;
	}

	@Override
	public Iterator<String> iterator() {
		return (Iterator<String>) seq.iterator();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pattern) {
			Pattern other = (Pattern) obj;
			if (this.length() == other.length()) {
				for (int i = 0; i < length(); i++) {
					if (!this.seq.get(i).contains(other.seq.get(i)) && !other.seq.get(i).contains(this.seq.get(i)))
						return false;
				}
				return true;
			}
			return false;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return java.util.Objects.hash(seq.toArray());
	}
	
	
	
	public boolean startWith(Pattern other) {
		if (this.length() >= other.length()) {
			for (int i = 0; i < other.length(); i++) {
				if (!this.seq.get(i).contains(other.seq.get(i)) && !other.seq.get(i).contains(this.seq.get(i)))
					return false;
			}
			return true;
		}
		return false;
	}
	
	
	
	public static void main(String[] args) {
		Pattern p1, p2;
		p1 = new Pattern(new String[] {"a", "b", "b", "b", "b", "b", "b", "b", "b"});
//		p1.add("b");
//		p1.add("c");
		
		p2 = new Pattern("a");
		p2.add("b");
		p2.add("b");
		p2.add("b");
		p2.add("b");
		p2.add("b");
		p2.add("b");
		p2.add("b");
		p2.add("b");
		
		
		System.out.println(p1 + " " + p1.hashCode());
		System.out.println(p2 + " " + p2.hashCode());
	}


	
}
