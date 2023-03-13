package uk.ac.mmu.advprog.hackathon;

import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.*;
import org.xml.sax.*;



/**
 * Handles database access from within your web service
 * @author You, Mainly!
 */
public class DB implements AutoCloseable {
	
	//allows us to easily change the database used
	private static final String JDBC_CONNECTION_STRING = "jdbc:sqlite:./data/NaPTAN.db";
	
	//allows us to re-use the connection between queries if desired
	private Connection connection = null;
	
	/**
	 * Creates an instance of the DB object and connects to the database
	 */
	public DB() {
		try {
			connection = DriverManager.getConnection(JDBC_CONNECTION_STRING);
			
			
		}
		catch (SQLException sqle) {
			error(sqle);
		}
	}
	
	/**
	 * Returns the number of entries in the database, by counting rows
	 * 
	 * @return The number of entries in the database, or -1 if empty
	 */
	public int getNumberOfEntries() {
		int result = -1;
		try {
			Statement s = connection.createStatement();
			ResultSet results = s.executeQuery("SELECT COUNT(*) AS count FROM Stops");
			while(results.next()) { //will only execute once, because SELECT COUNT(*) returns just 1 number
				result = results.getInt(results.findColumn("count"));
				
			}
		}
		catch (SQLException sqle) { 
			error(sqle);
			
		}
		return result;
	}
	
