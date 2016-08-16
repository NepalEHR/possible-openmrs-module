package org.openmrs.module.programautoenrolment.advice;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.api.PatientService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.programautoenrolment.ProgramAutoEnrolmentProperties;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.Creatable;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ProgramAutoEnrolmentProperties.class)
public class PatientProgramAutoEnrolmentAdviceTest {

    @Mock
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;
    @Mock
    private PatientService patientService;
    @Mock
    private Creatable resource;

    private PatientProgramAutoEnrolmentAdvice patientProgramAutoEnrolmentAdvice;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        PowerMockito.mockStatic(ProgramAutoEnrolmentProperties.class);
        when(ProgramAutoEnrolmentProperties.getProperty("ANC.diagnosis.uuid")).thenReturn("f7e858d5-5328-4120-9198-974fc401a050");
        when(ProgramAutoEnrolmentProperties.getProperty("ANC.program.uuid")).thenReturn("473fb329-d74b-4d72-9a9b-4979c56eac27");
        when(ProgramAutoEnrolmentProperties.getProperty("ANC.VDCs")).thenReturn("Sanfebagar,Municipality,Baradadevi,Payal");

        patientProgramAutoEnrolmentAdvice = new PatientProgramAutoEnrolmentAdvice(
            bahmniProgramWorkflowService,
            patientService,
            resource
        );
    }

    @Test
    public void shouldNotEnrollWhenPregnancyIsNotConfirmed() throws Throwable {
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction(encounterTransaction);
        BahmniEncounterTransaction[] transactions = new BahmniEncounterTransaction[]{transaction};
        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();

        List<BahmniDiagnosisRequest> bahmniDiagnoses = Arrays.asList(bahmniDiagnosisRequest);
        transaction.setBahmniDiagnoses(bahmniDiagnoses);

        bahmniDiagnosisRequest.setCodedAnswer(new EncounterTransaction.Concept("uuid1", "nothing"));
        patientProgramAutoEnrolmentAdvice.afterReturning(null, null, transactions, null);
        verify(resource,never()).create(null,null);

        bahmniDiagnosisRequest.setCodedAnswer(new EncounterTransaction.Concept("f7e858d5-5328-4120-9198-974fc401a050", "Pregnancy Confirmed"));
        bahmniDiagnosisRequest.setCertainty("PRESUMED");
        patientProgramAutoEnrolmentAdvice.afterReturning(null, null, transactions, null);
        verify(resource,never()).create(null,null);
    }

    @Test
    public void shouldNotEnrollWhenPregnancyIsConfirmedButVDCDoesNotMatch() throws Throwable {
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction(encounterTransaction);
        BahmniEncounterTransaction[] transactions = new BahmniEncounterTransaction[]{transaction};
        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();

        List<BahmniDiagnosisRequest> bahmniDiagnoses = Arrays.asList(bahmniDiagnosisRequest);
        transaction.setBahmniDiagnoses(bahmniDiagnoses);

        bahmniDiagnosisRequest.setCodedAnswer(new EncounterTransaction.Concept("f7e858d5-5328-4120-9198-974fc401a050", "Pregnancy Confirmed"));
        bahmniDiagnosisRequest.setCertainty("CONFIRMED");

        Patient patient = new Patient();

        encounterTransaction.setPatientUuid("patient-uuid");
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);

        LinkedHashSet<PersonAddress> addresses = new LinkedHashSet<PersonAddress>();
        PersonAddress address = new PersonAddress();
        address.setCityVillage("Gujarat");
        addresses.add(address);
        patient.setAddresses(addresses);

        patientProgramAutoEnrolmentAdvice.afterReturning(null, null, transactions, null);
        verify(resource,never()).create(null,null);

    }

    @Test
    public void shouldEnrollWhenPregnancyIsConfirmedANDVDCMatches() throws Throwable {
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction(encounterTransaction);
        BahmniEncounterTransaction[] transactions = new BahmniEncounterTransaction[]{transaction};
        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();

        List<BahmniDiagnosisRequest> bahmniDiagnoses = Arrays.asList(bahmniDiagnosisRequest);
        transaction.setBahmniDiagnoses(bahmniDiagnoses);

        bahmniDiagnosisRequest.setCodedAnswer(new EncounterTransaction.Concept("f7e858d5-5328-4120-9198-974fc401a050", "Pregnancy Confirmed"));
        bahmniDiagnosisRequest.setCertainty("CONFIRMED");

        Patient patient = new Patient();

        encounterTransaction.setPatientUuid("patient-uuid");
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);

        LinkedHashSet<PersonAddress> addresses = new LinkedHashSet<PersonAddress>();
        PersonAddress address = new PersonAddress();
        address.setCityVillage("Payal");
        addresses.add(address);
        patient.setAddresses(addresses);

        patientProgramAutoEnrolmentAdvice.afterReturning(null, null, transactions, null);

        ArgumentCaptor<SimpleObject> postContentCapture = ArgumentCaptor.forClass(SimpleObject.class);
        ArgumentCaptor<RequestContext> requestContextCaptor = ArgumentCaptor.forClass(RequestContext.class);
        verify(resource,times(1)).create(postContentCapture.capture(),requestContextCaptor.capture());

        SimpleObject simpleObject = postContentCapture.getValue();
        assertEquals(simpleObject.get("patient"),"patient-uuid");
        String programUuid = "473fb329-d74b-4d72-9a9b-4979c56eac27";
        assertEquals(simpleObject.get("program"), programUuid);
        assertEquals(simpleObject.get("attributes"),Arrays.asList());
    }
}