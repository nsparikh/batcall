package com.neenaparikh.locationsender.comms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.neenaparikh.locationsender.GCMIntentService;
import com.neenaparikh.locationsender.NearbyPlacesActivity;
import com.neenaparikh.locationsender.messageEndpoint.MessageEndpoint;
import com.neenaparikh.locationsender.messageEndpoint.model.BooleanResult;
import com.neenaparikh.locationsender.model.Person;
import com.neenaparikh.locationsender.model.PersonTimeComparator;
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
public class SendMessageTask extends AsyncTask<Person, Void, List<Person>> {
	private MessageEndpoint endpoint;
	private Activity activity;
	private Place place;
	private ArrayList<Person> successfulRecipients;

	/**
	 * Default constructor. Takes in the Place object.
	 * @param activity The activity that called the task
	 * @param place The given Place object
	 */
	public SendMessageTask(Activity activity, Place place) {
		this.endpoint = GCMIntentService.getAuthMessageEndpoint(activity);
		this.activity = activity;
		this.place = place;
		this.successfulRecipients = new ArrayList<Person>();
	}

	/**
	 * Sends the message. Takes in a list of Person objects.
	 */
	@Override
	protected List<Person> doInBackground(Person... params) {
		List<Person> peopleNotFound = new ArrayList<Person>();

		// Iterate through each person and send the message individually
		for (Person recipient : params) {
			System.out.println(recipient.getName());
			ArrayList<String> recipientDeviceList = recipient.getDeviceRegistrationIdList();
			for (String id : recipientDeviceList) System.out.println(id);
			try {
				// Go through each of this recipient's registered devices and send the message
				for (String deviceId : recipient.getDeviceRegistrationIdList()) {
					BooleanResult result = endpoint.sendMessage(place.getName(), place.getLatitude(), place.getLongitude(), 
							place.getDuration(), deviceId).execute();
					if (result.getResult()) successfulRecipients.add(recipient);
					else peopleNotFound.add(recipient);
				}
			} catch (IOException e) {
				Log.e(SendMessageTask.class.getName(), "IOException: " + e.getMessage());
				e.printStackTrace();
				peopleNotFound.add(recipient);
			}

		}

		return peopleNotFound;
	}

	/**
	 * Called after the task finishes.
	 */
	@Override
	protected void onPostExecute(List<Person> result) {
		// TODO: make a helper method to edit shared prefs?
		// Get recent contacts list from shared prefs
		SharedPreferences sharedPrefs = activity.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
		Set<String> recentContactStrings = sharedPrefs.getStringSet(Constants.SHARED_PREFERENCES_SAVED_CONTACTS_KEY, new HashSet<String>());
		ArrayList<Person> allRecentContacts = new ArrayList<Person>();
		for (String recentContactString : recentContactStrings) 
			allRecentContacts.add(Person.personFromJsonString(recentContactString));
		Collections.sort(allRecentContacts, new PersonTimeComparator()); // sort by most recent first
		
		// Replace older contacts in list with successful recipients
		int numSuccessfulRecipients = successfulRecipients.size();
		for (int i = 0; i < numSuccessfulRecipients; i++) {
			allRecentContacts.remove(allRecentContacts.size()-1); // Remove the oldest numSuccessfulRecipients objects
		}
		for (Person p : successfulRecipients) allRecentContacts.add(p);
		// No need to resort because the Set<String> in shared prefs isn't sorted anyway
		
		// TODO: make a helper method to convert ArrayList<Person> to Set<String> ?
		// Save to shared prefs
		ArrayList<String> newRecentContactStrings = new ArrayList<String>();
		for (Person p : allRecentContacts) newRecentContactStrings.add(p.toJsonString());
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putStringSet(Constants.SHARED_PREFERENCES_RECENT_CONTACTS_KEY, new HashSet<String>(newRecentContactStrings));
		editor.commit();
		
		// Notify the user based on result
		if (result.size() == 0) {
			// Send was successful
			Toast.makeText(activity, "Message sent!", Toast.LENGTH_SHORT).show();
			
			// Launch back to NearbyPlacesActivity then close this activity
			Intent intent = new Intent(activity, NearbyPlacesActivity.class);
			activity.startActivity(intent);
			activity.finish();
		} else {
			// Send was unsuccessful
			// TODO show names of unsuccessful recipients?
			Toast.makeText(activity, "Unable to send message. Please try again soon.", Toast.LENGTH_LONG).show();
		}
	}

}
