package com.neenaparikh.locationsender;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.neenaparikh.locationsender.util.Constants;


public class MainActivity extends Activity {
	private static final int REQUEST_ACCOUNT_PICKER = 2;
	
	private SharedPreferences sharedPrefs;
	private GoogleAccountCredential credential;

	private Button retryButton;
	private Button signupButton;
	private ProgressSpinner progressSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_main);

		// Make sure Retry button isn't visible to start
		retryButton = (Button) findViewById(R.id.main_activity_retry_button);
		retryButton.setVisibility(View.GONE);

		// Check to see if user is authenticated / signed in
		sharedPrefs = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
		credential = GoogleAccountCredential.usingAudience(this, Constants.CREDENTIAL_AUDIENCE);
		setAccountName(sharedPrefs.getString(Constants.SHARED_PREFERENCES_ACCOUNT_NAME_KEY, null));

		signupButton = (Button) findViewById(R.id.main_activity_signup_button);
		if (credential.getSelectedAccountName() != null) {
			// Already signed in, begin app!
			signupButton.setVisibility(View.GONE);
			onSignIn();	
		} else {
			// Not signed in, show "Sign Up" button to prompt user to choose an account
			signupButton.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Called when the user has successfully authenticated / signed in.
	 */
	private void onSignIn() {
		progressSpinner = ProgressSpinner.show(this, null, null, true, false);
		signupButton.setVisibility(View.GONE);

		try {
			register();
		} catch (GooglePlayServicesAvailabilityIOException e) {
			// This means Google Play Services is out of date
			Toast.makeText(this, getString(R.string.out_of_date_message), Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Registers with GCM
	 * @throws GooglePlayServicesAvailabilityIOException
	 */
	private void register() throws GooglePlayServicesAvailabilityIOException {
		GCMIntentService.register(getApplicationContext());
	}

	/**
	 * Called when startActivityForResult() is called
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if (data != null && data.getExtras() != null) {
				String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					setAccountName(accountName);
					SharedPreferences.Editor editor = sharedPrefs.edit();
					editor.putString(Constants.SHARED_PREFERENCES_ACCOUNT_NAME_KEY, accountName);
					editor.commit();
					onSignIn();
				}
			}
			break;
		}
	}

	/**
	 * Sets the account name in the Shared Preferences and the Google Accounts Credential object
	 * @param accountName
	 */
	private void setAccountName(String accountName) {
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(Constants.SHARED_PREFERENCES_ACCOUNT_NAME_KEY, accountName);
		editor.commit();

		credential.setSelectedAccountName(accountName);
	}

	/**
	 * Called when a new intent is received to start this activity.
	 * 
	 * For this to work, android:launchMode="singleTop" must be set for this activity
	 * in AndroidManifest.xml.
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// First determine whether the intent came from GCMIntentService 
		if (intent.getBooleanExtra(Constants.GCM_INTENT_SERVICE_KEY, false)) {
			
			// Hide progress dialog
			if (progressSpinner != null) {
				progressSpinner.cancel();
				progressSpinner = null;
			}

			if (intent.getBooleanExtra(Constants.REGISTER_INTENT_SUCCESS_KEY, false)) {
				// If registration was successful, start NearbyPlacesActivity
				Intent nearbyPlacesIntent = new Intent(this, NearbyPlacesActivity.class);
				startActivity(nearbyPlacesIntent);
				finish();
			} else {
				// Otherwise, prompt the user to retry
				Toast.makeText(MainActivity.this, "Registration unsuccessful. Retry?", Toast.LENGTH_LONG).show();
				retryButton.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Called when the user clicks the "Retry" button. Just retries the registration process.
	 */
	public void onClickRetry(View view) {
		onSignIn();
	}
	
	/**
	 * Called when the user clicks the "Sign In" button
	 */
	public void onClickSignup(View view) {
		// request an account by showing an account picker
		startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}

}
