package com.neenaparikh.locationsender;

import java.util.ArrayList;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.neenaparikh.locationsender.model.Person;

public class ContactsArrayAdapter extends ArrayAdapter<Person> {
	private ContactsActivity mActivity;
	private final ArrayList<Person> personList;
	private final boolean[] selectedPersonIndexes;
	private LayoutInflater mInflater;

	private final int[] defaultImageResources = { R.drawable.emo_1, R.drawable.emo_2, R.drawable.emo_3, 
			R.drawable.emo_4, R.drawable.emo_5, R.drawable.emo_6 };
	private int defaultImageResourceIndex = 0;

	/**
	 * Constructor takes in activity, list of Person objects, and header title
	 * @param mActivity The activity
	 * @param personList The list of Person objects
	 * @param title The header title of this list
	 */
	public ContactsArrayAdapter(ContactsActivity mActivity, ArrayList<Person> personList) {
		super(mActivity, R.layout.contact_list_item, personList);

		this.mActivity = mActivity;
		this.personList = personList;
		this.selectedPersonIndexes = new boolean[personList.size()];
		this.mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View currentRowItem = convertView;

		// Otherwise, get the current person object and set relevant fields in the view
		final Person currentPerson = personList.get(position);

		// Inflate the row item 
		currentRowItem = mInflater.inflate(R.layout.contact_list_item, null);

		// Set name
		TextView nameTextView = (TextView) currentRowItem.findViewById(R.id.contact_list_item_name);
		nameTextView.setText(currentPerson.getName());
		
		// Set image if any
		ImageView imageView = (ImageView) currentRowItem.findViewById(R.id.contact_list_item_image);
		if (!currentPerson.getPhotoUri().equals(Uri.EMPTY)) {
			imageView.setImageURI(currentPerson.getPhotoUri());
			imageView.setScaleType(ScaleType.FIT_CENTER);
		} else {
			imageView.setImageResource(defaultImageResources[defaultImageResourceIndex]);
			imageView.setScaleType(ScaleType.CENTER_INSIDE);
			defaultImageResourceIndex++;
			defaultImageResourceIndex %= defaultImageResources.length;
		}

		// Set check box selection listener
		CheckBox checkBox = (CheckBox) currentRowItem.findViewById(R.id.contact_list_item_checkbox);
		checkBox.setTag(String.valueOf(position));
		if (selectedPersonIndexes[position]) checkBox.setChecked(true);
		else checkBox.setChecked(false);

		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() { 
			@Override 
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
				int actualPosition = Integer.parseInt(buttonView.getTag().toString());
				selectedPersonIndexes[actualPosition] = isChecked;
				mActivity.updateNamesList(getSelectedNamesList());
			}
		});

		return currentRowItem;
	}

	/**
	 * @return The list of selected people as Person objects
	 */
	public ArrayList<Person> getSelectedPersonList() {
		ArrayList<Person> selectedPersonList = new ArrayList<Person>();
		for (int i = 0; i < personList.size(); i++) {
			if (selectedPersonIndexes[i])
				selectedPersonList.add(personList.get(i));
		}
		return selectedPersonList;
	}

	/**
	 * @return The list of selected people's names
	 */
	private ArrayList<String> getSelectedNamesList() {
		ArrayList<String> selectedNamesList = new ArrayList<String>();
		for (int i = 0; i < personList.size(); i++) {
			if (selectedPersonIndexes[i])
				selectedNamesList.add(personList.get(i).getName());
		}
		return selectedNamesList;
	}
}
