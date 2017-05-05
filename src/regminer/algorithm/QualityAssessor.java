package regminer.algorithm;

import java.util.ArrayList;

import regminer.struct.Circle;
import regminer.struct.PRegion;
import regminer.util.Debug;
import regminer.util.Env;
import regminer.util.Util;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class QualityAssessor
 * @date 5 May 2017
 *
 */
public class QualityAssessor {
	
	
	private double freqSum, freqMax, freqMin;
	private double farthestDistSum, farthestDistMax, farthestDistMin;
	private double densitySum, densityMax, densityMin;
	
	private double secDiaSum;
	private double densitySum2;
	
	
	public QualityAssessor() {
	}
	
	public void initMetrics() {
			this.freqSum = 0.0;
			this.freqMax = Double.MIN_VALUE;
			this.freqMin = Double.MAX_VALUE;

			this.farthestDistSum = 0.0;
			this.farthestDistMax = Double.MIN_VALUE;
			this.farthestDistMin = Double.MAX_VALUE;
			
			this.densitySum = 0.0;
			this.densityMax = Double.MIN_VALUE;
			this.densityMin = Double.MAX_VALUE;
			
			this.secDiaSum = 0.0;
			this.densitySum2 = 0.0;
	}

	public void assess(ArrayList<PRegion> result) {
		initMetrics();
		double frequency, diameter, density, area;
		double diameter2, density2, area2;
		Circle sec;
		for (PRegion pRegion: result) {
			frequency = pRegion.frequency();
//			diameter = pRegion.diameter()/Env.ScaleFactor; // convert to kilometers
//			area = Math.max(1.0, (Math.PI*Math.pow(diameter/2.0, 2)));
//			density = frequency/area;
			
			sec = pRegion.sec();
			diameter2 = (sec.getDiameter()+Env.ep)/Env.ScaleFactor; // convert to kilometers
			area2 = Math.max(1.0, (Math.PI*Math.pow(diameter2/2.0, 2)));
			density2 = frequency/area2;
			
			this.freqSum += frequency;
			this.freqMax = Math.max(this.freqMax, frequency);
			this.freqMin = Math.min(this.freqMin, frequency);
			
//			this.farthestDistSum += diameter;
//			this.farthestDistMax = Math.max(this.farthestDistMax, diameter);
//			this.farthestDistMin = Math.min(this.farthestDistMin, diameter);
			
//			this.densitySum += density;
//			this.densityMax = Math.max(this.densityMax, density);
//			this.densityMin = Math.min(this.densityMin, density);
			
			this.secDiaSum += diameter2;
			this.densitySum2 += density2;
		}
		
		Debug._PrintL("# occurrences avg.: " + freqSum/result.size() + "[" + freqMin + ", " + freqMax + "]");
//		Debug._PrintL("diameter: " + farthestDistSum/result.size() + " [" + farthestDistMin + ", " + farthestDistMax + "]");
//		Debug._PrintL("circular density: " + densitySum/result.size() + "[" + densityMin + ", " + densityMax + "]");
		Debug._PrintL("diameter avg.: " + secDiaSum/result.size() + "   circular density avg.: " + densitySum2/result.size());

		
	}
	
	
}
