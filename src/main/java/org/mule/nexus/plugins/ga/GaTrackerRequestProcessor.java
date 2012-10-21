package org.mule.nexus.plugins.ga;


import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;
import com.boxysystems.jgoogleanalytics.LoggingAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestProcessor;

import javax.inject.Named;

/**
 * This processor reacts on repository "read" actions and track
 * those in Google analytics. There's only two things to configure,
 * the google tracker code (mandatory) and an optional referrer URL; the
 * two are set via system properties:
 * <p/>
 * <pre>
 *     -Dnexus.ga.trackerId=UA-30755831-3
 * </pre>
 *
 * @author Lars J. Nilsson
 * @author Emiliano Lesende
 */
@Named("gaTracker")
public class GaTrackerRequestProcessor implements RequestProcessor {

  public static final String GA_TRACKER_ID = System.getProperty("nexus.ga.trackerId");

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final JGoogleAnalyticsTracker tracker;

  public GaTrackerRequestProcessor() {
    if (checkGaTrackerId()) {
      log.info("Creating new tracker, with id: " + GA_TRACKER_ID);
      tracker = new JGoogleAnalyticsTracker("Nexus", "1.9.2.2", GA_TRACKER_ID);
      adaptLogging();
    } else {
      tracker = new JGoogleAnalyticsTracker("Nexus", "1.9.2.2", "");
    }
  }

  public boolean process(Repository rep, ResourceStoreRequest req, Action action) {
    if (action == Action.read) {
      /*
       * 1) create path by appending repo path to repo id
       * 2) create a subclass of focus point that handles proper URI's
       * 3) track asynchronously, this will perform the tracking on a new thread
       */
      String path = rep.getId() + req.getRequestPath();
      log.debug("Tracking path: " + path);
      FocusPoint p = new Point(path);
      tracker.trackAsynchronously(p);
      String trackerId = System.getProperty("nexus.ga.trackerId");
      if (trackerId != null) {
        log.debug("Tracker ID: " + trackerId);
      }
    } else {
      log.debug("Ingoring request of action '" + action + "' for: " + req.getRequestPath());
    }
    return true;
  }

  public boolean shouldCache(ProxyRepository proxy, AbstractStorageItem item) {
    log.debug("Returns true for 'cache', on item: " + item.getPath());
    return true;
  }

  public boolean shouldProxy(ProxyRepository proxy, ResourceStoreRequest req) {
    log.debug("Returns true for 'proxy', on request: " + req.getRequestPath());
    return true;
  }


  // --- PRIVATE METHODS --- //

  private void adaptLogging() {
    /*
     * Adapt the logging to use slf4j instead.
     */
    tracker.setLoggingAdapter(new LoggingAdapter() {

      public void logMessage(String msg) {
        log.debug(msg);
      }

      public void logError(String msg) {
        log.error(msg);
      }
    });
  }

  private boolean checkGaTrackerId() {
    return GA_TRACKER_ID != null;
  }


  // --- INNER CLASSES --- //

  /**
   * Simple inner class that adapts the content URI to
   * not be URL-escaped.
   */
  private static class Point extends FocusPoint {

    public Point(String name) {
      super(name);
    }

    @Override
    public String getContentURI() {
      return getName();
    }
  }
}
