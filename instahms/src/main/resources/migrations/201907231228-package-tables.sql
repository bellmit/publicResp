-- liquibase formatted sql
-- changeset mohamedanees:packages-table-changes splitStatements:false

--ALTER TABLE packages ADD COLUMN package_category_id INTEGER DEFAULT(-1);
ALTER TABLE packages ADD COLUMN allow_discount BOOLEAN DEFAULT FALSE;
ALTER TABLE packages ADD COLUMN prior_auth_required CHARACTER(1) DEFAULT 'N';
ALTER TABLE packages ADD COLUMN allow_rate_increase BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE packages ADD COLUMN allow_rate_decrease BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE packages ADD COLUMN billing_group_id INTEGER;
ALTER TABLE packages ADD COLUMN handover_to CHARACTER(1) DEFAULT 'P';
ALTER TABLE packages ADD COLUMN allow_customization BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE packages ADD COLUMN allowed_loss INTEGER NOT NULL DEFAULT 0;
ALTER TABLE packages ADD COLUMN bill_display_type CHARACTER(1) DEFAULT 'P';
ALTER TABLE packages ADD COLUMN submission_batch_type CHARACTER(1) DEFAULT 'P';
ALTER TABLE packages ADD COLUMN approval_status CHARACTER(1); 
ALTER TABLE packages ADD COLUMN multi_visit_package BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE packages ADD COLUMN approval_remarks CHARACTER VARYING(2000);
ALTER TABLE packages ADD COLUMN approval_status_time TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE packages ADD COLUMN conditions  CHARACTER VARYING(2000);
ALTER TABLE packages ADD COLUMN pre_requisites  CHARACTER VARYING(2000);
ALTER TABLE packages ADD COLUMN min_age  INTEGER;
ALTER TABLE packages ADD COLUMN max_age  INTEGER;
ALTER TABLE packages ADD COLUMN age_unit  CHARACTER(1) DEFAULT 'Y';
ALTER TABLE packages ADD COLUMN chanelling CHARACTER(1) DEFAULT 'N';
ALTER TABLE packages ADD COLUMN approval_processed_by CHARACTER VARYING(30);
ALTER TABLE packages ADD COLUMN insurance_category_id INTEGER;
ALTER TABLE packages ADD COLUMN order_code CHARACTER VARYING(50);
 
ALTER TABLE package_contents ADD COLUMN content_id_ref INTEGER;
ALTER TABLE package_contents ADD COLUMN operation_id CHARACTER VARYING(10) REFERENCES operation_master(op_id);
ALTER TABLE package_contents ADD COLUMN bed_id INTEGER REFERENCES bed_names(bed_id);
ALTER TABLE package_contents ADD COLUMN panel_id INTEGER REFERENCES packages(package_id);
ALTER TABLE package_contents ADD COLUMN visit_qty_limit INTEGER;

-- Temp Column for migration
ALTER TABLE package_contents ADD COLUMN activity_charge NUMERIC(15,2);

ALTER TABLE package_charges ADD COLUMN modified_by character varying(50);
ALTER TABLE package_charges ADD COLUMN modified_at timestamp without time zone;
ALTER TABLE package_charges ADD COLUMN created_by character varying(50);
ALTER TABLE package_charges ADD COLUMN created_at timestamp without time zone;
ALTER TABLE center_package_applicability ADD COLUMN status character varying(1) DEFAULT 'A';

--CREATE TABLE package_item_sub_groups_obsolete (
--    package_id INTEGER ,
--    item_subgroup_id INTEGER
--);

--CREATE TABLE packages_insurance_category_mapping_obsolete (
--    package_id INTEGER ,
--    insurance_category_id INTEGER
--);

--CREATE TABLE package_center_master_obsolete (
--	package_center_id INTEGER ,
--    pack_id INTEGER,
--	center_id INTEGER,
--    status CHARACTER VARYING(1)
--);

--CREATE TABLE package_sponsor_master_obsolete (
--	package_sponsor_id INTEGER ,
--    pack_id INTEGER,
--	tpa_id character varying(15),
--    status CHARACTER VARYING(1)
--);


--CREATE TABLE dept_package_applicability_obsolete (
--    dept_package_id INTEGER,
--	package_id INTEGER,
--    dept_id character varying(10),
--    created_by character varying(50),
--    created_at timestamp without time zone,
--    modified_by character varying(50),
--    modified_at timestamp without time zone
--);

CREATE SEQUENCE package_content_charges_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE temp_package_content_charges_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
    
CREATE TEMP TABLE temp_package_content_charges (
    content_charge_id INTEGER DEFAULT nextval('temp_package_content_charges_seq'::regclass),
    package_content_id INTEGER,
    org_id character varying(15),
    bed_type character varying(50),
    charge NUMERIC(15,2),
	discount NUMERIC(15,2),
    is_override CHARACTER VARYING(1),
	package_id INTEGER,
	package_charge NUMERIC(15,2),
	modified_by character varying(50),
    modified_at timestamp without time zone,
    created_by character varying(50),
    created_at timestamp without time zone,
    consultation_type_id INTEGER
);

CREATE TEMP TABLE temp_charge_calc_table (
	content_charge_id INTEGER PRIMARY KEY,
	charge NUMERIC
);

CREATE TABLE package_content_charges (
    content_charge_id INTEGER DEFAULT nextval('package_content_charges_seq'::regclass),
    package_content_id INTEGER,
    org_id character varying(15),
    bed_type character varying(50),
    charge NUMERIC(15,2),
	discount NUMERIC(15,2),
    is_override CHARACTER VARYING(1),
	package_id INTEGER,
	package_charge NUMERIC(15,2),
	modified_by character varying(50),
    modified_at timestamp without time zone,
    created_by character varying(50),
    created_at timestamp without time zone DEFAULT now()
);
--
--CREATE SEQUENCE insurance_plan_package_applicability_seq
--    START WITH 1
--    INCREMENT BY 1
--    NO MINVALUE
--    NO MAXVALUE
--    CACHE 1;
--
--CREATE TABLE insurance_plan_package_applicability (
--    insurance_plan_package_id INTEGER DEFAULT nextval('insurance_plan_package_applicability_seq'::regclass) NOT NULL PRIMARY KEY,
--    package_id INTEGER,
--    plan_id INTEGER,
--    insurance_co_id character varying(10),
--    plan_type_id INTEGER,
--    modified_by character varying(50),
--    modified_at timestamp without time zone,
--    created_by character varying(50),
--    created_at timestamp without time zone DEFAULT now()
--);

--This table contains pack_master_id and packages id that was inserted during this migration. 
--To be dropped in future as part of CIG after stable release.
--To be removed by: 12.3.x
CREATE TABLE temp_pack_master_packages_mapping (
	pack_master_id INTEGER ,
	package_id INTEGER
);

--This table contains mapping between package_componentdetail(pack_ob_id) and package_contents(package_content_id)
--with the UPDATED packages(package_id)
--To be dropped in future as part of CIG 12.3.x after stable release
CREATE TABLE temp_package_component_id_mapping(
	package_id INTEGER,
	pack_ob_id CHARACTER VARYING(15),
	package_content_id INTEGER

);

--This table contains mapping between package_id in 
--package_content_charges(temp column) and associated charges for calculating the proportions later
--with the UPDATED packages(package_id)
--To be dropped in future as part of CIG 12.3.x after stable release
CREATE TABLE temp_package_charge_mapping (
	package_id INTEGER,
	item_count INTEGER,
	total_calculated_package_charge NUMERIC(15,2),
	org_id character varying(15) ,
    bed_type character varying(50)
);


