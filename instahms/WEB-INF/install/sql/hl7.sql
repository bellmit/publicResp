--
-- Stuff to enable hl7 interface, which is not normally enabled in all hospitals
-- Note that all required tables are already part of the schema, this script only
-- enables/changes hl7 related functions and triggers which enables HL7.
--

-- TODO: move this to vft.sql and do nothing if no rows in hosp_hl7_prefs

DROP FUNCTION IF EXISTS patient_details_set_export() CASCADE;
CREATE OR REPLACE FUNCTION patient_details_set_export() RETURNS TRIGGER AS $BODY$

DECLARE
	need_export boolean;
	item_id text;

BEGIN
	need_export = false;
	IF (TG_OP = 'INSERT') THEN
		-- Need an export anyway
		need_export := true;
		item_id := NEW.mr_no;

	ELSIF (TG_OP = 'UPDATE') THEN
		-- need export only if interesting values are changed
		IF ((NEW.dateofbirth != OLD.dateofbirth)
			OR (NEW.expected_dob != OLD.expected_dob)
			OR (NEW.patient_address != OLD.patient_address)
			OR (NEW.patient_city != OLD.patient_city)
			OR (NEW.patient_state != OLD.patient_state)
			OR (NEW.country != OLD.country)
			OR (NEW.patient_name != OLD.patient_name)
			OR (NEW.last_name != OLD.last_name)
			OR (NEW.salutation != OLD.salutation)
			OR (NEW.patient_gender != OLD.patient_gender)
			OR (NEW.patient_phone != OLD.patient_phone)
		) THEN
			need_export := true;
			item_id := NEW.mr_no;
		END IF;
	END IF;

	IF need_export THEN
		INSERT INTO hl7_export_items VALUES ('PATIENT', item_id);
	END IF;

	return NEW;
END;
$BODY$ LANGUAGE plpgsql;


DROP FUNCTION IF EXISTS patient_registration_set_export() CASCADE;
CREATE OR REPLACE FUNCTION patient_registration_set_export() RETURNS TRIGGER AS $BODY$
DECLARE
	need_export boolean;
	item_id text;
BEGIN

	need_export := false;
	IF (TG_OP = 'INSERT') THEN
		-- Need an export anyway
		need_export := true;
		item_id := NEW.patient_id;

	ELSIF (TG_OP = 'UPDATE') THEN
		-- RAISE NOTICE 'Inside update';
		-- need export only if interesting values are changed
		IF ((NEW.reg_date != OLD.reg_date)
			OR (NEW.reg_time != OLD.reg_time)
		) THEN
			need_export := true;
			item_id := NEW.patient_id;
			INSERT INTO hl7_export_items VALUES ('VISIT', item_id);
		END IF;
		RAISE NOTICE 'Old flag % New Flag %', OLD.discharge_flag, NEW.discharge_flag;
		IF ((NEW.discharge_flag != OLD.discharge_flag and NEW.discharge_flag = 'D')) THEN
			-- RAISE NOTICE 'Inside discharge check';
			need_export := true;
			item_id := NEW.patient_id;
			INSERT INTO hl7_export_items VALUES ('DISCHARGE', item_id);
		END IF;
	END IF;

	return NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS patient_admit_set_export() CASCADE;
CREATE OR REPLACE FUNCTION patient_admit_set_export() RETURNS TRIGGER AS $BODY$
DECLARE
	need_export boolean;
	item_id text;
	from_bed_id integer;
BEGIN
	need_export = false;
	from_bed_id = 0;
	IF (TG_OP = 'INSERT') THEN
		-- Need an export anyway
		need_export := true;
		item_id := NEW.patient_id;
	END IF;

