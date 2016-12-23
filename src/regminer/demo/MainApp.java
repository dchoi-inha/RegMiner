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

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import regminer.struct.Edge;
import regminer.struct.Place;
import regminer.struct.Trajectory;
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

		ArrayList<Place> POIs = loadPOIs(System.getProperty("user.home")+"/exp/TraRegion/dataset/4sq/places.txt");
		ArrayList<Trajectory> tras = loadTras(System.getProperty("user.home")+"/exp/TraRegion/dataset/4sq/check-ins.txt");

		Random rand = new Random();

		int k = 0;
		for (Trajectory traj: tras)
		{

			if (k++ % 100 != 0) continue;
						
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
		
//		for (Edge e: Env.Edge_Map.keySet()){
//			if (Env.Edge_Map.get(e) > 20) {
//				ArrayList<LatLong> path = new ArrayList<LatLong>();
//
//				
//				System.out.println(k++ +" "+ e + "\t" + Env.Edge_Map.get(e));
//				
//				path.add(new LatLong(e.src.lat, e.src.lon));
//				path.add(new LatLong(e.dst.lat, e.dst.lon));
//				
//				MarkerOptions markerOptions = new MarkerOptions();
//				markerOptions.position( new LatLong(e.src.lat, e.src.lon) )
//				.icon("icon/dot.png")
//				.title(e.src.category);
//				
//				MarkerOptions markerOptions2 = new MarkerOptions();
//				markerOptions2.position( new LatLong(e.dst.lat, e.dst.lon) )
//				.icon("icon/dot.png")
//				.title(e.dst.category);
//				
//				Marker marker = new Marker(markerOptions);
//				Marker marker2 = new Marker(markerOptions2);
//				map.addMarker(marker);
//				map.addMarker(marker2);
//
//				String colorStr = Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)).toString();
//				colorStr = colorStr.replace("0x", "#");
//				colorStr = colorStr.replace("ff", "");
//				
//				PolylineOptions lineOptions = new PolylineOptions();
//
//				lineOptions.path(new MVCArray(path.toArray()))
//				.geodesic(true)
//				.strokeWeight(1.0)
//				.strokeColor(colorStr);
//				
//				
//				Polyline line = new Polyline(lineOptions);
//				
//				map.addMapShape(line);
//			}
//		}

//		int i = 0;
//		for (Place venue: POIs)
//		{
//			Env.Cate_Set.add(venue.category);
//			if (venue.category.equals("Coffee Shop") || 
//					venue.category.equals("Office") || 
//					venue.category.equals("Bank") ||
//					venue.category.equals("College") ||
//					venue.category.equals("University") ||
//					venue.category.equals("Home") ||
//					venue.category.equals("Restaurant") ) {
//							if (i++ > 10) break;
//
//				MarkerOptions markerOptions = new MarkerOptions();
//
//				setMarkerIcon(venue, markerOptions); 
//
//				Marker marker = new Marker(markerOptions);
//				map.addMarker(marker);
//
//				//			if (venue.category.equals("Office")) {
//				//
//				//				CircleOptions circleOptions = new CircleOptions();
//				//				circleOptions.center(new LatLong(venue.lat, venue.lon))
//				//				.radius(10);
//				//				Circle dot = new Circle(circleOptions);
//				//
//				//				map.addMapShape(dot);
//				//			}
//			}
//		}

//		System.out.println(Env.Cate_Set);


	}


	public void setMarkerIcon(Place venue, MarkerOptions markerOptions) {
		if (venue.category.contains("Coffee")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/coffee.png")
			.title(venue.category);
		} else if (venue.category.contains("Office")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/office.png")
			.title(venue.category);
		} else if (venue.category.contains("Home")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/home.png")
			.title(venue.category);
		} else if (venue.category.contains("College")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/school.png")
			.title(venue.category);
		} else if (venue.category.contains("Restaurant")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/restaurant.png")
			.title(venue.category);
		} else if (venue.category.contains("School")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/school.png")
			.title(venue.category);
		} else if (venue.category.contains("Bank")) {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/bank.png")
			.title(venue.category);
		} else {
			markerOptions.position( new LatLong(venue.lat, venue.lon) )
			.icon("icon/dot.png")
			.title(venue.category + venue.loc);
		}
	}

	private ArrayList<Trajectory> loadTras(String fpath) {
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


	private ArrayList<Place> loadPOIs(String fpath)  {

		ArrayList<Place> POIs = new ArrayList<Place>();

		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(new File(fpath)));


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

				POIs.add(p);
			}
			Util.convertToXY(POIs);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 


		return POIs;
	}

	public static void main(String[] args) {
		launch(args);
	}
}