--Enforce all packages name to have Unique names(Existing Unique Constraint)
CREATE OR REPLACE FUNCTION update_package_names_unique() RETURNS boolean AS $BODY$
	DECLARE 
		packagesEntry record;
		packageCount INTEGER;
		packageToUpdate record;
	BEGIN
		FOR packagesEntry IN (SELECT * FROM 
			(SELECT count(*) as package_count,package_name from packages GROUP BY package_name)
			AS packCountMap where packCountMap.package_count>1) LOOP
			packageCount = packagesEntry.package_count;
			FOR packageToUpdate IN
				(SELECT package_name,package_id from packages where package_name=packagesEntry.package_name)
				LOOP
					UPDATE packages SET package_name=package_name||(' ')||packageCount WHERE package_id=packageToUpdate.package_id;
					packageCount = packageCount-1;
				END LOOP;	
		END LOOP;

		ALTER TABLE ONLY packages ADD CONSTRAINT packages_package_name_key UNIQUE (package_name);
	return true;
	END; 
$BODY$ 
LANGUAGE plpgsql;


--migrate pack_master to packages
CREATE OR REPLACE FUNCTION migrate_pack_master() RETURNS boolean as $BODY$
DECLARE
	packMasterEntry record;
	packageSeqId integer;
BEGIN
	ALTER TABLE packages DROP CONSTRAINT packages_package_name_key;
	FOR packMasterEntry IN (select * from pack_master where type='P') LOOP
	packageSeqId := nextval('packages_seq');
	
	INSERT INTO 
		temp_pack_master_packages_mapping(pack_master_id,package_id)
		VALUES
		(packMasterEntry.package_id,packageSeqId);
	
	INSERT INTO packages(package_id,package_name,package_code,type,status,
							description,visit_applicability,gender_applicability,
							service_sub_group_id,valid_from,valid_till,created_by,
							created_at,modified_by,modified_at,allow_rate_increase,
							package_category_id,allow_discount,prior_auth_required,
							allow_rate_decrease,billing_group_id,handover_to,
							allow_customization,allowed_loss,bill_display_type,
							submission_batch_type,approval_status,
							multi_visit_package,approval_remarks,age_unit,min_age,
							max_age,approval_status_time,conditions,
							pre_requisites,chanelling,approval_processed_by,
							insurance_category_id)
    SELECT  packageSeqId as package_id, 
    		packMasterEntry.package_name as package_name,
    		packMasterEntry.package_code as package_code,
              'P' AS type,
    		packMasterEntry.package_active as status,
    		packMasterEntry.description as description,
    		CASE 
    			WHEN packMasterEntry.package_type='d' THEN '*' 
    			WHEN packMasterEntry.package_type='i' THEN 'i'
				ELSE 'o' 
			END AS visit_applicability,
			'*' as gender_applicability,
			packMasterEntry.service_sub_group_id as service_sub_group_id,
			packMasterEntry.valid_from_date as valid_from,
            packMasterEntry.valid_to_date as valid_till,
            'InstaAdmin' as created_by,
			now() as created_at,
			NULL as modified_by,
			NULL as modified_at,
			packMasterEntry.allow_rate_increase as allow_rate_increase,
			CASE 
				WHEN packMasterEntry.package_type='d' THEN -3
				ELSE packMasterEntry.package_category_id 
				END
			AS package_category_id,
			packMasterEntry.allow_discount as allow_discount,
			packMasterEntry.prior_auth_required as prior_auth_required,
			packMasterEntry.allow_rate_decrease as allow_rate_decrease,
			packMasterEntry.billing_group_id as billing_group_id,
			packMasterEntry.handover_to as handover_to,
			false as allow_customization,
			0 as allowed_loss,
			'P' as bill_display_type,
			'P' as submission_batch_type,
			packMasterEntry.approval_status as approval_status,
			packMasterEntry.multi_visit_package as multi_visit_package,
			packMasterEntry.approval_remarks as approval_remarks,
			'Y' as age_unit,
			NULL as min_age,
			NULL as max_age,
			packMasterEntry.approval_status_time as approval_status_time,
			NULL as conditions,
			NULL as pre_requisites,
			packMasterEntry.chanelling as chanelling,
			packMasterEntry.approval_process_by as approval_processed_by,
			packMasterEntry.insurance_category_id as insurance_category_id;
	END LOOP;
	PERFORM update_package_names_unique();
	return true;
END;
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_update_package_orderable_item() RETURNS boolean AS $BODY$
	BEGIN
--		ALTER TABLE orderable_item DISABLE TRIGGER orderable_items_token_trigger;
		
		UPDATE orderable_item 
			SET entity_id=packageIdMapping.package_id::text 
			FROM (
			select package_id,pack_master_id from temp_pack_master_packages_mapping
			) as packageIdMapping
			WHERE entity_id=packageIdMapping.pack_master_id::text AND entity IN ('Package', 'MultiVisitPackage', 'Order Sets', 'DiagPackage');
			
	return true;
	END; 
$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_package_plan_master() RETURNS boolean AS $BODY$
	BEGIN
	
		UPDATE package_plan_master 
			SET pack_id=packageIdMapping.package_id 
			FROM (
			select package_id,pack_master_id from temp_pack_master_packages_mapping
			) as packageIdMapping
			WHERE pack_id=packageIdMapping.pack_master_id;
			
	return true;
	END; 
$BODY$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION migrate_package_item_subgroups() RETURNS boolean AS $BODY$
	BEGIN
		
--		INSERT INTO package_item_sub_groups_obsolete(package_id, item_subgroup_id)
--			SELECT package_id, item_subgroup_id FROM package_item_sub_groups;
			
		UPDATE package_item_sub_groups 
			SET package_id=packageIdMapping.package_id
			FROM (
			select package_id,pack_master_id from temp_pack_master_packages_mapping
			) as packageIdMapping
			WHERE package_item_sub_groups.package_id=packageIdMapping.pack_master_id;
		return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_insurance_category_mappings() RETURNS boolean AS $BODY$
	BEGIN
--		INSERT INTO packages_insurance_category_mapping_obsolete(package_id, insurance_category_id)
--			SELECT package_id, insurance_category_id FROM packages_insurance_category_mapping;
			
		UPDATE packages_insurance_category_mapping 
			SET package_id=packageIdMapping.package_id
			FROM (
			select package_id,pack_master_id from temp_pack_master_packages_mapping
			) as packageIdMapping
			WHERE packages_insurance_category_mapping.package_id=packageIdMapping.pack_master_id;
		return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_update_package_charges_pack_id() RETURNS boolean AS $BODY$
	BEGIN	
		
		CREATE TABLE temp_package_charges AS 
			SELECT pmpm.package_id as package_id,
					pc.org_id as org_id,
					pc.bed_type as bed_type,
					pc.charge as charge,
					pc.discount as discount,
					pc.is_override as is_override
			FROM package_charges pc
			JOIN temp_pack_master_packages_mapping pmpm ON (pc.package_id = pmpm.pack_master_id);

		DELETE FROM package_charges where package_id IN (SELECT pack_master_id from temp_pack_master_packages_mapping);

		INSERT INTO package_charges 
			SELECT 
			package_id,
					org_id,
					bed_type,
					charge,
					discount,
					is_override
			FROM temp_package_charges;
			
		DROP TABLE temp_package_charges;
--		UPDATE package_charges 
--			SET package_id = packageIdMapping.package_id
--			FROM (
--			SELECT package_id,pack_master_id FROM temp_pack_master_packages_mapping
--			) as packageIdMapping
--			WHERE package_charges.package_id = packageIdMapping.pack_master_id;
		return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION migrate_dept_package_applicability() RETURNS boolean AS $BODY$
	BEGIN
