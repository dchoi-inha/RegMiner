package regminer.algorithm;

import java.util.*;

import regminer.rtree.MBR;
import regminer.rtree.RTree;
import regminer.slist.Item;
import regminer.struct.*;
import regminer.util.Debug;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class SkeletonRegMiner
 * @date 21 Dec 2016
 *
 */
public class RegMiner extends Miner {

	public RegMiner(ArrayList<Place> places, ArrayList<Trajectory> trajs,
			Set<String> cateSet, double ep, double sg) {
		super(places, trajs, cateSet, ep, sg);
	}

	@Override
	public ArrayList<PRegion> mine() {
		ArrayList<PRegion> results = new ArrayList<PRegion>();
		ArrayList<Tset> fTrnSets = new ArrayList<Tset>();
		// 1. Find all frequent patterns along with their sets of transitions
		HashSet<String> fCates = freqCatesAndCreateMAs();

		// 2. For each frequent category, construct and partition its transition set
		for (String cate: fCates)
		{
			Pattern seq = new Pattern(cate);
			Tset tSet = new Tset(seq);
			ArrayList<Item> listItemsX = new ArrayList<Item>();
			ArrayList<Item> listItemsY = new ArrayList<Item>();
			for (Trajectory traj: this.trajs) {
				int idx = traj.pattern.indexOf(cate, 0);
				while (idx >= 0) {
					Transition trn = new Transition(traj, seq, idx, idx);
					tSet.add(trn);
					Item itemInX = new Item(trn.eMBR().x.l, trn, 1);
					Item itemOutX = new Item(trn.eMBR().x.h, trn, -1);
					Item itemInY = new Item(trn.eMBR().y.l, trn, 1);
					Item itemOutY = new Item(trn.eMBR().y.h, trn, -1);
					
					listItemsX.add(itemInX); listItemsX.add(itemOutX);
					listItemsY.add(itemInY); listItemsY.add(itemOutY);
					
					idx = traj.pattern.indexOf(cate, idx+1);
				}
				Collections.sort(listItemsX);
				Collections.sort(listItemsY);
			}
			Collections.sort(tSet.trns); // sort only for length-1 patterns
			fTrnSets.add(tSet); // output length-1 patterns
			
			
			for (Item itemX: listItemsX) {
				tSet.listX.add(itemX);
			}
			
			for (Item itemY: listItemsY) {
				tSet.listY.add(itemY);
			}
		}
		
		
		
		
		return results;
	}
	
	
	// finding all frequent items and construct MAs
	private HashSet<String> freqCatesAndCreateMAs() {
		HashMap<String, Integer> cateFreq = new HashMap<String, Integer>();
		HashSet<String> freqCateSet = new HashSet<String>();
		int cnt = 0;
		for (Trajectory traj: this.trajs)
		{
			MBR embr = new MBR();
			for (int i=traj.length()-1; i >= 0; i--) 
			{
				Visit v = traj.visits.get(i);
				embr.updateMBR(v.place.loc);
				v.embr = new MBR(embr.x.l, embr.x.h, embr.y.l, embr.y.h);
				
				String cate = v.place.category;
				
				if (cateFreq.containsKey(cate))
				{
					cnt = cateFreq.get(cate);
					cateFreq.put(cate, cnt+1);
				}
				else cateFreq.put(cate, 1);
			}
		}
		for (String cate: this.cateSet) {
			if (cateFreq.containsKey(cate) && cateFreq.get(cate) >= this.sg) 
				freqCateSet.add(cate);
		}
		return freqCateSet;
	}


	public void compactGrow(ArrayList<Tset> output) {
		Debug._PrintL("----Start compactGrow----");
		HashSet<String> freqCateSet = freqCatesAndCreateMAs();

		for (String cate: freqCateSet)
		{
			Pattern seq = new Pattern(cate);
			Tset tSet = new Tset(seq);
			for (Trajectory traj: this.trajs) {
				int idx = traj.pattern.indexOf(cate, 0);
				while (idx >= 0) {
					Transition trn = new Transition(traj, seq, idx, idx);
					tSet.add(trn);
					idx = traj.pattern.indexOf(cate, idx+1);
				}
			}
			Collections.sort(tSet.trns); // sort only for length-1 patterns

			output.add(tSet); // output length-1 patterns
			prefixSpan(output, freqCateSet, seq, tSet);
		}

		Debug._PrintL("# freq patterns: " + output.size());
		Debug._PrintL("----End compactGrow----\n");
	}

