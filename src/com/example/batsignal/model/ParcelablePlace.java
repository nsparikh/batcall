package com.example.batsignal.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;



public class ParcelablePlace implements Parcelable {
	private String id;
	private String name;
	private String reference;
	private double latitude;
	private double longitude;
	
	/**
	 * Default constructor
	 * @param id
	 * @param name
	 * @param reference 
	 * @param latitude
	 * @param longitude
	 **/
	public ParcelablePlace(String id, String name, String reference, double latitude, double longitude) {
		this.id = id;
		this.name = name;
		this.reference = reference;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * Alternate constructor that takes in Location object instead of lat/long coordinates
	 * @param id
	 * @param name
	 * @param reference 
	 * @param location
	 */
	public ParcelablePlace(String id, String name, String reference, Location location) {
		this.id = id;
		this.name = name;
		this.reference = reference;
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
	}
	
	/**
	 * @return the place ID
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * @return the name of the place
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return the refernece
	 */
	public String getReference() {
		return this.reference;
	}
	
	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return this.latitude;
	}
	
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return this.longitude;
	}
	
	/**
	 * @return a string representation of the place
	 */
	public String toString() {
		return this.name;
	}
	
	/**
	 * Writes object data to parcel
	 */
	public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(reference);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
    }

	/**
	 * Creator to create new Place object from parcel
	 */
    public static final Parcelable.Creator<ParcelablePlace> CREATOR = new Parcelable.Creator<ParcelablePlace>() {
        public ParcelablePlace createFromParcel(Parcel in) {
            return new ParcelablePlace(in);
        }

        public ParcelablePlace[] newArray(int size) {
            return new ParcelablePlace[size];
        }
    };
    
    /**
     * Private method to create new Place object from parcel data
     * @param in
     */
    private ParcelablePlace(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.reference = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

	@Override
	public int describeContents() {
		return 0;
	}
}
