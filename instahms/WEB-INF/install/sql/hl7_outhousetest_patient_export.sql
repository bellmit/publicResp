DROP FUNCTION IF EXISTS hl7_outhouse_tests_visit_details_set_export() CASCADE;
CREATE OR REPLACE FUNCTION hl7_outhouse_tests_visit_details_set_export() RETURNS TRIGGER AS $BODY$

DECLARE
	rec RECORD;
	need_export boolean;
	item_id text;
	status text;

BEGIN
	IF (TG_OP = 'INSERT') THEN
		item_id := NEW.patient_id;
		need_export := true;
	ELSEIF (TG_OP = 'UPDATE') THEN
		item_id := OLD.patient_id;
		IF (OLD.sponsor_id != NEW.sponsor_id 
			OR OLD.insurance_co !=  NEW.insurance_co
			OR OLD.plan_id != NEW.plan_id
			OR OLD.plan_type_id != NEW.plan_type_id) THEN
				need_export := true;
		END IF;
	ELSEIF (TG_OP = 'DELETE') THEN
		item_id := OLD.patient_id;		
		need_export := true;
	END IF;
	
	IF need_export THEN
		FOR rec IN
		 SELECT om.hl7_interface as interface_name, hci.center_id as centerid, om.hl7_lab_interface_id
			FROM tests_prescribed tp 
			JOIN tests_prescribed tpcur ON(tpcur.prescribed_id = tp.curr_location_presc_id)
			JOIN diag_outsource_master dom ON (tpcur.outsource_dest_id = dom.outsource_dest_id)
			JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)
			LEFT JOIN patient_registration pr ON (pr.patient_id = tpcur.pat_id)
			LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tpcur.pat_id)
			JOIN hl7_center_interfaces hci ON (hci.interface_name = om.hl7_interface AND 
							((coalesce(isr.center_id, pr.center_id) = hci.center_id) OR (0 = hci.center_id)))
			WHERE tp.pat_id = item_id and om.protocol ='hl7'
			GROUP BY  om.hl7_interface,hci.center_id, om.hl7_lab_interface_id
		LOOP
		
			INSERT INTO hl7_export_items (item_type,item_id,inserted_ts,interface_name, bill_paid, op_code, center_id, hl7_lab_interface_id) 
			VALUES ('VISIT', item_id,current_timestamp, rec.interface_name, 'Y', 'VM', rec.centerid, rec.hl7_lab_interface_id);
			
		END	LOOP;
	END IF;
	return NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS hl7_outhouse_tests_visit_details_set_export ON patient_insurance_plans;
CREATE TRIGGER hl7_outhouse_tests_visit_details_set_export
AFTER INSERT OR UPDATE OR DELETE ON patient_insurance_plans
FOR EACH ROW
EXECUTE PROCEDURE hl7_outhouse_tests_visit_details_set_export();


DROP FUNCTION IF EXISTS hl7_outhouse_tests_visit_details_memberid_set_export() CASCADE;
CREATE OR REPLACE FUNCTION hl7_outhouse_tests_visit_details_memberid_set_export() RETURNS TRIGGER AS $BODY$

DECLARE
	rec RECORD;
	need_export boolean;
	item_id text;
	status text;

BEGIN
	IF (TG_OP = 'INSERT') THEN
		item_id := NEW.visit_id;
		need_export := true;
			
	ELSEIF (TG_OP = 'UPDATE') THEN
		item_id := OLD.visit_id;
		 IF (OLD.member_id != NEW.member_id) THEN
					need_export := true;
				END IF;
	ELSEIF (TG_OP = 'DELETE') THEN
		item_id := OLD.visit_id;		
				need_export := true;
	END IF;
	
	IF need_export THEN
		FOR rec IN
		 SELECT om.hl7_interface as interface_name, hci.center_id as centerid, om.hl7_lab_interface_id
			FROM tests_prescribed tp 
			JOIN tests_prescribed tpcur ON(tpcur.prescribed_id = tp.curr_location_presc_id)
			JOIN diag_outsource_master dom ON (tpcur.outsource_dest_id = dom.outsource_dest_id)
			JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)
			LEFT JOIN patient_registration pr ON (pr.patient_id = tpcur.pat_id)
			LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tpcur.pat_id)
			JOIN hl7_center_interfaces hci ON (hci.interface_name = om.hl7_interface AND 
							((coalesce(isr.center_id, pr.center_id) = hci.center_id) OR (0 = hci.center_id)))
			WHERE tp.pat_id = item_id and om.protocol ='hl7'
			GROUP BY  om.hl7_interface,hci.center_id, om.hl7_lab_interface_id 
		LOOP
					
			INSERT INTO hl7_export_items (item_type,item_id,inserted_ts,interface_name, bill_paid, op_code, center_id, hl7_lab_interface_id) 
			VALUES ('VISIT', item_id,current_timestamp, rec.interface_name, 'Y', 'VM', rec.centerid, rec.hl7_lab_interface_id);
			
		END	LOOP;
	END IF;
	
	return NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS hl7_outhouse_tests_visit_details_memberid_set_export ON patient_policy_details;
CREATE TRIGGER hl7_outhouse_tests_visit_details_memberid_set_export
AFTER INSERT OR UPDATE OR DELETE ON patient_policy_details
FOR EACH ROW
EXECUTE PROCEDURE hl7_outhouse_tests_visit_details_memberid_set_export();


DROP FUNCTION IF EXISTS hl7_patient_export() CASCADE;
CREATE OR REPLACE FUNCTION hl7_patient_export() RETURNS TRIGGER AS $BODY$
 
DECLARE
    rec record;
    need_export boolean;
    is_hl7_protocol boolean;
 
BEGIN
	SELECT count(*) > 0 INTO is_hl7_protocol from tests_prescribed tp
		JOIN tests_prescribed tpcur ON(tpcur.prescribed_id = tp.curr_location_presc_id)
		JOIN diag_outsource_master dom ON (tpcur.outsource_dest_id = dom.outsource_dest_id)
		JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)
		WHERE tpcur.mr_no = NEW.mr_no AND om.protocol ='hl7' LIMIT 1; 
		
    FOR rec IN SELECT * from insta_integration ii
                        WHERE ii.integration_name = 'hl7_patient_export_server'
    LOOP
    	IF (is_hl7_protocol AND (NEW.mod_time != OLD.mod_time)) THEN
			need_export := true;
		END IF;
    	IF need_export THEN
        	INSERT INTO hl7_export_patient(integration_id, item_type, item_id) VALUES (rec.integration_id, 'PATIENT', NEW.mr_no);
        END IF;
    END LOOP;
    return NEW;
END;
$BODY$ LANGUAGE plpgsql;
 
DROP TRIGGER IF EXISTS hl7_patient_export_trigger ON patient_demographics_mod;
CREATE TRIGGER hl7_patient_export_trigger
AFTER UPDATE ON patient_demographics_mod
FOR EACH ROW
EXECUTE PROCEDURE hl7_patient_export();
