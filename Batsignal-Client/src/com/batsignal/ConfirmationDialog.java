package com.batsignal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.batsignal.model.Place;
import com.batsignal.util.Constants;

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
		builder.setMessage(formatMessage(recipientNames, selectedPlace.getDuration()));
		
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
	
	/**
	 * Helper method to format the dialog message given the recipients and duration.
	 * @param recipients The selected recipients of the notification
	 * @param duration The duration for which to send the notification
	 * @return A sensical dialog message to display
	 */
	private String formatMessage(String[] recipientNames, int duration) {
		String message = "Send notification to ";
		
		// Add recipient names
		int numRecipients = recipientNames.length;
		if (numRecipients == 1) {
			message += recipientNames[0];
		} else if (numRecipients == 2) {
			message += recipientNames[0] + " and " + recipientNames[1];
		} else {
			for (int i = 0; i < numRecipients - 1; i++) {
				message += recipientNames[i] + ", ";
			}
			message += "and " + recipientNames[recipientNames.length - 1];
		}
		
		// Add duration
		message += " for " + minutesToDurationText(duration);
		
		message += "?";
		return message;
	}
	
	/**
	 * Takes in a duration (in minutes) and returns a formatted text representation.
	 * For example, 100 minutes becomes "1 hour, 40 minutes"
	 * @param duration The duration in minutes
	 * @return A formatted text representation of the duration
	 */
	private String minutesToDurationText(int duration) {
		int hours = duration / 60;
		int minutes = duration % 60;
		
		if (hours == 0) {
			if (minutes == 1) return "1 minute";
			else return minutes + " minutes";
		} else if (minutes == 0) {
			if (hours == 1) return "1 hour";
			else return hours + " hours";
		} else {
			String hourString;
			if (hours == 1) hourString = "1 hour";
			else hourString = hours + " hours";
			
			String minuteString;
			if (minutes == 1) minuteString = "1 hour";
			else minuteString = minutes + " minutes";
			
			return hourString + ", " + minuteString;
		}
	}

}