-- Check if there is a bed for the patient (from bed) which is not the current bed

	SELECT ipb.bed_id
	FROM ip_bed_details ipb
	WHERE ipb.patient_id=NEW.patient_id AND (NOT is_bystander) AND ipb.status NOT IN ('A','C')
		AND ipb.bed_id != NEW.bed_id ORDER BY ipb.end_date DESC LIMIT 1 INTO from_bed_id;

	IF need_export THEN
		IF from_bed_id IS NOT NULL AND from_bed_id > 0 THEN
			INSERT INTO hl7_export_items VALUES ('TRANSFER', item_id);
		ELSE
			INSERT INTO hl7_export_items VALUES ('ADMISSION', item_id);
		END IF;
	END IF;

	return NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS patient_admit_set_export_trigger ON ip_bed_details;
CREATE TRIGGER patient_admit_set_export_trigger
  AFTER INSERT ON ip_bed_details
  FOR EACH ROW EXECUTE PROCEDURE patient_admit_set_export();

DROP TRIGGER IF EXISTS patient_registration_set_export_trigger ON patient_registration;
CREATE TRIGGER patient_registration_set_export_trigger
  AFTER UPDATE OR INSERT ON patient_registration
  FOR EACH ROW EXECUTE PROCEDURE patient_registration_set_export();

DROP FUNCTION IF EXISTS doctor_consultation_set_export() CASCADE;
CREATE OR REPLACE FUNCTION doctor_consultation_set_export() RETURNS TRIGGER AS $BODY$
DECLARE
	need_export boolean;
	item_id text;
BEGIN

	need_export = false;
	IF (TG_OP = 'INSERT') THEN
		-- Need an export anyway
		need_export := true;
		item_id := NEW.patient_id;
	END IF;

	IF need_export THEN
		INSERT INTO hl7_export_items(item_type,item_id,interface_name, hl7_lab_interface_id) 
		VALUES ('CONSULTATION', item_id,'Praxify', (SELECT hl7_lab_interface_id FROM hl7_lab_interfaces WHERE interface_name = 'Praxify' LIMIT 1));
	END IF;

	return NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS doctor_consultation_set_export_trigger ON doctor_consultation;
CREATE TRIGGER doctor_consultation_set_export_trigger
  AFTER UPDATE OR INSERT ON doctor_consultation
  FOR EACH ROW EXECUTE PROCEDURE doctor_consultation_set_export();


  
  
DROP FUNCTION IF EXISTS hl7_test_export_items_on_update() CASCADE;
CREATE FUNCTION hl7_test_export_items_on_update() RETURNS TRIGGER AS $BODY$
DECLARE

	rec RECORD;

BEGIN

-- Note : We get the appointment id and thereby the equipment code for a scheduled test.
-- If the test is not scheduled, then we check if there is at least one equipment with a
-- hl7 export code for the concerned diagnostic department. If there are multiple
-- equipments, satisfying the condition then we assume that any one is good enough, since
-- in such a case our basic assumption is that all the equipments will have the
-- same hl7 export code. Hence the group by on the query.

	FOR rec IN
		SELECT hli.equipment_code_required, hli.interface_name, tem.hl7_export_code, tp.appointment_id, tp.prescribed_id, hli.hl7_lab_interface_id
		FROM hl7_lab_interfaces hli
		JOIN diagnostics_export_interface dei USING(interface_name)
		JOIN diagnostics d USING(test_id)
		JOIN bill_charge bc ON (bc.charge_id = NEW.charge_id)
		JOIN bill_activity_charge bac ON (bac.charge_id = bc.charge_id AND bac.activity_code='DIA')
		JOIN tests_prescribed tp ON (tp.prescribed_id = bac.activity_id::Integer 
			and tp.test_id = dei.test_id)
		LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
		LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
		LEFT JOIN (SELECT em.ddept_id, em.center_id, min(em.hl7_export_code) as hl7_export_code
		FROM test_equipment_master em WHERE coalesce(em.hl7_export_code, '') != '' group by em.ddept_id, em.center_id) as tem
		ON (tem.center_id = COALESCE(pr.center_id, isr.center_id) AND tem.ddept_id = d.ddept_id)
		WHERE hli.status = 'A'
	LOOP
		IF rec.equipment_code_required = 'N' OR rec.appointment_id != 0 OR rec.hl7_export_code != '' THEN

			IF (TG_OP = 'UPDATE') THEN
			-- check if the order has not been sent yet
				IF OLD.payee_doctor_id != NEW.payee_doctor_id THEN
					INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, bill_paid, op_code, hl7_lab_interface_id)
					VALUES ('TEST', rec.prescribed_id, current_timestamp, rec.interface_name, 'N', 'U', rec.hl7_lab_interface_id);
				END IF;
			
			END IF;
		END IF;
	END LOOP;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;




