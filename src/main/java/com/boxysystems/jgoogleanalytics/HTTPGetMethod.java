package com.boxysystems.jgoogleanalytics;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Simple class peforming HTTP Get method on the requested url
 *
 * @author : Siddique Hameed
 * @version : 0.1
 */
public class HTTPGetMethod {
  private static final String GET_METHOD_NAME = "GET";
  private static final String SUCCESS_MESSAGE = "JGoogleAnalytics: Tracking Successfull!";
  private LoggingAdapter loggingAdapter = null;


  public void setLoggingAdapter(LoggingAdapter loggingAdapter) {
    this.loggingAdapter = loggingAdapter;
  }

  public int request(String urlString) {
    int responseCode = -1;
    try {
      URL url = new URL(urlString);
      HttpURLConnection urlConnection = openURLConnection(url);
      urlConnection.setInstanceFollowRedirects(true);
      urlConnection.setRequestMethod(GET_METHOD_NAME);
      urlConnection.connect();
      responseCode = getResponseCode(urlConnection);
      if (responseCode != HttpURLConnection.HTTP_OK) {
        logError("JGoogleAnalytics: Error tracking, url=" + urlString);
      } else {
        logMessage(SUCCESS_MESSAGE);
      }
    } catch (Exception e) {
      logError(e.getMessage());
    }
    return responseCode;
  }

  protected int getResponseCode(HttpURLConnection urlConnection) throws IOException {
    return urlConnection.getResponseCode();
  }

  private HttpURLConnection openURLConnection(URL url) throws IOException {
    return (HttpURLConnection) url.openConnection();
  }

  private void logMessage(String message) {
    if (loggingAdapter != null) {
      loggingAdapter.logMessage(message);
    }
  }

  private void logError(String errorMesssage) {
    if (loggingAdapter != null) {
      loggingAdapter.logError(errorMesssage);
    }
  }
}
