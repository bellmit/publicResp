-- liquibase formatted sql
-- changeset manjular:update-approved-qty-rem-approved-qty-in-preauth-prescription-activities

UPDATE preauth_prescription_activities SET approved_qty=act_qty,rem_approved_qty=rem_qty WHERE preauth_act_status='C';

