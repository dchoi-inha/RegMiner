package regminer.slist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import regminer.Main;
import regminer.rtree.MBR;
import regminer.struct.Place;
import regminer.struct.Trajectory;
import regminer.struct.Transition;
import regminer.util.Debug;
import regminer.util.Env;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class SLlist, to represent the x-ranges/y-ranges of MBRs of transitions
 * @date 9 Jan 2017
 *
 */
public class SLlist implements Iterable<Item>{
	protected Item head;
	protected Item tail;


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


	public static void main(String args[]) {
		ArrayList<Place> P;
		ArrayList<Trajectory> T;
		Set<String> C;
		double ep, sg;

		Debug._PrintL("sg: " + Env.sg +"  ep:" + Env.ep + "  BlockSize: " + Env.B);


		P = Main.loadPOIs(System.getProperty("user.home")+"/exp/TraRegion/dataset/4sq/places.txt");
		T = Main.loadTrajectories(System.getProperty("user.home")+"/exp/TraRegion/dataset/4sq/check-ins-sample.txt");
		C = Main.loadCategories();
		ep = Env.ep;
		sg = Env.sg;

		SLlist list = new SLlist();

		Random rand = new Random();

		HashMap<Transition, ArrayList<Item> > trnItemMap = new HashMap<Transition, ArrayList<Item> >();
		int k = 0;
		Transition trnRemoved = null;
		for (Trajectory traj: T)
		{
			if (k++ < 10) {
				ArrayList<Item> items = new ArrayList<Item>();

				if (traj.length() <= 0) {
					continue;
				}
				int i = rand.nextInt(traj.length());
				int j = rand.nextInt(traj.length());

				Transition trn = new Transition(traj,  null, Math.min(i, j), Math.max(i, j));
				MBR mbr = trn.getMBR();
				
				if (k == 3 || k == 7)
					trnRemoved = trn;

				Item itemIn = new Item(mbr.x.l, trn, 1);
				Item itemOut = new Item(mbr.x.h, trn, -1);
				items.add(itemIn);
				items.add(itemOut);

				list.add(itemIn);
				list.add(itemOut);
				
				trnItemMap.put(trn, items);
			}
		}
		
		
		for (Item item: list) {
			System.out.println(item);
		}
		
		System.out.println("after deletion of   " + trnRemoved);
		
		
		for (Item itemRemoved: trnItemMap.get(trnRemoved)) {
			list.delete(itemRemoved);
		}
		
		for (Item item: list) {
			System.out.println(item);
		}

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
