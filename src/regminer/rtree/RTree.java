package regminer.rtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

import regminer.util.Debug;
import regminer.util.Env;
import regminer.struct.NeighborTset;
import regminer.struct.Place;
import regminer.struct.Point;
import regminer.struct.Transition;

public class RTree {
	public static final int M = (int) Math.floor(Env.B/Entry.size);
	public static final int m = (int) Math.floor(M * 0.5);
	public Node R=null;
	public int nodeCount = 0;
	public int leafCount = 0;
	public int height = 0;
	public int nodes = 0;
	
	public RTree() {
		if (R==null){
			Node n = new Node(true);
			nodes++;
			R = n;
			height++;
		}
	}

	/* Search */
	private void _search(Node T, double xl, double xh, double yl, double yh, ArrayList<Entry> result){
		Entry e;
		if (T.isleaf) leafCount++;
		else nodeCount++;
		for (int a=0; a<T.size(); a++){
			e = T.get(a);
			if (((!(xh<e.x.l || xl>e.x.h))) && (!(yl>e.y.h || yh<e.y.l))){
				if (T.isleaf){ // leaf
					result.add(e);
				}
				else _search(e.child, xl, xh, yl, yh, result);
			} 
		}
	}
	public ArrayList<Entry> rangeSearch(double xl, double xh, double yl, double yh){
		ArrayList<Entry> result = new ArrayList<Entry>(); 
		nodeCount = 0;
		leafCount = 0;
		_search(R, xl, xh, yl, yh, result);

		return result;
	}
	
	
	private void _neighborSearch(Node T, Transition trn, HashMap<Transition, Integer> results, double ep){
		Entry e;
		MBR range = trn.getMBR();
		if (T.isleaf) leafCount++;
		else nodeCount++;
		for (int a=0; a<T.size(); a++){
			e = T.get(a);
			if (((!(range.x.h<e.x.l || range.x.l>e.x.h))) && (!(range.y.l>e.y.h || range.y.h<e.y.l))){
				if (T.isleaf){ // leaf
					Place p = ((LEntry)e).obj;
					if (trn.distance(p) <= ep) 
					{
						if (p.postingTrns == null)
							Debug._Error(this, "postingTrns is NULL");
						else {
							for (Transition neighbor: p.postingTrns)
							{
								if (results.containsKey(neighbor)) {
									int freq = results.get(neighbor);
									results.put(neighbor, freq+1);
								}
								else {
									results.put(neighbor, 1);
								}
							}
						}
					}
				}
				else _neighborSearch(e.child, trn, results, ep);
			} 
		}
	}
	public HashMap<Transition, Integer> neighborhoodSearch(Transition trn, double ep) {
		HashMap<Transition, Integer> results = new HashMap<Transition, Integer>();
		nodeCount = 0;
		leafCount = 0;
		_neighborSearch(R, trn, results, ep);

		return results;
	}

	public Entry nextNN(Point q, PriorityQueue<Entry> pq) {
		if (pq == null) {
			nodeCount = 1;
			leafCount = 0;
			pq = new PriorityQueue<Entry>(11, Entry.CompareDist);
			for(int i = 0; i < R.size(); i++) {
				Entry e = R.get(i);
				e.dist = e.distTo(q);
				pq.add(e);
			}
		}
		Entry next;
		while ((next=pq.poll()) != null)
		{
			if (next.child == null) // point
				break;
			else {
				if(next.child.isleaf) leafCount++;
				else nodeCount++;
				for(int i = 0; i < next.child.size(); i++) { // node
					Entry e = next.child.get(i);
					e.dist = e.distTo(q);
					pq.add(e);
				}
			}
		}
		return next;
	}

	public ArrayList<Entry> kNNSearch(Point q, int k) {
		ArrayList<Entry> knns = new ArrayList<Entry>();
		nodeCount = 1;
		leafCount = 0;
		PriorityQueue<Entry> pq = new PriorityQueue<Entry>(11, Entry.CompareDist);
		for(int i = 0; i < R.size(); i++) {
			Entry e = R.get(i);
			e.dist = e.distTo(q);
			pq.add(e);
		}
		for (int i=0; i < k; i++){
			knns.add(nextNN(q, pq));
		}
		return knns;
	}

