package org.openmrs.module.programautoenrolment.advice;


import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.BedManagementService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
public class PatientMergeAdviceTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private BedManagementService bedManagementService;

    private PatientMergeAdvice patientMergeAdvice;

    @Before
    public void setup(){
        initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getService(BedManagementService.class)).thenReturn(bedManagementService);

        patientMergeAdvice = new PatientMergeAdviceImpl();
    }
    @Test
    public void shouldThrowExceptionIfAnyPatientHasBedAssigned(){
        expectedException.expect(APIException.class);
        expectedException.expectMessage(Matchers.is("Bed is assigned to Patient: 'BAH1234568'. Unassign before continuing"));

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();
        PatientIdentifier patientIdentifier = new PatientIdentifier("BAH1234568",null,null);
        patient2.setIdentifiers(Collections.singleton(patientIdentifier));

        when(bedManagementService.getBedAssignmentDetailsByPatient(patient1)).thenReturn(null);
        when(bedManagementService.getBedAssignmentDetailsByPatient(patient2)).thenReturn(new BedDetails());

        patientMergeAdvice.verifyBedAssignmentStatus(Arrays.asList(patient1,patient2));
    }
}
