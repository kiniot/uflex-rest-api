package com.kiniot.uflex.api.media.domain.model.valueobjects;

/**
 * What a media asset is attached to. Keeping the owner polymorphic (type + id)
 * lets the same context serve every upload scenario without coupling to a
 * specific aggregate, so it can be added without breaking existing contexts.
 * <ul>
 *     <li>{@code PHYSIOTHERAPIST_RECORD}: media attached to a clinical record/note created by a physiotherapist.</li>
 *     <li>{@code PATIENT_EVIDENCE}: image/video uploaded by a patient from the mobile app (exercise evidence).</li>
 *     <li>{@code PROFILE_PHOTO}: avatar for a physiotherapist or patient (replaces the legacy photoUrl string field).</li>
 *     <li>{@code GENERIC}: any other attachment not tied to a known aggregate.</li>
 * </ul>
 */
public enum OwnerType {
    PHYSIOTHERAPIST_RECORD,
    PATIENT_EVIDENCE,
    EXERCISE_VIDEO,
    PROFILE_PHOTO,
    GENERIC
}
