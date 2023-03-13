package uk.ac.mmu.advprog.hackathon;
import static spark.Spark.get;
import static spark.Spark.port;

import java.util.ArrayList;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handles the setting up and starting of the web service
 * You will be adding additional routes to this class, and it might get quite large
 * Feel free to distribute some of the work to additional child classes, like I did with DB
 * @author You, Mainly!
 */
public class TransportWebService {

	/**
	 * Main program entry point, starts the web service
	 * @param args not used
	 */ 
	static ArrayList<String> StopTypes = new ArrayList<String>();
	
	public static void main(String[] args) {		
		port(8088);
		try {
			StopTypes.add("BUS");
			StopTypes.add("RLW");
			StopTypes.add("MET");
			StopTypes.add("FER");
			StopTypes.add("AIR");
			StopTypes.add("TXR");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Simple route so you can check things are working...
		//Accessible via http://localhost:8088/test in your browser
		get("/test", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
				
					return "Number of Entries: " + db.getNumberOfEntries();
				} 
			}			
		});
		
		
		
		get("/stopcount", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					
					String locality = request.queryParams("locality");
					if(locality == null  || locality.equals("")) {
						return "Invalid request";
					}
					
				
					
					return db.stopCountforLocality(locality);
				}
			}			
		});
		
		get("/stops", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					response.type("application/json");
					String locality = request.queryParams("locality");
					String stopType = request.queryParams("type");
					
					
					if(locality == null  || locality.equals("")||stopType == null  || stopType.equals("")  || !StopTypes.contains(stopType)) {
						return "Invalid Request";
					}
					
					
					
					return db.getStopDetailsForTransportType(locality,stopType);
				}
			}			
		});
		
		get("/nearest", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					response.type("application/xml");
					String latitude = request.queryParams("latitude");
					String longitute = request.queryParams("longitude");
					String stopType = request.queryParams("type");
					
					
				//	if(locality == null  || locality.equals("")||stopType == null  || stopType.equals("")  || !StopTypes.contains(stopType)) {
				//		return "Error";
				//	}
					
					
					
					return db.getClosest(latitude,longitute ,stopType);
				}
			}			
		});
		
		
		
		System.out.println("Server up! Don't forget to kill the program when done!");
	}
	
	
	

}
