package regminer.demo;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.JavascriptObject;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MVCArray;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.javascript.object.WeightedLocation;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import com.lynden.gmapsfx.shapes.Circle;
import com.lynden.gmapsfx.shapes.CircleOptions;
import com.lynden.gmapsfx.shapes.HeatmapLayer;
import com.lynden.gmapsfx.shapes.HeatmapLayerOptions;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;

import static javafx.application.Application.launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import regminer.Main;
import regminer.algorithm.Miner;
import regminer.algorithm.RegMiner;
import regminer.algorithm.RegMiner;
import regminer.struct.NeighborPRouteSet;
import regminer.struct.PRegion;
import regminer.struct.Pattern;
import regminer.struct.Place;
import regminer.struct.Trajectory;
import regminer.struct.PRoute;
import regminer.struct.PRouteSet;
import regminer.struct.Visit;
import regminer.util.Debug;
import regminer.util.Env;
import regminer.util.Util;


public class MainApp extends Application implements MapComponentInitializedListener {

	GoogleMapView mapView;
	GoogleMap map;

	@Override
	public void start(Stage stage) throws Exception {

		//Create the JavaFX component and set this as a listener so we know when 
		//the map has been initialized, at which point we can then begin manipulating it.
		mapView = new GoogleMapView();
		mapView.addMapInializedListener(this);
		
		Scene scene = new Scene(mapView, 1680, 1050);
		

		stage.setTitle("Trajectory View in Google Maps");
		stage.setScene(scene);
		stage.show();
	}


	@Override
	public void mapInitialized() {
		
		final String dataName = "UK";
		
		
		ArrayList<Place> P;
		ArrayList<Trajectory> T;
		Set<String> C;
		Env.NeighborSize = 5; // kilometers
		Env.MaxTimeGap = 60*24;
		Env.sg = 30;
		Debug._PrintL(dataName + "\tmax memory size: " + java.lang.Runtime.getRuntime().maxMemory()/(double)1024/(double)1024/(double)1024 + "GBs");

		P = Main.loadPOIs(System.getProperty("user.home")+"/exp/TraRegion/dataset/"+dataName+"/places.txt");
		T = Main.loadTrajectories(System.getProperty("user.home")+"/exp/TraRegion/dataset/"+dataName+"/check-ins.txt");
		C = Main.loadCategories();
		
//		Env.NeighborSize = Env.ep / Env.ScaleFactor;		
		Env.ep = Env.NeighborSize * Env.ScaleFactor;
		Debug._PrintL("sup: " + Env.sg +"  ep(Kms):" + Env.NeighborSize + " ep: " + Env.ep + "  time gap: " + Env.MaxTimeGap + "  BlockSize: " + Env.B);
		
		long cpuTimeElapsed;
		double [] t = new double[1];

		double avgLat = P.stream().mapToDouble(val -> val.lat).average().getAsDouble();
		double avgLon = P.stream().mapToDouble(val -> val.lon).average().getAsDouble();
		
		//Set the initial properties of the map.
		MapOptions mapOptions = new MapOptions();

		mapOptions.center(new LatLong(avgLat, avgLon))
		.mapType(MapTypeIdEnum.ROADMAP)
		.overviewMapControl(true)
		.panControl(true)
		.rotateControl(false)
		.scaleControl(true)
		.streetViewControl(true)
		.zoomControl(false)
		.zoom(12);

		map = mapView.createMap(mapOptions);
		
//		printTrajectories(T);
		
//		Pattern visiblePattern1 = new Pattern(new String[] {"Hotel", "Coffee Shop"});
//		Pattern visiblePattern2 = new Pattern(new String[] {"NULL", "NULL"});
		
		Pattern visiblePattern1 = new Pattern(new String[] {"Office", "Coffee Shop", "Office"});
		Pattern visiblePattern2 = new Pattern(new String[] {"NULL"});
//		
//		Pattern visiblePattern1 = new Pattern(new String[] {"Office", "Pub"});
//		Pattern visiblePattern2 = new Pattern(new String[] {"Office", "Bar"});
		
//		Pattern visiblePattern1 = new Pattern(new String[] {"Plaza", "Monument / Landmark"});
//		Pattern visiblePattern2 = new Pattern(new String[] {"NULL", "NULL"});

		RegMiner regminer = new RegMiner(P, T, C, Env.ep, Env.sg, Env.MaxTimeGap);
		
//		ArrayList<PRouteSet> freqPRSets = regminer.getFreqTrnSets();
//		for (PRouteSet prSet: freqPRSets) {
//			if (prSet.pattern.equals(visiblePattern1) || prSet.pattern.equals(visiblePattern2)) {
//				printTrnLines(prSet);
//			}
//		}
		
		
		cpuTimeElapsed = Util.getCpuTime();
		ArrayList<PRegion> results = regminer.mine();
		cpuTimeElapsed = Util.getCpuTime() - cpuTimeElapsed; t[0] = cpuTimeElapsed/(double)1000000000;
		
		HeatmapLayerOptions heatMapOptions = new HeatmapLayerOptions();
		ArrayList<WeightedLocation> heatMapData = new ArrayList<WeightedLocation>();
	
		int [] lengthCnts = new int[10];
		for (PRegion pRegion: results) {
//			lengthCnts[pRegion.S.length()-2]++;
			if (pRegion.S.equals(visiblePattern1) || pRegion.S.equals(visiblePattern2)) {
				Debug._PrintL(pRegion.toString());
				printPRegionTrns(pRegion.trns, heatMapData);
				Debug._Print("\n");
				
				avgLat = pRegion.P.stream().mapToDouble(val -> val.lat).average().getAsDouble();
				avgLon = pRegion.P.stream().mapToDouble(val -> val.lon).average().getAsDouble();
			}
		}
		
		map.setCenter(new LatLong(51.507222, -0.1275));
		
		heatMapOptions.radius(metersToEquatorPixels(Env.ep / Env.ScaleFactor * 200, map)).opacity(0.8).dissipating(true).maxIntensity(300).data(new MVCArray(heatMapData.toArray()));
		HeatmapLayer heatMap = new HeatmapLayer(heatMapOptions);
		heatMap.setMap(map);
		
		for (int i = 0; i < lengthCnts.length; i++)
			Debug._PrintL("# length-"+(i+2)+" patterns: " + lengthCnts[i]);		
		Debug._PrintL("Elapsed time: " + t[0]);
		
		
		map.zoomProperty().addListener((ObservableValue<? extends Number> obs, Number o, Number n) -> {
            heatMap.setOptions(heatMapOptions.radius(metersToEquatorPixels(Env.ep / Env.ScaleFactor * 200, map)));
        });
	}
	