--		INSERT INTO dept_package_applicability_obsolete
--			(dept_package_id, package_id,dept_id,created_by,
--			created_at,modified_by,modified_at)
--			SELECT dept_package_id, package_id,dept_id,created_by,
--			created_at,modified_by,modified_at FROM dept_package_applicability;
			
		UPDATE dept_package_applicability 
			SET package_id=packageIdMapping.package_id
			FROM (
			select package_id,pack_master_id from temp_pack_master_packages_mapping
			) as packageIdMapping
			WHERE dept_package_applicability.package_id=packageIdMapping.pack_master_id;
		return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_package_center_master_pack_id() RETURNS boolean AS $BODY$
	BEGIN
--		INSERT INTO package_center_master_obsolete(package_center_id,pack_id,center_id,status)
--			SELECT package_center_id,pack_id,center_id,status FROM package_center_master;

		CREATE TABLE temp_package_center_master AS
			SELECT pcm.package_center_id as package_center_id,
					tpmpm.package_id as pack_id,
					pcm.center_id as center_id,
					pcm.status as status
			FROM package_center_master pcm
			JOIN temp_pack_master_packages_mapping tpmpm ON (tpmpm.pack_master_id=pcm.pack_id);

		DELETE from package_center_master where pack_id IN (SELECT pack_master_id FROM temp_pack_master_packages_mapping);

		INSERT INTO package_center_master
			SELECT package_center_id,
					pack_id,
					 center_id,
					status
			FROM temp_package_center_master;

		DROP TABLE temp_package_center_master;
		
--		UPDATE package_center_master 
--			SET pack_id=packageIdMapping.package_id
--			FROM (
--			select package_id,pack_master_id from temp_pack_master_packages_mapping
--			) as packageIdMapping
--			WHERE package_center_master.pack_id=packageIdMapping.pack_master_id;

--		INSERT INTO center_package_applicability(package_id,center_id)
--			SELECT 	pack_id as package_id,
--					center_id as center_id
--			FROM package_center_master where pack_id IS NOT NULL AND center_id IS NOT NULL;

		INSERT INTO center_package_applicability(package_id,center_id)
			SELECT 	pcm.pack_id as package_id,
					pcm.center_id as center_id
			FROM package_center_master pcm
			JOIN temp_pack_master_packages_mapping pmmap ON (pmmap.package_id = pcm.pack_id) 
			WHERE pcm.center_id IS NOT NULL AND pcm.pack_id IS NOT NULL;

		return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION migrate_package_sponsor_master() RETURNS boolean AS $BODY$
	BEGIN
--		INSERT INTO package_sponsor_master_obsolete(package_sponsor_id,pack_id,tpa_id,status)
--			SELECT package_sponsor_id,pack_id,tpa_id,status FROM package_sponsor_master;

		
			
		UPDATE package_sponsor_master 
			SET pack_id=packageIdMapping.package_id
			FROM (
			select package_id,pack_master_id from temp_pack_master_packages_mapping
			) as packageIdMapping
			WHERE package_sponsor_master.pack_id=packageIdMapping.pack_master_id;
		return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_pack_org_details_mapping() RETURNS boolean AS $BODY$
	BEGIN
		CREATE TABLE temp_pack_org_details AS
			SELECT tpmmap.package_id as package_id,
					pod.org_id as org_id,
					pod.applicable as applicable,
					pod.item_code as item_code,
					pod.code_type as code_type,
					pod.base_rate_sheet_id as base_rate_sheet_id,
					pod.is_override as is_override
			FROM pack_org_details pod
			JOIN temp_pack_master_packages_mapping tpmmap ON (pod.package_id = tpmmap.pack_master_id);


		DELETE FROM pack_org_details WHERE package_id in (select pack_master_id from temp_pack_master_packages_mapping);

		INSERT INTO pack_org_details 
			SELECT package_id,
					org_id,
					applicable,
					item_code,
					code_type,
					base_rate_sheet_id,
					is_override 
					FROM temp_pack_org_details;

		DROP TABLE temp_pack_org_details;
		
--		UPDATE pack_org_details 
--			SET package_id = packageIdMapping.package_id
--			FROM (
--			SELECT package_id,pack_master_id FROM temp_pack_master_packages_mapping
--			) as packageIdMapping
--			WHERE pack_org_details.package_id = packageIdMapping.pack_master_id;
		return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_package_components() RETURNS boolean AS $BODY$
	DECLARE
		newPackageContentId integer;
		packageComponentDetailRecord record;
		packageOperationRecord record;
		updated_pack_id integer;
		operationChargeHeads text[] := ARRAY['SUOPE','SACOPE','ANAOPE'];
		operationChargeHead text;
		refOperationPackageContentId integer;
		refOperationPackageContentIdTemp integer;
		updatedPackageInPackageId integer;
		innerPackageRecord record;
		isFirstItemInPkgpkg boolean;
		refPkgpkgPackageContentIdTemp integer;
		refPkgpkgPackageContentId integer;
	BEGIN
		
		FOR packageComponentDetailRecord IN (
			SELECT pcd.*,idmap.package_id as updated_pack_id FROM package_componentdetail pcd 
			JOIN pack_master p ON (p.package_id = pcd.package_id)
			JOIN temp_pack_master_packages_mapping idmap ON(idmap.pack_master_id=p.package_id)
			WHERE p.type='P' AND pcd.charge_head <> 'PKGPKG'
		) 	
		LOOP
			newPackageContentId := nextval('package_contents_seq');
			INSERT INTO temp_package_component_id_mapping(package_id,pack_ob_id,package_content_id)
			SELECT 	packageComponentDetailRecord.updated_pack_id as package_id,
					packageComponentDetailRecord.pack_ob_id as pack_ob_id,
					newPackageContentId as package_content_id;
			INSERT INTO package_contents( package_content_id , package_id , activity_id,
			                              activity_type, activity_qty , activity_qty_uom ,
										  activity_remarks, doctor_id , dept_id, display_order, 
										  modified_by , modified_at , created_by , created_at, 
										  consultation_type_id, charge_head, conduction_gap , 
										  conduction_gap_unit, parent_pack_ob_id , operation_id ,
										  bed_id , panel_id,visit_qty_limit, activity_charge)
			SELECT	newPackageContentId as package_content_id,
					packageComponentDetailRecord.updated_pack_id::integer as package_id,
					packageComponentDetailRecord.activity_id AS activity_id,
					packageComponentDetailRecord.activity_type as activity_type,
					packageComponentDetailRecord.activity_qty as activity_qty,
					packageComponentDetailRecord.activity_units as activity_qty_uom,
					packageComponentDetailRecord.activity_remarks as activity_remarks,
					NULL as doctor_id,
					NULL as dept_id,
					packageComponentDetailRecord.display_order as display_order,
					NULL as modified_by,
					NULL as modified_at,
					'InstaAdmin' as created_by,
					NOW() as created_at,
					packageComponentDetailRecord.consultation_type_id as consultation_type_id,
					packageComponentDetailRecord.charge_head as charge_head,
					NULL as conduction_gap,
					NULL as conduction_gap_unit,
					NULL as parent_pack_ob_id,
					NULL as operation_id,
					NULL as bed_id,
					NULL AS panel_id,
					packageComponentDetailRecord.activity_qty AS visit_qty_limit,
					packageComponentDetailRecord.activity_charge AS activity_charge;
		END LOOP;
		
		FOR packageComponentDetailRecord IN 
			(SELECT pm.*,pMap.package_id as updated_pack_id FROM pack_master pm
				JOIN temp_pack_master_packages_mapping pMap ON (pm.package_id = pMap.pack_master_id)
				WHERE pm.type='P' AND pm.operation_id IS NOT NULL AND pm.operation_id<>'')
				LOOP
					FOREACH operationChargeHead IN ARRAY operationChargeHeads LOOP
					newPackageContentId := nextval('package_contents_seq');
					IF operationChargeHead='SUOPE' THEN
						refOperationPackageContentIdTemp := newPackageContentId;
						refOperationPackageContentId := NULL;
					ELSE
						refOperationPackageContentId := refOperationPackageContentIdTemp;
					END IF;
						INSERT INTO temp_package_component_id_mapping(package_id,pack_ob_id,package_content_id)
						SELECT 	packageComponentDetailRecord.updated_pack_id as package_id,
								NULL as pack_ob_id,
								newPackageContentId as package_content_id;
					INSERT INTO package_contents( package_content_id , package_id , activity_id,
			                              activity_type, activity_qty , activity_qty_uom ,
										  activity_remarks, doctor_id , dept_id, display_order, 
										  modified_by , modified_at , created_by , created_at, 
										  consultation_type_id, charge_head, conduction_gap , 
										  conduction_gap_unit, parent_pack_ob_id , operation_id ,
										  bed_id , panel_id,visit_qty_limit, activity_charge
										  , content_id_ref)
					SELECT
						newPackageContentId as package_content_id,
						packageComponentDetailRecord.updated_pack_id as package_id, 			
						packageComponentDetailRecord.operation_id as activity_id,
						'Operation' as activity_type,
						1 as activity_qty,
						NULL as activity_qty_uom,
						NULL as activity_remarks,
						NULL as doctor_id,
						NULL as dept_id,
						0 as display_order,
						NULL as modified_by,
						NULL as modified_at,
						'InstaAdmin' as created_by,
						NOW() as created_at,
						NULL as consultation_type_id,
						operationChargeHead as charge_head,
						NULL as conduction_gap,
						NULL as conduction_gap_unit,
						NULL as parent_pack_ob_id,
						packageComponentDetailRecord.operation_id as operation_id,
						NULL as bed_id,
						NULL as panel_id,
						1 as visit_qty_limit,
						0 as activity_charge,
						refOperationPackageContentId as content_id_ref;
					END LOOP;
		END LOOP;

