package regminer.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Pattern implements Iterable<String>{
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
	
}
