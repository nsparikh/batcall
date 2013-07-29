package com.neenaparikh.locationsender;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * An entity for Android device information.
 * 
 * Its associated endpoint, DeviceInfoEndpoint.java, was directly generated from
 * this class - the Google Plugin for Eclipse allows you to generate endpoints
 * directly from entities!
 * 
 * DeviceInfoEndpoint.java will be used for registering devices with this App
 * Engine application. Registered devices will receive messages broadcast by
 * this application over Google Cloud Messaging (GCM). If you'd like to take a
 * look at the broadcasting code, check out MessageEndpoint.java.
 * 
 * For more information, see
 * http://developers.google.com/eclipse/docs/cloud_endpoints.
 * 
 * NOTE: This DeviceInfoEndpoint.java does not use any form of authorization or
 * authentication! If this app is deployed, anyone can access this endpoint! If
 * you'd like to add authentication, take a look at the documentation.
 */
@Entity
// DeviceInfoEndpoint has NO AUTHENTICATION - it is an OPEN ENDPOINT!
public class DeviceInfo {

	// The Google Cloud Messaging registration token for the device. This token
	// indicates that the device is able to receive messages sent via GCM.
	@Id
	private String deviceRegistrationID;

	// Some identifying information about the device, such as its manufacturer and product name.
	private String deviceInformation;

	// Timestamp indicating when this device registered with the application.
	private long timestamp;
	
	// User information (fields in the com.google.appengine.api.users.User class)
	private String userName;
	private String userEmail;
	private String userAuthDomain;
	private String userId;
	private String userFederatedIdentity;
	

	public String getDeviceRegistrationID() {
		return deviceRegistrationID;
	}

	public String getDeviceInformation() {
		return this.deviceInformation;
	}

	public void setDeviceRegistrationID(String deviceRegistrationID) {
		this.deviceRegistrationID = deviceRegistrationID;
	}

	public void setDeviceInformation(String deviceInformation) {
		this.deviceInformation = deviceInformation;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
		if (userName == null) this.userName = "";
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
		if (userEmail == null) this.userEmail = "";
	}

	public String getUserAuthDomain() {
		return userAuthDomain;
	}

	public void setUserAuthDomain(String userAuthDomain) {
		this.userAuthDomain = userAuthDomain;
		if (userAuthDomain == null) this.userAuthDomain = "";
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
		if (userId == null) this.userId = "";
	}

	public String getUserFederatedIdentity() {
		return userFederatedIdentity;
	}

	public void setUserFederatedIdentity(String userFederatedIdentity) {
		this.userFederatedIdentity = userFederatedIdentity;
		if (userFederatedIdentity == null) this.userFederatedIdentity = "";
	}
}
