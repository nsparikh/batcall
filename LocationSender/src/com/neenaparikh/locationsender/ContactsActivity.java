package com.neenaparikh.locationsender;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.neenaparikh.locationsender.comms.LoadContactsTask;
import com.neenaparikh.locationsender.model.Place;
import com.neenaparikh.locationsender.util.Constants;
import com.neenaparikh.locationsender.util.HelperMethods;

public class ContactsActivity extends Activity {
	private RelativeLayout sendButtonContainer;
	private TextView selectedPeopleNamesView;
	private Place selectedPlace;
	boolean firstTimeFlag = false; // So that contacts are not refreshed every time activity is resumed

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);

		// Make sure send button isn't visible to start
		sendButtonContainer = (RelativeLayout) findViewById(R.id.activity_contacts_send_button_container);
		selectedPeopleNamesView = (TextView) findViewById(R.id.activity_contacts_selected_person_text_view);
		sendButtonContainer.setVisibility(View.GONE);

		// Get selected place object from the intent
		selectedPlace = getIntent().getParcelableExtra(Constants.SELECTED_PLACE_KEY);
		
		firstTimeFlag = true;
		
		// If the user hasn't selected text message fallback option (ie if this is the first time using the app),
		//	display a prompt asking for the user's preference
		final SharedPreferences sharedPrefs = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
		if (!sharedPrefs.contains(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.settings_text_checkbox_title));
			builder.setMessage(getString(R.string.text_message_fallback_prompt));
			builder.setPositiveButton(getString(R.string.yes), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences.Editor editor = sharedPrefs.edit();
					editor.putBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, true);
					editor.commit();
				}
			});
			builder.setNegativeButton(getString(R.string.no), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences.Editor editor = sharedPrefs.edit();
					editor.putBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, false);
					editor.commit();
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Load contacts in a background thread
		if (firstTimeFlag) {
			boolean refresh = getIntent().getBooleanExtra(Constants.INTENT_REFRESH_KEY, false);
			new LoadContactsTask(this).execute(refresh);
			firstTimeFlag = false;
		} else {
			new LoadContactsTask(this).execute(false);
		}
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.action_refresh:
			Intent refreshIntent = new Intent(this, ContactsActivity.class);
			refreshIntent.putExtra(Constants.INTENT_REFRESH_KEY, true);
			refreshIntent.putExtra(Constants.SELECTED_PLACE_KEY, selectedPlace);
			startActivity(refreshIntent);
			finish();
			break;

		case R.id.action_settings:
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
			break;

		default:
			break;
		}

		return true;
	}

	/**
	 * Called when an item in the list view is checked / unchecked - updates the text view
	 * @param names The list of names to be displayed
	 */
	public void updateNamesList(ArrayList<String> names) {
		if (names.size() == 0) sendButtonContainer.setVisibility(View.GONE);
		else sendButtonContainer.setVisibility(View.VISIBLE);
		selectedPeopleNamesView.setText(HelperMethods.formatNames(names));
	}

	/**
	 * Called when the Send button is clicked. Launches a confirmation dialog.
	 * @param view
	 */
	public void onClickSendButton(View view) {
		ListView contactsListView = (ListView) findViewById(R.id.contacts_list_view);
		ContactsArrayAdapter adapter = (ContactsArrayAdapter) contactsListView.getAdapter();

		ConfirmationDialog cDialog = ConfirmationDialog.getInstance(adapter.getSelectedPersonList(), selectedPlace);
		cDialog.show(getFragmentManager(), "ConfirmationDialog");
	}
}
