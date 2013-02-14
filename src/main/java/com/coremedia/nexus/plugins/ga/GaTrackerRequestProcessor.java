package com.coremedia.nexus.plugins.ga;


import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;
import com.boxysystems.jgoogleanalytics.LoggingAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestProcessor;

import javax.inject.Named;

/**
 * This processor reacts on repository "read" actions and track
 * those in Google analytics. There's only two things to configure,
 * the google tracker code (mandatory) and an optional referrer URL; the
 * trackerId is read from the pom.xml, for example:
 * <p/>
 * <pre>
 *     &lt;nexus.ga.trackerId&gt;UA-30755831-3&lt;/nexus.ga.trackerId&gt;
 * </pre>
 *
 * @author Lars J. Nilsson
 * @author Emiliano Lesende
 */
@Named("gaTracker")
public class GaTrackerRequestProcessor implements RequestProcessor {

  private final JGoogleAnalyticsTracker tracker;
  private static final String REQUEST_USER = "request.user";
  private static final String REQUEST_AGENT = "request.agent";
  private static final String DEFAULT_USERNAME = "CoreMedia";

  private final Logger LOG = LoggerFactory.getLogger(getClass());

  public GaTrackerRequestProcessor() {
    PropertiesLoader.loadProperties();
    tracker = new JGoogleAnalyticsTracker("Nexus", PropertiesLoader.NEXUS_VERSION, PropertiesLoader.GA_TRACKER_ID);
    LOG.info("Creating new Google Analytics tracker with id: " + PropertiesLoader.GA_TRACKER_ID);
    adaptLogging();
  }

  public boolean process(Repository rep, ResourceStoreRequest req, Action action) {
    String userName = "";
    RequestContext requestContext = req.getRequestContext();

    //TODO: remove for loop and logging when fixed
    LOG.info("Request Context: \n");
    for (String s : requestContext.keySet()) {
      LOG.info("key=" + s  + "   value=" + requestContext.get(s));
    }

    userName = requestContext.containsKey(REQUEST_USER) ? (String) requestContext.get(REQUEST_USER) : DEFAULT_USERNAME;

    if (action == Action.read) {
      /*
       * 1) create path by appending repo path to repo id
       * 2) create a subclass of focus point that handles proper URI's
       * 3) track asynchronously, this will perform the tracking on a new thread
       */
      String path = rep.getId() + req.getRequestPath();
      LOG.debug("Tracking path: " + path);
      FocusPoint focusPoint = new Point(path, userName);
      tracker.trackAsynchronously(focusPoint);
    } else {
      LOG.debug("Ingoring request of action '" + action + "' for: " + req.getRequestPath());
    }
    return true;
  }

  public boolean shouldRetrieve(Repository repository, ResourceStoreRequest request, StorageItem item) throws IllegalOperationException, ItemNotFoundException, AccessDeniedException {
    LOG.debug("Returns true for 'retrieve', on item: " + item.getPath());
    return true;
  }

  public boolean shouldCache(ProxyRepository proxy, AbstractStorageItem item) {
    LOG.debug("Returns true for 'cache', on item: " + item.getPath());
    return true;
  }

  public boolean shouldProxy(ProxyRepository proxy, ResourceStoreRequest req) {
    LOG.debug("Returns true for 'proxy', on request: " + req.getRequestPath());
    return true;
  }


  // --- PRIVATE METHODS --- //

  private void adaptLogging() {
    /*
     * Adapt the logging to use slf4j instead.
     */
    tracker.setLoggingAdapter(new LoggingAdapter() {

      public void logMessage(String msg) {
        LOG.debug(msg);
      }

      public void logError(String msg) {
        LOG.error(msg);
      }
    });
  }

  private boolean checkGaTrackerId() {
    return PropertiesLoader.GA_TRACKER_ID != null && !PropertiesLoader.GA_TRACKER_ID.equals("");
  }

  // --- INNER CLASSES --- //

  /**
   * Simple inner class that adapts the content URI to
   * not be URL-escaped.
   */
  private static class Point extends FocusPoint {

    public Point(String name, String userName) {
      super(name, userName);
    }

    @Override
    public String getContentURI() {
      return getName();
    }
  }
}
