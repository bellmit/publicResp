-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-functions-1 splitStatements:false


-- helper function to insert/update bill_receipts allocated amount
DROP FUNCTION IF EXISTS update_billreceipt_allocated_amount(text, text, numeric, numeric);
CREATE FUNCTION update_billreceipt_allocated_amount(rReceiptId text, billNo text, billReceiptId numeric, allocatedAmount numeric) 
 RETURNS void AS $BODY$
BEGIN
	IF billReceiptId > 0 THEN
    	UPDATE bill_receipts SET allocated_amount = allocated_amount + allocatedAmount WHERE bill_receipt_id = billReceiptId;
	ELSE
		INSERT into bill_receipts (receipt_no, bill_no, allocated_amount, display_date, mod_time, username) 
            VALUES (rReceiptId, billNo, allocatedAmount, now(), now(), '_system');
	END IF;
	
	RETURN;
END;
$BODY$ LANGUAGE 'plpgsql';