-- Moves Diag Package and Packages in package_componentdetail.
-- We must traverse one level inside packages, and bring its' contents also as the package contents
-- Example : If P1 contains P2 and P3, we are traversing P2 and P3 and bringing its contents.
-- 			But, if P2 contains any inner package, we are ignoring it.
-- Refer confluence
-- https://practo.atlassian.net/wiki/spaces/HIMS/pages/1148125367/Packages+4.0+-+Package+Master+Stories


		FOR packageComponentDetailRecord IN (
			SELECT pcd.activity_id, pcd.pack_ob_id ,idmap.package_id as updated_pack_id,
			  pcd.display_order
				FROM package_componentdetail pcd 
				JOIN pack_master p ON (p.package_id = pcd.package_id)
				JOIN temp_pack_master_packages_mapping idmap ON(idmap.pack_master_id=p.package_id)
				WHERE p.type='P' AND pcd.charge_head = 'PKGPKG'
		) LOOP
			isFirstItemInPkgpkg := true;
			FOR innerPackageRecord IN (
				SELECT pcompdet.*, activityIdMap.package_id as updatedActivityId 
				FROM package_componentdetail pcompdet
				JOIN temp_pack_master_packages_mapping activityIdMap 
					ON (activityIdMap.pack_master_id = pcompdet.package_id)
				WHERE  pcompdet.package_id::text = packageComponentDetailRecord.activity_id AND pcompdet.charge_head<>'PKGPKG'
			) LOOP
				newPackageContentId := nextval('package_contents_seq');
				IF isFirstItemInPkgpkg THEN
					refPkgpkgPackageContentIdTemp := newPackageContentId;
					refPkgpkgPackageContentId := NULL;
				ELSE 
					refPkgpkgPackageContentId := refPkgpkgPackageContentIdTemp;
				END IF;
				INSERT INTO temp_package_component_id_mapping(package_id,pack_ob_id,package_content_id)
				SELECT 	packageComponentDetailRecord.updated_pack_id as package_id,
					packageComponentDetailRecord.pack_ob_id as pack_ob_id,
					newPackageContentId as package_content_id;
				INSERT INTO package_contents( package_content_id , package_id , activity_id,
												activity_type, activity_qty , activity_qty_uom ,
												activity_remarks, doctor_id , dept_id, display_order, 
												modified_by , modified_at , created_by , created_at, 
												consultation_type_id, charge_head, conduction_gap , 
												conduction_gap_unit, parent_pack_ob_id , operation_id ,
												bed_id , panel_id,visit_qty_limit,
												activity_charge,content_id_ref)
				SELECT	newPackageContentId as package_content_id,
							packageComponentDetailRecord.updated_pack_id::integer as package_id,
							innerPackageRecord.activity_id AS activity_id,
							innerPackageRecord.activity_type as activity_type,
							innerPackageRecord.activity_qty as activity_qty,
							innerPackageRecord.activity_units as activity_qty_uom,
							innerPackageRecord.activity_remarks as activity_remarks,
							NULL as doctor_id,
							NULL as dept_id,
							packageComponentDetailRecord.display_order as display_order,
							NULL as modified_by,
							NULL as modified_at,
							'InstaAdmin' as created_by,
							NOW() as created_at,
							innerPackageRecord.consultation_type_id as consultation_type_id,
							innerPackageRecord.charge_head as charge_head,
							NULL as conduction_gap,
							NULL as conduction_gap_unit,
							NULL as parent_pack_ob_id,
							NULL as operation_id,
							NULL as bed_id,
							innerPackageRecord.updatedActivityId AS panel_id,
							innerPackageRecord.activity_qty AS visit_qty_limit,
							innerPackageRecord.activity_charge AS activity_charge,
							refPkgpkgPackageContentId AS content_id_ref;
			isFirstItemInPkgpkg := false;
			END LOOP;
		END LOOP;
		
	UPDATE package_contents SET consultation_type_id = -1 
	WHERE consultation_type_id IS NULL AND charge_head = 'OPDOC';
	return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_mvp_package_component_charges() RETURNS boolean AS $BODY$
	BEGIN
			
		   INSERT INTO package_content_charges(package_content_id,
		   										org_id,bed_type,charge,discount,is_override)

		   SELECT 
				pcmap.package_content_id as package_content_id,
				pic.org_id as org_id,
				pic.bed_type as bed_type,
				pic.charge as charge,
				0 as discount,
				pic.is_override as is_override
				FROM package_item_charges pic 
				JOIN temp_pack_master_packages_mapping pmmap ON (pic.package_id=pmmap.pack_master_id)
				JOIN temp_package_component_id_mapping pcmap ON (pcmap.package_id=pmmap.package_id) AND (pcmap.pack_ob_id=pic.pack_ob_id);
		return true;		
	END; 

$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_calculate_component_charge(activityId TEXT,
									activity_qty INTEGER,
									orgId TEXT, 
									bedType TEXT,
									activityType TEXT,
									chargeHead TEXT, 
									existing_package_charge NUMERIC,
									existing_package_discount NUMERIC,
									item_master_charge NUMERIC,
									item_master_discount NUMERIC,
									surg_assistant_charge NUMERIC,
									surgeon_charge NUMERIC,
									anesthetist_charge NUMERIC,
									equipment_charge NUMERIC)
									RETURNS numeric AS $BODY$
		DECLARE
			charge numeric;
		BEGIN
			CASE WHEN  activityType='Service' 
						OR activityType='Laboratory' 
						OR activityType='Radiology' THEN
				charge = item_master_charge - item_master_discount;
			WHEN chargeHead='SACOPE' THEN
				charge = surg_assistant_charge;
			WHEN chargeHead='SUOPE' THEN
				charge = surgeon_charge;
			WHEN chargeHead='ANAOPE' THEN
				charge = anesthetist_charge;
			WHEN chargeHead='EQUOTC' THEN
				charge = equipment_charge;
			ELSE
				charge = existing_package_charge;
			END CASE;
			IF charge<=0.0 THEN
				return 0;
			END IF;
			return charge * activity_qty;
		END;
