package com.neenaparikh.batsignal.util;

public class HelperMethods {

	/**
	 * Helper method to format the dialog message given the recipients and duration.
	 * @param recipients The selected recipients of the notification
	 * @param duration The duration for which to send the notification
	 * @return A sensical dialog message to display
	 */
	public static String formatMessage(String[] recipientNames, int duration) {
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
	public static String minutesToDurationText(int duration) {
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
