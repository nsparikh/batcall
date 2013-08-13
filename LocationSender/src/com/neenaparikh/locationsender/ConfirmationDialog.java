package com.neenaparikh.locationsender;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.neenaparikh.locationsender.comms.SendMessageTask;
import com.neenaparikh.locationsender.model.Person;
import com.neenaparikh.locationsender.model.Place;
import com.neenaparikh.locationsender.util.Constants;
import com.neenaparikh.locationsender.util.HelperMethods;

public class ConfirmationDialog extends DialogFragment {

	public static ConfirmationDialog getInstance(ArrayList<Person> recipients, Place selectedPlace) {
		ConfirmationDialog confirmationDialog = new ConfirmationDialog();
		
		Bundle arguments = new Bundle();
		arguments.putParcelable(Constants.SELECTED_PLACE_KEY, selectedPlace);
		arguments.putParcelableArrayList(Constants.SELECTED_RECIPIENTS_KEY, recipients);
		confirmationDialog.setArguments(arguments);
		
		return confirmationDialog;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// Retrieve the selected place and recipients from the arguments
		final Place selectedPlace = getArguments().getParcelable(Constants.SELECTED_PLACE_KEY);
		final ArrayList<Person> recipients = getArguments().getParcelableArrayList(Constants.SELECTED_RECIPIENTS_KEY);
		
		// Show dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(selectedPlace.getName());
		ArrayList<String> recipientNames = new ArrayList<String>();
		for (Person p : recipients) recipientNames.add(p.getName());
		builder.setMessage(HelperMethods.formatDialogMessage(recipientNames));
		
		builder.setPositiveButton(R.string.confirmation_dialog_confirm_text, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				new SendMessageTask(getActivity(), selectedPlace).execute(recipients.toArray(new Person[recipients.size()]));
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
