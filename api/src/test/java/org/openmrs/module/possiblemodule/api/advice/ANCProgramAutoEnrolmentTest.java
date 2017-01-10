package org.openmrs.module.possiblemodule.api.advice;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.possiblemodule.api.ProgramAutoEnrolmentProperties;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.api.RestService;
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
@PrepareForTest({Context.class})
public class ANCProgramAutoEnrolmentTest {

    @Mock
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;
    @Mock
    private PatientService patientService;
    @Mock
    private Creatable resource;
    @Mock
    private RestService restService;
    @Mock
    private ProgramAutoEnrolmentProperties programAutoEnrolmentProperties;

    private ANCProgramAutoEnrolment ancProgramAutoEnrolment;


    @Before
    public void setup() throws Exception {
        initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getService(ProgramAutoEnrolmentProperties.class)).thenReturn(programAutoEnrolmentProperties);
        when(programAutoEnrolmentProperties.getProperty("ANC.diagnosis.uuid")).thenReturn("f7e858d5-5328-4120-9198-974fc401a050");
        when(programAutoEnrolmentProperties.getProperty("ANC.program.uuid")).thenReturn("473fb329-d74b-4d72-9a9b-4979c56eac27");
        when(programAutoEnrolmentProperties.getProperty("ANC.VDCs")).thenReturn("Sanfebagar,Municipality,Baradadevi,Payal");

        when(Context.getPatientService()).thenReturn(patientService);
        when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(bahmniProgramWorkflowService);
        when(Context.getService(RestService.class)).thenReturn(restService);
        when(restService.getResourceBySupportedClass(BahmniPatientProgram.class)).thenReturn(resource);

        ancProgramAutoEnrolment = new ANCProgramAutoEnrolmentImpl(programAutoEnrolmentProperties);
    }

    @Test
    public void shouldNotEnrollWhenPregnancyIsNotConfirmed() throws Throwable {
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction(encounterTransaction);
        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();

        List<BahmniDiagnosisRequest> bahmniDiagnoses = Arrays.asList(bahmniDiagnosisRequest);
        transaction.setBahmniDiagnoses(bahmniDiagnoses);

        bahmniDiagnosisRequest.setCodedAnswer(new EncounterTransaction.Concept("uuid1", "nothing"));
        ancProgramAutoEnrolment.enroll(transaction);
        verify(resource,never()).create(null,null);

        bahmniDiagnosisRequest.setCodedAnswer(new EncounterTransaction.Concept("f7e858d5-5328-4120-9198-974fc401a050", "Pregnancy Confirmed"));
        bahmniDiagnosisRequest.setCertainty("PRESUMED");
        ancProgramAutoEnrolment.enroll(transaction);
        verify(resource,never()).create(null,null);
    }

    @Test
    public void shouldNotEnrollWhenPregnancyIsConfirmedButVDCDoesNotMatch() throws Throwable {
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction(encounterTransaction);
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

        ancProgramAutoEnrolment.enroll(transaction);
        verify(resource,never()).create(null,null);

    }

    @Test
    public void shouldEnrollWhenPregnancyIsConfirmedANDVDCMatches() throws Throwable {
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction(encounterTransaction);
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

        ancProgramAutoEnrolment.enroll(transaction);

        ArgumentCaptor<SimpleObject> postContentCapture = ArgumentCaptor.forClass(SimpleObject.class);
        ArgumentCaptor<RequestContext> requestContextCaptor = ArgumentCaptor.forClass(RequestContext.class);
        verify(resource,times(1)).create(postContentCapture.capture(),requestContextCaptor.capture());

        SimpleObject simpleObject = postContentCapture.getValue();
        assertEquals(simpleObject.get("patient"),"patient-uuid");
        String programUuid = "473fb329-d74b-4d72-9a9b-4979c56eac27";
        assertEquals(simpleObject.get("program"), programUuid);
        assertEquals(simpleObject.get("attributes"),Arrays.asList());
    }

    @Test
    public void shouldLogFailureOnException() throws Exception{
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction(encounterTransaction);
        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();

        List<BahmniDiagnosisRequest> bahmniDiagnoses = Arrays.asList(bahmniDiagnosisRequest);
        transaction.setBahmniDiagnoses(bahmniDiagnoses);

        bahmniDiagnosisRequest.setCodedAnswer(new EncounterTransaction.Concept("f7e858d5-5328-4120-9198-974fc401a050", "Pregnancy Confirmed"));
        bahmniDiagnosisRequest.setCertainty("CONFIRMED");

        when(Context.getPatientService()).thenReturn(null);

        ancProgramAutoEnrolment.enroll(transaction);

        verify(resource,never()).create(null,null);
    }
}