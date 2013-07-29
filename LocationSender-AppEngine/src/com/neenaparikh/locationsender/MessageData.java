package com.neenaparikh.locationsender;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

/**
 * An entity for messsages sent from the web console to the registered devices
 * 
 * Its associated endpoint, MessageEndpoint.java, was NOT automatically generated 
 * from this class. While it is easy to generate endpoints automatically, you can
 * write an endpoint manually without generating it. You still need to generate
 * the associated client library from the endpoint when changes are made.
 * 
 * For more information on endpoints, see
 * http://developers.google.com/eclipse/docs/cloud_endpoints.
 */

@Entity
public class MessageData {

	/*
	 * Autogenerated primary key
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;

	// Place name
	private String name;

	// Place coordinates
	private double latitude;
	private double longitude;

	// Timestamp of when the message was sent
	private long timestamp;
	
	// Duration of the message (in minutes)
	private int duration;

	// Details of the sender (fields in the com.google.appengine.api.users.User class)
	private String senderName;
	private String senderEmail;
	private String senderAuthDomain;
	private String senderId;
	private String senderFederatedIdentity;


	public Key getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
		if (senderName == null) this.senderName = "";
	}

	public String getSenderEmail() {
		return senderEmail;
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
		if (senderEmail == null) this.senderEmail = "";
	}

	public String getSenderAuthDomain() {
		return senderAuthDomain;
	}

	public void setSenderAuthDomain(String senderAuthDomain) {
		this.senderAuthDomain = senderAuthDomain;
		if (senderAuthDomain == null) this.senderAuthDomain = "";
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
		if (senderId == null) this.senderId = "";
	}

	public String getSenderFederatedIdentity() {
		return senderFederatedIdentity;
	}

	public void setSenderFederatedIdentity(String senderFederatedIdentity) {
		this.senderFederatedIdentity = senderFederatedIdentity;
		if (senderFederatedIdentity == null) this.senderFederatedIdentity = "";
	}
}
