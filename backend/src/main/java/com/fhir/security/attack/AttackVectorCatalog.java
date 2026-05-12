package com.fhir.security.attack;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Stable, comma-separated tags for aggregation (Week 10). One primary line per executable scenario.
 */
public final class AttackVectorCatalog {

    private AttackVectorCatalog() {}

    public static String tagsFor(Class<? extends ExecutableAttack> attackClass) {
        if (attackClass == null) {
            return "";
        }
        String name = attackClass.getSimpleName();
        return switch (name) {
            case "MalformedJsonRequestAttack" -> "http.post.patient,json.syntax_invalid,json.truncation";
            case "MetadataManipulationAttack" -> "http.post.patient,fhir.meta_semantics,fhir.resourceType,fhir.id_identifier";
            case "UnexpectedPayloadInjectionAttack" -> "http.post.patient,json.unknown_fields,json.duplicate_keys,json.extra_nested";
            case "ExtensionFieldsMisuseAttack" -> "http.post.patient,fhir.extension.covert_channel,fhir.extension.valueString";
            case "ContainedResourceSmugglingAttack" -> "http.post.patient,fhir.contained.binary,fhir.base64_payload";
            case "EncodedHiddenDataAttack" -> "http.post.patient,fhir.meta.tag,unicode.normalization_display";
            case "InvalidCredentialsAccessAttack" -> "http.get.patient,http.get.observation,auth.basic_invalid,auth.bearer_forged,auth.oauth_token_endpoint";
            case "OpenEndpointDetectionAttack" -> "http.get.metadata,http.get.smart_well_known,http.get.patient_anonymous";
            case "CrossPatientAccessAttack" -> "http.get.patient_by_id,http.post.observation_subject,http.get.observation_search,idor.cross_patient";
            case "AuthenticatedTokenIsolationAttack" -> "auth.bearer_lab_token,http.get.patient_out_of_scope,auth.token_isolation";
            case "ObservationBundleDuplicateClinicalAttack" -> "fhir.bundle.transaction,http.post.observation_batch,fhir.observation.duplicate_clinical";
            case "UnauthorizedWriteIdTamperingAttack" -> "http.put.patient,http.post.observation,http.get.observation,auth.anonymous_write";
            default -> "http.fhir_probe";
        };
    }

    public static String mergeIds(String a, String b) {
        Set<String> out = new LinkedHashSet<>();
        splitInto(a, out);
        splitInto(b, out);
        return String.join(",", out);
    }

    private static void splitInto(String csv, Set<String> sink) {
        if (csv == null || csv.isBlank()) {
            return;
        }
        for (String part : csv.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                sink.add(t);
            }
        }
    }
}
