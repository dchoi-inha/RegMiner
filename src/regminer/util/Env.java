package regminer.util;

import java.util.HashMap;

import regminer.struct.Edge;
import regminer.struct.Place;

public class Env {
	
	public static final String HomeDir = System.getProperty("user.home") + "/exp/TraRegion/";
	
	public static final int B = 1024; // block size in bytes


	public static HashMap<String, Place> Place_Map;	
	public static HashMap<Edge, Integer> Edge_Map;	
	public static HashMap<String, Integer> Cate_Id;	
	public final static double MaxCoord = 1.0;	
	public static double ScaleFactor = -1.0; // ScaleFactor =  (MaxCoord) / (Max Axis Len in kilometers)
	
	public static double lambda = 0.0;
	
	public static double ep = 0.001;
	public static double sg = 25;
	
	public static double NeighborSize = 1; // kilometers
	
	public static long MaxTimeGap = 60*24*1; // in minutes
	
}
