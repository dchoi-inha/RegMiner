package regminer.algorithm;

import java.util.*;

import regminer.rtree.RTree;
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
			Set<String> cateSet, double ep, double sg, double dt) {
		super(places, trajs, cateSet, ep, sg, dt);
	}
	
	public void splitTrajectoriesByTimeGap() {
		ArrayList<Trajectory> newTrajectories = new ArrayList<Trajectory>();
		
		long id = 0;
		for (Trajectory trajectory: this.trajs) {
			Trajectory newTraj = new Trajectory(++id);
			Visit prev = null;
			for (Visit visit: trajectory) {
				if (prev == null || (visit.timestamp - prev.timestamp) <= this.dt) {
					newTraj.add(visit); 
				} else {
					newTrajectories.add(newTraj);
					newTraj = new Trajectory(++id);
					newTraj.add(visit);
				}
				prev = visit;
			}
			if (newTraj.length() > 0) newTrajectories.add(newTraj);
		}
		
		this.trajs.clear();
		this.trajs = newTrajectories;
		
		Debug._PrintL("# split trajectories: " + this.trajs.size());
	}

	@Override
	public ArrayList<PRegion> mine() {
		Debug._PrintL("");
		Debug._PrintL("--------------Start RegMiner----------");
		
		ArrayList<PRegion> pRegions = new ArrayList<PRegion>();

		// 0. Split trajectories by time gap
		splitTrajectoriesByTimeGap();
		
		
		// 1. Find all frequent patterns along with their sets of transitions
		ArrayList<PRouteSet> freqTrnSets = new ArrayList<PRouteSet>();
		compactGrow(freqTrnSets);		

		
		// 2. Compute pRegions for each pattern
		for (PRouteSet prSet: freqTrnSets) {
			Pattern seq = prSet.pattern;
			
			/***************************************************************************/
			if (seq.length() < 2) continue;
			/***************************************************************************/

			// 2.1. construct the R-tree on POIs in trnSet
			RTree rtree = new RTree();
			HashSet<Place> places = prSet.computePOIs();
			for (Place p: places) {
				p.clearPostings(); // for each trnSet, clear postings..
				rtree.insert(p);
			}
//			Debug._PrintL("Nodes : "+rt.nodes+" heights: "+rt.height+"\n");
			
			// 2.2. construct the inverted list of posting transitions
			for (PRoute route: prSet) {
				route.addPostings();
			}

			// 2.3. perform pDBSCAN()
			ArrayList<PRouteSet> clusters = pDBSCAN(prSet, rtree);
			
			if (clusters.size() > 0) {
				Debug._PrintL(seq + "\t" + prSet.size() + "\t" + prSet.weight() + "\t" + clusters.size());
				for (PRouteSet cluster: clusters) {
					PRegion pRegion = new PRegion(cluster);
					pRegions.add(pRegion);
				}
			}
			else {
				
			}
		}

		Debug._PrintL("--------------End of RegMiner----------");
		return pRegions;
	}


	public void compactGrow(ArrayList<PRouteSet> output) {
		Debug._PrintL("----Start compactGrow----");
		HashSet<String> freqCateSet = freqCategories();

		for (String cate: freqCateSet)
		{
			Pattern seq = new Pattern(cate);
			PRouteSet prSet = new PRouteSet(seq);
			for (Trajectory traj: this.trajs) {
				int idx = traj.pattern.indexOf(cate, 0);
				while (idx >= 0) {
					PRoute route = new PRoute(traj, seq, idx, idx);
					prSet.add(route);
					idx = traj.pattern.indexOf(cate, idx+1);
				}
			}
			Collections.sort(prSet.routes); // sort only for length-1 patterns

			output.add(prSet); // output length-1 patterns
			prefixSpan(output, freqCateSet, seq, prSet);
		}

		Debug._PrintL("# global freq patterns: " + output.size());
		
		
		/***************************************************************************/
//		Collections.sort(output);
//		for (PRouteSet prSet: output) {
//			if (prSet.pattern.length() > 1)	Debug._PrintL(prSet.pattern.toString() + "\t" + prSet.size());
//		}
		/***************************************************************************/
		
		
		Debug._PrintL("----End compactGrow----");
	}

	// grow seq and tSet
	private void prefixSpan(ArrayList<PRouteSet> output, HashSet<String> freqCateSet, Pattern seq, PRouteSet prSet) {
	
		for (String cate: freqCateSet)
		{
			/***************************************************************************/
//			if (cate.equals(tSet.pattern.seq.get(tSet.pattern.seq.size()-1))) continue;
			/***************************************************************************/

			PRouteSet prSetPlus = routeGrow(prSet, seq, cate);

			if (prSetPlus.weight() >= this.sg) {
				output.add(prSetPlus);
				prefixSpan(output, freqCateSet, prSetPlus.pattern, prSetPlus);
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

	private PRouteSet routeGrow(PRouteSet prSet, Pattern seq, String cate)
	{
		Pattern seqP = seq.grow(cate);
		PRouteSet prSetPlus = new PRouteSet(seqP);

		PRoute rtPrev = null;
		int eNew = 0;
		for (PRoute rt: prSet) {
			if (rtPrev != null && rt.traj.equals(rtPrev.traj)) { // for each transition in the same trajectory
				eNew = rt.nextPos(cate);
				if (eNew > rt.e) { // if such a eNew exists
					
					if (rtPrev.s <= rt.s && rtPrev.e >= eNew) { // if previous transition contains new transition
						prSetPlus.decWeight(rtPrev.weight());
						rtPrev.setInterval(rt.s, eNew);
						prSetPlus.incWeight(rtPrev.weight());
					}
					else {
						PRoute trnNew = new PRoute(rt.traj, seqP, rt.s, eNew);
						prSetPlus.add(trnNew);
						rtPrev = trnNew;
					}
				}
			}
			else { // the first trn for each trajectory
				eNew = rt.nextPos(cate);
				if (eNew > rt.e) {
					PRoute rtNew = new PRoute(rt.traj, seqP, rt.s, eNew);
					prSetPlus.add(rtNew);
					rtPrev = rtNew;
				}
			}
		}

		return prSetPlus;
	}

	public ArrayList<PRouteSet> pDBSCAN(PRouteSet prSet, RTree rtree) {
		ArrayList<PRouteSet> clusters = new ArrayList<PRouteSet>();
		HashSet<PRoute> processed = new HashSet<PRoute>();
		ArrayList<PRoute> processing = new ArrayList<PRoute>();

		for (PRoute route: prSet) {
			if (processed.contains(route)) continue;

			processed.add(route);
//			NeighborTset neighborTrns = getNeighbors(trn, trnSet);
			NeighborPRouteSet neighborTrns = getNeighbors(route, rtree);
			route.setDensity(neighborTrns.sumRatios());
			route.setNeighbors(neighborTrns);

			
			if (route.density() >= this.sg) {
				PRouteSet cluster = new PRouteSet(prSet.pattern);
				clusters.add(cluster);
				cluster.add(route);
				processing.clear();
				processing.addAll(neighborTrns.set);

				for (int i=0; i < processing.size(); i++) {
					PRoute neighbor = processing.get(i);
					if (!processed.contains(neighbor)) {
						processed.add(neighbor);
//						NeighborTset newNeighborTrns = getNeighbors(neighbor, trnSet);
						NeighborPRouteSet newNeighborTrns = getNeighbors(neighbor, rtree);
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
	
	public NeighborPRouteSet getNeighbors(PRoute trn, RTree rtree) {
		NeighborPRouteSet neighbors = new NeighborPRouteSet(trn.pattern);
		
		HashMap<PRoute, Integer> neighborFreqs = rtree.neighborhoodSearch(trn, this.ep);
		
		for (PRoute neighbor: neighborFreqs.keySet()) {
			double ratio = (double) neighborFreqs.get(neighbor) / (double) neighbor.length();
			neighbors.add(neighbor, ratio);
		}
//		Debug._PrintL("T"+ trn.traj.uid+"["+trn.s+":"+trn.e+"]" + neighbors.toString());
		return neighbors;
	}

	public NeighborPRouteSet getNeighbors(PRoute rt, PRouteSet prSet) {
		NeighborPRouteSet neighbors = new NeighborPRouteSet(prSet.pattern);
		for (PRoute other: prSet) {
			double ratio = rt.computeRatio(other, this.ep);
			if (ratio > 0) {
				neighbors.add(other, ratio);
			}
		}
		
//		Debug._PrintL("T"+ trn.traj.uid+"["+trn.s+":"+trn.e+"]" + neighbors.toString());
		return neighbors;
	}


}


