package regminer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import regminer.algorithm.Miner;
import regminer.algorithm.SkeletonRegMiner;
import regminer.struct.Place;
import regminer.struct.Trajectory;
import regminer.struct.Visit;
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
		ArrayList<Place> P;
		ArrayList<Trajectory> T;
		Set<String> C;
		double ep, sg;
		
		P = loadPOIs(System.getProperty("user.home")+"/exp/TraRegion/dataset/4sq/places.txt");
		T = loadTrajectories(System.getProperty("user.home")+"/exp/TraRegion/dataset/4sq/check-ins.txt");
		C = loadCategories();
		ep = Env.ep;
		sg = Env.sg;
		
		long cpuTimeElapsed;
		double [] t = new double[1];
		
		Miner skeleton = new SkeletonRegMiner(P, T, C, ep, sg);
		cpuTimeElapsed = Util.getCpuTime();
		skeleton.mine();
		cpuTimeElapsed = Util.getCpuTime() - cpuTimeElapsed; t[0] = cpuTimeElapsed/(double)1000000000;
		
		System.out.println("time:" + t[0]);
		
	}
	
	private static ArrayList<Trajectory> loadTrajectories(String fpath) {
		ArrayList<Trajectory> tras = new ArrayList<Trajectory>();

		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(new File(fpath)));


			for (String line = in.readLine(); line != null; line = in.readLine())
			{
				String [] tokens = line.split("\t");
				String id = tokens[0];

				Trajectory traj = new Trajectory(id);

				String [] checkins = tokens[1].split("\\|");

				for (int i=0; i < checkins.length; i++)
				{
					String [] checkin = checkins[i].split(",");
					Visit visit = new Visit(checkin[0], checkin[1]);

					if (visit.place != null)
						traj.add(visit);
				}
				tras.add(traj);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 


		return tras;
	}


	private static ArrayList<Place> loadPOIs(String fpath)  {

		ArrayList<Place> POIs = new ArrayList<Place>();

		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(new File(fpath)));


			int catCnt = 0;
			for (String line = in.readLine(); line != null; line = in.readLine())
			{
				String [] tokens = line.split(",");

				String id = tokens[0];
				double lat = Double.parseDouble(tokens[1]);
				double lon = Double.parseDouble(tokens[2]);
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
			
			Util.convertToXY(POIs);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 


		return POIs;
	}
	
	private static Set<String> loadCategories() {
		return Env.Cate_Id.keySet();
	}

}
