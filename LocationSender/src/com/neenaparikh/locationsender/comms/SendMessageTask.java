package com.neenaparikh.locationsender.comms;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.neenaparikh.batsignal.messageEndpoint.MessageEndpoint;
import com.neenaparikh.locationsender.CloudEndpointUtils;
import com.neenaparikh.locationsender.model.Place;

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
	 * @param messageEndpoint The MessageEndpoint object used to send the message
	 * 		to the AppEngine server
	 */
	public SendMessageTask(MessageEndpoint messageEndpoint) {
		this.messageEndpoint = messageEndpoint;

		// If no MessageEndpoint object is passed, create one of our own
		if (messageEndpoint == null) {
			MessageEndpoint.Builder endpointBuilder = new MessageEndpoint.Builder(
					AndroidHttp.newCompatibleTransport(),
					new JacksonFactory(),
					new HttpRequestInitializer() {
						public void initialize(HttpRequest httpRequest) { }
					});

			this.messageEndpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();
		}
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
