-- liquibase formatted sql
-- changeset abhishekv31:package_plan_applicability_tables_migration
CREATE SEQUENCE package_plan_master_seq;

CREATE TABLE package_plan_master(
package_plan_id integer PRIMARY KEY NOT NULL DEFAULT nextval('package_plan_master_seq'),
pack_id integer,
plan_id integer,
status character);

CREATE INDEX ppm_pack_id_idx on package_plan_master(pack_id);

CREATE INDEX ppm_plan_id_idx on package_plan_master(plan_id);

CREATE SEQUENCE mapping_plan_id_seq;

CREATE TABLE mapping_plan_id(
id integer not null default nextval('mapping_plan_id_seq'),
orderable_item_id integer references orderable_item(orderable_item_id),
plan_id integer,
status character);

CREATE INDEX mapping_plan_id_status_idx on mapping_plan_id(status);

CREATE INDEX mapping_plan_id_plan_id_idx on mapping_plan_id(plan_id);

CREATE INDEX mapping_plan_id_orderable_item_id_idx on mapping_plan_id(orderable_item_id);

INSERT INTO package_plan_master select nextval('package_plan_master_seq'),package_id, -1, 'A' from pack_master;

INSERT INTO mapping_plan_id (orderable_item_id, plan_id, status)
            SELECT DISTINCT oi.orderable_item_id, ppm.plan_id, ppm.status 
            FROM orderable_item oi JOIN package_plan_master ppm ON (ppm.pack_id = oi.entity_id::integer and oi.entity IN ('Package','DiagPackage','MultiVisitPackage'));

COMMENT ON TABLE package_plan_master is '{ "type": "Master", "comment": "Association table between packages and plan" }';
COMMENT ON SEQUENCE package_plan_master_seq is '{ "type": "Master", "comment": "Sequence for package_plan_master table" }';
COMMENT ON SEQUENCE mapping_plan_id_seq is '{ "type": "Master", "comment": "Sequence for mapping_plan_id table" }';
COMMENT ON TABLE mapping_plan_id is '{ "type": "Master", "comment": "Table for maintaining package applicablity for orders" }';