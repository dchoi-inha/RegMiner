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
		ArrayList<PRegion> output = new ArrayList<PRegion>();
		
		// 1. Find all frequent patterns along with their sets of transitions
		HashSet<String> freqCateSet = freqCatesAndCreateMAs();

		// 2. For each frequent category, construct and partition its transition set
		for (String cate: freqCateSet)
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
					Item [] itemsX = new Item[2]; Item [] itemsY = new Item[2];
					itemsX[0] = new Item(trn.eMBR().x.l, trn, 1); itemsX[1] = new Item(trn.eMBR().x.h, trn, -1);
					itemsY[0] = new Item(trn.eMBR().y.l, trn, 1); itemsY[1] = new Item(trn.eMBR().y.h, trn, -1);
					
					for (int i=0; i < itemsX.length; i++) {
						listItemsX.add(itemsX[i]); listItemsY.add(itemsY[i]);
					}
					idx = traj.pattern.indexOf(cate, idx+1);
				}
			}
			Collections.sort(listItemsX);
			Collections.sort(listItemsY);
			
			// construct SL-list for the transition set
			for (Item itemX: listItemsX) {
				tSet.listX.add(itemX);
			} listItemsX.clear();
			
			for (Item itemY: listItemsY) {
				tSet.listY.add(itemY);
			} listItemsY.clear();
			
