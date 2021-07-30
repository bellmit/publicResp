-- liquibase formatted sql
-- changeset allabakash:Receipts-missed-a-packages-allocation splitStatements:false
-- validCheckSum: ANY

-- clear package releated receipt's allocated_amount, unallocated_amount
CREATE OR REPLACE FUNCTION clear_package_excess_deposit_allocation()

RETURNS VOID AS $BODY$
DECLARE
	records RECORD;
	ipdeposit_set numeric;
	deposit_set numeric;
	deposit_set_off_amount numeric;
	receiptid text;
	unallocatedamount numeric;
	pack_unallocatedamount numeric;
	package_records RECORD;
	receipt_records RECORD;
	package_set_offs numeric;
	updateresult text;
BEGIN
	
FOR package_records IN SELECT DISTINCT mr_no FROM 
		(SELECT br.bill_no, SUM(allocated_amount) allocated_amount, b.deposit_set_off, r.mr_no, entity_id FROM receipts r 
		JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id 
		JOIN bill_receipts br ON r.receipt_id=br.receipt_no 
		JOIN bill b ON br.bill_no=b.bill_no 
		WHERE b.deposit_set_off>0 AND is_deposit AND entity_type='pat_package_id' 
		GROUP BY br.bill_no,b.deposit_set_off,r.mr_no,ru.entity_id 
		HAVING SUM(br.allocated_amount)>b.deposit_set_off order by br.bill_no) as foo

LOOP

	INSERT INTO temp_package_mr_numbers VALUES (package_records.mr_no);
	
	FOR receipt_records IN SELECT r.receipt_id AS receiptid, r.amount, br.bill_no as bill_no  
			FROM receipts r 
			JOIN receipt_usage ru ON r.receipt_id = ru.receipt_id
			JOIN bill_receipts br ON r.receipt_id = br.receipt_no
			WHERE mr_no=package_records.mr_no 
			AND is_deposit AND entity_type = 'package_id' AND receipt_type = 'R'
			ORDER BY  r.receipt_id ASC, r.created_at ASC 		 
	LOOP
			-- set unallocated amount with amount
			SELECT update_unallocated_amount(receipt_records.receiptid, receipt_records.amount) INTO updateresult;
			-- set allocated_amount with zero
			UPDATE bill_receipts SET allocated_amount = 0 WHERE bill_no = receipt_records.bill_no;
	END LOOP;
END LOOP;

END;
$BODY$
LANGUAGE 'plpgsql';

--create temp_package_mr_numbers, to store the unique mr_no to process package deposits setoff 
CREATE OR REPLACE FUNCTION create_temp_table_for_package() 
RETURNS VOID AS $BODY$ 
DECLARE
BEGIN
	CREATE TABLE IF NOT EXISTS temp_package_mr_numbers (
  		mr_no varchar(20) NOT NULL,
  		PRIMARY KEY (mr_no)
	);
END;
$BODY$
LANGUAGE 'plpgsql';

--DROP TALBE temp_package_mr_numbers 
CREATE OR REPLACE FUNCTION drop_temp_table_for_package() 
RETURNS VOID AS $BODY$ 
DECLARE
BEGIN
	--Drop temp_package_mr_numbers
	DROP TABLE IF EXISTS temp_package_mr_numbers CASCADE;

END;
$BODY$
LANGUAGE 'plpgsql';


--Create table function
SELECT create_temp_table_for_package();

--EXECUTE MIGRATION METHOD
SELECT clear_package_excess_deposit_allocation();

--EXECUTE receipt_refund amount correction
SELECT correct_receipt_refunded_amount_with_receipt();



-------------------------------------XXXX-----------------------------------------------
-- migrate_cleared_package_deposits_setoff
CREATE OR REPLACE FUNCTION migrate_cleared_package_deposits_setoff()

RETURNS VOID AS $BODY$
DECLARE
	records RECORD;
	ipdeposit_set numeric;
	deposit_set numeric;
	deposit_set_off_amount numeric;
	receiptid text;
	unallocatedamount numeric;
	pack_unallocatedamount numeric;
	package_records RECORD;
	receipt_records RECORD;
	package_set_offs numeric;
	updateresult text;
BEGIN
	
FOR package_records IN SELECT r.mr_no AS package_mr_no, package_bills.package_id::text AS setoff_package_id, b.deposit_set_off AS deposit_set_off, b.bill_no
						FROM bill b JOIN patient_registration r ON (b.visit_id = r.patient_id) 
						JOIN temp_package_mr_numbers tpmr ON r.mr_no = tpmr.mr_no 
						JOIN (
							SELECT DISTINCT bc.bill_no, orders.package_id from bill_charge bc
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
							) AS orders ON bc.order_number = orders.common_order_id
						) AS package_bills
						ON (b.bill_no = package_bills.bill_no) 
						WHERE deposit_set_off >0
						ORDER BY COALESCE(b.finalized_date, b.mod_time)

LOOP
	package_set_offs:= package_records.deposit_set_off;

	FOR receipt_records IN SELECT r.receipt_id AS receiptid, r.unallocated_amount AS pack_unallocatedamount, COALESCE(br.bill_receipt_id, 0) as bill_receipt_id,
				payment_mode_id, realized   
			FROM  receipts r 
			JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id
			LEFT JOIN bill_receipts br ON r.receipt_id = br.receipt_no AND br.bill_no = package_records.bill_no
			WHERE mr_no=package_records.package_mr_no AND r.unallocated_amount > 0 
			AND is_deposit AND entity_type = 'package_id' AND entity_id = package_records.setoff_package_id 
			ORDER BY  r.receipt_id ASC, r.created_at ASC 
	LOOP
		-- IF PAYMENT MODE IS CHEQUE AND REALIZED IS NO, THEN EXIT
		IF (receipt_records.payment_mode_id = 3 AND receipt_records.realized = 'N') THEN
			EXIT;
		END IF;
		IF(package_set_offs>0) THEN
			IF(package_set_offs >= receipt_records.pack_unallocatedamount) THEN
				SELECT update_unallocated_amount(receipt_records.receiptid,0) INTO updateresult;
				package_set_offs:= package_set_offs - receipt_records.pack_unallocatedamount ;
			    SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, package_records.bill_no, receipt_records.bill_receipt_id, receipt_records.pack_unallocatedamount) INTO updateresult;
			ELSE
				deposit_set_off_amount:=receipt_records.pack_unallocatedamount-package_set_offs;
				SELECT update_unallocated_amount(receipt_records.receiptid,deposit_set_off_amount) INTO updateresult;
			    SELECT update_billreceipt_allocated_amount(receipt_records.receiptid, package_records.bill_no, receipt_records.bill_receipt_id, package_set_offs) INTO updateresult;
				package_set_offs :=0;
			END IF;
		END IF;
		IF(package_set_offs=0) THEN
			EXIT;
		END IF;
	END LOOP;
END LOOP;

END;
$BODY$
LANGUAGE 'plpgsql';

--EXECUTE MIGRATION METHOD
SELECT migrate_cleared_package_deposits_setoff();

--Drop temp table
SELECT drop_temp_table_for_package();

--EXECUTE migration_update_deposit_setoff_total 
SELECT migration_update_deposit_setoff_total();




