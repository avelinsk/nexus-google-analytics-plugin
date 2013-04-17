package com.coremedia.nexus.plugins;


import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.shiro.subject.Subject;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.security.SecuritySystem;

public class LoggingConverter  extends ClassicConverter {

    @Requirement
    private SecuritySystem securitySystem;

    @Override
    public String convert(ILoggingEvent event) {
        String userName = "anonymous";

        if (securitySystem != null) {
            final Subject subject = securitySystem.getSubject();
            if (subject != null) {
                userName = subject.getPrincipal().toString();
            }
        }
        return userName;
    }

}