	/* Insert */
	private Node chooseLeaf(Entry e){
		Node n = R;
		while (!n.isleaf){
			double mincost = Double.MAX_VALUE;
			double s = -1, d;
			Entry se = null;
			if (n.get(0).child.isleaf) { // the childpointers in N point to leaves
				for (int i=0; i < n.size(); i++)
				{
					Entry k = n.get(i);
					double cost = k.overlap(e);
					if (mincost > cost) {
						mincost = cost;
						se = k;
					} else if (mincost == cost) {
						d = k.diffArea(e);
						if (s < 0 || s > d) {
							s = d;
							se = k;
						}
						else if (s == d) {
							if (se.area() > k.area())
								se = k;
						}
					}
				}
			} else {
				for (int i=0; i < n.size(); i++)
				{
					Entry k = n.get(i);
					d = k.diffArea(e);
					if (s < 0 || s > d) {
						s = d;
						se = k;
					}
					else if (s == d) {
						if (se.area() > k.area())
							se = k;
					}
				}
			}
			n = se.child;
		}
		return n;
	}

	private Node splitNode(Node n, Entry e){  // node n's split and inserted entry is e
		Node nn = new Node(n.isleaf);
		nodes++;
		nn.parent = n.parent;
		n.add(e);
		ArrayList<Entry> temp = n.entryList;
		n.initEntries();
		nn.initEntries();

		chooseSplitAxis(temp);
		int k = chooseSplitIndex(temp);

		Entry t;
		for (int i=0; i < m-1+k; i++) {
			t = temp.get(i);
			n.add(t);
			if (!n.isleaf) t.child.parent = n;
		}
		for (int i=m-1+k; i < M+1; i++) {
			t = temp.get(i);
			nn.add(t);
			if (!n.isleaf) t.child.parent = nn;
		}

		return nn;
	}
	private int chooseSplitIndex(ArrayList<Entry> entries) {
		int index = -1;
		double min = Double.MAX_VALUE, cost;
		Node first = new Node(); Node second = new Node();
		for (int i = 0; i < m-1; i++) {
			first.add(entries.get(i));
		}
		for (int i = m-1; i < M+1; i++) {
			second.add(entries.get(i));
		}

		for (int k = 0; k < M-2*m+2; k++) {
			first.add(entries.get(m-1+k));
			second.remove(0);
			cost = overlap(first, second);
			if (min > cost) {
				index = k;
				min = cost;
			}
		}
		return index;
	}
	private double overlap(Node n1, Node n2) {
		double xl, xh, yl, yh;

		xl = Math.max(n1.x.l, n2.x.l);
		xh = Math.min(n1.x.h, n2.x.h);
		yl = Math.max(n1.y.l, n2.y.l);
		yh = Math.min(n1.y.h, n2.y.h);

		if (xl > xh || yl > yh) return 0;
		else return (xh-xl)*(yh-yl);
	}
	private void chooseSplitAxis(ArrayList<Entry> entries) { // sort entries by the best axis
		double xmargin = 0;
		Collections.sort(entries, Entry.CompareX);

		Node first = new Node(); Node second = new Node();
		for (int i = 0; i < m-1; i++) {
			first.add(entries.get(i));
		}
		for (int i = m-1; i < M+1; i++) {
			second.add(entries.get(i));
		}

		for (int k = 0; k < M-2*m+2; k++) {
			first.add(entries.get(m-1+k));
			second.remove(0);
			xmargin += first.margin() + second.margin();
		}

		double ymargin = 0;
		Collections.sort(entries, Entry.CompareY);

		first = new Node(); second = new Node();
		for (int i = 0; i < m-1; i++) {
			first.add(entries.get(i));
		}
		for (int i = m-1; i < M+1; i++) {
			second.add(entries.get(i));
		}

		for (int k = 0; k < M-2*m+2; k++) {
			first.add(entries.get(m-1+k));
			second.remove(0);
			ymargin += first.margin() + second.margin();
		}

		if (xmargin < ymargin)
			Collections.sort(entries, Entry.CompareX);		
	}

