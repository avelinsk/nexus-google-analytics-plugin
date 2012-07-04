package com.boxysystems.jgoogleanalytics;

import org.slf4j.LoggerFactory;

/**
 * @since 6/24/12
 */
public class Logger implements LoggingAdapter {
  private final org.slf4j.Logger LOG = LoggerFactory.getLogger(getClass());

  public void logError(String errorMessage) {
    LOG.error(errorMessage);
  }

  public void logMessage(String message) {
    LOG.info(message);
  }

}
