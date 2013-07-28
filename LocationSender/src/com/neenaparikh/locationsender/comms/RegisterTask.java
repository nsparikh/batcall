package com.neenaparikh.locationsender.comms;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.neenaparikh.locationsender.GCMIntentService;
import com.neenaparikh.locationsender.NearbyPlacesActivity;

public class RegisterTask extends AsyncTask<Void, Void, Boolean> {
	private Activity mActivity;
	private ProgressDialog pDialog;

	public RegisterTask(Activity activity) {
		mActivity = activity;
	}

	/**
	 * Called before the task executes. Shows a progress dialog.
	 */
	@Override
	protected void onPreExecute() {
		pDialog = new ProgressDialog(mActivity);
		pDialog.setMessage("Registering...");
		pDialog.setCancelable(false);
		pDialog.show();
	}

	/**
	 * Registers the device.
	 */
	@Override
	protected Boolean doInBackground(Void... arg0) {
		try {
			GCMIntentService.register(mActivity.getApplicationContext());

		} catch (Exception e) {
			// Handled in onPostExecute
			return false;
		}
		return true;
	}

	/**
	 * Called when the registration task is finished.
	 */
	@Override
	protected void onPostExecute(Boolean result) {

		// Dismiss the dialog if it's showing
		if (pDialog.isShowing()) {
			pDialog.dismiss();
		}

		// TODO: prompt user to retry if unsuccesful?
		if (result) {
			Toast.makeText(mActivity, "Registration successful", Toast.LENGTH_SHORT).show();
			mActivity.startActivity(new Intent(mActivity, NearbyPlacesActivity.class));
			mActivity.finish();
		} else {
			Toast.makeText(mActivity, "Registration unsuccessful", Toast.LENGTH_SHORT).show();
			//mActivity.startActivity(new Intent(mActivity, MainActivity.class));
			//mActivity.finish();
		}
	}

}
