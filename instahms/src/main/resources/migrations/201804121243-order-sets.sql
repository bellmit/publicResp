-- liquibase formatted sql
-- changeset kchetan:order-sets-migrations splitStatements:false


---------------------- ORDER SETS ------------------------

ALTER TABLE package_contents ADD COLUMN consultation_type_id integer, ADD COLUMN charge_head varchar(20);

ALTER TABLE packages ALTER COLUMN package_name TYPE varchar(100);

CREATE SEQUENCE patient_package_seq;

DROP TABLE IF EXISTS patient_package;
CREATE TABLE patient_package(
	patient_package_id 		integer DEFAULT nextval('patient_package_seq'::regclass) PRIMARY KEY NOT NULL,
	package_id 				integer NOT NULL,
	mr_no					varchar(15),
	status					char
);

select setval('patient_package_seq', last_value) from patient_packages_seq;

CREATE SEQUENCE patient_package_contents_seq;

DROP TABLE IF EXISTS patient_package_contents;
CREATE TABLE patient_package_contents(
	patient_package_content_id      integer DEFAULT nextval('patient_package_contents_seq'::regclass) PRIMARY KEY NOT NULL,
	patient_package_id              integer NOT NULL,
	package_content_id              integer,
	package_id                      integer NOT NULL,
	activity_id                     varchar(200),
	activity_type                   varchar(50),
	activity_qty                    integer,
	charge_head                     varchar(20),
	consultation_type_id            integer,
	created_by                      varchar(50),
	created_at                      timestamp without time zone DEFAULT NOW(),
	modified_by                     varchar(50),
	modified_at                     timestamp without time zone
);

CREATE SEQUENCE patient_package_content_consumed_seq;

DROP TABLE IF EXISTS patient_package_content_consumed;
CREATE TABLE patient_package_content_consumed(
	patient_package_consumed_id      integer DEFAULT nextval('patient_package_content_consumed_seq'::regclass) PRIMARY KEY NOT NULL,
	patient_package_content_id       integer NOT NULL,
	quantity                         integer NOT NULL,
	prescription_id                  integer,
	bill_charge_id                   varchar(15),
	item_type                        varchar(20)
);

INSERT INTO saved_searches values('Order Sets',  nextval('saved_searches_seq'), 'System', 'Active Order Sets', true,
'status=A', 'InstaAdmin', 'InstaAdmin', now(), now());

INSERT INTO saved_searches values('Order Sets',  nextval('saved_searches_seq'), 'System', 'Inactive Order Sets', false,
'status=I', 'InstaAdmin', 'InstaAdmin', now(), now());

INSERT INTO packages (package_id, package_name, package_code, description , type, service_sub_group_id, valid_from, valid_till,status,modified_at, modified_by, created_at, created_by, visit_applicability, gender_applicability) 
select package_id, package_name, package_code, description, 'O' as type, service_sub_group_id, valid_from_date, valid_to_date,package_active as status, now() as modified_at, approval_process_by as modified_by, now() as created_at, 'InstaAdmin' as created_by,
CASE WHEN package_type='o' THEN 'o'
	 WHEN package_type='i' THEN 'i'
	 WHEN package_type='d' THEN 'o'
	 END as visit_applicability, '*' as gender_applicability
 FROM pack_master WHERE type = 'T';

SELECT setval('packages_seq',last_value) from package_sequence;

------ FOR PACKAGE_COMPONENTDETAILS ---------------------

INSERT INTO package_contents (package_content_id,package_id,activity_id,activity_type,activity_qty, activity_qty_uom,activity_remarks, display_order,modified_by,modified_at,created_by,created_at, consultation_type_id, charge_head)
SELECT pcd.pack_ob_id::int as package_content_id, pcd.package_id,pcd.activity_id,pcd.activity_type,pcd.activity_qty,pcd.activity_units as activity_qty_uom ,pcd.activity_remarks, pcd.display_order,pm.approval_process_by as modified_by,now() as modified_at,'InstaAdmin' as created_by,now() as created_at, pcd.consultation_type_id,pcd.charge_head
FROM package_componentdetail pcd LEFT JOIN pack_master pm using (package_id) where pm.type='T';

select setval('package_contents_seq', last_value) from pack_chid_sequence;

------ FOR PACKAGE_CENTER_MASTER --------------------------

