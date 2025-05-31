package com.pm.patientservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@ActiveProfiles("test")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    private PatientResponseDTO testPatientResponse;
    private PatientRequestDTO testPatientRequest;
    private UUID testPatientId;

    @BeforeEach
    void setUp() {
        testPatientId = UUID.randomUUID();
        
        testPatientResponse = new PatientResponseDTO();
        testPatientResponse.setId(testPatientId.toString());
        testPatientResponse.setName("John Doe");
        testPatientResponse.setEmail("john.doe@example.com");
        testPatientResponse.setAddress("123 Main St");
        testPatientResponse.setDateOfBirth("1990-01-01");

        testPatientRequest = new PatientRequestDTO();
        testPatientRequest.setName("John Doe");
        testPatientRequest.setEmail("john.doe@example.com");
        testPatientRequest.setAddress("123 Main St");
        testPatientRequest.setDateOfBirth("1990-01-01");
        testPatientRequest.setRegisteredDate("2025-05-31");
    }

    @Test
    void testGetPatients_ReturnsPatientList() throws Exception {
        // Given
        List<PatientResponseDTO> patients = Arrays.asList(testPatientResponse);
        when(patientService.getPatients()).thenReturn(patients);

        // When & Then
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testPatientId.toString()))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));

        verify(patientService).getPatients();
    }

    @Test
    void testCreatePatient_ValidRequest_ReturnsCreatedPatient() throws Exception {
        // Given
        when(patientService.createPatient(any(PatientRequestDTO.class)))
                .thenReturn(testPatientResponse);

        // When & Then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatientRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(patientService).createPatient(any(PatientRequestDTO.class));
    }

    @Test
    void testCreatePatient_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        testPatientRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatientRequest)))
                .andExpect(status().isBadRequest());

        verify(patientService, never()).createPatient(any(PatientRequestDTO.class));
    }

    @Test
    void testCreatePatient_MissingName_ReturnsBadRequest() throws Exception {
        // Given
        testPatientRequest.setName(null);

        // When & Then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatientRequest)))
                .andExpect(status().isBadRequest());

        verify(patientService, never()).createPatient(any(PatientRequestDTO.class));
    }

    @Test
    void testUpdatePatient_ExistingPatient_ReturnsUpdatedPatient() throws Exception {
        // Given
        when(patientService.updatePatient(eq(testPatientId), any(PatientRequestDTO.class)))
                .thenReturn(testPatientResponse);

        // When & Then
        mockMvc.perform(put("/patients/{id}", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatientRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(patientService).updatePatient(eq(testPatientId), any(PatientRequestDTO.class));
    }

    @Test
    void testDeletePatient_ExistingPatient_ReturnsNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/patients/{id}", testPatientId))
                .andExpect(status().isNoContent());

        verify(patientService).deletePatient(testPatientId);
    }
} 