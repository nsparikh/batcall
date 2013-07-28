package com.neenaparikh.batsignal.comms;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.neenaparikh.batsignal.R;

/**
 * An asynchronous task to load the user's contact list in a background
 * thread, by using the Contacts Content Provider.
 * 
 * @author neenaparikh
 *
 */
public class LoadContactsTask extends AsyncTask<Void, Integer, ArrayList<String>> {
	
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

	private List<String> contactNamesList;

	public LoadContactsTask(Activity activity) {
		mActivity = activity;
		contactNamesList = new ArrayList<String>();
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
	protected ArrayList<String> doInBackground(Void... args) {
		return getAllContacts();
	}

	/**
	 * After the background task completes, display the results in the list view
	 */
	@Override
	protected void onPostExecute(ArrayList<String> result) {
		// Dismiss the dialog if it's showing
		if (pDialog.isShowing()) {
			pDialog.dismiss();
		}

		// Update the UI thread
		mActivity.runOnUiThread(new Runnable() {
			public void run() {

				// Bind the list of contact names to the list view
				final ListView contactsListView = (ListView) mActivity.findViewById(R.id.contacts_list_view);
				contactsListView.setAdapter(new ArrayAdapter<String>(
						mActivity, android.R.layout.simple_list_item_1, contactNamesList));

			}
		});
	}
	
	
	
	/**
	 * Helper method to retrieve the list of contacts from the phone.
	 * @return
	 */
	private ArrayList<String> getAllContacts() {
		ArrayList<String> contactNames = new ArrayList<String>();
		
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
			
			// Publish progress to show in progress dialog
			publishProgress(currentIndex);
			
			// Get ID and name
			int contactId = cursor.getInt(CONTACT_ID_INDEX);
			String name = cursor.getString(CONTACT_NAME_INDEX);
			contactNames.add(name);
			
			// Get photo URI
			Uri photoUri = Uri.EMPTY;
			String uriString = cursor.getString(CONTACT_PHOTO_URI_INDEX);
			if (uriString != null && uriString.length() > 0) {
				photoUri = Uri.parse(uriString);
			}
			
			// Get phone numbers, if any
			ArrayList<String> phoneNumbers = new ArrayList<String>();
			boolean hasPhone = Boolean.parseBoolean(cursor.getString(CONTACT_HAS_PHONE_INDEX));
			if (hasPhone) {
				Cursor phoneCursor = contentResolver.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
				phoneCursor.moveToFirst();
				while (phoneCursor.moveToNext()) {
					phoneNumbers.add(phoneCursor.getString(
							phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
				}
				phoneCursor.close();
			}
			
			currentIndex++;
		}
		
		cursor.close();
		return contactNames;
	}

}
