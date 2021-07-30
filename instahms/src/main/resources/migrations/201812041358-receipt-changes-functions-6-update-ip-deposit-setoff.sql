-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-functions-6-update-ip-deposit-setoff splitStatements:false
-- validCheckSum: ANY


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
    UPDATE bill SET ip_deposit_set_off_old = ip_deposit_set_off_old||', '||billReceiptRecord.setoff_amount, ip_deposit_set_off = billReceiptRecord.setoff_amount WHERE bill_no = billReceiptRecord.bill_no;
 END LOOP;
   
END;
$BODY$
LANGUAGE 'plpgsql';

--EXECUTE UPDATE METHOD
SELECT update_deposit_and_ip_deposit_set_off();

