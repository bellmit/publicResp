-- liquibase formatted sql
-- changeset goutham005:NRL-changes.

ALTER TABLE hl7_lab_interfaces ADD COLUMN send_orm character(1) default 'R';

CREATE SEQUENCE hl7_lab_interfaces_seq
START WITH 1
INCREMENT BY 1
NO MAXVALUE
NO MINVALUE;

CREATE SEQUENCE hl7_center_interfaces_seq
START WITH 1
INCREMENT BY 1
NO MAXVALUE
NO MINVALUE;

ALTER TABLE hl7_lab_interfaces DROP CONSTRAINT hl7_lab_interfaces_pkey;
ALTER TABLE hl7_lab_interfaces ADD CONSTRAINT hl7_lab_interfaces_ukey UNIQUE (interface_name);
ALTER TABLE hl7_lab_interfaces ADD COLUMN hl7_lab_interface_id integer NOT NULL DEFAULT (nextval ('hl7_lab_interfaces_seq'));
ALTER TABLE hl7_center_interfaces ADD COLUMN hl7_center_interface_id integer NOT NULL DEFAULT (nextval ('hl7_center_interfaces_seq'));
ALTER TABLE hl7_center_interfaces ADD COLUMN hl7_lab_interface_id integer;
ALTER TABLE hl7_lab_interfaces ADD CONSTRAINT hl7_lab_interface_id_pkey PRIMARY KEY (hl7_lab_interface_id);
ALTER TABLE hl7_center_interfaces DROP CONSTRAINT center_id_interface_name_composite_pk;
ALTER TABLE hl7_center_interfaces ADD CONSTRAINT hl7_center_id_interface_id_ckey PRIMARY KEY (center_id, hl7_center_interface_id);

UPDATE hl7_center_interfaces hci SET hl7_lab_interface_id = hli.hl7_lab_interface_id
FROM hl7_lab_interfaces hli
WHERE hli.interface_name = hci.interface_name;
ALTER TABLE hl7_center_interfaces ALTER COLUMN hl7_lab_interface_id SET NOT NULL;
ALTER TABLE hl7_center_interfaces ADD CONSTRAINT hl7_lab_interface_id_fkey FOREIGN KEY (hl7_lab_interface_id) REFERENCES hl7_lab_interfaces (hl7_lab_interface_id);
ALTER TABLE hl7_lab_interfaces ADD COLUMN result_parameter_source character(1) DEFAULT 'M';
COMMENT ON COLUMN hl7_lab_interfaces.result_parameter_source is 'M - From master, H - From HL7 Response (ORU message)';

ALTER TABLE diagnostics_export_interface DROP CONSTRAINT diagnostics_export_interface_pk;
ALTER TABLE diagnostics_export_interface ADD COLUMN hl7_lab_interface_id integer;
UPDATE diagnostics_export_interface dei SET hl7_lab_interface_id = hli.hl7_lab_interface_id 
FROM hl7_lab_interfaces hli
WHERE hli.interface_name = dei.interface_name;
ALTER TABLE diagnostics_export_interface ADD CONSTRAINT diagnostics_export_interface_pk PRIMARY KEY (test_id, interface_name, item_type);
CREATE index diagnostics_export_interface_interface_id_idx ON diagnostics_export_interface(hl7_lab_interface_id); 

ALTER TABLE hl7_export_items ADD COLUMN hl7_lab_interface_id integer;
UPDATE hl7_export_items hei SET hl7_lab_interface_id = hli.hl7_lab_interface_id 
FROM hl7_lab_interfaces hli WHERE hei.interface_name = hli.interface_name;

ALTER TABLE outhouse_master ADD COLUMN hl7_lab_interface_id integer;
UPDATE outhouse_master om SET hl7_lab_interface_id = hli.hl7_lab_interface_id
FROM hl7_lab_interfaces hli WHERE hli.interface_name = om.hl7_interface;

INSERT INTO test_format (format_name, testformat_id, format_description, report_file) VALUES ('External Report', 'FORMAT_EXTERNAL', 'External Report', '<p style="text-align: center;"><strong>External Report</strong></p>');
