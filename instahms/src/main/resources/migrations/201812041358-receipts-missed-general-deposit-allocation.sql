-- liquibase formatted sql
-- changeset allabakash:Receipts-missed-general-deposit-allocation splitStatements:false
-- validCheckSum: ANY

-- Allocate Non package Deposit set off for missed deposit migration receipts. 
CREATE OR REPLACE FUNCTION general_deposit_bill_receipt_allocation()

RETURNS VOID AS $BODY$
DECLARE
	records RECORD;
	records_pharma RECORD;
	records_non_pharma RECORD;
	ipdeposit_set numeric;
	deposit_set numeric;
	deposit_set_off_amount numeric;
	receiptid text;
	unallocatedamount numeric;
	pack_unallocatedamount numeric;
	unmatched_records RECORD;
	unmatched_record_results RECORD;
	receipt_records RECORD;
	package_set_offs numeric;
	updateresult text;
	receipt_query text;
	unallocated_amount  numeric;
	receipt_order_by text;
	
BEGIN
	
	
	FOR unmatched_records IN SELECT DISTINCT mr_no FROM (SELECT b.bill_no, deposit_set_off, COALESCE(allocatedamount,0) AS allocatedamount, 
								pr.mr_no AS mr_no FROM bill b 
							JOIN patient_registration pr ON pr.patient_id=b.visit_id 
							LEFT JOIN (SELECT bill_no, sum(allocated_amount) AS allocatedamount 
								FROM receipts r JOIN bill_receipts br ON br.receipt_no = r.receipt_id 
								WHERE r.is_deposit AND tpa_id IS NULL 
								GROUP BY bill_no) billreceipts ON billreceipts.bill_no = b.bill_no 
							WHERE (deposit_set_off>0) 
							GROUP BY b.bill_no, COALESCE(deposit_set_off,0), allocatedamount, mr_no 
							HAVING COALESCE(allocatedamount,0) != deposit_set_off) AS foo
	LOOP
		INSERT INTO temp_deposit_mr_numbers VALUES (unmatched_records.mr_no);
		
		--reset the unallocated amount and set zero in unmatched bill_receipts.
		FOR receipt_records IN SELECT r.receipt_id AS receiptid, r.unallocated_amount, r.amount, COALESCE(br.bill_receipt_id, 0) as bill_receipt_id, COALESCE(br.allocated_amount, 0) as bill_receipt_allocated_amount  
				FROM  receipts r
				LEFT JOIN receipt_usage ru ON r.receipt_id = ru.receipt_id 
				LEFT JOIN bill_receipts br ON r.receipt_id = br.receipt_no
				WHERE mr_no=unmatched_records.mr_no 
				AND r.amount != r.unallocated_amount
				AND is_deposit AND ((ru.entity_type != 'package_id' and ru.entity_type != 'pat_package_id') or ru.entity_type is null) AND receipt_type = 'R'
				ORDER BY  r.receipt_id ASC, r.created_at ASC 
		LOOP
			-- set unallocated amount with amount
			SELECT update_unallocated_amount(receipt_records.receiptid, receipt_records.amount) INTO updateresult;
			-- set allocated_amount with zero
			UPDATE bill_receipts SET allocated_amount = 0 WHERE bill_receipt_id = receipt_records.bill_receipt_id;
		END LOOP;
		-- 
	END LOOP;
		
		--refund receipt amount correction 
	
	FOR receipt_records IN SELECT r.receipt_id AS receiptid, r.amount, COALESCE(r.unallocated_amount,0) AS unallocated_amount, 
		            COALESCE(receipt_refund.receipt_refunded_amount,0) AS receipt_refunded_amount, COALESCE(allocated_amount,0) as allocated_amount , 
		            r.amount-(COALESCE(receipt_refund.receipt_refunded_amount,0)+COALESCE(allocated_amount,0)) as final_unallocated_amount
					FROM receipts r 
					JOIN temp_deposit_mr_numbers tdm ON r.mr_no = tdm.mr_no
					LEFT JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id
					LEFT JOIN (SELECT receipt_id, SUM(amount) as receipt_refunded_amount FROM receipt_refund_reference group by receipt_id) AS receipt_refund ON r.receipt_id = receipt_refund.receipt_id 
					LEFT JOIN (SELECT receipt_no, SUM(allocated_amount) as allocated_amount FROM bill_receipts group by receipt_no) AS bill_receipts ON r.receipt_id = bill_receipts.receipt_no 
					WHERE is_deposit  AND receipt_type = 'R' 
					AND r.unallocated_amount != r.amount-(COALESCE(receipt_refund.receipt_refunded_amount,0)+COALESCE(allocated_amount,0)) 
					GROUP BY receiptid, receipt_refund.receipt_refunded_amount, allocated_amount 
					HAVING amount != r.unallocated_amount+COALESCE(receipt_refund.receipt_refunded_amount,0)+COALESCE(allocated_amount,0)
					ORDER BY  r.receipt_id ASC, r.created_at ASC 
		
	LOOP
			-- set unallocated amount with refunded_amount
			SELECT update_unallocated_amount(receipt_records.receiptid, receipt_records.final_unallocated_amount) INTO updateresult;		
	END LOOP;
	
	
	-- bill receipts allocation with unmatched_records.mr_no 
	FOR records_pharma IN SELECT b.bill_no, pr.mr_no, deposit_set_off, COALESCE(ip_deposit_set_off,0) AS ip_deposit_set_off,
					b.visit_type, b.restriction_type 
				FROM bill b 
				JOIN patient_registration pr ON pr.patient_id=b.visit_id AND deposit_set_off>0 
				JOIN temp_deposit_mr_numbers tdm ON pr.mr_no = tdm.mr_no
				LEFT JOIN (SELECT DISTINCT bill_no FROM store_sales_main) ssm ON ssm.bill_no = b.bill_no 
				WHERE NOT EXISTS (SELECT DISTINCT bc.bill_no, orders.package_id from bill_charge bc
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
				ORDER BY restriction_type, COALESCE(b.closed_date, b.finalized_date, b.mod_time)

	LOOP
		ipdeposit_set:= records_pharma.ip_deposit_set_off;
		deposit_set := records_pharma.deposit_set_off - records_pharma.ip_deposit_set_off ;
		
		
	    IF records_pharma.visit_type = 'i' THEN
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
	
		FOR receipt_records IN EXECUTE receipt_query USING records_pharma.mr_no, records_pharma.mr_no, records_pharma.bill_no
		LOOP
			-- IF PAYMENT MODE IS CHEQUE AND REALIZED IS NO, THEN EXIT
			IF (receipt_records.payment_mode_id = 3 AND receipt_records.realized = 'N') THEN
				EXIT;
			END IF;
			unallocated_amount := receipt_records.unallocatedamount;
			IF(records_pharma.visit_type='i' AND ipdeposit_set > 0 AND receipt_records.entity_id = 'i') THEN
				IF(ipdeposit_set >= unallocated_amount) THEN
				    SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records_pharma.bill_no, receipt_records.bill_receipt_id, unallocated_amount) INTO updateresult;
					SELECT update_unallocated_amount(receipt_records.receiptid,0) INTO updateresult;
					ipdeposit_set:= ipdeposit_set - unallocated_amount;
					unallocated_amount := 0;
				ELSE
					unallocated_amount:=unallocated_amount-ipdeposit_set;
					SELECT update_unallocated_amount(receipt_records.receiptid,unallocated_amount) INTO updateresult;
			        SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records_pharma.bill_no, receipt_records.bill_receipt_id, ipdeposit_set) INTO updateresult;
	
					ipdeposit_set :=0;
				END IF;
			 END IF;
			 
			 IF(deposit_set > 0  AND  receipt_records.entity_id = 'i' AND records_pharma.visit_type = 'i' AND records_pharma.restriction_type != 'P') THEN
				IF(deposit_set >= unallocated_amount) THEN
					SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records_pharma.bill_no, receipt_records.bill_receipt_id, unallocated_amount) INTO updateresult;
					SELECT update_unallocated_amount(receipt_records.receiptid,0) INTO updateresult;
					deposit_set:= deposit_set - unallocated_amount;
					unallocated_amount := 0;
				ELSE
					unallocated_amount:=unallocated_amount-deposit_set;
					SELECT update_unallocated_amount(receipt_records.receiptid,unallocated_amount) INTO updateresult;
					SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records_pharma.bill_no, receipt_records.bill_receipt_id, deposit_set) INTO updateresult;
					deposit_set := 0;
				END IF;
			END IF;
			 
			  IF(deposit_set > 0  AND receipt_records.entity_id != 'i') THEN
				IF(deposit_set >= unallocated_amount) THEN
					SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records_pharma.bill_no, receipt_records.bill_receipt_id, unallocated_amount) INTO updateresult;
					SELECT update_unallocated_amount(receipt_records.receiptid,0) INTO updateresult;
					deposit_set:= deposit_set - unallocated_amount;
					unallocated_amount := 0;
				ELSE
					unallocated_amount:=unallocated_amount-deposit_set;
					SELECT update_unallocated_amount(receipt_records.receiptid,unallocated_amount) INTO updateresult;
					SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records_pharma.bill_no, receipt_records.bill_receipt_id, deposit_set) INTO updateresult;
					deposit_set := 0;
				END IF;
			END IF;
			
			
			  IF(deposit_set > 0  AND receipt_records.entity_id = 'i' AND records_pharma.restriction_type = 'P') THEN
				IF(deposit_set >= unallocated_amount) THEN
					SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records_pharma.bill_no, receipt_records.bill_receipt_id, unallocated_amount) INTO updateresult;
					SELECT update_unallocated_amount(receipt_records.receiptid,0) INTO updateresult;
					deposit_set := deposit_set - unallocated_amount;
					unallocated_amount := 0;
				ELSE
					unallocated_amount:=unallocated_amount-deposit_set;
					SELECT update_unallocated_amount(receipt_records.receiptid,unallocated_amount) INTO updateresult;
					SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, records_pharma.bill_no, receipt_records.bill_receipt_id, deposit_set) INTO updateresult;
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



