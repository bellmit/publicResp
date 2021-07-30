-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-functions-4 splitStatements:false
-- validCheckSum: ANY
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:t select count(*) > 0 from information_schema.columns where table_schema = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') and table_name='patient_deposits' and column_name='package_id'

-- Pharma Deposits setoff migrations
CREATE OR REPLACE FUNCTION migrate_pharma_deposits_setoff(start_open_date date, end_open_date date)

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
	receipt_query text;
	unallocated_amount  numeric;

BEGIN


FOR records IN SELECT b.bill_no, mr_no, deposit_set_off, COALESCE(ip_deposit_set_off,0) AS ip_deposit_set_off,b.visit_type, b.restriction_type
				FROM bill b 
				JOIN patient_registration pr ON pr.patient_id=b.visit_id 
				LEFT JOIN (SELECT DISTINCT bill_no FROM store_sales_main) ssm ON ssm.bill_no = b.bill_no 
				WHERE (deposit_set_off > 0 OR ip_deposit_set_off > 0) AND
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
				ORDER BY restriction_type, COALESCE(b.finalized_date, b.mod_time)

LOOP
	ipdeposit_set:= records.ip_deposit_set_off;
	deposit_set := records.deposit_set_off - records.ip_deposit_set_off ;
	
	
    IF records.visit_type = 'i' THEN
		receipt_query := 'SELECT *, COALESCE(br.bill_receipt_id, 0) AS bill_receipt_id FROM 
		((SELECT r.receipt_id AS receiptid, r.unallocated_amount AS unallocatedamount, entity_id,
				payment_mode_id, realized  
		FROM receipts r 
		JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id
		WHERE r.mr_no = $1 AND r.unallocated_amount > 0 AND r.is_deposit AND entity_id IN (''P'',''i'') AND receipt_type = ''R'' ) 
		UNION ALL 
		(SELECT r.receipt_id AS receiptid, r.unallocated_amount AS unallocatedamount, COALESCE (entity_id, ''b'') AS entity_id,
				payment_mode_id, realized   
		FROM receipts r 
		LEFT JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id
		WHERE r.mr_no = $2 AND r.unallocated_amount > 0 AND r.is_deposit AND entity_id IS NULL AND receipt_type = ''R'' )) AS receipts 
		LEFT JOIN bill_receipts br ON br.receipt_no = receipts.receiptid AND br.bill_no = $3 ORDER BY receipts.entity_id DESC, receipts.receiptid ASC';
	ELSE
		receipt_query := 'SELECT *, COALESCE(br.bill_receipt_id, 0) AS bill_receipt_id FROM 
		((SELECT r.receipt_id AS receiptid, r.unallocated_amount AS unallocatedamount, entity_id,
				payment_mode_id, realized  
		FROM receipts r 
		JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id
		WHERE r.mr_no= $1 AND r.unallocated_amount > 0 AND r.is_deposit AND entity_id IN (''P'',''i'') AND receipt_type = ''R'' ) 
		UNION ALL 
		(SELECT r.receipt_id AS receiptid, r.unallocated_amount AS unallocatedamount, COALESCE (entity_id, ''b'') AS entity_id,
				payment_mode_id, realized   
		FROM receipts r 
		LEFT JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id
		WHERE r.mr_no= $2 AND r.unallocated_amount > 0 AND r.is_deposit AND entity_id IS NULL AND receipt_type = ''R'' )) AS receipts 
		LEFT JOIN bill_receipts br ON br.receipt_no = receipts.receiptid AND br.bill_no = $3 ORDER BY receipts.entity_id ASC, receipts.receiptid ASC';
	END IF;

	FOR receipt_records IN EXECUTE receipt_query USING records.mr_no, records.mr_no, records.bill_no
	LOOP
		-- IF PAYMENT MODE IS CHEQUE AND REALIZED IS NO, THEN EXIT
		IF (receipt_records.payment_mode_id = 3 AND receipt_records.realized = 'N') THEN
			EXIT;
		END IF;
		unallocated_amount := receipt_records.unallocatedamount;
		IF(records.visit_type='i' AND ipdeposit_set > 0 AND receipt_records.entity_id = 'i') THEN
			IF(ipdeposit_set >= unallocated_amount) THEN
			    SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, unallocated_amount) INTO updateresult;
				SELECT update_unallocated_amount(receipt_records.receiptid,0) INTO updateresult;
				ipdeposit_set:= ipdeposit_set - unallocated_amount;
				unallocated_amount := 0;
			ELSE
				unallocated_amount:=unallocated_amount-ipdeposit_set;
				SELECT update_unallocated_amount(receipt_records.receiptid,unallocated_amount) INTO updateresult;
		        SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, ipdeposit_set) INTO updateresult;

				ipdeposit_set :=0;
			END IF;
		 END IF;
		 
		 IF(deposit_set > 0  AND  receipt_records.entity_id = 'i' AND records.visit_type = 'i' AND records.restriction_type != 'P') THEN
				IF(deposit_set >= unallocated_amount) THEN
					SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, unallocated_amount) INTO updateresult;
					SELECT update_unallocated_amount(receipt_records.receiptid,0) INTO updateresult;
					deposit_set:= deposit_set - unallocated_amount;
					unallocated_amount := 0;
				ELSE
					unallocated_amount:=unallocated_amount-deposit_set;
					SELECT update_unallocated_amount(receipt_records.receiptid,unallocated_amount) INTO updateresult;
					SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, deposit_set) INTO updateresult;
					deposit_set := 0;
				END IF;
			END IF;
			
		  IF(deposit_set > 0  AND receipt_records.entity_id != 'i') THEN
			IF(deposit_set >= unallocated_amount) THEN
				SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, unallocated_amount) INTO updateresult;
				SELECT update_unallocated_amount(receipt_records.receiptid,0) INTO updateresult;
				deposit_set:= deposit_set - unallocated_amount;
				unallocated_amount := 0;
			ELSE
				unallocated_amount:=unallocated_amount-deposit_set;
				SELECT update_unallocated_amount(receipt_records.receiptid,unallocated_amount) INTO updateresult;
				SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, deposit_set) INTO updateresult;
				deposit_set := 0;
			END IF;
		END IF;
		
		
		  IF(deposit_set > 0  AND receipt_records.entity_id = 'i' AND records.restriction_type = 'P') THEN
			IF(deposit_set >= unallocated_amount) THEN
				SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, unallocated_amount) INTO updateresult;
				SELECT update_unallocated_amount(receipt_records.receiptid,0) INTO updateresult;
				deposit_set := deposit_set - unallocated_amount;
				unallocated_amount := 0;
			ELSE
				unallocated_amount:=unallocated_amount-deposit_set;
				SELECT update_unallocated_amount(receipt_records.receiptid,unallocated_amount) INTO updateresult;
				SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records.bill_no, receipt_records.bill_receipt_id, deposit_set) INTO updateresult;
				deposit_set := 0;
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


-- function to iterate pharmacy bills in month wise
CREATE OR REPLACE FUNCTION iterate_patient_pharmacy_bill_with_deposits()
RETURNS void AS $BODY$
DECLARE
	openBillDates RECORD;
	updateresult text;
	
BEGIN
	FOR openBillDates IN SELECT (date_part('year',max_open_date)||'-'||date_part('month',max_open_date)||'-01')::date AS start_date, max_open_date AS end_date FROM 
							(SELECT  MAX(open_date)::date AS max_open_date FROM bill b
							JOIN (SELECT DISTINCT bill_no FROM store_sales_main) ssm ON ssm.bill_no = b.bill_no 
							WHERE (deposit_set_off > 0 OR ip_deposit_set_off > 0)
							GROUP BY (date_part('year',open_date)||'-'||date_part('month',open_date))) AS foo ORDER BY start_date
	LOOP
		SELECT migrate_pharma_deposits_setoff(openBillDates.start_date,openBillDates.end_date) INTO updateresult;
	END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';

--Execute iteration function for pharmacy bills
SELECT iterate_patient_pharmacy_bill_with_deposits();
