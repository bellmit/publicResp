-- liquibase formatted sql
-- changeset rajendratalekar:patient-policy-idx failOnError:false

create index idx_ppd_patient_policy_id on patient_policy_details(patient_policy_id);
