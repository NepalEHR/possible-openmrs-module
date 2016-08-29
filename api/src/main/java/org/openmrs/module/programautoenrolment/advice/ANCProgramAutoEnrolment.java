package org.openmrs.module.programautoenrolment.advice;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;

public interface ANCProgramAutoEnrolment {
    void enroll(BahmniEncounterTransaction transaction);
}