--create create_temp_table_mr_no, to store the unique mr_no to deposits 
CREATE OR REPLACE FUNCTION create_temp_table_mr_no() 
RETURNS VOID AS $BODY$ 
DECLARE
BEGIN
	CREATE TABLE IF NOT EXISTS temp_deposit_mr_numbers (
  		mr_no varchar(20) NOT NULL,
  		PRIMARY KEY (mr_no)
	);
END;
$BODY$
LANGUAGE 'plpgsql';

--DROP TALBE temp_deposit_mr_numbers 
CREATE OR REPLACE FUNCTION drop_temp_table_for_deposits() 
RETURNS VOID AS $BODY$ 
DECLARE
BEGIN
	--Drop temp_deposit_mr_numbers
	DROP TABLE IF EXISTS temp_deposit_mr_numbers CASCADE;

END;
$BODY$
LANGUAGE 'plpgsql';


--Create table function
SELECT create_temp_table_mr_no();


--EXECUTE CORRECT FUNCTION
SELECT general_deposit_bill_receipt_allocation();

--Drop temp table
select drop_temp_table_for_deposits();

-- function to update ip_deposit_setoff column of bill table
CREATE OR REPLACE FUNCTION update_deposit_and_ip_deposit_set_off() 
RETURNS VOID AS $BODY$ 
DECLARE
    billReceiptRecord RECORD;
