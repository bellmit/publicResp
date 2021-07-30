-- liquibase formatted sql
-- changeset asif:<creating-indexes-fro-registrationUtils-optimization>

create index patient_registration_discharge_type_id_idx on patient_registration(discharge_type_id);

create index patient_details_patient_group_idx on patient_details(patient_group);
