package org.openmrs.module.programautoenrolment.advice;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PersonAddress;
import org.openmrs.Program;
import org.openmrs.api.PatientService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.programautoenrolment.ProgramAutoEnrolmentProperties;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.resource.api.Creatable;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class PatientProgramAutoEnrolmentAdvice implements AfterReturningAdvice {
    private final String diagnosisUuid;
    private final String ancProgramUuid;
    private final List preferredVDCs;
    private final String confirmed;
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;
    private PatientService patientService;
    private Creatable bahmniProgramEnrollmentResource;

    public PatientProgramAutoEnrolmentAdvice(BahmniProgramWorkflowService bahmniProgramWorkflowService,
                                             PatientService patientService,
                                             Creatable bahmniProgramEnrollmentResource
    ) {
        this.patientService= patientService;
        this.bahmniProgramWorkflowService = bahmniProgramWorkflowService;
        this.bahmniProgramEnrollmentResource = bahmniProgramEnrollmentResource;
        diagnosisUuid = ProgramAutoEnrolmentProperties.getProperty("ANC.diagnosis.uuid");
        ancProgramUuid = ProgramAutoEnrolmentProperties.getProperty("ANC.program.uuid");
        preferredVDCs = CollectionUtils.arrayToList(ProgramAutoEnrolmentProperties.getProperty("ANC.VDCs")
            .split(","));
        confirmed = "CONFIRMED";
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] transactions, Object o1) throws Throwable {
        BahmniEncounterTransaction encounterTransaction = (BahmniEncounterTransaction) transactions[0];
        String patientUuid = encounterTransaction.getPatientUuid();
        if (isExpectedDiagnosisPresent(encounterTransaction) && hasPreferredVDC(patientUuid) &&
            !isEnrolledToProgram(patientUuid, ancProgramUuid)) {
            enrollPatient(patientUuid, ancProgramUuid);
        }
    }

    private boolean isExpectedDiagnosisPresent(BahmniEncounterTransaction encounterTransaction) {
        List<BahmniDiagnosisRequest> bahmniDiagnoses = encounterTransaction.getBahmniDiagnoses();
        for (BahmniDiagnosisRequest bahmniDiagnose : bahmniDiagnoses) {
            if (diagnosisUuid.equals(bahmniDiagnose.getCodedAnswer().getUuid()) &&
                confirmed.toLowerCase().equals(bahmniDiagnose.getCertainty().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPreferredVDC(String patientUuid) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        Set<PersonAddress> addresses = patient.getAddresses();
        for (PersonAddress address : addresses) {
            String cityVillage = address.getCityVillage();
            if (preferredVDCs.contains(cityVillage)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnrolledToProgram(String patientUuid, String programUuid) {
        Program program = bahmniProgramWorkflowService.getProgramByUuid(programUuid);
        Patient patient = patientService.getPatientByUuid(patientUuid);
        List<PatientProgram> patientPrograms = bahmniProgramWorkflowService.getPatientPrograms(patient, program, null, null, null, null, false);
        for (PatientProgram patientProgram : patientPrograms) {
            if(isRunning(patientProgram)){
                return true;
            }
        }
        return false;
    }

    private boolean isRunning(PatientProgram patientProgram) {
        return null == patientProgram.getDateCompleted();
    }

    private void enrollPatient(String patientUuid, String programUuid) {
        SimpleObject patientProgram = new SimpleObject();
        String startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());
        patientProgram.add("patient", patientUuid);
        patientProgram.add("program", programUuid);
        patientProgram.add("dateEnrolled", startDate);
        patientProgram.add("attributes", Arrays.asList());
        bahmniProgramEnrollmentResource.create(patientProgram, null);
    }
}
