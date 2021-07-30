-- liquibase formatted sql
-- changeset deepakpracto:index_creation_for_insurance_remittance_details_table failOnError:false


CREATE INDEX ird_claim_id_idx ON insurance_remittance_details USING btree (claim_id);
CREATE INDEX ird_remittance_id_idx ON insurance_remittance_details USING btree (remittance_id);