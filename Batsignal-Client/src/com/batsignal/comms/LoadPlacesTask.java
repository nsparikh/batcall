package com.batsignal.comms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.batsignal.DurationSelectorDialog;
import com.batsignal.R;
import com.batsignal.model.Place;
import com.batsignal.model.PlaceList;
import com.batsignal.util.Constants;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

public class LoadPlacesTask extends AsyncTask<Location, String, String> {
	private static final String TAG = "LoadPlacesTask";
	
	private Activity mActivity;
	private ProgressDialog pDialog;

	private List<Place> nearbyPlacesList;
	private String currentAddress;
	private Location currentLocation;
	
	private HttpTransport httpTransport;

	public LoadPlacesTask(Activity activity) {
		mActivity = activity;
		nearbyPlacesList = new ArrayList<Place>();

		httpTransport = new NetHttpTransport();
	}

	/**
	 * Before starting background thread, show progress dialog
	 **/
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		pDialog = new ProgressDialog(mActivity);
		pDialog.setMessage("Retrieving location...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
	}

	/**
	 * Retrieves list of places in background thread
	 **/
	@Override
	protected String doInBackground(Location... args) {
		currentLocation = args[0];
		nearbyPlacesList = getNearbyPlaces(currentLocation, Constants.RADIUS, Constants.TYPES).getPlaceList();
		currentAddress = getAddressFromLocation(currentLocation);
		return null;
	}


	/**
	 * After the background task completes, display the results in the list view
	 */
	@Override
	protected void onPostExecute(String result) {
		// Dismiss the dialog if it's showing
		if (pDialog.isShowing()) {
			pDialog.dismiss();
		}

		// Update the UI thread
		mActivity.runOnUiThread(new Runnable() {
			public void run() {

				// Display current address, if any
				if (currentAddress != null && currentAddress.length() > 0) {
					TextView currentAddressTextView = (TextView) mActivity.findViewById(R.id.current_address_text_view);
					currentAddressTextView.setText("Current Location: " + currentAddress);
					
					// Set click listener to launch duration selector dialog
					currentAddressTextView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							Place currentPlace = new Place("", currentAddress, currentAddress, "", currentLocation);
							DurationSelectorDialog dialog = DurationSelectorDialog.getInstance(currentPlace);
							dialog.show(mActivity.getFragmentManager(), "DurationSelectorDialog");
						}
						
					});
				}

				// Bind the list of nearby places to the list view
				final ListView nearbyListView = (ListView) mActivity.findViewById(R.id.nearby_list_view);
				nearbyListView.setAdapter(new ArrayAdapter<Place>
					(mActivity, android.R.layout.simple_list_item_1, nearbyPlacesList));
				
				// Set on click listener for list view items to launch duration selector
				nearbyListView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Place selectedPlace = (Place) nearbyListView.getAdapter().getItem(position);
						DurationSelectorDialog dialog = DurationSelectorDialog.getInstance(selectedPlace);
						dialog.show(mActivity.getFragmentManager(), "DurationSelectorDialog");
					}
					
				});

			}
		});
	}

	/**
	 * Helper method to create an HTTP request factory for this application
	 * @param transport
	 * @return request factory
	 */
	private HttpRequestFactory createRequestFactory(HttpTransport transport) {
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
	 * Given a location, search radius, and place types, returns a list of
	 * nearby places that match the parameters.
	 * @return a PlaceList object which is a list of nearby places
	 */
	private PlaceList getNearbyPlaces(Location location, double radius, String types) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();

		// Initialize HTTP request
		HttpRequestFactory httpRequestFactory = createRequestFactory(httpTransport);
		HttpRequest request;
		try {
			// Send request to Google Places API with location, radius, and place types
			request = httpRequestFactory.buildGetRequest(new GenericUrl(Constants.PLACES_SEARCH_URL));

			request.getUrl().put("key", Constants.API_KEY);
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
	 * Takes in a Location object and returns an approximate corresponding address
	 * @return A formatted address corresponding to the current location (lat/lng)
	 */
	private String getAddressFromLocation(Location location) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();

		// Initialize HTTP request
		HttpRequestFactory httpRequestFactory = createRequestFactory(httpTransport);
		HttpRequest request;

		// Send request to Google Maps API to get address from location
		try {
			request = httpRequestFactory.buildGetRequest(new GenericUrl(Constants.REVERSE_GEOCODE_URL));
			request.getUrl().put("latlng", latitude + "," + longitude);
			request.getUrl().put("sensor", "false");

			PlaceList responseList = request.execute().parseAs(PlaceList.class);
			if (responseList.getPlaceList().size() > 0) return responseList.getPlaceList().get(0).getAddress();

		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}


		return "";
	}

}