package regminer.algorithm;

import java.util.ArrayList;
import java.util.Set;

import regminer.struct.PRegion;
import regminer.struct.Place;
import regminer.struct.Trajectory;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class GridMiner
 * @date 14 Feb 2017
 *
 */
public class GridMiner extends Miner {
	
	
	public GridMiner(ArrayList<Place> places, ArrayList<Trajectory> trajs,
			Set<String> cateSet, double ep, double sg) {
		super(places, trajs, cateSet, ep, sg);
	}

	@Override
	public ArrayList<PRegion> mine() {
		ArrayList<PRegion> output = new ArrayList<PRegion>();
		
		
		return output;
	}

}
