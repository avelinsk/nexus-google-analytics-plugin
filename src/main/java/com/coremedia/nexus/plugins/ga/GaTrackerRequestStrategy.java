package com.coremedia.nexus.plugins.ga;


import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;
import com.boxysystems.jgoogleanalytics.LoggingAdapter;
import org.apache.shiro.subject.Subject;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestStrategy;
import org.sonatype.security.SecuritySystem;

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
public class GaTrackerRequestStrategy implements RequestStrategy {

  @Requirement
  private SecuritySystem securitySystem;

  private final JGoogleAnalyticsTracker tracker;
  private final Logger LOG = LoggerFactory.getLogger(getClass());

  public GaTrackerRequestStrategy() {
    PropertiesLoader.loadProperties();
    tracker = new JGoogleAnalyticsTracker("Nexus", PropertiesLoader.NEXUS_VERSION, PropertiesLoader.GA_TRACKER_ID);
    LOG.info("Creating new Google Analytics tracker with id: " + PropertiesLoader.GA_TRACKER_ID);
    adaptLogging();
  }

  public void onHandle(Repository rep, ResourceStoreRequest req, Action action) {
    String userName = "anonymous";

    if (securitySystem != null) {
      final Subject subject = securitySystem.getSubject();
      if (subject != null) {
        LOG.debug("All Principals: " + subject.getPrincipals().toString());
        LOG.debug("Current Principal: " + subject.getPrincipal().toString());
        userName = subject.getPrincipal().toString();
      }
    }

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
  }

  public void onServing(Repository repository, ResourceStoreRequest request, StorageItem item) {
    LOG.debug("Returns true for 'retrieve', on item: " + item.getPath());
  }

  @Override
  public void onRemoteAccess(ProxyRepository repository, ResourceStoreRequest request, StorageItem item) {
    LOG.debug("Remote access for path: " + request.getRequestPath() + " for  item: " + item.getPath());
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
