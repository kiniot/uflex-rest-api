package com.kiniot.uflex.api.planning.interfaces.rest.controllers;

import com.kiniot.uflex.api.planning.domain.exceptions.PatientClinicMismatchException;
import com.kiniot.uflex.api.planning.domain.model.queries.GetActiveTreatmentPlanByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.model.queries.GetTreatmentPlansByPatientIdQuery;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanCommandService;
import com.kiniot.uflex.api.planning.domain.services.TreatmentPlanQueryService;
import com.kiniot.uflex.api.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatientTreatmentPlansControllerTests {

    private TreatmentPlanQueryService queryService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var commandService = mock(TreatmentPlanCommandService.class);
        queryService = mock(TreatmentPlanQueryService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PatientTreatmentPlansController(commandService, queryService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listPatientPlansReturnsOkWithEmptyCollection() throws Exception {
        when(queryService.handle(any(GetTreatmentPlansByPatientIdQuery.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/patients/{patientId}/treatment-plans", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void activeRoutineReturnsNotFoundWhenPatientHasNoActivePlan() throws Exception {
        when(queryService.handle(any(GetActiveTreatmentPlanByPatientIdQuery.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/patients/{patientId}/treatment-plans/active", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void crossTenantPatientAccessReturnsConflict() throws Exception {
        when(queryService.handle(any(GetTreatmentPlansByPatientIdQuery.class)))
                .thenThrow(new PatientClinicMismatchException("patient-1", "clinic-1"));

        mockMvc.perform(get("/api/v1/patients/{patientId}/treatment-plans", UUID.randomUUID()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void malformedPatientIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/patients/not-a-uuid/treatment-plans"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deniedPatientPlanAccessReturnsForbidden() throws Exception {
        when(queryService.handle(any(GetTreatmentPlansByPatientIdQuery.class)))
                .thenThrow(new AccessDeniedException("denied"));

        mockMvc.perform(get("/api/v1/patients/{patientId}/treatment-plans", UUID.randomUUID()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
