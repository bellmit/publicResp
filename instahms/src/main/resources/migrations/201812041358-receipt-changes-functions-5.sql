-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-functions-5 splitStatements:false
-- validCheckSum: ANY
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:t select count(*) > 0 from information_schema.columns where table_schema = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') and table_name='patient_deposits' and column_name='package_id'

-- Hospital Deposits setoff migrations
CREATE OR REPLACE FUNCTION migrate_hospital_deposits_setoff(start_open_date date, end_open_date date)

RETURNS VOID AS $BODY$
DECLARE
	records RECORD;
	ipdeposit_set numeric;
	deposit_set numeric;
	receiptid text;
	unallocatedamount numeric;
	pack_unallocatedamount numeric;
	receipt_records RECORD;
	package_set_offs numeric;
	deposit_set_off_amount numeric;
	updateresult text;
	receipt_order_by text;
	

BEGIN

FOR records IN SELECT mr_no, deposit_set_off, ip_deposit_set_off,b.visit_type, b.bill_no 
				FROM bill b 
				JOIN patient_registration pr ON pr.patient_id=b.visit_id 
				LEFT JOIN (SELECT DISTINCT bill_no FROM store_sales_main) ssm ON ssm.bill_no = b.bill_no 
				WHERE (deposit_set_off > 0 OR ip_deposit_set_off > 0) AND ssm.bill_no IS NULL AND
				(b.open_date::date >= start_open_date AND b.open_date::date <=end_open_date) AND
				NOT EXISTS (SELECT DISTINCT bc.bill_no, orders.package_id from bill_charge bc
							JOIN (
							SELECT sp.common_order_id AS common_order_id, p.package_id
							FROM services_prescribed sp JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id)
							JOIN pack_master p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)
							UNION ALL 
							SELECT tp.common_order_id as common_order_id, p.package_id
							FROM tests_prescribed tp JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id)
							JOIN pack_master p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)
							UNION ALL
							SELECT dc.common_order_id as common_order_id, p.package_id
							FROM doctor_consultation dc
							JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id)
							JOIN pack_master p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)
							LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC'
							LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id)
							UNION ALL
							SELECT osp.common_order_id as common_order_id, p.package_id
							FROM other_services_prescribed osp JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id)
							JOIN pack_master p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)
							) AS orders ON bc.order_number = orders.common_order_id WHERE b.bill_no = bc.bill_no)
				ORDER BY visit_type 

LOOP
	ipdeposit_set:= records.ip_deposit_set_off;
	deposit_set := records.deposit_set_off - records.ip_deposit_set_off;
	IF records.visit_type = 'i' THEN
		receipt_order_by := ' receipts.entity_id DESC, receipts.receiptid ASC';
	ELSE
		receipt_order_by := ' receipts.entity_id ASC, receipts.receiptid ASC';
	END IF;
	
	FOR receipt_records IN SELECT * , COALESCE(br.bill_receipt_id, 0) AS bill_receipt_id FROM 
		((SELECT r.receipt_id AS receiptid, r.unallocated_amount AS unallocatedamount, entity_id
		FROM receipts r 
		JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id
		WHERE r.mr_no=records.mr_no AND r.unallocated_amount > 0 AND r.is_deposit AND entity_id IN ('H','i') AND receipt_type = 'R' ) 
		UNION ALL 
		(SELECT r.receipt_id AS receiptid, r.unallocated_amount AS unallocatedamount, COALESCE (entity_id, 'b') AS entity_id 
		FROM receipts r 
		LEFT JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id
		WHERE r.mr_no=records.mr_no AND r.unallocated_amount > 0 AND r.is_deposit AND entity_id IS NULL AND receipt_type = 'R' )) AS receipts 
		LEFT JOIN bill_receipts br ON br.receipt_no = receipts.receiptid AND br.bill_no = records.bill_no ORDER BY receipt_order_by
	LOOP
		
		IF(records.visit_type='i' AND ipdeposit_set > 0 AND receipt_records.entity_id = 'i') THEN
				
			IF(ipdeposit_set >= receipt_records.unallocatedamount) THEN
				SELECT update_unallocated_amount(receipt_records.receiptid,0) INTO updateresult;
				ipdeposit_set:= ipdeposit_set - receipt_records.unallocatedamount;
				SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, receipt_records.unallocatedamount) INTO updateresult;
			ELSE
				deposit_set_off_amount:=receipt_records.unallocatedamount-ipdeposit_set;
				SELECT update_unallocated_amount(receipt_records.receiptid,deposit_set_off_amount) INTO updateresult;
				SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, ipdeposit_set) INTO updateresult;
				ipdeposit_set:=0;
			END IF;
		 END IF;

		 IF(deposit_set > 0  AND receipt_records.entity_id != 'i') THEN
				
			IF(deposit_set >= receipt_records.unallocatedamount) THEN
				SELECT update_unallocated_amount(receipt_records.receiptid,0) INTO updateresult;
				deposit_set:= deposit_set - receipt_records.unallocatedamount;
			    SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, receipt_records.unallocatedamount) INTO updateresult;
			ELSE
				deposit_set_off_amount:=receipt_records.unallocatedamount-deposit_set;
				SELECT update_unallocated_amount(receipt_records.receiptid,deposit_set_off_amount) INTO updateresult;
				SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, deposit_set) INTO updateresult;
				deposit_set :=0;
			END IF;
		END IF;
		IF(deposit_set=0 AND ipdeposit_set=0) THEN
			EXIT;
		END IF;
	END LOOP;
END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';

-- function to iterate bills in month wise
CREATE OR REPLACE FUNCTION iterate_patient_bill_with_deposits()
RETURNS void AS $BODY$
DECLARE
	openBillDates RECORD;
	updateresult text;
	
BEGIN
	FOR openBillDates IN SELECT (date_part('year',max_open_date)||'-'||date_part('month',max_open_date)||'-01')::date AS start_date, max_open_date AS end_date FROM 
							(SELECT  MAX(open_date)::date AS max_open_date FROM bill b
								LEFT JOIN (SELECT DISTINCT bill_no FROM store_sales_main) ssm 
							ON ssm.bill_no = b.bill_no 
							WHERE (deposit_set_off > 0 OR ip_deposit_set_off > 0) AND ssm.bill_no IS NULL
							GROUP BY (date_part('year',open_date)||'-'||date_part('month',open_date))) AS foo ORDER BY start_date
	LOOP
		--SELECT migrate_hospital_deposits_setoff(openBillDates.start_date,openBillDates.end_date) INTO updateresult;		
	END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';
 
-- EXECUTE ITERATION METHOD
--SELECT iterate_patient_bill_with_deposits();
-- This migration discarded, as, while migrating parmacy bill we have also migrated in non-pharmacy bills
