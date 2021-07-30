-- liquibase formatted sql
-- changeset goutham005:type-cast-index-on-consultation-id

create index doctor_consultation_consultation_id_text_idx on doctor_consultation(CAST(consultation_id as text));
