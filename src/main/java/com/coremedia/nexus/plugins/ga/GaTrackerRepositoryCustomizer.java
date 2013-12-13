package com.coremedia.nexus.plugins.ga;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.plugins.RepositoryCustomizer;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestStrategy;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Small customizer, which only adds the GA tracker to the repositories.
 * Ideally we'd only like to add it to internal (hosted) repositories, but
 * as those don't get called if they are in a group (which all of them are)
 * we'll add this to all.
 *
 */
public class GaTrackerRepositoryCustomizer implements RepositoryCustomizer {

  private static final Logger LOG = LoggerFactory.getLogger(GaTrackerRepositoryCustomizer.class);

  public GaTrackerRepositoryCustomizer() {
    PropertiesLoader.loadProperties();
  }

  @Inject
  @Named("gaTracker")
  private RequestStrategy gaTracker;

  public void configureRepository(Repository rep) throws ConfigurationException {
    for (String s : PropertiesLoader.REPOSITORIES) {
      if (s.equalsIgnoreCase(rep.getId())) {
        LOG.info("Attaching Google Analytics tracker to: " + rep.getName());
        rep.registerRequestStrategy("gaTracker", gaTracker);
      }
    }
  }

  public boolean isHandledRepository(Repository rep) {
    boolean handlesRepository = PropertiesLoader.REPOSITORIES.contains(rep.getId());
    LOG.debug("Handles repository '" + rep.getName() + "': " + handlesRepository);
    return handlesRepository;
  }
}
