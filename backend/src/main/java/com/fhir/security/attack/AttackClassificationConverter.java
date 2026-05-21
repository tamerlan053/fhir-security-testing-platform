package com.fhir.security.attack;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Maps removed enum values when reading legacy rows from {@code test_result.classification}.
 */
@Converter(autoApply = true)
public class AttackClassificationConverter implements AttributeConverter<AttackClassification, String> {

    @Override
    public String convertToDatabaseColumn(AttackClassification attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public AttackClassification convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        if ("MISCONFIGURED".equals(dbData)) {
            return AttackClassification.VULNERABLE;
        }
        return AttackClassification.valueOf(dbData);
    }
}
