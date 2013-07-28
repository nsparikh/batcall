package com.neenaparikh.batsignal;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.neenaparikh.batsignal.comms.RegisterTask;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Log.e(MainActivity.class.getName(), "-----------------------------------");

		// Register the device
		new RegisterTask(this).execute();
		
	}
}