$BODY$ 
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION migrate_update_charge_in_package_charges() 
									RETURNS boolean AS $BODY$
		BEGIN
		CREATE INDEX temp_package_content_charges_idx ON temp_package_content_charges(package_id,org_id,bed_type);
		INSERT INTO temp_package_charge_mapping
					(package_id,item_count,total_calculated_package_charge,org_id,bed_type)
		SELECT 	package_id as package_id,
				count(package_id) as item_count,
				sum(pcontcharge.charge) as total_calculated_package_charge,
				org_id as org_id,
				bed_type as bed_type
				FROM temp_package_content_charges pcontcharge 
					GROUP BY pcontcharge.package_id,
								 pcontcharge.org_id,
								 pcontcharge.bed_type;
--								 
--		CREATE INDEX temp_package_charge_mapping_packid_idx ON temp_package_charge_mapping(package_id);
--		CREATE INDEX temp_package_charge_mapping_orgid_idx ON temp_package_charge_mapping(org_id);
--		CREATE INDEX temp_package_charge_mapping_btid_idx ON temp_package_charge_mapping(bed_type);

		INSERT INTO temp_charge_calc_table
		SELECT pcc.content_charge_id,
				pcc.charge + ((pcc.package_charge - tmap.total_calculated_package_charge) * 
					(pcc.charge/tmap.total_calculated_package_charge)) as charge
			 FROM temp_package_content_charges pcc JOIN
			 	temp_package_charge_mapping tmap ON 
				 (pcc.package_id=tmap.package_id 
				 	AND pcc.org_id=tmap.org_id 
					AND pcc.bed_type=tmap.bed_type) 
					WHERE tmap.total_calculated_package_charge > 0;

		INSERT INTO temp_charge_calc_table
			 select pcc.content_charge_id,
			 	(pcc.package_charge/tmap.item_count) as charge
				 FROM temp_package_content_charges pcc JOIN
				 temp_package_charge_mapping tmap ON 
				 			(pcc.package_id=tmap.package_id 
				 				AND pcc.org_id=tmap.org_id 
								AND pcc.bed_type=tmap.bed_type)
				 WHERE tmap.total_calculated_package_charge = 0;

		INSERT INTO package_content_charges(package_content_id,org_id,bed_type,charge,discount,is_override,package_id,package_charge,modified_by,modified_at,created_by,created_at)
			SELECT 
					pcc.package_content_id as package_content_id,
					pcc.org_id as org_id,
					pcc.bed_type as bed_type,
					coalesce(tat.charge,pcc.charge) as charge,
					pcc.discount as discount,
					pcc.is_override as is_override,
					pcc.package_id as package_id,
					pcc.package_charge as package_charge,
					pcc.modified_by as modified_by,
					pcc.modified_at as modified_at,
					pcc.created_by as created_by,
					pcc.created_at as created_at
				FROM temp_package_content_charges pcc LEFT JOIN
					temp_charge_calc_table tat ON (pcc.content_charge_id = tat.content_charge_id)
					LEFT JOIN packages p ON(p.package_id = pcc.package_id)
				WHERE 
					p.multi_visit_package = false;

			return true;
		END;
$BODY$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION migrate_fix_package_content_charges_delta() RETURNS boolean AS $BODY$
	BEGIN
	UPDATE package_content_charges pcc
		SET charge = pcc.charge + d_calc.delta
		FROM (
			SELECT pc.package_id, pcc.org_id,pcc.bed_type, pchg.charge - sum(pcc.charge) AS delta
			FROM package_content_charges pcc
			JOIN package_contents pc ON (pcc.package_content_id = pc.package_content_id)
			JOIN package_charges pchg ON (pc.package_id = pchg.package_id AND pcc.org_id = pchg.org_id AND pcc.bed_type = pchg.bed_type)
			GROUP BY pc.package_id, pcc.org_id, pcc.bed_type, pchg.charge
		) d_calc,
		(
			SELECT package_id, min(package_content_id) AS first_content_id FROM 
			package_contents GROUP BY package_id
		) pfc, 
		package_contents pc
		WHERE d_calc.delta > -0.5 AND d_calc.delta < 0.5 AND d_calc.delta != 0 
		AND (pcc.package_content_id = pfc.first_content_id) 
		AND d_calc.package_id = pc.package_id 
		AND pc.package_content_id = pcc.package_content_id
		AND d_calc.org_id = pcc.org_id AND d_calc.bed_type = pcc.bed_type;
		return true;
	END;
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_diagnostic_charges_packages() RETURNS boolean AS $BODY$
	BEGIN

			INSERT INTO temp_package_content_charges(package_content_id,org_id,bed_type,charge,is_override,package_id,package_charge)
				SELECT
					ipd.package_content_id as package_content_id,
					ipd.org_id as org_id,
					ipd.bed_type as bed_type,
					(COALESCE(dc.charge,0.0)-COALESCE(dc.discount,0.0)) * ipd.activity_qty
					as charge,
					ipd.is_override as is_override,
					ipd.package_id as package_id,
					ipd.package_charge as package_charge
					FROM init_package_details ipd
					JOIN diagnostic_charges dc ON
								(dc.test_id = ipd.activity_id 
									AND dc.bed_type = ipd.bed_type 
									AND dc.org_name = ipd.org_id);

	return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_service_master_charges_packages() RETURNS boolean AS $BODY$
	BEGIN
			INSERT INTO temp_package_content_charges(package_content_id,org_id,bed_type,charge,is_override,package_id,package_charge)
				SELECT
					ipd.package_content_id as package_content_id,
					ipd.org_id as org_id,
					ipd.bed_type as bed_type,
					(COALESCE(smc.unit_charge,0.0) - COALESCE(smc.discount,0.0)) * ipd.activity_qty
					as charge,
					ipd.is_override as is_override,
					ipd.package_id as package_id,
					ipd.package_charge as package_charge
					FROM init_package_details ipd
					JOIN service_master_charges smc ON
								(smc.service_id = ipd.activity_id 
									AND smc.bed_type = ipd.bed_type 
									AND smc.org_id = ipd.org_id);
								return true;
	END; 
$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_operation_charges_packages() RETURNS boolean AS $BODY$
	BEGIN
			INSERT INTO temp_package_content_charges(package_content_id,org_id,bed_type,charge,is_override,package_id,package_charge)
				SELECT
					ipd.package_content_id as package_content_id,
					ipd.org_id as org_id,
					ipd.bed_type as bed_type,
					CASE WHEN ipd.charge_head='SACOPE' THEN
							(COALESCE(surg_asstance_charge,0.0)-COALESCE(surg_asst_discount,0.0)) * ipd.activity_qty
						 WHEN ipd.charge_head='SUOPE' THEN
							(COALESCE(surgeon_charge,0.0)-COALESCE(surg_discount,0.0)) * ipd.activity_qty
						 WHEN ipd.charge_head='ANAOPE' THEN
							(COALESCE(anesthetist_charge,0.0)-COALESCE(anest_discount,0.0)) * ipd.activity_qty
						ELSE 0
					END AS charge,
					ipd.is_override as is_override,
					ipd.package_id as package_id,
					ipd.package_charge as package_charge
					FROM init_package_details ipd
					JOIN operation_charges oc ON
								(oc.op_id = ipd.activity_id 
									AND oc.bed_type = ipd.bed_type 
									AND oc.org_id = ipd.org_id);
								return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_equipment_charges_packages() RETURNS boolean AS $BODY$
	BEGIN
			INSERT INTO temp_package_content_charges(package_content_id,org_id,bed_type,charge,is_override,package_id,package_charge)
				SELECT
					ipd.package_content_id as package_content_id,
					ipd.org_id as org_id,
					ipd.bed_type as bed_type,
					(COALESCE(ec.daily_charge,0.0)-COALESCE(ec.daily_charge_discount,0.0)) * ipd.activity_qty
					as charge,
					ipd.is_override as is_override,
					ipd.package_id as package_id,
					ipd.package_charge as package_charge
					FROM init_package_details ipd
					JOIN equipement_charges ec ON
								(ec.equip_id = ipd.activity_id
									AND ec.org_id = ipd.org_id
									AND ec.bed_type = ipd.bed_type
								);
								return true;
	END; 
	
