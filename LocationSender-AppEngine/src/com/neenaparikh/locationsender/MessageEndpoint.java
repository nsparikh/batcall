package com.neenaparikh.locationsender;

import java.io.IOException;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.persistence.EntityManager;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;


/**
 * A simple Cloud Endpoint that receives notifications from a web client
 * (<server url>/index.html), and broadcasts them to all of the devices that
 * were registered with this application (via DeviceInfoEndpoint).
 * 
 * NOTE: This endpoint does not use any form of authorization or authentication!
 * If this app is deployed, anyone can access this endpoint! If you'd like to
 * add authentication, take a look at the documentation.
 */
@Api(name = "messageEndpoint", namespace = @ApiNamespace(ownerDomain = "neenaparikh.com", ownerName = "neenaparikh.com", packagePath="batsignal"))
// NO AUTHENTICATION; OPEN ENDPOINT!
public class MessageEndpoint {
	private static final String API_KEY = "AIzaSyA8YrzfpyrPzbfSY-pKLpulbWOPnQ-YAlU";

	private static final DeviceInfoEndpoint endpoint = new DeviceInfoEndpoint();

	/**
	 * This accepts a message and persists it in the AppEngine datastore, it 
	 * will also broadcast the message to the appropriate devices
	 * via Google Cloud Messaging
	 *  
	 * @param message The entity to be inserted.
	 * @param recipients The recipients of the message
	 * @return 
	 * @throws IOException
	 */
	// TODO: add recipients!
	@ApiMethod(name = "sendMessage")
	public void sendMessage(@Named("name") String name, @Named("latitude") double latitude, 
			@Named("longitude") double longitude, @Named("duration") int duration) throws IOException {
		
		Logger.getLogger(MessageEndpoint.class.getName()).info("IN SEND MESSAGE");
		Sender sender = new Sender(API_KEY);

		// create a MessageData entity with a timestamp of when it was
		// received, and persist it
		MessageData messageObj = new MessageData();
		messageObj.setName(name);
		messageObj.setLatitude(latitude);
		messageObj.setLongitude(longitude);
		messageObj.setDuration(duration);
		messageObj.setTimestamp(System.currentTimeMillis());

		EntityManager mgr = getEntityManager();
		try {
			mgr.persist(messageObj);
		} finally {
			mgr.close();
		}
		// ping a max of 10 registered devices
		CollectionResponse<DeviceInfo> response = endpoint.listDeviceInfo(null, 10);
		for (DeviceInfo deviceInfo : response.getItems()) {
			Logger.getLogger(MessageEndpoint.class.getName()).info("SENDING MESSAGE TO " + deviceInfo.getDeviceRegistrationID());
			doSendViaGcm(messageObj, sender, deviceInfo);
		}
	}

	/**
	 * Sends the message using the Sender object to the registered device.
	 * 
	 * @param message The message to be sent in the GCM ping to the device.
	 * @param sender The Sender object to be used for ping,
	 * @param deviceInfo The registration id of the device.
	 * @return Result the result of the ping.
	 */
	private static Result doSendViaGcm(MessageData message, Sender sender,
			DeviceInfo deviceInfo) throws IOException {

		// This message object is a Google Cloud Messaging object, it is NOT 
		// related to the MessageData class
		Message msg = new Message.Builder().addData("name", message.getName())
				.addData("latitude", String.valueOf(message.getLatitude()))
				.addData("longitude", String.valueOf(message.getLongitude()))
				.addData("duration", String.valueOf(message.getDuration()))
				.addData("timestamp", String.valueOf(message.getTimestamp()))
				.build();

		// Send the message with up to 5 retries
		Result result = sender.send(msg, deviceInfo.getDeviceRegistrationID(), 5);
		if (result.getMessageId() != null) {
			String canonicalRegId = result.getCanonicalRegistrationId();
			if (canonicalRegId != null) {
				endpoint.removeDeviceInfo(deviceInfo.getDeviceRegistrationID());
				deviceInfo.setDeviceRegistrationID(canonicalRegId);
				endpoint.insertDeviceInfo(deviceInfo);
			}
		} else {
			String error = result.getErrorCodeName();
			if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
				endpoint.removeDeviceInfo(deviceInfo.getDeviceRegistrationID());
			}
		}

		return result;
	}

	private static EntityManager getEntityManager() {
		return EMF.get().createEntityManager();
	}
}
