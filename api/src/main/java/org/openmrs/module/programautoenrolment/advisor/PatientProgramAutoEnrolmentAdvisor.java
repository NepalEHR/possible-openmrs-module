package org.openmrs.module.programautoenrolment.advisor;

import org.aopalliance.aop.Advice;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.programautoenrolment.advice.PatientProgramAutoEnrolmentAdvice;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.api.Creatable;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

import java.lang.reflect.Method;

public class PatientProgramAutoEnrolmentAdvisor extends StaticMethodMatcherPointcutAdvisor implements Advisor {
    private static final String SAVE_METHOD = "save";

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        return SAVE_METHOD.equals(method.getName());
    }

    @Override
    public Advice getAdvice() {
        return new PatientProgramAutoEnrolmentAdvice(
            Context.getService(BahmniProgramWorkflowService.class),
            Context.getPatientService(),
            (Creatable) Context.getService(RestService.class).getResourceBySupportedClass(BahmniPatientProgram.class));
    }
}
