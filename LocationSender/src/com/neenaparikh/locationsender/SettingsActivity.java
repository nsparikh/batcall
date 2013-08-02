package com.neenaparikh.locationsender;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.Menu;

import com.neenaparikh.locationsender.util.Constants;

/**
 * Activity to show the settings panel.
 * 
 * @author neenaparikh
 *
 */
public class SettingsActivity extends Activity {
	private SettingsFragment settingsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get info from SharedPreferences
		SharedPreferences sharedPrefs = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
		boolean isTextFallbackEnabled = sharedPrefs.getBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, false);
		int numRecentContacts = sharedPrefs.getInt(Constants.SHARED_PREFERENCES_NUM_RECENT_CONTACTS_KEY, 0);
		
		Bundle bundle = new Bundle();
		bundle.putBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, isTextFallbackEnabled);
		bundle.putInt(Constants.SHARED_PREFERENCES_NUM_RECENT_CONTACTS_KEY, numRecentContacts);
		
		// Display the fragment
		settingsFragment = new SettingsFragment();
		settingsFragment.setArguments(bundle);
		getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	
	@Override
	public void onPause() {
		super.onPause();
		
		// Get info from fragment bundle
		Bundle bundle = settingsFragment.getArguments();
		boolean isTextFallbackEnabled = bundle.getBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY);
		int numRecentContacts = bundle.getInt(Constants.SHARED_PREFERENCES_NUM_RECENT_CONTACTS_KEY);
		
		// Save to SharedPreferences
		SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0).edit();
		editor.putBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, isTextFallbackEnabled);
		editor.putInt(Constants.SHARED_PREFERENCES_NUM_RECENT_CONTACTS_KEY, numRecentContacts);
		editor.commit();
		
	}
	
	
	/**
	 * PreferenceFragment class to display user preferences
	 * @author neenaparikh
	 *
	 */
	public static class SettingsFragment extends PreferenceFragment {
		
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.layout.settings);
			
			// Get saved info 
			boolean isTextFallbackEnabled = getArguments().getBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY);
			int numRecentContacts = getArguments().getInt(Constants.SHARED_PREFERENCES_NUM_RECENT_CONTACTS_KEY);
			
			// Set "text message fallback" checkbox based on user preference
			PreferenceCategory prefCategory = (PreferenceCategory) findPreference(
					getString(R.string.settings_preference_category_key));
			CheckBoxPreference textCheckBoxPref = (CheckBoxPreference) prefCategory.findPreference(
					getString(R.string.settings_text_checkbox_key));
			if (isTextFallbackEnabled) textCheckBoxPref.setChecked(true);
			else textCheckBoxPref.setChecked(false);
			
			// Set num recent contacts
			ListPreference numRecentContactsPref = (ListPreference) prefCategory.findPreference(
					getString(R.string.settings_num_recent_contacts_key));
			numRecentContactsPref.setValue(String.valueOf(numRecentContacts));
			
		}
		
		/**
		 * Save the user's preferences when leaving this fragment
		 */
		@Override
		public void onPause() {
			super.onPause();
			
			// Save preferences
			PreferenceCategory prefCategory = (PreferenceCategory) findPreference(
					getString(R.string.settings_preference_category_key));
			CheckBoxPreference textCheckBoxPref = (CheckBoxPreference) prefCategory.findPreference(
					getString(R.string.settings_text_checkbox_key));
			ListPreference numRecentContactsPref = (ListPreference) prefCategory.findPreference(
					getString(R.string.settings_num_recent_contacts_key));
			
			boolean isTextFallbackEnabled = textCheckBoxPref.isChecked();
			int numRecentContacts = Integer.parseInt(numRecentContactsPref.getValue());
			
			// Save to bundle
			getArguments().putBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, isTextFallbackEnabled);
			getArguments().putInt(Constants.SHARED_PREFERENCES_NUM_RECENT_CONTACTS_KEY, numRecentContacts);
		}
	}

}
