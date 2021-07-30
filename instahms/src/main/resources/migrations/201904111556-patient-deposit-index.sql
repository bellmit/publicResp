-- liquibase formatted sql
-- changeset mancini2802:patient-deposits-index

CREATE INDEX patient_deposits_realized_idx ON patient_deposits USING btree (realized);