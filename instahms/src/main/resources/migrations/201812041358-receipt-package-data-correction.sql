-- liquibase formatted sql
-- changeset allabakash:Receipt-package-data-correction splitStatements:false
-- validCheckSum: ANY
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:t SELECT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') and table_name in ('patient_deposits_obsolete') and column_name='package_id_absolete')



-- clear package releated receipt's allocated_amount, unallocated_amount
CREATE OR REPLACE FUNCTION clear_package_bills_receipt_allocation()

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
						ON (b.bill_no = package_bills.bill_no) WHERE deposit_set_off >0 

LOOP
	package_set_offs:= package_records.deposit_set_off;
	FOR receipt_records IN SELECT r.receipt_id AS receiptid, r.unallocated_amount AS pack_unallocatedamount, r.amount, COALESCE(br.bill_no, '') as bill_no, COALESCE(br.allocated_amount, 0) as bill_receipt_allocated_amount  
			FROM  receipts r 
			JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id
			JOIN bill_receipts br ON r.receipt_id = br.receipt_no AND br.bill_no = package_records.bill_no
			WHERE mr_no=package_records.package_mr_no 
			AND is_deposit AND entity_type = 'package_id' AND entity_id = package_records.setoff_package_id AND receipt_type = 'R'
			ORDER BY  r.receipt_id ASC, r.created_at ASC 
	LOOP
		IF(package_set_offs>0) THEN
			-- set unallocated amount with amount
			SELECT update_unallocated_amount(receipt_records.receiptid, receipt_records.amount) INTO updateresult;
			-- set allocated_amount with zero
			UPDATE bill_receipts SET allocated_amount = 0 WHERE bill_no = receipt_records.bill_no;
		END IF;
	END LOOP;
END LOOP;

END;
$BODY$
LANGUAGE 'plpgsql';


--EXECUTE MIGRATION METHOD
SELECT clear_package_bills_receipt_allocation();

--EXECUTE receipt_refund amount correction
SELECT correct_receipt_refunded_amount_with_receipt();


-------------------------------------XXXX-----------------------------------------------
-- re-migrate package deposits setoff
CREATE OR REPLACE FUNCTION migrate_package_deposits_setoff()

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
						ON (b.bill_no = package_bills.bill_no) WHERE deposit_set_off >0
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
SELECT migrate_package_deposits_setoff();
