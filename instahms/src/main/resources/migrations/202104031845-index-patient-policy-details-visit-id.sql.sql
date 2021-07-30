-- liquibase formatted sql
-- changeset rajendratalekar:index-patient-policy-details-visit-id failOnError:false

create index patient_policy_details_visit_id_idx on patient_policy_details(visit_id);
