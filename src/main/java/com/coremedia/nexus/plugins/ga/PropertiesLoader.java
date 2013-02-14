package com.coremedia.nexus.plugins.ga;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

class PropertiesLoader {
  static Properties props;

  PropertiesLoader() {
    loadProperties();
  }

  /**
   * Nexus Version
   */
  static String NEXUS_VERSION;

  /**
   * GA trackerId
   */
  static String GA_TRACKER_ID;

  /**
   * List of repositories to be tracked
   */
  static Set<String> REPOSITORIES;

  static void loadProperties() {
    props = new Properties();

    try {
      props.load(PropertiesLoader.class.getResourceAsStream("/nexus-ga-plugin.properties"));
    } catch (IOException e) {
      //do nothing
    }

    String repositoriesLocal = props.getProperty("trackedRepositories");
    REPOSITORIES = (repositoriesLocal != null && !repositoriesLocal.equals(""))
            ? new HashSet<String>(Arrays.asList(repositoriesLocal.split(",")))
            : Collections.<String>emptySet();

    String trackerId = props.getProperty("trackerId");
    GA_TRACKER_ID = trackerId != null ? trackerId : "";

    String nexusVersion = props.getProperty("nexusVersion");
    NEXUS_VERSION = nexusVersion != null ? NEXUS_VERSION = nexusVersion : "";
  }

}
