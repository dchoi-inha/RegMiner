package regminer.slist;

import java.util.Iterator;


/**
 * @author Dong-Wan Choi at Imperial College London
 * @class SLlist, to represent the x-ranges/y-ranges of MBRs of transitions
 * 		  This is kind of a linked list but with a function of O(1) deletion
 * @date 9 Jan 2017
 *
 */
public class SLlist implements Iterable<Item> {
	protected Item head;
	protected Item tail;
		
	
//	public void add(Transition trn) {
//		Item items [] = new Item[2];
//		if (isXlist) {
//			items[0] = new Item(trn.eMBR().x.l, trn, 1);
//			items[1] = new Item(trn.eMBR().x.h, trn, -1);
//		} else {
//			items[0] = new Item(trn.eMBR().y.l, trn, 1);
//			items[1] = new Item(trn.eMBR().y.h, trn, -1);
//		}
//		
//		add(items[0]);
//		add(items[1]);
//		
//		trnItemMap.put(trn, items);
//	}
	
//	public void delete(Transition trn) {
//		for (Item item: trnItemMap.get(trn)) {
//			delete(item);
//		}
//		trnItemMap.remove(trn);
//	}


	public void add(Item item){
		if(head == null){
			head = item;
			tail = item;
		} else {
			item.prev = tail;
			tail.next = item;
			tail = item;
		}
	}

	public void delete(Item item) {
		if (item.equals(head)) {
			head = head.next;
			head.prev = null;
		} else if (item.equals(tail)) {
			tail = tail.prev;
			tail.next = null;
		} else {
			item.prev.next = item.next;
			item.next.prev = item.prev;
		}
	}

	@Override
	public Iterator<Item> iterator() {
		return new SLlistItemIterator(this);
	}
	
	public String toString() {
		String str = "";
		
		for (Item item: this) {
			str += item.toString();
			if (item.next != null) str += "-->";
		}
		
		return str;
	}


	public static void main(String args[]) {
//		ArrayList<Place> P;
//		ArrayList<Trajectory> T;
//		Set<String> C;
//		double ep, sg;
//
//		Debug._PrintL("sg: " + Env.sg +"  ep:" + Env.ep + "  BlockSize: " + Env.B);
//
//
//		P = Main.loadPOIs(System.getProperty("user.home")+"/exp/TraRegion/dataset/4sq/places.txt");
//		T = Main.loadTrajectories(System.getProperty("user.home")+"/exp/TraRegion/dataset/4sq/check-ins-sample.txt");
//		C = Main.loadCategories();
//		ep = Env.ep;
//		sg = Env.sg;
//
////		SLlist list = new SLlist(true);
//
//		Random rand = new Random(10);
//		
//		
//		ArrayList<Transition> removed = new ArrayList<Transition>();
//
//		int k = 0;
//		for (Trajectory traj: T)
//		{
//			if (k++ < 10) {
//				ArrayList<Item> items = new ArrayList<Item>();
//
//				if (traj.length() <= 0) {
//					continue;
//				}
//				int i = rand.nextInt(traj.length());
//				int j = rand.nextInt(traj.length());
//
//				Transition trn = new Transition(traj,  null, Math.min(i, j), Math.max(i, j));
//				trn.embr = trn.getMBR();
//				
//				if (rand.nextDouble() > 0.7)
//					removed.add(trn);
//
//				list.add(trn);
//				
//			}
//		}
//		
//		
//		for (Item item: list) {
//			System.out.println(item);
//		}
//		System.out.println();
//		
//		for (Transition remTrn: removed) {
//			System.out.println("after deletion of " + remTrn);
//				list.delete(remTrn);
//		}
//		
//		System.out.println();
//		for (Item item: list) {
//			System.out.println(item);
//		}

	}

	
}

class SLlistItemIterator implements Iterator<Item> {
	private Item cur;
	private SLlist list;

	public SLlistItemIterator(SLlist list) {
		this.list = list;
		this.cur = this.list.head;
	}

	@Override
	public boolean hasNext() {
		if (cur == null) return false;
		else return true;
	}

	@Override
	public Item next() {
		Item ret = this.cur;
		this.cur = this.cur.next;
		return ret;
	}

}
