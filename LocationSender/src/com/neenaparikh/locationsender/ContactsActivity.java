package com.neenaparikh.locationsender;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.neenaparikh.locationsender.comms.LoadContactsTask;
import com.neenaparikh.locationsender.model.Place;
import com.neenaparikh.locationsender.util.Constants;

public class ContactsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		
		// Get selected place object from the intent
		Place selectedPlace = getIntent().getParcelableExtra(Constants.SELECTED_PLACE_KEY);
		
		// Load contacts in a background thread
		new LoadContactsTask(this).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contacts, menu);
		return true;
	}

}
