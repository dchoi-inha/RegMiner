package regminer.util;

import java.util.HashMap;
import java.util.HashSet;

import regminer.struct.Edge;
import regminer.struct.Place;

public class Env {
	
	public static final String HomeDir = System.getProperty("user.home") + "/exp/TraRegion/";
	
	public static final int B = 1024; // block size in bytes


	public static HashMap<String, Place> Place_Map;
	
	public static HashMap<Edge, Integer> Edge_Map;
	
	public static HashMap<String, Integer> Cate_Id;
	
	public final static double MaxCoord = 1.0;
	
	public static double ScaleRatio = 1.0;

	public static double lambda = 0.0001;
	
	public final static double ep = 0.01;
	public final static double sg = 25;
	
	public final static long MaxDays = 1;
	public final static long MaxTimeGap = 60*24*MaxDays; // in minutes
}
