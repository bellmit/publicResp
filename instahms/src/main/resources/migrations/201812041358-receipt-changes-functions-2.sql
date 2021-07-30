-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-functions-2 splitStatements:false
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:t select count(*) > 0 from information_schema.columns where table_schema = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') and table_name='patient_deposits' and column_name='package_id'

-- function to migrate patient_deposits receipt_type refunds to reference table
CREATE OR REPLACE FUNCTION migrate_deposits_to_receipts_usage() 
RETURNS VOID AS $BODY$ 
DECLARE
    depositReceipts RECORD;
    receiptsResult RECORD;
    refundReferences RECORD;
    refundAmount decimal;
    patient_package_id integer;
BEGIN
 
 FOR depositReceipts IN SELECT * FROM patient_deposits LOOP 
    --insert into receipt_usage table

    --if package is not null insert package_id as entity_type
    IF depositReceipts.package_id IS NOT NULL THEN
    	-- get the pat_package_id for the package_id and mr_no
    	SELECT pat_package_id INTO patient_package_id FROM patient_packages WHERE package_id = depositReceipts.package_id AND mr_no = depositReceipts.mr_no;
    	IF patient_package_id > 0 THEN
    		INSERT INTO receipt_usage (receipt_id,entity_type,entity_id) VALUES (depositReceipts.deposit_no, 'pat_package_id', patient_package_id);
		END IF;
		INSERT INTO receipt_usage (receipt_id,entity_type,entity_id) VALUES (depositReceipts.deposit_no, 'package_id', depositReceipts.package_id);
		
    ELSEIF depositReceipts.deposit_available_for ilike 'I' THEN
		INSERT INTO receipt_usage (receipt_id,entity_type,entity_id) VALUES (depositReceipts.deposit_no, 'visit_type', 'i');
	ELSEIF depositReceipts.deposit_available_for IN ('H','P') THEN
		INSERT INTO receipt_usage (receipt_id,entity_type,entity_id) VALUES (depositReceipts.deposit_no, 'bill_type', depositReceipts.deposit_available_for);
	END IF; 
    -- for deposit_available_for='B', there will be no entry in receipt_usage table
 END LOOP;
   
END;
$BODY$
LANGUAGE 'plpgsql';

--EXECUTE MIGRATION METHOD
SELECT migrate_deposits_to_receipts_usage();


