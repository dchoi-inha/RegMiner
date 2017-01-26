package regminer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import regminer.algorithm.Miner;
import regminer.algorithm.RegMiner;
import regminer.algorithm.SkeletonRegMiner;
import regminer.struct.PRegion;
import regminer.struct.Place;
import regminer.struct.Trajectory;
import regminer.struct.Visit;
import regminer.util.Debug;
import regminer.util.Env;
import regminer.util.Util;

/**
 * @author Dong-Wan Choi at Imperial College London
 * @class Main
 * @date 21 Dec 2016
 *
 */
public class Main {

	
	public static void main(String[] args) {
		Debug._PrintL("max memory size: " + java.lang.Runtime.getRuntime().maxMemory()/(double)1024/(double)1024/(double)1024 + "GBs");
		ArrayList<Place> P=null;
		ArrayList<Trajectory> T=null;
		Set<String> C=null;
		double ep, sg;
		
		Debug._PrintL("sg: " + Env.sg +"  ep:" + Env.ep + "  BlockSize: " + Env.B);

		P = loadPOIs(System.getProperty("user.home")+"/exp/TraRegion/dataset/tsmc2014/places.txt");
		T = loadTrajectories(System.getProperty("user.home")+"/exp/TraRegion/dataset/tsmc2014/check-ins.txt");
		C = loadCategories();
		ep = Env.ep;
		sg = Env.sg;
		
		long cpuTimeElapsed;
		double [] t = new double[2];
		ArrayList<PRegion> results1 = new ArrayList<PRegion>();
		ArrayList<PRegion> results2 = new ArrayList<PRegion>();
		
//		Miner skeleton = new SkeletonRegMiner(P, T, C, ep, sg);
//		cpuTimeElapsed = Util.getCpuTime();
//		results1 = skeleton.mine();
//		cpuTimeElapsed = Util.getCpuTime() - cpuTimeElapsed; t[0] = cpuTimeElapsed/(double)1000000000;
		
		Miner reg = new RegMiner(P, T, C, ep, sg);
		cpuTimeElapsed = Util.getCpuTime();
		results2 = reg.mine();
		cpuTimeElapsed = Util.getCpuTime() - cpuTimeElapsed; t[1] = cpuTimeElapsed/(double)1000000000;
		
		System.out.println("# pRegions: " + results1.size() + "\t" + results2.size());
		System.out.println("time:" + t[0] +"\t"+t[1]);
		
	}
	
	public static ArrayList<Trajectory> loadTrajectories(String fpath) {
		Debug._PrintL("----Start loading trajectories----");
		ArrayList<Trajectory> tras = new ArrayList<Trajectory>();

		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(new File(fpath)));


			long idLong = 0;
			for (String line = in.readLine(); line != null; line = in.readLine())
			{
				String [] tokens = line.split("\t");
				String id = tokens[0];

				Trajectory traj = new Trajectory(++idLong);

				String [] checkins = tokens[1].split("\\|");

				Visit prev = null;
				for (int i=0; i < checkins.length; i++)
				{
					String [] checkin = checkins[i].split(",");
					
					Visit visit;
					if(checkin.length > 1)
						visit = new Visit(checkin[0].trim(), checkin[1]);
					else
						visit = new Visit(checkin[0].trim());
					
					if (visit.place != null && prev != null && (prev.place.category.equals(visit.place.category) && prev.place.loc.distance(visit.place.loc) <= Env.lambda))
						continue;

					if (visit.place != null && (prev == null || !prev.place.equals(visit.place) )) {
						traj.add(visit);
						prev = visit;
					}
				}
				tras.add(traj);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 

		Debug._PrintL("# trajectories: " + tras.size());
		Debug._PrintL("----Complete loading trajectories----\n");
		return tras;
	}


	public static ArrayList<Place> loadPOIs(String fpath)  {

		Debug._PrintL("----Start loading POIs----");
		Env.Place_Map = new HashMap<String, Place>();
		Env.Cate_Id = new HashMap<String, Integer>();
		Env.Cate_Str = new HashMap<Integer, String>();

		ArrayList<Place> POIs = new ArrayList<Place>();

		BufferedReader in;
		String line="";
		try {
			in = new BufferedReader(new FileReader(new File(fpath)));


			int catCnt = 0;
			for (line = in.readLine(); line != null; line = in.readLine())
			{
				String [] tokens = line.split(",");

				String id;
				double lat;
				double lon;
				id = tokens[0].trim();
				lat = Double.parseDouble(tokens[1].trim());
				lon = Double.parseDouble(tokens[2].trim());
				int pos = tokens[3].lastIndexOf("::");
				String category = tokens[3].substring((pos > 0? pos+2: 0));

				Place p = new Place(id, lat, lon, category);

				Env.Place_Map.put(id, p);
				if (!Env.Cate_Id.containsKey(category))
				{
					Env.Cate_Id.put(category, catCnt);
					Env.Cate_Str.put(catCnt++, category);
				}

				POIs.add(p);
			}
			
			Env.ScaleRatio = Util.convertToXY(POIs);
			in.close();
		} catch (Exception e) {
			Debug._Error(null, line);
			e.printStackTrace();
		} 

		Debug._PrintL("# POIs: " + POIs.size() +"  Scale Ratio: " + Env.ScaleRatio + "  # categories: " + Env.Cate_Id.size());
		Debug._PrintL("----Complete loading POIs----\n");
		return POIs;
	}
	
	public static Set<String> loadCategories() {
		return Env.Cate_Id.keySet();
	}

}
