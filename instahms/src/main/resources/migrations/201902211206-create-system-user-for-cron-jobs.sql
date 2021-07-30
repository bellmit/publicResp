-- liquibase formatted sql
-- changeset adityabhatia02:create-system-user

INSERT INTO u_user (emp_username, emp_password, emp_usrremk, emp_status, role_id, 
	counter_id, pharmacy_counter_id, hosp_user, pharmacy_store_id, temp_username, type, 
	doctor_id, inventory_store_id, last_login, total_login, prescription_note_taker, mod_user, 
	mod_date, is_shared_login, sample_collection_center, 
	po_approval_upto, center_id, allow_sig_usage_by_others, created_timestamp, 
	password_change_date, is_encrypted, 
	center_assoc_type, encrypt_algo) VALUES 
('_system', 'w9qzGEP9nCt3','Special user for whom confidentiality restrictions dont apply', 'A', 1, 
	'CNT0001', 'CNT0002', 'N', NULL, '_system', 'U', 
	NULL, 'ISTORE0001', NULL, 0, 'N', NULL, 
	now(), 'N', -1, 
	NULL, 0, 'N', now(), 
	now(), false,
	'G', 'SHA-1');