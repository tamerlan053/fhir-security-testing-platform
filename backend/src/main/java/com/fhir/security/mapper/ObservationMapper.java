package com.fhir.security.mapper;

import com.fhir.security.dto.ObservationDto;
import org.hl7.fhir.r4.model.*;


public class ObservationMapper {
    public static ObservationDto toDto(Observation obs) {
        String patiendId = null;
        if (obs.hasSubject() && obs.getSubject().hasReference()) {
            String ref = obs.getSubject().getReference();
            patiendId = ref.contains("/") ? ref.substring(ref.lastIndexOf("/") + 1) : ref;
        }

        String code = null;
        String display = null;
        if (obs.hasCode() && obs.getCode().hasCoding()) {
            var coding = obs.getCode().getCodingFirstRep();
            code = coding.getCode();
            display = coding.getDisplay();
        }

        String value = null;
        String unit = null;
        if (obs.hasValueQuantity()) {
            Quantity q = obs.getValueQuantity();
            value = q.getValue() != null ? q.getValue().toString() : null;
            unit = q.getUnit();
        } else if (obs.hasValueStringType()) {
            value = obs.getValueStringType().getValue();
        } else if (obs.hasValueCodeableConcept()) {
            value = obs.getValueCodeableConcept().getText();
        }

        String effectiveDateTime = obs.hasEffectiveDateTimeType() ? obs.getEffectiveDateTimeType().getValueAsString() : null;

        String status = obs.hasStatus() ? obs.getStatus().getDisplay() : null;

        return new ObservationDto(
                obs.getIdElement().getIdPart(),
                patiendId,
                code,
                display,
                value,
                unit,
                effectiveDateTime,
                status
        );
    }
}
