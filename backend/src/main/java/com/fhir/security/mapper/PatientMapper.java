package com.fhir.security.mapper;

import com.fhir.security.dto.PatientDto;
import org.hl7.fhir.r4.model.Patient;

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
}
