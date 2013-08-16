package com.neenaparikh.locationsender;

import java.io.IOException;

import android.app.Activity;
import android.widget.Toast;

import com.google.api.client.googleapis.services.AbstractGoogleClient;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

/**
 * Common utilities for working with Cloud Endpoints.
 * 
 * If you'd like to test using a locally-running version of your App Engine
 * backend (i.e. running on the Development App Server), you need to set
 * LOCAL_ANDROID_RUN to 'true'.
 * 
 * See the documentation at
 * http://developers.google.com/eclipse/docs/cloud_endpoints for more
 * information.
 */
public class CloudEndpointUtils {


  protected static final boolean LOCAL_ANDROID_RUN = false;
  protected static final String LOCAL_APP_ENGINE_SERVER_URL = "http://localhost:8888/";
  protected static final String LOCAL_APP_ENGINE_SERVER_URL_FOR_ANDROID = "http://10.0.2.2:8888";

  /**
   * Updates the Google client builder to connect the appropriate server based
   * on whether LOCAL_ANDROID_RUN is true or false.
   * 
   * @param builder Google client builder
   * @return same Google client builder
   */
  public static <B extends AbstractGoogleClient.Builder> B updateBuilder(B builder) {
    if (LOCAL_ANDROID_RUN) {
      builder.setRootUrl(LOCAL_APP_ENGINE_SERVER_URL_FOR_ANDROID + "/_ah/api/");
    }

    // only enable GZip when connecting to remote server
    final boolean enableGZip = builder.getRootUrl().startsWith("https:");

    builder.setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
      public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
        if (!enableGZip) request.setDisableGZipContent(true);
      }
    });

    return builder;
  }

  /**
   * Shows an error alert dialog with the given message.
   * 
   * @param activity Activity
   * @param message Message to show or {@code null} for none
   */
  public static void showError(final Activity activity, String message) {
    final String errorMessage = message == null ? "Error" : "[Error ] "
        + message;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG)
            .show();
      }
    });
  }
}
