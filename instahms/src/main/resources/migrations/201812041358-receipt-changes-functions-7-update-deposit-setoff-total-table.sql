-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-functions-7-update-deposit-setoff-total-table splitStatements:false
-- validCheckSum: ANY

-- function to update deposit_setoff_total table with bill's deposit_set_off
CREATE OR REPLACE FUNCTION migration_update_deposit_setoff_total() 
RETURNS VOID AS $BODY$ 
DECLARE
    billReceiptRecord RECORD;
BEGIN
 
 UPDATE
	deposit_setoff_total
SET
	hosp_total_setoffs = bills.depositssum,
	hosp_total_balance = hosp_total_deposits - bills.depositssum
FROM (
	SELECT
		pr.mr_no,
		sum(deposit_set_off) AS depositssum
	FROM
		bill b
		JOIN patient_registration pr ON b.visit_id = pr.patient_id
		JOIN deposit_setoff_total dst ON pr.mr_no = dst.mr_no
	WHERE deposit_set_off>0 
	GROUP BY
		pr.mr_no ) AS bills
WHERE
	deposit_setoff_total.mr_no = bills.mr_no;
   
END;
$BODY$
LANGUAGE 'plpgsql';
