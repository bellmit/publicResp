-- liquibase formatted sql
-- changeset raeshmika:<column-to-identify-appointments-package-group-id>

CREATE sequence unique_appt_package_grp_id
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;

ALTER TABLE scheduler_appointments ADD COLUMN appointment_pack_group_id INTEGER;