	public static double metersToEquatorPixels(double meters, GoogleMap map) {
		double metresPerPixel = 40075016.686 * Math.abs(Math.cos(map.getCenter().getLatitude() * 180/Math.PI)) / Math.pow(2, map.getZoom()+8);
		return meters/ metresPerPixel;     
	}


	
	public void printPRegionTrns(PRouteSet trns, ArrayList<WeightedLocation> heatMapData) {
		Random rand = new Random();
		int k = 0;
		
		for (PRoute trn: trns)
		{
			
//			Color color = Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
			Color color = Color.GREY;
			String colorStr = String.format( "#%02X%02X%02X",
		            (int)( color.getRed() * 255 ),
		            (int)( color.getGreen() * 255 ),
		            (int)( color.getBlue() * 255 ) );
			k++;
			Debug._Print(trn.toString()+ "(");

			ArrayList<LatLong> path = new ArrayList<LatLong>();
			PolylineOptions lineOptions = new PolylineOptions();
			

			for (int i = 0; i < trn.visits.size(); i++) 
			{
				Visit visit = trn.visits.get(i);
				Place p = visit.place;
				if (p != null) {
					Debug._Print(p.category + "->");		
					path.add(new LatLong(p.lat, p.lon)); 
					heatMapData.add(new WeightedLocation(p.lat, p.lon, trn.density()));
					MarkerOptions markerOptions = new MarkerOptions();
					setMarkerIcon(trn, p, markerOptions); 
					Marker marker = new Marker(markerOptions);
//					map.addMarker(marker);
				}
			}
			Debug._PrintL(")");
			
			lineOptions.path(new MVCArray(path.toArray()))
			.geodesic(true)
			.strokeWeight(1.0)
			.strokeOpacity(0.6)
			.strokeColor(colorStr);			
			Polyline line = new Polyline(lineOptions);
			map.addMapShape(line);
		}
		
	}
	
	public void printTrnLines(PRouteSet trns) {
		Random rand = new Random();
		int k = 0;
//		String colorStr = Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)).toString();
		String colorStr = Color.RED.toString();
//		System.out.println(colorStr);
		colorStr = colorStr.replace("0x", "#");
		colorStr = colorStr.replace("ff", "");
//		System.out.println(colorStr);
		for (PRoute trn: trns)
		{
			k++;

			ArrayList<LatLong> path = new ArrayList<LatLong>();
			PolylineOptions lineOptions = new PolylineOptions();
			

			for (int i = 0; i < trn.visits.size(); i++) 
			{
				Visit visit = trn.visits.get(i);
				Place p = visit.place;
				if (p != null) {
					path.add(new LatLong(p.lat, p.lon)); 
					MarkerOptions markerOptions = new MarkerOptions();
					setMarkerIcon(trn, p, markerOptions); 
					Marker marker = new Marker(markerOptions);
//					map.addMarker(marker);
				}
			}
			
			lineOptions.path(new MVCArray(path.toArray()))
			.geodesic(true)
			.strokeWeight(1.0)
			.strokeOpacity(0.5)
			.strokeColor(colorStr);			
			Polyline line = new Polyline(lineOptions);
			map.addMapShape(line);
		}
		
	}


