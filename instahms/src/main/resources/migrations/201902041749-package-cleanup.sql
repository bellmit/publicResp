-- liquibase formatted sql
-- changeset harishm18:package-cleanup

-- Update tests_prescribed from patient prescription

UPDATE tests_prescribed SET doc_presc_id = NULL, conducted='X',
cancelled_by = bc.username, cancel_date=bc.mod_time FROM bill_charge bc
WHERE common_order_id = bc.order_number AND conducted = 'N' AND bc.status='X'
AND bc.charge_group IN ('PKG', 'OPE');
