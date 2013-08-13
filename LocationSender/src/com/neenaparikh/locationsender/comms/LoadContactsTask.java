package com.neenaparikh.locationsender.comms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.neenaparikh.locationsender.ContactsActivity;
import com.neenaparikh.locationsender.ContactsArrayAdapter;
import com.neenaparikh.locationsender.GCMIntentService;
import com.neenaparikh.locationsender.R;
import com.neenaparikh.locationsender.deviceinfoendpoint.Deviceinfoendpoint;
import com.neenaparikh.locationsender.deviceinfoendpoint.model.CollectionResponseDeviceInfo;
import com.neenaparikh.locationsender.deviceinfoendpoint.model.DeviceInfo;
import com.neenaparikh.locationsender.model.Person;
import com.neenaparikh.locationsender.util.Constants;
import com.neenaparikh.locationsender.util.HelperMethods;

/**
 * An asynchronous task to load the user's contact list in a background
 * thread, by using the Contacts Content Provider.
 * 
 * Task takes in boolean denoting whether to refresh the data or just load from saved preferences,
 * and returns a list of contacts as Person objects.
 * 
 * @author neenaparikh
 *
 */
public class LoadContactsTask extends AsyncTask<Boolean, Integer, ArrayList<Person>> {
	
	// Projection array and selection for Contacts cursor
	public static final String[] CONTACTS_PROJECTION = new String[] {
		ContactsContract.Contacts._ID,
		ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
		ContactsContract.Contacts.PHOTO_URI,
		ContactsContract.Contacts.HAS_PHONE_NUMBER
	};
	public static final String CONTACTS_SELECTION = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1";
	
	// Indicies for projection array
	public static final int CONTACT_ID_INDEX = 0;
	public static final int CONTACT_NAME_INDEX = 1;
	public static final int CONTACT_PHOTO_URI_INDEX = 2;
	public static final int CONTACT_HAS_PHONE_INDEX = 3;

	private ContactsActivity mActivity;
	private ProgressDialog pDialog;
	private String dialogMessage;
	private boolean success;
	
	private Deviceinfoendpoint endpoint;
	private SharedPreferences sharedPrefs;
	private boolean isTextFallbackEnabled; // if not, only display contacts who are registered
	


