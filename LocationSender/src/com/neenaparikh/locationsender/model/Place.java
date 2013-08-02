package com.neenaparikh.locationsender.model;

import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.Key;
import com.neenaparikh.locationsender.util.Constants;



public class Place implements Parcelable {
	
	// Place object
	@Key("id")
	private String id;
	
	// Place name
	@Key("name")
	private String name;
	
	// Place address
	@Key("formatted_address")
	private String address;
	
	// Place reference (from Google Places API)
	@Key("reference")
	private String reference;
	
	/*
	 * This structure is a result of the Google Places API.
	 * The location coordinates are nested within a "geometry" attribute.
	 */
	@Key("geometry")
	private MyGeometry geometry;
	
	// The duration of the user's stay at this place (in minutes)
	private int duration;
	

	
	/**
	 * Helper class for JSON object parsing from Places API data
	 * Represents the geometry (location) of a Place
	 * 
	 * @author neenaparikh
	 *
	 */
	public static class MyGeometry {
		@Key("location")
		public MyLocation location;
		
		public MyGeometry() {
			this.location = new MyLocation();
		}
	}
	
	/**
	 * Helper class for JSON object parsing from Places API data
	 * Represents the location (lat/lng) of a Place
	 * 
	 * @author neenaparikh
	 *
	 */
	public static class MyLocation {
		@Key("lat")
		public double latitude;
		
		@Key("lng")
		public double longitude;
		
		public MyLocation() {
			this.latitude = 0;
			this.longitude = 0;
		}
	}
	
	/**
	 * Default constructor
	 * @param id
	 * @param name
	 * @param reference 
	 * @param latitude
	 * @param longitude
	 **/
	public Place(String id, String name, String address, String reference, double latitude, double longitude) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.reference = reference;
		this.geometry = new MyGeometry();
		this.geometry.location.latitude = latitude;
		this.geometry.location.longitude = longitude;
		this.duration = 0;
	}
	
	/**
	 * Empty constructor
	 */
	public Place() {
		this.id = "";
		this.name = "";
		this.address = "";
		this.reference = "";
		this.geometry = new MyGeometry();
		this.duration = 0;
	}
	
	/**
	 * Alternate constructor that takes in Location object instead of lat/long coordinates
	 * @param id
	 * @param name
	 * @param reference 
	 * @param location
	 */
	public Place(String id, String name, String address, String reference, Location location) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.reference = reference;
		this.geometry = new MyGeometry();
		this.geometry.location.latitude = location.getLatitude();
		this.geometry.location.longitude = location.getLongitude();
		this.duration = 0;
	}
	
	
	/**
	 * @return the place ID
	 */
	public String getId() {
		if (this.id == null) return "";
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
		if (id == null) this.id = "";
	}
	
	/**
	 * @return the name of the place
	 */
	public String getName() {
		if (this.name == null) return "";
		return this.name;
	}
	
	/**
	 * Sets the name of this Place
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
		if (name == null) this.name = "";
	}
	
	/**
	 * @return the address of the place
	 */
	public String getAddress() {
		if (this.address == null) return "";
		return this.address;
	}
	
	/**
	 * Sets the address of this Place
	 * @param address
	 */
	public void setAddress(String address) {
		this.address = address;
		if (address == null) this.address = "";
	}
	
	/**
	 * @return the refernece
	 */
	public String getReference() {
		if (this.reference == null) return "";
		return this.reference;
	}
	
	/**
	 * Sets the reference of this Place
	 * @param reference
	 */
	public void setReference(String reference) {
		this.reference = reference;
		if (reference == null) this.reference = "";
	}
	
	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return this.geometry.location.latitude;
	}
	
	/**
	 * Sets the latitude coordinate of this Place
	 * @param latitude
	 */
	public void setLatitude(double latitude) {
		this.geometry.location.latitude = latitude;
	}
	
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return this.geometry.location.longitude;
	}
	
	/**
	 * Sets the longitude coordinate of this Place
	 * @param longitude
	 */
	public void setLongitude(double longitude) {
		this.geometry.location.longitude = longitude;
	}
	
	/**
	 * @return the duration (in minutes) of the user's stay at this Place
	 */
	public int getDuration() {
		return this.duration;
	}
	
	
	/**
	 * Sets the duration (in minutes) of the user's stay at this Place.
	 * @param duration
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	/**
	 * @return a string representation of the place
	 */
	public String toString() {
		return getName();
	}
	
	/**
	 * @return a detailed string representation of the place
	 */
	public String toStringVerbose() {
		return "ID: " + getId() + 
				"\nName: " + getName() + 
				"\nAddress: " + getAddress() +
				"\nReference: " + getReference() + 
				"\nLatitude: " + getLatitude() + 
				"\nLongitude: " + getLongitude() + 
				"\nDuration: " + getDuration();
	}
	
	/**
	 * @return a Uri object that will launch this place's location in a map
	 */
	public Uri getUri() {
		String uriString = "geo:" + getLatitude() + "," + getLongitude();
		String query = getLatitude() + "," + getLongitude() + "(" + getName() + ")";
		uriString += "?q=" + Uri.encode(query) + "&z=" + Constants.MAP_ZOOM;
		return Uri.parse(uriString);
	}
	
	/**
	 * Writes object data to parcel
	 */
	public void writeToParcel(Parcel out, int flags) {
        out.writeString(getId());
        out.writeString(getName());
        out.writeString(getAddress());
        out.writeString(getReference());
        out.writeDouble(getLatitude());
        out.writeDouble(getLongitude());
        out.writeInt(getDuration());
    }

	/**
	 * Creator to create new Place object from parcel
	 */
    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
    
    /**
     * Private method to create new Place object from parcel data
     * @param in
     */
    private Place(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.address = in.readString();
        this.reference = in.readString();
		this.geometry = new MyGeometry();
        this.geometry.location.latitude = in.readDouble();
        this.geometry.location.longitude = in.readDouble();
        this.duration = in.readInt();
    }

	@Override
	public int describeContents() {
		return 0;
		
	}

}