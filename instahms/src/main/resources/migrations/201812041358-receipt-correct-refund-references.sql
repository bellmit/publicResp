-- liquibase formatted sql
-- changeset allabakash:Receipt-correct-refund-references splitStatements:false



-- CORRECT REFUND REFERENCE AMOUNT FOR ALL RECEIPTS OF
CREATE OR REPLACE FUNCTION correct_receipt_refunded_amount_with_receipt() 
RETURNS VOID AS $BODY$
DECLARE
	receipt_records RECORD;
	updateresult text;
BEGIN
	
FOR receipt_records IN SELECT r.receipt_id AS receiptid, r.amount, COALESCE(r.unallocated_amount,0) AS unallocated_amount, 
	            COALESCE(receipt_refund.receipt_refunded_amount,0) AS receipt_refunded_amount, COALESCE(allocated_amount,0) as allocated_amount , r.amount-(COALESCE(receipt_refund.receipt_refunded_amount,0)+COALESCE(allocated_amount,0)) as final_unallocated_amount
			FROM receipts r 
			LEFT JOIN receipt_usage ru ON r.receipt_id=ru.receipt_id
			LEFT JOIN (SELECT receipt_id, SUM(amount) as receipt_refunded_amount FROM receipt_refund_reference group by receipt_id) AS receipt_refund ON r.receipt_id = receipt_refund.receipt_id 
			LEFT JOIN (SELECT receipt_no, SUM(allocated_amount) as allocated_amount FROM bill_receipts group by receipt_no) AS bill_receipts ON r.receipt_id = bill_receipts.receipt_no 
			WHERE is_deposit  AND receipt_type = 'R' 
			GROUP BY receiptid, receipt_refund.receipt_refunded_amount, allocated_amount HAVING amount != unallocated_amount+COALESCE(receipt_refund.receipt_refunded_amount,0)+COALESCE(allocated_amount,0)
			ORDER BY  r.receipt_id ASC, r.created_at ASC 

LOOP
		IF(receipt_records.unallocated_amount != receipt_records.final_unallocated_amount) THEN
			-- set unallocated amount with final_unallocated_amount
			SELECT update_unallocated_amount(receipt_records.receiptid, receipt_records.final_unallocated_amount) INTO updateresult;
		END IF;		
END LOOP;

END;
$BODY$
LANGUAGE 'plpgsql';
