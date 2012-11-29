package org.mule.nexus.plugins.ga;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

class PropertiesLoader {
  /*
   * The GA trackerId used to track artifacts.
   */
  static String GA_TRACKER_ID;
  /**
   * The list of repositories to be tracked by GA.
   */
  static Set<String> REPOSITORIES;

  static void loadProperties() {
    Properties props = new Properties();

    try {
      props.load(GaTrackerRequestProcessor.class.getResourceAsStream("/nexus-ga-plugin.properties"));
    } catch (IOException e) {
      //do nothing
    }

    String repositoriesLocal = props.getProperty("trackedRepositories");
    if (repositoriesLocal != null && !repositoriesLocal.equals("")) {
      REPOSITORIES = new HashSet<String>(Arrays.asList(repositoriesLocal.split(",")));
    } else {
      REPOSITORIES = Collections.emptySet();
    }

    String trackerIdLocal = props.getProperty("trackerId");
    if (trackerIdLocal != null) {
      GA_TRACKER_ID = props.getProperty("trackerId");
    }
  }
}
