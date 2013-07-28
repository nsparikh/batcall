package com.neenaparikh.locationsender;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.neenaparikh.locationsender.R;
import com.neenaparikh.locationsender.model.Place;
import com.neenaparikh.locationsender.util.Constants;
import com.neenaparikh.locationsender.util.HelperMethods;

public class ConfirmationDialog extends DialogFragment {

	public static ConfirmationDialog getInstance(String[] recipientNames, Place selectedPlace) {
		ConfirmationDialog confirmationDialog = new ConfirmationDialog();
		
		Bundle arguments = new Bundle();
		arguments.putParcelable(Constants.SELECTED_PLACE_KEY, selectedPlace);
		arguments.putStringArray(Constants.SELECTED_RECIPIENTS_KEY, recipientNames);
		confirmationDialog.setArguments(arguments);
		
		return confirmationDialog;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// Retrieve the selected place and recipients from the arguments
		final Place selectedPlace = getArguments().getParcelable(Constants.SELECTED_PLACE_KEY);
		final String[] recipientNames = getArguments().getStringArray(Constants.SELECTED_RECIPIENTS_KEY);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(selectedPlace.getName());
		builder.setMessage(HelperMethods.formatMessage(recipientNames, selectedPlace.getDuration()));
		
		builder.setPositiveButton(R.string.confirmation_dialog_confirm_text, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// TODO: send!
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
