package com.neenaparikh.locationsender;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
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
		
		// Load contacts in a background thread
		boolean refresh = getIntent().getBooleanExtra(Constants.INTENT_REFRESH_KEY, false);
		new LoadContactsTask(this).execute(refresh);
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
