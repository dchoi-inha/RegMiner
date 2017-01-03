package regminer.util;

import java.util.HashMap;
import java.util.HashSet;

import regminer.struct.Edge;
import regminer.struct.Place;

public class Env {
	
	public static final String HomeDir = System.getProperty("user.home") + "/exp/TraRegion/";
	
	public static final int B = 1024; // block size in bytes


	public static HashMap<String, Place> Place_Map = new HashMap<String, Place>();
	
	public static HashMap<Edge, Integer> Edge_Map = new HashMap<Edge, Integer>();
	
	public static HashMap<String, Integer> Cate_Id = new HashMap<String, Integer>();
	public static HashMap<Integer, String> Cate_Str = new HashMap<Integer, String>();
	
	public final static double MaxCoord = 1.0;
	
	public final static double ep = 0.0001;
	public final static double sg = 2000;
}
