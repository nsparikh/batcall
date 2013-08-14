package com.neenaparikh.locationsender;

import java.io.IOException;
import java.net.URLEncoder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson.JacksonFactory;
import com.neenaparikh.locationsender.deviceinfoendpoint.Deviceinfoendpoint;
import com.neenaparikh.locationsender.deviceinfoendpoint.model.DeviceInfo;
import com.neenaparikh.locationsender.messageEndpoint.MessageEndpoint;
import com.neenaparikh.locationsender.model.Place;
import com.neenaparikh.locationsender.util.Constants;
import com.neenaparikh.locationsender.util.HelperMethods;


/**
 * This class is started up as a service of the Android application. It listens
 * for Google Cloud Messaging (GCM) messages directed to this device.
 * 
 * When the device is successfully registered for GCM, a message is sent to the
 * App Engine backend via Cloud Endpoints, indicating that it wants to receive
 * broadcast messages from the it.
 */
public class GCMIntentService extends GCMBaseIntentService {
	private Deviceinfoendpoint endpoint = null;
	

	/**
	 * Default constructor
	 */
	public GCMIntentService() {
		super(Constants.PROJECT_NUMBER);
	}

	/**
	 * Register the device for GCM.
	 * @param mContext The activity's context.
	 */
	public static void register(Context mContext) {
		GCMRegistrar.checkDevice(mContext);
		GCMRegistrar.checkManifest(mContext);
		GCMRegistrar.register(mContext, Constants.PROJECT_NUMBER);
	}

	/**
	 * Unregister the device from the GCM service. 
	 * @param mContext The activity's context.
	 */
	public static void unregister(Context mContext) {
		GCMRegistrar.unregister(mContext);
	}

	/**
	 * Called on registration error. This is called in the context of a Service - no dialog or UI.
	 * @param context The Context
	 * @param errorId An error message
	 */
	@Override
	public void onError(Context context, String errorId) {
		Log.e(GCMIntentService.class.getName(), "GCM registration failed. Check project number?");
		sendRegisterIntent(context, false, null);
	}

	/**
	 * Called back when a registration token has been received from the Google
	 * Cloud Messaging service.
	 * @param context The Context
	 */
	@Override
	public void onRegistered(Context context, String registration) {
		if (endpoint == null) endpoint = getAuthDeviceInfoEndpoint(context);
		
		/*
		 * This is some special exception-handling code that we're using to work around a problem
		 * with the DevAppServer and methods that return null in App Engine 1.7.5.
		 */
		boolean alreadyRegisteredWithEndpointServer = false;
		String senderName = null;

		try {
			// Using cloud endpoints, see if the device has already been registered
			DeviceInfo existingInfo = endpoint.getDeviceInfo(registration).execute();
			if (existingInfo != null && registration.equals(existingInfo.getDeviceRegistrationID())) {
				alreadyRegisteredWithEndpointServer = true;
				senderName = existingInfo.getUserName();
			}
		} catch (IOException e) {
			Log.e(GCMIntentService.class.getName(), "IOException: " + e.getMessage());
		}

		try {
			if (!alreadyRegisteredWithEndpointServer) {
				/*
				 * We are not registered as yet. Send an endpoint message
				 * containing the GCM registration id and some of the device's
				 * product information over to the backend. Then, we'll be
				 * registered.
				 */
				DeviceInfo deviceInfo = new DeviceInfo();
				deviceInfo.setDeviceRegistrationID(registration);
				deviceInfo.setTimestamp(System.currentTimeMillis());
				deviceInfo.setDeviceInformation(URLEncoder.encode(
						android.os.Build.MANUFACTURER + " " + android.os.Build.PRODUCT, "UTF-8"));
				
				// Set device phone number. Note: may only work for U.S. phone numbers!
				TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				String phone = tManager.getLine1Number();
				if (phone != null) deviceInfo.setPhoneNumber(HelperMethods.flattenPhone(phone));
						
				endpoint.insertDeviceInfo(deviceInfo).execute();
				senderName = endpoint.getDeviceInfo(registration).execute().getUserName();
			}
		} catch (IOException e) {
			Log.e(GCMIntentService.class.getName(),
					"Exception received when attempting to register with server at "
							+ endpoint.getRootUrl(), e);
			sendRegisterIntent(context, false, senderName);
			return;
		}
		Log.i(GCMIntentService.class.getName(), "Registration success!");
		sendRegisterIntent(context, true, senderName);
	}

