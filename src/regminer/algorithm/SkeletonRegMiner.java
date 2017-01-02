package regminer.algorithm;

import java.util.*;

import regminer.rtree.RTree;
import regminer.struct.PRegion;
import regminer.struct.Pattern;
import regminer.struct.Place;
import regminer.struct.Trajectory;
import regminer.struct.Transition;
import regminer.struct.Tset;

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
			Pattern seq = trnSet.pattern;			
			System.out.println(seq + "("+trnSet.size()+")");
			
			// construct the R-tree on POIs in trnSet
			RTree rt = new RTree();
			for (Place p: trnSet.places) {
				rt.insert(p);
			}
			System.out.print("Nodes : "+rt.nodes+" heights: "+rt.height+"\n");
			
			ArrayList<Tset> clusters = pDBSCAN(trnSet, rt);
			
//			for (Tset cluster: clusters) {
//				results.add(new PRegion(cluster.getPOIs(), cluster.pattern));
//			}
		}
		
		return results;
	}
	
	
	public void compactGrow(ArrayList<Tset> output) {
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
		
	}
	
	// grow seq and tSet
	private void prefixSpan(ArrayList<Tset> output, HashSet<String> freqCateSet, Pattern seq, Tset tSet) {
		for (String cate: freqCateSet)
		{
			Tset tSetP = transitionGrow(tSet, seq, cate);
			
			if (tSetP.size() >= this.sg) {
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
			if (cateFreq.get(cate) >= this.sg) freqCateSet.add(cate);
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
		HashSet<Transition> processed = new HashSet<Transition>();
		
		for (Transition trn: trnSet) {
			if (processed.contains(trn)) continue;
			
			processed.add(trn);
			Tset neighbors = getNeighbors(trn);
			
			// TODO:need to implement from here
			
			
			
		}
		
		
		return null;
	}
	
	public Tset getNeighbors(Transition trn) {
		return null;
	}
	
	
}