DROP TRIGGER IF EXISTS hl7_test_export_items_trigger_on_update ON bill_charge;
CREATE TRIGGER hl7_test_export_items_trigger_on_update
AFTER UPDATE ON bill_charge
FOR EACH ROW
EXECUTE PROCEDURE hl7_test_export_items_on_update();



DROP FUNCTION IF EXISTS hl7_save_service_export_items() CASCADE;
CREATE OR REPLACE FUNCTION hl7_save_service_export_items() RETURNS trigger LANGUAGE plpgsql AS $function$
DECLARE

	rec RECORD;

BEGIN

	-- Note : We get the appointment id and thereby the equipment code for a scheduled test.
	-- If the test is not scheduled, then we check if there is at least one equipment with a
	-- hl7 export code for the concerned diagnostic department. If there are multiple
	-- equipments, satisfying the condition then we assume that any one is good enough, since
	-- in such a case our basic assumption is that all the equipments will have the
	-- same hl7 export code. Hence the group by on the query.

	FOR rec IN
		SELECT hli.equipment_code_required, hli.interface_name, srm.hl7_export_code,
		COALESCE(pr.center_id, isr.center_id) AS centerid, hli.hl7_lab_interface_id
		FROM hl7_lab_interfaces hli
		JOIN services_export_interface sei USING(interface_name)
		JOIN services s ON (s.service_id = sei.service_id )
		LEFT JOIN patient_registration pr ON (pr.patient_id = NEW.patient_id)
		LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = NEW.patient_id)
		LEFT JOIN (
					SELECT rm.center_id, min(rm.hl7_export_code) as hl7_export_code
               		FROM service_resource_master rm 
					WHERE coalesce(rm.hl7_export_code, '') != '' group by rm.center_id
				  ) as srm ON (srm.center_id = COALESCE(pr.center_id, isr.center_id))
		WHERE s.service_id = NEW.service_id AND hli.status = 'A'
	LOOP

	IF rec.equipment_code_required = 'N' OR NEW.appointment_id != 0 OR rec.hl7_export_code != '' THEN

		IF (TG_OP = 'INSERT') THEN
			INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, bill_paid, op_code, center_id, hl7_lab_interface_id)
				VALUES ('SERVICE', NEW.prescription_id, current_timestamp, rec.interface_name, 'N', 'N', rec.centerid, rec.hl7_lab_interface_id);
		ELSIF (TG_OP = 'UPDATE') THEN
		-- check if the order has not been sent yet
			IF OLD.conducted != 'X' AND NEW.conducted = 'X' THEN
				INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, bill_paid, op_code, center_id, hl7_lab_interface_id)
					VALUES ('SERVICE', NEW.prescription_id, current_timestamp, rec.interface_name, 'N', 'C', rec.centerid, rec.hl7_lab_interface_id);
			END IF;
		END IF;
	END IF;
	END LOOP;
RETURN NEW;
END;
$function$;


DROP TRIGGER IF EXISTS hl7_save_service_export_items_trigger ON services_prescribed;
CREATE TRIGGER hl7_save_service_export_items_trigger 
AFTER INSERT OR UPDATE ON services_prescribed
	FOR EACH ROW EXECUTE PROCEDURE hl7_save_service_export_items();