	public LoadContactsTask(ContactsActivity activity) {
		mActivity = activity;
		endpoint = GCMIntentService.getAuthDeviceInfoEndpoint(activity);
		isTextFallbackEnabled = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0)
				.getBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, false);
		sharedPrefs = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
	}

	/**
	 * Before starting background thread, show progress dialog
	 **/
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		pDialog = new ProgressDialog(mActivity);
		dialogMessage = "Retrieving contacts...\n(This may take a minute the first time you launch the app.)";
		pDialog.setMessage(dialogMessage);
		pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pDialog.setCancelable(false);
		pDialog.show();
	}
	
	/**
	 * Displays the update in the progress dialog
	 */
	@Override
	protected void onProgressUpdate(Integer... progress) {
		int p = progress[0];
		if (p == 0) {
			// Update the progress dialog message
			pDialog.setMessage(dialogMessage);
		}
		pDialog.setProgress(p);
	}

	/**
	 * Retrieves list of contact names in background thread
	 **/
	@Override
	protected ArrayList<Person> doInBackground(Boolean... args) {
		boolean refresh = args[0];

		try {
			ArrayList<Person> allContacts = new ArrayList<Person>();
			if (refresh) allContacts = getAllContacts();
			else allContacts = getSavedContacts();
			
			// If text message fallback is enabled, show all contacts who are registered
			// 	and those with phone numbers. Otherwise, only show those who are registered.
			ArrayList<Person> displayList = new ArrayList<Person>();
			for (Person p : allContacts) {
				if (p.isRegistered()) displayList.add(p);
				else if (isTextFallbackEnabled && p.hasPhones()) displayList.add(p);
			}
			success = true;
			return displayList;
			
		} catch (IOException e) {
			Log.e(LoadContactsTask.class.getName(), "IOException in doInBackground: " + e.getMessage());
			success = false;
			return new ArrayList<Person>();
		}
	}

	/**
	 * After the background task completes, display the results in the list view
	 */
	@Override
	protected void onPostExecute(final ArrayList<Person> resultList) {
		// Dismiss the dialog if it's showing
		if (pDialog.isShowing()) {
			pDialog.dismiss();
		}

		// Update the UI thread
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				
				// If we got a network exception, notify the user
				if (!success) {
					Toast.makeText(mActivity, "Server error. Please try again soon.", Toast.LENGTH_SHORT).show();
				} else {
					// Bind the list of all contact names to the list view
					final ListView contactsListView = (ListView) mActivity.findViewById(R.id.contacts_list_view);
					contactsListView.setAdapter(new ContactsArrayAdapter(mActivity, resultList));
				}
			}
		});
	}
	
	
	
	/**
	 * Retrieves the list of contacts from the phone using the Contacts Content Provider.
	 * @return a list of Person objects that represents all of the user's contacts.
	 * @throws IOException 
	 */
	private ArrayList<Person> getAllContacts() throws IOException {
		ArrayList<Person> contacts = new ArrayList<Person>();
		
		HashMap<String, Person> phoneMap = new HashMap<String, Person>();
		HashMap<String, Person> emailMap = new HashMap<String, Person>();
		
		// Initialize cursor for contacts 
		ContentResolver contentResolver = mActivity.getContentResolver();
		Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
				CONTACTS_PROJECTION, CONTACTS_SELECTION, null, null);
		
		// Get total number of contacts
		int numContacts = cursor.getCount();
		pDialog.setMax(numContacts);
		
		// Iterate over all contacts
		int currentIndex = 0;
		while (cursor.moveToNext()) {
			Person currentPerson = new Person();
			
			// Publish progress to show in progress dialog
			publishProgress(currentIndex);
			
			// Get ID and name
			int contactId = cursor.getInt(CONTACT_ID_INDEX);
			String name = cursor.getString(CONTACT_NAME_INDEX);
			currentPerson.setName(name);
			
			// Get photo URI
			Uri photoUri = Uri.EMPTY;
			String uriString = cursor.getString(CONTACT_PHOTO_URI_INDEX);
			if (uriString != null && uriString.length() > 0) {
				photoUri = Uri.parse(uriString);
			}
			currentPerson.setPhotoUri(photoUri);
			
			// Get phone numbers, if any
			ArrayList<String> phoneNumbers = new ArrayList<String>();
			Cursor phoneCursor = contentResolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
			while (phoneCursor.moveToNext()) {
				String phone = phoneCursor.getString(phoneCursor.getColumnIndex(
						ContactsContract.CommonDataKinds.Phone.NUMBER));
				String flattenedPhone = HelperMethods.flattenPhone(phone);
				phoneNumbers.add(flattenedPhone);
				phoneMap.put(flattenedPhone, currentPerson);
			}
			phoneCursor.close();
			currentPerson.setPhones(phoneNumbers);
			
			// Get email addresses, if any
			ArrayList<String> emailAddresses = new ArrayList<String>();
			Cursor emailCursor = contentResolver.query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, 
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
			while (emailCursor.moveToNext()) {
				String email = emailCursor.getString(
						emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
				String flattenedEmail = email.trim().toLowerCase(Locale.getDefault());
				emailAddresses.add(flattenedEmail);
				emailMap.put(flattenedEmail, currentPerson);
			}
			emailCursor.close();
			currentPerson.setEmails(emailAddresses);
			
			
			contacts.add(currentPerson);
			currentIndex++;
		}
		cursor.close();
		

		// Go through and find registered devices for all phone numbers in contact list
		ArrayList<String> phoneList = new ArrayList<String>(phoneMap.keySet());
		ArrayList<String> emailList = new ArrayList<String>(emailMap.keySet());
		dialogMessage = "Checking for registered friends...\n(This may take a minute the first time you launch the app.)";
		pDialog.setMax(phoneList.size() + emailList.size());
		publishProgress(0);
		
		// TODO: clean up this code!
		// Check against all phones for registered phone numbers
		for (int i = 0; i < phoneList.size(); i+=20) {
			publishProgress(i);
			String phoneListString = HelperMethods.stringListToString(
					phoneList.subList(i, Math.min(i+20, phoneList.size())));
			CollectionResponseDeviceInfo matchedPhoneDevices = endpoint.findDevicesByPhoneList(phoneListString).execute();
			if (matchedPhoneDevices != null && matchedPhoneDevices.getItems() != null) {
				for (DeviceInfo matchedPhoneDevice : matchedPhoneDevices.getItems()) {
					String matchedPhone = matchedPhoneDevice.getPhoneNumber();
					String deviceId = matchedPhoneDevice.getDeviceRegistrationID();
					Person p = phoneMap.get(matchedPhone);
					p.addDeviceRegistrationId(deviceId);
					
					// Check for last contact time of this deviceId
					p.setLastContacted(sharedPrefs.getLong(deviceId, p.getLastContacted()));
				}
			}
		}
		
		// Check against all emails for registered email addresses
		for (int i = 0; i < emailList.size(); i+=20) {
			publishProgress(i + phoneList.size());
			String emailListString = HelperMethods.stringListToString(
					emailList.subList(i, Math.min(i+20, emailList.size())));
			CollectionResponseDeviceInfo matchedEmailDevices = endpoint.findDevicesByEmailList(emailListString).execute();
			if (matchedEmailDevices != null && matchedEmailDevices.getItems() != null) {
				for (DeviceInfo matchedEmailDevice : matchedEmailDevices.getItems()) {
					String matchedEmail = matchedEmailDevice.getUserEmail(); 
					String deviceId = matchedEmailDevice.getDeviceRegistrationID();
					Person p = emailMap.get(matchedEmail);
					p.addDeviceRegistrationId(deviceId);
					
					// Check for last contact time of this deviceId
					p.setLastContacted(sharedPrefs.getLong(deviceId, p.getLastContacted()));
				}
			}	
		}

		// Save contacts to shared prefs
		savePersonList(contacts);
		
		Collections.sort(contacts);
		return contacts;
	}

	/**
	 * Retrieves the saved list of contacts from SharedPreferences.
	 * @return a list of saved contacts as Person objects
	 * @throws IOException 
	 */
	private ArrayList<Person> getSavedContacts() throws IOException {
		Set<String> savedPersonStrings = sharedPrefs.getStringSet(Constants.SHARED_PREFERENCES_SAVED_CONTACTS_KEY, new HashSet<String>());
		
		// If we haven't saved any, just call getAllContacts
		if (savedPersonStrings.size() == 0) {
			return getAllContacts();
		} else {
			pDialog.setMax(savedPersonStrings.size());
			
			// Retrieve Person objects from saved strings
			ArrayList<Person> personList = new ArrayList<Person>();
			
			int count = 0;
			for (String personString : savedPersonStrings) {
				publishProgress(count);
				Person p = Person.personFromJsonString(personString);
				if (p != null) {
					personList.add(p);
					
					// Check in shared prefs for device IDs to get last contact time
					for (String deviceId : p.getDeviceRegistrationIdList()) {
						long lastContacted = p.getLastContacted();
						p.setLastContacted(sharedPrefs.getLong(deviceId, lastContacted));
					}
				}
			}
			Collections.sort(personList);
			return personList;
		}
	}

	
	/**
	 * Saves the given list of person objects to the shared prefs
	 */
	private void savePersonList(ArrayList<Person> personList) {
		ArrayList<String> personStrings = new ArrayList<String>();
		for (Person p : personList) personStrings.add(p.toJsonString());
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putStringSet(Constants.SHARED_PREFERENCES_SAVED_CONTACTS_KEY, new HashSet<String>(personStrings));
		editor.commit();
	}
}

