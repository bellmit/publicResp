-- liquibase formatted sql
-- changeset deepakpracto:index_creation_for_insurance_remittance_activity_details_table failOnError:false


CREATE INDEX irac_activity_id_idx ON insurance_remittance_activity_details USING btree (activity_id);
CREATE INDEX irac_claim_id_idx ON insurance_remittance_activity_details USING btree (claim_id);
CREATE INDEX irac_remittance_id_idx ON insurance_remittance_activity_details USING btree (remittance_id);