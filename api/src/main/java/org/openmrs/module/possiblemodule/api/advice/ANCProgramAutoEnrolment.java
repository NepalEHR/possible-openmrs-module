package org.openmrs.module.possiblemodule.api.advice;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;

public interface ANCProgramAutoEnrolment {
    void enroll(BahmniEncounterTransaction transaction);
}