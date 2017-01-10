package org.openmrs.module.possiblemodule.api.advice;
import java.util.List;

public interface PatientMergeAdvice {
    void verifyBedAssignmentStatus(List patients);
}
