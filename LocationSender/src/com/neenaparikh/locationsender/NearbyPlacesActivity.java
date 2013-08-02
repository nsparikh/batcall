package com.neenaparikh.locationsender;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.neenaparikh.locationsender.comms.LoadPlacesTask;
import com.neenaparikh.locationsender.util.Constants;

/**
 * Displays the user's nearby places using the Google Places API.
 * @author neenaparikh
 *
 */
public class NearbyPlacesActivity extends Activity {
	private static final String TAG = "NearbyPlacesActivity";

	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	private Location currentLocation;


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
				
				// We only want to update the location if it is accurate enough
				if (location.getAccuracy() < Constants.LOCATION_ACCURACY_THRESHOLD) {
					currentLocation = location;
					new LoadPlacesTask(NearbyPlacesActivity.this, false).execute(currentLocation);
					
					// Once we have an accurate enough location, we no longer need to 
					// receive location updates so remove the location manager updates
					mLocationManager.removeUpdates(this);
				}
				
			}

			@Override
			public void onProviderDisabled(String provider) {
				Log.i(TAG, "Location provider disabled");				
			}

			@Override
			public void onProviderEnabled(String provider) {
				Log.i(TAG, "Location provider enabled");	
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				Log.i(TAG, "Location listener status changed");	
			}			
		};
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mLocationListener);
		
		// Initialize currentLocation to be the last known location
		// Then get the list of nearby places of that location
		currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (currentLocation == null) currentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (currentLocation != null) new LoadPlacesTask(this, true).execute(currentLocation);
	}
	
	public void removeLocationUpdates() {
		mLocationManager.removeUpdates(mLocationListener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			Intent refreshIntent = new Intent(this, NearbyPlacesActivity.class);
			refreshIntent.putExtra(Constants.INTENT_REFRESH_KEY, true);
			startActivity(refreshIntent);
			finish();
			break;
			
		case R.id.action_settings:
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
			break;
			
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}



}