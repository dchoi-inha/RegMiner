package regminer.struct;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class PRegion
 * @date 21 Dec 2016
 *
 */
public class PRegion {
	public HashSet<Place> P;
	public Pattern S;
	
	public PRegion(HashSet<Place> POIs, Pattern seq) {
		P = POIs;
		S = seq;
	}
		
}
