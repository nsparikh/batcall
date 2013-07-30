package com.neenaparikh.locationsender.comms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.neenaparikh.locationsender.messageEndpoint.MessageEndpoint;
import com.neenaparikh.locationsender.messageEndpoint.model.BooleanResult;
import com.neenaparikh.locationsender.model.Person;
import com.neenaparikh.locationsender.model.Place;
import com.neenaparikh.locationsender.util.HelperMethods;

/**
 * An asynchronous task to send a message in a background thread.
 * Constructor takes in a list of recipients as Person objects.
 * Task takes in Place object as a single parameter and returns a List of 
 * 	Person objects that represents those who the message couldn't be sent to
 * 	(because they have not registered with the app).
 * 
 * @author neenaparikh
 *
 */
public class SendMessageTask extends AsyncTask<Person, Void, List<Person>> {
	private MessageEndpoint messageEndpoint;
	private Place place;

	/**
	 * Default constructor. Takes in the recipients as a list of Person objects.
	 * @param context The context
	 */
	public SendMessageTask(Context context, Place place) {
		this.messageEndpoint = HelperMethods.getMessageEndpoint(context);
		this.place = place;
	}

	/**
	 * Sends the message. Takes in a single Place object as a parameter.
	 */
	@Override
	protected List<Person> doInBackground(Person... params) {
		List<Person> peopleNotFound = new ArrayList<Person>();

		for (Person recipient : params) {
			if (recipient.isRegistered()) {
				try {
					BooleanResult result = messageEndpoint.sendMessage(place.getName(), 
							place.getLatitude(), place.getLongitude(), place.getDuration(), 
							recipient.getRegisteredEmail(), recipient.getRegisteredPhone())
							.execute();

					// If sending the message was unsuccessful, add to the list
					if (!result.getResult()) {
						peopleNotFound.add(recipient);
						Log.e(SendMessageTask.class.getName(), "Cound not send message to " + recipient.getName());
					}
				} catch (IOException e) {
					Log.e(SendMessageTask.class.getName(), "IOException: " + e.getMessage());
					peopleNotFound.add(recipient);
				}
			} else {
				// TODO: do something if the person is not registered?
			}
			
		}

		return peopleNotFound;
	}

	/**
	 * Called after the task finishes.
	 */
	@Override
	protected void onPostExecute(List<Person> result) {
		if (result.size() == 0) {
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
