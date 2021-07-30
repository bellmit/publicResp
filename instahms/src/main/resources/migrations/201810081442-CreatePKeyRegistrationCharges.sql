-- liquibase formatted sql
-- changeset utkarshjindal:creating-primary-key-on-registration-charges

DELETE FROM registration_charges a
WHERE a.ctid <> (SELECT min(b.ctid) FROM   registration_charges b WHERE  a.org_id = b.org_id AND a.bed_type = b.bed_type);

ALTER TABLE registration_charges ADD CONSTRAINT registration_charges_pkey PRIMARY KEY (org_id, bed_type);
