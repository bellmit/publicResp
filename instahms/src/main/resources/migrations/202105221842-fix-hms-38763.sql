-- liquibase formatted sql
-- changeset ssshreyans26:fix-hms-38763:Adding-flag-activity_timing_eclaim-in-services-table  

ALTER TABLE services ADD COLUMN activity_timing_eclaim BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN services.activity_timing_eclaim IS 'This attribute will decide whether service conduction or billing timing to be reported in the e-claim. True - Based on coduction Time, False - Based On Billing Time';
