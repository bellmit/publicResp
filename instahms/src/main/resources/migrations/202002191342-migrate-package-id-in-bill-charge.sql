-- liquibase formatted sql
-- changeset manjular:migrate-packageid-in-bill-charge-and-package-presrcibed  splitStatements:false


CREATE OR REPLACE FUNCTION migrate_update_package_bill_charge() RETURNS boolean AS $BODY$
	BEGIN
		
		UPDATE bill_charge 
			SET act_description_id=packageIdMapping.package_id::text 
			FROM (
			select package_id,pack_master_id from temp_pack_master_packages_mapping
			) as packageIdMapping
			WHERE act_description_id=packageIdMapping.pack_master_id::text AND charge_head='PKGPKG';
			
	return true;
	END; 
$BODY$ 
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_update_package_bill_activity_charge() RETURNS boolean AS $BODY$
        BEGIN

                UPDATE bill_activity_charge
                        SET act_description_id=packageIdMapping.package_id::text
                        FROM (
                        select package_id,pack_master_id from temp_pack_master_packages_mapping
                        ) as packageIdMapping
                        WHERE act_description_id=packageIdMapping.pack_master_id::text AND payment_charge_head='PKGPKG';

        return true;
        END;
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_update_package_patient_packages() RETURNS boolean AS $BODY$
        BEGIN

                UPDATE patient_packages pp
                        SET package_id=packageIdMapping.package_id
                        FROM (
                        select package_id,pack_master_id from temp_pack_master_packages_mapping
                        ) as packageIdMapping
                        WHERE pp.package_id=packageIdMapping.pack_master_id;

        return true;
        END;
$BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION migrate_update_package_package_prescribed() RETURNS boolean AS $BODY$
        BEGIN

                UPDATE package_prescribed pp
                        SET package_id=packageIdMapping.package_id
                        FROM (
                        select package_id,pack_master_id from temp_pack_master_packages_mapping
                        ) as packageIdMapping
                        WHERE pp.package_id=packageIdMapping.pack_master_id;

        return true;
        END;
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_update_package_scheduler_appointments() RETURNS boolean AS $BODY$
        BEGIN

                UPDATE scheduler_appointments sa
                        SET package_id=packageIdMapping.package_id
                        FROM (
                        select package_id,pack_master_id from temp_pack_master_packages_mapping
                        ) as packageIdMapping
                        WHERE sa.package_id=packageIdMapping.pack_master_id;

        return true;
        END;
$BODY$
LANGUAGE plpgsql;


--Update act_description_id in bill_charge with new package id
SELECT migrate_update_package_bill_charge();

--Update act_description_id in bill_activity_charge with new package id
SELECT migrate_update_package_bill_activity_charge();

--Update package_id in patient_packages with new package id
SELECT migrate_update_package_patient_packages();

--Update package_id in package_prescribed with new package id
SELECT migrate_update_package_package_prescribed();

--Update package_id in scheduler_appointments with new package id
SELECT migrate_update_package_scheduler_appointments();


DROP FUNCTION migrate_update_package_bill_charge();

DROP FUNCTION migrate_update_package_bill_activity_charge();

DROP FUNCTION migrate_update_package_patient_packages();

DROP FUNCTION migrate_update_package_package_prescribed();

DROP FUNCTION migrate_update_package_scheduler_appointments();

