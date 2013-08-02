package com.neenaparikh.locationsender.util;

import java.util.Calendar;
import java.util.List;

import android.telephony.PhoneNumberUtils;

public class HelperMethods {

	/**
	 * Helper method to format the dialog message given the recipients and duration.
	 * @param recipientNames The selected recipients of the notification
	 * @param duration The duration for which to send the notification
	 * @return A sensical dialog message to display
	 */
	public static String formatMessage(List<String> recipientNames, int duration) {
		String message = "Send notification to ";

		// Add recipient names
		int numRecipients = recipientNames.size();
		if (numRecipients == 1) {
			message += recipientNames.get(0);
		} else if (numRecipients == 2) {
			message += recipientNames.get(0) + " and " + recipientNames.get(0);
		} else {
			for (int i = 0; i < numRecipients - 1; i++) {
				message += recipientNames.get(i) + ", ";
			}
			message += "and " + recipientNames.get(recipientNames.size() - 1);
		}

		message += "?";
		return message;
	}

	/**
	 * Helper method to format a list of names.
	 * @param names The list of names
	 */
	public static String formatNames(List<String> names) {
		if (names == null || names.size() == 0) return "";
		
		String namesListString = names.get(0);
		
		for (int i = 1; i < names.size(); i++) {
			namesListString += ", " + names.get(i);
		}
		return namesListString;
	}
	
	/**
	 * Takes in a list of strings and returns a comma-separated single string.
	 */
	public static String stringListToString(List<String> stringList) {
		String str = "";
		for (String element : stringList) str += element + ",";
		return str;
	}
	
	/**
	 * Takes in a duration in minutes and a start timestamp and returns
	 * the formatted time that is timestamp + duration
	 */
	public static String getTimeAfterStart(long startTime, int duration) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(startTime);
		calendar.add(Calendar.MINUTE, duration);
		
		int minute = calendar.get(Calendar.MINUTE);
		String minuteString = "";
		if (minute < 10) minuteString = "0" + minute;
		else minuteString = "" + minute;
		String timeString = calendar.get(Calendar.HOUR) + ":" + minuteString + " " + calendar.get(Calendar.AM_PM);
		// TODO: Calendar.AM_PM returns an int?
		return timeString;
	}

	/**
	 * "Flattens" a phone number into a standard format by eliminating all symbols, etc.
	 */
	public static String flattenPhone(String formattedPhone) {
		String flattened = PhoneNumberUtils.stripSeparators(formattedPhone);
		flattened = flattened.replaceAll("\\+", "");
		if (flattened.charAt(0) == '1') flattened = flattened.replaceFirst("1", "");
		return flattened;
	}


}
