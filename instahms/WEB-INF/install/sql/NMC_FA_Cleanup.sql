DROP TRIGGER IF EXISTS bill_finalization_trigger ON bill CASCADE;
DROP TRIGGER IF EXISTS y_bill_charge_adjustment_trigger ON bill_charge CASCADE;
DROP TRIGGER IF EXISTS auto_post_sponsor_receipts ON bill;

CREATE TABLE bill_adjustment_bkp (LIKE bill_adjustment);
CREATE TABLE bill_charge_adjustment_bkp (LIKE bill_charge_adjustment);

INSERT INTO bill_adjustment_bkp (SELECT * FROM bill_adjustment);
INSERT INTO bill_charge_adjustment_bkp (SELECT * FROM bill_charge_adjustment);

DROP TABLE bill_adjustment CASCADE;
DROP TABLE bill_charge_adjustment CASCADE;