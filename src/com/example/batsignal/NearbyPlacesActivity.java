package com.example.batsignal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.batsignal.model.Place;
import com.example.batsignal.model.PlaceList;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * Displays the user's nearby places using the Google Places API.
 * @author neenaparikh
 *
 */
public class NearbyPlacesActivity extends Activity {
	
	// Location manager and listener for getting current location
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	private Location currentLocation;
	private String currentAddress;
	
	// List of nearby places
	private List<Place> nearbyPlacesList = new ArrayList<Place>();
	
	// API key for maps
	private static final String API_KEY = "AIzaSyB84Arz4_WbBVKIdkYvF5rpzaynKip71UM";
	
	// URL Sfor Places / Maps APIs
	private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	private static final String REVERSE_GEOCODE_URL = "https://maps.google.com/maps/api/geocode/json?";

	// Global instance of the HTTP transport
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	
	// Radius for nearby range
	private static final int RADIUS = 1000;
	
	// List of types of nearby places that we want
	private static final String TYPES = "cafe|restaurant";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby_places);
		
		
		// Initialize location manager and listener in order to get current location
		currentLocation = null;
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				currentLocation = location;
				new LoadPlacesTask().execute();
			}

			@Override
			public void onProviderDisabled(String provider) {}

			@Override
			public void onProviderEnabled(String provider) {}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			
		};  
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
	
		// Initialize the current location to last known GPS or network location, if any
		currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (currentLocation == null) currentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (currentLocation != null) new LoadPlacesTask().execute();

	}

	/**
	 * @return a PlaceList object which is a list of nearby places
	 */
	private PlaceList getNearbyPlaces(Location location, double radius, String types) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		
		// Initialize HTTP request
		HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
        HttpRequest request;
		try {
			// Send request to Google Places API with location, radius, and place types
			request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
			
			request.getUrl().put("key", API_KEY);
	        request.getUrl().put("location", latitude + "," + longitude);
	        request.getUrl().put("radius", radius); // in meters
	        request.getUrl().put("sensor", "false");
	        if(types != null) request.getUrl().put("types", types);

	        PlaceList list = request.execute().parseAs(PlaceList.class);
	        return list;
	        
		} catch (IOException e) {
			Log.e("Error:", e.getMessage());
			return null;
		}
        
	}
	
	/**
	 * @return a formatted address corresponding to the current location (lat/lng)
	 */
	private String getAddressFromLocation(Location location) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();

		// Initialize HTTP request
		HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
		HttpRequest request;
		
		// Send request to Google Maps API to get address from location
		try {
			request = httpRequestFactory.buildGetRequest(new GenericUrl(REVERSE_GEOCODE_URL));
			request.getUrl().put("latlng", latitude + "," + longitude);
			request.getUrl().put("sensor", "false");
			
			PlaceList responseList = request.execute().parseAs(PlaceList.class);
			if (responseList.getPlaceList().size() > 0) return responseList.getPlaceList().get(0).getAddress();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return "";
	}

	
	/**
	 * @param transport
	 * @return request factory
	 */
	private static HttpRequestFactory createRequestFactory(HttpTransport transport) {
	    return transport.createRequestFactory(new HttpRequestInitializer() {
	        @Override
	        public void initialize(HttpRequest request) throws IOException {
	        	HttpHeaders headers = new HttpHeaders();
	        	headers.setUserAgent("Batsignal");
                request.setHeaders(headers);
                JsonObjectParser parser = new JsonObjectParser(new JacksonFactory());
                request.setParser(parser);
	        }
	    });
	}
	
	/**
	 * Async class to perform data loading in background
	 */
	private class LoadPlacesTask extends AsyncTask<String, String, String> {
		
		 /**
         * Before starting background thread Show Progress Dialog
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            
            // TODO: create Progress Dialog type and show progress
            /*pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/
        }

        /**
         * Retrieves list of places in background thread
         **/
        @Override
        protected String doInBackground(String... args) {
        	nearbyPlacesList = getNearbyPlaces(currentLocation, RADIUS, TYPES).getPlaceList();
        	currentAddress = getAddressFromLocation(currentLocation);
        	return null;
        }
        
        
        /**
         * After the background task completes, display the results in the list view
         */
        @Override
        protected void onPostExecute(String result) {
        	//TODO: pDialog.dismiss();
        	
        	// Update the UI thread
        	runOnUiThread(new Runnable() {
        		public void run() {
        			
        			// Display current address, if any
        			if (currentAddress.length() > 0) {
        				TextView currentAddressTextView = (TextView) findViewById(R.id.current_address_text_view);
        				currentAddressTextView.setText("Current Location: " + currentAddress);
        			}

        			// Bind the list of nearby places to the list view
        			ListView nearbyListView = (ListView) findViewById(R.id.nearby_list_view);
        			nearbyListView.setAdapter(new ArrayAdapter<Place>
        				(NearbyPlacesActivity.this, android.R.layout.simple_list_item_1, nearbyPlacesList));
        			
        		}
        	});
        }

	}

}
