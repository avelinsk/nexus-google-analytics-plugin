package org.mule.nexus.plugins.ga;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.plugins.RepositoryCustomizer;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

/**
 * Small customizer, which only adds the GA tracker to the repositories.
 * Ideally we'd only like to add it to internal (hosted) repositories, but
 * as those don't get called if they are in a group (which all of them are)
 * we'll add this to all.
 *
 * @author Lars J. Nilsson
 * @author avelinsk
 */
public class GaTrackerRepositoryCustomizer implements RepositoryCustomizer {

  static {
    PropertiesLoader.loadProperties();
  }

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  @Named("gaTracker")
  private RequestProcessor gaTracker;

  public void configureRepository(Repository rep) throws ConfigurationException {
    for (String s : PropertiesLoader.REPOSITORIES) {
      if (s.equalsIgnoreCase(rep.getId())) {
        log.info("Attaching Google Analytics tracker to: " + rep.getName());
        rep.getRequestProcessors().put("gaTracker", gaTracker);
      }
    }
  }

  public boolean isHandledRepository(Repository rep) {
    boolean handlesRepository = PropertiesLoader.REPOSITORIES.contains(rep.getId());
    log.info("Handles repository '" + rep.getName() + "': " + handlesRepository);
    return handlesRepository;
  }
}
