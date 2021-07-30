-- liquibase formatted sql
-- changeset allabakash:Bill-total-amount-fix-HMS-34386 splitStatements:false

-- update total_amount and total_claim columns of bill table with bill_charge where char
 UPDATE
	bill b
	SET
		total_amount = COALESCE((
			SELECT
				sum(amount) AS ct FROM bill_charge bc1
			WHERE
				bc1.bill_no = b.bill_no
				AND bc1.status != 'X'
				AND amount IS NOT NULL), 0), 
		total_claim = COALESCE((
			SELECT
				sum(insurance_claim_amount) AS ct FROM bill_charge bc2
			WHERE
				bc2.bill_no = b.bill_no
				AND bc2.status != 'X'), 0)
	WHERE
		(b.total_amount = 0 OR b.total_amount = 1)  
		AND bill_no in(
			SELECT
				bill_no FROM bill_charge
			WHERE
				charge_head = 'ROF'
				AND status != 'X');
   
