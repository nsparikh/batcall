package com.neenaparikh.locationsender.comms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.neenaparikh.locationsender.GCMIntentService;
import com.neenaparikh.locationsender.NearbyPlacesActivity;
import com.neenaparikh.locationsender.R;
import com.neenaparikh.locationsender.comms.SendMessageTask.MessageResult;
import com.neenaparikh.locationsender.deviceinfoendpoint.Deviceinfoendpoint;
import com.neenaparikh.locationsender.deviceinfoendpoint.model.CollectionResponseDeviceInfo;
import com.neenaparikh.locationsender.deviceinfoendpoint.model.DeviceInfo;
import com.neenaparikh.locationsender.messageEndpoint.MessageEndpoint;
import com.neenaparikh.locationsender.messageEndpoint.model.BooleanResult;
import com.neenaparikh.locationsender.model.Person;
import com.neenaparikh.locationsender.model.Place;
import com.neenaparikh.locationsender.util.Constants;

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
public class SendMessageTask extends AsyncTask<Person, Void, MessageResult> {
	private Activity activity; // the calling activity
	private Place place; // the place that the user selected to send
	private boolean isTextFallbackEnabled; // whether the user has enabled text fallback
	private ArrayList<Person> unsuccessfulRecipients; // a list of unsuccessful recipients
	private ProgressDialog pDialog; // the progress dialog
	private SharedPreferences sharedPrefs; // the Shared Preferences, for easy access
	
	private MessageEndpoint messageEndpoint;
	private Deviceinfoendpoint deviceInfoEndpoint;
	
