package org.sonatype.nexus.plugins.ga.impl;

import com.boxysystems.jgoogleanalytics.GoogleAnalytics_v1_URLBuildingStrategy;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;
import com.boxysystems.jgoogleanalytics.LoggingAdapter;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.plugins.ga.GATrackerCache;
import org.sonatype.nexus.plugins.ga.NexusFPoint;

import java.util.HashMap;

@Component(role = GATrackerCache.class)
public class DefaultGATrackerCache
        implements GATrackerCache {

  @Requirement
  private Logger logger;

  private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

  @Requirement
  private ApplicationStatusSource applicationStatusSource;

  @Requirement
  private GlobalRestApiSettings globalRestApiSettings;

  private final HashMap<String, JGoogleAnalyticsTracker> jgaMap = new HashMap<String, JGoogleAnalyticsTracker>();

  public synchronized JGoogleAnalyticsTracker getGATracker(NexusFPoint nexusFPoint) {
    JGoogleAnalyticsTracker tracker = jgaMap.get(nexusFPoint.getTrackerId());

    if (tracker == null) {
      tracker = createJGoogleAnalyticsTracker(nexusFPoint);

      jgaMap.put(nexusFPoint.getTrackerId(), tracker);
    }

    return tracker;
  }

  // ==

  protected JGoogleAnalyticsTracker createJGoogleAnalyticsTracker(NexusFPoint fp) {
    JGoogleAnalyticsTracker tracker;

    tracker =
            new JGoogleAnalyticsTracker(applicationStatusSource.getSystemStatus().getAppName(),
                    applicationStatusSource.getSystemStatus().getVersion(), fp.getTrackerId());

    if (globalRestApiSettings.isEnabled() && globalRestApiSettings.isForceBaseUrl()
            && StringUtils.isNotEmpty(globalRestApiSettings.getBaseUrl())) {
      // we do it kinda awkward, but this what API gives us
      // tracker.getStrategy() would be nicer ;)
      // or a constructor taking one
      GoogleAnalytics_v1_URLBuildingStrategy urlb =
              new GoogleAnalytics_v1_URLBuildingStrategy(applicationStatusSource.getSystemStatus().getAppName(),
                      applicationStatusSource.getSystemStatus().getVersion(), fp.getTrackerId());

      // set new referer
      urlb.setRefererURL(globalRestApiSettings.getBaseUrl());

      tracker.setUrlBuildingStrategy(urlb);
    }

    tracker.setLoggingAdapter(new LoggingAdapter() {

      public void logMessage(String msg) {
        log.debug(msg);
      }

      public void logError(String msg) {
        log.error(msg);
      }
    });

    return tracker;
  }
}