//			Collections.sort(tSet.trns);
//			procPartition(tSet, freqCateSet, output);
			
			HashSet<Tset> partitions = partition(tSet);
			for (Tset subset: partitions) {
				Collections.sort(subset.trns); // sort
				procPartition(subset, freqCateSet, output); // length-1 patterns
			}
			
			
		}
		
		Debug._PrintL("");Debug._PrintL("");
		Debug._PrintL("--------------End of RegMiner----------");
		Debug._PrintL("");Debug._PrintL("");
		return output;
	}
	
	private HashSet<Tset> partition(Tset tSet) {
		HashSet<Tset> partitionsX = new HashSet<Tset>();
		
		// partition on the x-axis
		Item prev = null;
		int sgnSum = 0;
		Tset tmpSet = new Tset(tSet.pattern);
		for (Item itemX: tSet.listX) {
			sgnSum += itemX.sgn;
			tmpSet.listX.add(itemX.cloneData());
			
			if (itemX.sgn == -1) tmpSet.add(itemX.trn);
			
			if (itemX.next == null) {
				if (tmpSet.size() == tSet.size()) {// no partitioning
					partitionsX.add(tSet);
				} else {
					for (Item itemY: tSet.listY) {
						if (tmpSet.contains(itemY.trn)) {
							tmpSet.listY.add(itemY.cloneData());
						}
					}
					partitionsX.add(tmpSet);
				}
				break;
			}
			
			if (prev != null && prev.coord != itemX.coord) {
				if (sgnSum == 0) { // if there is a possible partitioning
					// update SL-list for the y-axis
					for (Item itemY: tSet.listY) {
						if (tmpSet.contains(itemY.trn)) {
							tmpSet.listY.add(itemY.cloneData());
						}
					}
					
					partitionsX.add(tmpSet);
					Debug._PrintL("Partitioning!! at " + tmpSet.pattern + "(size="+tmpSet.size()+")(weight="+tmpSet.weight()+")");
					
					tmpSet = new Tset(tSet.pattern);
				}
			}
			prev = itemX;
		}
		
		HashSet<Tset> partitions = new HashSet<Tset>();
		// partition on the y-axis
		for (Tset subset: partitionsX) {
			prev = null;
			sgnSum = 0;
			tmpSet = new Tset(subset.pattern);
			for (Item itemY: subset.listY) {
				sgnSum += itemY.sgn;
				tmpSet.listY.add(itemY.cloneData());

				if (itemY.sgn == -1 ) tmpSet.add(itemY.trn);
				
				if (itemY.next == null) {
					if (tmpSet.size() == tSet.size()) {
						partitions.add(tSet);
					} else {
						// update SL-list for the x-axis
						for (Item itemX: subset.listX) {
							if (tmpSet.contains(itemX.trn)) {
								tmpSet.listX.add(itemX.cloneData());
							}
						}
						partitions.add(tmpSet);
					}
					break;
				}

				if (prev != null && prev.coord != itemY.coord) {
					if (sgnSum == 0) {
						// update SL-list for the x-axis
						for (Item itemX: subset.listX) {
							if (tmpSet.contains(itemX.trn)) {
								tmpSet.listX.add(itemX.cloneData());
							}
						}
						partitions.add(tmpSet);
						Debug._PrintL("Partitioning!! at " + tmpSet.pattern + "(size="+tmpSet.size()+")(weight="+tmpSet.weight()+")");
						
						tmpSet = new Tset(subset.pattern);
					}
				}
				prev = itemY;
			}
		}
		
		return partitions;
	}
	
	private void procPartition(Tset tSet, HashSet<String> freqCateSet, ArrayList<PRegion> output) {
		if (tSet.weight() < this.sg) return;
//		Debug._PrintL("\n" + tSet.pattern + "("+tSet.size()+")" + "(" + tSet.weight() + ")");

		// construct the R-tree on POIs in tSet
		RTree rt = indexing(tSet);

		ArrayList<Tset> clusters = pDBSCAN(tSet, rt);

		if (clusters.size() > 0) {
			Debug._PrintL("\n" + tSet.pattern + "("+tSet.size()+")");// + "(" + tSet.weight() + ")");
			Debug._PrintL("Cluster size:" + clusters.size());
			for (Tset cluster: clusters) {
				output.add(new PRegion(cluster));
			}
		}
		
		growClustering(tSet, freqCateSet, output);		
	}
	
	private RTree indexing(Tset tSet) {
		// construct the R-tree on POIs in trnSet
		RTree rt = new RTree();
		HashSet<Place> places = tSet.computePOIs();

		for (Place p: places) {
			p.clearPostings();
			rt.insert(p);
		}
		// construct the inverted list of posting transitions
		for (Transition trn: tSet) {
			trn.addPostings();
		}
		return rt;
	}
	
	private void growClustering(Tset tSet, HashSet<String> freqCateSet, ArrayList<PRegion> output) {
		for (String cate: freqCateSet) {
			Tset tSetP = transitionGrow(tSet, tSet.pattern, cate);
			
			if (tSetP.weight() >= this.sg) {
				HashSet<Tset> partitions = partition(tSetP);

				if (partitions.size() == 1) {
					procPartition(tSetP, freqCateSet, output);
				} else {
					for (Tset subset: partitions) {
						Collections.sort(subset.trns); // sort
						procPartition(subset, freqCateSet, output);
					}
				}
			}
		}
	}
	
	private Tset transitionGrow(Tset tSet, Pattern seq, String cate)
	{
		Pattern seqP = seq.grow(cate);
		Tset tSetP = new Tset(seqP);
		
		HashMap<Transition, Transition> trnMap = new HashMap<Transition, Transition>();

		Transition trnPrev = null;
		int eNew = 0;
		for (Transition trn: tSet) {
			if (trnPrev != null && trn.traj.equals(trnPrev.traj)) { // for each transition in the same trajectory
				eNew = trn.nextPos(cate);
				if (eNew > trn.e) { // if such a eNew exists
					
					if (trnPrev.s <= trn.s && trnPrev.e >= eNew) {// if previous transition contains new transition
						tSetP.decWeight(trnPrev.weight());
						trnPrev.setInterval(trn.s, eNew);
						tSetP.incWeight(trnPrev.weight());
					}
					else {
						Transition trnNew = new Transition(trn.traj, seqP, trn.s, eNew);
						tSetP.add(trnNew);	trnMap.put(trn, trnNew);
						trnPrev = trnNew;
					}
				}
			}
			else { // the first trn for each trajectory
				eNew = trn.nextPos(cate);
				if (eNew > trn.e) {
					Transition trnNew = new Transition(trn.traj, seqP, trn.s, eNew);
					tSetP.add(trnNew);	trnMap.put(trn, trnNew);
					trnPrev = trnNew;
				}
			}
		}
		
		
		for (Item itemX: tSet.listX ) {
			if (trnMap.containsKey(itemX.trn)) {
				tSetP.listX.add(new Item(itemX.coord, trnMap.get(itemX.trn), itemX.sgn));
			}
		}
		
		for (Item itemY: tSet.listY ) {
			if (trnMap.containsKey(itemY.trn)) {
				tSetP.listY.add(new Item(itemY.coord, trnMap.get(itemY.trn), itemY.sgn));
			}
		}
		

		return tSetP;
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


	
	public ArrayList<Tset> pDBSCAN(Tset trnSet, RTree rt) {
		ArrayList<Tset> clusters = new ArrayList<Tset>();
		HashSet<Transition> processed = new HashSet<Transition>();
		ArrayList<Transition> processing = new ArrayList<Transition>();

		for (Transition trn: trnSet) {
			if (processed.contains(trn)) continue;

			processed.add(trn);
			NeighborTset neighborTrns = getNeighbors(trn, rt);
			trn.setDensity(neighborTrns.density());
			trn.setNeighbors(neighborTrns);

			
			if (trn.density() >= this.sg) {
				Tset cluster = new Tset(trnSet.pattern);
				clusters.add(cluster);
				cluster.add(trn);
				processing.clear();
				processing.addAll(neighborTrns.set);

				for (int i=0; i < processing.size(); i++) {
					Transition neighbor = processing.get(i);
					if (!processed.contains(neighbor)) {
						processed.add(neighbor);
						NeighborTset newNeighborTrns = getNeighbors(neighbor, rt);
						neighbor.setDensity(newNeighborTrns.density());
						neighbor.setNeighbors(newNeighborTrns);
						if (neighbor.density() >= this.sg) {
							cluster.add(neighbor);
							processing.addAll(newNeighborTrns.set);
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

}


