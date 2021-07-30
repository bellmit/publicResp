-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-functions splitStatements:false
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:t select (select count(*) = 0 from receipt_refund_reference) and (select COUNT(*) > 0 from information_schema.columns  where column_name = 'amount' and table_name='bill_receipts' and table_schema = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path'))
-- validCheckSum: ANY

-- helper function to update receipt unallocated amount
DROP FUNCTION IF EXISTS update_unallocated_amount(text, numeric);
CREATE FUNCTION update_unallocated_amount(r_receipt_id text, unallocatedAmount numeric) 
 RETURNS void AS $BODY$
BEGIN
	UPDATE  receipts r SET unallocated_amount = unallocatedAmount WHERE r.receipt_id=  r_receipt_id;
	RETURN;
END;
$BODY$ LANGUAGE 'plpgsql';

-- function to migrate bill_receipts refunds to reference table
CREATE OR REPLACE FUNCTION migrate_bill_refunds_to_reference() 
RETURNS VOID AS $BODY$ 
DECLARE
    refundReceipts RECORD;
    receiptsResult RECORD;
    refundReferences RECORD;
    refundAmount decimal;
    returnUpdate text;
BEGIN
 FOR refundReceipts IN SELECT bill_no, abs(amount) AS refundAmount, receipt_no FROM bill_receipts  WHERE payment_type ='F'
 LOOP 
 	refundAmount:= refundReceipts.refundAmount; 	
    FOR receiptsResult IN SELECT r.receipt_id, r.unallocated_amount AS unallocated_amount, ru.receipt_id AS receipt_usage_id, ru.entity_id AS bill_no 
    						FROM  receipts r JOIN  receipt_usage ru ON r.receipt_id = ru.receipt_id 
    						WHERE unallocated_amount>0 AND receipt_type = 'R' AND tpa_id IS null AND entity_type = 'bill_no' 
    						AND entity_id = refundReceipts.bill_no ORDER BY created_at DESC
 
    LOOP
	  IF refundAmount <= 0 THEN
	  	EXIT;
	  END IF;
	  IF( refundAmount>0) THEN
	      -- assumption is unallocated amount is same as receipt's amount 
	      -- if refund amount is more than the receipt's unallocated_amount
	      IF refundAmount > receiptsResult.unallocated_amount THEN
	      	SELECT update_unallocated_amount(receiptsResult.receipt_id,0) INTO returnUpdate;
	     	INSERT INTO receipt_refund_reference (receipt_id,refund_receipt_id,amount) VALUES (receiptsResult.receipt_id, refundReceipts.receipt_no,receiptsResult.unallocated_amount);
	     	refundAmount := refundAmount - receiptsResult.unallocated_amount;
	      ELSE
	        SELECT update_unallocated_amount(receiptsResult.receipt_id,receiptsResult.unallocated_amount-refundAmount) INTO returnUpdate;
	     	INSERT INTO receipt_refund_reference (receipt_id,refund_receipt_id,amount) VALUES (receiptsResult.receipt_id, refundReceipts.receipt_no,refundAmount);
	     	refundAmount := 0;
	      END IF;
   		END IF;
 	END LOOP;
 END LOOP;
   
END;
$BODY$
LANGUAGE 'plpgsql';

-- EXECUTE MIGRATION METHOD
SELECT migrate_bill_refunds_to_reference();
