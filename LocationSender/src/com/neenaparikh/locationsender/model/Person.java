package com.neenaparikh.locationsender.model;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.neenaparikh.locationsender.util.Constants;
import com.neenaparikh.locationsender.util.HelperMethods;

/**
 * Represents a user of the application. (Not called "User" in order to avoid
 * confusion with other classes named User.)
 * 
 * @author neenaparikh
 *
 */
public class Person implements Comparable<Person>, Parcelable {
	private String name;
	private ArrayList<String> emails;
	private ArrayList<String> phones;
	private Uri photoUri;
	private ArrayList<String> deviceRegistrationIdList; // The person's associated GCM registration IDs
	private long lastContacted; // Denotes the timestamp of when this Person was last contacted using the app
	
	public Person() {
		this.name = "";
		this.emails = new ArrayList<String>();
		this.phones = new ArrayList<String>();
		this.photoUri = Uri.EMPTY;
		this.deviceRegistrationIdList = new ArrayList<String>();
		this.lastContacted = Long.valueOf(0);
	}
	
	/**
	 * @return A Person object created from a JSON string
	 */
	public static Person personFromJsonString(String jsonString) {
		if (jsonString == null || jsonString.length() == 0) return null;
		
		try {
			Person person = new Person();
			JSONObject jsonObject = new JSONObject(jsonString);
			person.setName(jsonObject.getString(Constants.PERSON_NAME_KEY));
			
			String emailsString = jsonObject.getString(Constants.PERSON_EMAILS_KEY);
			if (emailsString != null && emailsString.length() > 0) 
				person.setEmails(new ArrayList<String>(Arrays.asList(emailsString.split(" "))));
			
			String phonesString = jsonObject.getString(Constants.PERSON_PHONES_KEY);
			if (phonesString != null && phonesString.length() > 0)
				person.setPhones(new ArrayList<String>(Arrays.asList(phonesString.split(" "))));
			
			person.setPhotoUri(Uri.parse(jsonObject.getString(Constants.PERSON_PHOTO_URI_KEY)));
			
			String deviceIdString = jsonObject.getString(Constants.PERSON_DEVICE_ID_LIST_KEY);
			if (deviceIdString != null && deviceIdString.length() > 0)
				person.setDeviceRegistrationIdList(new ArrayList<String>(Arrays.asList(deviceIdString.split(" "))));
			
			person.setLastContacted(Long.valueOf(jsonObject.getString(Constants.PERSON_LAST_CONTACTED_KEY)));
			
			return person;
		} catch (JSONException e) {
			return null;
		}
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
		if (emails == null) this.emails = new ArrayList<String>();
	}

	public ArrayList<String> getPhones() {
		return phones;
	}

	public void setPhones(ArrayList<String> phones) {
		this.phones = phones;
		if (phones == null) this.phones = new ArrayList<String>();
	}

	public Uri getPhotoUri() {
		return photoUri;
	}

	public void setPhotoUri(Uri photoUri) {
		this.photoUri = photoUri;
		if (photoUri == null) this.photoUri = Uri.EMPTY;
	}

	public ArrayList<String> getDeviceRegistrationIdList() {
		return deviceRegistrationIdList;
	}

	public void addDeviceRegistrationId(String deviceRegistrationId) {
		getDeviceRegistrationIdList().add(deviceRegistrationId);
	}
	
	public void setDeviceRegistrationIdList(ArrayList<String> deviceRegistrationIdList) {
		this.deviceRegistrationIdList = deviceRegistrationIdList;
		if (deviceRegistrationIdList == null) this.deviceRegistrationIdList = new ArrayList<String>();
	}
	
	public long getLastContacted() {
		return lastContacted;
	}

	public void setLastContacted(long lastContacted) {
		this.lastContacted = lastContacted;
	}
	
	public boolean isRegistered() {
		return (getDeviceRegistrationIdList().size() > 0);
	}
	
	public boolean hasPhones() {
		return (getPhones() != null && getPhones().size() > 0);
	}
	
	public String toString() {
		return this.name;
	}
	
	public String toStringVerbose() {
		String emailsString = "";
		for (String e : getEmails()) emailsString += e + " ";
		
		String phonesString = "";
		for (String p : getPhones()) phonesString += p + " ";
		
		return "Name: " + getName() + 
				"\nEmails: " + emailsString + 
				"\nPhones: " + phonesString + 
				"\nRegistered Devices: " + HelperMethods.stringListToString(getDeviceRegistrationIdList()) +
				"\nLast contacted: " + getLastContacted();
	}
	
	/**
	 * @return A representation of this Person object in the form of a JSON string
	 */
	public String toJsonString() {
		JSONObject jsonObject = new JSONObject();
		
		try {
			jsonObject.putOpt(Constants.PERSON_NAME_KEY, getName());
			
			String emailString = "";
			for (String email : getEmails()) emailString += email + " ";
			jsonObject.putOpt(Constants.PERSON_EMAILS_KEY, emailString);
			
			String phoneString = "";
			for (String phone : getPhones()) phoneString += phone + " ";
			jsonObject.putOpt(Constants.PERSON_PHONES_KEY, phoneString);
			
			jsonObject.putOpt(Constants.PERSON_PHOTO_URI_KEY, getPhotoUri().toString());
			
			String deviceIdString = "";
			for (String deviceId : getDeviceRegistrationIdList()) deviceIdString += deviceId + " ";
			jsonObject.putOpt(Constants.PERSON_DEVICE_ID_LIST_KEY, deviceIdString);
			
			jsonObject.putOpt(Constants.PERSON_LAST_CONTACTED_KEY, getLastContacted());
			
		} catch (JSONException e) {
			return "";
		}
		
		return jsonObject.toString();
	}

	@Override
	public int compareTo(Person another) {
		return this.getName().compareToIgnoreCase(another.getName());
	}
	
	
	
	
	/* Following methods are for parceling */
	
	/**
	 * Writes object data to parcel
	 */
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(getName());
		out.writeStringList(getEmails());
		out.writeStringList(getPhones());
		out.writeString(getPhotoUri().toString());
    }

	/**
	 * Creator to create new Person object from parcel
	 */
    public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
    
    /**
     * Private method to create new Person object from parcel data
     * @param in
     */
    private Person(Parcel in) {
    	setName(in.readString());
    	
    	ArrayList<String> emails = new ArrayList<String>();
    	in.readStringList(emails);
    	
    	ArrayList<String> phones = new ArrayList<String>();
    	in.readStringList(phones);
    	
    	setPhotoUri(Uri.parse(in.readString()));
    }

	@Override
	public int describeContents() {
		return 0;
		
	}

}
