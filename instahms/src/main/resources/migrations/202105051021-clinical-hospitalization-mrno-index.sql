-- liquibase formatted sql
-- changeset rajendratalekar:clinical-hospitalization-mrno-index

create index clinical_hospitalization_mrno_idx on clinical_hospitalization(mr_no);
