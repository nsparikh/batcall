package com.neenaparikh.locationsender.util;


public class Constants {
	
	// API key and Project Number for AppEngine project
	public static final String API_KEY = "AIzaSyA8YrzfpyrPzbfSY-pKLpulbWOPnQ-YAlU";
	public static final String PROJECT_NUMBER = "655975699066";
	
	// Keys for SharedPreferences and Credentials
	public static final String SHARED_PREFERENCES_NAME = "LocationSender_SharedPreferences";
	public static final String SHARED_PREFERENCES_ACCOUNT_NAME_KEY = "saved_account_name";
	public static final int REQUEST_ACCOUNT_PICKER = 2;
	public static final String CREDENTIAL_AUDIENCE = "server:client_id:655975699066-c4qfm3pbqol9vgu47qafsln27o9e7k8l.apps.googleusercontent.com";
	
	
	// Key for passing registration messages from GCMIntentService to Activities
	public static final String GCM_INTENT_SERVICE_KEY = "GCM_intent_service";
	public static final String REGISTER_INTENT_SUCCESS_KEY = "is_successful";
	
	// Keys for accessing message object info
	public static final String MESSAGE_PLACE_NAME_KEY = "placeName";
	public static final String MESSAGE_PLACE_LATITUDE_KEY = "latitude";
	public static final String MESSAGE_PLACE_LONGITUDE_KEY = "longitude";
	public static final String MESSAGE_DURATION_KEY = "duration";
	public static final String MESSAGE_TIMESTAMP_KEY = "timestamp";
	public static final String MESSAGE_SENDER_NAME_KEY = "senderName";
	public static final String MESSAGE_SENDER_EMAIL_KEY = "senderEmail";
	public static final String MESSAGE_SENDER_AUTH_DOMAIN_KEY = "senderAuthDomain";
	public static final String MESSAGE_SENDER_ID_KEY = "senderId";
	public static final String MESSAGE_SENDER_FEDERATED_IDENTITY_KEY = "senderFederatedIdentity";

	// URLs for Places / Maps APIs
	public static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	public static final String REVERSE_GEOCODE_URL = "https://maps.google.com/maps/api/geocode/json?";

	// Radius and accuracy for location
	public static final int RADIUS = 1000;
	public static final float LOCATION_ACCURACY_THRESHOLD = 100.0f;
	
	public static final int MAP_ZOOM = 25;

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
