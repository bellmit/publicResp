-- liquibase formatted sql
-- changeset kartikag01:add-baby-of-in-salutation_master

INSERT INTO salutation_master (salutation_id, salutation, status, gender) VALUES ('SALU0000', 'B/O', 'A', NULL);