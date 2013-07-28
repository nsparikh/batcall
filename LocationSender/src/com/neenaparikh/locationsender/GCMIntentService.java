package com.neenaparikh.locationsender;

import java.io.IOException;
import java.net.URLEncoder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.neenaparikh.batsignal.deviceinfoendpoint.Deviceinfoendpoint;
import com.neenaparikh.batsignal.deviceinfoendpoint.model.DeviceInfo;
import com.neenaparikh.locationsender.R;
import com.neenaparikh.locationsender.model.Place;


/**
 * This class is started up as a service of the Android application. It listens
 * for Google Cloud Messaging (GCM) messages directed to this device.
 * 
 * When the device is successfully registered for GCM, a message is sent to the
 * App Engine backend via Cloud Endpoints, indicating that it wants to receive
 * broadcast messages from the it.
 */
public class GCMIntentService extends GCMBaseIntentService {
	private final Deviceinfoendpoint endpoint;

	public static final String PROJECT_NUMBER = "754255894724";
	

	/**
	 * Default constructor
	 */
	public GCMIntentService() {
		super(PROJECT_NUMBER);
		Deviceinfoendpoint.Builder endpointBuilder = new Deviceinfoendpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new JacksonFactory(),
				new HttpRequestInitializer() {
					public void initialize(HttpRequest httpRequest) {}
				});
		endpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();
	}

	/**
	 * Register the device for GCM.
	 * @param mContext The activity's context.
	 */
	public static void register(Context mContext) {
		GCMRegistrar.checkDevice(mContext);
		GCMRegistrar.checkManifest(mContext);
		GCMRegistrar.register(mContext, PROJECT_NUMBER);
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
	}

	/**
	 * Called back when a registration token has been received from the Google
	 * Cloud Messaging service.
	 * @param context The Context
	 */
	@Override
	public void onRegistered(Context context, String registration) {
		/*
		 * This is some special exception-handling code that we're using to work around a problem
		 * with the DevAppServer and methods that return null in App Engine 1.7.5.
		 */
		boolean alreadyRegisteredWithEndpointServer = false;

		try {
			// Using cloud endpoints, see if the device has already been
			DeviceInfo existingInfo = endpoint.getDeviceInfo(registration).execute();
			if (existingInfo != null && registration.equals(existingInfo.getDeviceRegistrationID())) {
				alreadyRegisteredWithEndpointServer = true;
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
				endpoint.insertDeviceInfo(
						deviceInfo
						.setDeviceRegistrationID(registration)
						.setTimestamp(System.currentTimeMillis())
						.setDeviceInformation(
								URLEncoder
								.encode(android.os.Build.MANUFACTURER
										+ " "
										+ android.os.Build.PRODUCT,
										"UTF-8"))).execute();
			}
		} catch (IOException e) {
			Log.e(GCMIntentService.class.getName(),
					"Exception received when attempting to register with server at "
							+ endpoint.getRootUrl(), e);

			return;
		}
		Log.i(GCMIntentService.class.getName(), "Registration success!");
	}

	/**
	 * Called back when the Google Cloud Messaging service has unregistered the
	 * device.
	 * 
	 * @param context The Context
	 */
	@Override
	protected void onUnregistered(Context context, String registrationId) {

		if (registrationId != null && registrationId.length() > 0) {

			try {
				endpoint.removeDeviceInfo(registrationId).execute();
			} catch (IOException e) {
				Log.e(GCMIntentService.class.getName(),
						"Exception received when attempting to unregister with server at "
								+ endpoint.getRootUrl(), e);
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
		Log.i(GCMIntentService.class.getName(), 
				"Message received via Google Cloud Messaging");
		
		// Get notification info from intent
		Place testPlace = new Place();
		testPlace.setName(intent.getStringExtra("name"));
		testPlace.setAddress(intent.getStringExtra("address"));
		testPlace.setLatitude(Double.parseDouble(intent.getStringExtra("latitude")));
		testPlace.setLongitude(Double.parseDouble(intent.getStringExtra("longitude")));
		testPlace.setDuration(Integer.parseInt(intent.getStringExtra("duration")));
		
		// TODO: get sender info?
		showNotification(testPlace, "neena", Long.parseLong(intent.getStringExtra("timestamp")));
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
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("Batsignal from " + senderName)
			.setContentText(senderName + " is at: " + place.getName() + " for " + place.getDuration())
			.setWhen(timestamp)
			.setVibrate(new long[] {0, 30})
			.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
			.setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0))
			.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, notification);
	}
}