INSERT INTO center_package_applicability (center_package_id, package_id, center_id, modified_at, modified_by, created_at, created_by)
 select pcm.package_center_id as center_package_id, pcm.pack_id as package_id, pcm.center_id, now() as modified_at, 'InstaAdmin' as modified_by, now() as created_at, 'InstaAdmin' as created_by
 FROM pack_master pm JOIN package_center_master pcm  ON ( pcm.pack_id = pm.package_id) WHERE pcm.status = 'A' and pm.type='T';

select setval('center_package_applicability_seq', last_value ) from package_center_master_seq;

------ FOR DEPT_PACKAGE_APPLICABILITY

INSERT INTO dept_package_applicability (package_id, dept_id, modified_at, modified_by, created_at, created_by)
 select package_id, '*' as dept_id, now() as modified_at, 'InstaAdmin' as modified_by, now() as created_at, 'InstaAdmin' as created_by
 FROM pack_master pm WHERE pm.type='T';
 
 
--------- old data needs to be migrated as the entity for them is packages . To be done before deleting from pack_master.

CREATE FUNCTION migrate_ordersets() RETURNS void as $$
BEGIN
    UPDATE orderable_item SET entity = 'Order Sets'
        FROM pack_master p
        WHERE entity_id = p.package_id::text AND p.type = 'T';
END;
$$
LANGUAGE plpgsql;

SELECT migrate_ordersets();
DROP FUNCTION migrate_ordersets();

----------- DELETING TEMPLATE PACKAGE DATA .

DELETE FROM package_componentdetail pcd USING pack_master pm  where pm.type='T' AND pm.package_id = pcd.package_id;
 
DELETE FROM package_center_master pcm USING pack_master pm WHERE pm.type = 'T' AND pcm.pack_id = pm.package_id;
 
DELETE FROM pack_master WHERE type = 'T';
 
-- need to remove once all the packages are migrated.
CREATE FUNCTION order_sets_orderable_item_procedure() RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT' and NEW.type = 'O') THEN
       INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT NEW.package_id::text as entity_id, 'Order Sets' as entity, lower(NEW.package_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, NEW.service_sub_group_id, NEW.visit_applicability as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Package') as direct_billing,
        ssg.service_group_id as service_group_id, NEW.package_code as item_codes,
        NEW.status as status
        FROM packages p
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE p.package_id = NEW.package_id;
    
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.package_name), service_sub_group_id = NEW.service_sub_group_id, service_group_id = ssg.service_group_id,
        item_codes = NEW.package_code, status = NEW.status, visit_type = NEW.visit_applicability
        FROM service_sub_groups ssg
        WHERE ssg.service_sub_group_id = NEW.service_sub_group_id AND entity_id = NEW.package_id::text;
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER order_sets_orderable_item_trigger
    AFTER INSERT OR UPDATE ON packages
    FOR EACH ROW
    EXECUTE PROCEDURE order_sets_orderable_item_procedure();


-- migrating departments to orderable_item 
CREATE FUNCTION migrate_departments() RETURNS void as $$
BEGIN
    INSERT INTO orderable_item(entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, visit_type, status) 
        SELECT DISTINCT d.dept_id as entity_id, 'Department' as entity, lower(d.dept_name) as item_name,
        'mod_basic' as module_id, 'Y' as orderable,'Y' as operation_applicable,
        'Y' as package_applicable, 'Y' as is_multi_visit_package, '*' as visit_type, d.status as status
        FROM department d;

END;
$$
LANGUAGE plpgsql;

SELECT migrate_departments();
DROP FUNCTION migrate_departments();


CREATE FUNCTION department_orderable_item_procedure() RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN

        INSERT INTO orderable_item(entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, visit_type, status) 
            SELECT DISTINCT d.dept_id as entity_id, 'Department' as entity, lower(d.dept_name) as item_name,
            'mod_basic' as module_id, 'Y' as orderable,'Y' as operation_applicable,
            'Y' as package_applicable, 'Y' as is_multi_visit_package, '*' as visit_type, d.status as status
            FROM department d
            WHERE d.dept_id = NEW.dept_id;
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.dept_name), status = NEW.status
        WHERE entity_id = NEW.dept_id;
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER department_orderable_item_trigger
    AFTER INSERT OR UPDATE ON department
    FOR EACH ROW
    EXECUTE PROCEDURE department_orderable_item_procedure();
    
insert into url_action_rights (select role_id, 'mas_order_sets', rights from url_action_rights where action_id = 'mas_packages');

insert into action_rights (select role_id, 'mas_order_sets', rights from action_rights where action = 'mas_packages');

insert into screen_rights (select role_id, 'mas_order_sets', rights from screen_rights where screen_id = 'mas_packages');


---------------------- ORDER SETS END ------------------------
