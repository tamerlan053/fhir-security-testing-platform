package com.fhir.security.mapper;

import com.fhir.security.dto.request.CreatePatientRequest;
import com.fhir.security.dto.response.PatientDto;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

import java.util.List;

public class PatientMapper {

    public static PatientDto toDto(Patient patient) {
        String name = patient.getName().isEmpty()
                ? null
                : patient.getNameFirstRep().getNameAsSingleString();
        String birthDate = patient.getBirthDate() != null
                ? patient.getBirthDateElement().getValueAsString()
                : null;
        String gender = patient.getGender() != null
                ? patient.getGender().getDisplay()
                : null;
        return new PatientDto(
                patient.getIdElement().getIdPart(),
                name,
                birthDate,
                gender
        );
    }

    public static Patient fromCreateRequest(CreatePatientRequest request) {
        Patient patient = new Patient();
        patient.addName()
                .setGiven(request.givenName() != null ? List.of(new StringType(request.givenName())) : List.of(new StringType("Unknown")))
                .setFamily(request.familyName() != null ? request.familyName() : "Unknown");
        if (request.birthDate() != null && !request.birthDate().isBlank()) {
            patient.setBirthDateElement(new DateType(request.birthDate()));
        }
        if (request.gender() != null && !request.gender().isBlank()) {
            switch (request.gender().toLowerCase()) {
                case "male" -> patient.setGender(Enumerations.AdministrativeGender.MALE);
                case "female" -> patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                case "other" -> patient.setGender(Enumerations.AdministrativeGender.OTHER);
                case "unknown" -> patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
                default -> {}
            }
        }
        return patient;
    }
}
