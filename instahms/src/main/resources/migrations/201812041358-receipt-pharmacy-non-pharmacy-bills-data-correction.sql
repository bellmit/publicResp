-- liquibase formatted sql
-- changeset allabakash:Receipt-pharmacy-non-pharmacy-bills-data-correction splitStatements:false
-- validCheckSum: ANY
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:t SELECT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') and table_name in ('patient_deposits','patient_deposits_obsolete') and column_name='package_id_absolete');

CREATE OR REPLACE FUNCTION correct_bill_ip_deposit_set_off()
	
RETURNS VOID AS $BODY$
DECLARE
	
BEGIN
	-- correct depost_set_off and ip_deposit_set_off columns of bills table
	UPDATE
		bill b
	SET
		ip_deposit_set_off = dso
	FROM (
		SELECT
			bill_no,
			deposit_set_off AS dso
		FROM
			bill
		WHERE
			visit_type = 'i'
			AND deposit_set_off > 0
			AND ip_deposit_set_off IS NOT NULL
		GROUP BY
			bill_no,
			deposit_set_off
		HAVING
			deposit_set_off * 2 = ip_deposit_set_off) AS foo
	WHERE
		foo.bill_no = b.bill_no; 

END;
$BODY$
LANGUAGE 'plpgsql';

--EXECUTE CORRECT BILL'S ip_deposit_set_off column
SELECT correct_bill_ip_deposit_set_off();

--EXECUTE receipt_refund amount correction -- This will adjust unallocated amount, if any difference found.
SELECT correct_receipt_refunded_amount_with_receipt();


-- clear pharmacy receipts and Non-pharmacy receipts which has data missmatch
CREATE OR REPLACE FUNCTION correct_pharmacy_non_pharmacy_receipt_allocation()

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
	
	
FOR unmatched_records IN SELECT r.mr_no,SUM(amount) as amount, SUM(case when r.unallocated_amount>0 then r.unallocated_amount else 0 end) AS receipt_unallocated_amount,hosp_total_deposits,hosp_total_setoffs,hosp_total_balance 
				FROM receipts r LEFT JOIN deposit_setoff_total dst ON r.mr_no=dst.mr_no 
				WHERE r.is_deposit 
				GROUP BY r.mr_no,hosp_total_deposits,hosp_total_setoffs,hosp_total_balance
				HAVING SUM(case when r.unallocated_amount>0 then r.unallocated_amount else 0 end)!=hosp_total_balance AND SUM(amount)!=SUM(case when r.unallocated_amount>0 then r.unallocated_amount else 0 end)
LOOP
	
	--reset the unallocated amount and set zero in unmatched bill_receipts.
	FOR receipt_records IN SELECT r.receipt_id AS receiptid, r.unallocated_amount, r.amount, COALESCE(br.bill_receipt_id, 0) as bill_receipt_id, COALESCE(br.allocated_amount, 0) as bill_receipt_allocated_amount  
			FROM  receipts r
			LEFT JOIN receipt_usage ru ON r.receipt_id = ru.receipt_id 
			LEFT JOIN bill_receipts br ON r.receipt_id = br.receipt_no
			WHERE mr_no=unmatched_records.mr_no 
			AND is_deposit AND ((ru.entity_type != 'package_id' and ru.entity_type != 'pat_package_id') or ru.entity_type is null) AND receipt_type = 'R'
			ORDER BY  r.receipt_id ASC, r.created_at ASC 
	LOOP
		IF(receipt_records.amount != receipt_records.unallocated_amount) THEN
			-- set unallocated amount with amount
			SELECT update_unallocated_amount(receipt_records.receiptid, receipt_records.amount) INTO updateresult;
			-- set allocated_amount with zero
			UPDATE bill_receipts SET allocated_amount = 0 WHERE bill_receipt_id = receipt_records.bill_receipt_id;
		END IF;
	END LOOP;
	-- 
	
	--refund receipt amount correction 

	FOR receipt_records IN SELECT r.receipt_id AS receiptid, r.amount, COALESCE(r.unallocated_amount,0) AS unallocated_amount, 
	            COALESCE(receipt_refund.receipt_refunded_amount,0) AS receipt_refunded_amount, COALESCE(allocated_amount,0) as allocated_amount , r.amount-(COALESCE(receipt_refund.receipt_refunded_amount,0)+COALESCE(allocated_amount,0)) as final_unallocated_amount
				FROM receipts r 
				LEFT JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id
				LEFT JOIN (SELECT receipt_id, SUM(amount) as receipt_refunded_amount FROM receipt_refund_reference group by receipt_id) AS receipt_refund ON r.receipt_id = receipt_refund.receipt_id 
				LEFT JOIN (SELECT receipt_no, SUM(allocated_amount) as allocated_amount FROM bill_receipts group by receipt_no) AS bill_receipts ON r.receipt_id = bill_receipts.receipt_no 
				WHERE is_deposit  AND receipt_type = 'R' AND r.mr_no = unmatched_records.mr_no
				GROUP BY receiptid, receipt_refund.receipt_refunded_amount, allocated_amount 
				HAVING amount != r.unallocated_amount+COALESCE(receipt_refund.receipt_refunded_amount,0)+COALESCE(allocated_amount,0)
				ORDER BY  r.receipt_id ASC, r.created_at ASC 
	
	LOOP
			IF(receipt_records.unallocated_amount != receipt_records.final_unallocated_amount) THEN
				-- set unallocated amount with refunded_amount
				SELECT update_unallocated_amount(receipt_records.receiptid, receipt_records.final_unallocated_amount) INTO updateresult;
			END IF;		
	END LOOP;

	
	-- Pharmacy allocate pharmacy bill receipts with unmatched_records.mr_no 
	FOR records_pharma IN SELECT b.bill_no, mr_no, deposit_set_off, COALESCE(ip_deposit_set_off,0) AS ip_deposit_set_off,b.visit_type, b.restriction_type 
				FROM bill b 
				JOIN patient_registration pr ON pr.patient_id=b.visit_id AND pr.mr_no = unmatched_records.mr_no
				LEFT JOIN (SELECT DISTINCT bill_no FROM store_sales_main) ssm ON ssm.bill_no = b.bill_no 
				WHERE (deposit_set_off > 0 OR ip_deposit_set_off > 0) AND
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
				ORDER BY restriction_type, COALESCE (b.closed_date,b.finalized_date,b.mod_time)

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
			
END LOOP;



END;
$BODY$
LANGUAGE 'plpgsql';


--EXECUTE CORRECT FUNCTION
SELECT correct_pharmacy_non_pharmacy_receipt_allocation();



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
    UPDATE bill SET ip_deposit_set_off_old = ip_deposit_set_off_old||', '||billReceiptRecord.setoff_amount, ip_deposit_set_off = billReceiptRecord.setoff_amount WHERE bill_no = billReceiptRecord.bill_no and ip_deposit_set_off != billReceiptRecord.setoff_amount;
 END LOOP;
   
END;
$BODY$
LANGUAGE 'plpgsql';

--EXECUTE bill ip_deposit_set_off column with ip receipt setoff
SELECT update_deposit_and_ip_deposit_set_off();




