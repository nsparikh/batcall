package com.neenaparikh.batsignal.util;


public class Constants {
	
	// API key for maps
	public static final String API_KEY = "AIzaSyB84Arz4_WbBVKIdkYvF5rpzaynKip71UM";

	// URL Sfor Places / Maps APIs
	public static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	public static final String REVERSE_GEOCODE_URL = "https://maps.google.com/maps/api/geocode/json?";

	// Radius and accuracy for location
	public static final int RADIUS = 1000;
	public static final float LOCATION_ACCURACY_THRESHOLD = 100.0f;
	
	public static final int MAP_ZOOM = 15;

	// List of types of nearby places that we want
	// TODO: change this
	public static final String TYPES = "cafe|restaurant";
	
	// Keys for passing the selected Place object between activities using Bundles
	public static final String SELECTED_PLACE_KEY = "selected_place";
	public static final String SELECTED_RECIPIENTS_KEY = "selected_recipients";
	public static final String SENDER_NAME_KEY = "sender_name";
	
	// Vibration time (in milliseconds)
	public static final int VIBRATION_TIME = 30;
	
	

}
