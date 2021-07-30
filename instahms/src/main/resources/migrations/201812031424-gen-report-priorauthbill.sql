-- liquibase formatted sql
-- changeset mohamedanees:commit-LinkingPriorAuthToBilling

ALTER TABLE preauth_prescription_activities ADD COLUMN rem_qty integer not null DEFAULT -1;
UPDATE preauth_prescription_activities set rem_qty=act_qty;
ALTER TABLE preauth_prescription_activities ALTER rem_qty DROP DEFAULT;
ALTER TABLE bill_charge ADD COLUMN preauth_act_id INTEGER;


