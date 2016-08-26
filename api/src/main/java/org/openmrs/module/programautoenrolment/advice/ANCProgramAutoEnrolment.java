package org.openmrs.module.programautoenrolment.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PersonAddress;
import org.openmrs.Program;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.programautoenrolment.ProgramAutoEnrolmentProperties;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.api.Creatable;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ANCProgramAutoEnrolment{
    private final String confirmed = "CONFIRMED";
    private final String dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";

    private String diagnosisUuid;
    private String ancProgramUuid;
    private List preferredVDCs;
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;
    private PatientService patientService;
    private Creatable bahmniProgramEnrollmentResource;
    private static Log log = LogFactory.getLog(ANCProgramAutoEnrolment.class);

    private void initFields() {
        this.patientService = Context.getPatientService();
        this.bahmniProgramWorkflowService = Context.getService(BahmniProgramWorkflowService.class);
        this.bahmniProgramEnrollmentResource = (Creatable) Context.getService(RestService.class)
            .getResourceBySupportedClass(BahmniPatientProgram.class);
        this.diagnosisUuid = ProgramAutoEnrolmentProperties.getProperty("ANC.diagnosis.uuid");
        this.ancProgramUuid = ProgramAutoEnrolmentProperties.getProperty("ANC.program.uuid");
        this.preferredVDCs = CollectionUtils.arrayToList(ProgramAutoEnrolmentProperties.getProperty("ANC.VDCs")
            .split(","));
    }

    public void enrollWithSafety(BahmniEncounterTransaction transaction) {
        try {
            enrollOnEligible(transaction);
        } catch (Throwable e) {
            log.error("Program Auto Enrolment is failing due to" + e.getMessage());
        }
    }

    private void enrollOnEligible(BahmniEncounterTransaction encounterTransaction) {
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
            if (isRunning(patientProgram)) {
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
        String startDate = new SimpleDateFormat(dateFormat).format(new Date());
        patientProgram.add("patient", patientUuid);
        patientProgram.add("program", programUuid);
        patientProgram.add("dateEnrolled", startDate);
        patientProgram.add("attributes", Arrays.asList());
        bahmniProgramEnrollmentResource.create(patientProgram, null);
    }

    public static ANCProgramAutoEnrolment create() {
        ANCProgramAutoEnrolment ancProgramAutoEnrolment = new ANCProgramAutoEnrolment();
        try {
            ancProgramAutoEnrolment.initFields();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return ancProgramAutoEnrolment;
    }
}
