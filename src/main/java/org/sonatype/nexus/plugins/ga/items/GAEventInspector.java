package org.sonatype.nexus.plugins.ga.items;

import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.ga.GATrackerCache;
import org.sonatype.nexus.plugins.ga.GoogleAnalyticsPluginConfiguration;
import org.sonatype.nexus.plugins.ga.NexusFPoint;
import org.sonatype.nexus.plugins.ga.NexusFPointSelector;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.plexus.appevents.Event;

import java.util.Collection;

@Component( role = EventInspector.class, hint = "GAEventInspector" )
public class GAEventInspector
    extends AbstractEventInspector
{
    @Requirement
    private GoogleAnalyticsPluginConfiguration configuration;

    @Requirement
    private NexusFPointSelector nexusFPointSelector;

    @Requirement
    private GATrackerCache gaTrackerCache;

    public boolean accepts( Event<?> evt )
    {
        if ( configuration.isItemTrackingEnabled() )
        {
            return evt instanceof RepositoryItemEventRetrieve
                && evt.getEventContext().containsKey( AccessManager.REQUEST_REMOTE_ADDRESS );
        }
        else
        {
            return false;
        }
    }

    public void inspect( Event<?> evt )
    {
        RepositoryItemEventRetrieve revent = (RepositoryItemEventRetrieve) evt;

        Collection<NexusFPoint> fpoints = nexusFPointSelector.getNexusFPointFor( revent.getItemUid() );

        for ( NexusFPoint fpoint : fpoints )
        {
            JGoogleAnalyticsTracker gaTracker = gaTrackerCache.getGATracker( fpoint );

            gaTracker.trackAsynchronously( fpoint.getFocusPoint() );
        }
    }
}
