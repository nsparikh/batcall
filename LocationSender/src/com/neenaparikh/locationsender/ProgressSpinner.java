package com.neenaparikh.locationsender;

import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.content.Context;
import android.widget.ProgressBar;

public class ProgressSpinner extends Dialog {

	public ProgressSpinner(Context context) {
		super(context, R.style.progress_spinner);
	}

	public static ProgressSpinner show(final Context context, final CharSequence title, final CharSequence message) {
		return show(context, title, message, false);
	}

	public static ProgressSpinner show(final Context context, final CharSequence title, final CharSequence message, final boolean indeterminate) {
		return show(context, title, message, indeterminate, false, null);
	}

	public static ProgressSpinner show(final Context context, final CharSequence title, final CharSequence message, final boolean indeterminate, final boolean cancelable) {
		return show(context, title, message, indeterminate, cancelable, null);
	}

	public static ProgressSpinner show(final Context context, final CharSequence title, final CharSequence message, final boolean indeterminate, final boolean cancelable, final OnCancelListener onCancelListener) {
		ProgressSpinner dialog = new ProgressSpinner(context);
		dialog.setCancelable(cancelable);
		dialog.setOnCancelListener(onCancelListener);
		dialog.setTitle(title);

		dialog.addContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		dialog.show();

		return dialog;
	}

}
