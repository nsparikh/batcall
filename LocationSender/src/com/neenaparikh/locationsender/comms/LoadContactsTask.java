package com.neenaparikh.locationsender.comms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ListView;

import com.neenaparikh.locationsender.ContactsArrayAdapter;
import com.neenaparikh.locationsender.R;
import com.neenaparikh.locationsender.model.Person;
import com.neenaparikh.locationsender.util.HelperMethods;

/**
 * An asynchronous task to load the user's contact list in a background
 * thread, by using the Contacts Content Provider.
 * 
 * Task takes in no paremeters and returns a list of contacts as Person objects.
 * 
 * @author neenaparikh
 *
 */
public class LoadContactsTask extends AsyncTask<Void, Integer, ArrayList<Person>> {
	
	// Projection array and selection for Contacts cursor
	// TODO: change this to get necessary info from contacts once backend is setup
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

	private Activity mActivity;
	private ProgressDialog pDialog;
	


	public LoadContactsTask(Activity activity) {
		mActivity = activity;
	}

	/**
	 * Before starting background thread, show progress dialog
	 **/
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		pDialog = new ProgressDialog(mActivity);
		pDialog.setMessage("Retrieving contacts...");
		pDialog.setCancelable(false);
		pDialog.show();
	}
	
	/**
	 * Displays the update in the progress dialog
	 */
	@Override
	protected void onProgressUpdate(Integer... progress) {
		pDialog.setProgress(progress[0]);
	}

	/**
	 * Retrieves list of contact names in background thread
	 **/
	@Override
	protected ArrayList<Person> doInBackground(Void... args) {
		try {
			return getAllContacts();
		} catch (IOException e) {
			Log.e(LoadContactsTask.class.getName(), "IOException: " + e.getMessage());
			return new ArrayList<Person>();
		}
	}

	/**
	 * After the background task completes, display the results in the list view
	 */
	@Override
	protected void onPostExecute(final ArrayList<Person> result) {
		// Dismiss the dialog if it's showing
		if (pDialog.isShowing()) {
			pDialog.dismiss();
		}

		// Update the UI thread
		mActivity.runOnUiThread(new Runnable() {
			public void run() {

				// Bind the list of contact names to the list view
				final ListView contactsListView = (ListView) mActivity.findViewById(R.id.contacts_list_view);
				contactsListView.setAdapter(new ContactsArrayAdapter(mActivity, R.layout.contact_list_item, result));

			}
		});
	}
	
	
	
	/**
	 * Helper method to retrieve the list of contacts from the phone.
	 * @return
	 * @throws IOException 
	 */
	private ArrayList<Person> getAllContacts() throws IOException {
		ArrayList<Person> contacts = new ArrayList<Person>();
		
		// Initialize cursor for contacts 
		ContentResolver contentResolver = mActivity.getContentResolver();
		Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
				CONTACTS_PROJECTION, CONTACTS_SELECTION, null, null);
		cursor.moveToFirst();
		
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
			boolean hasPhone = Boolean.parseBoolean(cursor.getString(CONTACT_HAS_PHONE_INDEX));
			if (hasPhone) {
				Cursor phoneCursor = contentResolver.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
				phoneCursor.moveToFirst();
				while (phoneCursor.moveToNext()) {
					String phone = phoneCursor.getString(phoneCursor.getColumnIndex(
							ContactsContract.CommonDataKinds.Phone.NUMBER));
					phoneNumbers.add(HelperMethods.flattenPhone(phone));
				}
				phoneCursor.close();
			}
			currentPerson.setPhones(phoneNumbers);
			
			// TODO: Check for registered devices with each phone number
			
			
			// Get email addresses, if any
			ArrayList<String> emailAddresses = new ArrayList<String>();
			Cursor emailCursor = contentResolver.query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, 
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
			emailCursor.moveToFirst();
			while (emailCursor.moveToNext()) {
				emailAddresses.add(emailCursor.getString(
						emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
			}
			emailCursor.close();
			currentPerson.setEmails(emailAddresses);
			
			// TODO: Check for registered devices with email addresses if necessary
			
			
			contacts.add(currentPerson);
			currentIndex++;
		}
		
		cursor.close();
		
		Collections.sort(contacts);
		return contacts;
	}

}
