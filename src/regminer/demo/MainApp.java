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
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import com.lynden.gmapsfx.shapes.Circle;
import com.lynden.gmapsfx.shapes.CircleOptions;

import javafx.application.Application;
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
import regminer.algorithm.SkeletonRegMiner;
import regminer.struct.Edge;
import regminer.struct.PRegion;
import regminer.struct.Place;
import regminer.struct.Trajectory;
import regminer.struct.Transition;
import regminer.struct.Tset;
import regminer.struct.Visit;
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
		//Set the initial properties of the map.
		MapOptions mapOptions = new MapOptions();

		mapOptions.center(new LatLong(40.7242017240576, -73.97497613848594))
		.mapType(MapTypeIdEnum.ROADMAP)
		.overviewMapControl(true)
		.panControl(true)
		.rotateControl(false)
		.scaleControl(true)
		.streetViewControl(true)
		.zoomControl(false)
		.zoom(11);

		map = mapView.createMap(mapOptions);
		
		
		ArrayList<Place> P;
		ArrayList<Trajectory> T;
		Set<String> C;
		double ep, sg;

		P = Main.loadPOIs(System.getProperty("user.home")+"/exp/TraRegion/dataset/4sq/places.txt");
		T = Main.loadTrajectories(System.getProperty("user.home")+"/exp/TraRegion/dataset/4sq/check-ins.txt");
		C = Main.loadCategories();
		ep = Env.ep;
		sg = Env.sg;

//		printTrajectories(T);
		
		Miner skeleton = new SkeletonRegMiner(P, T, C, ep, sg);
		ArrayList<PRegion> results = skeleton.mine();
		
		for (PRegion pRegion: results) {
			if (!pRegion.P.isEmpty()) {
				System.out.println(pRegion);
				printTrns(pRegion.trns);
				for (Place p: pRegion.P) {
					MarkerOptions markerOptions = new MarkerOptions();
					setMarkerIcon(p, markerOptions); 
					Marker marker = new Marker(markerOptions);
					map.addMarker(marker);
				}
			}
		}
		

	}
	
	public void printTrns(Tset trns) {
		Random rand = new Random();
		int k = 0;
		for (Transition trn: trns)
		{

//			if (k++ % 100 != 0) continue;
						
			k++;
			System.out.print(trn.toString()+ "(");

			ArrayList<LatLong> path = new ArrayList<LatLong>();
			PolylineOptions lineOptions = new PolylineOptions();

			for (int i = 0; i < trn.visits.size(); i++) 
			{
				Visit visit = trn.visits.get(i);
				Place p = visit.place;
				if (p != null) {
					System.out.print(p.category + "->");		
					path.add(new LatLong(p.lat, p.lon));
					MarkerOptions markerOptions = new MarkerOptions();
					setMarkerIcon(p, markerOptions); 
					Marker marker = new Marker(markerOptions);
					map.addMarker(marker);
					
					CircleOptions circleOptions = new CircleOptions();
					circleOptions.center(new LatLong(p.lat, p.lon)).radius(0.1);
					Circle circle = new Circle();
					
					//TODO: draw circle....
				}
			}
			System.out.println(")");
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
					setMarkerIcon(p, markerOptions); 
					Marker marker = new Marker(markerOptions);
					map.addMarker(marker);
				}
			}
			System.out.println(")");
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


	public void setMarkerIcon(Place venue, MarkerOptions markerOptions) {
		if (venue.category.contains("Coffee")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/coffee.png")
			.title(venue.category + venue.loc);
		} else if (venue.category.contains("Office")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/office.png")
			.title(venue.category + venue.loc);
		} else if (venue.category.contains("Home")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/home.png")
			.title(venue.category + venue.loc);
		} else if (venue.category.contains("College")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/school.png")
			.title(venue.category + venue.loc);
		} else if (venue.category.contains("Restaurant")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/restaurant.png")
			.title(venue.category + venue.loc);
		} else if (venue.category.contains("School")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/school.png")
			.title(venue.category + venue.loc);
		} else if (venue.category.contains("Bank")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/bank.png")
			.title(venue.category + venue.loc);
		} else {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/dot.png")
			.title(venue.category + venue.loc);
		}
	}

	
	public static void main(String[] args) {
		launch(args);
	}
}