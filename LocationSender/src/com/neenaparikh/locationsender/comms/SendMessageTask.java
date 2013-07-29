package com.neenaparikh.locationsender.comms;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.neenaparikh.batsignal.messageEndpoint.MessageEndpoint;
import com.neenaparikh.locationsender.model.Place;
import com.neenaparikh.locationsender.util.HelperMethods;

/**
 * An asynchronous task to send a message in a background thread.
 * 
 * @author neenaparikh
 *
 */
public class SendMessageTask extends AsyncTask<Place, Void, Boolean> {
	private MessageEndpoint messageEndpoint;

	/**
	 * Default constructor.
	 * @param context The context
	 */
	public SendMessageTask(Context context) {
		this.messageEndpoint = HelperMethods.getMessageEndpoint(context);
	}

	/**
	 * Sends the message. Takes in a single Place object as a parameter.
	 */
	@Override
	protected Boolean doInBackground(Place... params) {
		Place place = params[0];
		try {
			messageEndpoint.sendMessage(place.getName(), place.getLatitude(), 
					place.getLongitude(), place.getDuration()).execute();
		} catch (IOException e) {
			Log.e(SendMessageTask.class.getName(), "IOException: " + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * Called after the task finishes.
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			// Send was successful
			// TODO: show toast?
			Log.i(SendMessageTask.class.getName(), "Message sent!");
		} else {
			// Send was unsuccessful
			// TODO: notify user
			Log.e(SendMessageTask.class.getName(), "Message send unsuccessful :(");
		}
	}

}
