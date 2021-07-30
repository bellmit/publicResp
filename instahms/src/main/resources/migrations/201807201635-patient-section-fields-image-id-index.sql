-- liquibase formatted sql
-- changeset tejakilaru:patient-section-fields-image-id-index

create index patient_section_fields_image_id_idx on patient_section_fields(image_id);
