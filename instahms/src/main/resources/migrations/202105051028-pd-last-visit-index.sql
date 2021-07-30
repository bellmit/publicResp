-- liquibase formatted sql
-- changeset rajendratalekar:patient-details-last-visit-index failOnError:false

create index pd_last_visit_idx on patient_details(COALESCE(visit_id,previous_visit_id));