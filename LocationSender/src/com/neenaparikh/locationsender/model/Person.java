package com.neenaparikh.locationsender.model;

import java.util.ArrayList;

import android.net.Uri;

/**
 * Represents a user of the application. (Not called "User" in order to avoid
 * confusion with other classes named User.)
 * 
 * @author neenaparikh
 *
 */
public class Person implements Comparable<Person> {
	private String name;
	private ArrayList<String> emails;
	private String registeredEmail; // The email address this person has used to register with the app
	private ArrayList<String> phones;
	private String registeredPhone; // The phone number this person has used to register with the app
	private boolean isRegistered; // Tells whether this Person is registered with the app
	private Uri photoUri;
	
	public Person() {
		this.name = "";
		this.emails = new ArrayList<String>();
		this.registeredEmail = "";
		this.phones = new ArrayList<String>();
		this.registeredPhone = "";
		this.photoUri = Uri.EMPTY;
		this.isRegistered = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		if (name == null) this.name = "";
	}

	public ArrayList<String> getEmails() {
		return emails;
	}

	public void setEmails(ArrayList<String> emails) {
		this.emails = emails;
	}

	public String getRegisteredEmail() {
		return registeredEmail;
	}

	public void setRegisteredEmail(String registeredEmail) {
		this.registeredEmail = registeredEmail;
	}

	public ArrayList<String> getPhones() {
		return phones;
	}

	public void setPhones(ArrayList<String> phones) {
		this.phones = phones;
	}

	public String getRegisteredPhone() {
		return registeredPhone;
	}

	public void setRegisteredPhone(String registeredPhone) {
		this.registeredPhone = registeredPhone;
	}

	public boolean isRegistered() {
		return isRegistered;
	}

	public void setRegistered(boolean isRegistered) {
		this.isRegistered = isRegistered;
	}

	public Uri getPhotoUri() {
		return photoUri;
	}

	public void setPhotoUri(Uri photoUri) {
		this.photoUri = photoUri;
	}
	
	public String toString() {
		return this.name;
	}

	@Override
	public int compareTo(Person another) {
		return this.getName().compareToIgnoreCase(another.getName());
	}
}