BEGIN
 
 FOR billReceiptRecord IN SELECT
								br.bill_no,
								sum(br.allocated_amount) AS setoff_amount
							FROM
								bill_receipts br
								JOIN receipts r ON r.receipt_id = br.receipt_no
									AND r.is_deposit
								JOIN receipt_usage ru ON ru.entity_type = 'visit_type'
									AND ru.entity_id = 'i'
									AND r.receipt_id = ru.receipt_id
								GROUP BY
									br.bill_no
 
 LOOP 
    --update bill table
    UPDATE bill SET ip_deposit_set_off_old = ip_deposit_set_off_old||', '||billReceiptRecord.setoff_amount, 
    				ip_deposit_set_off = billReceiptRecord.setoff_amount 
				WHERE bill_no = billReceiptRecord.bill_no and ip_deposit_set_off != billReceiptRecord.setoff_amount;
 END LOOP;
   
END;
$BODY$
LANGUAGE 'plpgsql';

--EXECUTE bill ip_deposit_set_off column with ip receipt setoff, if incase any ip deposit set off happened for pharmacy bill, 
-- This function with update ip_deposit_set_off column
SELECT update_deposit_and_ip_deposit_set_off();



--EXECUTE migration_update_deposit_setoff_total 
SELECT migration_update_deposit_setoff_total();