	private void adjustTree(Node l, Node ll){
		Node n = l;
		Node nn = ll;
		while(!n.equals(R)){
			Node p = n.parent;
			Entry en = p.find(n);
			en.adjust();
			if (nn!=null){
				Entry enn = new Entry();
				enn.child = nn;
				enn.adjust();
				if (p.size() < M) {
					p.add(enn);
					nn = null;
				}
				else nn = splitNode(p, enn);
			}
			n = p;
		}
		if (nn!=null){      // root is split!
			Node r = new Node(false);
			nodes++;
			R = r;
			n.parent = R;
			nn.parent = R;
			Entry e1 = new Entry();
			Entry e2 = new Entry();
			e1.child = n;
			e2.child = nn;
			e1.adjust();
			e2.adjust();
			r.add(e1);
			r.add(e2);	
			height++;
		}
	}
	public void insert(Entry e){
		Node l = chooseLeaf(e);
		Node ll = null;
		if (l.size() < M) l.add(e);
		else ll = splitNode(l, e);
		adjustTree(l, ll);
	}
	public void insert(Place o){
		Point p = o.loc;
		LEntry e = new LEntry(p.x, p.x, p.y, p.y, o);
		insert(e);
	}

	/* Delete */
	private Node chooseNode(Entry e){   /// e is not leaf, 
		Node n = R;    // at least has root
		int th = 1;
		int h = 1;
		while (!n.isleaf){
			n = n.get(0).child;
			th++;
		}
		n = e.child;
		while (!n.isleaf){
			n = n.get(0).child;
			h++;
		}
		h = th-h;
		n = R;
		while (h!=0){
			double s = -1;
			Node sn=null;
			for (int a=0; a<n.size(); a++){
				Entry k = n.get(a);
				if (e.x.h<=k.x.h&&e.x.l>=k.x.l&&e.y.h<=k.y.h&&e.y.l>=k.y.l){
					if ((s==-1)||s>k.area()) {
						s = k.area();
						sn = k.child;
					}
				}
			}
			n = sn;
			h--;
		}
		return n;
	}
	private void hinsert(Entry e){ // e has a appropriate subtree.   Insert �ܰ迡 ��� entry ����
		Node l = chooseNode(e);
		Node ll = null;
		if (l.size()<M) l.add(e);
		else ll = splitNode(l, e);
		adjustTree(l,ll);
	}
	private void _findLeaf(Node t, int xl, int xh, int yl, int yh, Entry k){  // assume there exists 1 entry
		for (int a = 0; a<t.size(); a++){
			Entry b = t.get(a);
			if (t.isleaf){
				if (xl==b.x.l&&xh==b.x.h&&yl==b.y.l&&yh==b.y.h)
					k.child = t;
			}
			else if (xl>=b.x.l&&xh<=b.x.h&&yl>=b.y.l&&yh<=b.y.h) 
				_findLeaf(b.child, xl, xh, yl, yh, k);
		}
	}
	private Node findLeaf(int xl, int xh, int yl, int yh){
		Entry result = new Entry();
		result.child = null;
		_findLeaf(R, xl, xh, yl, yh ,result);
		return result.child;
	}
	private void condenseTree(Node n){
		Node p = null;
		Entry e = null;
		ArrayList<Node> q = new ArrayList<Node>();
		while(n!=R){
			p = n.parent;
			e = p.find(n);
			if (n.size()<m){
				p.remove(e);
				q.add(n);
			}else e.adjust();
			n = p;
		}
		while (!q.isEmpty()){
			n = (Node) q.remove(0);
			for (int a=0; a<n.size(); a++){
				if (n.isleaf) insert(n.get(a));
				else hinsert(n.get(a));
			}
		}
	}
	public void delete(int xl, int xh, int yl, int yh){
		Node l = findLeaf(xl, xh, yl, yh);
		if (l==null) return;
		for (int a=0; a<l.size(); a++){
			Entry b = l.get(a);
			if (b.x.l==xl&&b.x.h==xh&&b.y.l==yl&&b.y.h==yh)
				l.remove(a);
		}
		condenseTree(l);
		if (R.size()==1) R = R.get(0).child;
	}
}
