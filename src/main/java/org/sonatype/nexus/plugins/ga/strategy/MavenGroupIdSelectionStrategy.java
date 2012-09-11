package org.sonatype.nexus.plugins.ga.strategy;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.GavCalculator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.ga.GoogleAnalyticsPluginConfiguration;
import org.sonatype.nexus.plugins.ga.NexusFPoint;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.MavenRepository;

import java.util.Collection;
import java.util.Collections;

@Component(role = PointSelectionStrategy.class, hint = "MavenGroupIdSelectionStrategy")
public class MavenGroupIdSelectionStrategy
        implements PointSelectionStrategy {
  @Requirement
  private GoogleAnalyticsPluginConfiguration configuration;

  public Collection<NexusFPoint> getNexusFPointFor(RepositoryItemUid uid) {
    String groupId = null;

    if (uid.getRepository().getRepositoryKind().isFacetAvailable(MavenRepository.class)) {
      MavenRepository mavenRepository = uid.getRepository().adaptToFacet(MavenRepository.class);

      GavCalculator gavCalculator = mavenRepository.getGavCalculator();

      Gav gav = gavCalculator.pathToGav(uid.getPath());

      if (gav != null) {
        groupId = gav.getGroupId();
      }
    }

    if (groupId != null && groupId.trim().length() > 0) {
      FocusPoint fp = new FocusPoint(groupId, new FocusPoint(uid.getRepository().getId()));

      // TODO: trackerID!!!
      NexusFPoint nfp = new NexusFPoint(fp, configuration.getUITrackerId());

      return Collections.singletonList(nfp);
    } else {
      return Collections.emptyList();
    }
  }
}
