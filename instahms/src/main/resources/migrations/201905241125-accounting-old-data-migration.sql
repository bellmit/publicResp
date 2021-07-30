-- liquibase formatted sql
-- changeset satishl2772:accounting-table-existing-data-migration-to-new-accounting-model


--	This is the migration for new accounting model.
--	New accounting model will not work for old data i.e. existing in hms_accounting_info table while upgradation.
--	So We need to post the reversals for those existing data. So this reversal posting process will be the below steps.
--	* For all open bills will post the reversals while upgradation/migration itself
--	* For Finalizad/Closed bills if bill got re-opened and then finalizing/closing that time we will post the reversals
--	* job_transaction in hms_accounting_info table has following meaning. If job_transaction
--		-> NULL = indicates data is came from cron job and it is older data
--		-> -1 = indicates it is older data and has reversals posted through mogration scripts.
--		-> -2 = indicates reversals for older data inserted through mogration scripts.

INSERT INTO hms_accounting_info 
 (center_id, center_name, visit_type, mr_no, visit_id, charge_group, charge_head, 
 account_group, service_group, service_sub_group, bill_no, audit_control_number, 
 voucher_no, voucher_type, voucher_date, item_code, item_name, receipt_store, issue_store, 
 currency, currency_conversion_rate, quantity, unit, unit_rate, gross_amount, 
 round_off_amount, discount_amount, points_redeemed, points_redeemed_rate, 
 points_redeemed_amount, item_category_id, purchase_vat_amount, 
 purchase_vat_percent, sales_vat_amount, sales_vat_percent, debit_account, 
 credit_account, tax_amount, net_amount, admitting_doctor, prescribing_doctor, 
 conductiong_doctor, referral_doctor, payee_doctor, outhouse_name, incoimng_hospital, 
 admitting_department, conducting_department, cost_amount, supplier_name, invoice_no, 
 invoice_date, voucher_ref, remarks, mod_time, counter_no, bill_open_date, 
 bill_finalized_date, is_tpa, insurance_co, old_mr_no, issue_store_center, 
 receipt_store_center, po_number, po_date, transaction_type, custom_1, 
 custom_2, custom_3, custom_4, cust_supplier_code, grn_date, cust_item_code, 
 prescribing_doctor_dept_name, custom_8, custom_9, custom_10, custom_11, guid, 
 update_status, created_at, sale_bill_no, job_transaction) 
 SELECT center_id, center_name, hai.visit_type, mr_no, 
 hai.visit_id, charge_group, charge_head, hai.account_group, service_group, service_sub_group, 
 hai.bill_no, hai.audit_control_number, voucher_no, voucher_type, voucher_date, item_code, 
 item_name, receipt_store, issue_store, currency, currency_conversion_rate, quantity, 
 unit, unit_rate, gross_amount, round_off_amount, discount_amount, hai.points_redeemed, 
 points_redeemed_rate, points_redeemed_amount, item_category_id, purchase_vat_amount, 
 purchase_vat_percent, sales_vat_amount, sales_vat_percent, credit_account, debit_account, 
 tax_amount, net_amount, admitting_doctor, prescribing_doctor, conductiong_doctor, 
 referral_doctor, payee_doctor, outhouse_name, incoimng_hospital, admitting_department, 
 conducting_department, cost_amount, supplier_name, invoice_no, invoice_date, voucher_ref, 
 hai.remarks, hai.mod_time, counter_no, bill_open_date, bill_finalized_date, hai.is_tpa, insurance_co, 
 old_mr_no, issue_store_center, receipt_store_center, po_number, po_date, 
 CASE WHEN transaction_type='R' THEN 'N' ELSE 'R' END as transaction_type, 
 custom_1, custom_2, custom_3, custom_4, cust_supplier_code, 
 grn_date, cust_item_code, prescribing_doctor_dept_name, custom_8, custom_9, custom_10, 
 custom_11, generate_id('ACCOUNTING_VOUCHER') as guid, update_status, 
 now(), sale_bill_no, -2 AS job_transaction 
 FROM hms_accounting_info hai
 LEFT JOIN bill b on (b.bill_no=hai.bill_no)
 WHERE b.status IN ('A','X') AND voucher_type IN ('HOSPBILLS', 'PHBILLS') 
 AND hai.charge_head NOT IN('INVITE','INVRET') AND job_transaction IS NULL;

 UPDATE hms_accounting_info hai set job_transaction = -1
 FROM bill b WHERE b.bill_no=hai.bill_no 
 AND b.status IN ('A','X') AND voucher_type IN ('HOSPBILLS', 'PHBILLS') 
 AND hai.charge_head NOT IN('INVITE','INVRET') AND job_transaction IS NULL;
 