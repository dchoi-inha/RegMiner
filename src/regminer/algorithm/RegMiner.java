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
public class RegMiner extends Miner {

	public RegMiner(ArrayList<Place> places, ArrayList<Trajectory> trajs,
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
			Pattern seq = trnSet.pattern;
			
			/***************************************************************************/
			if (seq.length() < 2) continue;
			/***************************************************************************/

//			Debug._PrintL("\n" + seq + "("+trnSet.size()+")" + "(" + trnSet.weight() + ")");
			// construct the R-tree on POIs in trnSet
			RTree rt = new RTree();
			HashSet<Place> places = trnSet.computePOIs();
			for (Place p: places) {
				p.clearPostings(); // for each trnSet, clear postings..
				rt.insert(p);
			}
//			Debug._PrintL("Nodes : "+rt.nodes+" heights: "+rt.height+"\n");
			
			// construct the inverted list of posting transitions
			for (Transition trn: trnSet) {
				trn.addPostings();
			}

			ArrayList<Tset> clusters = pDBSCAN(trnSet, rt);
			
			if (clusters.size() > 0) {
				Debug._PrintL(seq + "\t" + trnSet.size() + "\t" + trnSet.weight() + "\t" + clusters.size());
				for (Tset cluster: clusters) {
					PRegion pRegion = new PRegion(cluster);
					results.add(pRegion);
				}
			}
			else {
				
			}
		}

		Debug._PrintL("");Debug._PrintL("");
		Debug._PrintL("--------------End of SkeletonRegMine----------");
		Debug._PrintL("");Debug._PrintL("");
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

		Debug._PrintL("# global freq patterns: " + output.size());
		
		
		/***************************************************************************/
//		Collections.sort(output);
		for (Tset tSet: output) {
			if (tSet.pattern.length() > 1)	Debug._PrintL(tSet.pattern.toString() + "\t" + tSet.size());
		}
		/***************************************************************************/
		
		
		Debug._PrintL("----End compactGrow----\n");
	}

	// grow seq and tSet
	private void prefixSpan(ArrayList<Tset> output, HashSet<String> freqCateSet, Pattern seq, Tset tSet) {
	
		for (String cate: freqCateSet)
		{
			/***************************************************************************/
//			if (cate.equals(tSet.pattern.seq.get(tSet.pattern.seq.size()-1))) continue;
			/***************************************************************************/

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
					
					if (trnPrev.s <= trn.s && trnPrev.e >= eNew) { // if previous transition contains new transition
						tSetP.decWeight(trnPrev.weight());
						trnPrev.setInterval(trn.s, eNew);
						tSetP.incWeight(trnPrev.weight());
					}
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
		ArrayList<Transition> processing = new ArrayList<Transition>();

		for (Transition trn: trnSet) {
			if (processed.contains(trn)) continue;

			processed.add(trn);
//			NeighborTset neighborTrns = getNeighbors(trn, trnSet);
			NeighborTset neighborTrns = getNeighbors(trn, rt);
			trn.setDensity(neighborTrns.sumRatios());
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
//						NeighborTset newNeighborTrns = getNeighbors(neighbor, trnSet);
						NeighborTset newNeighborTrns = getNeighbors(neighbor, rt);
						neighbor.setDensity(newNeighborTrns.sumRatios());
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


