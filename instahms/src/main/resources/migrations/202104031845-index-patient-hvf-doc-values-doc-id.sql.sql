-- liquibase formatted sql
-- changeset rajendratalekar:index-patient-hvf-doc-values-doc-id failOnError:false

create index patient_hvf_doc_values_doc_id_idx on patient_hvf_doc_values(doc_id);
