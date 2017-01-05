package regminer.algorithm;

import java.util.*;

import regminer.rtree.RTree;
import regminer.struct.*;
import regminer.util.Debug;
import regminer.util.Env;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class SkeletonRegMiner
 * @date 21 Dec 2016
 *
 */
public class SkeletonRegMiner extends Miner {

	public SkeletonRegMiner(ArrayList<Place> places, ArrayList<Trajectory> trajs,
			Set<String> cateSet, double ep, double sg) {
		super(places, trajs, cateSet, ep, sg);
	}

	@Override
	public ArrayList<PRegion> mine() {
		ArrayList<PRegion> results = new ArrayList<PRegion>();
		// 1. Find all frequent patterns along with their sets of transitions
		ArrayList<Tset> freqTrnSets = new ArrayList<Tset>();
		compactGrow(freqTrnSets);		

		// 2. Compute pRegions for each pattern
		for (Tset trnSet: freqTrnSets) {
			if (trnSet.pattern.length() != 2) continue; //TODO: just for debug, to be deleted
			Pattern seq = trnSet.pattern;			
			Debug._PrintL(seq + "("+trnSet.size()+")" + "(" + trnSet.weight() + ")");

			// construct the R-tree on POIs in trnSet
			RTree rt = new RTree();
			HashSet<Place> places = trnSet.computePOIs();
			for (Place p: places) {
				p.clearPostings();
				rt.insert(p);
			}
			Debug._PrintL("Nodes : "+rt.nodes+" heights: "+rt.height+"\n");
			
			// construct the inverted list of posting transitions
			for (Transition trn: trnSet) {
				trn.addPostings();
			}

			ArrayList<Tset> clusters = pDBSCAN(trnSet, rt);
			
			if (clusters.size() > 1) {
				Debug._PrintL("Cluster size:" + clusters.size());

				for (Tset cluster: clusters) {
					results.add(new PRegion(cluster));
				}
//				break;
			}
		}

		return results;
	}


	public void compactGrow(ArrayList<Tset> output) {
		Debug._PrintL("----Start compactGrow----");
		HashSet<String> freqCateSet = freqCategories();

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

	// finding all frequent items
	private HashSet<String> freqCategories() {
		HashMap<String, Integer> cateFreq = new HashMap<String, Integer>();
		HashSet<String> freqCateSet = new HashSet<String>();
		int cnt = 0;
		for (Trajectory traj: this.trajs)
		{
			for (String cate: traj.pattern) 
			{
				if (cateFreq.containsKey(cate))
				{
					cnt = cateFreq.get(cate);
					cateFreq.put(cate, cnt+1);
				}
				else cateFreq.put(cate, 1);
			}
		}
		for (String cate: this.cateSet) {
			if (cateFreq.containsKey(cate) && cateFreq.get(cate) >= this.sg) freqCateSet.add(cate);
		}
		return freqCateSet;
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
			trn.neighbors = neighborTrns;

			if (trn.density() >= this.sg) {
				Tset cluster = new Tset(trnSet.pattern); //TODO: Tset.add() can be overhead...in this case
				clusters.add(cluster);
				cluster.add(trn);

				for (int i=0; i < neighborTrns.size(); i++) {
					Transition neighbor = neighborTrns.get(i);
					if (!processed.contains(neighbor)) {
						processed.add(neighbor);
//						NeighborTset newNeighborTrns = getNeighbors(neighbor, trnSet);
						NeighborTset newNeighborTrns = getNeighbors(neighbor, rt);
						neighbor.setDensity(newNeighborTrns.density());
						neighbor.neighbors = newNeighborTrns;
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
		Debug._PrintL("T"+ trn.traj.uid+"["+trn.s+":"+trn.e+"]" + neighbors.toString());
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
		
		Debug._PrintL("T"+ trn.traj.uid+"["+trn.s+":"+trn.e+"]" + neighbors.toString());
		return neighbors;
	}


}


