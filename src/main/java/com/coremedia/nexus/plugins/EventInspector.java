package com.coremedia.nexus.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.auth.ClientInfo;
import org.sonatype.nexus.auth.NexusAuthenticationEvent;
import org.sonatype.plexus.appevents.Event;

import java.util.Map;

/**
 * Used for debugging
 */
public class EventInspector implements org.sonatype.nexus.proxy.events.EventInspector {

  private static final Logger LOG = LoggerFactory.getLogger(EventInspector.class);

  public boolean accepts(Event<?> evt) {
//    return ( evt instanceof NexusAuthorizationEvent);
    return false;
  }

  public void inspect(Event<?> evt) {
    if (evt instanceof NexusAuthenticationEvent) {
      LOG.info("Authentication event: " + evt.toString());
      ClientInfo clientInfo = ((NexusAuthenticationEvent) evt).getClientInfo();
      LOG.info("UID and Agent: " +clientInfo.getUserid() + " " + clientInfo.getUserAgent());

    } else {
      LOG.info("Received event: " +
              evt.toString() + " with sender " +
              evt.getEventSender().toString());
              Map evtContext = evt.getEventContext();
      for (Object o : evtContext.keySet()) {
        LOG.info("Event context: element= " + o.toString() + " value= " + evtContext.get(o).toString());
      }
    }
  }
}
