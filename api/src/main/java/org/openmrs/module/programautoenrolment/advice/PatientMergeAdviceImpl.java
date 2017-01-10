package org.openmrs.module.programautoenrolment.advice;

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.BedManagementService;

import java.util.List;

public class PatientMergeAdviceImpl implements PatientMergeAdvice {
    private static final String ERROR_MESSAGE = "Bed is assigned to Patient: 'Patient.Identifier'. Unassign before continuing";
    @Override
    public void verifyBedAssignmentStatus(List patients) {
        for (Object patient_ : patients) {
            Patient patient = (Patient)patient_;
            BedDetails notPreferredPatient = Context.getService(BedManagementService.class).getBedAssignmentDetailsByPatient(patient);
            if (notPreferredPatient != null) {
                String message = ERROR_MESSAGE.replace("Patient.Identifier", patient.getPatientIdentifier().toString());
                throw new APIException(message);
            }
        }
    }
}
