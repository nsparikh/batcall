package com.batsignal.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.Key;



public class Place implements Parcelable {
	@Key("id")
	private String id;
	
	@Key("name")
	private String name;
	
	@Key("formatted_address")
	private String address;
	
	@Key("reference")
	private String reference;
	
	@Key("geometry")
	private MyGeometry geometry;

	
	/**
	 * Helper class for JSON object parsing from Places API data
	 * Represents the geometry (location) of a Place
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
	 * @return the address of the place
	 */
	public String getAddress() {
		return this.address;
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
		return this.geometry.location.latitude;
	}
	
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return this.geometry.location.longitude;
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
        out.writeString(address);
        out.writeString(reference);
        out.writeDouble(geometry.location.latitude);
        out.writeDouble(geometry.location.longitude);
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
        this.geometry.location.latitude = in.readDouble();
        this.geometry.location.longitude = in.readDouble();
    }

	@Override
	public int describeContents() {
		return 0;
		
	}
}