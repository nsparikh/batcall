package com.neenaparikh.locationsender.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.Key;

public class PlaceList implements Parcelable {
	
	@Key("results")
	private ArrayList<Place> places;
	
	@Key("next_page_token")
	private String nextPageToken;
	
	/**
	 * Empty constructor creates an empty list of places
	 */
	public PlaceList() {
		this.places = new ArrayList<Place>();
	}
	
	/**
	 * Constructor that takes in a list of Place objects
	 */
	public PlaceList(ArrayList<Place> placeList) {
		this.places = new ArrayList<Place>(placeList);
	}
	
	/**
	 * Adds a place to the list
	 */
	public void add(Place place) {
		this.places.add(place);
	}
	
	/**
	 * Returns the list of places
	 */
	public ArrayList<Place> getPlaceList() {
		return this.places;
	}

	public String getNextPageToken() {
		return nextPageToken;
	}

	/**
	 * Writes object data to parcel
	 */
	public void writeToParcel(Parcel out, int flags) {
		out.writeList(this.places);
    }

	/**
	 * Creator to create new PlaceList object from parcel
	 */
    public static final Parcelable.Creator<PlaceList> CREATOR = new Parcelable.Creator<PlaceList>() {
        public PlaceList createFromParcel(Parcel in) {
            return new PlaceList(in);
        }

        public PlaceList[] newArray(int size) {
            return new PlaceList[size];
        }
    };
    
    /**
     * Private method to create new PlaceList object from parcel data
     * @param in
     */
    private PlaceList(Parcel in) {
        this.places = new ArrayList<Place>();
        in.readList(places, null);
    }

	@Override
	public int describeContents() {
		return 0;
	}
}