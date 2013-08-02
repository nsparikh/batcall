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
	 * will also broadcast the message to the recipient device(s) associated with 
	 * the given phone number or email address (if any are found) via Google Cloud Messaging
	 *  
	 * @param placeName The name of the place associated with this message
	 * @param latitude The latitude coordinate of the place
	 * @param longitude The longitude coordinate of the place
	 * @param duration The duration of this message
	 * @param deviceRegistrationId The recipient's device registration ID
	 * @return A BooleanResult object indicating whether the message was sent successfully
	 * @throws IOException
	 * @throws OAuthRequestException 
	 */
	@ApiMethod(name = "sendMessage")
	public BooleanResult sendMessage(@Named("placeName") String placeName, 
			@Named("latitude") double latitude,
			@Named("longitude") double longitude,
			@Named("duration") int duration,
			@Named("deviceRegistrationId") String deviceRegistrationId,
			User user) throws OAuthRequestException {
		
		if (user == null) {
			// This means we're not authorized
			throw new OAuthRequestException("User not authorized");
		}
		
		// Find recipient device, if any
		DeviceInfo matchedDevice = endpoint.getDeviceInfo(deviceRegistrationId);
		if (matchedDevice == null) return new BooleanResult(false);
		
		Sender sender = new Sender(API_KEY);
		
		// Create MessageData object with place information
		MessageData messageObj = new MessageData();
		messageObj.setPlaceName(placeName);
		messageObj.setLatitude(latitude);
		messageObj.setLongitude(longitude);
		messageObj.setDuration(duration);
		messageObj.setTimestamp(System.currentTimeMillis());
		
		// Add sender info
		messageObj.setSenderName(user.getNickname());
		messageObj.setSenderEmail(user.getEmail());
		messageObj.setSenderAuthDomain(user.getAuthDomain());
		messageObj.setSenderFederatedIdentity(user.getFederatedIdentity());
		messageObj.setSenderId(user.getUserId());
		
		// Persist the object to the datastore
		EntityManager mgr = getEntityManager();
		try {
			mgr.persist(messageObj);
		} finally {
			mgr.close();
		}
		
		// Send object to recipients
		try {
			doSendViaGcm(messageObj, sender, matchedDevice, user);
			return new BooleanResult(true);
		} catch (IOException e) {
			Logger.getLogger(MessageEndpoint.class.getName()).severe("IOException in sendMessage(): " + e.getMessage());
			return new BooleanResult(false);
		}

	}

	/**
	 * Sends the message using the Sender object to the registered device.
	 * 
	 * @param messageObj The message to be sent in the GCM ping to the device
	 * @param sender The Sender object to be used for ping
	 * @param deviceInfo The recipient device
	 * @return Result the result of the ping
	 * @throws OAuthRequestException 
	 */
	private static Result doSendViaGcm(MessageData messageObj, Sender sender,
			DeviceInfo deviceInfo, User user) throws IOException, OAuthRequestException {

		// This message object is a Google Cloud Messaging object, it is NOT 
		// related to the MessageData class
		Message.Builder builder = new Message.Builder();
		builder.addData("placeName", messageObj.getPlaceName());
		builder.addData("latitude", String.valueOf(messageObj.getLatitude()));
		builder.addData("longitude", String.valueOf(messageObj.getLongitude()));
		builder.addData("duration", String.valueOf(messageObj.getDuration()));
		builder.addData("timestamp", String.valueOf(messageObj.getTimestamp()));
		builder.addData("senderName", messageObj.getSenderName());
		builder.addData("senderEmail", messageObj.getSenderEmail());
		builder.addData("senderAuthDomain", messageObj.getSenderAuthDomain());
		builder.addData("senderFederatedIdentity", messageObj.getSenderFederatedIdentity());
		builder.addData("senderId", messageObj.getSenderId());
		Message msg = builder.build();
		
		
		// Send the message with up to 5 retries
		Result result = sender.send(msg, deviceInfo.getDeviceRegistrationID(), 5);
		
		// Check result
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
	 * Wrapper class for boolean result 
	 * @author neenaparikh
	 *
	 */
	public class BooleanResult {
		final boolean result;
		public BooleanResult(boolean result) {
			this.result = result;
		}
		public boolean getResult() {
			return result;
		}
	}
}
