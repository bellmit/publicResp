-- liquibase formatted sql
-- changeset rajendratalekar:clinical-hospitalization-details-hospitalization-id-index failOnError:false

create index chd_hospitalization_id_idx on clinical_hospitalization_details(hospitalization_id);