	/**
	 * Returns the number of stops in the database,that are in a given Locality
	 * @param givenLocality is the given Location
	 * @return The number of stops that are in a given Locality in the database, or 0 if empty
	 *
	 */
	public int stopCountforLocality(String givenLocality) {
		int result = 0;
		try {
			
			
		
			PreparedStatement s = connection.prepareStatement("SELECT COUNT(*) AS Number FROM Stops WHERE LocalityName = ?");
			s.setString(1, givenLocality);
			//ResultSet results = s.executeQuery("SELECT COUNT(*) AS Number FROM Stops WHERE LocalityName = '" + givenLocality + "'");
			ResultSet results = s.executeQuery();
			
			
			while(results.next()) { //will only execute once, because SELECT COUNT(*) returns just 1 number
				result = results.getInt(results.findColumn("Number"));
				//System.out.println(result);
			}
			
		
			
			
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return result;
	}
	
	
	/**
	 * Returns the  stop details in the database,that are for a given Locality
	 * @param givenLocality
	 * @param givenstopType
	 * @return The  stop details in the database,that are for a given Locality or empty JSON array if there is none
	 *
	 */
	public JSONArray getStopDetailsForTransportType(String givenLocality, String givenstopType) {
		JSONArray returnArray = new JSONArray();
		
		try {
			
			
			
			PreparedStatement s = connection.prepareStatement("SELECT * FROM Stops WHERE LocalityName =? AND StopType =? ");
			s.setString(1,givenLocality);
			s.setString(2,givenstopType);
			ResultSet results = s.executeQuery();
			
			
			while(results.next()) { //will only execute once, because SELECT COUNT(*) returns just 1 number
				//int number = results.getInt(results.findColumn("Number"));
				
				JSONObject Part1Json = new JSONObject();
				JSONObject InnerPart1Json = new JSONObject();
				
				String name = "";
				String locality = "";
				String indicator = "";
				String bearing = "";
				String street = "";
				String landmark = "";
				String type = "";
				
				 name = results.getString(results.findColumn("CommonName"));
				 locality = results.getString(results.findColumn("LocalityName"));
				 indicator = results.getString(results.findColumn("Indicator"));
				 bearing = results.getString(results.findColumn("Bearing"));
				 street = results.getString(results.findColumn("Street"));
				 landmark = results.getString(results.findColumn("Landmark"));
				 type = results.getString(results.findColumn("StopType"));
				
				 
				 if(name== null) {name ="";}
				 if(locality== null) {locality ="";}
				 if(indicator== null) {indicator ="";}
				 if(bearing== null) {bearing ="";}
				 if(street== null) {street ="";}
				 if(landmark== null) {landmark ="";}
				 if(type== null) {type ="";}
				
				Part1Json.put("name" ,name);
				Part1Json.put("locality" ,locality);
				Part1Json.put("location", InnerPart1Json);
				InnerPart1Json.put("indicator" , indicator);
				InnerPart1Json.put("bearing", bearing);
				InnerPart1Json.put("street", street);
				InnerPart1Json.put("landmark", landmark);
				Part1Json.put("type" ,type);
				
				
				returnArray.put(Part1Json);
				
				//System.out.println(result);
			}
			
		
			
			
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return returnArray;
	}
	
	/**
	 * 
	 * @param Latitude provides the given latitude
	 * @param Longitude provides the given Longitute
	 * @param StopType provides the request StopType
	 * @return the  stop details of the 5 closest's stops to a given latitude and longitude
	 * 
	 *
	 */
	
	public String getClosest(String Latitude, String Longitude,String StopType ) {
		String returnString = "";
		
		try {
			
			
			
			PreparedStatement s = connection.prepareStatement("SELECT * FROM Stops WHERE StopType = ? AND ? IS NOT NULL AND ? IS NOT NULL"
					+ "ORDER BY  ("
					+ " ((53.472 - ?) * (53.472 - ?)) + "
					+ " (0.595 * ((-2.244 - ?) * (-2.244 - ?)))) ASC LIMIT 5");
			s.setString(1,StopType);
			s.setString(2,Latitude);
			s.setString(3,Longitude);
			s.setString(4,Latitude);
			s.setString(5,Latitude);
			s.setString(6,Longitude);
			s.setString(7,Longitude);
			ResultSet results = s.executeQuery();
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			Document doc = dbf.newDocumentBuilder().newDocument();
			
			while(results.next()) { //will only execute once, because SELECT COUNT(*) returns just 1 number
				//int number = results.getInt(results.findColumn("Number"));
				
				
				String code = results.getString(results.findColumn("NaptanCode"));
				String name = results.getString(results.findColumn("CommonName"));
				String locality = results.getString(results.findColumn("LocalityName"));
				//Location
				String street = results.getString(results.findColumn("Street"));
				String landmark = results.getString(results.findColumn("Landmark"));
				String LatitudeinXML = results.getString(results.findColumn("Latitude"));
				String LongitudeinXML = results.getString(results.findColumn("Longitude"));
				
			
			
				/////
				  // root elements
			     Element rootElement = doc.createElement("NearestStops");
			     doc.appendChild(rootElement);

			      // stop element
			      Element stop = doc.createElement("Stop");
			      rootElement.appendChild(stop);

			     
			      // name element
			      Element namexml = doc.createElement("Name");
			      stop.appendChild(namexml);

			      // locality element
			      Element localityxml = doc.createElement("Locality");
			      stop.appendChild(localityxml);

			      // location element
			      Element location = doc.createElement("Location");
			      stop.appendChild(location);

			      // street element
			      Element streetxml = doc.createElement("Street");
			      location.appendChild(streetxml);

			      // landmark element
			      Element landmarkxml = doc.createElement("Landmark");
			      location.appendChild(landmarkxml);

			      // latitude element
			      Element latitude = doc.createElement("Latitude");
			      location.appendChild(latitude);

			      // longitude element
			      Element longitudexml = doc.createElement("Longitude");
			      location.appendChild(longitudexml);
			      
			      
			      Transformer transformer =  TransformerFactory.newInstance().newTransformer();
			      Writer output = new StringWriter();
			      transformer.setOutputProperty(OutputKeys.INDENT,"yes");
			      transformer.transform(new DOMSource(doc), new StreamResult(output));
			      
			      returnString += output.toString();
			      
				
				
				//System.out.println(result);
			}
			
		
			
			
		}
		catch (SQLException | TransformerException | TransformerFactoryConfigurationError | ParserConfigurationException sqle) {
			//error(sqle);
			
		}
		return returnString;
	}
	
	
	
	/**
	 * Closes the connection to the database, required by AutoCloseable interface.
	 */
	@Override
	public void close() {
		try {
			if ( !connection.isClosed() ) {
				connection.close();
			}
		}
		catch(SQLException sqle) {
			error(sqle);
		}
	}

	/**
	 * Prints out the details of the SQL error that has occurred, and exits the programme
	 * @param sqle Exception representing the error that occurred
	 */
	private void error(SQLException sqle) {
		System.err.println("Problem Opening Database! " + sqle.getClass().getName());
		sqle.printStackTrace();
		System.exit(1);
	}
}
