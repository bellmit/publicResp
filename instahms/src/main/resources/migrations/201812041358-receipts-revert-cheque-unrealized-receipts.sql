-- liquibase formatted sql
-- changeset allabakash:Receipts-revert-cheque-unrealized-receipts splitStatements:false
-- validCheckSum: ANY

-- Revert Unrealized cheque payment deposit 
CREATE OR REPLACE FUNCTION revert_cheque_unrealized_receipts_allocations()

RETURNS VOID AS $BODY$
DECLARE
	
	receipt_records RECORD;
	updateresult text;
	receipt_id text;
	unallocated_amount  numeric;
	allocated_amount  numeric;
	receipt_order_by text;
	
BEGIN
	unallocated_amount := 0;
	allocated_amount := 0;
	receipt_id := '';
	FOR receipt_records IN SELECT r.receipt_id AS receiptid, r.unallocated_amount, r.amount, COALESCE(br.bill_receipt_id, 0) as bill_receipt_id, COALESCE(br.allocated_amount, 0) as bill_receipt_allocated_amount  , br.bill_no 
			FROM  receipts r
			LEFT JOIN receipt_usage ru ON r.receipt_id = ru.receipt_id 
			LEFT JOIN bill_receipts br ON r.receipt_id = br.receipt_no
			WHERE is_deposit AND ((ru.entity_type != 'package_id' and ru.entity_type != 'pat_package_id') or ru.entity_type is null) AND receipt_type = 'R' and payment_mode_id = 3 and realized = 'N' and r.amount != r.unallocated_amount
			ORDER BY  r.receipt_id ASC, r.created_at ASC 
	LOOP	

		IF(receipt_records.amount != receipt_records.unallocated_amount) THEN
			IF receipt_id = '' OR receipt_id != receipt_records.receiptid THEN
				receipt_id := receipt_records.receiptid;
				unallocated_amount := (receipt_records.unallocated_amount+receipt_records.bill_receipt_allocated_amount);
			ELSEIF receipt_id = receipt_records.receiptid THEN
				unallocated_amount := unallocated_amount + receipt_records.bill_receipt_allocated_amount;
			END IF;
			-- set unallocated_amount with unallocated_amount + allocated_amount			
			SELECT update_unallocated_amount(receipt_records.receiptid, unallocated_amount) INTO updateresult;
			-- set allocated_amount with zero
			UPDATE bill_receipts SET allocated_amount = 0 WHERE bill_receipt_id = receipt_records.bill_receipt_id;
		END IF;
	END LOOP;

END;
$BODY$
LANGUAGE 'plpgsql';


--EXECUTE REVERT FUNCTION
SELECT revert_cheque_unrealized_receipts_allocations();

-- Create Temp table
SELECT create_temp_table_mr_no();

--EXECUTE general_deposit_bill_receipt_allocation() TO adjust the reverted allocation receipts
SELECT general_deposit_bill_receipt_allocation();

-- Drop Temp table
SELECT drop_temp_table_for_deposits();


--EXECUTE bill ip_deposit_set_off column with ip receipt setoff, if incase any ip deposit set off happened for pharmacy bill, 
-- This function with update ip_deposit_set_off column
SELECT update_deposit_and_ip_deposit_set_off();


--EXECUTE migration_update_deposit_setoff_total 
SELECT migration_update_deposit_setoff_total();




