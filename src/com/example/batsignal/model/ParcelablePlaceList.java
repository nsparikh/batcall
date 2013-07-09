package com.example.batsignal.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.Key;

public class ParcelablePlaceList implements Parcelable {
	
	@Key("results")
	private List<ParcelablePlace> places;
	
	/**
	 * Empty constructor creates an empty list of places
	 */
	public ParcelablePlaceList() {
		this.places = new ArrayList<ParcelablePlace>();
	}
	
	/**
	 * Constructor that takes in a list of Place objects
	 */
	public ParcelablePlaceList(List<ParcelablePlace> placeList) {
		this.places = new ArrayList<ParcelablePlace>(placeList);
	}
	
	/**
	 * Adds a place to the list
	 */
	public void add(ParcelablePlace place) {
		this.places.add(place);
	}
	
	/**
	 * Returns the list of places
	 */
	public List<ParcelablePlace> getPlaceList() {
		return this.places;
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
    public static final Parcelable.Creator<ParcelablePlaceList> CREATOR = new Parcelable.Creator<ParcelablePlaceList>() {
        public ParcelablePlaceList createFromParcel(Parcel in) {
            return new ParcelablePlaceList(in);
        }

        public ParcelablePlaceList[] newArray(int size) {
            return new ParcelablePlaceList[size];
        }
    };
    
    /**
     * Private method to create new PlaceList object from parcel data
     * @param in
     */
    private ParcelablePlaceList(Parcel in) {
        this.places = new ArrayList<ParcelablePlace>();
        in.readList(places, null);
    }

	@Override
	public int describeContents() {
		return 0;
	}
}
