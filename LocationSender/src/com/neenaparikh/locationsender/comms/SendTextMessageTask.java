package com.neenaparikh.locationsender.comms;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.neenaparikh.locationsender.model.Person;
import com.neenaparikh.locationsender.model.Place;
import com.neenaparikh.locationsender.util.Constants;
import com.neenaparikh.locationsender.util.HelperMethods;

/**
 * An asynchronous task to send a message in a background thread.
 * Constructor takes in the Place that is the subject of the message
 * Task takes in a list of recipients as Person objects and returns a List of 
 * 	Person objects that represents those who the message couldn't be sent to
 * 	(because they have not registered with the app).
 * 
 * @author neenaparikh
 *
 */
public class SendTextMessageTask extends AsyncTask<Person, Person, Boolean> {
	private Activity activity;
	private boolean isTextFallbackEnabled;
	private SmsManager smsManager;
	private TelephonyManager telephonyManager;
	private String notificationMessage;
	
	private Object mLock = new Object();
    

	/**
	 * Default constructor. Takes in the Place object.
	 * @param activity The activity that called the task
	 * @param place The given Place object
	 */
	public SendTextMessageTask(Activity activity, Place place) {
		this.activity = activity;
		this.isTextFallbackEnabled = activity.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0)
				.getBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, false);
		this.smsManager = SmsManager.getDefault();
		this.telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
		this.notificationMessage = HelperMethods.formatTextMessage(place);
	}

	/**
	 * Sends the message. Takes in a list of Person objects.
	 */
	@Override
	protected Boolean doInBackground(Person... params) {
		if (telephonyManager.getSimState() != TelephonyManager.SIM_STATE_ABSENT) return false;
		
		boolean success = true;

		// Iterate through each person and send the message individually
		for (Person recipient : params) {
			if (isTextFallbackEnabled && recipient.hasPhones()) {
				// If the person is not registered and text fallback is enabled, send as text message
				final ArrayList<String> recipientPhones = recipient.getPhones();
				if (recipientPhones.size() == 1) {
					sendTextMessage(recipient.getPhones().get(0), notificationMessage);
				} else {
					publishProgress(recipient);
					try {
						synchronized(mLock) { mLock.wait(); }
					} catch (InterruptedException e) {
						Log.e(SendMessageTask.class.getName(), "InterruptedException: " + e.getMessage());
						success = false;
					}
				}	
			}
		}

		return success;
	}
	
	/**
	 * A way of accessing the UI thread in order to prompt the user for input.
	 * If a selected recipient has multiple phone numbers, prompt the user to choose
	 * which one to send the text message to.
	 */
	@Override
	protected void onProgressUpdate(Person... progress) {
		final Person recipient = progress[0];
		
		// Show dialog prompting user to select a phone number to send it to
		activity.runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle(recipient.getName() + "\nChoose a phone number");
				builder.setCancelable(false);
				final String[] items = recipient.getPhones().toArray(new String[recipient.getPhones().size()]);
				builder.setItems(items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String selectedPhone = items[which];
						sendTextMessage(HelperMethods.flattenPhone(selectedPhone), notificationMessage);
						synchronized(mLock) { mLock.notify(); }
					}

				});	
				builder.create().show();
			}
		});

	}

	
	/**
	 * Sends the text message to the given phone number.
	 * @param phone the phone number to which to send the text message
	 * @param message the message to send
	 */
	private void sendTextMessage(String phone, String message) {
		smsManager.sendTextMessage(phone, null, message, null, null);
	}
}
