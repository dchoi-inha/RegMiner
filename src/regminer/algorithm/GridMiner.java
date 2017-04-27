package regminer.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import regminer.struct.PRegion;
import regminer.struct.PRoute;
import regminer.struct.PRouteSet;
import regminer.struct.Pattern;
import regminer.struct.Place;
import regminer.struct.Trajectory;
import regminer.struct.Visit;
import regminer.util.Debug;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class GridMiner
 * @date 27 Apr 2017
 *
 */
public class GridMiner extends Miner {
	
	public GridMiner (ArrayList<Place> places, ArrayList<Trajectory> trajs,
			Set<String> cateSet, double ep, double sg, double dt) {
		super(places, trajs, cateSet, ep, sg, dt);
	}
	
	@Override
	public ArrayList<PRegion> mine() {
		Debug._PrintL("");
		Debug._PrintL("--------------Start GridMiner----------");
		
		ArrayList<PRegion> pRegions = new ArrayList<PRegion>();
		splitTrajectoriesByTimeGap();

		gridMine(this.trajs, pRegions);	
		
		return pRegions;
	}
	
	
	private void gridMine(ArrayList<Trajectory> trajectories, ArrayList<PRegion> pRegions) {
		// 1. Find all frequent patterns along with their sets of transitions
		ArrayList<PRouteSet> freqTrnSets = new ArrayList<PRouteSet>();
		compactGrow(freqTrnSets, trajectories);
		
		// 2. Return patterns that are dense w.r.t. the current space
		for (PRouteSet prSet: freqTrnSets) {
			/***************************************************************************/
			if (prSet.pattern.length() < 2) continue;
			/***************************************************************************/
			
			// TODO: SPECIFY THESE VARIABLES!
			double w = 0;
			double h = 0;
			/////////////////////////////////
			
			double pDensity = prSet.weight() * (4*Math.pow(this.sg, 2) * prSet.pattern.length()  / (w*h));
			if (pDensity >= this.sg) {
				PRegion pRegion = new PRegion(prSet);
				pRegions.add(pRegion);
			}
		}
		
		// 3. Split the input set of trajectories into 4 quadrants
		ArrayList<ArrayList<Trajectory>> cells = splitTrajectoriesByGrid(trajectories);
		
		for (ArrayList<Trajectory> cell: cells) {
			gridMine(cell, pRegions);
		}
	}
	
	// TODO: IMPLEMENT THIS FUNCTION!
	private ArrayList<ArrayList<Trajectory>> splitTrajectoriesByGrid(ArrayList<Trajectory> trajectories) {
		// if the size of each cell is less than \sigma, do not add the cell
		return null;
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
	
	// grow seq and tSet
	private void prefixSpan(ArrayList<PRouteSet> output, HashSet<String> freqCateSet, Pattern seq, PRouteSet prSet) {
	
		for (String cate: freqCateSet)
		{
			/***************************************************************************/
//			if (cate.equals(prSet.pattern.seq.get(prSet.pattern.seq.size()-1))) continue;
			/***************************************************************************/

			PRouteSet prSetPlus = routeGrow(prSet, seq, cate);

			if (prSetPlus.weight() >= this.sg) {
				output.add(prSetPlus);
				prefixSpan(output, freqCateSet, prSetPlus.pattern, prSetPlus);
			}
		}
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
	
	public void compactGrow(ArrayList<PRouteSet> output, ArrayList<Trajectory> trajectories) {
		Debug._PrintL("----Start compactGrow----");
		HashSet<String> freqCateSet = freqCategories();

		for (String cate: freqCateSet)
		{
			Pattern seq = new Pattern(cate);
			PRouteSet prSet = new PRouteSet(seq);
			for (Trajectory traj: trajectories) {
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

		Debug._PrintL("# freq patterns: " + output.size());
		
		
		/***************************************************************************/
		Collections.sort(output, Collections.reverseOrder());
//		for (PRouteSet prSet: output) {
//			if (prSet.pattern.length() > 1)	{
//				Debug._PrintL(prSet.pattern.toString() + "\t" + prSet.size() + "\t" + prSet.weight());
//			}
//		}
		/***************************************************************************/
		
		Debug._PrintL("----End compactGrow----");
	}

}
