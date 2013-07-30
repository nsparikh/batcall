package com.neenaparikh.locationsender;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Named;
import javax.persistence.EntityManager;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;


/**
 * A simple Cloud Endpoint that receives notifications from a web client
 * (<server url>/index.html), and broadcasts them to all of the devices that
 * were registered with this application (via DeviceInfoEndpoint).
 */
@Api(
		name = "messageEndpoint", 
		namespace = @ApiNamespace(
				ownerDomain = "neenaparikh.com", 
				ownerName = "neenaparikh.com", 
				packagePath="locationsender"
		),
		clientIds = {"655975699066-c4qfm3pbqol9vgu47qafsln27o9e7k8l.apps.googleusercontent.com",
					 "655975699066.apps.googleusercontent.com"},
		audiences = {"655975699066-c4qfm3pbqol9vgu47qafsln27o9e7k8l.apps.googleusercontent.com"}
)
public class MessageEndpoint {
	private static final String API_KEY = "AIzaSyA8YrzfpyrPzbfSY-pKLpulbWOPnQ-YAlU";

	private static final DeviceInfoEndpoint endpoint = new DeviceInfoEndpoint();

	/**
	 * This accepts a message and persists it in the AppEngine datastore, it 
	 * will also broadcast the message to the appropriate devices
	 * via Google Cloud Messaging
	 *  
	 * @param placeName The place name
	 * @param latitude The latitude coordinate of the place
	 * @param longitude The longitude coordinate of the place
	 * @param duration The duration of the message
	 * @param recipientEmail The recipient's email address, if any
	 * @param recipientPhone The recipient's phone number, if any
	 * @param user The authenticated user who is sending the message
	 * @return 
	 * @throws IOException
	 * @throws SQLException 
	 * @throws OAuthRequestException 
	 */
	@ApiMethod(name = "sendMessage")
	public BooleanResult sendMessage(@Named("name") String placeName, 
			@Named("latitude") double latitude, 
			@Named("longitude") double longitude, 
			@Named("duration") int duration, 
			@Named("recipientEmail") String recipientEmail, 
			@Named("recipientPhone") String recipientPhone,
			User user) throws OAuthRequestException {

		if (user == null) {
			// This means we're not authenticated
			throw new OAuthRequestException("User is not authorized");
		}

		// Try to find user by email or phone. If not found, throw exception.
		DeviceInfo matchedDevice = null;
		if (recipientEmail != null) 
			matchedDevice = endpoint.findDeviceByEmail(recipientEmail);
		if (matchedDevice == null && recipientPhone != null) 
			matchedDevice = endpoint.findDeviceByPhone(recipientPhone);
		if (matchedDevice == null) return new BooleanResult(false); 

		// Now we have the recipient device - create the message and persist it

		// Create a MessageData entity and persist it
		MessageData messageObj = new MessageData();
		messageObj.setPlaceName(placeName);
		messageObj.setLatitude(latitude);
		messageObj.setLongitude(longitude);
		messageObj.setDuration(duration);
		messageObj.setTimestamp(System.currentTimeMillis());

		// Add details of the User who is sending the message
		messageObj.setSenderName(user.getNickname());
		messageObj.setSenderEmail(user.getEmail());
		messageObj.setSenderAuthDomain(user.getAuthDomain());
		messageObj.setSenderId(user.getUserId());
		messageObj.setSenderFederatedIdentity(user.getFederatedIdentity());

		EntityManager mgr = getEntityManager();
		try {
			mgr.persist(messageObj);
		} finally {
			mgr.close();
		}

		// Send message to recipient
		Sender sender = new Sender(API_KEY);
		try {
			doSendViaGcm(messageObj, sender, matchedDevice, user);
			return new BooleanResult(true);
		} catch (IOException e) {
			return new BooleanResult(false);
		}

	}

	/**
	 * Sends the message using the Sender object to the registered device.
	 * 
	 * @param message The message to be sent in the GCM ping to the device.
	 * @param sender The Sender object to be used for ping,
	 * @param deviceInfo The registration id of the device.
	 * @return Result the result of the ping.
	 * @throws OAuthRequestException 
	 */
	private static Result doSendViaGcm(MessageData message, Sender sender,
			DeviceInfo deviceInfo, User user) throws IOException, OAuthRequestException {

		// This message object is a Google Cloud Messaging object, it is NOT 
		// related to the MessageData class
		Message msg = new Message.Builder().addData("placeName", message.getPlaceName())
				.addData("latitude", String.valueOf(message.getLatitude()))
				.addData("longitude", String.valueOf(message.getLongitude()))
				.addData("duration", String.valueOf(message.getDuration()))
				.addData("timestamp", String.valueOf(message.getTimestamp()))
				.addData("senderName", message.getSenderName())
				.addData("senderEmail", message.getSenderEmail())
				.addData("senderAuthDomain", message.getSenderAuthDomain())
				.addData("senderId", message.getSenderId())
				.addData("senderFederatedIdentity", message.getSenderFederatedIdentity())
				.build();

		// Send the message with up to 5 retries
		Result result = sender.send(msg, deviceInfo.getDeviceRegistrationID(), 5);
		if (result.getMessageId() != null) {
			String canonicalRegId = result.getCanonicalRegistrationId();
			if (canonicalRegId != null) {
				endpoint.removeDeviceInfo(deviceInfo.getDeviceRegistrationID());
				deviceInfo.setDeviceRegistrationID(canonicalRegId);
				endpoint.insertDeviceInfo(deviceInfo, user);
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
	
	/**
	 * Custom boolean result class
	 */
	public class BooleanResult {
		private boolean result;
		private BooleanResult(boolean result) {
			this.result = result;
		}
		public boolean getResult() {
			return result;
		}
		public void setResult(boolean result) {
			this.result = result;
		}
	}
}