	// grow seq and tSet
	private void prefixSpan(ArrayList<Tset> output, HashSet<String> freqCateSet, Pattern seq, Tset tSet) {
	
		for (String cate: freqCateSet)
		{
			Tset tSetP = transitionGrow(tSet, seq, cate);

			if (tSetP.weight() >= this.sg) {
				output.add(tSetP);
				prefixSpan(output, freqCateSet, tSetP.pattern, tSetP);
			}
		}
	}


	private Tset transitionGrow(Tset tSet, Pattern seq, String cate)
	{
		Pattern seqP = seq.grow(cate);
		Tset tSetP = new Tset(seqP);

		Transition trnPrev = null;
		int eNew = 0;
		for (Transition trn: tSet) {
			if (trnPrev != null && trn.traj.equals(trnPrev.traj)) { // for each transition in the same trajectory
				eNew = trn.nextPos(cate);
				if (eNew > trn.e) { // if such a eNew exists
					
					if (trnPrev.s <= trn.s && trnPrev
							.e >= eNew) // if previous transition contains new transition
						trnPrev.setInterval(trn.s, eNew);
					else {
						Transition trnNew = new Transition(trn.traj, seqP, trn.s, eNew);
						tSetP.add(trnNew);
						trnPrev = trnNew;
					}
				}
			}
			else { // the first trn for each trajectory
				eNew = trn.nextPos(cate);
				if (eNew > trn.e) {
					Transition trnNew = new Transition(trn.traj, seqP, trn.s, eNew);
					tSetP.add(trnNew);
					trnPrev = trnNew;
				}
			}
		}

		return tSetP;
	}

	public ArrayList<Tset> pDBSCAN(Tset trnSet, RTree rt) {
		ArrayList<Tset> clusters = new ArrayList<Tset>();
		HashSet<Transition> processed = new HashSet<Transition>();

		for (Transition trn: trnSet) {
			if (processed.contains(trn)) continue;

			processed.add(trn);
//			NeighborTset neighborTrns = getNeighbors(trn, trnSet);
			NeighborTset neighborTrns = getNeighbors(trn, rt);
			trn.setDensity(neighborTrns.density());
			trn.setNeighbors(neighborTrns);

			
			if (trn.density() >= this.sg) {
				Tset cluster = new Tset(trnSet.pattern);
				clusters.add(cluster);
				cluster.add(trn);

				for (int i=0; i < neighborTrns.size(); i++) {
					Transition neighbor = neighborTrns.get(i);
					if (!processed.contains(neighbor)) {
						processed.add(neighbor);
//						NeighborTset newNeighborTrns = getNeighbors(neighbor, trnSet);
						NeighborTset newNeighborTrns = getNeighbors(neighbor, rt);
						neighbor.setDensity(newNeighborTrns.density());
						neighbor.setNeighbors(newNeighborTrns);
						if (neighbor.density() >= this.sg) {
							cluster.add(neighbor);
							neighborTrns.mergeWith(newNeighborTrns);
						}
					}
				}
			}

		}

		return clusters;
	}
	
	public NeighborTset getNeighbors(Transition trn, RTree rt) {
		NeighborTset neighbors = new NeighborTset(trn.pattern);
		
		HashMap<Transition, Integer> neighborFreqs = rt.neighborhoodSearch(trn, this.ep);
		
		for (Transition neighbor: neighborFreqs.keySet()) {
			double ratio = (double) neighborFreqs.get(neighbor) / (double) neighbor.length();
			neighbors.add(neighbor, ratio);
		}
//		Debug._PrintL("T"+ trn.traj.uid+"["+trn.s+":"+trn.e+"]" + neighbors.toString());
		return neighbors;
	}

	public NeighborTset getNeighbors(Transition trn, Tset trnSet) {
		NeighborTset neighbors = new NeighborTset(trnSet.pattern);
		for (Transition other: trnSet) {
			double ratio = trn.computeRatio(other, this.ep);
			if (ratio > 0) {
				neighbors.add(other, ratio);
			}
		}
		
//		Debug._PrintL("T"+ trn.traj.uid+"["+trn.s+":"+trn.e+"]" + neighbors.toString());
		return neighbors;
	}


}