	/**
	 * Default constructor. Takes in the Place object.
	 * @param activity the activity that called the task
	 * @param place the given Place object
	 */
	public SendMessageTask(Activity activity, Place place) {
		this.activity = activity;
		this.place = place;
		
		sharedPrefs = activity.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
		this.messageEndpoint = GCMIntentService.getAuthMessageEndpoint(activity);
		this.deviceInfoEndpoint = GCMIntentService.getAuthDeviceInfoEndpoint(activity);
		
		this.isTextFallbackEnabled = sharedPrefs.getBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, true);
		this.unsuccessfulRecipients = new ArrayList<Person>();
	}
	
	/**
	 * Before starting background thread, show progress dialog
	 **/
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		pDialog = new ProgressDialog(activity);
		pDialog.setMessage("Sending...");
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setCancelable(false);
		pDialog.show();
	}

	/**
	 * Sends the message. Takes in a list of Person objects.
	 */
	@Override
	protected MessageResult doInBackground(Person... params) {
		MessageResult messageResult = MessageResult.SUCCESS;
		ArrayList<Person> textMessageRecipients = new ArrayList<Person>();
		
		// Iterate through each person and send the message individually
		ArrayList<String> successfulRecipients = new ArrayList<String>();
		for (Person recipient : params) {
			try {
				ArrayList<String> regIds = checkPersonRegistration(recipient);

				if (regIds != null) {
					// Go through each of this recipient's registered devices and send the message
					for (String deviceId : regIds) {
						BooleanResult result = messageEndpoint.sendMessage(place.getName(), place.getLatitude(), place.getLongitude(), 
								place.getDuration(), deviceId).execute();
						if (result.getResult()) successfulRecipients.add(deviceId);
						else {
							unsuccessfulRecipients.add(recipient);
							messageResult = MessageResult.NOTIFICATION_FAILURE;
						}
					}
				} else if (isTextFallbackEnabled && recipient.hasPhones()) {
					textMessageRecipients.add(recipient);
				} else {
					unsuccessfulRecipients.add(recipient);
					messageResult = MessageResult.NOTIFICATION_FAILURE;
				}
			} catch (IOException e) {
				messageResult = MessageResult.NOTIFICATION_FAILURE;
			}
			
		}

		// Send text message to recipients who are unregistered, if any
		MessageResult smsMessageResult = MessageResult.SUCCESS;
		int numTextRecipients = textMessageRecipients.size();
		if (numTextRecipients > 0) {
			SendTextMessageTask textTask = new SendTextMessageTask(activity, place);
			textTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, textMessageRecipients.toArray(new Person[numTextRecipients]));
			try {
				boolean temp = textTask.get();
				if (!temp) smsMessageResult = MessageResult.SMS_FAILURE;
			} catch (InterruptedException e) {
				smsMessageResult = MessageResult.SMS_FAILURE;
			} catch (ExecutionException e) {
				smsMessageResult = MessageResult.SMS_FAILURE;
			}
		} 
		messageResult = messageResult.or(smsMessageResult);
		
		// Update lastContacted of all successful recipient devices in sharedprefs
		// Maps each device ID or phone number to the last contact time
		SharedPreferences.Editor editor = sharedPrefs.edit();
		for (String s : successfulRecipients) {
			editor.putLong(Constants.SHARED_PREFERENCES_LAST_CONTACTED_PREFIX + s, System.currentTimeMillis());
		}
		editor.commit();

		return messageResult;
	}

	/**
	 * Called after the task finishes.
	 */
	@Override
	protected void onPostExecute(MessageResult result) {
		pDialog.dismiss();
		
		switch (result) {
		case SUCCESS:
			Toast.makeText(activity, activity.getString(R.string.notification_response_sent), Toast.LENGTH_SHORT).show();

			// Launch back to NearbyPlacesActivity then close this activity
			Intent intent = new Intent(activity, NearbyPlacesActivity.class);
			activity.startActivity(intent);
			activity.finish();
			break;
		case NOTIFICATION_FAILURE:
			// Send was unsuccessful - show unsuccessful recipients' names
			displayUnsuccessfulRecipients(unsuccessfulRecipients);
			break;
		case SMS_FAILURE:
			// Couldn't send text message - display toast informing the user
			Toast.makeText(activity, activity.getString(R.string.sms_response_failure), Toast.LENGTH_SHORT).show();
			break;
		case FAILURE:
			displayUnsuccessfulRecipients(unsuccessfulRecipients);
			Toast.makeText(activity, activity.getString(R.string.sms_response_failure), Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		
	}
	
	/**
	 * Displays a dialog with the unsuccessful recipients' names
	 */
	private void displayUnsuccessfulRecipients(ArrayList<Person> personList) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Cannot send message");
		builder.setCancelable(false);
		
		String recipientsString = "";
		for (Person p : personList) recipientsString += "\n" + p.toString();
		builder.setMessage(activity.getString(R.string.notification_response_failure_prompt) + recipientsString);
		builder.setPositiveButton(activity.getString(R.string.ok), null);
		builder.create().show();
	}
	
	/**
	 * Given a Person object, checks in Shared Preferences for any emails or phones that have associated 
	 * registration IDs. If none are found in Shared Preferences, checks against the server and then
	 * updates the Shared Preferences accordingly.
	 * @param person the given person
	 * @throws IOException 
	 */
	private ArrayList<String> checkPersonRegistration(Person person) throws IOException {
		ArrayList<String> regIds = new ArrayList<String>();
		
		// First check in shared preferences
		for (String phone : person.getPhones()) {
			String regId = sharedPrefs.getString(Constants.SHARED_PREFERENCES_CONTACT_PHONE_PREFIX + phone, null);
			if (regId != null) regIds.add(regId);
		}
		for (String email : person.getEmails()) {
			Set<String> emailRegIds = sharedPrefs.getStringSet(Constants.SHARED_PREFERENCES_CONTACT_EMAIL_PREFIX + email, null);
			if (emailRegIds != null) regIds.addAll(emailRegIds);
		}
		if (regIds.size() > 0) return regIds;
		
		
		// Didn't find any in Shared Prefs, now go through again and check against server,
		//	and update shared preferences if necessary
		SharedPreferences.Editor editor = sharedPrefs.edit();
		
		for (String phone : person.getPhones()) {
			DeviceInfo matchedPhone = deviceInfoEndpoint.findDeviceByPhone(phone).execute();
			if (matchedPhone != null && matchedPhone.getDeviceRegistrationID() != null) {
				String regId = matchedPhone.getDeviceRegistrationID();
				regIds.add(regId);
				editor.putString(Constants.SHARED_PREFERENCES_CONTACT_PHONE_PREFIX + phone, regId);
			}
		}
		for (String email : person.getEmails()) {
			CollectionResponseDeviceInfo matchedEmails = deviceInfoEndpoint.findDevicesByEmail(email).execute();
			if (matchedEmails != null && matchedEmails.getItems() != null && matchedEmails.getItems().size() > 0) {
				HashSet<String> matchedEmailDeviceIds = new HashSet<String>();
				for (DeviceInfo matchedEmail : matchedEmails.getItems()) {
					String regId = matchedEmail.getDeviceRegistrationID();
					regIds.add(regId);
					matchedEmailDeviceIds.add(regId);
				}
				editor.putStringSet(Constants.SHARED_PREFERENCES_CONTACT_EMAIL_PREFIX + email, matchedEmailDeviceIds);
			}
		}
		editor.commit();
		
		if (regIds.size() == 0) return null;
		return regIds;
	}
	
	/**
	 * Enumeration class to represent the result of sending a message.
	 * 
	 * @author neenaparikh
	 *
	 */
	protected enum MessageResult {
		SUCCESS, SMS_FAILURE, NOTIFICATION_FAILURE, FAILURE;
		
		/**
		 * An "or" operation to compare two MessageResults
		 * 
		 * @param other
		 * @return
		 */
		protected MessageResult or(MessageResult other) {
			if (this.equals(SUCCESS)) return other;
			else if (other.equals(SUCCESS)) return this;
			else if (this.equals(FAILURE) || other.equals(FAILURE)) return FAILURE;
			else if (this.equals(NOTIFICATION_FAILURE)) {
				if (other.equals(NOTIFICATION_FAILURE)) return NOTIFICATION_FAILURE;
				else return FAILURE;
			} else if (this.equals(SMS_FAILURE)) {
				if (other.equals(SMS_FAILURE)) return SMS_FAILURE;
				else return FAILURE;
			} else return FAILURE;
		}
	}
}