	/**
	 * Called back when the Google Cloud Messaging service has unregistered the
	 * device.
	 * 
	 * @param context The Context
	 */
	@Override
	protected void onUnregistered(Context context, String registrationId) {
		if (endpoint == null) endpoint = getAuthDeviceInfoEndpoint(context);

		if (registrationId != null && registrationId.length() > 0) {

			try {
				endpoint.removeDeviceInfo(registrationId).execute();
				sendUnregisterIntent(context, true);
			} catch (IOException e) {
				Log.e(GCMIntentService.class.getName(),
						"Exception received when attempting to unregister with server at "
								+ endpoint.getRootUrl(), e);
				sendUnregisterIntent(context, false);
				return;
			}
		}

		Log.i(GCMIntentService.class.getName(), "Unregistration succeeded!");
	}


	/**
	 * Called when a cloud message has been received.
	 */
	@Override
	public void onMessage(Context context, Intent intent) {
		
		// Get notification info from intent
		Place testPlace = new Place();
		testPlace.setName(intent.getStringExtra(Constants.MESSAGE_PLACE_NAME_KEY));
		testPlace.setLatitude(Double.parseDouble(intent.getStringExtra(Constants.MESSAGE_PLACE_LATITUDE_KEY)));
		testPlace.setLongitude(Double.parseDouble(intent.getStringExtra(Constants.MESSAGE_PLACE_LONGITUDE_KEY)));
		testPlace.setDuration(Integer.parseInt(intent.getStringExtra(Constants.MESSAGE_DURATION_KEY)));
		
		// Get sender info?
		showNotification(testPlace, intent.getStringExtra(Constants.MESSAGE_SENDER_NAME_KEY), 
				Long.parseLong(intent.getStringExtra(Constants.MESSAGE_TIMESTAMP_KEY)));
	}
	
	/**
	 * Shows a notification of the message on the recipient device
	 * @param place the place where the sender is located
	 * @param senderName the name of the sender of the message
	 */
	private void showNotification(Place place, String senderName, Long timestamp) {
		
		// The intent which is triggered if the notification is selected.
		// Launches the place in the Maps application
		Intent notificationIntent = new Intent(Intent.ACTION_VIEW, place.getUri());
		Notification notification = new Notification.Builder(this)
			.setSmallIcon(R.drawable.batcall_ic_launcher)
			.setContentTitle("BatCall from " + senderName)
			.setContentText(senderName + " is at: " + place.getName() + " until " + 
					HelperMethods.getTimeAfterStart(timestamp, place.getDuration()))
			.setWhen(timestamp)
			.setVibrate(new long[] {0, 30})
			.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
			.setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0))
			.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, notification);
	}
	
	/**
	 * Sends an intent back to the activity from which this was launched.
	 * This is how we get information from this service back to the activity.
	 */
	private void sendRegisterIntent(Context context, boolean isSuccessful, String senderName) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra(Constants.GCM_INTENT_SERVICE_KEY, true);
		intent.putExtra(Constants.REGISTER_INTENT_SUCCESS_KEY, isSuccessful);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	/**
	 * Sends an intent upon unregistration. If successful, relaunch the application.
	 * If unsuccessful, notify the user.
	 */
	private void sendUnregisterIntent(Context context, boolean isSuccessful) {
		if (isSuccessful) {
			// Clear all shared prefs
			SharedPreferences sharedPrefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
			sharedPrefs.edit().clear().commit();
			
			// Relaunch app from the start
			Intent intent = new Intent(context, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			
		} else {
			Toast.makeText(context, "Unregistration unsuccessful. Please try again soon.", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	/**
	 * Retrieves an authenticated MessageEndpoint object.
	 * Should only be called after the user has been authenticated.
	 */
	public static MessageEndpoint getAuthMessageEndpoint(Context context) {
		// Get saved account name
		SharedPreferences sharedPrefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
		String accountName = sharedPrefs.getString(Constants.SHARED_PREFERENCES_ACCOUNT_NAME_KEY, null);

		// Retrieve credentials
		GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(context, Constants.CREDENTIAL_AUDIENCE);
		credential.setSelectedAccountName(accountName);

		// Create the message endpoint object with credentials
		MessageEndpoint.Builder endpointBuilder = new MessageEndpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new JacksonFactory(), credential);
		MessageEndpoint messageEndpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();
		return messageEndpoint;
	}


	/**
	 * Retrieves an authenticated DeviceInfoEndpoint object.
	 * Should only be called after the user has been authenticated.
	 */
	public static Deviceinfoendpoint getAuthDeviceInfoEndpoint(Context context) {
		// Get saved account name
		SharedPreferences sharedPrefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
		String accountName = sharedPrefs.getString(Constants.SHARED_PREFERENCES_ACCOUNT_NAME_KEY, null);

		// Retrieve credentials
		GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(context, Constants.CREDENTIAL_AUDIENCE);
		credential.setSelectedAccountName(accountName);

		// Create the message endpoint object with credentials
		Deviceinfoendpoint.Builder endpointBuilder = new Deviceinfoendpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new JacksonFactory(), credential);
		Deviceinfoendpoint endpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();
		return endpoint;
	}
	

}
