package com.neenaparikh.locationsender;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.neenaparikh.locationsender.comms.SendMessageTask;
import com.neenaparikh.locationsender.model.Person;
import com.neenaparikh.locationsender.model.Place;
import com.neenaparikh.locationsender.util.Constants;

/**
 * A custom DialogFragment class that prompts the user to select a duration
 * that he/she will be at the chosen place.
 * 
 * @author neenaparikh
 *
 */
public class DurationSelectorDialog extends DialogFragment {

	public static DurationSelectorDialog getInstance(Place selectedPlace) {
		DurationSelectorDialog durationSelectorDialog = new DurationSelectorDialog();

		Bundle arguments = new Bundle();
		arguments.putParcelable(Constants.SELECTED_PLACE_KEY, selectedPlace);
		durationSelectorDialog.setArguments(arguments);

		return durationSelectorDialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Retrieve the selected place from the arguments
		final Place selectedPlace = getArguments().getParcelable(Constants.SELECTED_PLACE_KEY);
		
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(selectedPlace.getName());

		// Inflate the layout
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_duration_dialog, null);
		builder.setView(view);

		// Set parameters for number pickers
		final NumberPicker hourPicker = (NumberPicker) view.findViewById(R.id.duration_dialog_hour_picker);
		hourPicker.setMinValue(0);
		hourPicker.setMaxValue(23);
		hourPicker.setValue(0); // default to 0 hours
		hourPicker.setWrapSelectorWheel(true);
		final NumberPicker minutePicker = (NumberPicker) view.findViewById(R.id.duration_dialog_minute_picker);
		minutePicker.setMinValue(0);
		minutePicker.setMaxValue(59);
		minutePicker.setValue(30); // default to 30 minutes
		minutePicker.setWrapSelectorWheel(true);

		builder.setPositiveButton(R.string.duration_dialog_confirm_text, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				int hours = hourPicker.getValue();
				int minutes = minutePicker.getValue();
				selectedPlace.setDuration(minutes + 60*hours);

				// Launch new activity to display contacts, pass place object
				//Intent contactsIntent = new Intent(getActivity(), ContactsActivity.class);
				//contactsIntent.putExtra(Constants.SELECTED_PLACE_KEY, selectedPlace);
				//startActivity(contactsIntent);
				Person test = new Person();
				test.setRegistered(true);
				test.setRegisteredEmail("nparikh92@gmail.com");
				new SendMessageTask(getActivity(), selectedPlace).execute(test);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Don't save anything; close dialog
				dismiss();
			}
		});

		// Create the AlertDialog object and return it
		return builder.create();
	}
}
