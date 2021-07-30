-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-functions-3-1 splitStatements:false
-- validCheckSum: ANY
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:t select count(*) > 0 from information_schema.columns where table_schema = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') and table_name='patient_deposits' and column_name='package_id'

-- function to migrate bill_receipts refunds to reference table
CREATE OR REPLACE FUNCTION migrate_patient_deposits_refunds_to_reference() 
RETURNS VOID AS $BODY$ 
DECLARE
    refundReceipts RECORD;
    receiptsResult RECORD;
    refundReferences RECORD;
    refundAmount decimal;
    returnUpdate text;
    selectQuery text;
BEGIN
 FOR refundReceipts IN SELECT mr_no as mr_no, abs(amount) AS refundAmount, deposit_no AS receipt_no, package_id,
                      CASE WHEN deposit_available_for='I' THEN 'visit_type' 
                            WHEN deposit_available_for='B' AND package_id > 0 THEN 'package_id' 
                            ELSE NULL END 
                            AS entity_type, 
                      CASE WHEN deposit_available_for='I' THEN 'i' 
                            WHEN deposit_available_for='B' AND package_id > 0 THEN package_id::varchar END
                            AS entity_id
                            FROM patient_deposits  WHERE deposit_type ='F'
 LOOP 
 	refundAmount:= refundReceipts.refundAmount;

 	IF refundReceipts.entity_type is null THEN
 		FOR receiptsResult IN SELECT r.receipt_id, r.unallocated_amount AS unallocated_amount, ru.receipt_id AS receipt_usage_id, ru.entity_id AS bill_no 
	                            FROM  receipts r LEFT JOIN  receipt_usage ru ON r.receipt_id = ru.receipt_id 
	                            WHERE unallocated_amount>0 AND receipt_type = 'R' AND tpa_id IS null 
	                                  AND r.is_deposit AND r.mr_no = refundReceipts.mr_no AND (entity_type is null OR entity_type = 'bill_type') 
	                                  ORDER BY r.created_at DESC
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
	                                  
 	ELSE
 		FOR receiptsResult IN SELECT r.receipt_id, r.unallocated_amount AS unallocated_amount, ru.receipt_id AS receipt_usage_id, ru.entity_id AS bill_no 
	                            FROM  receipts r LEFT JOIN  receipt_usage ru ON r.receipt_id = ru.receipt_id 
	                            WHERE unallocated_amount>0 AND receipt_type = 'R' AND tpa_id IS null 
	                                  AND r.is_deposit AND r.mr_no = refundReceipts.mr_no 
	                                  AND entity_type = refundReceipts.entity_type 
	                                  AND entity_id = refundReceipts.entity_id
	                                  ORDER BY r.created_at DESC
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
	                                  
    END IF;
    
 	
 END LOOP;
 
   
END;
$BODY$
LANGUAGE 'plpgsql';

-- EXECUTE MIGRATION METHOD
SELECT migrate_patient_deposits_refunds_to_reference();

