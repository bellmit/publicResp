-- liquibase formatted sql
-- changeset pranays:pbm-prescription-flow-missing-indexes failOnError:false

DROP INDEX IF EXISTS pbm_medicine_prescriptions_op_medicine_pres_id_idx;
CREATE INDEX pbm_medicine_prescriptions_op_medicine_pres_id_idx ON pbm_medicine_prescriptions(op_medicine_pres_id);

DROP INDEX IF EXISTS idx_patient_prescription_presc_type;
CREATE INDEX idx_patient_prescription_presc_type ON patient_prescription USING btree (presc_type, store_item);

DROP INDEX IF EXISTS pbm_presc_observations_pbm_medicine_pres_id_obs_id_idx;
CREATE INDEX pbm_presc_observations_pbm_medicine_pres_id_obs_id_idx ON pbm_presc_observations(pbm_medicine_pres_id,obs_id);