$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_bed_charges_packages() RETURNS boolean AS $BODY$
	BEGIN
			INSERT INTO temp_package_content_charges(package_content_id,org_id,bed_type,charge,is_override,package_id,package_charge)
				SELECT
					ipd.package_content_id as package_content_id,
					ipd.org_id as org_id,
					ipd.bed_type as bed_type,
					(COALESCE(bd.bed_charge,0.0)-COALESCE(bd.bed_charge_discount,0.0)) * ipd.activity_qty
					as charge,
					ipd.is_override as is_override,
					ipd.package_id as package_id,
					ipd.package_charge as package_charge
					FROM init_package_details ipd
					JOIN bed_details bd ON
								(bd.bed_type = ipd.activity_id
									AND bd.organization = ipd.org_id
								);
								return true;
	END; 
	
$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_remaining_charges_packages() RETURNS boolean AS $BODY$
	BEGIN
			INSERT INTO temp_package_content_charges(package_content_id,org_id,bed_type,charge,is_override,package_id,package_charge)
				SELECT
					ipd.package_content_id as package_content_id,
					ipd.org_id as org_id,
					ipd.bed_type as bed_type,
					ipd.charge * ipd.activity_qty as charge,
					ipd.is_override as is_override,
					ipd.package_id as package_id,
					ipd.package_charge as package_charge
					FROM init_package_details ipd;
					return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_other_package_contents() RETURNS boolean AS $BODY$
	BEGIN
			INSERT INTO temp_package_content_charges(package_content_id,org_id,bed_type,charge,is_override,package_id,package_charge)
				SELECT
					ipd.package_content_id as package_content_id,
					ipd.org_id as org_id,
					ipd.bed_type as bed_type,
					COALESCE(ocm.charge,0.0) * ipd.activity_qty
					as charge,
					ipd.is_override as is_override,
					ipd.package_id as package_id,
					ipd.package_charge as package_charge
					FROM init_package_details ipd
					JOIN other_charge_master ocm ON
								(ocm.charge_name = ipd.activity_id);
	return true;
	END; 
	
$BODY$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION migrate_doctor_package_contents() RETURNS boolean AS $BODY$
	BEGIN
			INSERT INTO temp_package_content_charges(package_content_id,org_id,bed_type,charge,is_override,package_id,package_charge)
				SELECT
					ipd.package_content_id as package_content_id,
					ipd.org_id as org_id,
					ipd.bed_type as bed_type,
					(COALESCE(cc.charge,0.0)-COALESCE(cc.discount,0.0)) * ipd.activity_qty
					as charge,
					ipd.is_override as is_override,
					ipd.package_id as package_id,
					ipd.package_charge as package_charge
					FROM init_package_details ipd
					JOIN consultation_charges cc ON
					(cc.consultation_type_id = ipd.consultation_type_id
									AND cc.org_id = ipd.org_id
									AND cc.bed_type = ipd.bed_type);
	return true;
	END; 
	
$BODY$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION migrate_clean_package_contents() RETURNS boolean AS $BODY$
	BEGIN
	DELETE FROM init_package_details 
						WHERE package_content_id IN
						(SELECT package_content_id from temp_package_content_charges);
	return true;
	END; 
$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_get_init_details() RETURNS boolean AS $BODY$
	BEGIN
	CREATE TABLE init_package_details AS
	SELECT pcont.package_content_id as package_content_id,
					pcont.activity_id as activity_id,
					pc.org_id as org_id,
					pc.bed_type as bed_type,
					pc.is_override as is_override,
					pmmap.package_id as package_id,
					pcont.activity_qty as activity_qty,
					pcont.charge_head as  charge_head,
					pcont.activity_charge as charge,
					pcont.consultation_type_id as consultation_type_id,
					COALESCE(pc.charge,0) as package_charge 
	FROM package_charges pc
					JOIN organization_details od ON
								(od.org_id = pc.org_id)
					JOIN pack_master pm ON 
								(pc.package_id=pm.package_id AND pm.package_type='o')
					JOIN temp_pack_master_packages_mapping pmmap ON
								(pmmap.pack_master_id=pm.package_id)
					JOIN temp_package_contents pcont ON 
								(pcont.package_id=pmmap.package_id)
					WHERE pc.bed_type in ('GENERAL', (SELECT billing_bed_type_for_op FROM generic_preferences));
					
	
					
	INSERT INTO init_package_details
	SELECT pcont.package_content_id as package_content_id,
					pcont.activity_id as activity_id,
					pc.org_id as org_id,
					pc.bed_type as bed_type,
					pc.is_override as is_override,
					pmmap.package_id as package_id,
					pcont.activity_qty as activity_qty,
					pcont.charge_head as  charge_head,
					pcont.activity_charge as charge,
					pcont.consultation_type_id as consultation_type_id,
					COALESCE(pc.charge,0) as package_charge 
	FROM package_charges pc
					JOIN organization_details od ON
								(od.org_id = pc.org_id)
					JOIN pack_master pm ON 
								(pc.package_id=pm.package_id AND pm.package_type<>'o')
					JOIN temp_pack_master_packages_mapping pmmap ON
								(pmmap.pack_master_id=pm.package_id)
					JOIN temp_package_contents pcont ON 
								(pcont.package_id=pmmap.package_id);
	return true;
	END; 
$BODY$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION migrate_package_charges() RETURNS boolean AS $BODY$
	BEGIN
