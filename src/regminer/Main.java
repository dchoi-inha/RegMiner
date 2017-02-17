package regminer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


import regminer.algorithm.Miner;
import regminer.algorithm.RegMiner;
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
		
		final String dataName = "UK";	

		
		Debug._PrintL(dataName + "\tmax memory size: " + java.lang.Runtime.getRuntime().maxMemory()/(double)1024/(double)1024/(double)1024 + "GBs");
		ArrayList<Place> P=null;
		ArrayList<Trajectory> T=null;
		Set<String> C=null;

		double [] neighborSizeArray = new double [] {0.1, 0.5, 1, 5, 10}; // in kilometers
		double [] sgArray = new double [] {200, 250};

		for (int i=0; i < sgArray.length; i++) {
			P = loadPOIs(System.getProperty("user.home")+"/exp/TraRegion/dataset/"+dataName+"/places.txt");
			T = loadTrajectories(System.getProperty("user.home")+"/exp/TraRegion/dataset/"+dataName+"/check-ins.txt");
			C = loadCategories();

			Env.sg = sgArray[i];
			Env.NeighborSize = neighborSizeArray[2];
			Env.ep = Env.NeighborSize * Env.ScaleFactor;
			
			Debug._PrintL("sup: " + Env.sg +"  ep(Kms):" + Env.NeighborSize +"  ep:" + Env.ep + "  time gap: " + Env.MaxTimeGap + "  BlockSize: " + Env.B);

			
			long cpuTimeElapsed;
			double time = 0.0;
			ArrayList<PRegion> result = new ArrayList<PRegion>();
			
			Miner skeleton = new RegMiner(P, T, C, Env.ep, Env.sg, Env.MaxTimeGap);
			cpuTimeElapsed = Util.getCpuTime();
			result = skeleton.mine();
			cpuTimeElapsed = Util.getCpuTime() - cpuTimeElapsed; time = cpuTimeElapsed/(double)1000000000;
			
			Debug._PrintL("# pRegions: " + result.size());
			Debug._PrintL("time:" + time);
			Debug._PrintL("\n\n");
		}
		
	}
	
	public static ArrayList<Trajectory> loadTrajectories(String fpath) {
		Debug._PrintL("----Start loading trajectories----");
		ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();

		int pairCnt = 0;
		double sumTimeGap = 0.0;
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(new File(fpath)));


			long idLong = 0;
			for (String line = in.readLine(); line != null; line = in.readLine())
			{
				String [] tokens = line.split("\t");

				Trajectory traj = new Trajectory(++idLong);
				Visit prev = null;
				String [] checkins = tokens[1].split("\\|");
				for (int i=0; i < checkins.length; i++)
				{
					String [] checkin = checkins[i].split(",");
					
					try {
						Visit visit = new Visit(checkin[0].trim(), checkin[1].trim());


						/************************************************ Filtering **********************************************************************/
						if (visit.place == null) continue;
						else if (prev != null && prev.place.equals(visit.place)) continue;
						else if (prev != null && prev.place.category.equals(visit.place.category) && prev.place.loc.distance(visit.place.loc) <= Env.lambda) continue;
						/************************************************ Filtering **********************************************************************/

						if (prev != null) {
							sumTimeGap += (visit.timestamp - prev.timestamp); 
							pairCnt++;
						}

						traj.add(visit);
						prev = visit;
					} catch (ParseException e1) {
						System.err.println(checkin[0]+","+checkin[1]+"|");
					}

				}
				if (traj.length() > 0) 
					trajectories.add(traj);
			}
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Debug._PrintL("# trajectories: " + trajectories.size() +" avg time gap: " + (sumTimeGap / (double) pairCnt));
		Debug._PrintL("----Complete loading trajectories----\n");
		return trajectories;
	}


	public static ArrayList<Place> loadPOIs(String fpath)  {

		Debug._PrintL("----Start loading POIs----");
		Env.Place_Map = new HashMap<String, Place>();
		Env.Cate_Id = new HashMap<String, Integer>();

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
				String category;
				if (tokens.length == 4) {
					int pos = tokens[3].lastIndexOf("::");
					category = tokens[3].substring((pos > 0? pos+2: 0));
				}
				else continue;

				Place p = new Place(id, lat, lon, category);

				Env.Place_Map.put(id, p);
				if (!Env.Cate_Id.containsKey(category))
				{
					Env.Cate_Id.put(category, catCnt++);
				}

				POIs.add(p);
			}
			
			Env.ScaleFactor = Util.convertToXY(POIs);
			in.close();
		} catch (Exception e) {
			Debug._Error(null, line);
			e.printStackTrace();
		} 

		Debug._PrintL("# POIs: " + POIs.size() +"  Scale factor: " + Env.ScaleFactor + "  # categories: " + Env.Cate_Id.size());
		Debug._PrintL("----Complete loading POIs----\n");
		return POIs;
	}
	
	public static Set<String> loadCategories() {
		return Env.Cate_Id.keySet();
	}

}
