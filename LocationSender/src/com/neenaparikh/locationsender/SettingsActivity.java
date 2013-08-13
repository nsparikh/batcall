package com.neenaparikh.locationsender;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
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
		boolean isTextFallbackEnabled = sharedPrefs.getBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, true);
		
		Bundle bundle = new Bundle();
		bundle.putBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, isTextFallbackEnabled);
		
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
		
		// Save to SharedPreferences
		SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0).edit();
		editor.putBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, isTextFallbackEnabled);
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
			
			// Set "text message fallback" checkbox based on user preference
			PreferenceCategory prefCategory = (PreferenceCategory) findPreference(
					getString(R.string.settings_preference_category_key));
			CheckBoxPreference textCheckBoxPref = (CheckBoxPreference) prefCategory.findPreference(
					getString(R.string.settings_text_checkbox_key));
			if (isTextFallbackEnabled) textCheckBoxPref.setChecked(true);
			else textCheckBoxPref.setChecked(false);
			
			// Set behavior for the Delete Account "button"
			Preference deleteButton = (Preference) findPreference(getString(R.string.settings_delete_account_key));
			deleteButton.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					// Use the Builder class for convenient dialog construction
			        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			        builder.setMessage(R.string.delete_account_confirm_title)
			               .setPositiveButton(R.string.delete_account_confirm_button_text, new DialogInterface.OnClickListener() {
			                   public void onClick(DialogInterface dialog, int id) {
			                       // Unregister the account
			                	   GCMIntentService.unregister(getActivity().getApplicationContext());
			                   }
			               })
			               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			                   public void onClick(DialogInterface dialog, int id) {
			                       // User cancelled the dialog so do nothing
			                   }
			               });
			        // Create the AlertDialog object and show it
			        builder.create().show();
			        return true;
				}
				
			});
			
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
			
			boolean isTextFallbackEnabled = textCheckBoxPref.isChecked();
			
			// Save to bundle
			getArguments().putBoolean(Constants.SHARED_PREFERENCES_TEXT_ENABLED_KEY, isTextFallbackEnabled);
		}
	}

}