	private void printNeighbors(Place origin, NeighborPRouteSet neighbors, String colorStr) {
		for (PRoute neighbor: neighbors) {
			ArrayList<LatLong> path = new ArrayList<LatLong>();
			PolylineOptions lineOptions = new PolylineOptions();
			for (Visit visit: neighbor.visits) {
				Place p = visit.place;
				PolylineOptions neighborLineOptions = new PolylineOptions();
				ArrayList<LatLong> path2 = new ArrayList<LatLong>();
				path2.add(new LatLong(origin.lat, origin.lon));
				path2.add(new LatLong(p.lat, p.lon));
				neighborLineOptions.path(new MVCArray(path2.toArray()))
				.geodesic(true).strokeWeight(0.1).strokeColor(colorStr);
				Polyline neighborLine = new Polyline(neighborLineOptions);
				map.addMapShape(neighborLine);
				if (p != null) {
					path.add(new LatLong(p.lat, p.lon));
					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.position( new LatLong(p.lat, p.lon) )
					.icon("icon/dot.png")
					.title("N (" + origin.id +")" + neighbor.toString()); 
					Marker marker = new Marker(markerOptions);
					map.addMarker(marker);
				}
			}
			
			lineOptions.path(new MVCArray(path.toArray()))
			.geodesic(true)
			.strokeWeight(0.5)
			.strokeColor(colorStr);
			
			
			Polyline line = new Polyline(lineOptions);
			
			map.addMapShape(line);
		}
		
	}


	public void printTrajectories(ArrayList<Trajectory> T) {
		Random rand = new Random();
		int k = 0;
		for (Trajectory traj: T)
		{

//			if (k++ % 100 != 0) continue;
			k++;
			System.out.print("T_"+k+ "(");

			ArrayList<LatLong> path = new ArrayList<LatLong>();
			PolylineOptions lineOptions = new PolylineOptions();

			for (int i = 0; i < traj.visits.size(); i++) 
			{
				Visit visit = traj.visits.get(i);
				Place p = visit.place;
				if (p != null) {
					System.out.print(p.category + "->");		
					path.add(new LatLong(p.lat, p.lon));
					MarkerOptions markerOptions = new MarkerOptions();
//					setMarkerIcon(p, markerOptions);
					Marker marker = new Marker(markerOptions);
					map.addMarker(marker);
				}
			}
			Debug._PrintL(")");
			String colorStr = Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)).toString();
			colorStr = colorStr.replace("0x", "#");
			colorStr = colorStr.replace("ff", "");
			
			lineOptions.path(new MVCArray(path.toArray()))
			.geodesic(true)
			.strokeWeight(1.0)
			.strokeColor(colorStr);
			
			
			Polyline line = new Polyline(lineOptions);
			
			map.addMapShape(line);

		}
	}


	public void setMarkerIcon(PRoute trn, Place venue, MarkerOptions markerOptions) {
//		if (venue.category.contains("Coffee")) {
//			markerOptions.position( new LatLong(venue.lat, venue.lon) )
//			.icon("icon/coffee.png")
//			.title(trn.toString() + " " + venue.toString());
//		} else if (venue.category.contains("Office")) {
//			markerOptions.position( new LatLong(venue.lat, venue.lon) )
//			.icon("icon/office.png")
//			.title(trn.toString() + " " + venue.toString());
//		} else if (venue.category.contains("Home")) {
//			markerOptions.position( new LatLong(venue.lat, venue.lon) )
//			.icon("icon/home.png")
//			.title(trn.toString() + " " + venue.toString());
//		} else if (venue.category.contains("College")) {
//			markerOptions.position( new LatLong(venue.lat, venue.lon) )
//			.icon("icon/school.png")
//			.title(trn.toString() + " " + venue.toString());
//		} else if (venue.category.contains("Restaurant")) {
//			markerOptions.position( new LatLong(venue.lat, venue.lon) )
//			.icon("icon/restaurant.png")
//			.title(trn.toString() + " " + venue.toString());
//		} else if (venue.category.contains("School")) {
//			markerOptions.position( new LatLong(venue.lat, venue.lon) )
//			.icon("icon/school.png")
//			.title(trn.toString() + " " + venue.toString());
//		} else if (venue.category.contains("Bank")) {
//			markerOptions.position( new LatLong(venue.lat, venue.lon) )
//			.icon("icon/bank.png")
//			.title(trn.toString() + " " + venue.toString());
//		} else {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/" + venue.category.substring(0, 1).toLowerCase()+ ".png")
			.title(trn.toString() + " " + venue.toString());
//		}
	}

	
	public static void main(String[] args) {
		launch(args);
	}
}