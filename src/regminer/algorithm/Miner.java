package regminer.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import regminer.struct.PRegion;
import regminer.struct.Place;
import regminer.struct.Trajectory;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class Miner
 * @date 21 Dec 2016
 *
 */
public abstract class Miner {

	// input
	ArrayList<Place> places;
	ArrayList<Trajectory> trajs;
	Set<String> cateSet;
	double ep, sg;
	
	
	
	public Miner(ArrayList<Place> places, ArrayList<Trajectory> trajs,
			Set<String> cateSet, double ep, double sg) {
		super();
		this.places = places;
		this.trajs = trajs;
		this.cateSet = cateSet;
		this.ep = ep;
		this.sg = sg;
	}
	
	abstract public ArrayList<PRegion> mine();
	
	
}
