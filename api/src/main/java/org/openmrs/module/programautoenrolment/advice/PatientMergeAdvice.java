package org.openmrs.module.programautoenrolment.advice;
import java.util.List;

public interface PatientMergeAdvice {
    void verifyBedAssignmentStatus(List patients);
}
