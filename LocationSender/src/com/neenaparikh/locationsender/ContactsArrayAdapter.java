package com.neenaparikh.locationsender;

import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.neenaparikh.locationsender.model.Person;

public class ContactsArrayAdapter extends ArrayAdapter<Person> {
	private Context context;
	private int listItemResourceId;
	private List<Person> personList;

	public ContactsArrayAdapter(Context context, int listItemResourceId, List<Person> personList) {
		super(context, listItemResourceId, personList);
		
		this.context = context;
		this.listItemResourceId = listItemResourceId;
		this.personList = personList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View currentRowItem = convertView;

		// Inflate the row item if we haven't before
        if (currentRowItem == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
            currentRowItem = inflater.inflate(listItemResourceId, null);
        }
        
        // Get the current person object and set relevant fields in the view
        Person currentPerson = personList.get(position);
        
        TextView nameTextView = (TextView) currentRowItem.findViewById(R.id.contact_list_item_name);
        nameTextView.setText(currentPerson.getName());
        
        ImageView imageView = (ImageView) currentRowItem.findViewById(R.id.contact_list_item_image);
        if (!currentPerson.getPhotoUri().equals(Uri.EMPTY)) {
        	imageView.setImageURI(currentPerson.getPhotoUri());
        } else {
        	imageView.setImageResource(R.drawable.ic_contact_picture_2);
        }
        
        
        return currentRowItem;
	}
}