--			INSERT INTO temp_package_content_charges(package_content_id,org_id,bed_type,charge,is_override,package_id,package_charge)
--				SELECT
--					pcont.package_content_id as package_content_id,
--					pc.org_id as org_id,
--					pc.bed_type as bed_type,
--					migrate_calculate_component_charge(	pcont.activity_id,
--														pcont.activity_qty,
--														pc.org_id,
--														pc.bed_type,
--														pcont.activity_type,
--														pcont.charge_head,
--														pcont.activity_charge,
--														0.0,
--														COALESCE(dc.charge,smc.unit_charge,0.0),COALESCE(dc.discount,smc.discount,0.0),
--														COALESCE(surg_asstance_charge,0.0)-COALESCE(surg_asst_discount,0.0),
--														COALESCE(surgeon_charge,0.0)-COALESCE(surg_discount,0.0),
--														COALESCE(anesthetist_charge,0.0)-COALESCE(anest_discount,0.0),
--														COALESCE(ec.daily_charge,0.0)-COALESCE(ec.daily_charge_discount,0.0)
--														) 
--					as charge,
--					pc.is_override as is_override,
--					pmmap.package_id as package_id,
--					COALESCE(pc.charge,0) - COALESCE(pc.discount,0) as package_charge
--					FROM package_charges pc
--					JOIN pack_master pm ON 
--								(pc.package_id=pm.package_id and pm.type='P')
--					JOIN temp_pack_master_packages_mapping pmmap ON
--								(pmmap.pack_master_id=pm.package_id)
--					JOIN package_contents pcont ON 
--								(pcont.package_id=pmmap.package_id)
--					LEFT JOIN diagnostic_charges dc ON
--								(dc.test_id = pcont.activity_id 
--									AND dc.bed_type = pc.bed_type 
--									AND dc.org_name = pc.org_id)
--					LEFT JOIN service_master_charges smc ON
--								(smc.service_id = pcont.activity_id 
--									AND smc.bed_type = pc.bed_type 
--									AND smc.org_id = pc.org_id)
--					LEFT JOIN operation_charges oc ON
--								(oc.op_id = pcont.activity_id 
--									AND oc.bed_type = pc.bed_type 
--									AND oc.org_id = pc.org_id)
--					LEFT JOIN equipement_charges ec ON
--								(ec.equip_id = pcont.activity_id
--									AND ec.org_id = pc.org_id
--									AND ec.bed_type = pc.bed_type
--								);

		

		PERFORM migrate_service_master_charges_packages();
		 PERFORM migrate_clean_package_contents();
		RAISE NOTICE 'COMPLETED SERVICE MASTER CHARGES MIGRATION';

		PERFORM migrate_operation_charges_packages();
		 PERFORM migrate_clean_package_contents();
		RAISE NOTICE 'COMPLETED OPERATION_CHARGES_MIGRATION';

		PERFORM migrate_equipment_charges_packages();
		 PERFORM migrate_clean_package_contents();

		RAISE NOTICE 'COMPLETED EQUIPMENT_CHARGES_MIGRATION';
		
		PERFORM migrate_bed_charges_packages();
		PERFORM migrate_clean_package_contents();
		RAISE NOTICE 'COMPLETED BED_CHARGES_MIGRATION';

		PERFORM migrate_diagnostic_charges_packages();
		PERFORM migrate_clean_package_contents();
		RAISE NOTICE 'COMPLETED DIAGNOSTIC PACKAGES CHARGES MIGRATION';
		
		PERFORM migrate_other_package_contents();
		PERFORM migrate_clean_package_contents();
		RAISE NOTICE 'COMPLETED OTHER PACKAGES CHARGES MIGRATION';
		
		PERFORM migrate_doctor_package_contents();
		PERFORM migrate_clean_package_contents();
		RAISE NOTICE 'COMPLETED DOCTOR PACKAGES CHARGES MIGRATION';
		
		PERFORM migrate_remaining_charges_packages();
--		PERFORM migrate_clean_package_contents();
		RAISE NOTICE 'COMPLETED REMAINING PACKAGES CHARGES MIGRATION';
																		
--	CREATE INDEX temp_package_content_charges_content_id_idx ON temp_package_content_charges(package_content_id);
--	
--	CREATE INDEX temp_package_content_charges_org_id_idx ON temp_package_content_charges(org_id);
--	
--	CREATE INDEX temp_package_content_charges_bed_type_idx ON temp_package_content_charges(bed_type);
	return true;
	END; 

$BODY$ 
LANGUAGE plpgsql;


-------------------Calling Migration functions------------------------------

--1. Migrates pack_master -> packages and also creates temporary table containing the ID mapping.
SELECT migrate_pack_master();
CREATE INDEX packages_mapping_package_id_idx ON temp_pack_master_packages_mapping(package_id);
CREATE INDEX packages_mapping_pack_master_id_idx ON temp_pack_master_packages_mapping(pack_master_id);

--2.Migrates package components to respective tables
--2.1 Migrating package_componentdetail to package_contents

-- EXECUTION SEQUENCE :  
-- STEP 1: Moves package_component_detail entries to package_contents
-- STEP 2: 	Copies every operation_details from pack_master(Currently at package level) into 3 package_content
-- 			The copying is done with 3 different chargeheads PSACOPE (Surgical Assistance Charge)
--															 PSUOPE (Surgeon Charge)
--															 PANAOPE (Anaesthetic Charge)
SELECT migrate_package_components();
--CREATE INDEX packages_mapping_package_id_idx ON temp_pack_master_packages_mapping(package_id);
--CREATE INDEX packages_mapping_pack_master_id_idx ON temp_pack_master_packages_mapping(pack_master_id);
CREATE INDEX packagecomp_mapping_package_id_idx ON temp_package_component_id_mapping(package_id);
CREATE INDEX packagecomp_mapping_pack_ob_id_idx ON temp_package_component_id_mapping(pack_ob_id);
CREATE INDEX packagecomp_mapping_package_content_id_idx ON temp_package_component_id_mapping(package_content_id);
CREATE INDEX package_item_charges_package_id_idx ON package_item_charges(package_id);

--2.2 Migrating package_item_charges to package_content_charges
--MVP charges are stored in item level already, so we need to move it to package_content_charges
SELECT migrate_mvp_package_component_charges();

--CREATE INDEX diagnostic_charges_test_id_bed_type_org_idx ON diagnostic_charges(test_id,bed_type,org_name);
CREATE TABLE temp_package_contents AS TABLE package_contents;
CREATE INDEX temp_package_contents_pack_id_idx ON temp_package_contents(package_id);
CREATE INDEX temp_package_contents_pack_cont_id_idx ON temp_package_contents(package_content_id);
CREATE INDEX temp_package_contents_activity_type_idx ON temp_package_contents(activity_type);
CREATE INDEX temp_package_contents_charge_head_idx ON temp_package_contents(charge_head);

CREATE INDEX package_charges_pack_id_idx ON package_charges(package_id);

SELECT migrate_get_init_details();

--2.3 Migrating package_charges to package_content_charges
CREATE INDEX init_package_details_package_content_id_idx ON init_package_details(package_content_id);
CREATE INDEX init_package_details_package_act_org_bt_id_idx ON init_package_details(activity_id,org_id,bed_type);
SELECT migrate_package_charges();

--2.4 Proportially splitting charges among the components.
SELECT migrate_update_charge_in_package_charges();

--3.Updates package_item_sub_groups with new package_id
SELECT migrate_package_item_subgroups();

--4. Updates  packages_insurance_category_mapping with new package_id
SELECT migrate_insurance_category_mappings();

--5. Updates entity_id in orderable_item
SELECT migrate_update_package_orderable_item();

--6. Update package_center_master with new package_id
SELECT migrate_package_center_master_pack_id();

--7. Not required to update insurance_category_id since it is migrated now.

--8. Update package_sponsor_master with new package id
SELECT migrate_package_sponsor_master();

--9. Update dept_package_applicability with new package id
SELECT migrate_dept_package_applicability();

--10. Update pack_org_details with new package_id
SELECT migrate_pack_org_details_mapping();

--11.Update package_id in package_charges
SELECT migrate_update_package_charges_pack_id();

--12 Update package_plan_master with new package_id
SELECT migrate_package_plan_master();

--13 Correct content charges broken due to roundoffs
SELECT migrate_fix_package_content_charges_delta();

-------------------END--Calling Migration functions------------------------------

--Dropping migration functions/temp tables post-migration.
DROP FUNCTION migrate_pack_master();

DROP FUNCTION migrate_package_components();

DROP FUNCTION migrate_package_item_subgroups();

DROP FUNCTION migrate_insurance_category_mappings();

DROP FUNCTION migrate_update_package_orderable_item();

DROP FUNCTION migrate_mvp_package_component_charges();

DROP FUNCTION migrate_package_charges();

DROP FUNCTION migrate_package_center_master_pack_id();

DROP FUNCTION migrate_calculate_component_charge
	(text,integer,text,text,text,text,numeric,numeric,
	numeric,numeric,numeric,numeric,numeric,numeric);

DROP FUNCTION migrate_update_charge_in_package_charges();

DROP FUNCTION update_package_names_unique();

DROP FUNCTION migrate_package_sponsor_master();

DROP FUNCTION migrate_dept_package_applicability();

