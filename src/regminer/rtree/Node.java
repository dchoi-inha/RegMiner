package regminer.rtree;

import java.util.ArrayList;

import regminer.util.Env;
import regminer.util.Debug;

public class Node {
	public boolean isleaf;   // leaf = true
	ArrayList<Entry> entryList;
	Node parent;
	Pair x;
	Pair y;
	
	public Node(boolean flag){
		isleaf = flag;
		parent = null;
		x = new Pair();
		y = new Pair();
		initEntries();
	}
	public Node(){
		isleaf = false;
		parent = null;
		x = new Pair();
		y = new Pair();
		initEntries();
	}
	public void initEntries(){
		entryList = new ArrayList<Entry>();
		x.l = Env.MaxCoord;
		x.h = 0;
		y.l = Env.MaxCoord;
		y.h = 0;
	}
	public int size(){
		return entryList.size();
	}
	public Entry get(int i){
		return entryList.get(i);
	}
	public void add(Entry e){
		entryList.add(e);
		updateMBR(e);
		
	}
	private void updateMBR(Entry e) {
		x.l = Math.min(x.l, e.x.l);
		x.h = Math.max(x.h, e.x.h);
		y.l = Math.min(y.l, e.y.l);
		y.h = Math.max(y.h, e.y.h);
	}
	public void remove(Entry e){
		entryList.remove(e);
		updateMBR(e);
	}
	public Entry remove(int a){
		Entry e = entryList.remove(a);
		updateMBR(e);
		return e;
	}
	public double overlap(int i, Entry e){// overlap cost if e is inserted into i-th entry
		double xl, xh, yl, yh;
		xl = Math.min(get(i).x.l, e.x.l);
		xh = Math.max(get(i).x.h, e.x.h);
		yl = Math.min(get(i).y.l, e.y.l);
		yh = Math.max(get(i).y.h, e.y.h);
		Entry k = new Entry(xl, xh, yl, yh);
		
		double cost = 0;
		for (int j = 0; j < size(); j++)
		{
			if (i == j) continue;
			cost += k.overlap(get(j));
		}
		return cost;
	}
	public double diffArea(Entry e) { // area difference if e is inserted
		double s = area();
		double xl, xh, yl, yh;
		xl = Math.min(x.l, e.x.l);
		xh = Math.max(x.h, e.x.h);
		yl = Math.min(y.l, e.y.l);
		yh = Math.max(y.h, e.y.h);
		double r = (xh-xl)*(yh-yl);
		return r-s;
	}
	public Entry find(Node n){
		for (int i=0; i<size(); i++)
			if (get(i).child.equals(n)) return get(i);
		return null;
	}	
	public double area(){        // return area
		return (x.h-x.l)*(y.h-y.l); 

	}
	public double margin(){
		return 2*((x.h-x.l)+(y.h-y.l));
	}
	
	public String toString() {
		return "("+ this.x.l + "," + this.x.h + ")" + "("+ this.y.l + "," + this.y.h + ")";
	}
}
