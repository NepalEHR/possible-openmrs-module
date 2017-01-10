package org.openmrs.module.programautoenrolment.advisor;

import org.aopalliance.aop.Advice;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.BedManagementService;
import org.openmrs.module.programautoenrolment.advice.PatientMergeAdvice;
import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PatientMergeAdvisor extends StaticMethodMatcherPointcutAdvisor implements Advisor {
    private static final String METHOD = "mergePatients";
    private static final Class SECOND_PARAM_TYPE = List.class;

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        // TODO: 1/9/17 Need to implement this using pointcut or some of the same sort. So that it is more specific.
        // It may look like 'execution(public void PatientService.mergePatients(<argument description>))'
        return METHOD.equals(method.getName()) && SECOND_PARAM_TYPE.equals(method.getParameterTypes()[1]);
    }

    @Override
    public Advice getAdvice() {
        return new BeforeAdvice();
    }

    private class BeforeAdvice implements MethodBeforeAdvice {
        @Override
        public void before(Method method, Object[] args, Object target) throws Throwable {
            List patients = new ArrayList();
            patients.add(args[0]);
            patients.addAll((List)args[1]);
            Context.getService(PatientMergeAdvice.class).verifyBedAssignmentStatus(patients);
        }
    }
}