DROP FUNCTION migrate_pack_org_details_mapping();

DROP FUNCTION migrate_update_package_charges_pack_id();


--- JPA Alter table queries to have non-composite-primary-keys
--- Also,enforce unique constraint for already existing composite-primary-key

--- 1. Table name: package_charges
--- Old Indexes:
-- "package_charges_pkey" PRIMARY KEY, btree (package_id, org_id, bed_type)
-- "package_charges_indx" btree (package_id, org_id, bed_type)
--
--  New Indexes:
--     "package_charges_pkey" PRIMARY KEY, btree (package_charges_id)
--     "package_charges_unique_constraint" UNIQUE CONSTRAINT, btree (package_id, org_id, bed_type)
--     "package_charges_indx" btree (package_id, org_id, bed_type)

ALTER TABLE package_charges DROP CONSTRAINT IF EXISTS package_charges_pkey;

CREATE SEQUENCE package_charges_id_seq
   START WITH 1
   INCREMENT BY 1
   NO MINVALUE
   NO MAXVALUE
   CACHE 1;

ALTER TABLE package_charges ADD COLUMN package_charges_id 
	INTEGER DEFAULT nextval('package_charges_id_seq'::regclass) PRIMARY KEY;

ALTER TABLE package_charges
	ADD CONSTRAINT package_charges_unique_constraint UNIQUE (package_id, org_id, bed_type);

-- 2. Table Name: package_item_sub_groups
-- Old Indexes:     
-- "package_item_sub_groups_pkey" PRIMARY KEY, btree (package_id, item_subgroup_id)
-- "package_item_sub_groups_item_subgroup_id_idx" btree (item_subgroup_id)
--
-- New Indexes:
-- "package_item_sub_groups_pkey" PRIMARY KEY, btree (package_item_sub_groups_id)
-- "package_item_sub_groups_unique_constraint" UNIQUE CONSTRAINT, btree (package_id, item_subgroup_id)
-- "package_item_sub_groups_item_subgroup_id_idx" btree (item_subgroup_id)

ALTER TABLE package_item_sub_groups DROP CONSTRAINT IF EXISTS package_item_sub_groups_pkey;

CREATE SEQUENCE package_item_sub_groups_id_seq
   START WITH 1
   INCREMENT BY 1
   NO MINVALUE
   NO MAXVALUE
   CACHE 1;

ALTER TABLE package_item_sub_groups ADD COLUMN package_item_sub_groups_id 
	INTEGER DEFAULT nextval('package_item_sub_groups_id_seq'::regclass) PRIMARY KEY;

ALTER TABLE package_item_sub_groups
	ADD CONSTRAINT package_item_sub_groups_unique_constraint UNIQUE (package_id, item_subgroup_id);

-- 3. Table name: packages_insurance_category_mapping
-- Old Indexes:
-- "insurance_category_id_packages_insurance_category_mapping" btree (insurance_category_id)
-- "package_id_packages_insurance_category_mapping" btree (package_id)     
--
-- New Indexes:
-- "packages_insurance_category_mapping_pkey" PRIMARY KEY, btree (packages_insurance_category_mapping_id)
-- "insurance_category_id_packages_insurance_category_mapping" btree (insurance_category_id)
-- "package_id_packages_insurance_category_mapping" btree (package_id)

CREATE SEQUENCE packages_insurance_category_mapping_id_seq
   START WITH 1
   INCREMENT BY 1
   NO MINVALUE
   NO MAXVALUE
   CACHE 1;
	
ALTER TABLE packages_insurance_category_mapping ADD COLUMN packages_insurance_category_mapping_id 
	INTEGER DEFAULT nextval('packages_insurance_category_mapping_id_seq'::regclass) PRIMARY KEY;


-- Enabling back triggers post-migration
--ALTER TABLE packages ENABLE TRIGGER packages_orderable_item_mapping_trigger;

--Cleanup existing data
--ALTER TABLE pack_master rename to obsolete_pack_master;
ALTER TABLE package_content_charges DROP COLUMN package_id;

ALTER TABLE package_content_charges DROP COLUMN package_charge;

ALTER TABLE package_contents DROP COLUMN activity_charge ;

DROP INDEX packages_mapping_package_id_idx;

DROP INDEX packages_mapping_pack_master_id_idx;

DROP INDEX packagecomp_mapping_package_id_idx;

DROP INDEX packagecomp_mapping_pack_ob_id_idx;

DROP INDEX packagecomp_mapping_package_content_id_idx;

DROP TABLE temp_package_charge_mapping;

DROP TABLE init_package_details;
--Comments on tables and columns
--COMMENT ON TABLE obsolete_pack_master IS 
--	'{ "type": "Master", "comment": "Moved to packages from pack_master" }';

COMMENT ON TABLE pack_master IS 
	'{ "type": "Master", "comment": "Deprecated Moved to packages from pack_master" }';

COMMENT ON TABLE packages_insurance_category_mapping IS
	'{ "type": "Master", "comment": "packages to insurance category mapping" } ';

--COMMENT ON TABLE packages_insurance_category_mapping_obsolete IS 
--	'{ "type": "Master", "comment": "Backup of packages_insurance_category_mapping before migration of new packages model" } ';

COMMENT ON TABLE package_item_sub_groups IS 
	'{ "type": "Master", "comment": "Backup of packages_insurance_category_mapping before migration of new packages model" }';
	
COMMENT ON TABLE package_content_charges IS 
	'{ "type": "Master", "comment": "Contains individual charges of package items" }';
	
--COMMENT ON TABLE insurance_plan_package_applicability IS 
--	'{ "type": "Master", "comment": "TO BE DROPPED" }';
	
COMMENT ON TABLE temp_pack_master_packages_mapping IS 
	'{ "type": "Txn", "comment": "Contains packages - pack_master id mapping" }';

COMMENT ON TABLE temp_package_contents IS 
	'{ "type": "Txn", "comment": "TEMP TABLE" }';
	
COMMENT ON TABLE temp_package_component_id_mapping IS 
	'{ "type": "Txn", "comment": "Contains component id mapping" }';
	
COMMENT ON TABLE temp_package_content_charges IS 
	'{ "type": "Txn", "comment": "Temp table" }';
COMMENT ON TABLE temp_charge_calc_table IS 
	'{ "type": "Txn", "comment": "Temp table" }';

COMMENT ON  SEQUENCE package_content_charges_seq IS
	'{ "type": "Txn", "comment": "" }';

--COMMENT ON SEQUENCE insurance_plan_package_applicability_seq IS
--	'{ "type": "Txn", "comment": "" }';
	
	
--
--COMMENT ON TABLE package_item_sub_groups_obsolete IS 
--	'{ "type": "Master", "comment": "Backup of previous data, package_item_sub_groups" }';
--	
--COMMENT ON TABLE package_center_master_obsolete IS 
--	'{ "type": "Master", "comment": "Backup of previous data, package_center_master" }';

--COMMENT ON TABLE package_sponsor_master_obsolete IS 
--	'{ "type": "Master", "comment": "Backup of previous data, package_sponsor_master" }';

--COMMENT ON TABLE dept_package_applicability_obsolete IS 
--	'{ "type": "Master", "comment": "Backup of previous data, dept_package_applicability" }';

COMMENT ON SEQUENCE package_charges_id_seq IS 
	'{ "type": "Txn", "comment": "" }';

COMMENT ON SEQUENCE temp_package_content_charges_seq IS 
	'{ "type": "Txn", "comment": "" }';
	


COMMENT ON SEQUENCE package_item_sub_groups_id_seq IS
	'{ "type": "Txn", "comment": "" }';

COMMENT ON SEQUENCE packages_insurance_category_mapping_id_seq IS
	'{ "type": "Txn", "comment": "" }';