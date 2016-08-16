package org.openmrs.module.programautoenrolment;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ProgramAutoEnrolmentProperties {

    private static final String PROGRAM_AUTO_ENROLMENT_PROPERTIES = "program-auto-enrolment.properties";
    private static Log log = LogFactory.getLog(ProgramAutoEnrolmentProperties.class);
    private static Properties properties;

    public static void load () {
        properties = new java.util.Properties(System.getProperties());
        File file = new File(OpenmrsUtil.getApplicationDataDirectory(), PROGRAM_AUTO_ENROLMENT_PROPERTIES);
        if (!(file.exists() && file.canRead())) {
            log.warn(PROGRAM_AUTO_ENROLMENT_PROPERTIES + " does not exist or not readable.");
            return;
        }

        String propertyFile = file.getAbsolutePath();
        log.info(String.format("Reading bahmni properties from : %s", propertyFile));
        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        String property = properties.getProperty(key);
        if(null == property){
            return "";
        }
        return property;
    }

    public static void initialize(java.util.Properties props) {
        properties = props;
    }

}
