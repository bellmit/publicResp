-- liquibase formatted sql
-- changeset shilpanr:claim-history-amount-precision-3-changes context:precision-3
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:3 select after_decimal_digits from generic_preferences
-- validCheckSum: ANY

ALTER TABLE claim_submission_history ALTER COLUMN total_amount TYPE numeric(16,3);
ALTER TABLE claim_submission_history ALTER COLUMN total_claim_amount TYPE numeric(16,3);
ALTER TABLE claim_submission_history ALTER COLUMN total_patient_amount TYPE numeric(16,3);
ALTER TABLE claim_submission_history ALTER COLUMN total_claim_tax TYPE numeric(16,3);

ALTER TABLE claim_activity_history ALTER COLUMN claim_amount TYPE numeric(16,3);
ALTER TABLE claim_activity_history ALTER COLUMN activity_vat TYPE numeric(16,3);
ALTER TABLE claim_activity_history ALTER COLUMN activity_vat_percent TYPE numeric(16,3);

ALTER TABLE claim_activity_history ALTER COLUMN received_amt TYPE numeric(16,3);
ALTER TABLE claim_activity_history ALTER COLUMN recovery_amt TYPE numeric(16,3);

ALTER TABLE claim_submission_history ALTER COLUMN received_amt TYPE numeric(16,3);
ALTER TABLE claim_submission_history ALTER COLUMN recovery_amt TYPE numeric(16,3);
