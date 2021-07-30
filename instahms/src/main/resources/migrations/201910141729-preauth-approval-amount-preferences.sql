-- liquibase formatted sql
-- changeset sandeep:preauth-approval-preferences

ALTER TABLE generic_preferences ADD COLUMN set_preauth_approved_amt_as_claim_amt VARCHAR(1) DEFAULT 'N';
ALTER TABLE generic_preferences ADD COLUMN update_claim_of_ordered_item_on_preauth_approval VARCHAR(1) DEFAULT 'N';
