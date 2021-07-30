-- liquibase formatted sql
-- changeset sandeep:adding-preauth-limit-to-insurance-plan

ALTER TABLE insurance_plan_main ADD COLUMN enable_pre_authorized_limit VARCHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE insurance_plan_main ADD COLUMN op_pre_authorized_amount DECIMAL(15,2) NOT NULL DEFAULT 0;
ALTER TABLE insurance_plan_main ADD COLUMN excluded_charge_groups VARCHAR(50);
COMMENT ON COLUMN insurance_plan_main.excluded_charge_groups IS 'Comma seperated values of charge groups excluded when checking is the claim amount has exceeded op_pre_authorized_amount';