package org.openmrs.module.programautoenrolment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("file:/opt/openmrs/program-auto-enrolment.properties")
public class ProgramAutoEnrolmentProperties {

    @Autowired
    public Environment environment;

    public String getProperty(String key) {
        String property = environment.getProperty(key);
        if (null == property) {
            property = "";
        }
        return property;
    }
}
