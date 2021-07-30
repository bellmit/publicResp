-- liquibase formatted sql
-- changeset adityabhatia02:insert-data.sql
-- validCheckSum: ANY

--
-- Data for Name: hospital_center_master; Type: TABLE DATA; 
--

INSERT INTO hospital_center_master (center_id, center_name, status, center_code, city_id, state_id, country_id, center_address, accounting_company_name, hospital_center_service_reg_no, center_contact_phone, created_timestamp, updated_timestamp, region_id, health_authority, dhpo_facility_user_id, dhpo_facility_password, shafafiya_user_id, shafafiya_password, shafafiya_pbm_active, shafafiya_preauth_user_id, shafafiya_preauth_password, shafafiya_preauth_active, shafafiya_pbm_test_member_id, shafafiya_pbm_test_provider_id, shafafiya_preauth_test_member_id, shafafiya_preauth_test_provider_id, eclaim_active, tin_number, ha_username, ha_password) VALUES (0, 'Default Center', 'A', 'Default Center', 'CT0088', 'ST0017', 'CM0105', NULL, NULL, NULL, NULL, '2013-10-08 10:09:19.476906', '2013-10-08 10:09:19.619951', NULL, 'HAAD', NULL, NULL, NULL, NULL, 'N', NULL, NULL, 'N', NULL, NULL, NULL, NULL, 'N', NULL, NULL, NULL);

--
-- Data for Name: printer_definition; Type: TABLE DATA; 
--

INSERT INTO printer_definition (printer_id, printer_definition_name, print_mode, logo_header, footer, continuous_feed, page_height, top_margin, bottom_margin, left_margin, font_name, font_size, page_width, text_mode_column, right_margin, orientation, status, repeat_patient_info, text_mode_extra_lines, page_number, pg_no_position, pg_no_font_size, pg_no_vertical_position, footer_vertical_position) VALUES (2, 'Diagnostic Report Settings', 'P', 'N', 'N', 'N', 842, 0, 72, 72, 'sans-serif', 10, 480, 80, 72, 'P', 'A', 'N', 15, 'Y', 'R', 12, 'm', 'm');
INSERT INTO printer_definition (printer_id, printer_definition_name, print_mode, logo_header, footer, continuous_feed, page_height, top_margin, bottom_margin, left_margin, font_name, font_size, page_width, text_mode_column, right_margin, orientation, status, repeat_patient_info, text_mode_extra_lines, page_number, pg_no_position, pg_no_font_size, pg_no_vertical_position, footer_vertical_position) VALUES (4, 'Patient Document Printer Settings', 'P', 'N', 'N', 'N', 842, 100, 20, 72, NULL, 10, 400, 96, 72, 'P', 'A', 'N', 15, 'Y', 'R', 12, 'm', 'm');
INSERT INTO printer_definition (printer_id, printer_definition_name, print_mode, logo_header, footer, continuous_feed, page_height, top_margin, bottom_margin, left_margin, font_name, font_size, page_width, text_mode_column, right_margin, orientation, status, repeat_patient_info, text_mode_extra_lines, page_number, pg_no_position, pg_no_font_size, pg_no_vertical_position, footer_vertical_position) VALUES (3, 'Pharmacy Print Settings', 'P', 'N', 'N', 'N', 842, 20, 20, 20, 'Courier', 10, 440, 80, 20, 'P', 'A', 'N', 15, 'Y', 'R', 12, 'm', 'm');
INSERT INTO printer_definition (printer_id, printer_definition_name, print_mode, logo_header, footer, continuous_feed, page_height, top_margin, bottom_margin, left_margin, font_name, font_size, page_width, text_mode_column, right_margin, orientation, status, repeat_patient_info, text_mode_extra_lines, page_number, pg_no_position, pg_no_font_size, pg_no_vertical_position, footer_vertical_position) VALUES (6, 'Insurance Template Settings', 'T', 'Y', 'Y', 'N', 842, 100, 20, 50, 'Sans-Serif', 10, 400, 80, 15, 'P', 'A', 'N', 15, 'Y', 'R', 12, 'm', 'm');
INSERT INTO printer_definition (printer_id, printer_definition_name, print_mode, logo_header, footer, continuous_feed, page_height, top_margin, bottom_margin, left_margin, font_name, font_size, page_width, text_mode_column, right_margin, orientation, status, repeat_patient_info, text_mode_extra_lines, page_number, pg_no_position, pg_no_font_size, pg_no_vertical_position, footer_vertical_position) VALUES (7, 'Store Print Settings', 'T', 'H', 'Y', 'Y', 500, 40, 200, 10, 'Courier', 8, 400, 80, 5, 'P', 'A', 'N', 15, 'Y', 'R', 12, 'm', 'm');
INSERT INTO printer_definition (printer_id, printer_definition_name, print_mode, logo_header, footer, continuous_feed, page_height, top_margin, bottom_margin, left_margin, font_name, font_size, page_width, text_mode_column, right_margin, orientation, status, repeat_patient_info, text_mode_extra_lines, page_number, pg_no_position, pg_no_font_size, pg_no_vertical_position, footer_vertical_position) VALUES (8, 'Radiology Report Settings', 'P', 'N', 'N', 'N', 842, 140, 20, 50, 'sans-serif', 10, 595, 80, 50, 'P', 'A', 'N', 15, 'Y', 'R', 12, 'm', 'm');
INSERT INTO printer_definition (printer_id, printer_definition_name, print_mode, logo_header, footer, continuous_feed, page_height, top_margin, bottom_margin, left_margin, font_name, font_size, page_width, text_mode_column, right_margin, orientation, status, repeat_patient_info, text_mode_extra_lines, page_number, pg_no_position, pg_no_font_size, pg_no_vertical_position, footer_vertical_position) VALUES (1, 'Bill Print Settings', 'P', 'N', 'Y', 'N', 842, 100, 20, 15, 'Arial', 10, 400, 96, 72, 'P', 'A', 'N', 15, 'Y', 'R', 12, 'm', 'm');

--
-- Data for Name: acc_voucher_templates; Type: TABLE DATA; 
--

INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('CSRECEIPT', NULL, NULL, NULL, NULL);
INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('DEPOSIT', NULL, NULL, NULL, NULL);
INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('HOSPBILL', NULL, NULL, NULL, NULL);
INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('PAYMENTDUES', NULL, NULL, NULL, NULL);
INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('PHARMACYBILL', NULL, NULL, NULL, NULL);
INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('PP', NULL, NULL, NULL, NULL);
INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('PR', NULL, NULL, NULL, NULL);
INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('PURCHASE', NULL, NULL, NULL, NULL);
INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('RECEIPT', NULL, NULL, NULL, NULL);
INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('SRWDEBITNOTE', NULL, NULL, NULL, NULL);
INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('CSISSUED', NULL, NULL, NULL, NULL);
INSERT INTO acc_voucher_templates (voucher_type, template_content, reason, username, mode_time) VALUES ('CSRETURNED', NULL, NULL, NULL, NULL);

--
-- Data for Name: account_group_master; Type: TABLE DATA; 
--

INSERT INTO account_group_master (account_group_id, account_group_name, status, inter_comp_acc_name, account_group_service_reg_no, accounting_company_name) VALUES (1, 'Hospital', 'A', 'Hospital Company', NULL, NULL);

--
-- Data for Name: accounting_voucher_details; Type: TABLE DATA; 
--

--
-- Data for Name: accounting_xml_export_import_log; Type: TABLE DATA; 
--

--
-- Data for Name: action_rights; Type: TABLE DATA; 
--

INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'bill_reopen', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'bill_reopen', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'bill_reopen', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'bill_reopen', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'dishcharge_close', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'dishcharge_close', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'dishcharge_close', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'dishcharge_close', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'bed_close', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'bed_close', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'bed_close', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'bed_close', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'addtobill_charges', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'addtobill_charges', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'addtobill_charges', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'addtobill_charges', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_refund', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_refund', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_refund', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_refund', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'edit_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'edit_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'edit_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'edit_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'cancel_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'cancel_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'cancel_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'cancel_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'cancel_elements_in_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'cancel_elements_in_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'cancel_elements_in_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'cancel_elements_in_bill', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_new_registration', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_new_registration', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_new_registration', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_new_registration', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_cancel_test', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_cancel_test', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_cancel_test', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_cancel_test', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_retail_credit_sales', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_retail_credit_sales', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_retail_credit_sales', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_retail_credit_sales', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'change_max_costprice', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'change_max_costprice', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'change_max_costprice', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'change_max_costprice', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'direct_stock_entry', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'direct_stock_entry', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'direct_stock_entry', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'direct_stock_entry', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'usr_or_counter_day_book_access', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'usr_or_counter_day_book_access', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'usr_or_counter_day_book_access', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'usr_or_counter_day_book_access', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'edit_receipt_amounts', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'edit_receipt_amounts', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'edit_receipt_amounts', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'edit_receipt_amounts', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'new_bill_for_order_screen', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'new_bill_for_order_screen', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'new_bill_for_order_screen', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'new_bill_for_order_screen', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'new_bill_for_order_screen', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'new_bill_for_order_screen', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'cancel_scheduler_appointment', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'cancel_scheduler_appointment', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'cancel_scheduler_appointment', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'cancel_scheduler_appointment', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'reopen_consultation_after_time_limit', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'reopen_consultation_after_time_limit', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'reopen_consultation_after_time_limit', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'reopen_consultation_after_time_limit', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'reopen_consultation_after_time_limit', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'reopen_consultation_after_time_limit', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'validate_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'validate_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'validate_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'validate_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'validate_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'validate_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'revert_signoff', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'revert_signoff', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'revert_signoff', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'revert_signoff', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'revert_signoff', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'revert_signoff', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'amend_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'amend_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'amend_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'amend_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'amend_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'amend_test_results', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'view_all_rates', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_pharmacy_item_return_after_validity_days', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_pharmacy_item_return_after_validity_days', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_pharmacy_item_return_after_validity_days', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_pharmacy_item_return_after_validity_days', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_pharmacy_item_return_after_validity_days', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_pharmacy_item_return_after_validity_days', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'package_approval', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'package_approval', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'package_approval', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'package_approval', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'package_approval', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'package_approval', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'reg_charges_app', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'reg_charges_app', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'reg_charges_app', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'reg_charges_app', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'reg_charges_app', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'reg_charges_app', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_backdated_app', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_backdated_app', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_backdated_app', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_backdated_app', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_backdated_app', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_backdated_app', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_delete_patient_doc', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_delete_patient_doc', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_delete_patient_doc', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_delete_patient_doc', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_delete_patient_doc', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_delete_patient_doc', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_retail_sales', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_retail_sales', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_retail_sales', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_retail_sales', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_retail_sales', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_retail_sales', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'cancel_test_any_time', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'cancel_test_any_time', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'cancel_test_any_time', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'cancel_test_any_time', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'cancel_test_any_time', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'cancel_test_any_time', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_credit_bill_later', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_credit_bill_later', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_credit_bill_later', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_credit_bill_later', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_credit_bill_later', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_credit_bill_later', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_dyna_package_include_exclude', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_dyna_package_include_exclude', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_dyna_package_include_exclude', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_dyna_package_include_exclude', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_dyna_package_include_exclude', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_dyna_package_include_exclude', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_edit_dyna_package', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_edit_dyna_package', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_edit_dyna_package', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_edit_dyna_package', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_edit_dyna_package', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_edit_dyna_package', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'show_other_center_patients', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_edit_bill_open_date', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_edit_bill_open_date', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_edit_bill_open_date', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_edit_bill_open_date', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_edit_bill_open_date', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_edit_bill_open_date', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_denial_acceptance', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_denial_acceptance', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_denial_acceptance', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_denial_acceptance', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_denial_acceptance', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_denial_acceptance', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_dynamic_copay_change', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_dynamic_copay_change', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_dynamic_copay_change', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_dynamic_copay_change', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_dynamic_copay_change', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_dynamic_copay_change', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_discount_plans_in_bill', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_discount_plans_in_bill', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_discount_plans_in_bill', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_discount_plans_in_bill', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_discount_plans_in_bill', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_discount_plans_in_bill', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'add_edit_scheduler_rights', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'add_edit_scheduler_rights', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'add_edit_scheduler_rights', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'add_edit_scheduler_rights', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'add_edit_scheduler_rights', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'add_edit_scheduler_rights', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'allow_print_report_visit_wise', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'allow_print_report_visit_wise', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'allow_print_report_visit_wise', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'allow_print_report_visit_wise', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'allow_print_report_visit_wise', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'allow_print_report_visit_wise', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'view_ceed_response_comments', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'view_ceed_response_comments', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'view_ceed_response_comments', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'view_ceed_response_comments', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'view_ceed_response_comments', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'view_ceed_response_comments', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'initiate_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'initiate_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'initiate_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'initiate_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'initiate_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'initiate_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'clinical_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'clinical_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'clinical_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'clinical_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'clinical_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'clinical_discharge', 'A');
INSERT INTO action_rights (role_id, action, rights) VALUES (1, 'undo_section_finalization', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (2, 'undo_section_finalization', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (3, 'undo_section_finalization', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (4, 'undo_section_finalization', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (5, 'undo_section_finalization', 'N');
INSERT INTO action_rights (role_id, action, rights) VALUES (6, 'undo_section_finalization', 'N');

--
-- Data for Name: admission; Type: TABLE DATA; 
--

--
-- Data for Name: allergy_master; Type: TABLE DATA; 
--

--
-- Data for Name: alternate_activity_codes; Type: TABLE DATA; 
--

--
-- Data for Name: ambulance_master; Type: TABLE DATA; 
--

--
-- Data for Name: ambulance_usage; Type: TABLE DATA; 
--

--
-- Data for Name: anesthesia_charges_backup; Type: TABLE DATA; 
--

--
-- Data for Name: anesthesia_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: anesthesia_master; Type: TABLE DATA; 
--

--
-- Data for Name: anesthesia_type_charges; Type: TABLE DATA; 
--

--
-- Data for Name: anesthesia_type_master; Type: TABLE DATA; 
--

--
-- Data for Name: anesthesia_type_org_details; Type: TABLE DATA; 
--

--
-- Data for Name: antenatal; Type: TABLE DATA; 
--

--
-- Data for Name: appointment_source_master; Type: TABLE DATA; 
--

INSERT INTO appointment_source_master (appointment_source_id, appointment_source_name, status, paid_at_source) VALUES (1, 'Practo', 'A', 'N');

--
-- Data for Name: area_master; Type: TABLE DATA; 
--

--
-- Data for Name: assert_complaint_master; Type: TABLE DATA; 
--

--
-- Data for Name: assert_complaint_master_bkp; Type: TABLE DATA; 
--

--
-- Data for Name: assessment_components; Type: TABLE DATA; 
--

INSERT INTO assessment_components (dept_id, vitals, forms, id) VALUES ('-1', 'Y', '', 1);

--
-- Data for Name: asset_maintenance_activity; Type: TABLE DATA; 
--

--
-- Data for Name: asset_maintenance_activity_item; Type: TABLE DATA; 
--

--
-- Data for Name: contractor_master; Type: TABLE DATA; 
--

--
-- Data for Name: asset_maintenance_master; Type: TABLE DATA; 
--

--
-- Data for Name: auto_save_json_data; Type: TABLE DATA; 
--

--
-- Data for Name: baby_details; Type: TABLE DATA; 
--

--
-- Data for Name: bagtype_master; Type: TABLE DATA; 
--

INSERT INTO bagtype_master (bagtype_id, bagtype_name, status) VALUES ('1', 'Double Pocket', 'A');
INSERT INTO bagtype_master (bagtype_id, bagtype_name, status) VALUES ('2', 'Single Pocket', 'A');

--
-- Data for Name: bank_master; Type: TABLE DATA; 
--

INSERT INTO bank_master (bank_id, bank_name, status) VALUES (1, 'Other', 'A');

--
-- Data for Name: bar_code_print_templates; Type: TABLE DATA; 
--

INSERT INTO bar_code_print_templates (template_type, print_template_content, user_name, reason) VALUES ('REGBARCODE', '', 'InstaAdmin', 'Registration Bar Code');
INSERT INTO bar_code_print_templates (template_type, print_template_content, user_name, reason) VALUES ('SAMBARCODE', '', 'InstaAdmin', 'Sample Collection Bar Code');
INSERT INTO bar_code_print_templates (template_type, print_template_content, user_name, reason) VALUES ('ITMBARCODE', '', '', '');

--
-- Data for Name: bed_details; Type: TABLE DATA; 
--

INSERT INTO bed_details (bed_type, bed_charge, nursing_charge, initial_payment, duty_charge, maintainance_charge, organization, charge_type, bed_status, luxary_tax, intensive_bed_status, child_bed_status, hourly_charge, bed_charge_discount, nursing_charge_discount, duty_charge_discount, maintainance_charge_discount, hourly_charge_discount, initial_payment_discount, daycare_slab_1_charge, daycare_slab_1_charge_discount, daycare_slab_2_charge, daycare_slab_2_charge_discount, daycare_slab_3_charge, daycare_slab_3_charge_discount, is_override, item_code, code_type) VALUES ('GENERAL', 350.00, 100.00, 3000.00, 100.00, 0.00, 'ORG0001', 'G', 'A', 0.00, 'N', 'N', 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'N', NULL, NULL);
INSERT INTO bed_details (bed_type, bed_charge, nursing_charge, initial_payment, duty_charge, maintainance_charge, organization, charge_type, bed_status, luxary_tax, intensive_bed_status, child_bed_status, hourly_charge, bed_charge_discount, nursing_charge_discount, duty_charge_discount, maintainance_charge_discount, hourly_charge_discount, initial_payment_discount, daycare_slab_1_charge, daycare_slab_1_charge_discount, daycare_slab_2_charge, daycare_slab_2_charge_discount, daycare_slab_3_charge, daycare_slab_3_charge_discount, is_override, item_code, code_type) VALUES ('SEMI-PVT', 500.00, 200.00, 5000.00, 200.00, 0.00, 'ORG0001', 'G', 'A', 0.00, 'N', 'N', 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'N', NULL, NULL);
INSERT INTO bed_details (bed_type, bed_charge, nursing_charge, initial_payment, duty_charge, maintainance_charge, organization, charge_type, bed_status, luxary_tax, intensive_bed_status, child_bed_status, hourly_charge, bed_charge_discount, nursing_charge_discount, duty_charge_discount, maintainance_charge_discount, hourly_charge_discount, initial_payment_discount, daycare_slab_1_charge, daycare_slab_1_charge_discount, daycare_slab_2_charge, daycare_slab_2_charge_discount, daycare_slab_3_charge, daycare_slab_3_charge_discount, is_override, item_code, code_type) VALUES ('PRIVATE', 750.00, 300.00, 10000.00, 300.00, 0.00, 'ORG0001', 'G', 'A', 0.00, 'N', 'N', 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'N', NULL, NULL);

--
-- Data for Name: bed_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: bed_names; Type: TABLE DATA; 
--

INSERT INTO bed_names (ward_no, bed_type, bed_name, occupancy, status, bed_id, bed_ref_id, avilable_date, remarks, bed_status) VALUES ('WARD0001', 'GENERAL', 'GENERAL1', 'N', 'A', 1, NULL, NULL, NULL, 'A');

--
-- Data for Name: manf_master; Type: TABLE DATA; 
--

--
-- Data for Name: store_item_details; Type: TABLE DATA; 
--

--
-- Data for Name: bed_linen_current_list; Type: TABLE DATA; 
--

--
-- Data for Name: bed_operation_schedule; Type: TABLE DATA; 
--

--
-- Data for Name: bed_operation_secondary; Type: TABLE DATA; 
--

--
-- Data for Name: bed_types; Type: TABLE DATA; 
--

INSERT INTO bed_types (bed_type_name, is_icu, is_child_bed, billing_bed_type, display_order, status, insurance_category_id) VALUES ('SEMI-PVT', 'N', 'N', 'Y', 1, 'A', -1);
INSERT INTO bed_types (bed_type_name, is_icu, is_child_bed, billing_bed_type, display_order, status, insurance_category_id) VALUES ('PRIVATE', 'N', 'N', 'Y', 1, 'A', -1);
INSERT INTO bed_types (bed_type_name, is_icu, is_child_bed, billing_bed_type, display_order, status, insurance_category_id) VALUES ('GENERAL', 'N', 'N', 'Y', 1, 'A', -1);

--
-- Data for Name: bill; Type: TABLE DATA; 
--

--
-- Data for Name: bill_account_heads; Type: TABLE DATA; 
--

INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (1, 'Other Charges', 100);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (2, 'Registration Charges', 2);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (3, 'MLC Charges', 3);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (4, 'Doctor Consultation Charges', 4);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (5, 'Laboratory Charges', 5);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (6, 'Radiology Charges', 6);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (7, 'Ward Charges', 7);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (8, 'Duty Doctor Charges', 8);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (9, 'Nursing Charges', 9);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (10, 'ICU Charges', 10);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (11, 'Operation Charges', 11);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (12, 'OT Charges', 12);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (13, 'Surgeon/Anaesthetist Fees', 13);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (14, 'Consumables', 14);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (15, 'Pharmacy Charges', 15);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (16, 'Equipment Charges', 16);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (17, 'Services and Procedures', 17);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (18, 'Package Charges', 18);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (19, 'Discounts', 19);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (20, 'Pharmacy Credit Sales', 20);
INSERT INTO bill_account_heads (account_head_id, account_head_name, display_order) VALUES (21, 'Dietary charges', 21);

--
-- Data for Name: bill_activity_charge; Type: TABLE DATA; 
--

--
-- Data for Name: bill_adjustment; Type: TABLE DATA; 
--

--
-- Data for Name: bill_adjustment_alerts; Type: TABLE DATA; 
--

--
-- Data for Name: bill_approvals; Type: TABLE DATA; 
--

--
-- Data for Name: bill_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: bill_available_templates; Type: TABLE DATA; 
--

INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('CUSTOM-BUILTIN_HTML', 'Built-in HTML template', '*', '*', 21, 'A');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('CUSTOM-BUILTIN_TEXT', 'Built-in Text template', '*', '*', 22, 'A');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('CUSTOMEXP-BUILTIN_HTML', 'Expense Statement - Built-in HTML', 'C', '*', 55, 'A');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('CUSTOMEXP-BUILTIN_TEXT', 'Expense Statement - Built-in Text', 'C', '*', 56, 'A');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('BILL-DET-ALL', 'Bill - Detailed', '*', '*', 10, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('BILL-CHS-ALL', 'Bill - Charge Summary', 'C', '*', 11, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('BILL-SUM-ALL', 'Bill - Group Summary', 'C', '*', 12, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('BILL-DET-SNP', 'Bill Extract - Services', 'C', '*', 13, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('BILL-DET-DIA', 'Bill Extract - Diagnostics', 'C', '*', 14, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('BILL-DET-OPE', 'Bill Extract - Operations', 'C', '*', 15, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('BILL-DET-MED', 'Bill Extract - Pharmacy', 'C', '*', 16, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('PHBI', 'Pharmacy Breakup', 'C', '*', 17, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('PHEX', 'Pharmacy Expense Statement', 'C', '*', 18, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('BILL-INS-ALL', 'Bill - Claim Amounts', 'C', 'Y', 19, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('BILL-PAT-ALL', 'Bill - Patient Amounts', 'C', 'Y', 20, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('EXPS-DET-ALL', 'Visit Statement - Detailed', 'C', '*', 51, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('EXPS-CHS-ALL', 'Visit Statement - Charge Summary', 'C', '*', 52, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('EXPS-SUM-ALL', 'Visit Statement - Group Summary', 'C', '*', 53, 'I');
INSERT INTO bill_available_templates (template_id, template_name, bill_type, insurance_type, display_order, status) VALUES ('EXPS-DET-MED', 'Visit Statement - Pharmacy Extract', 'C', '*', 54, 'I');

--
-- Data for Name: bill_charge; Type: TABLE DATA; 
--

--
-- Data for Name: bill_charge_adjustment; Type: TABLE DATA; 
--

--
-- Data for Name: bill_charge_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: bill_charge_claim; Type: TABLE DATA; 
--

--
-- Data for Name: bill_charge_claim_tax; Type: TABLE DATA; 
--

--
-- Data for Name: bill_charge_details_adjustment; Type: TABLE DATA; 
--

--
-- Data for Name: bill_charge_item_account; Type: TABLE DATA; 
--

--
-- Data for Name: bill_charge_tax; Type: TABLE DATA; 
--

--
-- Data for Name: bill_claim; Type: TABLE DATA; 
--

--
-- Data for Name: bill_claim_adjustment; Type: TABLE DATA; 
--

--
-- Data for Name: bill_credit_notes; Type: TABLE DATA; 
--

--
-- Data for Name: bill_dashboard_heads; Type: TABLE DATA; 
--

INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('LTDIA', 'Diagnostics', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('RTDIA', 'Diagnostics', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('SERSNP', 'Services', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('PHMED', 'Pharmacy', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('PHRET', 'Pharmacy', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('BBED', 'Ward Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('PCBED', 'Ward Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('PCICU', 'Ward Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('DDBED', 'Ward Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('DDICU', 'Ward Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('NCBED', 'Ward Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('NCICU', 'Ward Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('BICU', 'Ward Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('SACOPE', 'Operation Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('CONOPE', 'Operation Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('TCOPE', 'Operation Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('SUOPE', 'Operation Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('ANAOPE', 'Operation Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('ASUOPE', 'Operation Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('AANOPE', 'Operation Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('COSOPE', 'Operation Charges', 0);
INSERT INTO bill_dashboard_heads (charge_head, display_title, display_order) VALUES ('EQOPE', 'Operation Charges', 0);

--
-- Data for Name: bill_dashboard_topn; Type: TABLE DATA; 
--

--
-- Data for Name: bill_label_master; Type: TABLE DATA; 
--

INSERT INTO bill_label_master (bill_label_id, bill_label_name, highlight, alert, remarks_reqd, status) VALUES (0, 'Other', 'Y', 'N', 'Y', 'A');

--
-- Data for Name: bill_print_template; Type: TABLE DATA; 
--

INSERT INTO bill_print_template (template_name, bill_template_content, template_mode, user_name, reason, download_content_type, download_extn) VALUES ('Web Based Bill Later Print Template', '', 'H', NULL, NULL, '', '');
INSERT INTO bill_print_template (template_name, bill_template_content, template_mode, user_name, reason, download_content_type, download_extn) VALUES ('Web Based Bill Now Print Template', '', 'H', NULL, NULL, '', '');

--
-- Data for Name: bill_receipts; Type: TABLE DATA; 
--

--
-- Data for Name: bill_receipts_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: bill_sponsor; Type: TABLE DATA; 
--

--
-- Data for Name: bill_sponsor_receipts; Type: TABLE DATA; 
--

--
-- Data for Name: blood_components_master; Type: TABLE DATA; 
--

INSERT INTO blood_components_master (bcmaster_id, donor_blood_component_name, status) VALUES ('1', 'Cryoprecipitate', 'A');
INSERT INTO blood_components_master (bcmaster_id, donor_blood_component_name, status) VALUES ('2', 'Crypto Poor Plasma', 'A');
INSERT INTO blood_components_master (bcmaster_id, donor_blood_component_name, status) VALUES ('3', 'Fresh Frozen Plasma', 'A');
INSERT INTO blood_components_master (bcmaster_id, donor_blood_component_name, status) VALUES ('4', 'Packed Red Cells', 'A');
INSERT INTO blood_components_master (bcmaster_id, donor_blood_component_name, status) VALUES ('5', 'Platelet Concentrate', 'A');
INSERT INTO blood_components_master (bcmaster_id, donor_blood_component_name, status) VALUES ('6', 'Whole Blood', 'A');

--
-- Data for Name: blood_discard_reasons_master; Type: TABLE DATA; 
--

INSERT INTO blood_discard_reasons_master (blood_discard_reason_id, blood_discard_reason_name, status) VALUES ('1', 'Broken Bag', 'A');
INSERT INTO blood_discard_reasons_master (blood_discard_reason_id, blood_discard_reason_name, status) VALUES ('2', 'Damaged Label', 'A');
INSERT INTO blood_discard_reasons_master (blood_discard_reason_id, blood_discard_reason_name, status) VALUES ('3', 'Error On Tag Attached to unit Bag', 'A');
INSERT INTO blood_discard_reasons_master (blood_discard_reason_id, blood_discard_reason_name, status) VALUES ('4', 'Improper Packaging', 'A');
INSERT INTO blood_discard_reasons_master (blood_discard_reason_id, blood_discard_reason_name, status) VALUES ('5', 'Improper Storage', 'A');
INSERT INTO blood_discard_reasons_master (blood_discard_reason_id, blood_discard_reason_name, status) VALUES ('6', 'No Segments', 'A');

--
-- Data for Name: blood_donation_type_master; Type: TABLE DATA; 
--

INSERT INTO blood_donation_type_master (bdtype_id, bdtype_name, status) VALUES ('1', 'Donor', 'A');
INSERT INTO blood_donation_type_master (bdtype_id, bdtype_name, status) VALUES ('2', 'Paid', 'A');
INSERT INTO blood_donation_type_master (bdtype_id, bdtype_name, status) VALUES ('3', 'Voluantary', 'A');

--
-- Data for Name: blood_group_master; Type: TABLE DATA; 
--

INSERT INTO blood_group_master (blood_group_id, blood_group_name, status) VALUES ('1', 'A', 'A');
INSERT INTO blood_group_master (blood_group_id, blood_group_name, status) VALUES ('2', 'AB', 'A');
INSERT INTO blood_group_master (blood_group_id, blood_group_name, status) VALUES ('3', 'B', 'A');
INSERT INTO blood_group_master (blood_group_id, blood_group_name, status) VALUES ('4', 'O', 'A');

--
-- Data for Name: blood_requests_purpose_master; Type: TABLE DATA; 
--

INSERT INTO blood_requests_purpose_master (blood_requests_purpose_id, blood_requests_purpose_name, status) VALUES ('1', 'Surgery', 'A');
INSERT INTO blood_requests_purpose_master (blood_requests_purpose_id, blood_requests_purpose_name, status) VALUES ('2', 'Therapeutic', 'A');

--
-- Data for Name: card_type_master; Type: TABLE DATA; 
--

--
-- Data for Name: store_category_master; Type: TABLE DATA; 
--

INSERT INTO store_category_master (category_id, category, identification, issue_type, billable, status, claimable, expiry_date_val, retailable, discount, purchases_cat_vat_account_prefix, purchases_cat_cst_account_prefix, sales_cat_vat_account_prefix, asset_tracking) VALUES (1, 'general', 'B', 'R', true, 'A', true, true, true, 0.00, 'Pharmacy Purchase', 'Pharmacy Purchase', 'Pharmacy Credit Sales', 'N');

--
-- Data for Name: category_bed_markups; Type: TABLE DATA; 
--

--
-- Data for Name: category_type_master; Type: TABLE DATA; 
--

INSERT INTO category_type_master (cat_id, cat_name, status, cat_desc) VALUES (1, 'General', 'A', NULL);

--
-- Data for Name: ceed_integration_details; Type: TABLE DATA; 
--

--
-- Data for Name: ceed_integration_main; Type: TABLE DATA; 
--

--
-- Data for Name: center_group_master; Type: TABLE DATA; 
--

--
-- Data for Name: center_group_details; Type: TABLE DATA; 
--

--
-- Data for Name: insta_integration; Type: TABLE DATA; 
--

INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('ceed', 'https://eclaimlink.dimensions-healthcare.net/DHCEG/gateway.asmx', '', '', NULL, NULL, 2, 'A', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('uhid_login', 'http://blue.phrdemo.com/data/login?', 'hie@hh.com', 'hie123', NULL, NULL, 8, 'A', 'TEST', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('uhid_getprofile', 'http://blue.phrdemo.com/data/getprofile', '', '', NULL, NULL, 9, 'A', 'TEST', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('uhid_hieSearch', 'http://blue.phrdemo.com/data/hieSearch', '', '', NULL, NULL, 10, 'A', 'TEST', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('paytm_withdraw_money', 'https://trust.paytm.in/wallet-web/v7/withdraw', NULL, NULL, NULL, NULL, 5, 'A', 'PROD', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('paytm_check_transaction_status', 'https://trust.paytm.in/wallet-web/checkStatus', NULL, NULL, NULL, NULL, 7, 'A', 'PROD', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('paytm_withdraw_money', 'http://trust-uat.paytm.in/wallet-web/v7/withdraw', NULL, NULL, NULL, NULL, 4, 'I', 'TEST', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('paytm_check_transaction_status', 'http://trust-uat.paytm.in/wallet-web/checkStatus', NULL, NULL, NULL, NULL, 6, 'I', 'TEST', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('chargebee', NULL, NULL, NULL, 'insta.chargebee.api.prod', NULL, 11, 'A', 'PROD', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('itdose_external_report', 'http://uatlims.apollohl.in/apollo/design/onlinelab/reportAPI.aspx', 'PHR', 'NHYF0VU456BMK3345', NULL, NULL, 13, 'A', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('chargebee', NULL, NULL, NULL, 'insta.chargebee.api.test', NULL, 12, 'I', 'TEST', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('comm_send', 'http://10.50.0.1/comm/generictransactionalsms/send', NULL, NULL, NULL, NULL, 1, 'A', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('comm_get', 'http://10.50.0.1/comm/generictransactionalsms/get_messages_by_reference_type', NULL, NULL, NULL, NULL, 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('loyalty_card', NULL, NULL, NULL, NULL, NULL, 15, 'A', 'PROD', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('loyalty_card', NULL, NULL, NULL, NULL, NULL, 16, 'I', 'TEST', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('practo_book', NULL, NULL, NULL, NULL, NULL, 14, 'I', 'PROD', NULL, NULL, NULL, 'insta_book', 'XUaSMUcyWojy2cKu7MaHaiw5Nxy2CD5oe5HDGeTywOcROlxPn7ik0zWfwKyvLN2UKBHp8TmsYip+WEfX+ZvNUg==', 7070, 'localhost');
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('practo_book', NULL, NULL, NULL, NULL, NULL, 17, 'A', 'TEST', NULL, NULL, NULL, 'Insta_app', 'Kqev8uqqVrFeYZ0TClk78H0RxNUtkh0dys4xEtf0h/YLZkEcToUIR7B8HqHP8hTBZ0x5cS6jlZwhKeA2D5u34g==', 7070, 'localhost');
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('DBAJ_SMS_gateway', 'http://www.ducont.ae/smscompanion/smsadmin/rapidegovsmssubmit.asp', 'DBAJ', 'DBAJ2017', NULL, NULL, 18, 'I', 'PROD', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('loyalty_offers', NULL, NULL, NULL, NULL, NULL, 19, 'A', 'PROD', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, integration_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('loyalty_offers', NULL, NULL, NULL, NULL, NULL, 20, 'I', 'TEST', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

--
-- Data for Name: center_integration_details; Type: TABLE DATA; 
--

--
-- Data for Name: center_outsources; Type: TABLE DATA; 
--

--
-- Data for Name: center_package_applicability; Type: TABLE DATA; 
--

--
-- Data for Name: center_preferences; Type: TABLE DATA; 
--

INSERT INTO center_preferences (center_id, pref_default_ip_bill_type, pref_default_op_bill_type, pref_rate_plan_for_non_insured_bill, pacs_mrno_url, pacs_order_url, pref_op_default_category, pref_ip_default_category, pref_osp_default_category, pref_pre_reg_default_category, pref_smart_card_enabled, government_id_provider_url, government_id_provider, smart_card_id_pattern, share_pat_details_to_practo) VALUES (0, 'C', 'P', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'N', NULL, NULL, NULL, NULL);

--
-- Data for Name: center_report_deliv_days_overrides; Type: TABLE DATA; 
--

--
-- Data for Name: center_report_deliv_override_details; Type: TABLE DATA; 
--

--
-- Data for Name: center_report_deliv_times_default; Type: TABLE DATA; 
--

--
-- Data for Name: chargegroup_constants; Type: TABLE DATA; 
--

INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('REG', 'Registration', 'Y', 'Y', '', 1, false, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('BED', 'Ward Charges', 'Y', 'N', 'mod_ipservices', 3, false, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('ICU', 'ICU Charges', 'Y', 'N', 'mod_ipservices', 4, false, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('PKG', 'Packages', 'Y', 'Y', 'mod_basic', 8, false, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('RET', 'Returns', 'N', 'N', '', 13, false, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('ITE', 'Items', 'N', 'N', 'mod_inventory', 14, false, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('DIA', 'Diagnostics', 'Y', 'Y', 'mod_diagnostics', 5, true, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('OPE', 'Operation Charges', 'Y', 'N', 'mod_ipservices', 6, true, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('MED', 'Medicines & Consumables', 'Y', 'Y', '', 10, true, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('OTC', 'Other Charges', 'Y', 'Y', NULL, 9, true, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('DOC', 'Doctor Charges', 'Y', 'Y', 'mod_prescribe', 2, true, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('SNP', 'Services & Procedures', 'Y', 'Y', 'mod_prescribe', 7, true, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('DIE', 'Dietary Charges', 'Y', 'N', 'mod_ipservices', 15, true, 'mod_dietary');
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('TAX', 'Taxes', 'Y', 'Y', '', 11, false, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('DIS', 'Discounts', 'Y', 'Y', '', 20, false, NULL);
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('DRG', 'DRG Cost', 'Y', 'Y', 'mod_eclaim', 25, false, 'mod_adv_ins');
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('PDM', 'Per Diem Cost', 'Y', 'Y', 'mod_eclaim', 30, false, 'mod_adv_ins');
INSERT INTO chargegroup_constants (chargegroup_id, chargegroup_name, ip_applicable, op_applicable, associated_module, display_order, ordereable, dependent_module) VALUES ('ADJ', 'Adjustment', 'Y', 'Y', '', 27, false, '');

--
-- Data for Name: chargehead_constants; Type: TABLE DATA; 
--

INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DOC', 'ROPDOC', 'OP Revisit Consultation', 'N', 'Y', NULL, 22, 4, 'Y', false, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OPE', 'SUOPE', 'Surgeon Fees', 'Y', 'N', NULL, 61, 13, 'Y', false, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OPE', 'ANAOPE', 'Anaesthetist Fees', 'Y', 'N', NULL, 63, 13, 'Y', false, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OPE', 'TCOPE', 'Theater Charge', 'Y', 'N', NULL, 62, 12, 'N', false, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('MED', 'CONMED', 'Consumables', 'Y', 'Y', NULL, 103, 14, 'N', false, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('MED', 'MEMED', 'Medicine', 'Y', 'Y', NULL, 101, 15, 'N', false, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('PKG', 'PKGPKG', 'Package Charges', 'Y', 'Y', NULL, 81, 18, 'N', false, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('ITE', 'INVITE', 'Inventory Item', 'N', 'N', NULL, 131, 14, 'N', false, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('RET', 'INVRET', 'Inventory Returns', 'N', 'N', NULL, 132, 15, 'N', false, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DIA', 'LTDIA', 'Lab Tests', 'Y', 'Y', NULL, 52, 5, 'Y', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DIA', 'RTDIA', 'Radiology Tests', 'Y', 'Y', NULL, 53, 6, 'Y', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DOC', 'IPDOC', 'IP Doctor Visit', 'Y', 'N', NULL, 23, 4, 'Y', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('SNP', 'SERSNP', 'Service', 'Y', 'Y', NULL, 71, 17, 'Y', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OPE', 'SACOPE', 'Surgical Assistance', 'Y', 'N', NULL, 66, 11, 'N', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OPE', 'CONOPE', 'Consumables', 'Y', 'N', NULL, 64, 1, 'N', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OPE', 'EQOPE', 'Equipment', 'Y', 'N', NULL, 65, 16, 'N', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OTC', 'EQUOTC', 'Equipment', 'Y', 'Y', NULL, 91, 16, 'N', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OTC', 'IMPOTC', 'Implant Charge', 'Y', 'Y', NULL, 95, 1, 'N', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OTC', 'CONOTC', 'Consumables', 'Y', 'Y', NULL, 93, 14, 'N', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OTC', 'OCOTC', 'Other charge', 'Y', 'Y', NULL, 92, 1, 'N', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DIE', 'MDIE', 'Dietary charges', 'Y', 'N', NULL, 140, 21, 'Y', true, 'mod_dietary', 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DOC', 'OPDOC', 'OP Consultation', 'N', 'Y', NULL, 21, 4, 'Y', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OPE', 'ASUOPE', 'Asst. Surgeon Fees', 'Y', 'N', NULL, 67, 13, 'Y', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OPE', 'AANOPE', 'Asst. Anaesthetist Fees', 'Y', 'N', NULL, 68, 13, 'Y', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OPE', 'COSOPE', 'Co-op. Surgeon Fees', 'Y', 'N', NULL, 69, 13, 'Y', true, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('REG', 'GREG', 'General Registration', 'Y', 'Y', 'mod_billing', 11, 2, 'N', false, NULL, 'Y', 'Y', 'N', 1, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('REG', 'IPREG', 'Admission Charge', 'Y', 'N', 'mod_billing', 12, 2, 'N', false, NULL, 'Y', 'Y', 'N', 1, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('REG', 'OPREG', 'OP Visit Registration', 'N', 'Y', 'mod_billing', 13, 2, 'N', false, NULL, 'Y', 'Y', 'N', 1, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('REG', 'EMREG', 'EMR Charge', 'Y', 'Y', 'mod_billing', 15, 2, 'N', false, NULL, 'Y', 'Y', 'N', 1, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('REG', 'MLREG', 'MLC Charge', 'Y', 'Y', 'mod_billing', 16, 3, 'N', false, NULL, 'Y', 'Y', 'N', 1, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OTC', 'MISOTC', 'Miscellaneous', 'Y', 'Y', 'mod_billing', 94, 1, 'N', false, NULL, 'Y', 'Y', 'N', 1, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('BED', 'BBED', 'Bed Charge', 'Y', 'N', NULL, 31, 7, 'N', false, NULL, 'Y', 'Y', 'N', 2, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('BED', 'PCBED', 'Professional Charge', 'Y', 'N', NULL, 34, 7, 'N', false, NULL, 'Y', 'Y', 'N', 2, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('BED', 'DDBED', 'Duty Doctor Charge', 'Y', 'N', NULL, 33, 8, 'N', false, NULL, 'Y', 'Y', 'N', 2, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('BED', 'NCBED', 'Nurse Charge', 'Y', 'N', NULL, 32, 9, 'N', false, NULL, 'Y', 'Y', 'N', 2, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('BED', 'BYBED', 'Bystander Bed Charges', 'Y', 'N', NULL, NULL, 1, 'Y', false, NULL, 'Y', 'Y', 'N', 2, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('ICU', 'PCICU', 'Professional Charge (ICU)', 'Y', 'N', NULL, 44, 7, 'N', false, NULL, 'Y', 'Y', 'N', 3, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('ICU', 'DDICU', 'Duty Doctor Charge (ICU)', 'Y', 'N', NULL, 43, 8, 'N', false, NULL, 'Y', 'Y', 'N', 3, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('ICU', 'NCICU', 'Nurse Charge (ICU)', 'Y', 'N', NULL, 42, 9, 'N', false, NULL, 'Y', 'Y', 'N', 3, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('ICU', 'BICU', 'Bed Charge (ICU)', 'Y', 'N', NULL, 41, 10, 'N', false, NULL, 'Y', 'Y', 'N', 3, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DIS', 'BIDIS', 'Bill Discounts', 'Y', 'Y', 'mod_billing', 121, 19, 'N', false, NULL, 'Y', 'Y', 'N', 1, 'Y', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DIS', 'ROF', 'Round Off', 'N', 'N', 'mod_billing', 122, 19, 'N', false, '', 'Y', 'Y', 'N', 1, 'Y', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OPE', 'ANATOPE', 'Anesthesia type Charge', NULL, NULL, NULL, NULL, 1, 'Y', false, NULL, 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('OPE', 'TCAOPE', 'Additional Theatre Charge', 'Y', 'N', '', 57, 12, 'N', false, '', 'Y', 'N', 'Y', 0, 'N', -1, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('TAX', 'BSTAX', 'Service Charge', 'Y', 'Y', 'mod_billing', 115, 1, 'N', false, NULL, 'N', 'N', 'N', 1, 'Y', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('MED', 'PHCMED', 'Pharmacy Medicine', 'N', 'N', NULL, 74, 20, 'N', false, NULL, 'Y', 'Y', 'N', 36, 'N', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('RET', 'PHCRET', 'Pharmacy Returns', 'N', 'N', NULL, 133, 20, 'N', false, NULL, 'Y', 'Y', 'N', 36, 'N', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('MED', 'PHMED', 'Pharmacy Sales', 'N', 'N', NULL, 102, 15, 'N', false, NULL, 'Y', 'Y', 'N', 36, 'N', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('RET', 'PHRET', 'Pharmacy Sales Returns', 'N', 'N', NULL, 130, 15, 'N', false, NULL, 'Y', 'Y', 'N', 36, 'N', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('TAX', 'LTAX', 'Luxury Tax', 'N', 'N', 'mod_billing', 111, 1, 'N', false, NULL, 'Y', 'Y', 'N', 1, 'N', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('TAX', 'STAX', 'Service Tax', 'N', 'N', 'mod_billing', 112, 1, 'N', false, NULL, 'Y', 'Y', 'N', 1, 'N', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('TAX', 'CSTAX', 'Claim Service Tax', 'Y', 'Y', 'mod_billing', 114, 1, 'N', false, NULL, 'Y', 'N', 'N', 1, 'Y', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DRG', 'MARDRG', 'DRG Base Margin', 'Y', 'Y', 'mod_eclaim', 160, 1, 'N', false, 'mod_adv_ins', 'Y', 'N', 'N', 0, 'N', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('PKG', 'MARPKG', 'Package Margin', 'Y', 'Y', '', 150, 18, 'N', false, '', 'Y', 'Y', 'N', 0, 'N', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('PDM', 'MARPDM', 'Per Diem Margin', 'Y', 'Y', 'mod_eclaim', 170, 1, 'N', false, 'mod_adv_ins', 'Y', 'N', 'N', 0, 'Y', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('ADJ', 'SPNADJ', 'Sponsor Adjustment', 'Y', 'Y', '', 180, 1, 'N', false, '', 'Y', 'N', 'N', 0, 'Y', -1, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DRG', 'OUTDRG', 'DRG Outlier Payment', 'Y', 'Y', 'mod_eclaim', 161, 1, 'N', false, 'mod_adv_ins', 'Y', 'N', 'N', 0, 'N', -2, false, false, 'N');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DRG', 'BPDRG', 'DRG Base Payment', 'Y', 'Y', 'mod_eclaim', 162, 1, 'Y', false, 'mod_adv_ins', 'Y', 'Y', 'N', 0, 'N', -2, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DRG', 'ADJDRG', 'DRG Adjustment', 'Y', 'Y', 'mod_eclaim', 163, 1, 'Y', false, 'mod_adv_ins', 'Y', 'Y', 'N', 0, 'N', -2, false, false, 'Y');
INSERT INTO chargehead_constants (chargegroup_id, chargehead_id, chargehead_name, ip_applicable, op_applicable, associated_module, display_order, account_head_id, payment_eligible, ordereable, dependent_module, insurance_payable, claim_service_tax_applicable, codification_supported, service_sub_group_id, obsolete_excluded_charge, insurance_category_id, allow_rate_increase, allow_rate_decrease, service_charge_applicable) VALUES ('DRG', 'APDRG', 'DRG Add On Payment', 'Y', 'Y', 'mod_eclaim', 164, 1, 'Y', false, 'mod_adv_ins', 'Y', 'Y', 'N', 0, 'N', -2, false, false, 'Y');

--
-- Data for Name: city; Type: TABLE DATA; 
--

INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0001', 'ST0001', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0002', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0003', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0004', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0005', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0006', 'ST0006', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0007', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0008', 'ST0008', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0009', 'ST0009', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0010', 'ST0010', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0011', 'ST0011', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0012', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0013', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0014', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0015', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0016', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0017', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0018', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0019', 'ST0019', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0020', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0021', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0022', 'ST0022', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0023', 'ST0023', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0024', 'ST0024', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0025', 'ST0025', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0026', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0027', 'ST0027', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0028', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0029', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0030', 'ST0030', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0031', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0032', 'ST0032', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0033', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0034', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0035', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OTHER', 'CT0036', 'ST0036', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ADILABAD', 'CT0037', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AGARTALA', 'CT0038', 'ST0032', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AGRA', 'CT0039', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AHMEDABAD', 'CT0040', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AHMEDNAGAR', 'CT0041', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AHWA', 'CT0042', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AIZAWL', 'CT0043', 'ST0024', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AJMER', 'CT0044', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AKBARPUR', 'CT0045', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AKOLA', 'CT0046', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ALAPPUZHA', 'CT0047', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ALIBAG', 'CT0048', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ALIGARH', 'CT0049', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ALIPORE', 'CT0050', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ALLAHABAD', 'CT0051', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ALMORA', 'CT0052', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ALONG', 'CT0053', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ALWAR', 'CT0054', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AMBALA', 'CT0055', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AMBASSA', 'CT0056', 'ST0032', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AMBIKAPUR', 'CT0057', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AMRAVATI', 'CT0058', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AMRELI', 'CT0059', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AMRITSAR', 'CT0060', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AMROHA', 'CT0061', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ANAND', 'CT0062', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ANANTAPUR', 'CT0063', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ANANTNAG', 'CT0064', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ANGUL', 'CT0065', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ANINI', 'CT0066', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ARA', 'CT0067', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ARARIA', 'CT0068', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ARIYALUR', 'CT0069', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AURAIYA', 'CT0070', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AURANGABAD', 'CT0071', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('AZAMGARH', 'CT0072', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BADAUN', 'CT0073', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BADGAM', 'CT0074', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BAGALKOT', 'CT0075', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BAGESHWAR', 'CT0076', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BAGHMARA', 'CT0077', 'ST0023', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BAGHPAT', 'CT0078', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BAHRAICH', 'CT0079', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BALAGHAT', 'CT0080', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BALESHWAR', 'CT0081', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BALLIA', 'CT0082', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BALRAMPUR', 'CT0083', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BALURGHAT', 'CT0084', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BANAS KANTHA', 'CT0085', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BANDA', 'CT0086', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BANDRA', 'CT0087', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BANGALORE', 'CT0088', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BANKA', 'CT0089', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BANKURA', 'CT0090', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BANSWARA', 'CT0091', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BARABANKI', 'CT0092', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BARAGARH', 'CT0093', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BARAMULA', 'CT0094', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BARAN', 'CT0095', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BARASAT', 'CT0096', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BARDHAMAN', 'CT0097', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BAREILLY', 'CT0098', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BARIPADA', 'CT0099', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BARMER', 'CT0100', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BARPETA', 'CT0101', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BARWANI', 'CT0102', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BASTI', 'CT0103', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BATHINDA', 'CT0104', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BEGUSARAI', 'CT0105', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BELGAUM', 'CT0106', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BELLARY', 'CT0107', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BERHAMPORE', 'CT0108', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BETTIAH', 'CT0109', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BETUL', 'CT0110', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHABUA', 'CT0111', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHADOHI', 'CT0112', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHADRAK', 'CT0113', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHAGALPUR', 'CT0114', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHANDARA', 'CT0115', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHARATPUR', 'CT0116', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHARUCH', 'CT0117', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHAVNAGAR', 'CT0118', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHAWANIPATNA', 'CT0119', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHILWARA', 'CT0120', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHIND', 'CT0121', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHIWANI', 'CT0122', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHOPAL', 'CT0123', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHUBANESWAR', 'CT0124', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BHUJ', 'CT0125', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BID', 'CT0126', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BIDAR', 'CT0127', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BIHAR SHARIF', 'CT0128', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BIJAPUR', 'CT0129', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BIJNOR', 'CT0130', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BIKANER', 'CT0131', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BILASPUR', 'CT0132', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BILASPUR', 'CT0133', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BISHNUPUR', 'CT0134', 'ST0022', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BOKARO', 'CT0135', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BOLANGIR', 'CT0136', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BOMDILA', 'CT0137', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BONGAIGAON', 'CT0138', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BOUDH', 'CT0139', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BULANDSHAHR', 'CT0140', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BULDHANA', 'CT0141', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BUNDI', 'CT0142', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('BUXAR', 'CT0143', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CAR NICOBAR', 'CT0144', 'ST0001', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHABASA', 'CT0145', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHAMBA', 'CT0146', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHAMOLI', 'CT0147', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHAMPAWAT', 'CT0148', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHAMPHAI', 'CT0149', 'ST0024', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHAMRAJNAGAR', 'CT0150', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHANDAULI', 'CT0151', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHANDEL', 'CT0152', 'ST0022', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHANDIGARH', 'CT0153', 'ST0006', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHANDRAPUR', 'CT0154', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHANGLANG', 'CT0155', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHATRA', 'CT0156', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHENNAI', 'CT0157', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHHAPRA', 'CT0158', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHHATARPUR', 'CT0159', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHHATRAPUR', 'CT0160', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHHINDWARA', 'CT0161', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHIKMAGALUR', 'CT0162', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHINSURAH', 'CT0163', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHITRADURGA', 'CT0164', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHITRAKOOTDHAM', 'CT0165', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHITTOOR', 'CT0166', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHITTORGARH', 'CT0167', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHURACHANDPUR', 'CT0168', 'ST0022', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CHURU', 'CT0169', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('COIMBATORE', 'CT0170', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CUDDALORE', 'CT0171', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CUDDAPAH', 'CT0172', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('CUTTACK', 'CT0173', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DAHOD', 'CT0174', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DALTONGANJ', 'CT0175', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DAMAN', 'CT0176', 'ST0009', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DAMOH', 'CT0177', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DANTEWADA', 'CT0178', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DAPORIJO', 'CT0179', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DARBHANGA', 'CT0180', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DARJEELING', 'CT0181', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DATIA', 'CT0182', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DAUSA', 'CT0183', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DAVANAGERE', 'CT0184', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DEHRADUN', 'CT0185', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DELHI', 'CT0186', 'ST0010', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DEOGARH', 'CT0187', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DEOGHAR', 'CT0188', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DEORIA', 'CT0189', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DEWAS', 'CT0190', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DHAMTARI', 'CT0191', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DHANBAD', 'CT0192', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DHAR', 'CT0193', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DHARAMASALA', 'CT0194', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DHARMAPURI', 'CT0195', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DHARWAD', 'CT0196', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DHEMAJI', 'CT0197', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DHENKANAL', 'CT0198', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DHOLPUR', 'CT0199', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DHUBRI', 'CT0200', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DHULE', 'CT0201', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DIBRUGARH', 'CT0202', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DIMAPUR', 'CT0203', 'ST0025', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DINDIGUL', 'CT0204', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DINDORI', 'CT0205', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DIPHU', 'CT0206', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DISPUR', 'CT0207', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DIU', 'CT0208', 'ST0009', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DODA', 'CT0209', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DUMKA', 'CT0210', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DUNGARPUR', 'CT0211', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('DURG', 'CT0212', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ELURU', 'CT0213', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ENGLISH BAZAR', 'CT0214', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ERODE', 'CT0215', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ETAH', 'CT0216', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ETAWAH', 'CT0217', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('FAIZABAD', 'CT0218', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('FARIDABAD', 'CT0219', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('FARIDKOT', 'CT0220', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('FATEHABAD', 'CT0221', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('FATEHGARH', 'CT0222', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('FATEHGARH SAHIB', 'CT0223', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('FATEHPUR', 'CT0224', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('FIROZABAD', 'CT0225', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('FIROZPUR', 'CT0226', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GADAG', 'CT0227', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GADCHIROLI', 'CT0228', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GANDHINAGAR', 'CT0229', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GANGANAGAR', 'CT0230', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GANGTOK', 'CT0231', 'ST0030', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GARHWA', 'CT0232', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GAYA', 'CT0233', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GEZING', 'CT0234', 'ST0030', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GHAZIABAD', 'CT0235', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GHAZIPUR', 'CT0236', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GIRIDIH', 'CT0237', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GOALPARA', 'CT0238', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GODDA', 'CT0239', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GOLAGHAT', 'CT0240', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GONDA', 'CT0241', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GONDIYA', 'CT0242', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GOPALGANJ', 'CT0243', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GORAKHPUR', 'CT0244', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GULBARGA', 'CT0245', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GUMLA', 'CT0246', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GUNA', 'CT0247', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GUNTUR', 'CT0248', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GURDASPUR', 'CT0249', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GURGAON', 'CT0250', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GUWAHATI', 'CT0251', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('GWALIOR', 'CT0252', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HAFLONG', 'CT0253', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HAILAKANDI', 'CT0254', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HAJIPUR', 'CT0255', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HAMIRPUR', 'CT0256', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HAMIRPUR', 'CT0257', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HANUMANGARH', 'CT0258', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HARDA', 'CT0259', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HARDOI', 'CT0260', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HARIDWAR', 'CT0261', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HASSAN', 'CT0262', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HATHRAS', 'CT0263', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HAVERI', 'CT0264', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HAZARIBAGH', 'CT0265', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HINGOLI', 'CT0266', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HISSAR', 'CT0267', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HOSHANGABAD', 'CT0268', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HOSHIARPUR', 'CT0269', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HOWRAH', 'CT0270', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('HYDERABAD', 'CT0271', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('INDORE', 'CT0272', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JABALPUR', 'CT0273', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JAGATSINGHPUR', 'CT0274', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JAGDALPUR', 'CT0275', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JAIPUR', 'CT0276', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JAISALMER', 'CT0277', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JALANDHAR', 'CT0278', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JALGAON', 'CT0279', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JALNA', 'CT0280', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JALORE', 'CT0281', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JALPAIGURI', 'CT0282', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JAMMU', 'CT0283', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JAMNAGAR', 'CT0284', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JAMSHEDPUR', 'CT0285', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JAMUI', 'CT0286', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JANJGIR', 'CT0287', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JASHPUR', 'CT0288', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JAUNPUR', 'CT0289', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JEHANABAD', 'CT0290', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JHABUA', 'CT0291', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JHAJJAR', 'CT0292', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JHALAWAR', 'CT0293', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JHANSI', 'CT0294', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JHARSUGUDA', 'CT0295', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JHUNJHUNUN', 'CT0296', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JIND', 'CT0297', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JODHPUR', 'CT0298', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JORHAT', 'CT0299', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JOWAL', 'CT0300', 'ST0023', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('JUNAGADH', 'CT0301', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KAILASAHAR', 'CT0302', 'ST0032', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KAITHAL', 'CT0303', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KAKINADA', 'CT0304', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KALPETTA', 'CT0305', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KANCHEEPURAM', 'CT0306', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KANKER', 'CT0307', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KANNAUJ', 'CT0308', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KANNUR', 'CT0309', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KANPUR', 'CT0310', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KAPURTHALA', 'CT0311', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KARAIKAL', 'CT0312', 'ST0027', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KARAULI', 'CT0313', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KARGIL', 'CT0314', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KARIMGANJ', 'CT0315', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KARIMNAGAR', 'CT0316', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KARNAL', 'CT0317', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KARUR', 'CT0318', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KARWAR', 'CT0319', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KASARGOD', 'CT0320', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KATHUA', 'CT0321', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KATIHAR', 'CT0322', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KATNI', 'CT0323', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KAUSHAMBI', 'CT0324', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KAVARATTI', 'CT0325', 'ST0019', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KAWARDHA', 'CT0326', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KENDRAPARA', 'CT0327', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KEONJHAR', 'CT0328', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KEYLONG', 'CT0329', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KHAGARIA', 'CT0330', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KHALILABAD', 'CT0331', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KHAMMAM', 'CT0332', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KHANDWA', 'CT0333', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KHARGONE', 'CT0334', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KHEDA', 'CT0335', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KHERI', 'CT0336', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KHONSA', 'CT0337', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KISHANGANJ', 'CT0338', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOCH BIHAR', 'CT0339', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOCHI', 'CT0340', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KODERMA', 'CT0341', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOHIMA', 'CT0342', 'ST0025', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOKRAJHAR', 'CT0343', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOLAR', 'CT0344', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOLASIB', 'CT0345', 'ST0024', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOLHAPUR', 'CT0346', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOLKATA', 'CT0347', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOLLAM', 'CT0348', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOPPAL', 'CT0349', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KORAPUT', 'CT0350', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KORBA', 'CT0351', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KORIYA', 'CT0352', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOTA', 'CT0353', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOTTAYAM', 'CT0354', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KOZHIKODE', 'CT0355', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KRISHNANAGAR', 'CT0356', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KULU', 'CT0357', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KUPWARA', 'CT0358', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KURNOOL', 'CT0359', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('KURUKSHETRA', 'CT0360', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('LAKHIMPUR', 'CT0361', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('LAKHISARAI', 'CT0362', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('LALITPUR', 'CT0363', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('LAMPHELPAT', 'CT0364', 'ST0022', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('LATUR', 'CT0365', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('LAWNGTLAI', 'CT0366', 'ST0024', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('LEH', 'CT0367', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('LOHARDAGA', 'CT0368', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('LUCKNOW', 'CT0369', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('LUDHIANA', 'CT0370', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('LUNGLEI', 'CT0371', 'ST0024', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MACHILIPATNAM', 'CT0372', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MADHEPURA', 'CT0373', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MADHUBANI', 'CT0374', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MADIKERI', 'CT0375', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MADURAI', 'CT0376', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MAHARAJGANJ', 'CT0377', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MAHASAMUND', 'CT0378', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MAHBUBNAGAR', 'CT0379', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MAHE', 'CT0380', 'ST0027', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MAHESANA', 'CT0381', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MAHOBA', 'CT0382', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MAINPURI', 'CT0383', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MALAPPURAM', 'CT0384', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MALKANGIRI', 'CT0385', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MAMIT', 'CT0386', 'ST0024', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MANDI', 'CT0387', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MANDLA', 'CT0388', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MANDSAUR', 'CT0389', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MANDYA', 'CT0390', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MANGALDAI', 'CT0391', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MANGALORE', 'CT0392', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MANGAN', 'CT0393', 'ST0030', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MANSA', 'CT0394', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MARGAO', 'CT0395', 'ST0011', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MARIGAON', 'CT0396', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MATHURA', 'CT0397', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MAU', 'CT0398', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MEERUT', 'CT0399', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MIDNAPORE', 'CT0400', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MIRZAPUR', 'CT0401', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MOGA', 'CT0402', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MOKOKCHUNG', 'CT0403', 'ST0025', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MON', 'CT0404', 'ST0025', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MORADABAD', 'CT0405', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MORENA', 'CT0406', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MOTIHARI', 'CT0407', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MUKTSAR', 'CT0408', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MUMBAI', 'CT0409', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MUNGER', 'CT0410', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MUZAFFARNAGAR', 'CT0411', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MUZAFFARPUR', 'CT0412', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('MYSORE', 'CT0413', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NABARANGPUR', 'CT0414', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAGAON', 'CT0415', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAGAPATTINAM', 'CT0416', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAGAUR', 'CT0417', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAGERCOIL', 'CT0418', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAGPUR', 'CT0419', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAHAN', 'CT0420', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAINITAL', 'CT0421', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NALBARI', 'CT0422', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NALGONDA', 'CT0423', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAMAKKAL', 'CT0424', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAMCHI', 'CT0425', 'ST0030', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NANDED', 'CT0426', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NANDURBAR', 'CT0427', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NARNAUL', 'CT0428', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NARSINGHPUR', 'CT0429', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NASHIK', 'CT0430', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAVGARH', 'CT0431', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAVSARI', 'CT0432', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAWADA', 'CT0433', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAWAN SHEHAR', 'CT0434', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NAYAGARH', 'CT0435', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NEEMUCH', 'CT0436', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NELLORE', 'CT0437', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NEW TEHRI', 'CT0438', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NIZAMABAD', 'CT0439', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NOIDA', 'CT0440', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NONGPOH', 'CT0441', 'ST0023', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NONGSTOIN', 'CT0442', 'ST0023', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('NUAPADA', 'CT0443', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ONGOLE', 'CT0444', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ORAI', 'CT0445', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ORAS', 'CT0446', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('OSMANABAD', 'CT0447', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PADARAUNA', 'CT0448', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PAINAW', 'CT0449', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PAKUR', 'CT0450', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PALAKKAD', 'CT0451', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PALI', 'CT0452', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PANAJI', 'CT0453', 'ST0011', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PANCH MAHALS', 'CT0454', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PANCHKULA', 'CT0455', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PANIKOILI', 'CT0456', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PANIPAT', 'CT0457', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PANNA', 'CT0458', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PARALAKHEMUNDI', 'CT0459', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PARBHANI', 'CT0460', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PASIGHAT', 'CT0461', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PATAN', 'CT0462', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PATHANAMTHITTA', 'CT0463', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PATIALA', 'CT0464', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PATNA', 'CT0465', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PAURI', 'CT0466', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PERAMBALUR', 'CT0467', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PHEK', 'CT0468', 'ST0025', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PHULBANI', 'CT0469', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PILIBHIT', 'CT0470', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PITHORAGARH', 'CT0471', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PONDICHERRY', 'CT0472', 'ST0027', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('POONCH', 'CT0473', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PORBANDAR', 'CT0474', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('POROMPAT', 'CT0475', 'ST0022', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PORT BLAIR', 'CT0476', 'ST0001', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PRATAPGARH', 'CT0477', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PUDUKKOTTAI', 'CT0478', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PULWAMA', 'CT0479', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PUNE', 'CT0480', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PURI', 'CT0481', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PURNIA', 'CT0482', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('PURULIA', 'CT0483', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAE BARELI', 'CT0484', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAICHUR', 'CT0485', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAIGARH', 'CT0486', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAIGUNJ', 'CT0487', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAIPUR', 'CT0488', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAISEN', 'CT0489', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAJAURI', 'CT0490', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAJGARH', 'CT0491', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAJKOT', 'CT0492', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAJNANDGAON', 'CT0493', 'ST0007', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAJPIPLA', 'CT0494', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAJSAMAND', 'CT0495', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAMANATHAPURAM', 'CT0496', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAMPUR', 'CT0497', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RANCHI', 'CT0498', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RATLAM', 'CT0499', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RATNAGIRI', 'CT0500', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RAYAGADA', 'CT0501', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RECKONG PEO', 'CT0502', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('REWA', 'CT0503', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('REWARI', 'CT0504', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ROBERTSGANJ', 'CT0505', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ROHTAK', 'CT0506', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RUDRA PRAYAG', 'CT0507', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RUDRAPUR', 'CT0508', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('RUPNAGAR', 'CT0509', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SABAR KANTHA', 'CT0510', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SAGAR', 'CT0511', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SAHARANPUR', 'CT0512', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SAHARSA', 'CT0513', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SAHIBGANJ', 'CT0514', 'ST0016', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SAIHA', 'CT0515', 'ST0024', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SALEM', 'CT0516', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SAMASTIPUR', 'CT0517', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SAMBALPUR', 'CT0518', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SANGAREDDI', 'CT0519', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SANGLI', 'CT0520', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SANGRUR', 'CT0521', 'ST0028', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SATARA', 'CT0522', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SATNA', 'CT0523', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SAWAI MADHOPUR', 'CT0524', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SEHORE', 'CT0525', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SENAPATI', 'CT0526', 'ST0022', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SEONI', 'CT0527', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SEPPA', 'CT0528', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SERCHHIP', 'CT0529', 'ST0024', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SHAHDOL', 'CT0530', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SHAHJAHANPUR', 'CT0531', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SHAJAPUR', 'CT0532', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SHEIKHPURA', 'CT0533', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SHEOHAR', 'CT0534', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SHEOPUR', 'CT0535', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SHILLONG', 'CT0536', 'ST0023', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SHIMLA', 'CT0537', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SHIMOGA', 'CT0538', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SHIVPURI', 'CT0539', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SHRAVASTI', 'CT0540', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SIBSAGAR', 'CT0541', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SIDHI', 'CT0542', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SIKAR', 'CT0543', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SILCHAR', 'CT0544', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SILVASSA', 'CT0545', 'ST0008', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SIROHI', 'CT0546', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SIRSA', 'CT0547', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SITAMARHI', 'CT0548', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SITAPUR', 'CT0549', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SIVAGANGA', 'CT0550', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SIWAN', 'CT0551', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SOLAN', 'CT0552', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SOLAPUR', 'CT0553', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SONEPAT', 'CT0554', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SONEPUR', 'CT0555', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SRIKAKULAM', 'CT0556', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SRINAGAR', 'CT0557', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SULTANPUR', 'CT0558', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SUNDARGARH', 'CT0559', 'ST0026', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SUPAUL', 'CT0560', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SURAT', 'CT0561', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SURENDRANAGAR', 'CT0562', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SURI', 'CT0563', 'ST0035', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('SUSARAM', 'CT0564', 'ST0005', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TAMENGLONG', 'CT0565', 'ST0022', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TAWANG', 'CT0566', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TEZU', 'CT0567', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('THANE', 'CT0568', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('THANJAVUR', 'CT0569', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('THENI', 'CT0570', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('THIRUVALLUR', 'CT0571', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('THIRUVANANTHAPURAM', 'CT0572', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('THIRUVARUR', 'CT0573', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('THOOTHUKUDI', 'CT0574', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('THOUBAL', 'CT0575', 'ST0022', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('THRISSUR', 'CT0576', 'ST0018', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TIKAMGARH', 'CT0577', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TINSUKIA', 'CT0578', 'ST0004', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TIRUCHIRAPPALLI', 'CT0579', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TIRUNELVELI', 'CT0580', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TIRUVANNAMALAI', 'CT0581', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TONK', 'CT0582', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TUENSANG', 'CT0583', 'ST0025', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TUMKUR', 'CT0584', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('TURA', 'CT0585', 'ST0023', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('UDAIPUR', 'CT0586', 'ST0032', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('UDAIPUR', 'CT0587', 'ST0029', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('UDHAGAMANDALAM', 'CT0588', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('UDHAMPUR', 'CT0589', 'ST0015', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('UDUPI', 'CT0590', 'ST0017', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('UJJAIN', 'CT0591', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('UKHRUL', 'CT0592', 'ST0022', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('UMARIA', 'CT0593', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('UNA', 'CT0594', 'ST0014', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('UNNAO', 'CT0595', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('UTTARKASHI', 'CT0596', 'ST0033', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('VADODARA', 'CT0597', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('VALSAD', 'CT0598', 'ST0012', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('VARANASI', 'CT0599', 'ST0034', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('VELLORE', 'CT0600', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('VIDISHA', 'CT0601', 'ST0020', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('VILLUPURAM', 'CT0602', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('VIRUDHUNAGAR', 'CT0603', 'ST0031', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('VISHAKHAPATNAM', 'CT0604', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('VIZIANAGARAM', 'CT0605', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('WARANGAL', 'CT0606', 'ST0002', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('WARDHA', 'CT0607', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('WASHIM', 'CT0608', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('WILLIAMNAGAR', 'CT0609', 'ST0023', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('WOKHA', 'CT0610', 'ST0025', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('YAMUNA NAGAR', 'CT0611', 'ST0013', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('YANAM', 'CT0612', 'ST0027', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('YAVATMAL', 'CT0613', 'ST0021', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('YINGKIONG', 'CT0614', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('YUPIA', 'CT0615', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ZIRO', 'CT0616', 'ST0003', 'A');
INSERT INTO city (city_name, city_id, state_id, status) VALUES ('ZUNHEBOTO', 'CT0617', 'ST0025', 'A');

--
-- Data for Name: claim_submissions; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_blood_transfusions; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_dial_adeq_values; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_hospitalization; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_hospitalization_details; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_hospitalization_reasons; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_infection_antibiotic_log; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_infection_site_master; Type: TABLE DATA; 
--

INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (1, 'UTI', 'A');
INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (2, 'Foot Ulcer', 'A');
INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (3, 'Bactrimia', 'A');
INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (4, 'Septicaemia', 'A');
INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (5, 'Chronic Obstructive Lung Disease', 'A');
INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (6, 'Pneumonia', 'A');
INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (7, 'Bronchitis', 'A');
INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (8, 'Asthma', 'A');
INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (9, 'Lower Respiratory Tract Infection', 'A');
INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (10, 'Tuberculosis', 'A');
INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (11, 'Skin Diseases', 'A');
INSERT INTO clinical_infection_site_master (infection_site_id, infection_site_name, status) VALUES (12, 'Tooth Decay', 'A');

--
-- Data for Name: clinical_infections; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_infections_master; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_infections_recorded; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_lab_recorded; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_lab_result; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_lab_values; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_transfusion_details; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_vacc_no_reason; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_vaccination; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_vaccinations_details; Type: TABLE DATA; 
--

--
-- Data for Name: clinical_vaccinations_master; Type: TABLE DATA; 
--

--
-- Data for Name: code_type_classification; Type: TABLE DATA; 
--

INSERT INTO code_type_classification (code_category, code_type_classification) VALUES ('Diagnosis', 'ICD9');
INSERT INTO code_type_classification (code_category, code_type_classification) VALUES ('Diagnosis', 'ICD10');
INSERT INTO code_type_classification (code_category, code_type_classification) VALUES ('Treatment', 'CPT4v2012');
INSERT INTO code_type_classification (code_category, code_type_classification) VALUES ('Treatment', 'CPT4v2011');
INSERT INTO code_type_classification (code_category, code_type_classification) VALUES ('Drug', 'HCPCS');
INSERT INTO code_type_classification (code_category, code_type_classification) VALUES ('Treatment', 'ASL');

--
-- Data for Name: codification_message_types; Type: TABLE DATA; 
--

--
-- Data for Name: u_role; Type: TABLE DATA; 
--

INSERT INTO u_role (role_id, role_name, role_status, role_sdate, role_edate, role_flag, role_remk, portal_id, stats_item1, stats_item2, stats_item3, stats_item4, stats_item5, stats_item6, stats_item7, stats_item8, stats_item9, stats_item10, stats_item11, stats_item12, mod_user, mod_date) VALUES (1, 'InstaAdmin', 'A', NULL, NULL, NULL, 'Special role for Insta only (not editable)', 'N', 'ACTIVE_OP_PATIENTS', 'ACTIVE_IP_PATIENTS', 'TODAYS_DISCHARGES', 'TODAYS_ADMISSIONS', 'TODAYS_OP_REGISTRATIONS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO u_role (role_id, role_name, role_status, role_sdate, role_edate, role_flag, role_remk, portal_id, stats_item1, stats_item2, stats_item3, stats_item4, stats_item5, stats_item6, stats_item7, stats_item8, stats_item9, stats_item10, stats_item11, stats_item12, mod_user, mod_date) VALUES (2, 'Administrator', 'A', NULL, NULL, 'Y', 'Super user with all privileges (not editable)', 'N', 'ACTIVE_OP_PATIENTS', 'ACTIVE_IP_PATIENTS', 'TODAYS_DISCHARGES', 'TODAYS_ADMISSIONS', 'TODAYS_OP_REGISTRATIONS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO u_role (role_id, role_name, role_status, role_sdate, role_edate, role_flag, role_remk, portal_id, stats_item1, stats_item2, stats_item3, stats_item4, stats_item5, stats_item6, stats_item7, stats_item8, stats_item9, stats_item10, stats_item11, stats_item12, mod_user, mod_date) VALUES (3, 'Billing', 'A', NULL, NULL, 'Y', 'Access to Billing module', 'N', 'ACTIVE_OP_PATIENTS', 'ACTIVE_IP_PATIENTS', 'TODAYS_DISCHARGES', 'TODAYS_ADMISSIONS', 'TODAYS_OP_REGISTRATIONS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO u_role (role_id, role_name, role_status, role_sdate, role_edate, role_flag, role_remk, portal_id, stats_item1, stats_item2, stats_item3, stats_item4, stats_item5, stats_item6, stats_item7, stats_item8, stats_item9, stats_item10, stats_item11, stats_item12, mod_user, mod_date) VALUES (4, 'Registration', 'A', NULL, NULL, 'N', 'Access to Registration module', 'N', 'ACTIVE_OP_PATIENTS', 'ACTIVE_IP_PATIENTS', 'TODAYS_DISCHARGES', 'TODAYS_ADMISSIONS', 'TODAYS_OP_REGISTRATIONS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO u_role (role_id, role_name, role_status, role_sdate, role_edate, role_flag, role_remk, portal_id, stats_item1, stats_item2, stats_item3, stats_item4, stats_item5, stats_item6, stats_item7, stats_item8, stats_item9, stats_item10, stats_item11, stats_item12, mod_user, mod_date) VALUES (5, 'Patient', 'A', NULL, NULL, 'N', 'Access to patient medical records', 'P', 'ACTIVE_OP_PATIENTS', 'ACTIVE_IP_PATIENTS', 'TODAYS_DISCHARGES', 'TODAYS_ADMISSIONS', 'TODAYS_OP_REGISTRATIONS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO u_role (role_id, role_name, role_status, role_sdate, role_edate, role_flag, role_remk, portal_id, stats_item1, stats_item2, stats_item3, stats_item4, stats_item5, stats_item6, stats_item7, stats_item8, stats_item9, stats_item10, stats_item11, stats_item12, mod_user, mod_date) VALUES (6, 'Doctor', 'A', NULL, NULL, 'N', 'Access to all medical records', 'D', 'ACTIVE_OP_PATIENTS', 'ACTIVE_IP_PATIENTS', 'TODAYS_DISCHARGES', 'TODAYS_ADMISSIONS', 'TODAYS_OP_REGISTRATIONS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

--
-- Data for Name: codification_message_type_role; Type: TABLE DATA; 
--

--
-- Data for Name: u_user; Type: TABLE DATA; 
--

INSERT INTO u_user (emp_username, emp_password, emp_usrsdate, emp_usredate, emp_usrremk, emp_status, role_id, counter_id, specialization_id, lab_dept_id, pharmacy_counter_id, hosp_user, pharmacy_store_id, temp_username, type, doctor_id, inventory_store_id, last_login, total_login, scheduler_dept_id, prescription_note_taker, sch_default_doctor, multi_store, email_id, mobile_no, bed_view_default_ward, mod_user, mod_date, is_shared_login, sample_collection_center, po_approval_upto, center_id, allow_sig_usage_by_others, created_timestamp, password_change_date, is_encrypted, writeoff_limit, disc_auth_id, request_handler_key, login_handle, center_assoc_type, report_center_id, encrypt_algo) VALUES ('admin', 'admin', NULL, NULL, 'Hospital administrator (cannot edit)', 'A', 2, 'CNT0001', NULL, NULL, 'CNT0002', 'N', NULL, 'admin', 'U', NULL, 'ISTORE0001', NULL, 0, NULL, 'N', NULL, NULL, NULL, NULL, NULL, NULL, '2013-10-08 10:09:22', 'N', -1, NULL, 0, 'N', '2013-10-08 10:09:22', '2014-06-17 13:04:11.60557', false, NULL, NULL, 'admin_fresh', NULL, 'G', NULL, 'SHA-1');
INSERT INTO u_user (emp_username, emp_password, emp_usrsdate, emp_usredate, emp_usrremk, emp_status, role_id, counter_id, specialization_id, lab_dept_id, pharmacy_counter_id, hosp_user, pharmacy_store_id, temp_username, type, doctor_id, inventory_store_id, last_login, total_login, scheduler_dept_id, prescription_note_taker, sch_default_doctor, multi_store, email_id, mobile_no, bed_view_default_ward, mod_user, mod_date, is_shared_login, sample_collection_center, po_approval_upto, center_id, allow_sig_usage_by_others, created_timestamp, password_change_date, is_encrypted, writeoff_limit, disc_auth_id, request_handler_key, login_handle, center_assoc_type, report_center_id, encrypt_algo) VALUES ('InstaAdmin', 'InstaAdmin', NULL, NULL, 'Special user for InstaAdmin role (cannot edit)', 'A', 1, 'CNT0001', NULL, NULL, 'CNT0002', 'N', NULL, 'InstaAdmin', 'U', NULL, 'ISTORE0001', NULL, 0, NULL, 'N', NULL, NULL, NULL, NULL, NULL, NULL, '2013-10-08 10:09:22', 'N', -1, NULL, 0, 'N', '2013-10-08 10:09:22', '2014-06-17 13:04:11.60557', false, NULL, NULL, 'InstaAdmin_fresh', NULL, 'G', NULL, 'SHA-1');
INSERT INTO u_user (emp_username, emp_password, emp_usrsdate, emp_usredate, emp_usrremk, emp_status, role_id, counter_id, specialization_id, lab_dept_id, pharmacy_counter_id, hosp_user, pharmacy_store_id, temp_username, type, doctor_id, inventory_store_id, last_login, total_login, scheduler_dept_id, prescription_note_taker, sch_default_doctor, multi_store, email_id, mobile_no, bed_view_default_ward, mod_user, mod_date, is_shared_login, sample_collection_center, po_approval_upto, center_id, allow_sig_usage_by_others, created_timestamp, password_change_date, is_encrypted, writeoff_limit, disc_auth_id, request_handler_key, login_handle, center_assoc_type, report_center_id, encrypt_algo) VALUES ('InstaAPI', '123456', NULL, NULL, 'Special user for API Services (cannot edit)', 'A', 1, 'CNT0001', NULL, NULL, 'CNT0002', 'N', NULL, 'U', 'U', NULL, 'ISTORE0001', NULL, 0, NULL, 'N', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'N', -1, NULL, 0, 'N', '2016-04-15 12:23:05.723613', '2016-04-15 12:23:05.723613', false, NULL, NULL, 'InstaAPI_fresh', NULL, 'G', NULL, 'SHA-1');
INSERT INTO u_user (emp_username, emp_password, emp_usrsdate, emp_usredate, emp_usrremk, emp_status, role_id, counter_id, specialization_id, lab_dept_id, pharmacy_counter_id, hosp_user, pharmacy_store_id, temp_username, type, doctor_id, inventory_store_id, last_login, total_login, scheduler_dept_id, prescription_note_taker, sch_default_doctor, multi_store, email_id, mobile_no, bed_view_default_ward, mod_user, mod_date, is_shared_login, sample_collection_center, po_approval_upto, center_id, allow_sig_usage_by_others, created_timestamp, password_change_date, is_encrypted, writeoff_limit, disc_auth_id, request_handler_key, login_handle, center_assoc_type, report_center_id, encrypt_algo) VALUES ('auto_update', 'auto_update', '2010-12-08', '2010-12-08', 'Insta Automatic Updates', 'A', 1, '', '', '', '', 'N', '', 'auto_update', ' ', NULL, NULL, NULL, 0, NULL, 'N', NULL, NULL, NULL, NULL, NULL, NULL, '2013-10-08 10:09:22', 'N', -1, NULL, 0, 'N', '2013-10-08 10:09:22', '2014-06-17 13:04:11.60557', false, NULL, NULL, 'auto_update_fresh', NULL, 'G', NULL, 'SHA-1');
INSERT INTO u_user (emp_username, emp_password, emp_usrsdate, emp_usredate, emp_usrremk, emp_status, role_id, counter_id, specialization_id, lab_dept_id, pharmacy_counter_id, hosp_user, pharmacy_store_id, temp_username, type, doctor_id, inventory_store_id, last_login, total_login, scheduler_dept_id, prescription_note_taker, sch_default_doctor, multi_store, email_id, mobile_no, bed_view_default_ward, mod_user, mod_date, is_shared_login, sample_collection_center, po_approval_upto, center_id, allow_sig_usage_by_others, created_timestamp, password_change_date, is_encrypted, writeoff_limit, disc_auth_id, request_handler_key, login_handle, center_assoc_type, report_center_id, encrypt_algo) VALUES ('auto_po', 'auto_po', '2016-09-14', '2016-09-14', 'Insta Auto PO Generation', 'A', 1, NULL, NULL, NULL, NULL, 'N', NULL, 'auto_po', 'U', NULL, NULL, NULL, 0, NULL, 'N', NULL, NULL, NULL, NULL, NULL, NULL, '2016-09-14 18:52:47.988101', 'N', -1, NULL, 0, 'N', '2016-09-14 18:52:47.988101', '2016-09-14 18:52:47.988101', false, NULL, NULL, 'instaapi', NULL, 'G', NULL, 'SHA-1');
INSERT INTO u_user (emp_username, emp_password, emp_usrsdate, emp_usredate, emp_usrremk, emp_status, role_id, counter_id, specialization_id, lab_dept_id, pharmacy_counter_id, hosp_user, pharmacy_store_id, temp_username, type, doctor_id, inventory_store_id, last_login, total_login, scheduler_dept_id, prescription_note_taker, sch_default_doctor, multi_store, email_id, mobile_no, bed_view_default_ward, mod_user, mod_date, is_shared_login, sample_collection_center, po_approval_upto, center_id, allow_sig_usage_by_others, created_timestamp, password_change_date, is_encrypted, writeoff_limit, disc_auth_id, request_handler_key, login_handle, center_assoc_type, report_center_id, encrypt_algo) VALUES ('APIPatient', 'APIPatient', '2016-09-14', '2016-09-14', 'Insta API Patient user', 'A', 5, NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 'U', NULL, NULL, NULL, 0, NULL, 'N', NULL, NULL, NULL, NULL, NULL, NULL, '2016-09-14 19:22:26.078493', 'N', -1, NULL, 0, 'N', '2016-09-14 19:22:26.078493', '2016-09-14 19:22:26.078493', false, NULL, NULL, 'instaapi', NULL, 'G', NULL, 'SHA-1');

--
-- Data for Name: tickets; Type: TABLE DATA; 
--

--
-- Data for Name: codification_ticket_details; Type: TABLE DATA; 
--

--
-- Data for Name: collection_bar_code_print_templates; Type: TABLE DATA; 
--

--
-- Data for Name: collection_cards; Type: TABLE DATA; 
--

--
-- Data for Name: common_charges_master; Type: TABLE DATA; 
--

--
-- Data for Name: common_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: common_print_templates; Type: TABLE DATA; 
--

--
-- Data for Name: commonmedicine_timestamp; Type: TABLE DATA; 
--

INSERT INTO commonmedicine_timestamp (medicine_timestamp) VALUES (1);

--
-- Data for Name: complaintslog; Type: TABLE DATA; 
--

--
-- Data for Name: component_bar_code_print_templates; Type: TABLE DATA; 
--

--
-- Data for Name: consolidated_patient_bill; Type: TABLE DATA; 
--

--
-- Data for Name: consultation_charges; Type: TABLE DATA; 
--

INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-4, 'ORG0001', 'GENERAL', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-4, 'ORG0001', 'PRIVATE', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-4, 'ORG0001', 'SEMI-PVT', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-3, 'ORG0001', 'GENERAL', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-3, 'ORG0001', 'PRIVATE', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-3, 'ORG0001', 'SEMI-PVT', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-2, 'ORG0001', 'GENERAL', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-2, 'ORG0001', 'PRIVATE', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-2, 'ORG0001', 'SEMI-PVT', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-1, 'ORG0001', 'GENERAL', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-1, 'ORG0001', 'PRIVATE', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (-1, 'ORG0001', 'SEMI-PVT', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (11, 'ORG0001', 'GENERAL', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (11, 'ORG0001', 'PRIVATE', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (11, 'ORG0001', 'SEMI-PVT', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (12, 'ORG0001', 'GENERAL', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (12, 'ORG0001', 'PRIVATE', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (12, 'ORG0001', 'SEMI-PVT', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (13, 'ORG0001', 'GENERAL', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (13, 'ORG0001', 'PRIVATE', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (13, 'ORG0001', 'SEMI-PVT', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (14, 'ORG0001', 'GENERAL', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (14, 'ORG0001', 'PRIVATE', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (14, 'ORG0001', 'SEMI-PVT', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (15, 'ORG0001', 'GENERAL', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (15, 'ORG0001', 'PRIVATE', 0.00, 0.00, NULL, NULL, 'N');
INSERT INTO consultation_charges (consultation_type_id, org_id, bed_type, charge, discount, username, mod_time, is_override) VALUES (15, 'ORG0001', 'SEMI-PVT', 0.00, 0.00, NULL, NULL, 'N');

--
-- Data for Name: consultation_charges_backup; Type: TABLE DATA; 
--

--
-- Data for Name: consultation_em_calculation; Type: TABLE DATA; 
--

--
-- Data for Name: consultation_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: consultation_org_details; Type: TABLE DATA; 
--

INSERT INTO consultation_org_details (consultation_type_id, org_id, applicable, item_code, code_type, username, mod_time, base_rate_sheet_id, is_override) VALUES (11, 'ORG0001', true, NULL, NULL, NULL, NULL, NULL, 'N');
INSERT INTO consultation_org_details (consultation_type_id, org_id, applicable, item_code, code_type, username, mod_time, base_rate_sheet_id, is_override) VALUES (12, 'ORG0001', true, NULL, NULL, NULL, NULL, NULL, 'N');
INSERT INTO consultation_org_details (consultation_type_id, org_id, applicable, item_code, code_type, username, mod_time, base_rate_sheet_id, is_override) VALUES (13, 'ORG0001', true, NULL, NULL, NULL, NULL, NULL, 'N');
INSERT INTO consultation_org_details (consultation_type_id, org_id, applicable, item_code, code_type, username, mod_time, base_rate_sheet_id, is_override) VALUES (14, 'ORG0001', true, NULL, NULL, NULL, NULL, NULL, 'N');
INSERT INTO consultation_org_details (consultation_type_id, org_id, applicable, item_code, code_type, username, mod_time, base_rate_sheet_id, is_override) VALUES (15, 'ORG0001', true, NULL, NULL, NULL, NULL, NULL, 'N');
INSERT INTO consultation_org_details (consultation_type_id, org_id, applicable, item_code, code_type, username, mod_time, base_rate_sheet_id, is_override) VALUES (-1, 'ORG0001', true, NULL, NULL, NULL, NULL, NULL, 'N');
INSERT INTO consultation_org_details (consultation_type_id, org_id, applicable, item_code, code_type, username, mod_time, base_rate_sheet_id, is_override) VALUES (-3, 'ORG0001', true, NULL, NULL, NULL, NULL, NULL, 'N');
INSERT INTO consultation_org_details (consultation_type_id, org_id, applicable, item_code, code_type, username, mod_time, base_rate_sheet_id, is_override) VALUES (-4, 'ORG0001', true, NULL, NULL, NULL, NULL, NULL, 'N');
INSERT INTO consultation_org_details (consultation_type_id, org_id, applicable, item_code, code_type, username, mod_time, base_rate_sheet_id, is_override) VALUES (-2, 'ORG0001', true, NULL, NULL, NULL, NULL, NULL, 'N');

--
-- Data for Name: consultation_token; Type: TABLE DATA; 
--

--
-- Data for Name: consultation_types; Type: TABLE DATA; 
--

INSERT INTO consultation_types (consultation_type_id, status, consultation_type, consultation_code, patient_type, doctor_charge_type, service_sub_group_id, username, mod_time, charge_head, insurance_category_id, visit_consultation_type, allow_rate_increase, allow_rate_decrease) VALUES (-1, 'A', 'OP Consultation', '', 'o', 'op_charge', -1, 'default', '2011-04-26 12:42:29.647256', 'OPDOC', 1, -1, false, false);
INSERT INTO consultation_types (consultation_type_id, status, consultation_type, consultation_code, patient_type, doctor_charge_type, service_sub_group_id, username, mod_time, charge_head, insurance_category_id, visit_consultation_type, allow_rate_increase, allow_rate_decrease) VALUES (-2, 'A', 'OP Revisit Consultation', '', 'o', 'op_revisit_charge', -1, 'default', '2011-04-26 12:42:29.691353', 'ROPDOC', 1, -2, false, false);
INSERT INTO consultation_types (consultation_type_id, status, consultation_type, consultation_code, patient_type, doctor_charge_type, service_sub_group_id, username, mod_time, charge_head, insurance_category_id, visit_consultation_type, allow_rate_increase, allow_rate_decrease) VALUES (-4, 'A', 'IP Follow Up Consultation', '', 'o', 'op_revisit_charge', -1, 'default', '2011-04-26 12:42:29.694933', 'ROPDOC', 1, -4, false, false);
INSERT INTO consultation_types (consultation_type_id, status, consultation_type, consultation_code, patient_type, doctor_charge_type, service_sub_group_id, username, mod_time, charge_head, insurance_category_id, visit_consultation_type, allow_rate_increase, allow_rate_decrease) VALUES (-3, 'A', 'IP Doctor Visit', '', 'i', 'doctor_ip_charge', -1, 'default', '2011-04-26 12:42:29.693503', 'IPDOC', 1, -3, false, false);
INSERT INTO consultation_types (consultation_type_id, status, consultation_type, consultation_code, patient_type, doctor_charge_type, service_sub_group_id, username, mod_time, charge_head, insurance_category_id, visit_consultation_type, allow_rate_increase, allow_rate_decrease) VALUES (11, 'A', 'Surgeon Fees', '', 'ot', 'ot_charge', -1, 'default', '2011-04-26 12:42:29.701798', 'SUOPE', 1, 0, false, false);
INSERT INTO consultation_types (consultation_type_id, status, consultation_type, consultation_code, patient_type, doctor_charge_type, service_sub_group_id, username, mod_time, charge_head, insurance_category_id, visit_consultation_type, allow_rate_increase, allow_rate_decrease) VALUES (12, 'A', 'Anaesthetist Fees', '', 'ot', 'ot_charge', -1, 'default', '2011-04-26 12:42:29.702717', 'ANAOPE', 1, 0, false, false);
INSERT INTO consultation_types (consultation_type_id, status, consultation_type, consultation_code, patient_type, doctor_charge_type, service_sub_group_id, username, mod_time, charge_head, insurance_category_id, visit_consultation_type, allow_rate_increase, allow_rate_decrease) VALUES (13, 'A', 'Asst. Surgeon Fees', '', 'ot', 'assnt_surgeon_charge', -1, 'default', '2011-04-26 12:42:29.70341', 'ASUOPE', 1, 0, false, false);
INSERT INTO consultation_types (consultation_type_id, status, consultation_type, consultation_code, patient_type, doctor_charge_type, service_sub_group_id, username, mod_time, charge_head, insurance_category_id, visit_consultation_type, allow_rate_increase, allow_rate_decrease) VALUES (14, 'A', 'Asst. Anaesthetist Fees', '', 'ot', 'assnt_surgeon_charge', -1, 'default', '2011-04-26 12:42:29.704126', 'AANOPE', 1, 0, false, false);
INSERT INTO consultation_types (consultation_type_id, status, consultation_type, consultation_code, patient_type, doctor_charge_type, service_sub_group_id, username, mod_time, charge_head, insurance_category_id, visit_consultation_type, allow_rate_increase, allow_rate_decrease) VALUES (15, 'A', 'Co-op. Surgeon Fees', '', 'ot', 'co_surgeon_charge', -1, 'default', '2011-04-26 12:42:29.704823', 'COSOPE', 1, 0, false, false);

--
-- Data for Name: consumables_master; Type: TABLE DATA; 
--

--
-- Data for Name: contract_type_master; Type: TABLE DATA; 
--

--
-- Data for Name: contracts; Type: TABLE DATA; 
--

--
-- Data for Name: corporate_docs_details; Type: TABLE DATA; 
--

--
-- Data for Name: counters; Type: TABLE DATA; 
--

INSERT INTO counters (counter_id, counter_no, status, counter_type, collection_counter, center_id) VALUES ('CNT0001', 'BILLING COUNTER', 'A', 'B', 'Y', 0);
INSERT INTO counters (counter_id, counter_no, status, counter_type, collection_counter, center_id) VALUES ('CNT0002', 'PHARMACY COUNTER', 'A', 'P', 'Y', 0);

--
-- Data for Name: country_master; Type: TABLE DATA; 
--

INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0002', 'OTHER', 'I', NULL, NULL, NULL, NULL, NULL, 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0001', 'INDIA', 'I', '91', NULL, NULL, NULL, NULL, 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0003', 'Afghanistan', 'A', '93', 'AF', 'AFG', 'AFG', '004', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0004', 'Åland Islands', 'A', '358', 'AX', 'ALA', 'ALA', '248', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0005', 'Albania', 'A', '355', 'AL', 'ALB', 'ALB', '008', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0006', 'Algeria', 'A', '213', 'DZ', 'DZA', 'DZA', '012', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0007', 'American Samoa', 'A', '1684', 'AS', 'ASM', 'ASM', '016', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0008', 'Andorra', 'A', '376', 'AD', 'AND', 'AND', '020', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0009', 'Angola', 'A', '244', 'AO', 'AGO', 'AGO', '024', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0010', 'Anguilla', 'A', '1264', 'AI', 'AIA', 'AIA', '660', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0011', 'Antarctica', 'A', '672', 'AQ', 'ATA', 'ATA', '010', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0012', 'Antigua and Barbuda', 'A', '1268', 'AG', 'ATG', 'ATG', '028', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0013', 'Argentina', 'A', '54', 'AR', 'ARG', 'ARG', '032', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0014', 'Armenia', 'A', '374', 'AM', 'ARM', 'ARM', '051', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0015', 'Aruba', 'A', '297', 'AW', 'ABW', 'ABW', '533', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0016', 'Australia', 'A', '61', 'AU', 'AUS', 'AUS', '036', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0017', 'Austria', 'A', '43', 'AT', 'AUT', 'AUT', '040', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0018', 'Azerbaijan', 'A', '994', 'AZ', 'AZE', 'AZE', '031', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0019', 'Bahamas', 'A', '1242', 'BS', 'BHS', 'BHS', '044', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0020', 'Bahrain', 'A', '973', 'BH', 'BHR', 'BHR', '048', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0021', 'Bangladesh', 'A', '880', 'BD', 'BGD', 'BGD', '050', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0022', 'Barbados', 'A', '1246', 'BB', 'BRB', 'BRB', '052', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0023', 'Belarus', 'A', '375', 'BY', 'BLR', 'BLR', '112', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0024', 'Belgium', 'A', '32', 'BE', 'BEL', 'BEL', '056', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0025', 'Belize', 'A', '501', 'BZ', 'BLZ', 'BLZ', '084', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0026', 'Benin', 'A', '229', 'BJ', 'BEN', 'BEN', '204', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0027', 'Bermuda', 'A', '1441', 'BM', 'BMU', 'BMU', '060', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0028', 'Bhutan', 'A', '975', 'BT', 'BTN', 'BTN', '064', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0029', 'Bolivia', 'A', '591', 'BO', 'BOL', 'BOL', '068', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0030', 'Bonaire, Sint Eustatius and Saba', 'A', '599', 'BQ', 'BES', 'BES', '535', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0031', 'Bosnia and Herzegovina', 'A', '387', 'BA', 'BIH', 'BIH', '070', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0032', 'Botswana', 'A', '267', 'BW', 'BWA', 'BWA', '072', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0033', 'Bouvet Island', 'A', '47', 'BV', 'BVT', 'BVT', '074', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0034', 'Brazil', 'A', '55', 'BR', 'BRA', 'BRA', '076', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0035', 'British Indian Ocean Territory', 'A', '246', 'IO', 'IOT', 'IOT', '086', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0036', 'Brunei Darussalam', 'A', '673', 'BN', 'BRN', 'BRN', '096', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0037', 'Bulgaria', 'A', '359', 'BG', 'BGR', 'BGR', '100', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0038', 'Burkina Faso', 'A', '226', 'BF', 'BFA', 'BFA', '854', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0039', 'Burundi', 'A', '257', 'BI', 'BDI', 'BDI', '108', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0040', 'Cabo Verde', 'A', '238', 'CV', 'CPV', 'CPV', '132', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0041', 'Cambodia', 'A', '855', 'KH', 'KHM', 'KHM', '116', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0042', 'Cameroon', 'A', '237', 'CM', 'CMR', 'CMR', '120', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0043', 'Canada', 'A', '1', 'CA', 'CAN', 'CAN', '124', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0044', 'Cayman Islands', 'A', '1345', 'KY', 'CYM', 'CYM', '136', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0045', 'Central African Republic', 'A', '236', 'CF', 'CAF', 'CAF', '140', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0046', 'Chad', 'A', '235', 'TD', 'TCD', 'TCD', '148', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0047', 'Chile', 'A', '56', 'CL', 'CHL', 'CHL', '152', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0048', 'China', 'A', '86', 'CN', 'CHN', 'CHN', '156', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0049', 'Christmas Island', 'A', '61', 'CX', 'CXR', 'CXR', '162', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0050', 'Cocos (Keeling) Islands', 'A', '61891', 'CC', 'CCK', 'CCK', '166', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0051', 'Colombia', 'A', '57', 'CO', 'COL', 'COL', '170', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0052', 'Comoros', 'A', '269', 'KM', 'COM', 'COM', '174', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0053', 'Congo', 'A', '242', 'CG', 'COG', 'COG', '178', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0054', 'Congo (Democratic Republic of the)', 'A', '243', 'CD', 'COD', 'COD', '180', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0055', 'Cook Islands', 'A', '682', 'CK', 'COK', 'COK', '184', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0056', 'Costa Rica', 'A', '506', 'CR', 'CRI', 'CRI', '188', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0057', 'Cote d Ivoire', 'A', '225', 'CI', 'CIV', 'CIV', '384', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0058', 'Croatia', 'A', '385', 'HR', 'HRV', 'HRV', '191', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0059', 'Cuba', 'A', '53', 'CU', 'CUB', 'CUB', '192', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0060', 'Curaçao', 'A', '5999', 'CW', 'CUW', 'CUW', '531', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0061', 'Cyprus', 'A', '357', 'CY', 'CYP', 'CYP', '196', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0062', 'Czechia', 'A', '420', 'CZ', 'CZE', 'CZE', '203', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0063', 'Denmark', 'A', '45', 'DK', 'DNK', 'DNK', '208', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0064', 'Djibouti', 'A', '253', 'DJ', 'DJI', 'DJI', '262', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0065', 'Dominica', 'A', '1767', 'DM', 'DMA', 'DMA', '212', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0066', 'Dominican Republic', 'A', '1809', 'DO', 'DOM', 'DOM', '214', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0067', 'Ecuador', 'A', '593', 'EC', 'ECU', 'ECU', '218', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0068', 'Egypt', 'A', '20', 'EG', 'EGY', 'EGY', '818', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0069', 'El Salvador', 'A', '503', 'SV', 'SLV', 'SLV', '222', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0070', 'Equatorial Guinea', 'A', '240', 'GQ', 'GNQ', 'GNQ', '226', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0071', 'Eritrea', 'A', '291', 'ER', 'ERI', 'ERI', '232', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0072', 'Estonia', 'A', '372', 'EE', 'EST', 'EST', '233', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0073', 'Ethiopia', 'A', '251', 'ET', 'ETH', 'ETH', '231', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0074', 'Falkland Islands (Malvinas)', 'A', '500', 'FK', 'FLK', 'FLK', '238', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0075', 'Faroe Islands', 'A', '298', 'FO', 'FRO', 'FRO', '234', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0076', 'Fiji', 'A', '679', 'FJ', 'FJI', 'FJI', '242', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0077', 'Finland', 'A', '358', 'FI', 'FIN', 'FIN', '246', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0078', 'France', 'A', '33', 'FR', 'FRA', 'FRA', '250', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0079', 'French Guiana', 'A', '594', 'GF', 'GUF', 'GUF', '254', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0080', 'French Polynesia', 'A', '689', 'PF', 'PYF', 'PYF', '258', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0081', 'French Southern Territories', 'A', '262', 'TF', 'ATF', 'ATF', '260', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0082', 'Gabon', 'A', '241', 'GA', 'GAB', 'GAB', '266', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0083', 'Gambia', 'A', '220', 'GM', 'GMB', 'GMB', '270', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0084', 'Georgia', 'A', '995', 'GE', 'GEO', 'GEO', '268', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0085', 'Germany', 'A', '49', 'DE', 'DEU', 'D', '276', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0086', 'Ghana', 'A', '233', 'GH', 'GHA', 'GHA', '288', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0087', 'Gibraltar', 'A', '350', 'GI', 'GIB', 'GIB', '292', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0088', 'Greece', 'A', '30', 'GR', 'GRC', 'GRC', '300', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0089', 'Greenland', 'A', '299', 'GL', 'GRL', 'GRL', '304', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0090', 'Grenada', 'A', '1473', 'GD', 'GRD', 'GRD', '308', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0091', 'Guadeloupe', 'A', '590', 'GP', 'GLP', 'GLP', '312', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0092', 'Guam', 'A', '1671', 'GU', 'GUM', 'GUM', '316', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0093', 'Guatemala', 'A', '502', 'GT', 'GTM', 'GTM', '320', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0094', 'Guernsey', 'A', '44', 'GG', 'GGY', 'GGY', '831', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0095', 'Guinea', 'A', '224', 'GN', 'GIN', 'GIN', '324', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0096', 'Guinea-Bissau', 'A', '245', 'GW', 'GNB', 'GNB', '624', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0097', 'Guyana', 'A', '592', 'GY', 'GUY', 'GUY', '328', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0098', 'Haiti', 'A', '509', 'HT', 'HTI', 'HTI', '332', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0099', 'Heard Island and McDonald Islands', 'A', '61', 'HM', 'HMD', 'HMD', '334', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0100', 'Vatican City', 'A', '379', 'VA', 'VAT', 'VAT', '336', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0101', 'Honduras', 'A', '504', 'HN', 'HND', 'HND', '340', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0102', 'Hong Kong', 'A', '852', 'HK', 'HKG', 'HKG', '344', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0103', 'Hungary', 'A', '36', 'HU', 'HUN', 'HUN', '348', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0104', 'Iceland', 'A', '354', 'IS', 'ISL', 'ISL', '352', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0105', 'India', 'A', '91', 'IN', 'IND', 'IND', '356', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0106', 'Indonesia', 'A', '62', 'ID', 'IDN', 'IDN', '360', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0107', 'Iran', 'A', '98', 'IR', 'IRN', 'IRN', '364', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0108', 'Iraq', 'A', '964', 'IQ', 'IRQ', 'IRQ', '368', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0109', 'Ireland', 'A', '353', 'IE', 'IRL', 'IRL', '372', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0110', 'Isle of Man', 'A', '44', 'IM', 'IMN', 'IMN', '833', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0111', 'Israel', 'A', '972', 'IL', 'ISR', 'ISR', '376', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0112', 'Italy', 'A', '39', 'IT', 'ITA', 'ITA', '380', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0113', 'Jamaica', 'A', '1876', 'JM', 'JAM', 'JAM', '388', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0114', 'Japan', 'A', '81', 'JP', 'JPN', 'JPN', '392', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0115', 'Jersey', 'A', '44', 'JE', 'JEY', 'JEY', '832', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0116', 'Jordan', 'A', '962', 'JO', 'JOR', 'JOR', '400', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0117', 'Kazakhstan', 'A', '76', 'KZ', 'KAZ', 'KAZ', '398', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0118', 'Kenya', 'A', '254', 'KE', 'KEN', 'KEN', '404', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0119', 'Kiribati', 'A', '686', 'KI', 'KIR', 'KIR', '296', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0120', 'North Korea', 'A', '850', 'KP', 'PRK', 'PRK', '408', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0121', 'South Korea', 'A', '82', 'KR', 'KOR', 'KOR', '410', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0122', 'Kuwait', 'A', '965', 'KW', 'KWT', 'KWT', '414', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0123', 'Kyrgyzstan', 'A', '996', 'KG', 'KGZ', 'KGZ', '417', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0124', 'Laos', 'A', '856', 'LA', 'LAO', 'LAO', '418', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0125', 'Latvia', 'A', '371', 'LV', 'LVA', 'LVA', '428', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0126', 'Lebanon', 'A', '961', 'LB', 'LBN', 'LBN', '422', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0127', 'Lesotho', 'A', '266', 'LS', 'LSO', 'LSO', '426', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0128', 'Liberia', 'A', '231', 'LR', 'LBR', 'LBR', '430', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0129', 'Libya', 'A', '218', 'LY', 'LBY', 'LBY', '434', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0130', 'Liechtenstein', 'A', '423', 'LI', 'LIE', 'LIE', '438', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0131', 'Lithuania', 'A', '370', 'LT', 'LTU', 'LTU', '440', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0132', 'Luxembourg', 'A', '352', 'LU', 'LUX', 'LUX', '442', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0133', 'Macao', 'A', '853', 'MO', 'MAC', 'MAC', '446', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0134', 'Macedonia', 'A', '389', 'MK', 'MKD', 'MKD', '807', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0135', 'Madagascar', 'A', '261', 'MG', 'MDG', 'MDG', '450', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0136', 'Malawi', 'A', '265', 'MW', 'MWI', 'MWI', '454', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0137', 'Malaysia', 'A', '60', 'MY', 'MYS', 'MYS', '458', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0138', 'Maldives', 'A', '960', 'MV', 'MDV', 'MDV', '462', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0139', 'Mali', 'A', '223', 'ML', 'MLI', 'MLI', '466', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0140', 'Malta', 'A', '356', 'MT', 'MLT', 'MLT', '470', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0141', 'Marshall Islands', 'A', '692', 'MH', 'MHL', 'MHL', '584', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0142', 'Martinique', 'A', '596', 'MQ', 'MTQ', 'MTQ', '474', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0143', 'Mauritania', 'A', '222', 'MR', 'MRT', 'MRT', '478', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0144', 'Mauritius', 'A', '230', 'MU', 'MUS', 'MUS', '480', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0145', 'Mayotte', 'A', '262', 'YT', 'MYT', 'MYT', '175', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0146', 'Mexico', 'A', '52', 'MX', 'MEX', 'MEX', '484', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0147', 'Micronesia', 'A', '691', 'FM', 'FSM', 'FSM', '583', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0148', 'Moldova', 'A', '373', 'MD', 'MDA', 'MDA', '498', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0149', 'Monaco', 'A', '377', 'MC', 'MCO', 'MCO', '492', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0150', 'Mongolia', 'A', '976', 'MN', 'MNG', 'MNG', '496', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0151', 'Montenegro', 'A', '382', 'ME', 'MNE', 'MNE', '499', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0152', 'Montserrat', 'A', '1664', 'MS', 'MSR', 'MSR', '500', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0153', 'Morocco', 'A', '212', 'MA', 'MAR', 'MAR', '504', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0154', 'Mozambique', 'A', '258', 'MZ', 'MOZ', 'MOZ', '508', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0155', 'Myanmar', 'A', '95', 'MM', 'MMR', 'MMR', '104', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0156', 'Namibia', 'A', '264', 'NA', 'NAM', 'NAM', '516', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0157', 'Nauru', 'A', '674', 'NR', 'NRU', 'NRU', '520', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0158', 'Nepal', 'A', '977', 'NP', 'NPL', 'NPL', '524', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0159', 'Netherlands', 'A', '31', 'NL', 'NLD', 'NLD', '528', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0160', 'New Caledonia', 'A', '687', 'NC', 'NCL', 'NCL', '540', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0161', 'New Zealand', 'A', '64', 'NZ', 'NZL', 'NZL', '554', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0162', 'Nicaragua', 'A', '505', 'NI', 'NIC', 'NIC', '558', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0163', 'Niger', 'A', '227', 'NE', 'NER', 'NER', '562', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0164', 'Nigeria', 'A', '234', 'NG', 'NGA', 'NGA', '566', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0165', 'Niue', 'A', '683', 'NU', 'NIU', 'NIU', '570', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0166', 'Norfolk Island', 'A', '672', 'NF', 'NFK', 'NFK', '574', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0167', 'Northern Mariana Islands', 'A', '1670', 'MP', 'MNP', 'MNP', '580', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0168', 'Norway', 'A', '47', 'NO', 'NOR', 'NOR', '578', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0169', 'Oman', 'A', '968', 'OM', 'OMN', 'OMN', '512', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0170', 'Pakistan', 'A', '92', 'PK', 'PAK', 'PAK', '586', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0171', 'Palau', 'A', '680', 'PW', 'PLW', 'PLW', '585', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0172', 'Palestine', 'A', '970', 'PS', 'PSE', 'PSE', '275', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0173', 'Panama', 'A', '507', 'PA', 'PAN', 'PAN', '591', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0174', 'Papua New Guinea', 'A', '675', 'PG', 'PNG', 'PNG', '598', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0175', 'Paraguay', 'A', '595', 'PY', 'PRY', 'PRY', '600', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0176', 'Peru', 'A', '51', 'PE', 'PER', 'PER', '604', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0177', 'Philippines', 'A', '63', 'PH', 'PHL', 'PHL', '608', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0178', 'Pitcairn', 'A', '64', 'PN', 'PCN', 'PCN', '612', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0179', 'Poland', 'A', '48', 'PL', 'POL', 'POL', '616', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0180', 'Portugal', 'A', '351', 'PT', 'PRT', 'PRT', '620', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0181', 'Puerto Rico', 'A', '1787', 'PR', 'PRI', 'PRI', '630', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0182', 'Qatar', 'A', '974', 'QA', 'QAT', 'QAT', '634', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0183', 'Réunion', 'A', '262', 'RE', 'REU', 'REU', '638', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0184', 'Romania', 'A', '40', 'RO', 'ROU', 'ROU', '642', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0185', 'Russian Federation', 'A', '7', 'RU', 'RUS', 'RUS', '643', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0186', 'Rwanda', 'A', '250', 'RW', 'RWA', 'RWA', '646', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0187', 'Saint Barthélemy', 'A', '590', 'BL', 'BLM', 'BLM', '652', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0188', 'Saint Helena, Ascension and Tristan da Cunha', 'A', '290', 'SH', 'SHN', 'SHN', '654', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0189', 'Saint Kitts and Nevis', 'A', '1869', 'KN', 'KNA', 'KNA', '659', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0190', 'Saint Lucia', 'A', '1758', 'LC', 'LCA', 'LCA', '662', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0191', 'Saint Martin (French)', 'A', '590', 'MF', 'MAF', 'MAF', '663', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0192', 'Saint Pierre and Miquelon', 'A', '508', 'PM', 'SPM', 'SPM', '666', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0193', 'Saint Vincent and the Grenadines', 'A', '1784', 'VC', 'VCT', 'VCT', '670', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0194', 'Samoa', 'A', '685', 'WS', 'WSM', 'WSM', '882', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0195', 'San Marino', 'A', '378', 'SM', 'SMR', 'SMR', '674', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0196', 'Sao Tome and Principe', 'A', '239', 'ST', 'STP', 'STP', '678', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0197', 'Saudi Arabia', 'A', '966', 'SA', 'SAU', 'SAU', '682', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0198', 'Senegal', 'A', '221', 'SN', 'SEN', 'SEN', '686', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0199', 'Serbia', 'A', '381', 'RS', 'SRB', 'SRB', '688', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0200', 'Seychelles', 'A', '248', 'SC', 'SYC', 'SYC', '690', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0201', 'Sierra Leone', 'A', '232', 'SL', 'SLE', 'SLE', '694', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0202', 'Singapore', 'A', '65', 'SG', 'SGP', 'SGP', '702', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0203', 'Sint Maarten (Dutch)', 'A', '1721', 'SX', 'SXM', 'SXM', '534', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0204', 'Slovakia', 'A', '421', 'SK', 'SVK', 'SVK', '703', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0205', 'Slovenia', 'A', '386', 'SI', 'SVN', 'SVN', '705', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0206', 'Solomon Islands', 'A', '677', 'SB', 'SLB', 'SLB', '090', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0207', 'Somalia', 'A', '252', 'SO', 'SOM', 'SOM', '706', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0208', 'South Africa', 'A', '27', 'ZA', 'ZAF', 'ZAF', '710', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0209', 'South Georgia and the South Sandwich Islands', 'A', '500', 'GS', 'SGS', 'SGS', '239', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0210', 'South Sudan', 'A', '211', 'SS', 'SSD', 'SSD', '728', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0211', 'Spain', 'A', '34', 'ES', 'ESP', 'ESP', '724', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0212', 'Sri Lanka', 'A', '94', 'LK', 'LKA', 'LKA', '144', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0213', 'Sudan', 'A', '249', 'SD', 'SDN', 'SDN', '729', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0214', 'Suriname', 'A', '597', 'SR', 'SUR', 'SUR', '740', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0215', 'Svalbard and Jan Mayen', 'A', '47', 'SJ', 'SJM', 'SJM', '744', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0216', 'Swaziland', 'A', '268', 'SZ', 'SWZ', 'SWZ', '748', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0217', 'Sweden', 'A', '46', 'SE', 'SWE', 'SWE', '752', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0218', 'Switzerland', 'A', '41', 'CH', 'CHE', 'CHE', '756', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0219', 'Syrian Arab Republic', 'A', '963', 'SY', 'SYR', 'SYR', '760', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0220', 'Taiwan', 'A', '886', 'TW', 'TWN', 'TWN', '158', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0221', 'Tajikistan', 'A', '992', 'TJ', 'TJK', 'TJK', '762', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0222', 'Tanzania', 'A', '255', 'TZ', 'TZA', 'TZA', '834', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0223', 'Thailand', 'A', '66', 'TH', 'THA', 'THA', '764', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0224', 'Timor-Leste', 'A', '670', 'TL', 'TLS', 'TLS', '626', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0225', 'Togo', 'A', '228', 'TG', 'TGO', 'TGO', '768', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0226', 'Tokelau', 'A', '690', 'TK', 'TKL', 'TKL', '772', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0227', 'Tonga', 'A', '676', 'TO', 'TON', 'TON', '776', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0228', 'Trinidad and Tobago', 'A', '1868', 'TT', 'TTO', 'TTO', '780', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0229', 'Tunisia', 'A', '216', 'TN', 'TUN', 'TUN', '788', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0230', 'Turkey', 'A', '90', 'TR', 'TUR', 'TUR', '792', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0231', 'Turkmenistan', 'A', '993', 'TM', 'TKM', 'TKM', '795', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0232', 'Turks and Caicos Islands', 'A', '1649', 'TC', 'TCA', 'TCA', '796', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0233', 'Tuvalu', 'A', '688', 'TV', 'TUV', 'TUV', '798', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0234', 'Uganda', 'A', '256', 'UG', 'UGA', 'UGA', '800', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0235', 'Ukraine', 'A', '380', 'UA', 'UKR', 'UKR', '804', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0236', 'United Arab Emirates', 'A', '971', 'AE', 'ARE', 'ARE', '784', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0237', 'United Kingdom', 'A', '44', 'GB', 'GBR', 'GBR', '826', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0238', 'United States of America', 'A', '1', 'US', 'USA', 'USA', '840', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0239', 'US Minor Outlying Islands', 'A', '963', 'UM', 'UMI', 'UMI', '581', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0240', 'Uruguay', 'A', '598', 'UY', 'URY', 'URY', '858', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0241', 'Uzbekistan', 'A', '998', 'UZ', 'UZB', 'UZB', '860', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0242', 'Vanuatu', 'A', '678', 'VU', 'VUT', 'VUT', '548', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0243', 'Venezuela', 'A', '58', 'VE', 'VEN', 'VEN', '862', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0244', 'Viet Nam', 'A', '84', 'VN', 'VNM', 'VNM', '704', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0245', 'Virgin Islands (British)', 'A', '1284', 'VG', 'VGB', 'VGB', '092', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0246', 'Virgin Islands (U.S.)', 'A', '1340', 'VI', 'VIR', 'VIR', '850', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0247', 'Wallis and Futuna', 'A', '681', 'WF', 'WLF', 'WLF', '876', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0248', 'Western Sahara', 'A', '212', 'EH', 'ESH', 'ESH', '732', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0249', 'Yemen', 'A', '967', 'YE', 'YEM', 'YEM', '887', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0250', 'Zambia', 'A', '260', 'ZM', 'ZMB', 'ZMB', '894', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0251', 'Zimbabwe', 'A', '263', 'ZW', 'ZWE', 'ZWE', '716', 'f');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0252', 'European Union', 'A', NULL, '', '', 'EUE', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0253', 'British Overseas Territories Citizen', 'A', NULL, '', '', 'GBD', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0254', 'British National', 'A', NULL, '', '', 'GBN', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0255', 'British Overseas Citizen', 'A', NULL, '', '', 'GBO', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0256', 'British Protected Person', 'A', NULL, '', '', 'GBP', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0257', 'British Subject', 'A', NULL, '', '', 'GBS', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0258', 'United Nations Organization', 'A', NULL, '', '', 'UNO', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0259', 'United Nations Specialized Agency', 'A', NULL, '', '', 'UNA', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0260', 'Resident of Kosovo', 'A', NULL, '', '', 'UNK', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0261', 'African Development Bank', 'A', NULL, '', '', 'XBA', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0262', 'African Export-Import Bank', 'A', NULL, '', '', 'XIM', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0263', 'Caribbean Community or one of its emissaries', 'A', NULL, '', '', 'XCC', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0264', 'Common Market for Eastern and Southern Africa', 'A', NULL, '', '', 'XCO', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0265', 'Economic Community of West African States', 'A', NULL, '', '', 'XEC', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0266', 'International Criminal Police Organization', 'A', NULL, '', '', 'XPO', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0267', 'Sovereign Military Order of Malta', 'A', NULL, '', '', 'XOM', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0268', 'Stateless Person', 'A', NULL, '', '', 'XXA', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0269', 'Refugee as defined in Article 1 of the 1951', 'A', NULL, '', '', 'XXB', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0270', 'Refugee', 'A', NULL, '', '', 'XXC', '', 't');
INSERT INTO country_master (country_id, country_name, status, country_code, alpha2_code, alpha3_code, icao_alpha3_code, numeric_code, nationality) VALUES ('CM0271', 'Person of unspecified nationality', 'A', NULL, '', '', 'XXX', '', 't');

--
-- Data for Name: cron_details; Type: TABLE DATA; 
--

INSERT INTO cron_details (cron_name, last_exec_time) VALUES ('PractoMessageStatusUpdateJob', NULL);
INSERT INTO cron_details (cron_name, last_exec_time) VALUES ('CEODashboardJob', NULL);

--
-- Data for Name: cron_job; Type: TABLE DATA; 
--

INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (2, 'UpdateStatsMessageJob', 'UpdateStatsMessageJob', '0 9 * * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, '*:09:00 Hourly check of data for Stats messages');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (3, 'ResetSequenceJob', 'ResetSequenceJob', '0 0 0 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, '00:00:00 daily: Exactly at midnight every day: reset sequences');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (4, 'ResetDeptTokensJob', 'ResetDeptTokensJob', '0 10 6 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, '06:10:00 daily: reset department wise tokens for tests');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (5, 'CasefileAutoCloseJob', 'CasefileAutoCloseJob', '0 11 6 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, '06:11:00 daily: close casefile indents automatically');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (6, 'SetAppoitmentJob', 'SetAppoitmentJob', '0 12 6 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, '06:12:00 daily: scheduler appointments: mark as no show any open appointments');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (8, 'CloseNoChargeBillsJob', 'CloseNoChargeBillsJob', '0 20 6 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, '06:20:00 daily: Close any bills which have no charges');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (9, 'PackageJob', 'PackageJob', '0 40 6 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, '06:40:00 daily: Activates/Inactivates packages.');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (17, 'FinalizeOpenOPBills', 'FinalizeOpenOPBills', '0 10 3 * * ?', NULL, NULL, 'Y', 'I', NULL, NULL, NULL, '03:10:00 daily: Finalized Yesterday open OP bills');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (10, 'OpDeactivateJob', 'OpDeactivateJob', '0 10 5 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, '05:10:00 daily: deactivate op patients automatically');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (11, 'RateMasterJob', 'RateMasterJob', '0 20 5 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, '05:20:00 daily: Clean up any problems with rate masters');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (12, 'NextDayAppointmentJob', 'NextDayAppointmentJob', '0 0 8 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, '08:00:00 daily: Send an Appointment Reminder one day before the Appointment(Automatic)');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (18, 'VaccReminderJob', 'VaccReminderJob', '0 30 8 * * ?', NULL, NULL, 'Y', 'I', NULL, NULL, NULL, '08:30:00 daily: Send message to patient to remind him/her about the upcoming vaccine ');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (19, 'AutoPOGeneratorJob', 'AutoPOGeneratorJob', '0 33 16 * * ?', NULL, NULL, 'Y', 'I', NULL, NULL, NULL, '02:00:00 daily: Raise Auto PO based on store frequency');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (20, 'AutoPOCancelJob', 'AutoPOCancelJob', '0 19 17 * * ?', NULL, NULL, 'Y', 'I', NULL, NULL, NULL, '02:00:00 daily: Cancel PO based on store cancel frequency days');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (21, 'SponsorApprovalsJob', 'SponsorApprovalsJob', '0 25 2 * * ?', NULL, NULL, 'Y', 'I', NULL, NULL, NULL, 'Process Sponsor Approvals Cleanup');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (13, 'PractoMessageStatusUpdateJob', 'PractoMessageStatusUpdateJob', '0 30 6 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, 'To update the status of messages from practo communicator');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (14, 'WeeklyEmailReportJob', 'WeeklyEmailReportJob', '0 10 0 ? * Mon', 'Weekly,/tmp', NULL, 'Y', 'A', NULL, NULL, NULL, 'WeeklyEmailReport Weekly is an event and /tmp is a work folder for storing email report while it is running in debug');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (22, 'DailyCollectionSMSJob', 'DailyCollectionSMSJob', '0 00 07 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, '00:07:00 daily: Send a message to hospital management regarding yesterday collection. The message will be sent only if the daily collection message type is active.');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (23, 'CEODashboardJob', 'CEODashboardJob', '0 0 */4 * * ?', NULL, NULL, 'Y', 'I', NULL, NULL, NULL, 'Every 4 hours: This job empowers CEO Dashboard. Please enable only if hospital wants to have CEO Dashboard');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (7, 'DailyEmailReportJob', 'DailyEmailReportJob', '0 05 0 * * ?', 'Daily,/tmp', NULL, 'Y', 'A', NULL, NULL, NULL, 'DailyEmailReport Daily at (00:05:00) is an event and /tmp is a work folder for storing email report while it is running in debug mode.');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (16, 'MonthlyEmailReportJob', 'MonthlyEmailReportJob', '0 13 0 1 * ?', 'Monthly,/tmp', NULL, 'Y', 'A', NULL, NULL, NULL, 'MonthlyEmailReport Monthly at (00:13:00) is an event and /tmp is a work folder for storing email report while it is running in debug mode.');
INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (24, 'PatientTokenGenerator', 'PatientTokenGenerator', '0 59 23 * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, NULL);

--
-- Data for Name: crown_status_master; Type: TABLE DATA; 
--

INSERT INTO crown_status_master (crown_status_id, crown_status_desc, color_code, status) VALUES (1, 'Bridge', '#0CEF0C', 'A');
INSERT INTO crown_status_master (crown_status_id, crown_status_desc, color_code, status) VALUES (2, 'Crown', '#EF0C27', 'A');
INSERT INTO crown_status_master (crown_status_id, crown_status_desc, color_code, status) VALUES (3, 'Denture', '#120CEF', 'A');
INSERT INTO crown_status_master (crown_status_id, crown_status_desc, color_code, status) VALUES (4, 'Missing', '#E20CEF', 'A');
INSERT INTO crown_status_master (crown_status_id, crown_status_desc, color_code, status) VALUES (5, 'Veneer', '#EFD70C', 'A');
INSERT INTO crown_status_master (crown_status_id, crown_status_desc, color_code, status) VALUES (6, 'Over denture', '#ECEAEB', 'A');
INSERT INTO crown_status_master (crown_status_id, crown_status_desc, color_code, status) VALUES (7, 'Condition', '#84DCE2', 'A');

--
-- Data for Name: custom_list1_master; Type: TABLE DATA; 
--

--
-- Data for Name: custom_list2_master; Type: TABLE DATA; 
--

--
-- Data for Name: custom_list3_master; Type: TABLE DATA; 
--

--
-- Data for Name: custom_list4_master; Type: TABLE DATA; 
--

INSERT INTO custom_list4_master (custom_value, status) VALUES ('O+', 'A');
INSERT INTO custom_list4_master (custom_value, status) VALUES ('O-', 'A');
INSERT INTO custom_list4_master (custom_value, status) VALUES ('A+', 'A');
INSERT INTO custom_list4_master (custom_value, status) VALUES ('A-', 'A');
INSERT INTO custom_list4_master (custom_value, status) VALUES ('B+', 'A');
INSERT INTO custom_list4_master (custom_value, status) VALUES ('B-', 'A');
INSERT INTO custom_list4_master (custom_value, status) VALUES ('AB+', 'A');
INSERT INTO custom_list4_master (custom_value, status) VALUES ('AB-', 'A');

--
-- Data for Name: custom_list5_master; Type: TABLE DATA; 
--

INSERT INTO custom_list5_master (custom_value, status) VALUES ('HINDU', 'A');
INSERT INTO custom_list5_master (custom_value, status) VALUES ('MUSLIM', 'A');
INSERT INTO custom_list5_master (custom_value, status) VALUES ('SIKH', 'A');
INSERT INTO custom_list5_master (custom_value, status) VALUES ('CHRISTIAN', 'A');
INSERT INTO custom_list5_master (custom_value, status) VALUES ('JAIN', 'A');
INSERT INTO custom_list5_master (custom_value, status) VALUES ('OTHER', 'A');

--
-- Data for Name: custom_list6_master; Type: TABLE DATA; 
--

INSERT INTO custom_list6_master (custom_value, status) VALUES ('BUSINESS', 'A');
INSERT INTO custom_list6_master (custom_value, status) VALUES ('SALARIED', 'A');
INSERT INTO custom_list6_master (custom_value, status) VALUES ('SELF EMPLOYED', 'A');

--
-- Data for Name: custom_list7_master; Type: TABLE DATA; 
--

--
-- Data for Name: custom_list8_master; Type: TABLE DATA; 
--

--
-- Data for Name: custom_list9_master; Type: TABLE DATA; 
--

--
-- Data for Name: custom_report_rights; Type: TABLE DATA; 
--

--
-- Data for Name: custom_reports; Type: TABLE DATA; 
--

--
-- Data for Name: custom_report_variables; Type: TABLE DATA; 
--

--
-- Data for Name: custom_views; Type: TABLE DATA; 
--

--
-- Data for Name: custom_visit_list1_master; Type: TABLE DATA; 
--

--
-- Data for Name: custom_visit_list2_master; Type: TABLE DATA; 
--

--
-- Data for Name: customer_preferences; Type: TABLE DATA; 
--

--
-- Data for Name: death_reason_master; Type: TABLE DATA; 
--

--
-- Data for Name: dental_shades_master; Type: TABLE DATA; 
--

--
-- Data for Name: dental_supplier_master; Type: TABLE DATA; 
--

--
-- Data for Name: dental_supplies_master; Type: TABLE DATA; 
--

--
-- Data for Name: dental_supplier_item_rate_master; Type: TABLE DATA; 
--

--
-- Data for Name: dental_supplies_order; Type: TABLE DATA; 
--

--
-- Data for Name: dental_supplies_items; Type: TABLE DATA; 
--

--
-- Data for Name: department; Type: TABLE DATA; 
--

INSERT INTO department (dept_id, dept_name, status, allowed_gender, cost_center_code, dept_type_id) VALUES ('DEP_RAD', 'Radiology', 'A', 'ALL', '', NULL);
INSERT INTO department (dept_id, dept_name, status, allowed_gender, cost_center_code, dept_type_id) VALUES ('DEP_LAB', 'Laboratory', 'A', 'ALL', '', NULL);
INSERT INTO department (dept_id, dept_name, status, allowed_gender, cost_center_code, dept_type_id) VALUES ('DEP_PHA', 'Pharmacy', 'A', 'ALL', '', NULL);
INSERT INTO department (dept_id, dept_name, status, allowed_gender, cost_center_code, dept_type_id) VALUES ('DEP0001', 'General', 'A', 'ALL', '', NULL);
INSERT INTO department (dept_id, dept_name, status, allowed_gender, cost_center_code, dept_type_id) VALUES ('DEP0002', 'Anaesthesiology', 'A', 'ALL', '', NULL);

--
-- Data for Name: department_type_master; Type: TABLE DATA; 
--

INSERT INTO department_type_master (dept_type_id, dept_type_desc, status) VALUES ('DENT', 'Dental', 'A');
INSERT INTO department_type_master (dept_type_id, dept_type_desc, status) VALUES ('NOCL', 'Non Clinical', 'A');
INSERT INTO department_type_master (dept_type_id, dept_type_desc, status) VALUES ('OTH', 'Others', 'A');
INSERT INTO department_type_master (dept_type_id, dept_type_desc, status) VALUES ('OBST', 'Obstetrics', 'A');

--
-- Data for Name: deposit_receipt_refund_template; Type: TABLE DATA; 
--

--
-- Data for Name: deposit_setoff_total; Type: TABLE DATA; 
--

--
-- Data for Name: deposits_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: dept_package_applicability; Type: TABLE DATA; 
--

--
-- Data for Name: dept_unit_master; Type: TABLE DATA; 
--

--
-- Data for Name: diag_methodology_master; Type: TABLE DATA; 
--

--
-- Data for Name: diag_outsource_detail; Type: TABLE DATA; 
--

--
-- Data for Name: diag_outsource_master; Type: TABLE DATA; 
--

--
-- Data for Name: diag_states_master; Type: TABLE DATA; 
--

INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Newly ordered test', 'N', 'New', 1, 'S', 1, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Patient has arrived', 'MA', 'Modality Arrived', 1, 'N', 2, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Conduction is completed', 'CC', 'Conduction completed', 1, 'N', 3, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Report writing can start', 'TS', 'Scheduled for transcriptionist', 1, 'N', 4, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Report writing in progress', 'P', 'Report in Progress', 1, 'N', 5, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Report Completed', 'C', 'Report Completed', 1, 'N', 6, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Report validated', 'V', 'Validated', 1, 'N', 8, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Report requires correction', 'CR', 'Change Required', -1, 'N', -1, 'I');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Signoff', 'S', 'Signoff', 1, 'O', 1, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Amendment started', 'RP', 'Revision in progress', 1, 'R', 1, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Amendment completed', 'RC', 'Revision completed', 1, 'R', 2, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Amendment validated', 'RV', 'Revision validated', 1, 'R', 3, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Test cancelled', 'X', 'Cancelled', 0, 'S', 1, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Reconduction after signoff', 'RAS', 'Reconduction after signoff', 0, 'S', 0, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Reconduction before signoff', 'RBS', 'Reconduction before signoff', 0, 'S', 0, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('No conduction required', 'U', 'No conduction required', 0, 'S', 0, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('New (No Results)', 'NRN', 'New (No Results)', 0, 'S', 0, 'A');
INSERT INTO diag_states_master (state_desc, value, display_name, level, category, state_order, status) VALUES ('Completed (No Results)', 'CRN', 'Completed (No Results)', 0, 'S', 0, 'A');

--
-- Data for Name: diag_tat_center_master; Type: TABLE DATA; 
--

--
-- Data for Name: diag_test_timestamp; Type: TABLE DATA; 
--

INSERT INTO diag_test_timestamp (test_timestamp) VALUES (1);

--
-- Data for Name: diagnosis_statuses; Type: TABLE DATA; 
--

--
-- Data for Name: diagnostic_charges; Type: TABLE DATA; 
--

--
-- Data for Name: diagnostic_charges_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: diagnostic_charges_backup; Type: TABLE DATA; 
--

--
-- Data for Name: diagnostic_department_stores; Type: TABLE DATA; 
--

INSERT INTO diagnostic_department_stores (ddept_id, center_id, store_id, status) VALUES ('DDept0001', 0, -1, 'A');

--
-- Data for Name: diagnostic_outhouse_print_template; Type: TABLE DATA; 
--

--
-- Data for Name: diagnostic_reagent_usage; Type: TABLE DATA; 
--

--
-- Data for Name: diagnostics; Type: TABLE DATA; 
--

--
-- Data for Name: diagnostics_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: diagnostics_departments; Type: TABLE DATA; 
--

INSERT INTO diagnostics_departments (ddept_id, ddept_name, status, designation, category, display_order) VALUES ('DDept0001', 'Biochemistry', 'A', '', 'DEP_LAB', 1);

--
-- Data for Name: diagnostics_export_interface; Type: TABLE DATA; 
--

--
-- Data for Name: diagnostics_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: diagnostics_reagents; Type: TABLE DATA; 
--

--
-- Data for Name: dialysate_type; Type: TABLE DATA; 
--

INSERT INTO dialysate_type (dialysate_type_id, dialysate_type_name, potasium, calcium, magnesium, sodium, glucose, remarks, status) VALUES (1, '0K', 0.00, 2.60, 0.80, 140.00, 2.00, '', 'A');
INSERT INTO dialysate_type (dialysate_type_id, dialysate_type_name, potasium, calcium, magnesium, sodium, glucose, remarks, status) VALUES (2, '1K', 1.00, 2.50, 0.70, 140.00, 2.00, '', 'A');
INSERT INTO dialysate_type (dialysate_type_id, dialysate_type_name, potasium, calcium, magnesium, sodium, glucose, remarks, status) VALUES (3, '2K', 2.00, 2.00, 1.00, 140.00, 2.00, '', 'A');
INSERT INTO dialysate_type (dialysate_type_id, dialysate_type_name, potasium, calcium, magnesium, sodium, glucose, remarks, status) VALUES (4, '2K Glucose', 2.00, 1.50, 0.38, 140.00, 200.00, '', 'A');
INSERT INTO dialysate_type (dialysate_type_id, dialysate_type_name, potasium, calcium, magnesium, sodium, glucose, remarks, status) VALUES (5, '3K', 3.00, 2.50, 0.80, 140.00, 2.00, '', 'A');

--
-- Data for Name: dialysis_access_sites; Type: TABLE DATA; 
--

INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (1, 'Left i/j vein', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (2, 'Left Chest', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (3, 'Left Femoral', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (4, 'Left Forearm', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (5, 'Left Groin', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (6, 'Left Jugular', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (7, 'Left Neck', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (8, 'Left Side', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (9, 'Left Subclavian', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (10, 'Left Thigh', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (11, 'Left Upper Arm', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (12, 'Right i/j Vein', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (13, 'Right Chest', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (14, 'Right Femoral', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (15, 'Right Forearm', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (16, 'Right Groin', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (17, 'Right Jugular', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (18, 'Right Neck', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (19, 'Right Side', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (20, 'Right Subclavian', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (21, 'Right Thigh', '', 'A');
INSERT INTO dialysis_access_sites (access_site_id, access_site, description, status) VALUES (22, 'Right Upper arm', '', 'A');

--
-- Data for Name: dialysis_access_types; Type: TABLE DATA; 
--

INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (1, 'AV GRAFT', 'Arteriovenous Graft', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (2, 'AV GRAFT-Loop', 'AV graft in a loop configuration', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (3, 'AV GRAFT-STRAIGHT', 'AV graft in a straight line configuration', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (4, 'AV fistula', 'Radio-cephalic fistula', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (5, 'Brachio baslic', 'Transposed basilic vein in the upper arm', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (6, 'Brachiocephalic', 'AV fistula,halfway up on the arm(elbow)', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (7, 'Catheter-jugular', 'Catheter placed in either the internal or external jugular vein', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (8, 'Femoral', 'Catheter placed in the femoral vein', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (9, 'Fistula/Graft combination', 'Combination fistula and graft', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (10, 'Native AV Fistula', 'Arteriovenous Fistula', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (11, 'Necklace graft', 'Chest wall(necklace) prosthetic graft', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (12, 'PERITONEAL', '', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (13, 'PERMA-CATH', '', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (14, 'Temporary i/j catheter', 'Non-tunneled,non-cuffed catheter placed in the jugular vein to be used as a', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (15, 'Tesio', 'Twin single lumen venous catheters', 'A', 'P', 'P', 'AVG');
INSERT INTO dialysis_access_types (access_type_id, access_type, description, status, access_patency, access_mode, access_category) VALUES (16, 'Thigh Graft', 'Graft placed using femoral artery and vein', 'A', 'P', 'P', 'AVG');

--
-- Data for Name: dialysis_machine_master; Type: TABLE DATA; 
--

--
-- Data for Name: dialysis_machine_status; Type: TABLE DATA; 
--

--
-- Data for Name: dialysis_patient_drugs; Type: TABLE DATA; 
--

--
-- Data for Name: dialysis_prep_master; Type: TABLE DATA; 
--

INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (1, 'Sterilant present', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (2, 'Machine disinfected', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (3, 'Machine Rinsed', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (4, 'Dialyzer Rinsed', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (5, 'Dialyzer Primed', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (6, 'Sterilant Negative', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (7, 'Dialysate Comp.', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (8, 'Conductivity', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (9, 'Machine Test', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (10, 'Pressure Test', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (11, 'Alarm Test', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (12, 'Residual Blood Line Sterilant Test Negative', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (13, 'Saline Bag Sterilant Test Negative', 'pre', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (14, 'Air Detector Armed', 'post', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (15, 'Saline Clamped', 'post', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (16, 'Pressure Limits Set', 'post', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (17, 'Nurses'' round', 'post', 'A');
INSERT INTO dialysis_prep_master (prep_param_id, prep_param, prep_state, status) VALUES (18, 'Dialysate Counter Flow', 'post', 'A');

--
-- Data for Name: dialysis_prep_values; Type: TABLE DATA; 
--

--
-- Data for Name: dialysis_prescriptions; Type: TABLE DATA; 
--

--
-- Data for Name: dialysis_prescriptions_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: dialyzer_ratings; Type: TABLE DATA; 
--

INSERT INTO dialyzer_ratings (dialyzer_rating_id, dialyzer_rating, description, status) VALUES (1, 'Clear', 'Dialyzer headers and fibers are clear (no clots noted)', 'A');
INSERT INTO dialyzer_ratings (dialyzer_rating_id, dialyzer_rating, description, status) VALUES (2, 'Clotted', 'Dialyzer completely clotted-unable to return the blood in the dialyzer', 'A');
INSERT INTO dialyzer_ratings (dialyzer_rating_id, dialyzer_rating, description, status) VALUES (3, 'Streaked-few fibers', '10-20% of the fibers are clotted', 'A');
INSERT INTO dialyzer_ratings (dialyzer_rating_id, dialyzer_rating, description, status) VALUES (4, 'Streaked-several fibers', '>20-50% of the fibers are clotted', 'A');

--
-- Data for Name: dialyzer_types; Type: TABLE DATA; 
--

INSERT INTO dialyzer_types (dialyzer_type_id, dialyzer_type_name, description, status) VALUES (1, 'Dual EX 210', '', 'A');
INSERT INTO dialyzer_types (dialyzer_type_id, dialyzer_type_name, description, status) VALUES (2, 'F4', '', 'A');
INSERT INTO dialyzer_types (dialyzer_type_id, dialyzer_type_name, description, status) VALUES (3, 'F8', '', 'A');
INSERT INTO dialyzer_types (dialyzer_type_id, dialyzer_type_name, description, status) VALUES (4, 'FRESENIUS', '', 'A');
INSERT INTO dialyzer_types (dialyzer_type_id, dialyzer_type_name, description, status) VALUES (5, 'HF80S', '', 'A');
INSERT INTO dialyzer_types (dialyzer_type_id, dialyzer_type_name, description, status) VALUES (6, 'ND15H', '', 'A');
INSERT INTO dialyzer_types (dialyzer_type_id, dialyzer_type_name, description, status) VALUES (7, 'NL-13-GR', '', 'A');
INSERT INTO dialyzer_types (dialyzer_type_id, dialyzer_type_name, description, status) VALUES (8, 'NL11G1', '', 'A');

--
-- Data for Name: location_master; Type: TABLE DATA; 
--

--
-- Data for Name: services_prescribed; Type: TABLE DATA; 
--

--
-- Data for Name: dialysis_session; Type: TABLE DATA; 
--

--
-- Data for Name: dialysis_session_incidents; Type: TABLE DATA; 
--

--
-- Data for Name: dialysis_session_notes; Type: TABLE DATA; 
--

--
-- Data for Name: dialysis_session_parameters; Type: TABLE DATA; 
--

--
-- Data for Name: diet_master; Type: TABLE DATA; 
--

--
-- Data for Name: diet_charges; Type: TABLE DATA; 
--

--
-- Data for Name: diet_chart_documents; Type: TABLE DATA; 
--

--
-- Data for Name: diet_constituents; Type: TABLE DATA; 
--

--
-- Data for Name: diet_prescribed; Type: TABLE DATA; 
--

--
-- Data for Name: dietary_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: dis_detail; Type: TABLE DATA; 
--

--
-- Data for Name: dis_header; Type: TABLE DATA; 
--

--
-- Data for Name: discharge_fileupload; Type: TABLE DATA; 
--

--
-- Data for Name: discharge_format; Type: TABLE DATA; 
--

--
-- Data for Name: discharge_format_detail; Type: TABLE DATA; 
--

--
-- Data for Name: discharge_hvf_print_template; Type: TABLE DATA; 
--

--
-- Data for Name: discharge_medication; Type: TABLE DATA; 
--

--
-- Data for Name: discharge_medication_details; Type: TABLE DATA; 
--

--
-- Data for Name: discharge_medication_sale_details; Type: TABLE DATA; 
--

--
-- Data for Name: discharge_state_names; Type: TABLE DATA; 
--

INSERT INTO discharge_state_names (discharge_state, discharge_state_name) VALUES ('N', 'Not Discharged');
INSERT INTO discharge_state_names (discharge_state, discharge_state_name) VALUES ('I', 'Discharge Initiated');
INSERT INTO discharge_state_names (discharge_state, discharge_state_name) VALUES ('C', 'Clinical Discharge');
INSERT INTO discharge_state_names (discharge_state, discharge_state_name) VALUES ('F', 'Financial Discharge');
INSERT INTO discharge_state_names (discharge_state, discharge_state_name) VALUES ('D', 'Physical Discharge');

--
-- Data for Name: discharge_type_master; Type: TABLE DATA; 
--

INSERT INTO discharge_type_master (discharge_type_id, discharge_type, status) VALUES (1, 'Normal', 'A');
INSERT INTO discharge_type_master (discharge_type_id, discharge_type, status) VALUES (2, 'Absconded', 'A');
INSERT INTO discharge_type_master (discharge_type_id, discharge_type, status) VALUES (3, 'Death', 'A');
INSERT INTO discharge_type_master (discharge_type_id, discharge_type, status) VALUES (4, 'DAMA', 'A');
INSERT INTO discharge_type_master (discharge_type_id, discharge_type, status) VALUES (5, 'Referred To', 'A');
INSERT INTO discharge_type_master (discharge_type_id, discharge_type, status) VALUES (6, 'Admission Cancelled', 'A');

--
-- Data for Name: discount_authorizer; Type: TABLE DATA; 
--

INSERT INTO discount_authorizer (disc_auth_id, disc_auth_name, status, created_timestamp, updated_timestamp, center_id) VALUES (-1, 'Rate Plan Discount', 'A', '2013-10-08 10:09:21.412949', '2013-10-08 10:09:21.479156', '0');

--
-- Data for Name: discount_category_chargeheads; Type: TABLE DATA; 
--

--
-- Data for Name: discount_category_master; Type: TABLE DATA; 
--

--
-- Data for Name: discount_plan_main; Type: TABLE DATA; 
--

--
-- Data for Name: discount_plan_center_master; Type: TABLE DATA; 
--

--
-- Data for Name: discount_plan_details; Type: TABLE DATA; 
--

--
-- Data for Name: doc_hosp_images; Type: TABLE DATA; 
--

--
-- Data for Name: doc_hvf_template_fields; Type: TABLE DATA; 
--

INSERT INTO doc_hvf_template_fields (field_id, template_id, field_name, display_order, num_lines, default_value, print_column, field_status, field_input) VALUES (1, -1, 'Diagnosis', 1, 5, '', 'Y', 'A', NULL);
INSERT INTO doc_hvf_template_fields (field_id, template_id, field_name, display_order, num_lines, default_value, print_column, field_status, field_input) VALUES (2, -1, 'Description', 2, 5, '', 'Y', 'A', NULL);
INSERT INTO doc_hvf_template_fields (field_id, template_id, field_name, display_order, num_lines, default_value, print_column, field_status, field_input) VALUES (3, -1, 'Doctor Notes', 3, 5, '', 'N', 'A', NULL);

--
-- Data for Name: doc_hvf_templates; Type: TABLE DATA; 
--

INSERT INTO doc_hvf_templates (template_id, template_name, title, specialized, status, doc_type, dept_name, access_rights, print_template_name, doc_seq_pattern_id) VALUES (-1, 'Consultation default Template', 'Consultation Template', true, 'A', 'SYS_CONSULT', '', '', NULL, NULL);

--
-- Data for Name: doc_patient_header_templates; Type: TABLE DATA; 
--

--
-- Data for Name: doc_pdf_form_templates; Type: TABLE DATA; 
--

--
-- Data for Name: doc_pdf_template_ext_fields; Type: TABLE DATA; 
--

--
-- Data for Name: doc_print_configuration; Type: TABLE DATA; 
--

INSERT INTO doc_print_configuration (document_type, center_id, printer_settings, page_settings, template_name) VALUES ('discharge', 0, 4, 'Discharge', 'BuiltinDischargeSummary');
INSERT INTO doc_print_configuration (document_type, center_id, printer_settings, page_settings, template_name) VALUES ('prescription_BUILTIN_HTML', 0, 4, 'Discharge', '');
INSERT INTO doc_print_configuration (document_type, center_id, printer_settings, page_settings, template_name) VALUES ('prescription_BUILTIN_TEXT', 0, 4, 'Discharge', '');
INSERT INTO doc_print_configuration (document_type, center_id, printer_settings, page_settings, template_name) VALUES ('prescription_Web Based Prescription Template', 0, 4, 'Discharge', '');

--
-- Data for Name: doc_rich_templates; Type: TABLE DATA; 
--

INSERT INTO doc_rich_templates (template_id, template_name, title, doc_type, specialized, status, template_content, dept_name, pheader_template_id, access_rights, auto_gen_op, auto_gen_ip) VALUES (1, 'service_reports', 'service_reports', 'SYS_ST', true, 'A', '', '', 0, 'U', 'N', 'N');

--
-- Data for Name: doc_rtf_templates; Type: TABLE DATA; 
--

--
-- Data for Name: doc_template_center_master; Type: TABLE DATA; 
--

INSERT INTO doc_template_center_master (doc_template_center_id, doc_template_type, template_id, center_id, status) VALUES (1, 'H', -1, 0, 'A');
INSERT INTO doc_template_center_master (doc_template_center_id, doc_template_type, template_id, center_id, status) VALUES (2, 'R', 1, 0, 'A');

--
-- Data for Name: doc_type; Type: TABLE DATA; 
--

INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('1', 'X-ray', 'N', 'XRY', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('5', 'USG', 'N', 'USG', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('7', 'Untagged', 'N', 'UNT', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_LR', 'Lab Reports', 'Y', 'LR', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_DS', 'Discharge Summary', 'Y', 'DS', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_CI', 'Clinical Information', 'Y', 'CI', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_RR', 'Radiology Reports', 'Y', 'Ra', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_SS', 'Hospital Services', 'Y', 'Ss', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_RX', 'Prescription', 'Y', 'Rx', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_VP', 'Vitals', 'Y', 'Vp', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('4', 'MLC Reports', 'Y', 'MLC', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_ST', 'Service Report', 'Y', 'ST', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_RG', 'Registration Templates', 'Y', 'RG', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_INS', 'Insurance', 'Y', 'INS', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_TPA', 'TPA Preauth Forms', 'Y', 'TP', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_DIE', 'Dietary Chart', 'Y', 'DIE', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_OP', 'OP Case Forms', 'Y', 'OP', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_OT', 'OT Report', 'Y', 'OT', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_DIALYSIS', 'Dialysis Sessions Summary', 'Y', 'DIA', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_MRDCODE', 'MRD CODES REPORT', 'Y', 'MRD', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_CONSULT', 'Consultation Templates', 'Y', 'CON', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_CLINICAL', 'Clinical Information', 'Y', 'CI', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_TRIAGE', 'Triage Summary', 'Y', 'Triage', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_ASSESSMENT', 'Initial Assessment', 'Y', 'ASSESSMENT', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_PROGRESS_NOTES', 'Progress Notes', 'Y', 'PrgNotes', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_CLINICAL_LAB', 'Clinical Lab Results', 'Y', 'CLR', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_IP', 'IP Documents', 'Y', 'IP', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_IVP', 'Vitals and Intake/Output', 'Y', 'Ivp', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_GROWTH_CHART', 'Growth Charts', 'Y', 'Gc', 'A');
INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_GEN_INSTA_FORM', 'Generic Insta Forms', 'Y', 'GENFORM', 'A');

--
-- Data for Name: docs_upload; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_center_master; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_charges_backup; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_charges_op_backup; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_consult_images; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_consultation; Type: TABLE DATA; 
--

--
-- Data for Name: doctors; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_consultation_charge; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_consultation_favourites; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_consultation_tokens; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_images; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_medicine_favourites; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_notes_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_op_consultation_charge; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_operation_favourites; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_order_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_org_details; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_other_favourites; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_other_medicine_favourites; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_prescription; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_service_favourites; Type: TABLE DATA; 
--

--
-- Data for Name: doctor_speciality_master; Type: TABLE DATA; 
--

INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('ORTHD', 'Orthodontics', 'Orthodontics', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('PERD', 'Periodontics', 'Periodontics', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('PROSD', 'Prosthodontics', 'Prosthodontics', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('PEDOD', 'Pediatric Dentistry', 'Pediatric Dentistry', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('ENDOD', 'Endodontics', 'Endodontics', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('OBS', 'Obstetrician', 'Obstetrician', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('GYN', 'Gynecologist', 'Gynecologist', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('ORTH', 'Orthopaedics', 'Orthopaedics', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('PAED', 'Pediatrics', 'Pediatrics', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('CARD', 'Cardiology', 'Cardiology', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('NEPH', 'Nephrology', 'Nephrology', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('GAEN', 'Gastroentrology', 'Gastroentrology', 'A');
INSERT INTO doctor_speciality_master (speciality_id, speciality_desc, display_name, status) VALUES ('ANAE', 'Anesthesiolog', 'Anesthesiology', 'A');

--
-- Data for Name: doctor_test_favourites; Type: TABLE DATA; 
--

--
-- Data for Name: patient_details; Type: TABLE DATA; 
--

--
-- Data for Name: donor_registration; Type: TABLE DATA; 
--

--
-- Data for Name: donor_collection_det; Type: TABLE DATA; 
--

--
-- Data for Name: donor_blood_component; Type: TABLE DATA; 
--

--
-- Data for Name: donor_blood_discard; Type: TABLE DATA; 
--

--
-- Data for Name: donor_blood_grouping; Type: TABLE DATA; 
--

--
-- Data for Name: donor_blood_issue; Type: TABLE DATA; 
--

--
-- Data for Name: rhtype_master; Type: TABLE DATA; 
--

INSERT INTO rhtype_master (rhtype_id, rhtype_name, status) VALUES ('1', 'Negative', 'A');
INSERT INTO rhtype_master (rhtype_id, rhtype_name, status) VALUES ('2', 'Positive', 'A');

--
-- Data for Name: donor_blood_request; Type: TABLE DATA; 
--

--
-- Data for Name: donor_blood_test_results; Type: TABLE DATA; 
--

--
-- Data for Name: drg_code_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: drg_codes_master; Type: TABLE DATA; 
--

--
-- Data for Name: drugs_administered; Type: TABLE DATA; 
--

--
-- Data for Name: duration_units; Type: TABLE DATA; 
--

INSERT INTO duration_units (unit, unit_name, status) VALUES ('I', 'minutes', NULL);
INSERT INTO duration_units (unit, unit_name, status) VALUES ('H', 'hours', NULL);
INSERT INTO duration_units (unit, unit_name, status) VALUES ('D', 'days', NULL);
INSERT INTO duration_units (unit, unit_name, status) VALUES ('W', 'weeks', NULL);
INSERT INTO duration_units (unit, unit_name, status) VALUES ('M', 'months', NULL);

--
-- Data for Name: dyna_package_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: dyna_package_category; Type: TABLE DATA; 
--

--
-- Data for Name: dyna_packages; Type: TABLE DATA; 
--

--
-- Data for Name: dyna_package_category_limits; Type: TABLE DATA; 
--

--
-- Data for Name: dyna_package_category_limits_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: dyna_package_category_limits_backup; Type: TABLE DATA; 
--

--
-- Data for Name: dyna_package_charges; Type: TABLE DATA; 
--

--
-- Data for Name: dyna_package_charges_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: dyna_package_charges_backup; Type: TABLE DATA; 
--

--
-- Data for Name: dyna_package_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: dyna_package_master_charges_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: organization_details; Type: TABLE DATA; 
--

INSERT INTO organization_details (org_id, org_name, org_abbreviation, org_address, org_city, org_state, org_country, org_pincode, org_phone, org_phone2, org_fax, org_mailid, org_website, org_contact_person, status, cons_code, revisit_code, private_cons_code, private_cons_revisit_code, modd_cons_code, modd_cons_revisit_code, spl_cons_code, spl_cons_revisit_code, pharmacy_discount_percentage, has_date_validity, valid_from_date, valid_to_date, rate_variation, pharmacy_discount_type, store_rate_plan_id, eligible_to_earn_points, created_timestamp, updated_timestamp, is_rate_sheet) VALUES ('ORG0001', 'GENERAL', 'GENERAL', 'BANGALORE', 'CT0002', 'ST0001', 'CM0001', 560001, '080-45124512', '', '08027845166', 'info@gneral.com', 'www.general.com', 'SIVA', 'A', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, NULL, NULL, NULL, 'E', NULL, 'N', '2013-10-08 10:09:21.59636', '2013-10-08 10:09:21.704997', 'Y');

--
-- Data for Name: dyna_package_org_details; Type: TABLE DATA; 
--

--
-- Data for Name: dyna_package_rules; Type: TABLE DATA; 
--

--
-- Data for Name: email_category_master; Type: TABLE DATA; 
--

INSERT INTO email_category_master (category_name, category_id) VALUES ('Clinical', 'C');
INSERT INTO email_category_master (category_name, category_id) VALUES ('Scheduler', 'S');
INSERT INTO email_category_master (category_name, category_id) VALUES ('Schedulable Reports', 'R');

--
-- Data for Name: email_template; Type: TABLE DATA; 
--

INSERT INTO email_template (email_template_id, template_name, from_address, subject, mail_message, email_category, to_address) VALUES ('ET0001', 'Patient Registration', 'EnterMailIDHere@domain.com', 'Welcome to ${hospital} Patient Portal', '<p>Hello <strong>${user}</strong>,</p>
<p>&nbsp;</p>
<p>Welcome to <strong>${hospital}</strong> Patient Portal !!!</p>
<p>&nbsp;</p>
<p>We have setup a account for you in our HMS patient portal. You account details are as follows:</p>
<p>Username: <strong>${mrno}</strong></p>
<p>Password: <strong>${password}</strong></p>
<p>&nbsp;</p>
<p>You can access the portal from the following url:</p>
<p><span style="text-decoration: underline;">${portalurl}</span></p>
<p>&nbsp;</p>
<p>Please change your password when you login in the first time.</p>
<p>&nbsp;</p>
<p>You should keep a copy of&nbsp; the mail for your future reference.</p>
<p>&nbsp;</p>
<p>Regards,</p>
<p>${hospital} Administrator</p>
<p>&nbsp;</p>
<p>--------------------------------------------</p>
<p>Automated mail sent via Insta HMS</p>', 'C', NULL);
INSERT INTO email_template (email_template_id, template_name, from_address, subject, mail_message, email_category, to_address) VALUES ('ET0004', 'Patient Template', 'welcomtopatient@doman.com', '${hospital}: Documents awaiting your input', '<p>&nbsp;</p>
<p>Hello <strong>${user},</strong></p>
<p>&nbsp;</p>
<p>The following documents are awaiting&nbsp; your input:</p>
<p><strong>${documents}</strong></p>
<p>&nbsp;</p>
<p>You can access the documents&nbsp; online via our <strong>${hospital}</strong> Patient Portal</p>
<p><strong>${portalurl}</strong></p>
<p>&nbsp;</p>
<p>Regards,</p>
<p><strong>${hospital}</strong> Administrator.</p>', 'C', NULL);
INSERT INTO email_template (email_template_id, template_name, from_address, subject, mail_message, email_category, to_address) VALUES ('ET0002', 'Patient to Doctor Template', 'welcomtodoctor@doman.com', '${patient}''s: Document awaiting your review', '<p>Hello&nbsp; <strong>${user}</strong></p>
<p>&nbsp;</p>
<p>&nbsp; Patient <strong>${patient}</strong> has updated <strong>${document}</strong> and is awaiting&nbsp; your&nbsp; review.</p>
<p>&nbsp;</p>
<p>&nbsp; You can access&nbsp; the document online via our&nbsp;<strong> ${portalurl}</strong> Doctor Portal</p>
<p><strong> </strong></p>
<p>&nbsp;</p>
<p>&nbsp;&nbsp; Regards,</p>
<p>&nbsp;&nbsp; <strong>${patient}</strong>.</p>
<p>&nbsp;&nbsp; <strong>${mobileNo}</strong>.</p>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>', 'C', NULL);
INSERT INTO email_template (email_template_id, template_name, from_address, subject, mail_message, email_category, to_address) VALUES ('ET0003', 'Doctor Template', 'welcomtodoctor@doman.com', ' ${hospital}: Documents awaiting your review', '<p>&nbsp;</p>
<p>Hello Dr <strong>${user}</strong>,</p>
<p>&nbsp;</p>
<p>The following documents are&nbsp; awaiting your review:</p>
<p><strong>${documents}</strong></p>
<p>&nbsp;</p>
<p>You can access the documents online via our<strong> ${hospital} </strong>Doctor portal</p>
<p><strong>${portalurl}</strong></p>
<p>&nbsp;</p>
<p>Regards,</p>
<p><strong>${hospital}</strong> Administrator</p>
<p>&nbsp;</p>
<p>&nbsp;</p>', 'C', NULL);
INSERT INTO email_template (email_template_id, template_name, from_address, subject, mail_message, email_category, to_address) VALUES ('ET0005', 'Doctor to Patient', 'welcomeadmin@insta.com', 'Document ${document} awaiting your review', '<p>hello <strong>${patient}</strong></p>
<p>&nbsp;</p>
<p>Doctor <strong>${doctor}</strong> has updated the document <strong>${document}</strong> and is awaiting your review.</p>
<p>&nbsp;</p>
<p>Regards.</p>
<p><strong>${doctor}</strong></p>', 'C', NULL);
INSERT INTO email_template (email_template_id, template_name, from_address, subject, mail_message, email_category, to_address) VALUES ('ET0007', 'Scheduled Email Reports Template', 'welcomeadmin@insta.com', '${hospital}: ${reportperiod} ${reportname} Report', '<p><span style="text-decoration: underline;">
    <strong>${hospital}</strong> Reports.</span></p>
	<p>&nbsp;</p>
	<p>Please find attached</p>
	<p><strong><br /></strong></p>
	<p><strong>${hospital}&nbsp; </strong><strong>&nbsp;</strong></p>
	<p><strong>${reportperiod} </strong><strong>${reportdate} </strong><strong>&nbsp;</strong></p>
	<p>Report:<strong> ${reportname} </strong></p>
	<p>&nbsp;</p>
	<p>&nbsp;</p>
	<p>Regards,</p>
	<p>Administrator</p>
	<p>&nbsp;</p>
	<p>&nbsp;</p>
	<p>-------------------------------------------------------------------</p>
	<p>Automated mail sent via Insta HMS</p>
	<p>&nbsp;</p>
	<p><strong><br /></strong></p>
	<p><strong>&nbsp;</strong><strong> </strong><strong><br /></strong></p>
	<p><strong><br /></strong></p>
	<p>&nbsp;</p>
	<p>&nbsp;</p>
	<p>&nbsp;</p>
	<p>&nbsp;</p>
	<p>&nbsp;</p>', 'R', NULL);
INSERT INTO email_template (email_template_id, template_name, from_address, subject, mail_message, email_category, to_address) VALUES ('ET0006', 'Appointment Booked Template', 'welcomeadmin@insta.com', 'Doctor Appointment', '<p>Booked appointment with</p>
<p><strong>${doctor} </strong></p>
<p>on &nbsp; <strong> ${date} </strong> at <strong> ${time} </strong> .</p>', 'S', NULL);

--
-- Data for Name: emailable_reports_config; Type: TABLE DATA; 
--

--
-- Data for Name: emergency_call_log; Type: TABLE DATA; 
--

--
-- Data for Name: emr_access_rule; Type: TABLE DATA; 
--

--
-- Data for Name: emr_access_rule_details; Type: TABLE DATA; 
--

--
-- Data for Name: encounter_end_types; Type: TABLE DATA; 
--

INSERT INTO encounter_end_types (code_type, code, code_desc, status) VALUES ('Encounter End', '1', 'Discharged with approval', 'A');
INSERT INTO encounter_end_types (code_type, code, code_desc, status) VALUES ('Encounter End', '2', 'Discharged against advice', 'A');
INSERT INTO encounter_end_types (code_type, code, code_desc, status) VALUES ('Encounter End', '3', 'Discharged absent without leave', 'A');
INSERT INTO encounter_end_types (code_type, code, code_desc, status) VALUES ('Encounter End', '4', 'Transfer to another facility', 'A');
INSERT INTO encounter_end_types (code_type, code, code_desc, status) VALUES ('Encounter End', '5', 'Deceased', 'A');

--
-- Data for Name: encounter_start_types; Type: TABLE DATA; 
--

INSERT INTO encounter_start_types (code_type, code, code_desc, status) VALUES ('Encounter Start', '1', 'Elective', 'A');
INSERT INTO encounter_start_types (code_type, code, code_desc, status) VALUES ('Encounter Start', '2', 'Emergency', 'A');
INSERT INTO encounter_start_types (code_type, code, code_desc, status) VALUES ('Encounter Start', '3', 'Transfer', 'A');
INSERT INTO encounter_start_types (code_type, code, code_desc, status) VALUES ('Encounter Start', '4', 'Live birth', 'A');
INSERT INTO encounter_start_types (code_type, code, code_desc, status) VALUES ('Encounter Start', '5', 'Still birth', 'A');
INSERT INTO encounter_start_types (code_type, code, code_desc, status) VALUES ('Encounter Start', '6', 'Dead On Arrival', 'A');

--
-- Data for Name: encounter_type_codes; Type: TABLE DATA; 
--

INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (4, 'Inpatient Bed + Emergency room', 'N', 'Y', 'N', 'A', 'N', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (2, 'No Bed + Emergency room', 'N', 'Y', 'Y', 'A', 'N', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (6, 'Daycase Bed + Emergency room', 'N', 'Y', 'Y', 'A', 'N', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (7, 'Nationals Screening', 'N', 'Y', 'Y', 'A', 'N', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (8, 'New Visa Screening', 'N', 'Y', 'Y', 'A', 'N', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (9, 'Renewal Visa Screening', 'N', 'Y', 'Y', 'A', 'N', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (12, 'Home', 'N', 'Y', 'Y', 'A', 'N', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (13, 'Assisted Living Facility', 'N', 'Y', 'Y', 'A', 'N', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (15, 'Mobile Unit', 'N', 'Y', 'Y', 'A', 'N', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (41, 'Ambulance - Land', 'N', 'Y', 'Y', 'A', 'N', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (42, 'Ambulance - Air or Water', 'N', 'Y', 'Y', 'A', 'N', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (1, 'No Bed + No emergency room', 'Y', 'N', 'N', 'A', 'Y', 'N', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (3, 'Inpatient Bed + No emergency room', 'N', 'Y', 'N', 'A', 'N', 'Y', 'N', 'Encounter Type');
INSERT INTO encounter_type_codes (encounter_type_id, encounter_type_desc, op_applicable, ip_applicable, daycare_applicable, status, op_encounter_default, ip_encounter_default, daycare_encounter_default, code_type) VALUES (5, 'Daycase Bed + No emergency room', 'N', 'Y', 'Y', 'A', 'N', 'N', 'Y', 'Encounter Type');

--
-- Data for Name: equip_org_details; Type: TABLE DATA; 
--

--
-- Data for Name: equipment_master; Type: TABLE DATA; 
--

--
-- Data for Name: equipement_charges; Type: TABLE DATA; 
--

--
-- Data for Name: equipment_charges_backup; Type: TABLE DATA; 
--

--
-- Data for Name: equipment_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: equipment_prescribed; Type: TABLE DATA; 
--

--
-- Data for Name: equipment_test_conducted; Type: TABLE DATA; 
--

--
-- Data for Name: equipment_test_result; Type: TABLE DATA; 
--

--
-- Data for Name: equipment_test_values; Type: TABLE DATA; 
--

--
-- Data for Name: erx_response; Type: TABLE DATA; 
--

--
-- Data for Name: estimate_bill; Type: TABLE DATA; 
--

--
-- Data for Name: estimate_charge; Type: TABLE DATA; 
--

--
-- Data for Name: estimate_header; Type: TABLE DATA; 
--

--
-- Data for Name: event_log; Type: TABLE DATA; 
--

--
-- Data for Name: eye_test_attrib_master; Type: TABLE DATA; 
--

INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (1, 'Near_LE', 'A', 'Near', 'N36,N18,N12,N9,N6', 'dropdown');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (2, 'Near_RE', 'A', 'Near', 'N36,N18,N12,N9,N6', 'dropdown');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (3, 'Distance_LE', 'A', 'Distance', '6/60,6/36,6/24,6/12,6/18,6/9,6/6', 'dropdown');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (4, 'Distance_RE', 'A', 'Distance', '6/60,6/36,6/24,6/12,6/18,6/9,6/6', 'dropdown');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (6, 'PH_RE', 'A', 'PH', NULL, 'input');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (5, 'PH_LE', 'A', 'PH', NULL, 'input');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (9, 'LE', 'A', ' ', NULL, 'input');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (10, 'RE', 'A', ' ', NULL, 'input');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (13, 'Axis_LE', 'A', 'Axis', NULL, 'input');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (14, 'Axis_RE', 'A', 'Axis', NULL, 'input');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (15, 'NCT_LE', 'A', 'NCT', NULL, 'input');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (16, 'NCT_RE', 'A', 'NCT', NULL, 'input');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (17, 'AT_LE', 'A', 'AT', NULL, 'input');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (18, 'AT_RE', 'A', 'AT', NULL, 'input');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (7, 'Spl_RE', 'A', 'Spl', '0.25,0.50,0.75,1.00,1.25,1.50,1.75,2.00,2.25,2.50,2.75,3.00,3.25,3.50,3.75,4.00,4.25,4.50,4.75,5.00,5.25,5.50,5.75,6.00,6.25,6.50,6.75,7.00,7.25,7.50,7.75,8.00,8.25,8.50,8.75,9.00,9.25,9.50,9.75,10.00', 'valuepicker');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (8, 'Spl_LE', 'A', 'Spl', '0.25,0.50,0.75,1.00,1.25,1.50,1.75,2.00,2.25,2.50,2.75,3.00,3.25,3.50,3.75,4.00,4.25,4.50,4.75,5.00,5.25,5.50,5.75,6.00,6.25,6.50,6.75,7.00,7.25,7.50,7.75,8.00,8.25,8.50,8.75,9.00,9.25,9.50,9.75,10.00', 'valuepicker');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (11, 'Cyl_LE', 'A', 'Cyl', '0.25,0.50,0.75,1.00,1.25,1.50,1.75,2.00,2.25,2.50,2.75,3.00,3.25,3.50,3.75,4.00,4.25,4.50,4.75,5.00,5.25,5.50,5.75,6.00,6.25,6.50,6.75,7.00,7.25,7.50,7.75,8.00,8.25,8.50,8.75,9.00,9.25,9.50,9.75,10.00', 'valuepicker');
INSERT INTO eye_test_attrib_master (attribute_id, attribute_name, attribute_status, display_name, default_values, field_type) VALUES (12, 'Cyl_RE', 'A', 'Cyl', '0.25,0.50,0.75,1.00,1.25,1.50,1.75,2.00,2.25,2.50,2.75,3.00,3.25,3.50,3.75,4.00,4.25,4.50,4.75,5.00,5.25,5.50,5.75,6.00,6.25,6.50,6.75,7.00,7.25,7.50,7.75,8.00,8.25,8.50,8.75,9.00,9.25,9.50,9.75,10.00', 'valuepicker');

--
-- Data for Name: eye_test_master; Type: TABLE DATA; 
--

INSERT INTO eye_test_master (test_id, test_name, test_status) VALUES (1, 'Unaided Vision', 'A');
INSERT INTO eye_test_master (test_id, test_name, test_status) VALUES (3, 'Vision with PH', 'A');
INSERT INTO eye_test_master (test_id, test_name, test_status) VALUES (4, 'Auto Refraction', 'A');
INSERT INTO eye_test_master (test_id, test_name, test_status) VALUES (5, 'Retinascopy', 'A');
INSERT INTO eye_test_master (test_id, test_name, test_status) VALUES (6, 'IOP', 'A');
INSERT INTO eye_test_master (test_id, test_name, test_status) VALUES (7, 'Subjective Acceptance', 'A');
INSERT INTO eye_test_master (test_id, test_name, test_status) VALUES (8, 'Previous Glass Prescription', 'A');
INSERT INTO eye_test_master (test_id, test_name, test_status) VALUES (2, 'Vision with PG', 'A');

--
-- Data for Name: favourite_report_rights; Type: TABLE DATA; 
--

--
-- Data for Name: favourite_reports; Type: TABLE DATA; 
--

--
-- Data for Name: fields; Type: TABLE DATA; 
--

INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld1', 'frm1', 'Procedure', 1, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld2', 'frm1', 'Findings', 2, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld3', 'frm1', 'Investigations', 3, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld4', 'frm1', 'Recommendation', 4, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld5', 'frm2', 'Date of Surgery', 1, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld6', 'frm2', 'Diagnosis', 2, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld7', 'frm2', 'Surgery / Procedure', 3, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld8', 'frm2', 'Consultant Physician', 4, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld9', 'frm2', 'Consultant Surgeon', 5, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld10', 'frm2', 'Consultant Anaesthesist', 6, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld11', 'frm2', 'Surgical notes / Findings', 7, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld12', 'frm2', 'Post operation notes', 8, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld13', 'frm2', 'Medication', 9, 3, '');
INSERT INTO fields (field_id, form_id, caption, displayorder, no_of_lines, default_text) VALUES ('fld14', 'frm2', 'Recommendation', 10, 3, '');

--
-- Data for Name: fixed_asset_master; Type: TABLE DATA; 
--

--
-- Data for Name: fixed_asset_master_bkp; Type: TABLE DATA; 
--

--
-- Data for Name: fixed_asset_uploads; Type: TABLE DATA; 
--

--
-- Data for Name: follow_up_details; Type: TABLE DATA; 
--

--
-- Data for Name: foreign_currency; Type: TABLE DATA; 
--

--
-- Data for Name: form_components; Type: TABLE DATA; 
--

INSERT INTO form_components (dept_id, sections, id, group_patient_sections, form_type, operation_id, service_id, form_name, immunization, print_template_id, status, doc_type, doctor_id) VALUES ('-1', '6', 3, 'Y', 'Form_OT', '-1', NULL, 'Surgery', 'N', NULL, 'A', NULL, NULL);
INSERT INTO form_components (dept_id, sections, id, group_patient_sections, form_type, operation_id, service_id, form_name, immunization, print_template_id, status, doc_type, doctor_id) VALUES ('-1', '7', 4, 'Y', 'Form_Serv', '', '-1', 'Service', 'N', NULL, 'A', NULL, NULL);
INSERT INTO form_components (dept_id, sections, id, group_patient_sections, form_type, operation_id, service_id, form_name, immunization, print_template_id, status, doc_type, doctor_id) VALUES ('-1', '-2,-4,1,2,3,4,5', 5, 'N', 'Form_TRI', NULL, NULL, 'Triage', 'Y', NULL, 'A', NULL, NULL);
INSERT INTO form_components (dept_id, sections, id, group_patient_sections, form_type, operation_id, service_id, form_name, immunization, print_template_id, status, doc_type, doctor_id) VALUES ('-1', '-4', 6, 'N', 'Form_IA', NULL, NULL, 'Assessment', 'N', NULL, 'A', NULL, NULL);
INSERT INTO form_components (dept_id, sections, id, group_patient_sections, form_type, operation_id, service_id, form_name, immunization, print_template_id, status, doc_type, doctor_id) VALUES ('-1', '-1,-6', 1, 'N', 'Form_IP', '-1', NULL, 'IP', 'N', NULL, 'A', NULL, NULL);
INSERT INTO form_components (dept_id, sections, id, group_patient_sections, form_type, operation_id, service_id, form_name, immunization, print_template_id, status, doc_type, doctor_id) VALUES ('-1', '-2', 7, 'N', 'Form_Gen', NULL, NULL, 'Generic Form', 'N', NULL, 'A', 'SYS_GEN_INSTA_FORM', NULL);
INSERT INTO form_components (dept_id, sections, id, group_patient_sections, form_type, operation_id, service_id, form_name, immunization, print_template_id, status, doc_type, doctor_id) VALUES ('-1', '-1,-4,-5,1,2,3,4,-6,-7', 2, 'N', 'Form_CONS', '-1', NULL, 'Consultation', 'N', NULL, 'A', NULL, '-1');
INSERT INTO form_components (dept_id, sections, id, group_patient_sections, form_type, operation_id, service_id, form_name, immunization, print_template_id, status, doc_type, doctor_id) VALUES ('-1', '-1,-2,-4,-6,-7', 8, 'N', 'Form_OP_FOLLOW_UP_CONS', NULL, NULL, 'OP Follow Up Consultation', 'N', NULL, 'A', NULL, '-1');

--
-- Data for Name: form_header; Type: TABLE DATA; 
--

INSERT INTO form_header (form_id, form_caption, patient_detail, visit_detail, status, form_title, form_type, access_rights) VALUES ('frm1', 'DISCHARGE SUMMARY', 'Y', 'Y', 'A', 'Discharge Summary', 'Discharge Summary', 'U');
INSERT INTO form_header (form_id, form_caption, patient_detail, visit_detail, status, form_title, form_type, access_rights) VALUES ('frm2', 'OPERATION ROOM DISCHARGE SUMMARY', 'Y', 'Y', 'A', 'Discharge Summary', 'Discharge Summary', 'U');

--
-- Data for Name: general_reagent_usage_details; Type: TABLE DATA; 
--

--
-- Data for Name: general_reagent_usage_main; Type: TABLE DATA; 
--

--
-- Data for Name: generic_classification_master; Type: TABLE DATA; 
--

--
-- Data for Name: generic_name; Type: TABLE DATA; 
--

--
-- Data for Name: generic_preferences; Type: TABLE DATA; 
--

INSERT INTO generic_preferences (protocol, host_name, port_no, auth_required, username, password, hospital_mail_id, stock_negative_sale, sampleflow_required, autogenerate_sampleid, bill_later_print_default, hospital_name, sale_expiry, warn_expiry, pharma_allow_cp_sale, pharma_auto_roundoff, domain_name, obsolete_vat_type, autogenerate_labno, pharma_return_restricted, user_name_in_bill_print, sms_username, luxary_tax_applicable_on, cfd_max_count, pharmacy_schedule_h_alert, pharmacy_patient_type, enable_sms, seperate_pharmacy_credit_bill, deposit_cheque_realization_flow, bill_now_print_default, receipt_refund_print_default, doctors_custom_field1, doctors_custom_field2, doctors_custom_field3, doctors_custom_field4, doctors_custom_field5, sample_collection_print_type, validate_cost_price, pharmacy_sale_margin_in_per, claim_service_tax, deposit_avalibility, obsolete_pharmacy_defalut_vat_rate, default_prescribe_doctor, se_with_po, receive_transfer_indent, hospital_tin, hospital_pan, hospital_service_regn_no, prescribing_doctor_required, show_hosp_amts_pharmasales, qty_default_to_issue_unit, allow_decimals_for_qty, currency_symbol, whole, "decimal", daily_checkpoint1, daily_checkpoint2, force_remarks_po_item_reject, returns_against_specific_supplier, auto_close_indented_casefiles, obsolete_default_diagnosis_code_type, fixed_ot_charges, allow_only_indent_based_issue, sales_returns_print_type, force_sub_group_selection, operation_apllicable_for, go_live_date, menu_background_color, show_central_excise_duty, default_bill_later_creditsales, hospital_address, after_decimal_digits, default_printer_for_bill_later, default_printer_for_bill_now, vat_applicable, cess_applicable, barcode_for_item, default_prescription_print_template, calendar_start_day, upload_limit_in_mb, sales_print_items, stock_inc, default_consultation_print_template, po_approval_reqd_more_than_amt, validate_diagnosis_codification, allow_bill_now_insurance, return_validity, obsolete_prescriptions_by_generics, prescription_uses_stores, package_uom, issue_uom, package_size, hosp_uses_dynamic_addresses, default_po_print_template, max_centers_inc_default, show_tests_in_emr, obsolete_rate_plan_for_non_insured_bill, max_active_hosp_users, service_name_required, op_consultation_edit_across_doctors, consultation_reopen_time_limit, independent_generation_of_sample_id, allow_decimals_in_qty_for_issues, sample_no_generation, normal_color_code, abnormal_color_code, critical_color_code, improbable_color_code, surgery_name_required, use_smart_card, auto_close_nocharge_op_bills, auto_close_visit, gen_token_for_lab, gen_token_for_rad, default_emr_print_template, signature_device_ip, billing_bed_type_for_op, tooth_numbering_system, deposit_receipt_print_default, auto_close_claims_with_difference, ip_cases_across_doctors, points_earning_points, points_earning_amt, points_redemption_rate, default_dental_cons_print_template, allow_zero_claim_amount_for_op, allow_zero_claim_amount_for_ip, slida_mailslot_path, allow_consumable_stock_negative, obsolete_eclaim_xml_schema, allow_cons_qty_incr, fp_authentication_url, allow_all_cons_types_in_reg, enable_nestloop, bill_service_charge_percent, po_to_be_validated, shafafiya_user_id_obsolete, shafafiya_password_obsolete, shafafiya_pbm_active_obsolete, default_voucher_print, dhpo_facility_user_id_obsolete, dhpo_facility_password_obsolete, shafafiya_preauth_user_id_obsolete, shafafiya_preauth_password_obsolete, shafafiya_preauth_active_obsolete, dental_chart, sample_assertion, pac_validity_days, indent_approval_by, issue_to_dept_only, allow_cross_center_indents, fin_year_start_month, fin_year_end_month, bill_cancellation_requires_approval, diag_images, currency_format, consultation_validity_units, blood_exp, corporate_insurance, op_one_presc_doc, max_dial_machine_count, max_collection_centers_count, apply_rateplan_discount, enable_force_selection_for_mrno_search, emr_url_date, procurement_tax_label, restrict_inactive_visits, aggregate_amt_on_remittance, scheduler_generate_order, hijricalendar, procurement_expiry_days, expired_items_procurement, stock_entry_agnst_do, check_insu_card_exp_in_sales, auto_mail_po_to_sup, diag_report_print_center, google_analytics_key, is_return_against_grnno, apply_supplier_tax_rules, no_of_credit_debit_card_digits, pbm_price_threshold, mobile_number_validation, mobile_starting_pattern, mobile_length_pattern, allow_bill_reopen, separator_type, email_bill_printer, email_bill_now_template, email_bill_later_template, nurse_staff_ward_assignments_applicable, default_grn_print_template, mod_username, default_prescription_web_template, default_prescription_web_printer, appointment_name_order, registration_on_arrival, patient_name_match_distance) VALUES ('smtp', '', 25, false, '', '', 'customer-support@instahealthsolutions.com', 'D', 'Y', 'N', 'BILL-SUM-ALL', '', 'N', 30, 'N', 'Y', 'mrpb', 'MB', 'N', 'N', 'Y', '.instahms@smscountry.net', 'B', 4, 'Y', 'H', 'N', 'N', 'N', 'BILL-SUM-ALL', NULL, NULL, NULL, NULL, NULL, NULL, 'SL', 'N', 0.00, 10.30, 'B', 4, 'N', 'N', 'N', NULL, NULL, NULL, 'N', 'N', 'N', 'N', 'Rs', 'Rupees', 'Paise', '08:00:00', '20:00:00', 'N', 'N', 'N', NULL, 'N', 'N', 'R', 'N', 'i', '2011-04-26', NULL, 'N', 'N', NULL, 2, NULL, NULL, 'Y', 'Y', 'N', 'BUILTIN_HTML', 0, 10, 'BILLONLY', 'M', 'BUILTIN_HTML', 0, 'N', 'N', NULL, 'N', 'Y', 'Numbers', 'Numbers', 1, 'N', 'BUILTIN_HTML', 1, 'S', NULL, 9999, 'O', 'Y', 10000, 'N', 'N', 'P', '#FFFFFF', '#FFF3B6', '#FFB6B9', '#E18EF3', 'M', 'N', 'A', 'P', 'N', 'N', 'BUILTIN_HTML', NULL, 'GENERAL', 'U', NULL, 0.00, 'Y', 1, 0.00, 0.00, NULL, 'Y', 'Y', NULL, 'Y', 'HAAD', 'Y', NULL, 'N', 'Y', 0.00, 'N', NULL, NULL, 'N', 'summary', NULL, NULL, NULL, NULL, 'N', 'Y', 'N', 30, 'I', 'N', 'Y', 4, 3, 'N', 'T,P,G', NULL, 'T', 90, 'N', 'N', NULL, NULL, 'N', 'N', 'D', 'V', 'N', 'N', 'Y', 'N', NULL, 'N', 'N', 'N', false, 'col', NULL, false, false, NULL, 0.00, 'Y', '7,8,9', '10', 'Y', NULL, NULL, 'CUSTOM-Web Based Bill Now Print Template', 'CUSTOM-Web Based Bill Later Print Template', 'N', 'BUILTIN_HTML', NULL, 'Web Based Prescription Template', 4, 'FML', 'Y', 0);

--
-- Data for Name: generic_resource_type; Type: TABLE DATA; 
--

--
-- Data for Name: generic_resource_master; Type: TABLE DATA; 
--

--
-- Data for Name: generic_sub_classification_master; Type: TABLE DATA; 
--

--
-- Data for Name: govt_identifier_master; Type: TABLE DATA; 
--

INSERT INTO govt_identifier_master (identifier_id, identifier_type, remarks, status, unique_id, default_option, govt_id_pattern, value_mandatory) VALUES (1, '000-0000-0000000-0', 'National without card', 'A', 'N', 'N', NULL, 'N');
INSERT INTO govt_identifier_master (identifier_id, identifier_type, remarks, status, unique_id, default_option, govt_id_pattern, value_mandatory) VALUES (2, '111-1111-1111111-1', 'Expatriate resident without a card', 'A', 'N', 'N', NULL, 'N');
INSERT INTO govt_identifier_master (identifier_id, identifier_type, remarks, status, unique_id, default_option, govt_id_pattern, value_mandatory) VALUES (3, '222-2222-2222222-2', 'Non national, non-expat resident without a card', 'A', 'N', 'N', NULL, 'N');
INSERT INTO govt_identifier_master (identifier_id, identifier_type, remarks, status, unique_id, default_option, govt_id_pattern, value_mandatory) VALUES (4, '999-9999-9999999-9', 'Unknown status, without a card.', 'A', 'N', 'N', NULL, 'N');

--
-- Data for Name: grn_print_template; Type: TABLE DATA; 
--

--
-- Data for Name: growth_chart_reference_data; Type: TABLE DATA; 
--

--
-- Data for Name: ha_ins_company_code; Type: TABLE DATA; 
--

--
-- Data for Name: ha_item_code_type; Type: TABLE DATA; 
--

--
-- Data for Name: ha_tpa_code; Type: TABLE DATA; 
--

INSERT INTO ha_tpa_code (ha_tpa_code_id, tpa_id, tpa_code, health_authority) VALUES (1, 'TPAID0001', NULL, 'HAAD');
INSERT INTO ha_tpa_code (ha_tpa_code_id, tpa_id, tpa_code, health_authority) VALUES (2, 'TPAID0002', NULL, 'HAAD');
INSERT INTO ha_tpa_code (ha_tpa_code_id, tpa_id, tpa_code, health_authority) VALUES (3, 'TPAID0005', NULL, 'HAAD');
INSERT INTO ha_tpa_code (ha_tpa_code_id, tpa_id, tpa_code, health_authority) VALUES (4, 'TPAID0003', NULL, 'HAAD');
INSERT INTO ha_tpa_code (ha_tpa_code_id, tpa_id, tpa_code, health_authority) VALUES (5, 'TPAID0006', NULL, 'HAAD');
INSERT INTO ha_tpa_code (ha_tpa_code_id, tpa_id, tpa_code, health_authority) VALUES (6, 'TPAID0004', NULL, 'HAAD');

--
-- Data for Name: health_authority_master; Type: TABLE DATA; 
--

INSERT INTO health_authority_master (health_authority_id, health_authority) VALUES (1, 'HAAD');

--
-- Data for Name: health_authority_preferences; Type: TABLE DATA; 
--

INSERT INTO health_authority_preferences (health_authority, diagnosis_code_type, prescriptions_by_generics, consultation_code_types, drug_code_type, default_gp_first_consultation, default_gp_revisit_consultation, default_sp_first_consultation, default_sp_revisit_consultation, child_mother_ins_member_validity_days, presc_doctor_as_ordering_clinician, base_rate_plan) VALUES ('HAAD', NULL, 'N', 'E&M', 'Drug HAAD', NULL, NULL, NULL, NULL, 30, 'N', NULL);
INSERT INTO health_authority_preferences (health_authority, diagnosis_code_type, prescriptions_by_generics, consultation_code_types, drug_code_type, default_gp_first_consultation, default_gp_revisit_consultation, default_sp_first_consultation, default_sp_revisit_consultation, child_mother_ins_member_validity_days, presc_doctor_as_ordering_clinician, base_rate_plan) VALUES ('DHA', NULL, 'N', 'E&M', 'Drug DHA', NULL, NULL, NULL, NULL, 30, 'N', NULL);
INSERT INTO health_authority_preferences (health_authority, diagnosis_code_type, prescriptions_by_generics, consultation_code_types, drug_code_type, default_gp_first_consultation, default_gp_revisit_consultation, default_sp_first_consultation, default_sp_revisit_consultation, child_mother_ins_member_validity_days, presc_doctor_as_ordering_clinician, base_rate_plan) VALUES ('Default', NULL, 'N', NULL, 'Drug', NULL, NULL, NULL, NULL, 30, 'N', NULL);

--
-- Data for Name: histo_impression_master; Type: TABLE DATA; 
--

--
-- Data for Name: hl7_center_interfaces; Type: TABLE DATA; 
--

--
-- Data for Name: hl7_export_items; Type: TABLE DATA; 
--

--
-- Data for Name: hl7_export_patient; Type: TABLE DATA; 
--

--
-- Data for Name: hl7_lab_interfaces; Type: TABLE DATA; 
--

--
-- Data for Name: hl7_order_items; Type: TABLE DATA; 
--

--
-- Data for Name: hl7_order_items_main; Type: TABLE DATA; 
--

--
-- Data for Name: hl7_patient_details; Type: TABLE DATA; 
--

--
-- Data for Name: hl7_result_codes; Type: TABLE DATA; 
--

--
-- Data for Name: hl7_visit_details; Type: TABLE DATA; 
--

--
-- Data for Name: hosp_accounting_prefs; Type: TABLE DATA; 
--

INSERT INTO hosp_accounting_prefs (pharmacy_separate_entity, inter_co_vouchers, tally_guid_prefix, separate_acc_for_out_vat, separate_purcharse_acc_for_vat, separate_acc_for_in_vat, separate_sales_acc_for_vat, pharmacy_sales_acc_include_vat, ip_income_acc_prefix, ip_income_acc_suffix, op_income_acc_prefix, op_income_acc_suffix, others_income_acc_prefix, others_income_acc_suffix, bill_reference, cost_center_basis, single_acc_for_item_and_bill_discounts, pharmacy_income_dept, incoming_test_income_dept, outside_pat_income_dept, phar_sales_to_hosp_patient, all_centers_same_comp_name) VALUES ('N', 'N', 'xxx', 'N', 'N', 'N', 'N', 'N', '', '', '', '', '', '', 'bill_no', 'None', 'N', 'DEP_PHA', 'DEP_LAB', 'DEP_PHA', 'Pharmacy Dept', 'Y');

--
-- Data for Name: hosp_allowed_certificates; Type: TABLE DATA; 
--

INSERT INTO hosp_allowed_certificates (processing_order, certificate_pattern, action) VALUES (100, '.*', 'A');

--
-- Data for Name: hosp_bill_audit_seq_prefs; Type: TABLE DATA; 
--

INSERT INTO hosp_bill_audit_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, center_id, bill_audit_number_seq_id, is_tpa, is_credit_note) VALUES (100, '*', '*', 'BILL_ACN_DEFAULT', '*', 0, 1, '*', '*');

--
-- Data for Name: hosp_bill_seq_prefs; Type: TABLE DATA; 
--

INSERT INTO hosp_bill_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, center_id, is_tpa, is_credit_note, bill_seq_id) VALUES (10, 'P', '*', 'BILL_PHARMACY', 'P', 0, '*', 'f', 1);
INSERT INTO hosp_bill_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, center_id, is_tpa, is_credit_note, bill_seq_id) VALUES (11, 'P', '*', 'BILL_PHARMACY_RETURN', 'P', 0, '*', 'f', 2);
INSERT INTO hosp_bill_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, center_id, is_tpa, is_credit_note, bill_seq_id) VALUES (20, '*', 't', 'BILL_INCOMING', '*', 0, '*', 'f', 3);
INSERT INTO hosp_bill_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, center_id, is_tpa, is_credit_note, bill_seq_id) VALUES (30, 'P', '*', 'BILL_ALTERNATE', '*', 0, '*', 'f', 4);
INSERT INTO hosp_bill_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, center_id, is_tpa, is_credit_note, bill_seq_id) VALUES (100, '*', '*', 'BILL_DEFAULT', '*', 0, '*', 'f', 5);
INSERT INTO hosp_bill_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, center_id, is_tpa, is_credit_note, bill_seq_id) VALUES (101, '*', '*', 'BILL_DEFAULT_CREDITNOTE', '*', 0, '*', 't', 6);

--
-- Data for Name: hosp_claim_seq_prefs; Type: TABLE DATA; 
--

INSERT INTO hosp_claim_seq_prefs (priority, center_id, pattern_id, claim_seq_id) VALUES (1000, 0, 'CLAIM_DEFAULT', 1);

--
-- Data for Name: hosp_direct_bill_prefs; Type: TABLE DATA; 
--

INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('Doctor', 'N', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('Meal', 'N', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('Equipment', 'N', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('Laboratory', 'N', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('Radiology', 'N', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('Service', 'N', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('Operation', 'N', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('Package', 'N', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('DiagPackage', 'N', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('Bed', 'N', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('ICU', 'N', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('Other Charge', 'Y', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('Direct Charge', 'Y', 'Y');
INSERT INTO hosp_direct_bill_prefs (item_type, orderable, block_unpaid) VALUES ('MultiVisitPackage', 'N', 'Y');

--
-- Data for Name: hosp_hl7_prefs; Type: TABLE DATA; 
--

--
-- Data for Name: hosp_id_patterns; Type: TABLE DATA; 
--

INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BILL_DEFAULT', 'BL', '', '99000000', 'bill_default_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BILL_ALTERNATE', 'BN', '', '99000000', 'bill_alt_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BILL_PHARMACY', 'BP', '', '99000000', 'bill_pharmacy_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BILL_PHARMACY_RETURN', 'BR', '', '99000000', 'bill_pharmacy_return_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BILL_INCOMING', 'RT', '', '99000000', 'bill_incoming_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('RECEIPT_DEFAULT', 'RC', '', '99000000', 'receipt_default_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('RECEIPT_ALTERNATE', 'RA', '', '99000000', 'receipt_alt_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('RECEIPT_PHARMACY', 'RP', '', '99000000', 'receipt_pharmacy_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('SPONSOR_RECEIPT_DEFAULT', 'RS', '', '99000000', 'sponsor_receipt_default_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('REFUND_DEFAULT', 'RF', '', '99000000', 'refund_default_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('SALES_DEFAULT', 'SB', '', '99000000', 'pharmacy_medicine_sales_main_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('CASENO', 'CS', '', '99000000', 'case_number_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('OUTPATIENT', 'OP', '', '99000000', 'op_number_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('INPATIENT', 'IP', '', '99000000', 'ip_number_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('MRNO', 'MR', '', '99000000', 'mrno_number_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('SPONSOR_BILL_DEFAULT', 'BS', '', '99000000', 'sponsor_bill_default', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('SPONSOR_CONSOLIDATED_RECEIPT_DEFAULT', 'RB', '', '99000000', 'sponsor_consolidated_receipt_default', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('MLCNO', 'MLC', '', '99000000', 'patient_mlc_no_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('PO_DEFAULT', 'PO', '', '9990000', 'po_id_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('GRN_DEFAULT', 'GR', '', '9990000', 'grn_id_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('anesthesia_type_master', 'AT', '', '0000', 'anesthesia_type_master_seq', NULL, '', 'Master', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('ITEM_BAR_CODE', 'ITM', '', '9900000', 'item_barcode_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('insurance_submission_batch', 'IS', '', '99000000', 'insurance_submission_batch_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('insurance_claim', 'IC', '', '99000000', 'insurance_claim_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('insurance_claim_receipt', 'IR', '', '99000000', 'insurance_claim_receipt_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('SAMPLE_ID', 'SL', '', '99000000', 'sample_id_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('PATIENT_INDENT_NO', 'PI', '', '99000000', 'store_patient_indent_main_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BILL_ACN_DEFAULT', 'ACN', '', '999000000', 'bill_acn_default_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('CONSUMPTIONNO', 'CN', '', '9900000', 'general_reagent_usage_main_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('pbm_request_approval_details', 'PR', '', '99000000', 'pbm_request_id_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('preauth_request_approval_details', 'EPR', '', '999000000', 'preauth_request_id_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('CLAIM_DEFAULT', 'CLD', '', '99000000', 'claim_id_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BILL_ADJ_DEFAULT', 'BAD', '', '99000000', 'bill_adjustment_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BILL_CHARGE_ADJ_DEFAULT', 'CAD', '', '99000000', 'bill_charge_adjustment_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BILL_CLAIM_ADJ_DEFAULT', 'BCAD', '', '99000000', 'bill_claim_adjustment_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BAG_BAR_CODE', 'BAGCODE', '', '9900000', 'bag_barcode_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BAG_COMPONENT_BAR_CODE', 'COMPCODE', '', '9900000', 'bag_component_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('CONSOLIDATED_BILL', 'CO', '', '9900000', 'consolidated_bill_no_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BILL_DEFAULT_CREDITNOTE', 'CN', 'YY', '9999000000', 'creditnote_default_seq', 'F', '16', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('CONSOLIDATED_CREDIT_NOTE', 'CCN', '', '9900000', 'consolidated_credit_note_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('ITEM_DEFAULT', 'ITMDEF', '', '9900000', 'item_code_seq', ' ', '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('DOCTOR_PAYMENTS', 'P', '', 'FM0000000000', 'payments_sequence', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('BILL_CHARGE_DETAILS_ADJ_DEFAULT', 'CADE', '', '99000000', 'bill_charge_details_adjustment_seq', NULL, '', 'Txn', NULL);
INSERT INTO hosp_id_patterns (pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type, transaction_type) VALUES ('VOUCHER_NUMBER_DEFAULT', 'VC', '', '99000000', 'hosp_voucher_seq_prefs_seq', ' ', '', 'Txn', 'PVN');

--
-- Data for Name: hosp_item_seq_prefs; Type: TABLE DATA; 
--

INSERT INTO hosp_item_seq_prefs (item_seq_id, priority, pattern_id, category_id, center_id) VALUES (1, 100, 'ITEM_DEFAULT', 0, 0);

--
-- Data for Name: hosp_op_ip_seq_prefs; Type: TABLE DATA; 
--

INSERT INTO hosp_op_ip_seq_prefs (priority, visit_type, pattern_id, center_id, visit_seq_id) VALUES (100, 'o', 'OUTPATIENT', 0, 1);
INSERT INTO hosp_op_ip_seq_prefs (priority, visit_type, pattern_id, center_id, visit_seq_id) VALUES (200, 'i', 'INPATIENT', 0, 2);

--
-- Data for Name: hosp_party_account_names; Type: TABLE DATA; 
--

INSERT INTO hosp_party_account_names (supplier_individual_accounts, supplier_ac_name, supplier_ac_prefix, supplier_ac_suffix, tpa_individual_accounts, tpa_ac_name, tpa_ac_prefix, tpa_ac_suffix, doctor_individual_accounts, doctor_ac_name, doctor_op_ac_prefix, doctor_op_ac_suffix, outhouse_individual_accounts, outhouse_ac_name, outhouse_ac_prefix, outhouse_ac_suffix, referral_individual_accounts, referral_ac_name, referral_op_ac_prefix, referral_op_ac_suffix, misc_individual_accounts, misc_ac_name, misc_ac_prefix, misc_ac_suffix, prescribingdoctor_individual_accounts, prescribingdoctor_ac_name, prescribingdoctor_op_ac_prefix, prescribingdoctor_op_ac_suffix, doctor_ip_ac_prefix, doctor_ip_ac_suffix, prescribingdoctor_ip_ac_prefix, prescribingdoctor_ip_ac_suffix, referral_ip_ac_prefix, referral_ip_ac_suffix) VALUES ('Y', '', '', '', 'Y', '', '', '', 'Y', '', '', '', 'Y', '', '', '', 'Y', '', '', '', 'Y', '', '', '', 'Y', '', '', '', '', '', '', '', '', '');

--
-- Data for Name: hosp_pharmacy_sale_seq_prefs; Type: TABLE DATA; 
--

INSERT INTO hosp_pharmacy_sale_seq_prefs (priority, bill_type, visit_type, pattern_id, sale_type, dept_id, pharmacy_bill_seq_id) VALUES (100, '*', '*', 'SALES_DEFAULT', '*', '*', 1);

--
-- Data for Name: hosp_print_master; Type: TABLE DATA; 
--

INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('Bill', 1, '', '', '', '', '', '', NULL, 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('Pharmacy', 3, '', '', '', '', '', '', NULL, 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('Discharge', 4, '', '', '', '', '', '', NULL, 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('Service', 2, '', '', '', '', '', '', NULL, 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('Insurence', 6, '', '', '', '', '', '', NULL, 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('Store', 7, '', '', '', '', '', '', NULL, 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('Rad', 8, '', '', '', '', '', '', NULL, 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('Lab', 2, '', '', '', '', '', '', NULL, 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('Appointment', 4, '', '', '', '', '', '', '', 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('FeedbackForm', 4, '', '', '', '', '', '', '', 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('Sample', 4, '', '', '', '', '', '', NULL, 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('SurveyResponse', 4, '', '', '', '', '', '', NULL, 0, NULL);
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('PrescLabel', 3, '', '', '', '', '', '', '', 0, '');
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('Web Diag', 2, '', '', '', '', '', '', '', 0, '');
INSERT INTO hosp_print_master (print_type, printer_id, header1, header2, header3, footer1, footer2, footer3, pre_final_watermark, center_id, duplicate_watermark) VALUES ('SampleWorkSheet', 2, '', '', '', '', '', '', '', 0, '');

--
-- Data for Name: hosp_print_master_files; Type: TABLE DATA; 
--

INSERT INTO hosp_print_master_files (logo, custom_reg_card_template, screen_logo, center_id) VALUES (NULL, NULL, NULL, 0);

--
-- Data for Name: hosp_receipt_seq_prefs; Type: TABLE DATA; 
--

INSERT INTO hosp_receipt_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, payment_type, center_id, receipt_number_seq_id) VALUES (10, '*', '*', 'RECEIPT_PHARMACY', 'P', 'R', 0, 1);
INSERT INTO hosp_receipt_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, payment_type, center_id, receipt_number_seq_id) VALUES (20, '*', '*', 'REFUND_DEFAULT', '*', 'F', 0, 2);
INSERT INTO hosp_receipt_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, payment_type, center_id, receipt_number_seq_id) VALUES (40, '*', '*', 'RECEIPT_DEFAULT', '*', 'R', 0, 3);
INSERT INTO hosp_receipt_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, payment_type, center_id, receipt_number_seq_id) VALUES (100, '*', '*', 'RECEIPT_DEFAULT', '*', '*', 0, 4);
INSERT INTO hosp_receipt_seq_prefs (priority, bill_type, visit_type, pattern_id, restriction_type, payment_type, center_id, receipt_number_seq_id) VALUES (30, '*', '*', 'SPONSOR_RECEIPT_DEFAULT', '*', 'S', 0, 5);

--
-- Data for Name: hosp_special_account_names; Type: TABLE DATA; 
--

INSERT INTO hosp_special_account_names (counter_receipts_ac_name, claims_ac_name, inv_purchase_ac_name, inv_preturns_ac_name, misc_payments_ac_name, pharma_claim_ac_name, pharma_receipts_ac_name, pharma_refunds_ac_name, pharma_preturns_ac_name, hospital_transfer_act_name, writeoff_ac_name, doctor_payments_exp_ac_name, referral_payments_exp_act_name, outgoing_vat_ac_name, incoming_vat_ac_name, pharm_sales_round_off_ac_name, pharm_sales_disc_ac_name, pharm_inv_disc_ac_name, pharm_inv_round_off_ac_name, pharm_inv_other_charges_ac_name, patient_deposit_ac_name, pharmacy_cess_ac_name, prescribing_doctor_payments_exp_ac_name, outhouse_payments_exp_act_name, incoming_cst_ac_name, transfer_expenses, counter_receipts_ac_name_ip, counter_receipts_ac_name_op, counter_receipts_ac_name_others, tds_receipt_ac_name, tds_payment_ac_name, outgoing_ced_ac_name, patient_points_ac_name) VALUES ('Counter Receipts', 'Bill Claims', 'Purchase', 'Purchase', 'Misc Expenses', 'Pharmacy Claims', 'Pharmacy Receipts', 'Pharmacy Refunds', 'Pharmacy Purchase', 'Hospital Transfer', 'Write offs', 'Doctor Payments', 'Referral Payments', 'Outgoing VAT', 'Incoming VAT', 'Pharmacy Sales Round-Off', 'Pharmacy Sales Discount', 'Pharmacy Invoice Discount', 'Pharmacy Invoice Round-Off', 'Pharmacy Invoice Other Charges', 'Patient Deposits', 'Pharmacy Cess', 'Prescribing Doctor Payments', 'Outhouse Payments', 'Outgoing CST', 'Transfer Expenses', 'Counter Receipts', 'Counter Receipts', 'Counter Receipts', 'TDS (Receipt)', 'TDS (Payment)', 'Outgoing CED', 'Patient Points');

--
-- Data for Name: hosp_voucher_seq_prefs; Type: TABLE DATA; 
--

INSERT INTO hosp_voucher_seq_prefs (voucher_seq_id, priority, center_id, pattern_id) VALUES (1, 1000, 0, 'VOUCHER_NUMBER_DEFAULT');

--
-- Data for Name: hosp_voucher_types; Type: TABLE DATA; 
--

INSERT INTO hosp_voucher_types (receipt_vtype, refund_vtype, claim_vtype, bill_vtype, pharmacy_bill_vtype, pharmacy_return_vtype, payment_voucher_vtype, purchase_vtype, purchase_return_vtype, transfers_vtype, payment_vtype) VALUES ('Journal', 'Journal', 'Journal', 'Journal', 'Journal', 'Journal', 'Journal', 'Journal', 'Journal', 'Journal', 'Journal');

--
-- Data for Name: hospital_roles_master; Type: TABLE DATA; 
--

--
-- Data for Name: hospital_technical; Type: TABLE DATA; 
--

--
-- Data for Name: hvf_print_template; Type: TABLE DATA; 
--

--
-- Data for Name: icu_bed_charges; Type: TABLE DATA; 
--

--
-- Data for Name: image_markers; Type: TABLE DATA; 
--

--
-- Data for Name: incoming_hospitals; Type: TABLE DATA; 
--

--
-- Data for Name: incoming_sample_registration; Type: TABLE DATA; 
--

--
-- Data for Name: incoming_sample_registration_details; Type: TABLE DATA; 
--

--
-- Data for Name: insta_section_rights; Type: TABLE DATA; 
--

INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (1, 4, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (2, 4, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (3, 4, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (4, 4, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (5, 4, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (6, 1, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (7, 1, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (8, 1, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (9, 1, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (10, 1, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (11, 2, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (12, 2, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (13, 2, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (14, 2, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (15, 2, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (16, 3, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (17, 3, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (18, 3, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (19, 3, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (20, 3, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (21, 5, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (22, 5, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (23, 5, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (24, 5, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (25, 5, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (26, 6, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (27, 6, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (28, 6, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (29, 6, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (30, 6, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (31, 7, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (32, 7, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (33, 7, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (34, 7, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (35, 7, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (36, -5, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (37, -5, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (38, -5, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (39, -5, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (40, -5, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (41, -7, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (42, -7, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (43, -7, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (44, -7, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (45, -7, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (46, -15, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (47, -15, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (48, -15, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (49, -15, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (50, -15, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (51, -3, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (52, -3, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (53, -3, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (54, -3, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (55, -3, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (56, -1, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (57, -1, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (58, -1, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (59, -1, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (60, -1, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (61, -2, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (62, -2, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (63, -2, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (64, -2, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (65, -2, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (66, -6, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (67, -6, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (68, -6, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (69, -6, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (70, -6, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (71, -16, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (72, -16, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (73, -16, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (74, -16, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (75, -16, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (76, -4, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (77, -4, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (78, -4, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (79, -4, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (80, -4, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (81, -13, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (82, -13, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (83, -13, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (84, -13, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (85, -13, 6);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (86, -14, 2);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (87, -14, 3);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (88, -14, 4);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (89, -14, 5);
INSERT INTO insta_section_rights (section_role_id, section_id, role_id) VALUES (90, -14, 6);

--
-- Data for Name: insurance_aggregator_center_config; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_case; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_category_center_master; Type: TABLE DATA; 
--

INSERT INTO insurance_category_center_master (inscat_center_id, category_id, center_id, status) VALUES (1, 133, 0, 'A');

--
-- Data for Name: insurance_category_master; Type: TABLE DATA; 
--

INSERT INTO insurance_category_master (category_id, insurance_co_id, category_name, status) VALUES (133, 'ICM00002', 'Default Plan Type', 'A');

--
-- Data for Name: insurance_change_log; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_claim; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_claim_docs; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_claim_receipt; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_claim_template; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_company_master; Type: TABLE DATA; 
--

INSERT INTO insurance_company_master (insurance_co_id, insurance_co_name, insurance_co_address, insurance_co_city, insurance_co_state, insurance_co_country, insurance_co_phone, insurance_co_email, status, default_rate_plan, insurance_co_code_obsolete, insurance_rules_doc_bytea, insurance_rules_doc_name, insurance_rules_doc_type, tin_number) VALUES ('ICM00002', 'Default Insurance Company', '', '', '', '', '', '', 'A', NULL, NULL, NULL, NULL, NULL, NULL);

--
-- Data for Name: insurance_company_tpa_master; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_denial_code_types; Type: TABLE DATA; 
--

INSERT INTO insurance_denial_code_types (type, status) VALUES ('Administrative information', 'A');
INSERT INTO insurance_denial_code_types (type, status) VALUES ('Audit', 'A');
INSERT INTO insurance_denial_code_types (type, status) VALUES ('Authorization', 'A');
INSERT INTO insurance_denial_code_types (type, status) VALUES ('Benefit expiration', 'A');
INSERT INTO insurance_denial_code_types (type, status) VALUES ('Clinical information', 'A');
INSERT INTO insurance_denial_code_types (type, status) VALUES ('Copay', 'A');
INSERT INTO insurance_denial_code_types (type, status) VALUES ('Duplicate', 'A');
INSERT INTO insurance_denial_code_types (type, status) VALUES ('Eligibility', 'A');
INSERT INTO insurance_denial_code_types (type, status) VALUES ('Medical Necessity', 'A');
INSERT INTO insurance_denial_code_types (type, status) VALUES ('Non-coverage', 'A');
INSERT INTO insurance_denial_code_types (type, status) VALUES ('Price', 'A');
INSERT INTO insurance_denial_code_types (type, status) VALUES ('Timely filing', 'A');

--
-- Data for Name: insurance_denial_codes; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_docs; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_estimate; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_payment_allocation; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_payment_unalloc_amount; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_plan_details; Type: TABLE DATA; 
--

INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, -1, 0.00, 0.00, 0.00, 0.00, 'o', 0.00, 'InstaAdmin', 'Y', NULL, 1);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, -1, 0.00, 0.00, 0.00, 0.00, 'i', 0.00, 'InstaAdmin', 'Y', NULL, 2);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 1, 0.00, 0.00, 0.00, 0.00, 'o', 0.00, 'InstaAdmin', 'Y', NULL, 3);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 1, 0.00, 0.00, 0.00, 0.00, 'i', 0.00, 'InstaAdmin', 'Y', NULL, 4);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 2, 0.00, 0.00, 0.00, 0.00, 'o', 0.00, 'InstaAdmin', 'Y', NULL, 5);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 2, 0.00, 0.00, 0.00, 0.00, 'i', 0.00, 'InstaAdmin', 'Y', NULL, 6);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 3, 0.00, 0.00, 0.00, 0.00, 'o', 0.00, 'InstaAdmin', 'Y', NULL, 7);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 3, 0.00, 0.00, 0.00, 0.00, 'i', 0.00, 'InstaAdmin', 'Y', NULL, 8);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 4, 0.00, 0.00, 0.00, 0.00, 'o', 0.00, 'InstaAdmin', 'Y', NULL, 9);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 4, 0.00, 0.00, 0.00, 0.00, 'i', 0.00, 'InstaAdmin', 'Y', NULL, 10);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 5, 0.00, 0.00, 0.00, 0.00, 'o', 0.00, 'InstaAdmin', 'Y', NULL, 11);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 5, 0.00, 0.00, 0.00, 0.00, 'i', 0.00, 'InstaAdmin', 'Y', NULL, 12);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 6, 0.00, 0.00, 0.00, 0.00, 'o', 0.00, 'InstaAdmin', 'Y', NULL, 13);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 6, 0.00, 0.00, 0.00, 0.00, 'i', 0.00, 'InstaAdmin', 'Y', NULL, 14);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 7, 0.00, 0.00, 0.00, 0.00, 'o', 0.00, 'InstaAdmin', 'Y', NULL, 15);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 7, 0.00, 0.00, 0.00, 0.00, 'i', 0.00, 'InstaAdmin', 'Y', NULL, 16);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 8, 0.00, 0.00, 0.00, 0.00, 'o', 0.00, 'InstaAdmin', 'Y', NULL, 17);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, 8, 0.00, 0.00, 0.00, 0.00, 'i', 0.00, 'InstaAdmin', 'Y', NULL, 18);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, -2, 0.00, 0.00, NULL, NULL, 'o', 0.00, 'InstaAdmin', 'Y', NULL, 19);
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_amount, patient_percent, patient_amount_cap, per_treatment_limit, patient_type, patient_amount_per_category, username, category_payable, category_prior_auth_required, insurance_plan_details_id) VALUES (1, -2, 0.00, 0.00, NULL, NULL, 'i', 0.00, 'InstaAdmin', 'Y', NULL, 20);

--
-- Data for Name: insurance_plan_details_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_plan_main; Type: TABLE DATA; 
--

INSERT INTO insurance_plan_main (plan_id, plan_name, category_id, overall_treatment_limit, insurance_co_id, plan_notes, plan_exclusions, default_rate_plan, username, mod_time, ip_applicable, op_applicable, status, is_copay_pc_on_post_discnt_amt, base_rate, gap_amount, marginal_percent, perdiem_copay_per, perdiem_copay_amount, require_pbm_authorization, op_visit_copay_limit, ip_visit_copay_limit, insurance_validity_start_date, insurance_validity_end_date, op_plan_limit, op_episode_limit, op_visit_limit, ip_plan_limit, ip_visit_limit, ip_per_day_limit, op_visit_deductible, ip_visit_deductible, op_copay_percent, ip_copay_percent, limits_include_followup, sponsor_id, discount_plan_id, add_on_payment_factor) VALUES (1, 'Default Insurance Company Plan', 133, 0.00, 'ICM00002', NULL, NULL, NULL, 'unknown', '2016-09-14 17:51:06.671204', 'Y', 'Y', 'A', 'Y', 0.00, 0.00, 0.00, 0.00, 0.00, 'N', 0.00, 0.00, NULL, NULL, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'N', NULL, NULL, 75.00);

--
-- Data for Name: insurance_plan_main_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_preauth; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_preauth_values; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_remittance; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_remittance_activity_details; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_remittance_details; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_status; Type: TABLE DATA; 
--

INSERT INTO insurance_status (status_id, status_name) VALUES ('P', 'Preauth');
INSERT INTO insurance_status (status_id, status_name) VALUES ('A', 'Approved');
INSERT INTO insurance_status (status_id, status_name) VALUES ('F', 'Finalized');
INSERT INTO insurance_status (status_id, status_name) VALUES ('C', 'Closed');
INSERT INTO insurance_status (status_id, status_name) VALUES ('D', 'Denied');

--
-- Data for Name: insurance_submission_batch; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_tpa_docs; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_transaction; Type: TABLE DATA; 
--

--
-- Data for Name: insurance_transaction_attachments; Type: TABLE DATA; 
--

--
-- Data for Name: integ_diag_in; Type: TABLE DATA; 
--

--
-- Data for Name: integ_diag_in_bkp; Type: TABLE DATA; 
--

--
-- Data for Name: ip_bed_details; Type: TABLE DATA; 
--

--
-- Data for Name: ip_doctor_notes; Type: TABLE DATA; 
--

--
-- Data for Name: ip_nurse_notes; Type: TABLE DATA; 
--

--
-- Data for Name: ip_preferences; Type: TABLE DATA; 
--

INSERT INTO ip_preferences (bed_charge_posting, charge_posting_hour, hrly_charge_threshold, halfday_charge_threshold, fullday_charge_threshold, bedshift_hrly_charge_threshold, bedshift_halfday_charge_threshold, bedshift_fullday_charge_threshold, max_daycare_hours, discharge_grace_period, alarm_limit, bystander_bed_charges_applicable_on, current_bed_type_is_bill_bed_type, bystander_availability, duty_doctor_selection, force_remarks, cut_off_required, allocate_bed_at_reg, daycare_min_duration, daycare_slab_1_threshold, daycare_slab_2_threshold, icu_hrly_charge_threshold, icu_halfday_charge_threshold, icu_fullday_charge_threshold, icu_bedshift_hrly_charge_threshold, icu_bedshift_halfday_charge_threshold, icu_bedshift_fullday_charge_threshold, merge_beds, slab1_duration, next_slabs_duration, retain_bed_charges, split_theatre_charges, theatre_charge_code_type, theatre_daily_charge_code, theatre_min_charge_code, theatre_slab1_charge_code, theatre_incr_charge_code, max_billable_cons_day) VALUES ('Estimated_duration', '00:00:00', '0', '0', '0', '0', '0', '0', 0, 0, '0', 'B', 'N', 'I', 'N', 'N', 'N', 'N', 0, 0, 0, '0', '0', '0', '0', '0', '0', 'N', 24, 24, 'B', 'N', NULL, NULL, NULL, NULL, NULL, 2);

--
-- Data for Name: ip_prescription; Type: TABLE DATA; 
--

--
-- Data for Name: item_form_master; Type: TABLE DATA; 
--

--
-- Data for Name: item_group_type; Type: TABLE DATA; 
--

INSERT INTO item_group_type (item_group_type_id, item_group_type_name, system_group, description, status) VALUES ('TAX', 'TAX', 'S', '                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ', 'A');

--
-- Data for Name: item_groups; Type: TABLE DATA; 
--

INSERT INTO item_groups (item_group_id, item_group_name, group_code, item_group_display_order, item_group_type_id, status) VALUES (1, 'CGST', 'GST', 1, 'TAX', 'A');
INSERT INTO item_groups (item_group_id, item_group_name, group_code, item_group_display_order, item_group_type_id, status) VALUES (2, 'SGST', 'GST', 2, 'TAX', 'A');
INSERT INTO item_groups (item_group_id, item_group_name, group_code, item_group_display_order, item_group_type_id, status) VALUES (3, 'i-GST', 'IGST', 3, 'TAX', 'A');

--
-- Data for Name: item_insurance_categories; Type: TABLE DATA; 
--

INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name, insurance_payable, system_category) VALUES (1, 'GP Consultation', 'Y', 'N');
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name, insurance_payable, system_category) VALUES (2, 'SP Consultation', 'Y', 'N');
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name, insurance_payable, system_category) VALUES (3, 'Laboratory', 'Y', 'N');
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name, insurance_payable, system_category) VALUES (4, 'Radiology', 'Y', 'N');
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name, insurance_payable, system_category) VALUES (5, 'Pharmacy', 'Y', 'N');
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name, insurance_payable, system_category) VALUES (6, 'Dental', 'Y', 'N');
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name, insurance_payable, system_category) VALUES (7, 'Maternity', 'Y', 'N');
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name, insurance_payable, system_category) VALUES (8, 'Chronic', 'Y', 'N');
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name, insurance_payable, system_category) VALUES (-1, 'General', 'Y', 'N');
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name, insurance_payable, system_category) VALUES (-2, 'DRG', 'Y', 'Y');

--
-- Data for Name: item_store_level_details; Type: TABLE DATA; 
--

--
-- Data for Name: item_sub_groups; Type: TABLE DATA; 
--

INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (1, 'CGST-0', 1, NULL, 1, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (7, 'SGST-0', 6, NULL, 2, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (13, 'iGST-0', 11, NULL, 3, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (14, 'iGST-5', 12, NULL, 3, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (15, 'iGST-12', 13, NULL, 3, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (16, 'iGST-18', 14, NULL, 3, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (17, 'iGST-28', 15, NULL, 3, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (2, 'CGST-2.5', 2, NULL, 1, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (3, 'CGST-6', 3, NULL, 1, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (4, 'CGST-9', 4, NULL, 1, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (5, 'CGST-14', 5, NULL, 1, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (8, 'SGST-2.5', 7, NULL, 2, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (9, 'SGST-6', 8, NULL, 2, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (10, 'SGST-9', 9, NULL, 2, 'A');
INSERT INTO item_sub_groups (item_subgroup_id, item_subgroup_name, item_subgroup_display_order, subgroup_code, item_group_id, status) VALUES (11, 'SGST-14', 10, NULL, 2, 'A');

--
-- Data for Name: item_sub_groups_tax_details; Type: TABLE DATA; 
--

INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (1, 0.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (2, 2.50, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (3, 6.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (4, 9.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (5, 14.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (7, 0.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (8, 2.50, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (9, 6.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (10, 9.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (11, 14.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (13, 0.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (14, 5.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (15, 12.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (16, 18.00, NULL, '2018-04-07', NULL);
INSERT INTO item_sub_groups_tax_details (item_subgroup_id, tax_rate, tax_rate_expr, validity_start, validity_end) VALUES (17, 28.00, NULL, '2018-04-07', NULL);

--
-- Data for Name: item_supplier_prefer_supplier; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_compl_oocyte; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_compl_oocyte_assess; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_comple_embryo_inf; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_cycle; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_cycle_allergies; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_daily_details; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_daily_follicles; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_daily_hormone_results; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_daily_prescription; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_donor_details; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_donor_header; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_luteal_hormone_levels; Type: TABLE DATA; 
--

--
-- Data for Name: ivf_luteal_prescriptions; Type: TABLE DATA; 
--

--
-- Data for Name: job_log; Type: TABLE DATA; 
--

--
-- Data for Name: label_master; Type: TABLE DATA; 
--

--
-- Data for Name: license_type_master; Type: TABLE DATA; 
--

--
-- Data for Name: licenses; Type: TABLE DATA; 
--

--
-- Data for Name: linen_usage_main; Type: TABLE DATA; 
--

--
-- Data for Name: linen_usage_details; Type: TABLE DATA; 
--

--
-- Data for Name: linen_user_category; Type: TABLE DATA; 
--

--
-- Data for Name: linen_user_master; Type: TABLE DATA; 
--

--
-- Data for Name: master_timestamp; Type: TABLE DATA; 
--

INSERT INTO master_timestamp (master_count) VALUES (1);

--
-- Data for Name: medical_fileupload; Type: TABLE DATA; 
--

--
-- Data for Name: medicine_dosage_master; Type: TABLE DATA; 
--

--
-- Data for Name: medicine_route; Type: TABLE DATA; 
--

--
-- Data for Name: medicine_type; Type: TABLE DATA; 
--

--
-- Data for Name: message_action_log; Type: TABLE DATA; 
--

--
-- Data for Name: message_actions; Type: TABLE DATA; 
--

INSERT INTO message_actions (message_action_id, message_action_name, message_url_action, message_action_type, allowed_actors, allowed_usage, options) VALUES (1, 'Delete', '', 'std_delete', 'A', 1, NULL);
INSERT INTO message_actions (message_action_id, message_action_name, message_url_action, message_action_type, allowed_actors, allowed_usage, options) VALUES (2, 'Archive', '', 'std_archive', 'A', 1, NULL);
INSERT INTO message_actions (message_action_id, message_action_name, message_url_action, message_action_type, allowed_actors, allowed_usage, options) VALUES (4, 'Bill Cancellation', '', 'custom_bill_cancellation', 'O', 1, 'Approve;Reject');

--
-- Data for Name: message_category; Type: TABLE DATA; 
--

INSERT INTO message_category (message_category_id, message_category_name, status) VALUES (1, 'Practo Share', 'A');

--
-- Data for Name: message_events; Type: TABLE DATA; 
--

INSERT INTO message_events (event_id, event_name, event_description) VALUES ('ui_trigger', 'User Trigger', 'Generic event used for all user triggered messages');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('appointment_confirmed', 'Appointment Confirmed', 'Event used for triggering the appointment confirmation message');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('doctor_unavailable', 'Doctor Unavailable', 'Event used for triggering the appointment reschedule when a doctor is marked unavailable');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('diag_report_signoff', 'Report Signoff', 'Event used for triggering a message when the diag report is signed off');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('appointment_booked', 'Appointment Booked', 'Event used for triggering the appointment booked message');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('patient_admitted', 'Patient Admitted', 'Event used for triggering IP patient admitted message');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('appointment_cancelled', 'Appointment Cancelled', 'Event used for triggering the appointment cancellation message');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('patient_on_patient_admitted', 'Patient Admission to Patient and Patient Family', 'Event used for triggering IP patient admitted message');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('revise_patient_on_patient_admitted', 'Patient Admission to Patient and Patient Family Revise', 'Event used for triggering IP/OP patient admitted message revise');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('bill_cancelled', 'Bill Cancellation', 'Event used for triggering the bill cancellation message');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('appointment_details_changed', 'Appointment Details Change', 'Event used for triggering the appointment Reschedule Details message');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('edit_patient_appointment_reminder', 'Edit Patient Appointment Details', 'Event used for triggering the Edit Patient Details message');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('next_day_appointment_reminder', 'Patient Appointment Reminder', 'Event used for triggering the appointment Reminder one day before an appointment');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('purches_order_report', 'Email sent to supllier', 'Event used for triggering send mail to supplier');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('patient_on_ip_patient_admission', 'Send to Patient on IP patient admission', 'Event used for triggering the IP patient admission');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('family_on_ip_patient_admission', 'Send to Family on IP patient admission', 'Event used for triggering the IP patient admission');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('patient_on_op_patient_admission', 'Send to Patient on OP patient admission', 'Event used for triggering the OP patient admission');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('family_on_op_patient_admission', 'Send to Family on OP patient admission', 'Event used for triggering the OP patient admission');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('patient_on_ip_patient_revisit', 'Send to Patient on IP patient revisit', 'Event used for triggering the IP patient revisit');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('family_on_ip_patient_revisit', 'Send to Family on IP patient revisit', 'Event used for triggering the IP patient revisit');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('patient_on_op_patient_revisit', 'Send to Patient on OP patient revisit', 'Event used for triggering the OP patient revisit');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('family_on_op_patient_revisit', 'Send to family on OP patient revisit', 'Event used for triggering the OP patient revisit');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('diag_report_revert', 'PHR_diag_report_revert', 'Event used for triggering a cancellation email to PHR when the diag report is reverted');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('bill_payment_message', 'Payment Received', 'Event used for sending Payment Received message');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('vaccine_reminder', 'Vaccination Reminder', 'Event used for triggering the vaccination Reminder');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('op_bn_cash_bill_paid_closed', 'Bills over mail event', 'Event used for triggering a OP BN Cash Bill email to patient when the bill is closed');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('manual_op_bn_cash_bill_paid_closed', 'Bills over mail event for sending manual mails', 'Event used for triggering a manual OP BN Cash Bill email to patient when the bill is closed');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('gen_doc_finalize', 'PHR_gen_doc_finalize', 'Event used for triggering a finaliation email to PHR when the generic document is finalized');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('gen_doc_delete', 'PHR_gen_doc_delete', 'Event used for triggering a deletion email to PHR when the generic document is deleted');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('op_bill_paid', 'PHR_op_bill_paid', 'Event used for triggering a finaliation email to PHR when the OP bill is paid');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('ip_bill_paid', 'PHR_ip_bill_paid', 'Event used for triggering a finaliation email to PHR when the IP bill is paid');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('advance_paid', 'Advance paid', 'Event used for triggering a SMS to the owner when advance is paid');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('daily_collection', 'Daily Collection', 'Event used for triggering a SMS to the owner to inform about daily collection');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('patient_physical_discharge', 'Patient Physical Discharge', 'Event used to inform discharging doctor and referral doctor about patient''s physical discharge');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('discount_given', 'Discount Given', 'Event used for triggering a SMS to the owner to inform discount given to a patient by a authorizer');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('ip_phr_diag_share', 'IP PHR Diag Report share', 'Event used for triggering an email to phr team for sharing diag report for ip');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('op_phr_diag_share', 'OP PHR Diag Report share', 'Event used for triggering an email to phr team for sharing diag report for op');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('ward_bed_shift', 'sms_ward_bed_shift', 'Event used for SMS when bed or ward is changed');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('patient_on_discharge', 'Patient Discharge', 'Event used when patient initiated for various discharge states');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('inform_nok_on_patient_discharge', 'Patient Discharge Family Information Message', 'Event used to inform patient family for various discharge states');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('pharmacy_bill_paid', 'PHR_pharmacy_bill_paid', 'Event used for triggering a finaliation email to PHR when the pharmacy bill is paid');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('dynamic_appointment_reminder', 'dynamic appointment reminder', 'Event used for triggering a SMS to the patient ');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('patient_due_for_visit', 'Patient Due SMS for Visit Id', 'Event used for triggering a SMS to the patient and next of kin for the visit number');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('prescription_saved', 'phr_prescription_saved', 'Event used for triggering a finaliation email to PHR when the prescription is saved');
INSERT INTO message_events (event_id, event_name, event_description) VALUES ('lab_critical_val', 'Critical Lab Values', 'Event used to inform test critical values');

--
-- Data for Name: message_types; Type: TABLE DATA; 
--

INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('notification_bill_cancellation', 'Bill Cancellation', 'Message sent to Discount Authorizer When Bill is Cancelled', NULL, NULL, NULL, 'Bill Cancallation Notification For Discount Authorizer', NULL, NULL, 'bill_cancelled', 'NOTIFICATION', 'A', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_revise_patient_on_patient_admitted', 'Patient Admission to Patient and Patient Family Revise', 'Message sent to Patient and Patient party on IP/OP admission Revise', NULL, NULL, NULL, 'obsolete', NULL, NULL, 'revise_patient_on_patient_admitted', 'SMS', 'I', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_patient_on_patient_admitted', 'Patient Admission to Patient and Patient Family', 'Message sent to Patient and Patient party on IP admission', NULL, NULL, NULL, 'obsolete', NULL, NULL, 'patient_on_patient_admitted', 'SMS', 'I', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_purches_oder_report', 'Purchase Order Report', 'Message is sent automatically to supplier whenever the Purchase order is approved in the system.', NULL, NULL, NULL, NULL, NULL, NULL, 'purches_order_report', 'EMAIL', 'A', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_appointment_cancellation', 'Appointment Cancelled', 'Message is sent automatically to the patient when an appointment is cancelled.', NULL, NULL, NULL, 'Your appointment is Cancelled', NULL, NULL, 'appointment_cancelled', 'SMS', 'I', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_appointment_confirmation', 'Appointment Confirmed', 'Message is sent automatically to the patient when an appointment is confirmed.', NULL, NULL, NULL, 'Your appointment with doctor ${appointment_doctor} on ${appointment_time} is confirmed', NULL, NULL, 'appointment_confirmed', 'SMS', 'I', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_appointment_details_change', 'Appointment Details Change', 'Message is sent automatically to the patient when an appointment details are changed.', NULL, NULL, NULL, 'Your appointment is Rescheduled', NULL, NULL, 'appointment_details_changed', 'SMS', 'I', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_appointment_reschedule', 'Appointment Reschedule', 'Message is sent automatically to all the patients in doctor''s schedule when someone marks the doctor as unavailable. A message is sent to reschedule the appointment.', NULL, NULL, NULL, 'Your appointment with ${appointment_doctor} due on ${appointment_time} needs to be rescheduled as the doctor is unavailable. Please rescedule your appointment', NULL, NULL, 'doctor_unavailable', 'SMS', 'I', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_edit_patient_access', 'Edit Patient Marking', 'Message is automatically sent to the patient when user edits his/her registration details and the patient has mobile access.', NULL, NULL, NULL, 'Edit Patient Appointment Reminder', NULL, NULL, 'edit_patient_appointment_reminder', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_followup_reminder', 'Followup Reminder', 'Message is manually sent to patients reminding them about the due follow-up appointments.', NULL, NULL, NULL, 'Your Followup with doctor ${followup_doctor} is due on ${followup_date}', NULL, NULL, 'ui_trigger', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_patient_admitted', 'Patient Admission to Doctor', 'Message is sent automatically to the admitting and referral doctor on IP admission of a patient. This is to inform the doctor about patient''s admission.', NULL, NULL, NULL, 'Patient, ${patient_name}, admitted on ${admission_date} at ${admission_time}', NULL, NULL, 'patient_admitted', 'SMS', 'I', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_family_on_ip_admission', 'New IP Patient, Family Information Message', 'Message is sent automatically to the IP patient''s kin when the patient registers in the hospital for the first time.', NULL, NULL, NULL, 'Your relative ${patient_name} is admitted in ${center_name}. The patient id is ${mr_no}. ', NULL, NULL, 'family_on_ip_patient_admission', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_patient_on_op_admission', 'New OP Patient Welcome Message', 'Message is sent automatically to the OP patient when the patient registers in the hospital for the first time.', NULL, NULL, NULL, 'Dear ${patient_name}, thank you for registering at ${center_name}. Your patient id is ${mr_no}', NULL, NULL, 'patient_on_op_patient_admission', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_family_on_op_admission', 'New OP Patient, Family Information Message', 'Message is sent automatically to the OP patient''s kin when the patient registers in the hospital for the first time.', NULL, NULL, NULL, 'Your relative ${patient_name} has successfully registered as an out-patient at our hospital with patient id ${mr_no}.', NULL, NULL, 'family_on_op_patient_admission', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_patient_on_op_revisit', 'Revisit OP Patient welcome message', 'Message is sent automatically to the OP patient when the patient revisits the hospital. The message is sent post visit registration.', NULL, NULL, NULL, 'Dear ${patient_name}, welcome to ${center_name}. Your patient Id is ${mr_no}', NULL, NULL, 'patient_on_op_patient_revisit', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_patient_on_ip_revisit', 'Revisit IP Patient welcome message', 'Message is sent automatically to the IP patient when the patient revisits the hospital. The message is sent post visit registration.', NULL, NULL, NULL, 'Dear ${patient_name}, thank you for registering at ${center_name}. Your patient id is ${mr_no}.', NULL, NULL, 'patient_on_ip_patient_revisit', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_family_on_ip_revisit', 'Revisit IP Patient Family Information Message', 'Message is sent automatically to the IP patient''s kin when the patient revisits the hospital. The message is sent post visit registration.', NULL, NULL, NULL, 'Your relative ${patient_name} is admitted in ${center_name}. The patient id is ${mr_no}.', NULL, NULL, 'family_on_ip_patient_revisit', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_appointment_reminder', 'Patient Appointments', 'Message is manually sent to patients to inform about their booked appointments.', NULL, NULL, NULL, 'You have an appointment with doctor ${appointment_doctor} on ${appointment_time}', NULL, NULL, 'ui_trigger', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_report_ready', 'Test Report Ready', 'Message is sent automatically to the patient when the test report is signed off. This message is to inform patient that test report is ready and patient can collect the report from the hospital.', NULL, NULL, NULL, 'Reports for your diagnostic tests are ready ', NULL, NULL, 'diag_report_signoff', 'SMS', 'I', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_vaccine_reminder', 'Vaccine Reminder', 'Message is automatically sent to patient to remind him/her about the upcoming vaccine. Message is sent at 8:30 am.', NULL, NULL, NULL, 'Please schedule an appointment for ${vaccine_name} vaccination', NULL, NULL, 'vaccine_reminder', 'SMS', 'I', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_doctor_appointments', 'Doctor Appointments', 'Message is manually triggered. This message is sent to a doctor detailing out his/her schedule for the day.', NULL, NULL, NULL, 'You have ${total_appointments} for ${appointment_date}', NULL, NULL, 'ui_trigger', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_patient_on_ip_admission', 'New IP Patient Welcome Message', 'Message is sent automatically to the IP patient when the patient registers in the hospital for the first time.', NULL, NULL, NULL, 'Dear ${patient_name}, thank you for registering at ${center_name}. Your patient id is ${mr_no}.', NULL, NULL, 'patient_on_ip_patient_admission', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_family_on_op_revisit', 'Revisit OP Patient Family Information Message', 'Message is sent automatically to the OP patient''s kin when the patient revisits the hospital. The message is sent post visit registration.', NULL, NULL, NULL, 'Your relative ${patient_name} has registered as an out patient.', NULL, NULL, 'family_on_op_patient_revisit', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_next_day_appointment_reminder', 'Patient Appointment Reminder', 'Message is sent automatically one day before an appointment at 8:00 am in the morning. For example if a patient has an appointment on Wednesday, patient will receive a message on Tuesday morning reminding him about the appointment. ', NULL, NULL, NULL, 'This is Reminder of Appointment confirmation', NULL, NULL, 'next_day_appointment_reminder', 'SMS', 'I', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_advance_paid', 'Advance Payment Information Message', 'Message is automatically sent to management whenever patient pays advance against a bill.', NULL, NULL, NULL, 'Patient, ${patient_name}, has paid an advance of ${currency_symbol} ${advance_amount_paid} against bill ${bill_no} at ${center_name}.', NULL, NULL, 'advance_paid', 'SMS', 'I', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_bill_payment_received', 'Payments Message', 'Message is sent automatically to patients whenever patient makes a payment. This message is to thank patient for paying. It can also be used to remind patient of the remaining due.', NULL, NULL, NULL, 'Dear ${recipient_name}, thank you for paying ${currency_symbol} ${amount_paid} on ${payment_date} at ${center_name}. Please contact ${center_contact_phone} for any queries.', NULL, NULL, 'bill_payment_message', 'SMS', 'A', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_op_bn_cash_bill', 'OP Bill to Patients', 'Message is automatically sent to OP patients when they have paid their bill and their bill status is closed. ', NULL, '', 'Bill', 'Dear ${recipient_name},<br><br>Thank you for visiting ${center_name}. Please find your bill attached below in the mail.<br><br>Regards,<br>${center_name} <br>${center_contact_phone}', NULL, NULL, 'op_bn_cash_bill_paid_closed', 'EMAIL', 'A', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_manual_op_bn_cash_bill', 'Email Bills (Manually)', 'Message is manually sent by a user to email bill to patients. This email is triggered from billing screen.', NULL, '', 'Bill', 'Dear ${recipient_name},<br><br>Thank you for visiting ${center_name}. Please find your bill attached below in the mail.<br><br>Regards,<br>${center_name} <br>${center_contact_phone}', NULL, NULL, 'manual_op_bn_cash_bill_paid_closed', 'EMAIL', 'A', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_diag_report', 'Diagnostic Reports', 'Message is sent manually to patients when the test report is signed off.', NULL, NULL, NULL, 'Please find attached your diagnostic report ${report_name} dated ${report_date}<br/><br/>${_report_content}', NULL, NULL, 'ui_trigger', 'EMAIL', 'A', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_phr_gen_doc_delete', 'Practo Share Generic document deletion', ' Email sent to PHR when generic document is deleted ', NULL, 'drive-insta@practo.net', 'Delete generic document', '{
                         "source": "insta",
                         "event_type": "record_sync",
                         "record_type": "${record_type}",
                         "operation_type": "cancel",
                         "record_id": "${document_id?c}", 
                         "patient_demographics": {
                         "mod_time": "${mod_time}",
                         "patient_name": "${patient_full_name}",
                         "patient_phone": "${patient_phone}",
                         "gender": "${patient_gender}",
                         "date_of_birth": "${patient_dateofbirth}",
                         "email_id": "${patient_email}",
                         "age": "${patient_age?c}",
                         "age_unit":"${patient_age_unit}",
                         "mr_no": "${mr_no}"
                         },
                         "group_details": {
                              "name": "${hospital_name}",
                              "address": "${(default_center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${default_center_city}",
                              "state": "${default_center_state}",
                              "contact_no": "${default_center_phone}",
                              "id": "${group_id}"
                          }
                         }', NULL, NULL, 'gen_doc_delete', 'EMAIL', 'I', 1, 'general', 'N', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_phr_gen_doc_finalize', 'Practo Share Generic document finalization', ' Email sent to PHR when generic document is finalized ', NULL, 'drive-insta@practo.net', 'Generic document', '{ 
   "source": "insta",
   "event_type": "record_sync",
   "record_type": "doc_${record_type}",
   "operation_type": "upsert", 
   "record_id": "${doc_id?c}",    
   "content_type":"${content_type}",
   "report_name": "${doc_name}", 
   "report_date": "${doc_date}",  
   "visit_id": "${visit_id}",  		
   "visit_date":"${visit_date}", 
   "patient_demographics": { 
                              "mod_time": "${mod_time}", 
                              "patient_name": "${patient_full_name}", 
                              "patient_phone": "${patient_phone}", 
                              "gender": "${patient_gender}", 
                              "date_of_birth": "${patient_dateofbirth}", 
                              "email_id": "${patient_email}", 
                              "age":"${patient_age?c}", 
                              "age_unit":"${patient_age_unit}", 
                              "mr_no": "${mr_no}" 
                             }, 
   "visit_center_details": { 
                              "name": "${center_name}", 
                              "address": "${(center_address?html)?replace("\r\n","\\r\\n")}", 
                              "city": "${center_city}", 
                              "state": "${center_state}", 
                              "contact_no": "${center_phone}", 
                              "id": "<#if center_id?? && center_id?has_content>${center_id?c}</#if>" 
                           }, 
   "group_details": {  
                              "name": "${hospital_name}", 
                              "address": "${(default_center_address?html)?replace("\r\n","\\r\\n")}", 
                              "city": "${default_center_city}", 
                              "state": "${default_center_state}", 
                              "contact_no": "${default_center_phone}", 
                              "id": "${group_id}" 
                    }, 
   "prescribing_doctor_details" : { 
                              "id":"${presc_doc_id}", 
                              "name":"${presc_doc_name}", 
                              "dept":"${specialization}" 
                                  }, 
   "referral_details": { 
                              "id":"${ref_id}", 
                              "name":"${ref_name}" 
                       } 
}', NULL, NULL, 'gen_doc_finalize', 'EMAIL', 'I', 1, 'general', 'N', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_phr_diag_report', 'OP Practo Share Diagnostic Report', 'Diagnostic report is automatically shared with OP patient via Practo Drive when the test report is signed off.', NULL, 'drive-insta@practo.net', 'Diagnostic Report', '{
                          "source": "insta",
                          "event_type": "record_sync",
                          "record_type": "lab_report",
                          "operation_type": "upsert",
                          "record_id": "${key?c}",
                          "report_name": "${diag_report_name}",
                          "report_date": "${report_date_yyyy_mm_dd}",
                          "visit_id": "${user_name}",
                          "visit_date":"${visit_date}",
                          "patient_demographics": {
                              "mod_time": "${mod_time}",
                              "patient_name": "${patient_full_name}",
                              "patient_phone": "${recipient_mobile}",
                              "gender": "${recipient_gender}",
                              "date_of_birth": "${recipient_dateofbirth}",
                              "email_id": "${patient_email}",
                              "age":"${recipient_age?c}",
                              "age_unit":"${recipient_age_unit}",
                              "mr_no": "${mr_no}"
                          },
                          "visit_center_details": {
                              "name": "${center_name}",
                              "address": "${(center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${center_city}",
                              "state": "${center_state}",
                              "contact_no": "${center_phone}",
                              "id": "${center_id?c}"
                          },
                          "group_details": {
                              "name": "${hospital_name}",
                              "address": "${(default_center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${default_center_city}",
                              "state": "${default_center_state}",
                              "contact_no": "${default_center_phone}",
                              "id": "${group_id}"
                          },
                          "prescribing_doctor_details" : {
                              "id":"${doctor_id}",
                              "name":"${doctor_name}",
                              "dept":"${specialization}"
                           },
                          "referral_details": {
                              "id":"${ref_id}",
                              "name":"${ref_name}"
                           }
                           }', NULL, NULL, 'op_phr_diag_share', 'EMAIL', 'I', 1, 'general', 'N', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_phr_ip_bill_paid', 'Practo Share IP Bill', 'Bill is automatically shared with patient via Practo Drive when the IP bill is paid ', NULL, 'drive-insta@practo.net', 'Bill', '{
   "source": "insta",
   "event_type": "record_sync",
   "record_type": "Bill",
   "operation_type": "upsert", 
   "record_id": "${bill_no}",    
   "report_name": "", 
   "report_date": "${bill_date}",
   "visit_type":"${visit_type}",
   "visit_id": "${visit_id}",          
   "visit_date":"${visit_date}", 
   "patient_demographics": { 
                              "mod_time": "${mod_time}", 
                              "patient_name": "${patient_full_name}", 
                              "patient_phone": "${patient_phone}", 
                              "gender": "${patient_gender}", 
                              "date_of_birth": "${patient_dateofbirth}", 
                              "email_id": "${patient_email}", 
                              "age":"${patient_age?c}", 
                              "age_unit":"${patient_age_unit}", 
                              "mr_no": "${mr_no}" 
                             }, 
   "visit_center_details": { 
                              "name": "${center_name}", 
                              "address": "${(center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${center_city}", 
                              "state": "${center_state}", 
                              "contact_no": "${center_phone}", 
                              "id": "<#if center_id?? && center_id?has_content>${center_id?c}</#if>" 
                           }, 
   "group_details": {  
                              "name": "${hospital_name}", 
                              "address": "${(default_center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${default_center_city}", 
                              "state": "${default_center_state}", 
                              "contact_no": "${default_center_phone}", 
                              "id": "${group_id}" 
                    }, 
   "prescribing_doctor_details" : { 
                              "id":"${presc_doc_id}", 
                              "name":"${presc_doc_name}", 
                              "dept":"${specialization}" 
                                  }, 
   "referral_details": { 
                              "id":"${ref_id}", 
                              "name":"${ref_name}" 
                       } 
}', NULL, NULL, 'ip_bill_paid', 'EMAIL', 'I', 1, 'general', 'N', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_phr_op_bill_paid', 'Practo Share OP Bill', 'Bill is automatically shared with patient via Practo Drive when the OP bill is paid ', NULL, 'drive-insta@practo.net', 'Bill', '{
 "source": "insta",
   "event_type": "record_sync",
   "record_type": "Bill",
   "operation_type": "upsert", 
   "record_id": "${bill_no}",    
   "report_name": "", 
   "report_date": "${bill_date}",
   "visit_type":"${visit_type}",
   "visit_id": "${visit_id}",          
   "visit_date":"${visit_date}", 
   "patient_demographics": { 
                              "mod_time": "${mod_time}", 
                              "patient_name": "${patient_full_name}", 
                              "patient_phone": "${patient_phone}", 
                              "gender": "${patient_gender}", 
                              "date_of_birth": "${patient_dateofbirth}", 
                              "email_id": "${patient_email}", 
                              "age":"${patient_age?c}", 
                              "age_unit":"${patient_age_unit}", 
                              "mr_no": "${mr_no}" 
                             }, 
   "visit_center_details": { 
                              "name": "${center_name}", 
                              "address": "${(center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${center_city}", 
                              "state": "${center_state}", 
                              "contact_no": "${center_phone}", 
                              "id": "<#if center_id?? && center_id?has_content>${center_id?c}</#if>" 
                           }, 
   "group_details": {  
                              "name": "${hospital_name}", 
                              "address": "${(default_center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${default_center_city}", 
                              "state": "${default_center_state}", 
                              "contact_no": "${default_center_phone}", 
                              "id": "${group_id}" 
                    }, 
   "prescribing_doctor_details" : { 
                              "id":"${presc_doc_id}", 
                              "name":"${presc_doc_name}", 
                              "dept":"${specialization}" 
                                  }, 
   "referral_details": { 
                              "id":"${ref_id}", 
                              "name":"${ref_name}" 
                       } 
}', NULL, NULL, 'op_bill_paid', 'EMAIL', 'I', 1, 'general', 'N', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_phr_diag_report_cancel', 'Practo Share Diagnostic Report Cancellation', 'Diagnostic report is automatically removed from Practo Drive when the report is revert signed-off by the doctor.', NULL, 'drive-insta@practo.net', 'Cancel Diagnostic Report', '{
                         "source": "insta",
                        "event_type": "record_sync",
                         "record_type": "lab_report",
                         "operation_type": "cancel",
                         "record_id": "${key?c}",
                         "report_date": "${cancellation_date}", 
					"visit_id": "${user_name}",
					"visit_date":"${visit_date}", 
                         "patient_demographics": {
                         "mod_time": "${mod_time}",
                         "patient_name": "${patient_full_name}",
                         "patient_phone": "${recipient_mobile}",
                         "gender": "${recipient_gender}",
                         "date_of_birth": "${recipient_dateofbirth}",
                         "email_id": "${patient_email}",
                         "age": "${recipient_age?c}",
                         "age_unit":"${recipient_age_unit}",
                         "mr_no": "${mr_no}"
                         },
						"visit_center_details": {
                              "name": "${center_name}",
                              "address": "${(center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${center_city}",
                              "state": "${center_state}",
                              "contact_no": "${center_phone}",
                              "id": "${center_id?c}"
                          },
                         "group_details": {
                              "name": "${hospital_name}",
                              "address": "${(default_center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${default_center_city}",
                              "state": "${default_center_state}",
                              "contact_no": "${default_center_phone}",
                              "id": "${group_id}"
                          }
                         }', NULL, NULL, 'diag_report_revert', 'EMAIL', 'I', 1, 'general', 'N', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_daily_collection', 'Daily Collection Information Message', 'Message is automatically sent to management every day detailing yesterday''s collection in hospital. This message will be triggered every day at a particular time chosen by the Hospital.', NULL, NULL, NULL, '${hospital_name} collection on ${yesteday_date} is as follow: 
Total collection: ${currency_symbol} ${total_collection} 
${detailed_summary}', NULL, NULL, 'daily_collection', 'SMS', 'I', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_discount_given', 'Discount Information Message', 'Message is automatically sent to management every time discount is given to a patient. Message is only sent when the bill status is either finalized or closed and patient due is zero.', NULL, NULL, NULL, 'Dear Sir / Madam, patient  ${patient_name} with bill no ${bill_no} had been given ${currency_symbol} ${total_discount} discount on ${date_time} ${authorizer_name}.', NULL, NULL, 'discount_given', 'SMS', 'I', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_to_doctor_on_pat_discharge', 'Patient Physical Discharge', 'This message is used to send notifications to the discharging doctor and referral doctor  when Physical Discharge is  done.', NULL, NULL, NULL, '${patient_name} MRNo ${receipient_id__}/Visit ID ${visit_id} has been discharged at ${discharge_time} on ${discharge_date}', NULL, NULL, 'patient_physical_discharge', 'SMS', 'A', NULL, 'general', 'A', 'Download Practo App');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_ip_phr_diag_report', 'IP Practo Share Diagnostic Report', 'Diagnostic report is automatically shared with IP patient via Practo Drive when the test report is signed off and patient is discharged.', NULL, 'drive-insta@practo.net', 'Diagnostic Report for IP', '{
                          "source": "insta",
                          "event_type": "record_sync",
                          "record_type": "lab_report",
                          "operation_type": "upsert",
                          "record_id": "${key?c}",
                          "report_name": "${diag_report_name}",
                          "report_date": "${report_date_yyyy_mm_dd}",
                          "visit_id": "${user_name}",
                          "visit_date":"${visit_date}",
                          "patient_demographics": {
                              "mod_time": "${mod_time}",
                              "patient_name": "${patient_full_name}",
                              "patient_phone": "${recipient_mobile}",
                              "gender": "${recipient_gender}",
                              "date_of_birth": "${recipient_dateofbirth}",
                              "email_id": "${patient_email}",
                              "age":"${recipient_age?c}",
                              "age_unit":"${recipient_age_unit}",
                              "mr_no": "${mr_no}"
                          },
                          "visit_center_details": {
                              "name": "${center_name}",
                              "address": "${(center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${center_city}",
                              "state": "${center_state}",
                              "contact_no": "${center_phone}",
                              "id": "${center_id?c}"
                          },
                          "group_details": {
                              "name": "${hospital_name}",
                              "address": "${(default_center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${default_center_city}",
                              "state": "${default_center_state}",
                              "contact_no": "${default_center_phone}",
                              "id": "${group_id}"
                          },
                          "prescribing_doctor_details" : {
                              "id":"${doctor_id}",
                              "name":"${doctor_name}",
                              "dept":"${specialization}"
                           },
                          "referral_details": {
                              "id":"${ref_id}",
                              "name":"${ref_name}"
                           }
                           }', NULL, NULL, 'ip_phr_diag_share', 'EMAIL', 'I', 1, 'general', 'N', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_patient_ward_bed_shift', 'Ward shift message to Patient', 'Message is automatically sent to Patient when an IP patient is shifted from one ward/bed to another.', NULL, '${patient_mobile}', '', 'Dear ${patient_name}, you have been shifted from ${old_ward}, ${old_bed} to ${new_ward}, ${new_bed}. Please call ${center_phone} for any queries.', NULL, NULL, 'ward_bed_shift', 'SMS', 'A', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_next_of_kin_ward_bed_shift', 'Ward shift message to Next of Kin', 'Message is automatically sent to next of kin when an IP patient is shifted from one ward/bed to another. ', NULL, '${next_of_kin_phone}', '', 'Your relative, ${patient_name}, is shifted from ${old_ward}, ${old_bed} to ${new_ward}, ${new_bed}. Please call ${center_phone} for any queries.', NULL, NULL, 'ward_bed_shift', 'SMS', 'A', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_doctor_ward_bed_shift', 'Ward shift message to Doctor', 'Message is automatically sent to admitting doctor when an IP patient is shifted from one ward/bed to another.', NULL, '${doctor_mobile}', '', 'Dear ${doctor_name}, your patient, ${patient_name}, is shifted from ${old_ward}, ${old_bed} to ${new_ward}, ${new_bed}. Please call ${center_phone} for any queries.', NULL, NULL, 'ward_bed_shift', 'SMS', 'A', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_appointment_confirmation_for_doctor', 'Appointment Confirmation to Doctor', 'Message is sent automatically to the doctor when an appointment is confirmed.', NULL, NULL, NULL, 'Dear doctor, ${patient_name} has booked an appointment with you on date ${appointment_date} and time ${appointment_time_12hr} at ${center_name}, ${center_address}. Please call hospital ${center_phone} for any queries.', NULL, NULL, 'appointment_confirmed', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_patient_on_discharge', 'Patient Discharge', 'This message is used to send notifications to the patient based on various discharge statuses such as when Discharge is Initiated,
Financial Discharge done with billing dues if any or  a marketing message on Discharge Complete etc', NULL, NULL, NULL, '[#if discharge_status = ''I'']
Dear ${patient_name}, ${discharge_state} has been initiated..You are going to be discharged at ${expected_discharge_date} at ${expected_discharge_time}.
[#elseif discharge_status = ''F'']
Dear ${patient_name}, ${discharge_state} has been initiated
Your Patient Due Amount is : ${patient_due}
Your Discharge Date and Time is: ${financial_discharge_date!}
  at ${financial_discharge_time!}
[#else]
Thank you and hope you had a pleasant stay
[/#if]', NULL, NULL, 'patient_on_discharge', 'SMS', 'A', NULL, 'general', 'A', 'Download Practo App');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_nok_on_patient_discharge', 'Patient Discharge Family Information Message', 'This message is used to send notifications to the patient''family based on various discharge statuses such as when Discharge is Initiated,
Financial Discharge done with billing dues if any or  a marketing message on Discharge Complete etc', NULL, NULL, NULL, '[#if discharge_status = ''I'']
Your relative ${patient_name} has been initiated for ${discharge_state}
Your relative  is going to be discharged at ${expected_discharge_date} at ${expected_discharge_time}.
[#elseif discharge_status = ''F'']
Patient Due Amount is : ${patient_due}
Patient Total Amount is : ${billed_amount}
Patient''s Discharge Date and Time is: ${financial_discharge_date!}
  at ${financial_discharge_time!}
[#else]
 Thank you for visiting
[/#if]', NULL, NULL, 'inform_nok_on_patient_discharge', 'SMS', 'A', NULL, 'general', 'A', 'Download Practo App');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_phr_pharmacy_bill_paid', 'Practo Share Pharmacy Bill', ' Bill is automatically shared with patient via Practo Drive when pharmacy bill is paid ', NULL, 'drive-insta@practo.net', 'Bill', '{ 
   "source": "insta",
   "event_type": "record_sync",
   "record_type": "Bill",
   "operation_type": "upsert", 
   "record_id": "${bill_no}",    
   "report_name": "", 
   "report_date": "${bill_date}",
   "visit_type":"${visit_type}",
   "visit_id": "${visit_id}",          
   "visit_date":"${visit_date}", 
   "patient_demographics": { 
                              "mod_time": "${mod_time}", 
                              "patient_name": "${patient_full_name}", 
                              "patient_phone": "${patient_phone}", 
                              "gender": "${patient_gender}", 
                              "date_of_birth": "${patient_dateofbirth}", 
                              "email_id": "${patient_email}", 
                              "age":"${patient_age?c}", 
                              "age_unit":"${patient_age_unit}", 
                              "mr_no": "${mr_no}" 
                             }, 
   "visit_center_details": { 
                              "name": "${center_name}", 
                              "address": "${(center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${center_city}", 
                              "state": "${center_state}", 
                              "contact_no": "${center_phone}", 
                              "id": "<#if center_id?? && center_id?has_content>${center_id?c}</#if>" 
                           }, 
   "group_details": {  
                              "name": "${hospital_name}", 
                              "address": "${(default_center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${default_center_city}", 
                              "state": "${default_center_state}", 
                              "contact_no": "${default_center_phone}", 
                              "id": "${group_id}" 
                    }, 
   "prescribing_doctor_details" : { 
                              "id":"${presc_doc_id}", 
                              "name":"${presc_doc_name}", 
                              "dept":"${specialization}" 
                                  }, 
   "referral_details": { 
                              "id":"${ref_id}", 
                              "name":"${ref_name}" 
                       } 
}', NULL, NULL, 'pharmacy_bill_paid', 'EMAIL', 'I', 1, 'general', 'N', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_dynamic_appointment_reminder', 'Dynamic Appointment Reminder', 'Message is automatically sent to patient reminding him/her about the upcoming appointment. This message will be sent ''x'' hours before the appointment. The ''x'' can be configured from the message configuration screen.', NULL, NULL, NULL, 'Dear ${recipient_name}, this is to remind you that you have an appointment with ${appointment_doctor} at ${center_name} on ${appointment_date} at ${appointment_time_12hr}. Please call ${center_phone} for any query.', NULL, NULL, 'dynamic_appointment_reminder', 'SMS', 'A', NULL, 'general', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('email_phr_prescription', 'Practo Share Prescription', ' Prescription is automatically shared with patient via Practo Drive when its saved', NULL, 'drive-insta@practo.net', 'Prescription', '{ 
   "source": "insta",
   "event_type": "record_sync",
   "record_type": "Prescription",
   "operation_type": "upsert", 
   "record_id": "${consultation_id?c}",    
   "report_name": "${report_name}", 
   "report_date": "${prescription_date}",
   "visit_id": "${visit_id}",          
   "visit_date":"${visit_date}", 
   "patient_demographics": { 
                              "mod_time": "${mod_time}", 
                              "patient_name": "${patient_full_name}", 
                              "patient_phone": "${patient_phone}", 
                              "gender": "${patient_gender}", 
                              "date_of_birth": "${patient_dateofbirth}", 
                              "email_id": "${patient_email}", 
                              "age":"${patient_age?c}", 
                              "age_unit":"${patient_age_unit}", 
                              "mr_no": "${mr_no}" 
                             }, 
   "visit_center_details": { 
                              "name": "${center_name}", 
                              "address": "${(center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${center_city}", 
                              "state": "${center_state}", 
                              "contact_no": "${center_phone}", 
                              "id": "<#if center_id?? && center_id?has_content>${center_id?c}</#if>" 
                           }, 
   "group_details": {  
                              "name": "${hospital_name}", 
                              "address": "${(default_center_address?html)?replace("\r\n","\\r\\n")}",
                              "city": "${default_center_city}", 
                              "state": "${default_center_state}", 
                              "contact_no": "${default_center_phone}", 
                              "id": "${group_id}" 
                    }, 
   "prescribing_doctor_details" : { 
                              "id":"${presc_doc_id}", 
                              "name":"${presc_doc_name}", 
                              "dept":"${specialization}" 
                                  }, 
   "referral_details": { 
                              "id":"${ref_id}", 
                              "name":"${ref_name}" 
                       } 
}', NULL, NULL, 'prescription_saved', 'EMAIL', 'I', 1, 'general', 'N', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('message_for_lab_critical_val', 'Critical Lab Values', 'Message is sent automatically to the Prescribing/Consulting/Admitting/Referral Doctor on Sign off when test values falls under critical range.', NULL, NULL, NULL, '${patient_name},  ${receipient_id__},
${age},  ${gender}
has  ${test_name} with 
[#if result_name?has_content]
[#list result_name as resultlabel]
	${result_name[resultlabel_index]} : ${report_value[resultlabel_index]} ${units[resultlabel_index]}
	[#if result_name?size-1 != resultlabel_index] , [/#if]
[/#list]
[/#if]
in the Critical Range and requires your attention.', NULL, NULL, 'lab_critical_val', 'SMS', 'A', NULL, 'general', 'A', 'Download Practo App');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_appointment_booked', 'Appointment Booked', 'Message is sent automatically to the patient when an appointment is booked with status as booked.', NULL, NULL, NULL, 'Dear ${receipient_name}, Your appointment <#if appointment_doctor?? && appointment_doctor?has_content>with ${appointment_doctor}</#if> at ${center_name} scheduled for ${appointment_date} at ${appointment_time} has been tentatively booked. Please call hospital for any query.', NULL, NULL, 'appointment_booked', 'SMS', 'A', NULL, 'scheduler', 'A', '');
INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer) VALUES ('sms_patient_due_for_visit', 'Patient Due Message', 'Message is manually sent by a user from billing screen to IP patients and next of kin informing them about the balance due in the active visit. This helps in collections as patients are always informed about their dues.', NULL, '${patient_phone},${next_of_kin_phone}', NULL, 'This is to inform that ${patient_name} with patient id ${mr_no} has ${currency_symbol} ${net_patient_due} due against total bill amount of ${currency_symbol} ${total_bill_amount}. Please contact the billing desk for any other queries.', NULL, NULL, 'patient_due_for_visit', 'SMS', 'A', NULL, 'general', 'A', '');
--
-- Data for Name: message_attachments; Type: TABLE DATA; 
--

--
-- Data for Name: message_config; Type: TABLE DATA; 
--

INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'sms_followup_reminder', 'overdue_period', '7', 'Followups OVERDUE for the specified no of days will be available for selection');
INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'sms_followup_reminder', 'due_period', '7', 'Followups DUE in the specified no. of days will be available for selection');
INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'sms_doctor_appointments', 'due_period', '1', 'Doctors with appointments in the specified no. of days will be available for selection');
INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'sms_appointment_reminder', 'due_period', '1', 'Patients with appointments in the specified no. of days will be available for selection');
INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'email_diag_report', 'ready_period', '7', 'Reports that were signed off in the specified no. of days will be available for selection');
INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'sms_appointment_confirmation', 'due_period', '3', 'Patients with confirmed appointments in the specified no. of days will be available for selection');
INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'sms_appointment_confirmation', 'status', 'Confirmed', 'status for confirmed appointments');
INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'sms_appointment_reminder', 'status', 'Booked', 'status for booked appointments');
INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'sms_vaccine_reminder', 'due_period', '1', 'Vaccines OVERDUE in the specified no of days will be available for selection');
INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'email_diag_report', 'check_patient_due', 'Y', 'Check for patient due for OP incase of auto triggered message');
INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'sms_dynamic_appointment_reminder', 'buffer_hours', '4', 'Appointment Reminder before this hours from appointment will be sent.');

--
-- Data for Name: message_dispatcher_config; Type: TABLE DATA; 
--

INSERT INTO message_dispatcher_config (message_mode, display_name, protocol, host_name, port_no, auth_required, use_tls, username, password, attachment_allowed, max_attachment_kb, custom_param_1, custom_param_2, status, country_code_prefix) VALUES ('EMAIL', 'Email', 'smtp', '10.11.0.1', 25, false, false, NULL, NULL, 'Y', 1024, NULL, NULL, 'A', NULL);
INSERT INTO message_dispatcher_config (message_mode, display_name, protocol, host_name, port_no, auth_required, use_tls, username, password, attachment_allowed, max_attachment_kb, custom_param_1, custom_param_2, status, country_code_prefix) VALUES ('NOTIFICATION', 'Notification', '', '', 0, false, false, '', '', 'N', 0, '', '', 'A', NULL);
INSERT INTO message_dispatcher_config (message_mode, display_name, protocol, host_name, port_no, auth_required, use_tls, username, password, attachment_allowed, max_attachment_kb, custom_param_1, custom_param_2, status, country_code_prefix) VALUES ('SMS', 'Mobile Text Message', 'smtp', '10.11.0.1', 25, false, false, NULL, NULL, 'N', 1024, '.instahms@smscountry.net', NULL, 'A', 'WP');

--
-- Data for Name: message_log; Type: TABLE DATA; 
--

--
-- Data for Name: message_log_attachments; Type: TABLE DATA; 
--

--
-- Data for Name: message_recipient; Type: TABLE DATA; 
--

--
-- Data for Name: message_type_actions; Type: TABLE DATA; 
--

INSERT INTO message_type_actions (message_type_id, message_action_mask, sender_override_mask) VALUES ('notification_bill_cancellation', 6, 7);

--
-- Data for Name: micro_abst_antibiotic_master; Type: TABLE DATA; 
--

--
-- Data for Name: micro_abst_panel_master; Type: TABLE DATA; 
--

--
-- Data for Name: micro_org_group_master; Type: TABLE DATA; 
--

--
-- Data for Name: micro_abst_orggr; Type: TABLE DATA; 
--

--
-- Data for Name: micro_antibiotic_master; Type: TABLE DATA; 
--

--
-- Data for Name: micro_growth_template_master; Type: TABLE DATA; 
--

--
-- Data for Name: micro_nogrowth_template_master; Type: TABLE DATA; 
--

--
-- Data for Name: micro_organism_master; Type: TABLE DATA; 
--

--
-- Data for Name: misc_payees; Type: TABLE DATA; 
--

--
-- Data for Name: modules_activated; Type: TABLE DATA; 
--

INSERT INTO modules_activated (module_id, activation_status) VALUES ('mod_basic', 'Y');
INSERT INTO modules_activated (module_id, activation_status) VALUES ('mod_services', 'Y');
INSERT INTO modules_activated (module_id, activation_status) VALUES ('mod_registration', 'Y');
INSERT INTO modules_activated (module_id, activation_status) VALUES ('mod_prescribe', 'Y');
INSERT INTO modules_activated (module_id, activation_status) VALUES ('mod_billing', 'Y');
INSERT INTO modules_activated (module_id, activation_status) VALUES ('mod_discharge', 'Y');

--
-- Data for Name: mrd_casefile_attributes; Type: TABLE DATA; 
--

--
-- Data for Name: mrd_casefile_indent; Type: TABLE DATA; 
--

--
-- Data for Name: mrd_casefile_issuelog; Type: TABLE DATA; 
--

--
-- Data for Name: mrd_casefile_users; Type: TABLE DATA; 
--

--
-- Data for Name: mrd_code_claim_groups; Type: TABLE DATA; 
--

INSERT INTO mrd_code_claim_groups (mrd_code_id, code_group) VALUES (26, 'LG');
INSERT INTO mrd_code_claim_groups (mrd_code_id, code_group) VALUES (27, 'LG');

--
-- Data for Name: mrd_codes_doctor_master; Type: TABLE DATA; 
--

--
-- Data for Name: mrd_codes_master; Type: TABLE DATA; 
--

INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter End', '1', 'Discharged with approval', 'A', 1);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter End', '2', 'Discharged against advice', 'A', 2);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter End', '3', 'Discharged absent without leave', 'A', 3);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter End', '4', 'Transfer to another facility', 'A', 4);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter End', '5', 'Deceased', 'A', 5);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Start', '1', 'Elective', 'A', 6);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Start', '2', 'Emergency', 'A', 7);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Start', '3', 'Transfer', 'A', 8);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Start', '4', 'Live birth', 'A', 9);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Start', '5', 'Still birth', 'A', 10);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Start', '6', 'Dead On Arrival', 'A', 11);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '1', 'No Bed + No emergency room', 'A', 12);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '12', 'Home', 'A', 13);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '13', 'Assisted Living Facility', 'A', 14);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '15', 'Mobile Unit', 'A', 15);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '2', 'No Bed + Emergency room', 'A', 16);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '3', 'Inpatient Bed + No emergency room', 'A', 17);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '4', 'Inpatient Bed + Emergency room', 'A', 18);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '41', 'Ambulance - Land', 'A', 19);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '42', 'Ambulance - Air or Water', 'A', 20);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '5', 'Daycase Bed + No emergency room', 'A', 21);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '6', 'Daycase Bed + Emergency room', 'A', 22);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '7', 'Nationals Screening', 'A', 23);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '8', 'New Visa Screening', 'A', 24);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Encounter Type', '9', 'Renewal Visa Screening', 'A', 25);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Service Code', '99', 'DRG Outlier Payment', 'A', 26);
INSERT INTO mrd_codes_master (code_type, code, code_desc, status, mrd_code_id) VALUES ('Service Code', '98', 'DRG Add On Payment', 'A', 27);

--
-- Data for Name: mrd_diagnosis; Type: TABLE DATA; 
--

--
-- Data for Name: mrd_diagnosis_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: mrd_icdcodes_cm; Type: TABLE DATA; 
--

--
-- Data for Name: mrd_observations; Type: TABLE DATA; 
--

--
-- Data for Name: mrd_supported_code_categories; Type: TABLE DATA; 
--

INSERT INTO mrd_supported_code_categories (code_category, status) VALUES ('Diagnosis', 'A');
INSERT INTO mrd_supported_code_categories (code_category, status) VALUES ('Encounter', 'A');
INSERT INTO mrd_supported_code_categories (code_category, status) VALUES ('Treatment', 'A');
INSERT INTO mrd_supported_code_categories (code_category, status) VALUES ('Drug', 'A');
INSERT INTO mrd_supported_code_categories (code_category, status) VALUES ('Observations', 'A');
INSERT INTO mrd_supported_code_categories (code_category, status) VALUES ('Consultations', 'A');

--
-- Data for Name: mrd_supported_code_types; Type: TABLE DATA; 
--

INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (3, 'CPT', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (4, 'HCPCS', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (6, 'Dental', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (8, 'Service Code', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (10, 'ICD', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (11, 'LOINC', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (12, 'HL7v3 Native', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (13, 'SNOMED CT', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (14, 'Text', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (15, 'File', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (16, 'Universal Dental', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (19, 'Encounter Start', 'A', 'encounter_start_types');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (20, 'Encounter End', 'A', 'encounter_end_types');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (18, 'Encounter Type', 'A', 'encounter_type_codes');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (3, 'E&M', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (9, 'IR-DRG', 'A', 'drg_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (101, 'ICD9', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (102, 'ICD10', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (6, 'Service', 'A', 'mrd_codes_master');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (5, 'Drug HAAD', 'A', 'store_item_codes');
INSERT INTO mrd_supported_code_types (haad_code, code_type, status, code_master_table) VALUES (5, 'Drug', 'A', 'store_item_codes');

--
-- Data for Name: mrd_supported_codes; Type: TABLE DATA; 
--

INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Diagnosis', 'ICD', '', 1);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Treatment', 'CPT', '', 2);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Treatment', 'HCPCS', '', 3);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Treatment', 'Dental', '', 4);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Treatment', 'Service Code', '', 5);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Treatment', 'IR-DRG', '', 6);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Drug', 'Drug', '', 7);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Observations', 'LOINC', '', 8);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Observations', 'Universal Dental', '', 9);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Observations', 'HL7v3 Native', '', 10);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Observations', 'SNOMED CT', '', 11);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Encounter', 'Encounter Type', '', 12);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Encounter', 'Encounter Start', '', 13);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Encounter', 'Encounter End', '', 14);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Consultations', 'E&M', '', 15);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Treatment', 'Drug', '', 16);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Drug', 'Drug HAAD', '', 17);
INSERT INTO mrd_supported_codes (code_category, code_type, code_type_classification, id) VALUES ('Treatment', 'Drug HAAD', '', 18);

--
-- Data for Name: national_sponsor_docs_details; Type: TABLE DATA; 
--

--
-- Data for Name: network_center_details; Type: TABLE DATA; 
--

--
-- Data for Name: network_sync_status; Type: TABLE DATA; 
--

INSERT INTO network_sync_status (last_successful_full_sync_time, last_sync_attempt_time, last_sync_attempt_type, last_sync_attempt_status, last_sync_attempt_msg) VALUES (NULL, NULL, NULL, NULL, NULL);

--
-- Data for Name: nurse_notes_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: nurse_ward_assignments; Type: TABLE DATA; 
--

--
-- Data for Name: obsolete_dyna_package_master_charges; Type: TABLE DATA; 
--

--
-- Data for Name: obsolete_dyna_package_master_charges_backup; Type: TABLE DATA; 
--

--
-- Data for Name: obsolete_insurance_claim_resubmission; Type: TABLE DATA; 
--

--
-- Data for Name: obsolete_ip_record; Type: TABLE DATA; 
--

--
-- Data for Name: obsolete_medication_order_details; Type: TABLE DATA; 
--

--
-- Data for Name: obsolete_medication_order_main; Type: TABLE DATA; 
--

--
-- Data for Name: obsolete_patient_doctor_instructions; Type: TABLE DATA; 
--

--
-- Data for Name: obsolete_test_report_images; Type: TABLE DATA; 
--

--
-- Data for Name: obsolete_vitals_schedule; Type: TABLE DATA; 
--

--
-- Data for Name: obstetric_headrecords; Type: TABLE DATA; 
--

--
-- Data for Name: oh_sample_registration; Type: TABLE DATA; 
--

--
-- Data for Name: ohmaster_detail; Type: TABLE DATA; 
--

--
-- Data for Name: op_type_names; Type: TABLE DATA; 
--

INSERT INTO op_type_names (op_type, op_type_name) VALUES ('M', 'Main Visit');
INSERT INTO op_type_names (op_type, op_type_name) VALUES ('F', 'Follow Up (With Consultation)');
INSERT INTO op_type_names (op_type, op_type_name) VALUES ('D', 'Follow Up (No Consultation)');
INSERT INTO op_type_names (op_type, op_type_name) VALUES ('R', 'Revisit');
INSERT INTO op_type_names (op_type, op_type_name) VALUES ('O', 'Outside Patient');

--
-- Data for Name: operation_anaesthesia_details; Type: TABLE DATA; 
--

--
-- Data for Name: operation_billable_resources; Type: TABLE DATA; 
--

--
-- Data for Name: operation_master; Type: TABLE DATA; 
--

--
-- Data for Name: operation_charges; Type: TABLE DATA; 
--

--
-- Data for Name: operation_charges_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: operation_charges_backup; Type: TABLE DATA; 
--

--
-- Data for Name: operation_details; Type: TABLE DATA; 
--

--
-- Data for Name: operation_doc_templates_master; Type: TABLE DATA; 
--

--
-- Data for Name: operation_documents; Type: TABLE DATA; 
--

--
-- Data for Name: operation_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: operation_master_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: operation_org_details; Type: TABLE DATA; 
--

--
-- Data for Name: operation_procedures; Type: TABLE DATA; 
--

--
-- Data for Name: operation_report; Type: TABLE DATA; 
--

--
-- Data for Name: operation_reports; Type: TABLE DATA; 
--

--
-- Data for Name: operation_schedule; Type: TABLE DATA; 
--

--
-- Data for Name: operation_team; Type: TABLE DATA; 
--

--
-- Data for Name: patient_registration; Type: TABLE DATA; 
--

--
-- Data for Name: opthal_doctor_exam_main; Type: TABLE DATA; 
--

--
-- Data for Name: opthal_doctor_fundus_exam; Type: TABLE DATA; 
--

--
-- Data for Name: opthal_doctor_lens_exam; Type: TABLE DATA; 
--

--
-- Data for Name: opthal_doctor_overall_exam; Type: TABLE DATA; 
--

--
-- Data for Name: opthal_test_attributes; Type: TABLE DATA; 
--

INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (1, 1, 1, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (2, 1, 2, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (3, 1, 3, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (4, 1, 4, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (5, 2, 1, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (6, 2, 2, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (7, 2, 3, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (8, 2, 4, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (9, 3, 9, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (10, 3, 10, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (11, 4, 8, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (12, 4, 7, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (13, 4, 11, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (14, 4, 12, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (15, 4, 13, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (16, 4, 14, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (17, 5, 8, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (18, 5, 7, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (19, 5, 11, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (20, 5, 12, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (21, 5, 13, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (22, 5, 14, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (23, 6, 15, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (24, 6, 16, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (25, 6, 17, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (26, 6, 18, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (27, 7, 8, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (28, 7, 7, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (29, 7, 11, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (30, 7, 12, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (31, 7, 13, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (32, 7, 14, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (33, 8, 8, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (34, 8, 7, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (35, 8, 11, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (36, 8, 12, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (37, 8, 13, NULL);
INSERT INTO opthal_test_attributes (test_attrib_id, test_id, attribute_id, display_order) VALUES (38, 8, 14, NULL);

--
-- Data for Name: opthal_test_main; Type: TABLE DATA; 
--

--
-- Data for Name: opthal_test_details; Type: TABLE DATA; 
--

--
-- Data for Name: order_kit_details; Type: TABLE DATA; 
--

--
-- Data for Name: order_kit_main; Type: TABLE DATA; 
--

--
-- Data for Name: ot_activities; Type: TABLE DATA; 
--

--
-- Data for Name: ot_consumable_usage; Type: TABLE DATA; 
--

--
-- Data for Name: ot_consumables; Type: TABLE DATA; 
--

--
-- Data for Name: other_charge_master; Type: TABLE DATA; 
--

--
-- Data for Name: other_services_prescribed; Type: TABLE DATA; 
--

--
-- Data for Name: outhouse_master; Type: TABLE DATA; 
--

--
-- Data for Name: outpatient_docs; Type: TABLE DATA; 
--

--
-- Data for Name: outsource_sample_details; Type: TABLE DATA; 
--

--
-- Data for Name: pack_doc_master; Type: TABLE DATA; 
--

--
-- Data for Name: pack_master; Type: TABLE DATA; 
--

--
-- Data for Name: pack_org_details; Type: TABLE DATA; 
--

--
-- Data for Name: package_category_master; Type: TABLE DATA; 
--

INSERT INTO package_category_master (package_category_id, package_category, status) VALUES (-1, 'General', 'A');

--
-- Data for Name: package_center_master; Type: TABLE DATA; 
--

--
-- Data for Name: package_charges; Type: TABLE DATA; 
--

--
-- Data for Name: package_componentdetail; Type: TABLE DATA; 
--

--
-- Data for Name: package_contents; Type: TABLE DATA; 
--

--
-- Data for Name: package_issue_uom; Type: TABLE DATA; 
--

INSERT INTO package_issue_uom (package_uom, issue_uom, package_size) VALUES ('Numbers', 'Numbers', 1);

--
-- Data for Name: package_item_charges; Type: TABLE DATA; 
--

--
-- Data for Name: package_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: package_prescribed; Type: TABLE DATA; 
--

--
-- Data for Name: package_sponsor_master; Type: TABLE DATA; 
--

--
-- Data for Name: packages; Type: TABLE DATA; 
--

--
-- Data for Name: password_history; Type: TABLE DATA; 
--

--
-- Data for Name: password_rule; Type: TABLE DATA; 
--

INSERT INTO password_rule (min_len, min_lower, min_upper, min_digits, min_special_chars, specail_char_list, last_password_frequency, password_change_freq_days, password_change_notify_days, max_login_attempt) VALUES (12, 0, 0, 0, 0, NULL, 4, NULL, NULL, 50);

--
-- Data for Name: pat_export_data_pref; Type: TABLE DATA; 
--

--
-- Data for Name: patient_activities; Type: TABLE DATA; 
--

--
-- Data for Name: patient_activities_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_activities_obsolete; Type: TABLE DATA; 
--

--
-- Data for Name: patient_admission_request; Type: TABLE DATA; 
--

--
-- Data for Name: patient_admission_request_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_allergies; Type: TABLE DATA; 
--

--
-- Data for Name: patient_allergies_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_bed_eqipmentcharges; Type: TABLE DATA; 
--

--
-- Data for Name: patient_category_master; Type: TABLE DATA; 
--

INSERT INTO patient_category_master (category_id, category_name, status, seperate_num_seq, ip_rate_plan_id, primary_ip_sponsor_id, ip_allowed_rate_plans, ip_allowed_sponsors, code, registration_charge_applicable, case_file_required, passport_details_required, op_rate_plan_id, primary_op_sponsor_id, op_allowed_rate_plans, op_allowed_sponsors, primary_ip_insurance_co_id, primary_op_insurance_co_id, ip_allowed_insurance_co_ids, op_allowed_insurance_co_ids, secondary_ip_sponsor_id, secondary_op_sponsor_id, secondary_ip_insurance_co_id, secondary_op_insurance_co_id, center_id) VALUES (1, 'General', 'A', 'N', NULL, NULL, '*', '*', NULL, 'Y', 'Y', 'N', NULL, NULL, '*', '*', NULL, NULL, '*', '*', NULL, NULL, NULL, NULL, 0);

--
-- Data for Name: patient_consultation_field_values; Type: TABLE DATA; 
--

--
-- Data for Name: patient_consultation_prescriptions; Type: TABLE DATA; 
--

--
-- Data for Name: patient_consultation_prescriptions_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_corporate_details; Type: TABLE DATA; 
--

--
-- Data for Name: patient_demographics_mod; Type: TABLE DATA; 
--

--
-- Data for Name: patient_dental_condition_details; Type: TABLE DATA; 
--

--
-- Data for Name: patient_dental_condition_main; Type: TABLE DATA; 
--

--
-- Data for Name: patient_deposits; Type: TABLE DATA; 
--

--
-- Data for Name: patient_deposits_setoff_adjustments; Type: TABLE DATA; 
--

--
-- Data for Name: patient_details_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_diet_prescriptions; Type: TABLE DATA; 
--

--
-- Data for Name: patient_discharge; Type: TABLE DATA; 
--

--
-- Data for Name: patient_documents; Type: TABLE DATA; 
--

--
-- Data for Name: patient_eye_history; Type: TABLE DATA; 
--

--
-- Data for Name: patient_finger_prints; Type: TABLE DATA; 
--

--
-- Data for Name: patient_general_docs; Type: TABLE DATA; 
--

--
-- Data for Name: patient_general_docs_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_general_images; Type: TABLE DATA; 
--

--
-- Data for Name: patient_header_preferences; Type: TABLE DATA; 
--

INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('mr_no', 'Y', 'P', 'MR No', 'b', 'Both', 1, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('full_name', 'Y', 'P', 'Name', 'b', 'Both', 2, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_gender', 'Y', 'P', 'Gender', 'b', 'Both', 3, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('age_text', 'Y', 'P', 'Age', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('salutation', 'N', 'P', 'Salutation', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_name', 'N', 'P', 'First Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('middle_name', 'N', 'P', 'Middle Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('last_name', 'N', 'P', 'Last Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_phone', 'Y', 'P', 'Mobile No.', 'b', 'Both', 5, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('addnl_phone', 'Y', 'P', 'Additional Phone', 'b', 'Both', 6, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_address', 'N', 'P', 'Address', 'b', 'Both', 7, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_area', 'N', 'P', 'Area', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('oldmrno', 'N', 'P', 'Old MR No', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('casefile_no', 'N', 'P', 'Case File No', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('remarks', 'N', 'P', 'Remarks', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_care_oftext', 'N', 'P', 'Care Of Text', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_careof_address', 'N', 'P', 'Care Of Address', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('relation', 'N', 'P', 'Relation Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('next_of_kin_relation', 'N', 'P', 'Relation', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('dead_on_arrival', 'N', 'P', 'Death On Arrival', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('original_mr_no', 'N', 'P', 'Original MR No', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('isbaby', 'N', 'V', 'Is Baby', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('category_name', 'N', 'V', 'Category Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('dateofbirth', 'Y', 'P', 'Date Of Birth', 'b', 'Both', 4, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('expected_dob', 'Y', 'P', 'Expected Date Of Birth', 'b', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('death_date', 'N', 'P', 'Date Of Death', 'b', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('death_time', 'N', 'P', 'Time Of Death', 'b', 'Both', 50, 'time');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('member_id', 'N', 'V', 'Member Id', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('category_expiry_date', 'N', 'P', 'Category Expiry Date', 'b', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_category_name', 'N', 'P', 'Patient Category Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_consulttaion_info', 'N', 'P', 'Patient Consultation Info', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_photo_available', 'N', 'P', 'Patient Photo Available', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('previous_visit_id', 'N', 'P', 'Previous Visit Id', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_id', 'N', 'P', 'Visit Id', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('med_allergies', 'N', 'P', 'Med Allergies', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('food_allergies', 'N', 'P', 'Food Allergies', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('other_allergies', 'N', 'P', 'Other Allergies', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('vip_status', 'N', 'P', 'Vip Status', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('government_identifier', 'N', 'P', 'Government Identifier', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('portal_access', 'N', 'P', 'Portal Access', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('email_id', 'Y', 'P', 'Email Id', 'b', 'Both', 8, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('passport_no', 'N', 'P', 'Passport No', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_id', 'N', 'V', 'Patient Id', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_status', 'N', 'V', 'Visit Status', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_type', 'N', 'V', 'Visit Type', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('revisit', 'N', 'V', 'Revisit', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('op_type', 'N', 'V', 'Op Type', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('op_type_name', 'N', 'V', 'Op Type Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('main_visit_id', 'N', 'V', 'Main Visit Id', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('mlc_status', 'N', 'V', 'Mlc Status', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patrelation', 'N', 'V', 'Patient Relation', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('pataddress', 'N', 'V', 'Patient Relation Address', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('complaint', 'N', 'V', 'Complaint', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('analysis_of_complaint', 'N', 'V', 'Analysis Of Complaint', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('doctor_name', 'Y', 'V', 'Doctor', 'b', 'Both', 9, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('admitted_dept', 'Y', 'V', 'Admitted Dept', 'i', 'Both', 10, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('dept_name', 'Y', 'V', 'Dept Name', 'o', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('unit_name', 'N', 'V', 'Unit Name', 'o', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('org_name', 'Y', 'V', 'Rate Plan', 'o', 'Both', 11, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('bill_bed_type', 'N', 'V', 'Billing Bed Type', 'i', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('alloc_bed_type', 'N', 'V', 'Alloc. Bed Type', 'i', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('alloc_bed_name', 'N', 'V', 'Bed', 'i', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('reg_ward_name', 'N', 'V', 'Reg Ward', 'i', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('alloc_ward_name', 'N', 'V', 'Ward', 'i', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('dis_format', 'N', 'V', 'Discharge Format', 'i', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('discharge_flag', 'N', 'V', 'Discharge Flag', 'i', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('dis_finalized_user', 'N', 'V', 'Discharge Finalized User', 'i', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('discharged_by', 'N', 'V', 'Discharge By', 'i', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('admitted_by', 'N', 'V', 'Admitted By', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('codification_status', 'N', 'V', 'Codification Status', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('established_type', 'N', 'V', 'Established Type', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('refdoctorname', 'N', 'V', 'Referred By', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('reg_charge_accepted', 'N', 'V', 'Reg Charge Accepted', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('use_drg', 'N', 'V', 'Use Drg', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('drg_code', 'N', 'V', 'Drg Code', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('use_perdiem', 'N', 'V', 'Use Perdiem', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('per_diem_code', 'N', 'V', 'Perdiem Code', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('mlc_no', 'N', 'V', 'Mlc No', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('mlc_type', 'N', 'V', 'Mlc Type', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('accident_place', 'N', 'V', 'Accident Place', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('police_stn', 'N', 'V', 'Police Station', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('mlc_remarks', 'N', 'V', 'Mlc Remarks', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('certificate_status', 'N', 'V', 'Certificate Status', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('primary_diagnosis_code', 'N', 'V', 'Primary Diagnosis Code', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('primary_diagnosis_description', 'N', 'V', 'Primary Diagnosis Description', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('secondary_diagnosis_description', 'N', 'V', 'Secondary Diagnosis Description', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('primary_insurance_approval', 'N', 'V', 'Primary Insurance Approval', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('secondary_insurance_approval', 'N', 'V', 'Secondary Insurance Approval', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('tpa_name', 'N', 'V', 'TPA/Sponsor/Corporate', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('sec_tpa_name', 'N', 'V', 'Sec. TPA/Sponsor/Corporate', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('sponsor_type', 'N', 'V', 'Sponsor Type', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('sec_sponsor_type', 'N', 'V', 'Sec. Sponsor Type', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('insurance_co_name', 'N', 'V', 'Insurance Co.', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('insurance_co_address', 'N', 'V', 'Insurance Co. Address', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('sec_insurance_co_name', 'N', 'V', 'Sec. Insurance Co.', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('sec_insurance_co_address', 'N', 'V', 'Sec. Insurance Co. Address', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('verify_finger_print', 'N', 'V', 'Verify Finger Print', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('plan_type_name', 'N', 'V', 'Net./Plan Type', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('plan_exclusions', 'N', 'V', 'Plan Exclusions', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('plan_notes', 'N', 'V', 'Plan Notes', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('plan_name', 'N', 'V', 'Plan Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('policy_number', 'N', 'V', 'Policy Number', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('policy_holder_name', 'N', 'V', 'Policy Holder Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_relationship', 'N', 'V', 'Patient Relationship', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_corporate_relation', 'N', 'V', 'Patient Corporate Relation', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('employee_id', 'N', 'V', 'Employee Id', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('employee_name', 'N', 'V', 'Employee Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('citizen_name', 'N', 'V', 'Citizen Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_national_relation', 'N', 'V', 'Patient National Relation', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('sec_patient_corporate_relation', 'N', 'V', 'Sec. Patient Corporate Relation', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('sec_employee_id', 'y', 'V', 'Sec. Employee Id', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visa_validity', 'N', 'P', 'Visa Validity', 'b', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('reg_date', 'Y', 'V', 'Reg Date', 'b', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('reg_time', 'Y', 'V', 'Reg Time', 'b', 'Both', 50, 'time');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('bed_start_date', 'N', 'V', 'Bed Start Date', 'i', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('bed_end_date', 'N', 'V', 'Bed End Date', 'i', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('discharge_date', 'N', 'V', 'Discharge Date', 'i', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('discharge_time', 'N', 'V', 'Discharge Time', 'i', 'Both', 50, 'time');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('disch_date_for_disch_summary', 'N', 'V', 'Discharge Date For Discharge Summary', 'b', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('disch_time_for_disch_summary', 'N', 'V', 'Discharge Time For Discharge Summary', 'b', 'Both', 50, 'time');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('policy_validity_start', 'N', 'V', 'Policy Validity Start', 'b', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('family_id', 'N', 'P', 'Family ID', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('policy_validity_end', 'N', 'V', 'Policy Validity End', 'b', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('sec_employee_name', 'N', 'V', 'Sec. Employee Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('national_id', 'Y', 'V', 'National ID', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('sec_national_id', 'N', 'V', 'Sec. National ID', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('sec_citizen_name', 'N', 'V', 'Sec. Citizen Name', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('sec_patient_national_relation', 'N', 'V', 'Sec. Patient National Relation', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('signatory_username', 'N', 'V', 'Signatory Username', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('require_pbm_authorization', 'N', 'V', 'Require Pbm Authorization', 'b', 'Both', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('passport_validity', 'N', 'P', 'Passport Validity', 'b', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('dis_finalized_date', 'N', 'V', 'Discharge Finalized Date', 'i', 'Both', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field1', 'Y', 'P', 'Custom Field 1', 'b', 'None', 12, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field2', 'Y', 'P', 'Custom Field 2', 'b', 'None', 13, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field3', 'Y', 'P', 'Custom Field 3', 'b', 'None', 14, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field4', 'Y', 'P', 'Patient Category', 'b', 'None', 15, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field5', 'N', 'P', 'Patient Sourcing Category', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field6', 'N', 'P', 'Custom Field 6', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field7', 'N', 'P', 'Custom Field 7', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field8', 'N', 'P', 'Custom Field 8', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field9', 'N', 'P', 'Custom Field 9', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field10', 'N', 'P', 'Custom Field 10', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field11', 'N', 'P', 'Custom Field 11', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field12', 'N', 'P', 'Custom Field 12', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field13', 'N', 'P', 'Custom Field 13', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field14', 'N', 'P', 'Custom Field 14', 'b', 'None', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field15', 'N', 'P', 'Custom Field 15', 'b', 'None', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field16', 'N', 'P', 'Custom Field 16', 'b', 'None', 50, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field17', 'N', 'P', 'Custom Field 17', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field18', 'N', 'P', 'Custom Field 18', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_field19', 'N', 'P', 'Custom Field 19', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_list1_value', 'N', 'P', 'Custom List 1', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_list2_value', 'N', 'P', 'Custom List 2', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_list3_value', 'N', 'P', 'Custom List 3', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_list4_value', 'N', 'P', 'Blood Group', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_list5_value', 'N', 'P', 'Religion', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_list6_value', 'N', 'P', 'Occupation', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_list7_value', 'N', 'P', 'Custom List 7', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_list8_value', 'N', 'P', 'Custom List 8', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('custom_list9_value', 'N', 'P', 'Custom List 9', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_custom_field1', 'Y', 'V', 'Visit Custom Field 1', 'b', 'None', 18, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_custom_field2', 'Y', 'V', 'Visit Custom Field 2', 'b', 'None', 19, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_custom_field3', 'Y', 'V', 'Visit Custom Field 3', 'b', 'None', 20, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_custom_field4', 'Y', 'V', 'Visit Custom Field 4', 'b', 'None', 21, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_custom_field5', 'Y', 'V', 'Visit Custom Field 5', 'b', 'None', 22, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_custom_field6', 'Y', 'V', 'Visit Custom Field 6', 'b', 'None', 23, 'date');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_custom_field7', 'N', 'V', 'Visit Custom Field 7', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_custom_field8', 'N', 'V', 'Visit Custom Field 8', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_custom_field9', 'N', 'V', 'Visit Custom Field 9', 'b', 'None', 50, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_custom_list1', 'Y', 'V', 'Visit Custom List 1', 'b', 'None', 16, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('visit_custom_list2', 'Y', 'V', 'Visit Custom List 2', 'b', 'None', 17, 'Text');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('patient_mod_time', 'N', 'P', 'Patient Mod Time', 'b', 'Both', 50, 'timestamp');
INSERT INTO patient_header_preferences (field_name, display, data_level, field_desc, visit_type, data_category, display_order, data_type) VALUES ('discharge_state_name', 'Y', 'V', 'Discharge Status', 'b', 'Both', 50, 'Text');

--
-- Data for Name: patient_health_maintenance; Type: TABLE DATA; 
--

--
-- Data for Name: patient_history; Type: TABLE DATA; 
--

--
-- Data for Name: patient_hvf_doc_images; Type: TABLE DATA; 
--

--
-- Data for Name: patient_hvf_doc_values; Type: TABLE DATA; 
--

--
-- Data for Name: patient_insurance_details; Type: TABLE DATA; 
--

--
-- Data for Name: patient_insurance_plan_details; Type: TABLE DATA; 
--

--
-- Data for Name: tpa_master; Type: TABLE DATA; 
--

INSERT INTO tpa_master (tpa_id, tpa_name, state, city, country, pincode, phone_no, mobile_no, email_id, fax, contact_name, contact_designation, contact_phone, contact_mobile, contact_email, address, status, tpa_pdf_form, validity_end_date, claim_template_id, default_claim_template, tpa_code_obsolete, sponsor_type, per_day_rate, scanned_doc_required, per_visit_copay_op, per_visit_copay_ip, created_timestamp, updated_timestamp, pre_auth_mode, claim_format, sponsor_type_id, member_id_pattern, max_resubmission_count, tpa_member_id_validation_type, tin_number, claim_amount_includes_tax, limit_includes_tax) VALUES ('TPAID0001', 'TTK', 'ANDAMAN AND NICOBAR', 'CAR NICOBAR', 'INDIA', '560301', '080-69989898', '', '', '080-69989898', 'EXAMPLE', 'manager', '080-69989898', '9565656565', '', 'BANGALORE', 'A', '1', NULL, 0, 'Y', NULL, 'I', NULL, 'N', NULL, NULL, '2013-10-08 10:09:20.637345', '2013-10-08 10:09:20.737408', 'M', 'XML', 3, NULL, 3, 'A', NULL, 'Y', 'Y');
INSERT INTO tpa_master (tpa_id, tpa_name, state, city, country, pincode, phone_no, mobile_no, email_id, fax, contact_name, contact_designation, contact_phone, contact_mobile, contact_email, address, status, tpa_pdf_form, validity_end_date, claim_template_id, default_claim_template, tpa_code_obsolete, sponsor_type, per_day_rate, scanned_doc_required, per_visit_copay_op, per_visit_copay_ip, created_timestamp, updated_timestamp, pre_auth_mode, claim_format, sponsor_type_id, member_id_pattern, max_resubmission_count, tpa_member_id_validation_type, tin_number, claim_amount_includes_tax, limit_includes_tax) VALUES ('TPAID0002', 'BAJAJ ALLIANZ', 'KARNATAKA', 'BANGALORE', 'INDIA', '560035', '080-65656567', '', '', '080-65656567', 'EXAMPLE', 'manager', '080-65656567', '9675654322', '', 'BANGALORE', 'A', '2', NULL, 0, 'Y', NULL, 'I', NULL, 'N', NULL, NULL, '2013-10-08 10:09:20.637345', '2013-10-08 10:09:20.737408', 'M', 'XML', 3, NULL, 3, 'A', NULL, 'Y', 'Y');
INSERT INTO tpa_master (tpa_id, tpa_name, state, city, country, pincode, phone_no, mobile_no, email_id, fax, contact_name, contact_designation, contact_phone, contact_mobile, contact_email, address, status, tpa_pdf_form, validity_end_date, claim_template_id, default_claim_template, tpa_code_obsolete, sponsor_type, per_day_rate, scanned_doc_required, per_visit_copay_op, per_visit_copay_ip, created_timestamp, updated_timestamp, pre_auth_mode, claim_format, sponsor_type_id, member_id_pattern, max_resubmission_count, tpa_member_id_validation_type, tin_number, claim_amount_includes_tax, limit_includes_tax) VALUES ('TPAID0005', 'STAR HEALTH', 'ANDAMAN AND NICOBAR', 'CAR NICOBAR', 'INDIA', '571511', '080-78756433', '', '', '080-78756433', 'EXAMPLE', 'manager', '080-78752434', '9552545345', '', 'BANGALORE', 'A', '3', NULL, 0, 'Y', NULL, 'I', NULL, 'N', NULL, NULL, '2013-10-08 10:09:20.637345', '2013-10-08 10:09:20.737408', 'M', 'XML', 3, NULL, 3, 'A', NULL, 'Y', 'Y');
INSERT INTO tpa_master (tpa_id, tpa_name, state, city, country, pincode, phone_no, mobile_no, email_id, fax, contact_name, contact_designation, contact_phone, contact_mobile, contact_email, address, status, tpa_pdf_form, validity_end_date, claim_template_id, default_claim_template, tpa_code_obsolete, sponsor_type, per_day_rate, scanned_doc_required, per_visit_copay_op, per_visit_copay_ip, created_timestamp, updated_timestamp, pre_auth_mode, claim_format, sponsor_type_id, member_id_pattern, max_resubmission_count, tpa_member_id_validation_type, tin_number, claim_amount_includes_tax, limit_includes_tax) VALUES ('TPAID0003', 'UNITED INSURANCE', 'ANDAMAN AND NICOBAR', 'CAR NICOBAR', 'INDIA', '571511', '080-78756433', '', '', '080-78756433', 'EXAMPLE', 'manager', '080-78756433', '9554545545', '', 'BANGALORE', 'A', '4', NULL, 0, 'Y', NULL, 'I', NULL, 'N', NULL, NULL, '2013-10-08 10:09:20.637345', '2013-10-08 10:09:20.737408', 'M', 'XML', 3, NULL, 3, 'A', NULL, 'Y', 'Y');
INSERT INTO tpa_master (tpa_id, tpa_name, state, city, country, pincode, phone_no, mobile_no, email_id, fax, contact_name, contact_designation, contact_phone, contact_mobile, contact_email, address, status, tpa_pdf_form, validity_end_date, claim_template_id, default_claim_template, tpa_code_obsolete, sponsor_type, per_day_rate, scanned_doc_required, per_visit_copay_op, per_visit_copay_ip, created_timestamp, updated_timestamp, pre_auth_mode, claim_format, sponsor_type_id, member_id_pattern, max_resubmission_count, tpa_member_id_validation_type, tin_number, claim_amount_includes_tax, limit_includes_tax) VALUES ('TPAID0006', 'RAKSHA', 'ANDAMAN AND NICOBAR', 'CAR NICOBAR', 'INDIA', '571511', '080-22632452', '', '', '080-21632452', 'EXAMPLE', 'manager', '080-21632453', '9151542341', '', 'BANGALORE', 'A', '5', NULL, 0, 'Y', NULL, 'I', NULL, 'N', NULL, NULL, '2013-10-08 10:09:20.637345', '2013-10-08 10:09:20.737408', 'M', 'XML', 3, NULL, 3, 'A', NULL, 'Y', 'Y');
INSERT INTO tpa_master (tpa_id, tpa_name, state, city, country, pincode, phone_no, mobile_no, email_id, fax, contact_name, contact_designation, contact_phone, contact_mobile, contact_email, address, status, tpa_pdf_form, validity_end_date, claim_template_id, default_claim_template, tpa_code_obsolete, sponsor_type, per_day_rate, scanned_doc_required, per_visit_copay_op, per_visit_copay_ip, created_timestamp, updated_timestamp, pre_auth_mode, claim_format, sponsor_type_id, member_id_pattern, max_resubmission_count, tpa_member_id_validation_type, tin_number, claim_amount_includes_tax, limit_includes_tax) VALUES ('TPAID0004', 'MEDI ASSIST', 'ANDAMAN AND NICOBAR', 'CAR NICOBAR', 'INDIA', '571511', '080-78756433', '', '', '080-78756433', 'EXAMPLE', 'manager', '080-78756433', '9554545545', '', 'BANGALORE', 'A', '6', NULL, 0, 'Y', NULL, 'I', NULL, 'N', NULL, NULL, '2013-10-08 10:09:20.637345', '2013-10-08 10:09:20.737408', 'M', 'XML', 3, NULL, 3, 'A', NULL, 'Y', 'Y');

--
-- Data for Name: patient_insurance_plans; Type: TABLE DATA; 
--

--
-- Data for Name: pbm_request_approval_details; Type: TABLE DATA; 
--

--
-- Data for Name: pbm_prescription; Type: TABLE DATA; 
--

--
-- Data for Name: store_sales_main; Type: TABLE DATA; 
--

--
-- Data for Name: patient_medicine_prescriptions; Type: TABLE DATA; 
--

--
-- Data for Name: patient_medicine_prescriptions_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_national_sponsor_details; Type: TABLE DATA; 
--

--
-- Data for Name: patient_operation_prescriptions; Type: TABLE DATA; 
--

--
-- Data for Name: patient_operation_prescriptions_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_other_medicine_prescriptions; Type: TABLE DATA; 
--

--
-- Data for Name: patient_other_medicine_prescriptions_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_other_prescriptions; Type: TABLE DATA; 
--

--
-- Data for Name: patient_other_prescriptions_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_pac; Type: TABLE DATA; 
--

--
-- Data for Name: patient_package_details; Type: TABLE DATA; 
--

--
-- Data for Name: patient_packages; Type: TABLE DATA; 
--

--
-- Data for Name: patient_partogram; Type: TABLE DATA; 
--

--
-- Data for Name: patient_partogram_details; Type: TABLE DATA; 
--

--
-- Data for Name: patient_pdf_doc_images; Type: TABLE DATA; 
--

--
-- Data for Name: patient_pdf_form_doc_values; Type: TABLE DATA; 
--

--
-- Data for Name: patient_pdf_form_doc_values_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_policy_details; Type: TABLE DATA; 
--

--
-- Data for Name: patient_prescription; Type: TABLE DATA; 
--

--
-- Data for Name: patient_prescription_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_registration_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_registration_cards; Type: TABLE DATA; 
--

--
-- Data for Name: patient_section_details; Type: TABLE DATA; 
--

--
-- Data for Name: patient_section_details_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_section_details_orig; Type: TABLE DATA; 
--

--
-- Data for Name: patient_section_forms; Type: TABLE DATA; 
--

--
-- Data for Name: patient_section_image_details; Type: TABLE DATA; 
--

--
-- Data for Name: patient_section_values; Type: TABLE DATA; 
--

--
-- Data for Name: patient_section_values_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_service_prescriptions; Type: TABLE DATA; 
--

--
-- Data for Name: patient_service_prescriptions_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_sponsor_approvals; Type: TABLE DATA; 
--

--
-- Data for Name: patient_sponsor_approval_details; Type: TABLE DATA; 
--

--
-- Data for Name: patient_sponsor_approvals_docs; Type: TABLE DATA; 
--

--
-- Data for Name: patient_standing_instructions_obsolete; Type: TABLE DATA; 
--

--
-- Data for Name: patient_test_prescriptions; Type: TABLE DATA; 
--

--
-- Data for Name: patient_test_prescriptions_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: patient_treatment; Type: TABLE DATA; 
--

--
-- Data for Name: patient_vaccination; Type: TABLE DATA; 
--

--
-- Data for Name: patient_visit_bills_template; Type: TABLE DATA; 
--

--
-- Data for Name: payment_mode_master; Type: TABLE DATA; 
--

INSERT INTO payment_mode_master (mode_id, payment_mode, card_type_required, bank_required, ref_required, realization_required, status, displayorder, spl_account_name, bank_batch_required, card_auth_required, card_holder_required, card_number_required, card_expdate_required, totp_required, mobile_number_required, transaction_limit, allow_payments_more_than_transaction_limit) VALUES (-1, 'Cash', 'N', 'N', 'N', 'N', 'A', 1, 'Cash', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 0.00, 'A');
INSERT INTO payment_mode_master (mode_id, payment_mode, card_type_required, bank_required, ref_required, realization_required, status, displayorder, spl_account_name, bank_batch_required, card_auth_required, card_holder_required, card_number_required, card_expdate_required, totp_required, mobile_number_required, transaction_limit, allow_payments_more_than_transaction_limit) VALUES (1, 'Credit Card', 'N', 'Y', 'Y', 'N', 'A', 2, 'Credit Card', 'Y', 'Y', 'Y', 'Y', 'Y', 'N', 'N', 0.00, 'A');
INSERT INTO payment_mode_master (mode_id, payment_mode, card_type_required, bank_required, ref_required, realization_required, status, displayorder, spl_account_name, bank_batch_required, card_auth_required, card_holder_required, card_number_required, card_expdate_required, totp_required, mobile_number_required, transaction_limit, allow_payments_more_than_transaction_limit) VALUES (2, 'Debit Card', 'N', 'Y', 'Y', 'N', 'A', 3, 'Bank', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 0.00, 'A');
INSERT INTO payment_mode_master (mode_id, payment_mode, card_type_required, bank_required, ref_required, realization_required, status, displayorder, spl_account_name, bank_batch_required, card_auth_required, card_holder_required, card_number_required, card_expdate_required, totp_required, mobile_number_required, transaction_limit, allow_payments_more_than_transaction_limit) VALUES (4, 'Demand Draft', 'N', 'Y', 'Y', 'N', 'A', 5, 'Bank', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 0.00, 'A');
INSERT INTO payment_mode_master (mode_id, payment_mode, card_type_required, bank_required, ref_required, realization_required, status, displayorder, spl_account_name, bank_batch_required, card_auth_required, card_holder_required, card_number_required, card_expdate_required, totp_required, mobile_number_required, transaction_limit, allow_payments_more_than_transaction_limit) VALUES (3, 'Cheque', 'N', 'Y', 'Y', 'Y', 'A', 4, 'Bank', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 0.00, 'A');
INSERT INTO payment_mode_master (mode_id, payment_mode, card_type_required, bank_required, ref_required, realization_required, status, displayorder, spl_account_name, bank_batch_required, card_auth_required, card_holder_required, card_number_required, card_expdate_required, totp_required, mobile_number_required, transaction_limit, allow_payments_more_than_transaction_limit) VALUES (-2, 'Paytm', 'N', 'N', 'N', 'N', 'A', 6, 'paytm', 'N', 'N', 'N', 'N', 'N', 'Y', 'Y', 0.00, 'A');
INSERT INTO payment_mode_master (mode_id, payment_mode, card_type_required, bank_required, ref_required, realization_required, status, displayorder, spl_account_name, bank_batch_required, card_auth_required, card_holder_required, card_number_required, card_expdate_required, totp_required, mobile_number_required, transaction_limit, allow_payments_more_than_transaction_limit) VALUES (-3, 'Loyalty Card', 'N', 'N', 'N', 'N', 'I', 7, 'loyaltycard', 'N', 'N', 'N', 'N', 'N', 'Y', 'Y', 0.00, 'A');

--
-- Data for Name: payment_rules; Type: TABLE DATA; 
--

INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'GREG', NULL, 10, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 1, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'IPREG', NULL, 20, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 2, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'OPREG', NULL, 30, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 3, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'EMREG', NULL, 40, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 4, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'MLREG', NULL, 50, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 5, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'OPDOC', NULL, 60, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 6, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'ROPDOC', NULL, 70, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 7, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'IPDOC', NULL, 80, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 8, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'BBED', NULL, 170, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 9, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'NCBED', NULL, 180, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 10, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'DDBED', NULL, 190, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 11, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'PCBED', NULL, 200, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 12, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'BICU', NULL, 210, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 13, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'NCICU', NULL, 220, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 14, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'DDICU', NULL, 230, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 15, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'PCICU', NULL, 240, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 16, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'LTDIA', NULL, 250, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 17, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'RTDIA', NULL, 260, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 18, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'SUOPE', NULL, 270, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 19, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'TCOPE', NULL, 280, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 20, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'ANAOPE', NULL, 290, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 21, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'CONOPE', NULL, 300, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 22, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'EQOPE', NULL, 310, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 23, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'SACOPE', NULL, 320, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 24, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'ASUOPE', NULL, 330, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 25, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'AANOPE', NULL, 340, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 26, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'COSOPE', NULL, 350, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 27, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'SERSNP', NULL, 360, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 28, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'PKGPKG', NULL, 370, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 29, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'EQUOTC', NULL, 380, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 30, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'OCOTC', NULL, 390, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 31, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'CONOTC', NULL, 400, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 32, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'MISOTC', NULL, 410, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 33, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'IMPOTC', NULL, 420, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 34, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'MEMED', NULL, 430, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 35, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'PHMED', NULL, 440, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 36, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'CONMED', NULL, 450, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 37, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'LTAX', NULL, 460, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 38, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'STAX', NULL, 470, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 39, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'BIDIS', NULL, 490, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 40, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'PHRET', NULL, 500, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 41, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'INVITE', NULL, 510, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 42, '1', '*');
INSERT INTO payment_rules (doctor_category, referrer_category, rate_plan, charge_head, activity_id, precedance, dr_payment_option, dr_payment_value, ref_payment_option, ref_payment_value, hosp_payment_option, hosp_payment_value, prescribed_category, presc_payment_option, presc_payment_value, dr_pkg_amt, username, dr_payment_expr, ref_payment_expr, presc_payment_expr, payment_id, use_discounted_amount, center_id) VALUES ('*', '*', '*', 'INVRET', NULL, 520, '1', 0.00, '1', 0.00, '1', 0.00, '*', '1', 0.00, 0.00, NULL, NULL, NULL, NULL, 43, '1', '*');

--
-- Data for Name: payment_rules_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: payment_transactions; Type: TABLE DATA; 
--

--
-- Data for Name: payments; Type: TABLE DATA; 
--

--
-- Data for Name: payments_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: payments_details; Type: TABLE DATA; 
--

--
-- Data for Name: payments_details_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: pbm_approval_amount_details; Type: TABLE DATA; 
--

--
-- Data for Name: pbm_medicine_prescriptions; Type: TABLE DATA; 
--

--
-- Data for Name: pbm_medicine_sales; Type: TABLE DATA; 
--

--
-- Data for Name: pbm_observations_master; Type: TABLE DATA; 
--

INSERT INTO pbm_observations_master (id, observation_name, patient_med_presc_value_column, patient_med_presc_units_column, observation_type, code, required, status) VALUES (1, 'Strength', 'item_strength', 'item_strength_units', 'Text', 'Dose', 'Y', 'A');
INSERT INTO pbm_observations_master (id, observation_name, patient_med_presc_value_column, patient_med_presc_units_column, observation_type, code, required, status) VALUES (2, 'Duration', 'duration', 'duration_units', 'Text', 'Duration', 'Y', 'A');
INSERT INTO pbm_observations_master (id, observation_name, patient_med_presc_value_column, patient_med_presc_units_column, observation_type, code, required, status) VALUES (3, 'Dosage Form', 'item_form_id', '', 'Text', 'DosageForm', 'Y', 'A');
INSERT INTO pbm_observations_master (id, observation_name, patient_med_presc_value_column, patient_med_presc_units_column, observation_type, code, required, status) VALUES (4, 'Route', 'route_of_admin', '', 'Text', 'Route', 'Y', 'A');

--
-- Data for Name: pbm_presc_observations; Type: TABLE DATA; 
--

--
-- Data for Name: pbm_prescription_request; Type: TABLE DATA; 
--

--
-- Data for Name: per_diem_codes_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: per_diem_codes_master; Type: TABLE DATA; 
--

--
-- Data for Name: per_diem_codes_charges; Type: TABLE DATA; 
--

--
-- Data for Name: per_diem_codes_charges_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: perdiem_code_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: permanent_access_types; Type: TABLE DATA; 
--

--
-- Data for Name: ph_payment_terms; Type: TABLE DATA; 
--

--
-- Data for Name: pharmacy_medicine_category; Type: TABLE DATA; 
--

INSERT INTO pharmacy_medicine_category (category_id, category_name, status, claimable, temp_cat_id) VALUES (1, 'general', 'A', true, 1);

--
-- Data for Name: phrase_suggestions_category_master; Type: TABLE DATA; 
--

INSERT INTO phrase_suggestions_category_master (phrase_suggestions_category_id, phrase_suggestions_category, status) VALUES (-1, 'complaints', 'A');

--
-- Data for Name: phrase_suggestions_master; Type: TABLE DATA; 
--

--
-- Data for Name: plan_docs_details; Type: TABLE DATA; 
--

--
-- Data for Name: po_print_template; Type: TABLE DATA; 
--

INSERT INTO po_print_template (template_name, pharmacy_template_content, user_name, reason, template_mode) VALUES ('Purchase Order Print Template', '', '', '', 'H');

--
-- Data for Name: preauth_activities_observations; Type: TABLE DATA; 
--

--
-- Data for Name: preauth_approval_amount_details; Type: TABLE DATA; 
--

--
-- Data for Name: preauth_request_approval_details; Type: TABLE DATA; 
--

--
-- Data for Name: preauth_prescription; Type: TABLE DATA; 
--

--
-- Data for Name: preauth_prescription_activities; Type: TABLE DATA; 
--

--
-- Data for Name: preauth_prescription_activities_docs; Type: TABLE DATA; 
--

--
-- Data for Name: preauth_prescription_request; Type: TABLE DATA; 
--

--
-- Data for Name: pregnancy_history; Type: TABLE DATA; 
--

--
-- Data for Name: presc_instr_master; Type: TABLE DATA; 
--

--
-- Data for Name: prescribed_medicines_master; Type: TABLE DATA; 
--

--
-- Data for Name: prescribed_services_master; Type: TABLE DATA; 
--

--
-- Data for Name: prescribed_tests_master; Type: TABLE DATA; 
--

--
-- Data for Name: prescription_label_print_template; Type: TABLE DATA; 
--

--
-- Data for Name: prescription_print_template; Type: TABLE DATA; 
--

INSERT INTO prescription_print_template (template_name, prescription_template_content, template_mode, user_name, reason) VALUES ('Web Based Prescription Template', '', 'H', 'InstaAdmin', 'web sharing');

--
-- Data for Name: print_templates; Type: TABLE DATA; 
--

INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('D', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('L', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('R', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('PE', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('RPB', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('RP', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('ORD', '', 'InstaAdmin', 'order', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('GRN', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('GTPASS', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('PWACT', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('INDENT', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('TSheet', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('APPINDENT', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('RETURNNOTE', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('APP_PRINT', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('Voucher', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('REGBARCODE', '', 'InstaAdmin', 'Registration Bar Code', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('ITMBARCODE', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('PrgNotes', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('CL', '', 'InstaAdmin', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('CI', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('TRANSFER', '', 'InstaAdmin', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('PAPERPRINT', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('TRMT_QUOTATION', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('DENTAL_SUPPLIER_PRINT', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('WEB_LAB', '', 'InstaAdmin', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('WEB_RAD', '', 'InstaAdmin', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('DoctorOrder', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('DoctorNotes', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('NurseNotes', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('PATIENT_RESPONSE_PRINT', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('BuiltinDischargeSummary', '', 'InstaAdmin', '', NULL, true, 'DischargeHVFTemplate');
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('PATIENT_INDENT', '', 'InstaAdmin', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('PENDING_PRESC', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('Medication_Chart', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('ConsultationDetails', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('WORKSHEET', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('OTDetails', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('API_LAB', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('API_RAD', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('Process_indent', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('PatientIssuePrintTemplate', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('PatientIssueReturnPrintTemplate', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('Vital', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('UserIssuePrintTemplate', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('UserIssueReturnPrintTemplate', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('WorkOrderPrintTemplate', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('PriorAuth', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('Discharge_Medication', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('Investigation', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('Vaccination', '', '', '', 0, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('Triage', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('Assessment', '', '', '', 0, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('VisitSummaryRecord', '', '', '', NULL, false, NULL);
INSERT INTO print_templates (template_type, print_template_content, user_name, reason, pheader_template_id, built_in, print_template_file) VALUES ('S', '', '', '', NULL, false, NULL);

--
-- Data for Name: prior_auth_modes; Type: TABLE DATA; 
--

INSERT INTO prior_auth_modes (prior_auth_mode_id, prior_auth_mode_name) VALUES (1, 'Verbal');
INSERT INTO prior_auth_modes (prior_auth_mode_id, prior_auth_mode_name) VALUES (2, 'E-Mail');
INSERT INTO prior_auth_modes (prior_auth_mode_id, prior_auth_mode_name) VALUES (3, 'Fax');
INSERT INTO prior_auth_modes (prior_auth_mode_id, prior_auth_mode_name) VALUES (4, 'Electronic');

--
-- Data for Name: progress_notes; Type: TABLE DATA; 
--

--
-- Data for Name: rate_plan_parameters; Type: TABLE DATA; 
--

--
-- Data for Name: receipt_refund_print_template; Type: TABLE DATA; 
--

--
-- Data for Name: receipts_collection; Type: TABLE DATA; 
--

--
-- Data for Name: recurrence_daily_master; Type: TABLE DATA; 
--

INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (13, 'Every 2 hours (odd)', 'A', '00:00, 02:00, 04:00, 06:00, 08:00, 10:00, 12:00, 14:00, 16:00, 18:00, 20:00, 22:00', 12, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (14, 'Every 2 hours (even)', 'A', '01:00, 03:00, 05:00, 07:00, 09:00, 11:00, 13:00, 15:00, 17:00, 19:00, 21:00, 23:00', 12, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (15, 'Every 4 hours (4,8,..)', 'A', '00:00, 04:00, 08:00, 12:00, 16:00, 20:00, 22:00', 6, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (16, 'Every 4 hours (2,6,..)', 'A', '02:00, 06:00, 10:00, 14:00, 18:00, 22:00', 6, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (17, 'Every 4 hours (1,5,..)', 'A', '01:00, 05:00, 09:00, 13:00, 17:00, 21:00', 6, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (18, 'Every 4 hours (3,7,..)', 'A', '03:00, 07:00, 11:00, 15:00, 19:00, 23:00', 6, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (19, '1-0-1 (early)', 'A', '08:00, 20:00', 2, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (20, '1-0-1', 'A', '09:00, 21:00', 2, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (21, '1-0-1 (late)', 'A', '10:00, 22:00', 2, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (22, '1-1-1 (early)', 'A', '08:00, 14:00, 20:00', 3, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (23, '1-1-1', 'A', '09:00, 15:00, 21:00', 3, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (24, '1-1-1 (late)', 'A', '10:00, 16:00, 22:00', 3, '2011-06-14 12:00:40', NULL, NULL);
INSERT INTO recurrence_daily_master (recurrence_daily_id, display_name, status, timings, num_activities, mod_time, username, display_order) VALUES (-1, 'Once', 'A', '', 1, '2013-10-08 10:09:29', '', -1);

--
-- Data for Name: refdcoc_charge_details; Type: TABLE DATA; 
--

--
-- Data for Name: referral; Type: TABLE DATA; 
--

--
-- Data for Name: referral_center_master; Type: TABLE DATA; 
--

--
-- Data for Name: reg_custom_fields; Type: TABLE DATA; 
--

INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (1, 'P', 'custom_list1_value', NULL, '.*', 'dropdown', 'string', NULL, 1, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (2, 'P', 'custom_list2_value', NULL, '.*', 'dropdown', 'string', NULL, 2, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (3, 'P', 'custom_list3_value', NULL, '.*', 'dropdown', 'string', NULL, 3, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (4, 'P', 'custom_list4_value', 'Blood Group', '.*', 'dropdown', 'string', 'M', 4, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (5, 'P', 'custom_list5_value', 'Religion', '.*', 'dropdown', 'string', 'M', 5, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (6, 'P', 'custom_list6_value', 'Occupation', '.*', 'dropdown', 'string', 'M', 6, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (7, 'P', 'custom_list7_value', NULL, '.*', 'dropdown', 'string', 'M', 7, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (8, 'P', 'custom_list8_value', NULL, '.*', 'dropdown', 'string', 'M', 8, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (9, 'P', 'custom_list9_value', NULL, '.*', 'dropdown', 'string', 'M', 9, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (10, 'P', 'custom_field1', 'Custom Field 1', '^.{0,100}$', 'input', 'string', NULL, 10, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (11, 'P', 'custom_field2', 'Custom Field 2', '^.{0,100}$', 'input', 'string', NULL, 11, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (12, 'P', 'custom_field3', 'Custom Field 3', '^.{0,100}$', 'input', 'string', NULL, 12, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (13, 'P', 'custom_field4', 'Patient Category', '^.{0,100}$', 'input', 'string', NULL, 13, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (14, 'P', 'custom_field5', 'Patient Sourcing Category', '^.{0,100}$', 'input', 'string', NULL, 14, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (15, 'P', 'custom_field6', NULL, '^.{0,100}$', 'input', 'string', NULL, 15, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (16, 'P', 'custom_field7', NULL, '^.{0,100}$', 'input', 'string', NULL, 16, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (17, 'P', 'custom_field8', NULL, '^.{0,100}$', 'input', 'string', NULL, 17, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (18, 'P', 'custom_field9', NULL, '^.{0,100}$', 'input', 'string', NULL, 18, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (19, 'P', 'custom_field10', NULL, '^.{0,100}$', 'input', 'string', NULL, 19, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (20, 'P', 'custom_field11', NULL, '^.{0,100}$', 'input', 'string', NULL, 20, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (21, 'P', 'custom_field12', NULL, '^.{0,100}$', 'input', 'string', NULL, 21, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (22, 'P', 'custom_field13', NULL, '^.{0,100}$', 'input', 'string', NULL, 22, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (23, 'P', 'custom_field14', NULL, '^(\d{2})-(\d{2})-(\d{4})$', 'input', 'date', 'D', 23, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (24, 'P', 'custom_field15', NULL, '^(\d{2})-(\d{2})-(\d{4})$', 'input', 'date', 'D', 24, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (25, 'P', 'custom_field16', NULL, '^(\d{2})-(\d{2})-(\d{4})$', 'input', 'date', 'D', 25, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (26, 'P', 'custom_field17', NULL, '^\d{0,8}(\.\d{1,2})?$', 'input', 'number', 'D', 26, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (27, 'P', 'custom_field18', NULL, '^\d{0,8}(\.\d{1,2})?$', 'input', 'number', 'D', 27, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (28, 'P', 'custom_field19', NULL, '^\d{0,8}(\.\d{1,2})?$', 'input', 'number', 'D', 28, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (29, 'V', 'visit_custom_list1', NULL, '.*', 'dropdown', 'string', 'M', 1, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (30, 'V', 'visit_custom_list2', NULL, '.*', 'dropdown', 'string', 'M', 2, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (31, 'V', 'visit_custom_field1', NULL, '^.{0,100}$', 'input', 'string', 'M', 3, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (32, 'V', 'visit_custom_field2', NULL, '^.{0,100}$', 'input', 'string', 'M', 4, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (33, 'V', 'visit_custom_field3', NULL, '^.{0,100}$', 'input', 'string', 'M', 5, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (34, 'V', 'visit_custom_field4', NULL, '^(\d{2})-(\d{2})-(\d{4})$', 'input', 'date', 'M', 6, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (35, 'V', 'visit_custom_field5', NULL, '^(\d{2})-(\d{2})-(\d{4})$', 'input', 'date', 'M', 7, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (36, 'V', 'visit_custom_field6', NULL, '^(\d{2})-(\d{2})-(\d{4})$', 'input', 'date', 'M', 8, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (37, 'V', 'visit_custom_field7', NULL, '^\d{0,8}(\.\d{1,2})?$', 'input', 'number', 'M', 9, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (38, 'V', 'visit_custom_field8', NULL, '^\d{0,8}(\.\d{1,2})?$', 'input', 'number', 'M', 10, 'N', 'A');
INSERT INTO reg_custom_fields (field_id, applicable_to, name, label, validation, display_type, type, show_group, display_order, mandatory, status) VALUES (39, 'V', 'visit_custom_field9', NULL, '^\d{0,8}(\.\d{1,2})?$', 'input', 'number', 'M', 11, 'N', 'A');

--
-- Data for Name: regexp_pattern_master; Type: TABLE DATA; 
--

INSERT INTO regexp_pattern_master (pattern_id, regexp_pattern, pattern_name, pattern_desc, status) VALUES (1, '/^\d+$/', 'Numerics', 'Numeric pattern 0-9', 'A');
INSERT INTO regexp_pattern_master (pattern_id, regexp_pattern, pattern_name, pattern_desc, status) VALUES (2, '/^\d*\.?\d+$/', 'Decimal', 'Numeric pattern with decimals', 'A');
INSERT INTO regexp_pattern_master (pattern_id, regexp_pattern, pattern_name, pattern_desc, status) VALUES (3, '/^[a-zA-Z]+$/', 'Alphabets', 'Plain text pattern', 'A');
INSERT INTO regexp_pattern_master (pattern_id, regexp_pattern, pattern_name, pattern_desc, status) VALUES (4, '/^\w+$/', 'AlphaNumeric', 'Alphanumeric pattern', 'A');
INSERT INTO regexp_pattern_master (pattern_id, regexp_pattern, pattern_name, pattern_desc, status) VALUES (5, '/^\w{1,}[@][\w\-]{1,}([.]([\w\-]{1,})){1,3}$/', 'Email Validation', 'aaa@domain.com', 'A');

--
-- Data for Name: region_master; Type: TABLE DATA; 
--

--
-- Data for Name: registration_cards; Type: TABLE DATA; 
--

--
-- Data for Name: registration_charges; Type: TABLE DATA; 
--

INSERT INTO registration_charges (org_id, ip_reg_charge, op_reg_charge, gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge, gen_reg_charge_discount, op_reg_charge_discount, reg_renewal_charge_discount, ip_reg_charge_discount, ip_mlccharge_discount, mrcharge_discount, bed_type, op_mlccharge, op_mlccharge_discount, is_override) VALUES ('ORG0001', 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'GENERAL', 0.00, 0.00, 'N');
INSERT INTO registration_charges (org_id, ip_reg_charge, op_reg_charge, gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge, gen_reg_charge_discount, op_reg_charge_discount, reg_renewal_charge_discount, ip_reg_charge_discount, ip_mlccharge_discount, mrcharge_discount, bed_type, op_mlccharge, op_mlccharge_discount, is_override) VALUES ('ORG0001', 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'PRIVATE', 0.00, 0.00, 'N');
INSERT INTO registration_charges (org_id, ip_reg_charge, op_reg_charge, gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge, gen_reg_charge_discount, op_reg_charge_discount, reg_renewal_charge_discount, ip_reg_charge_discount, ip_mlccharge_discount, mrcharge_discount, bed_type, op_mlccharge, op_mlccharge_discount, is_override) VALUES ('ORG0001', 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'SEMI-PVT', 0.00, 0.00, 'N');

--
-- Data for Name: registration_charges_backup; Type: TABLE DATA; 
--

--
-- Data for Name: registration_preferences; Type: TABLE DATA; 
--

INSERT INTO registration_preferences (pr_no, ip_validity_days, ip_credit, op_validity_days, grace, scda, receipt_require, night_pm, night_am, gen_charge_collect, op_ip, ip_op, op_validity_period, op_cons_validity, op_cons_validity_type, test_status, service_status, reg_validity_period, op_generate_token, custom_field1_label, custom_field2_label, custom_field3_label, custom_field4_label, custom_field5_label, patient_category_field_label, category_expiry_field_label, area_field_validate, nextofkin_field_validate, address_field_validate, complaint_field_validate, old_reg_field_label, case_file_settings, hosp_uses_units, dept_units_settings, custom_field6_label, custom_field6_validate, custom_field7_label, custom_field7_validate, custom_field8_label, custom_field8_validate, custom_field9_label, custom_field9_validate, custom_field10_label, custom_field10_validate, referredby_field_validate, default_op_ip_description, custom_field11_label, custom_field12_label, custom_field13_label, op_default_selection, ip_default_selection, custom_field1_validate, custom_field2_validate, custom_field3_validate, custom_field1_show, custom_field2_show, custom_field3_show, custom_field4_validate, custom_field5_validate, custom_field4_show, custom_field5_show, custom_field11_validate, custom_field12_validate, custom_field13_validate, custom_field11_show, custom_field12_show, custom_field13_show, custom_field6_show, custom_field7_show, custom_field8_show, custom_field9_show, custom_field10_show, custom_list4_validate, custom_list5_validate, custom_list6_validate, custom_list4_show, custom_list5_show, custom_list6_show, custom_list1_validate, custom_list2_validate, custom_list3_validate, custom_list1_show, custom_list2_show, custom_list3_show, custom_list1_name, custom_list2_name, custom_list3_name, issue_to_mrd_on_registration, government_identifier_label, government_identifier_type_label, outside_default_selection, encntr_start_and_end_reqd, encntr_type_reqd, patientphone_field_validate, passport_no, passport_no_validate, passport_no_show, passport_validity, passport_validity_validate, passport_validity_show, passport_issue_country, passport_issue_country_validate, passport_issue_country_show, visa_validity, visa_validity_validate, visa_validity_show, family_id, family_id_validate, family_id_show, allow_multiple_active_visits, doc_eandm_codification_required, visit_type_dependence, prior_auth_required, member_id_label, member_id_valid_from_label, member_id_valid_to_label, copy_paste_option, custom_list4_name, custom_list5_name, custom_list6_name, custom_list7_name, custom_list7_show, custom_list7_validate, custom_list8_name, custom_list8_show, custom_list8_validate, custom_list9_name, custom_list9_show, custom_list9_validate, default_followup_eandm_code, visit_custom_field1_name, visit_custom_field1_validate, visit_custom_field2_name, visit_custom_field2_validate, visit_custom_field3_name, visit_custom_field3_validate, visit_custom_list1_name, visit_custom_list1_validate, visit_custom_list2_name, visit_custom_list2_validate, default_op_encounter_start_type, default_op_encounter_end_type, default_ip_encounter_start_type, default_ip_encounter_end_type, custom_field14_label, custom_field15_label, custom_field16_label, custom_field14_show, custom_field15_show, custom_field16_show, custom_field14_validate, custom_field15_validate, custom_field16_validate, visit_custom_field4_name, visit_custom_field5_name, visit_custom_field6_name, visit_custom_field4_validate, visit_custom_field5_validate, visit_custom_field6_validate, custom_field17_label, custom_field18_label, custom_field19_label, custom_field17_show, custom_field18_show, custom_field19_show, custom_field17_validate, custom_field18_validate, custom_field19_validate, visit_custom_field7_name, visit_custom_field8_name, visit_custom_field9_name, visit_custom_field7_validate, visit_custom_field8_validate, visit_custom_field9_validate, visit_custom_list1_show, visit_custom_list2_show, visit_custom_field1_show, visit_custom_field2_show, visit_custom_field3_show, visit_custom_field4_show, visit_custom_field5_show, visit_custom_field6_show, visit_custom_field7_show, visit_custom_field8_show, visit_custom_field9_show, name_parts, name_local_lang_required, obsolete_govt_id_pattern, admitting_doctor_mandatory, patient_identification, referal_for_life, validate_email_id, allow_age_entry, mobile_phone_pattern, patient_reg_basis, allow_drg_perdiem, nationality, nationality_validate, nationality_show) VALUES ('PREF0001', 7, 0.00, 7, 0, 0, NULL, 20, 8, NULL, '0', '1', 1, 1, '0', '1', '1', 0, 'N', 'Custom Field 1', 'Custom Field 2', 'Custom Field 3',NULL, NULL, 'Patient Category', 'Patient Sourcing Category', NULL, NULL, NULL, NULL, 'Old Reg', 'N', 'N', NULL, NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'N', 'N', 'OP-IP Conversion', NULL, NULL, NULL, NULL, NULL, 'N', 'N', 'N', NULL, NULL, NULL, 'N', 'N', NULL, NULL, 'N', 'N', 'N', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', 'N', 'M', 'M', 'M', 'N', 'N', 'N', NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL, NULL, NULL, 'RQ', 'RQ', NULL, NULL, 'N', 'M', NULL, 'N', 'M', NULL, 'N', 'M', NULL, 'N', 'M', NULL, 'N', 'M', 'N', 'N', 'D', 'N', 'Membership ID', 'Validity Start', 'Validity End', 'N', 'Blood Group', 'Religion', 'Occupation', NULL, 'M', 'N', NULL, 'M', 'N', NULL, 'M', 'N', NULL, NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'N', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'D', 'D', 'D', 'N', 'N', 'N', NULL, NULL, NULL, 'N', 'N', 'N', NULL, NULL, NULL, 'D', 'D', 'D', 'N', 'N', 'N', NULL, NULL, NULL, 'N', 'N', 'N', 'M', 'M', 'M', 'M', 'M', 'M', 'M', 'M', 'M', 'M', 'M', 3, 'N', NULL, 'N', NULL, 'N', 'N', 'Y', NULL, 'V', 'Y', NULL, 'A', 'M');

--
-- Data for Name: rejection_reason_categories; Type: TABLE DATA; 
--

--
-- Data for Name: relation_master; Type: TABLE DATA; 
--

INSERT INTO relation_master (relation_id, relation__name) VALUES ('RELA0001', 'S/O');
INSERT INTO relation_master (relation_id, relation__name) VALUES ('RELA0002', 'D/O');
INSERT INTO relation_master (relation_id, relation__name) VALUES ('RELA0003', 'W/O');
INSERT INTO relation_master (relation_id, relation__name) VALUES ('RELA0004', 'C/O');

--
-- Data for Name: reward_points_earnings; Type: TABLE DATA; 
--

--
-- Data for Name: reward_points_status; Type: TABLE DATA; 
--

--
-- Data for Name: roster_resource_master; Type: TABLE DATA; 
--

--
-- Data for Name: roster_resource_type_master; Type: TABLE DATA; 
--

INSERT INTO roster_resource_type_master (resource_type_id, resource_type, status) VALUES (1, 'Counter', 'A');

--
-- Data for Name: store_sales_details; Type: TABLE DATA; 
--

--
-- Data for Name: sales_claim_details; Type: TABLE DATA; 
--

--
-- Data for Name: sales_claim_tax_details; Type: TABLE DATA; 
--

--
-- Data for Name: salutation_master; Type: TABLE DATA; 
--

INSERT INTO salutation_master (salutation_id, salutation, status, gender) VALUES ('SALU0001', ' ', 'A', NULL);
INSERT INTO salutation_master (salutation_id, salutation, status, gender) VALUES ('SALU0002', 'Mr.', 'A', 'M');
INSERT INTO salutation_master (salutation_id, salutation, status, gender) VALUES ('SALU0003', 'Mrs.', 'A', 'F');
INSERT INTO salutation_master (salutation_id, salutation, status, gender) VALUES ('SALU0004', 'Ms.', 'A', 'F');
INSERT INTO salutation_master (salutation_id, salutation, status, gender) VALUES ('SALU0005', 'Master', 'A', 'M');
INSERT INTO salutation_master (salutation_id, salutation, status, gender) VALUES ('SALU0006', 'Miss', 'A', 'F');
INSERT INTO salutation_master (salutation_id, salutation, status, gender) VALUES ('SALU0007', 'Dr.', 'A', NULL);
INSERT INTO salutation_master (salutation_id, salutation, status, gender) VALUES ('SALU0008', 'Baby', 'A', NULL);

--
-- Data for Name: sample_bar_code_print_templates; Type: TABLE DATA; 
--

INSERT INTO sample_bar_code_print_templates (template_name, print_template_content, user_name, reason) VALUES ('Sample Collection Bar Code', '', 'InstaAdmin', 'Sample Collection Bar Code');

--
-- Data for Name: sample_collection; Type: TABLE DATA; 
--

--
-- Data for Name: sample_collection_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: sample_collection_centers; Type: TABLE DATA; 
--

INSERT INTO sample_collection_centers (collection_center_id, collection_center, status, center_id) VALUES (-1, 'In House', 'A', 0);

--
-- Data for Name: sample_rejections; Type: TABLE DATA; 
--

--
-- Data for Name: sample_sources; Type: TABLE DATA; 
--

--
-- Data for Name: sample_type; Type: TABLE DATA; 
--

--
-- Data for Name: sample_type_number_prefs; Type: TABLE DATA; 
--

--
-- Data for Name: saved_searches; Type: TABLE DATA; 
--

INSERT INTO saved_searches (flow_id, search_id, search_type, search_name, is_default, query_params, created_by, updated_by, created_time, mod_time, display_order) VALUES ('OP Flow', 1, 'System', 'Today''s OP Patients', true, 'appointment_date=today&appointment_date=today&visit_date=today&visit_date=today&visit_type=o', 'InstaAdmin', 'InstaAdmin', '2017-10-05 13:46:21', '2017-10-05 13:46:21', 1);
INSERT INTO saved_searches (flow_id, search_id, search_type, search_name, is_default, query_params, created_by, updated_by, created_time, mod_time, display_order) VALUES ('OP Flow', 2, 'System', 'Recent Patients', false, 'visit_date=last_week&visit_date=today&visit_type=o', 'InstaAdmin', 'InstaAdmin', '2017-10-05 13:46:21', '2017-10-05 13:46:21', 4);
INSERT INTO saved_searches (flow_id, search_id, search_type, search_name, is_default, query_params, created_by, updated_by, created_time, mod_time, display_order) VALUES ('OP Flow', 3, 'System', 'All Patients with visit(s)', false, '', 'InstaAdmin', 'InstaAdmin', '2017-10-05 13:46:21', '2017-10-05 13:46:21', 5);
INSERT INTO saved_searches (flow_id, search_id, search_type, search_name, is_default, query_params, created_by, updated_by, created_time, mod_time, display_order) VALUES ('OP Flow', 4, 'System', 'Today''s Patients for Consultation', false, 'visit_date=today&visit_date=today&visit_type=o&consultations_only=Y', 'InstaAdmin', 'InstaAdmin', '2017-10-05 13:46:21', '2017-10-05 13:46:21', 2);
INSERT INTO saved_searches (flow_id, search_id, search_type, search_name, is_default, query_params, created_by, updated_by, created_time, mod_time, display_order) VALUES ('IP Flow', 5, 'System', 'My Patients', false, 'doctor=#loggedInDoctor#', 'InstaAdmin', 'InstaAdmin', '2017-10-05 13:46:28', '2017-10-05 13:46:28', 0);
INSERT INTO saved_searches (flow_id, search_id, search_type, search_name, is_default, query_params, created_by, updated_by, created_time, mod_time, display_order) VALUES ('IP Flow', 6, 'System', 'My Ward', false, '', 'InstaAdmin', 'InstaAdmin', '2017-10-05 13:46:33', '2017-10-05 13:46:33', 1);
INSERT INTO saved_searches (flow_id, search_id, search_type, search_name, is_default, query_params, created_by, updated_by, created_time, mod_time, display_order) VALUES ('IP Flow', 7, 'System', 'Active Patients', true, '', 'InstaAdmin', 'InstaAdmin', '2017-10-05 13:46:38', '2017-10-05 13:46:38', 1);

--
-- Data for Name: sch_default_res_availability_details; Type: TABLE DATA; 
--

INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 0, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 1, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 2, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 3, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 4, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 5, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 6, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 0, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 1, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 2, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 3, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 4, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 5, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 6, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 0, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 1, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 2, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 3, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 4, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 5, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 6, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 0, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 1, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 2, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 3, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 4, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 5, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 6, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 0, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 1, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 2, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 3, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 4, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 5, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (2, 6, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 0, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 1, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 2, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 3, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 4, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 5, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 6, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 0, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 1, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 2, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 3, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 4, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 5, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 6, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 0, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 1, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 2, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 3, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 4, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 5, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 6, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 0, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 1, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 2, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 3, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 4, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 5, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 6, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 0, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 1, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 2, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 3, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 4, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 5, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (4, 6, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 0, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 1, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 2, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 3, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 4, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 5, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 6, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 0, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 1, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 2, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 3, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 4, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 5, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 6, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 0, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 1, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 2, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 3, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 4, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 5, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 6, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 0, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 1, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 2, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 3, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 4, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 5, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 6, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 0, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 1, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 2, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 3, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 4, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 5, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (3, 6, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 0, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 1, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 2, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 3, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 4, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 5, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 6, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 0, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 1, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 2, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 3, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 4, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 5, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 6, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 0, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 1, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 2, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 3, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 4, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 5, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 6, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 0, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 1, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 2, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 3, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 4, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 5, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 6, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 0, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 1, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 2, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 3, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 4, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 5, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (1, 6, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 0, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 1, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 2, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 3, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 4, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 5, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 6, '00:00:00', '08:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 0, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 1, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 2, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 3, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 4, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 5, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 6, '08:00:00', '13:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 0, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 1, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 2, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 3, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 4, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 5, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 6, '13:00:00', '16:00:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 0, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 1, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 2, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 3, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 4, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 5, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 6, '16:00:00', '21:00:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 0, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 1, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 2, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 3, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 4, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 5, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (5, 6, '21:00:00', '23:59:00', 'N', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (7, 0, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (7, 1, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (7, 2, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (7, 3, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (7, 4, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (7, 5, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (7, 6, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (8, 0, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (8, 1, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (8, 2, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (8, 3, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (8, 4, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (8, 5, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (8, 6, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (9, 0, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (9, 1, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (9, 2, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (9, 3, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (9, 4, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (9, 5, '00:00:00', '23:59:00', 'A', '', 0);
INSERT INTO sch_default_res_availability_details (res_sch_id, day_of_week, from_time, to_time, availability_status, remarks, center_id) VALUES (9, 6, '00:00:00', '23:59:00', 'A', '', 0);

--
-- Data for Name: sch_resource_availability; Type: TABLE DATA; 
--

--
-- Data for Name: sch_resource_availability_details; Type: TABLE DATA; 
--

--
-- Data for Name: scheduled_export_prefs; Type: TABLE DATA; 
--

--
-- Data for Name: scheduler_appointment_items; Type: TABLE DATA; 
--

--
-- Data for Name: scheduler_appointment_items_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: scheduler_master; Type: TABLE DATA; 
--

INSERT INTO scheduler_master (res_sch_id, res_sch_category, dept, res_sch_name, description, default_duration, height_in_px, status, res_sch_type) VALUES (2, 'OPE', '*', '*', 'for all surgeries', 30, 30, 'A', 'SUR');
INSERT INTO scheduler_master (res_sch_id, res_sch_category, dept, res_sch_name, description, default_duration, height_in_px, status, res_sch_type) VALUES (4, 'DIA', '*', '*', 'for all tests', 30, 30, 'A', 'TST');
INSERT INTO scheduler_master (res_sch_id, res_sch_category, dept, res_sch_name, description, default_duration, height_in_px, status, res_sch_type) VALUES (3, 'SNP', '*', '*', 'for all services&procedures', 30, 30, 'A', 'SER');
INSERT INTO scheduler_master (res_sch_id, res_sch_category, dept, res_sch_name, description, default_duration, height_in_px, status, res_sch_type) VALUES (1, 'DOC', '*', '*', 'for all doctors across the department', 15, 15, 'A', 'DOC');
INSERT INTO scheduler_master (res_sch_id, res_sch_category, dept, res_sch_name, description, default_duration, height_in_px, status, res_sch_type) VALUES (6, '*', '*', '*', 'for all catgories', 5, 5, 'A', '*');
INSERT INTO scheduler_master (res_sch_id, res_sch_category, dept, res_sch_name, description, default_duration, height_in_px, status, res_sch_type) VALUES (7, 'OPE', '*', '*', 'for all theatres', 15, 15, 'A', 'THID');
INSERT INTO scheduler_master (res_sch_id, res_sch_category, dept, res_sch_name, description, default_duration, height_in_px, status, res_sch_type) VALUES (8, 'DIA', '*', '*', 'for all test equipments', 15, 15, 'A', 'EQID');
INSERT INTO scheduler_master (res_sch_id, res_sch_category, dept, res_sch_name, description, default_duration, height_in_px, status, res_sch_type) VALUES (9, 'SNP', '*', '*', 'for all Service Resources', 15, 15, 'A', 'SRID');

--
-- Data for Name: scheduler_appointments; Type: TABLE DATA; 
--

--
-- Data for Name: scheduler_appointments_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: scheduler_item_master; Type: TABLE DATA; 
--

INSERT INTO scheduler_item_master (res_sch_id, resource_type, resource_id) VALUES (1, 'Room', '*');
INSERT INTO scheduler_item_master (res_sch_id, resource_type, resource_id) VALUES (2, 'SUDOC', '*');
INSERT INTO scheduler_item_master (res_sch_id, resource_type, resource_id) VALUES (2, 'ANEDOC', '*');
INSERT INTO scheduler_item_master (res_sch_id, resource_type, resource_id) VALUES (2, 'THID', '*');
INSERT INTO scheduler_item_master (res_sch_id, resource_type, resource_id) VALUES (3, 'EQID', '*');
INSERT INTO scheduler_item_master (res_sch_id, resource_type, resource_id) VALUES (4, 'EQID', '*');
INSERT INTO scheduler_item_master (res_sch_id, resource_type, resource_id) VALUES (4, 'LABTECH', '*');

--
-- Data for Name: scheduler_resource_types; Type: TABLE DATA; 
--

INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('DIA', 'EQID', 'Equipment', true, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('DIA', 'LABTECH', 'Technician/Radiologist', false, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('DOC', 'EQID', 'Equipment', false, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('DOC', 'OPDOC', 'Doctor', true, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('OPE', 'SUDOC', 'Surgeon', false, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('OPE', 'ANEDOC', 'Anesthetist', false, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('OPE', 'THID', 'Operation Theatre', true, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('OPE', 'EQID', 'Equipment', false, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('SNP', 'DOC', 'Doctor', false, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('SNP', 'SRID', 'Service Resource', true, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('OPE', 'SRID', 'Service Resource', false, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('DIA', 'TST', 'Test', false, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('SNP', 'SER', 'Service', false, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('OPE', 'SUR', 'Surgery', false, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('OPE', 'ASUDOC', 'Asst Surgeon', false, NULL);
INSERT INTO scheduler_resource_types (category, resource_type, resource_description, primary_resource, resource_group) VALUES ('OPE', 'PAEDDOC', 'Paediatrician', false, NULL);

--
-- Data for Name: scheduler_status; Type: TABLE DATA; 
--

INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DOC', 'Booked', 'Booked');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DOC', 'Confirmed', 'Appt Confirmed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DOC', 'Arrived', 'Arrived');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DOC', 'Noshow', 'No Show');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DOC', 'Cancel', 'Cancel');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DOC', 'Completed', 'Completed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('OPE', 'Booked', 'Booked');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('OPE', 'Confirmed', 'Appt Confirmed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('OPE', 'Arrived', 'Arrived');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('OPE', 'OPIP', 'OP/IP Regn');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('OPE', 'Noshow', 'No Show');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('OPE', 'Cancel', 'Cancel');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('OPE', 'Completed', 'Completed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('OPE', 'SurgeryConfirm', 'Surgery Confirmed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('BED', 'Booked', 'Booked');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('BED', 'Confirmed', 'Appt Confirmed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('BED', 'Arrived', 'Arrived');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('BED', 'OPIP', 'OP/IP Regn');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('BED', 'Noshow', 'No Show');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('BED', 'Cancel', 'Cancel');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('BED', 'Completed', 'Completed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('BED', 'BedAlloted', 'Bed Allocated');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('SNP', 'Booked', 'Booked');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('SNP', 'Confirmed', 'Appt Confirmed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('SNP', 'Arrived', 'Arrived');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('SNP', 'OPIP', 'OP/IP Regn');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('SNP', 'Noshow', 'No Show');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('SNP', 'Cancel', 'Cancel');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('SNP', 'Completed', 'Completed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('SNP', 'Serviceconfirm', 'Service Confirmed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DIA', 'Booked', 'Booked');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DIA', 'Confirmed', 'Appt Confirmed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DIA', 'Arrived', 'Arrived');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DIA', 'OPIP', 'OP/IP Regn');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DIA', 'Noshow', 'No Show');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DIA', 'Cancel', 'Cancel');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DIA', 'Completed', 'Completed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DIA', 'Testconfirm', 'Test Confirmed');
INSERT INTO scheduler_status (category, status_name, status_description) VALUES ('DOC', 'OP', 'OP Regn');

--
-- Data for Name: score_card_details; Type: TABLE DATA; 
--

--
-- Data for Name: score_card_main; Type: TABLE DATA; 
--

--
-- Data for Name: screen_group_rights; Type: TABLE DATA; 
--

INSERT INTO screen_group_rights (role_id, group_id, rights) VALUES (3, 'grp_billing', 'A');
INSERT INTO screen_group_rights (role_id, group_id, rights) VALUES (4, 'grp_registration', 'A');
INSERT INTO screen_group_rights (role_id, group_id, rights) VALUES (6, 'grp_speciality_treatment', 'A');
INSERT INTO screen_group_rights (role_id, group_id, rights) VALUES (6, 'grp_discharge_summary', 'A');
INSERT INTO screen_group_rights (role_id, group_id, rights) VALUES (5, 'grp_speciality_treatment', 'A');
INSERT INTO screen_group_rights (role_id, group_id, rights) VALUES (5, 'grp_discharge_summary', 'A');

--
-- Data for Name: screen_rights; Type: TABLE DATA; 
--

INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'bill_audit_log', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'billing_duplicate_bill', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'billing_tally_export', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'change_billtype', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'credit_bill_collection', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'deposits_realization', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'new_prepaid_bills', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'patient_deposits', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'receipts', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'search_bills', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'transaction_approval', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (4, 'folowup_details', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (4, 'prescription', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (4, 'reg_general', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (4, 'reg_quick_estimate', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (4, 'reg_re_admit', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (5, 'discharge_summary', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (5, 'emr_patient_search', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (5, 'grp_speciality_treatment', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (5, 'rep_starttreatment', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (5, 'rep_treatment', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (6, 'discharge_summary', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (6, 'emr_patient_search', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (6, 'rep_starttreatment', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (6, 'rep_treatment', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (4, 'edit_visit_details', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (4, 'order', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'patient_details_search', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (4, 'patient_details_search', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (5, 'patient_details_search', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (6, 'patient_details_search', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'visit_details_search', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (4, 'visit_details_search', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (5, 'visit_details_search', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (6, 'visit_details_search', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'change_billprimary', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'bill_print', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'receipt_print', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'deposit_receipts', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (3, 'bill_email', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (4, 'new_op_registration', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (5, 'new_discharge_summary', 'A');
INSERT INTO screen_rights (role_id, screen_id, rights) VALUES (6, 'new_discharge_summary', 'A');

--
-- Data for Name: search_parameters; Type: TABLE DATA; 
--

--
-- Data for Name: secondary_complaints; Type: TABLE DATA; 
--

--
-- Data for Name: secondary_complaints_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: section_field_desc; Type: TABLE DATA; 
--

INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (1, 1, 1, 'Location', 'dropdown', 'Y', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (2, 1, 2, 'Quality', 'dropdown', 'Y', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (3, 1, 3, 'Duration', 'dropdown', 'Y', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (4, 1, 4, 'Timing', 'dropdown', 'Y', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (5, 1, 5, 'Context', 'dropdown', 'Y', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (6, 1, 6, 'Modifying Factors', 'dropdown', 'Y', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (7, 1, 7, 'Severity', 'dropdown', 'Y', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (8, 1, 8, 'Associated Signs & Symptoms', 'dropdown', 'Y', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (9, 2, 1, 'Constitutional', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (10, 2, 2, 'Eyes', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (11, 2, 3, 'ENT', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (12, 2, 4, 'Cardiovascular', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (13, 2, 5, 'Respiratory', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (14, 2, 6, 'Gastrointestinal', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (15, 2, 7, 'Genitourinary', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (16, 2, 8, 'Musculoskeletal', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (17, 2, 9, 'Integumentary', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (18, 2, 10, 'Neurological', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (19, 2, 11, 'Psychiatric', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (20, 2, 12, 'Endocrine', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (21, 2, 13, 'Hematologic/Lymphatic', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (22, 2, 14, 'Allergic/Immunologic', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (23, 3, 1, 'Constitutional', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (24, 3, 2, 'Eyes', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (25, 3, 3, 'ENT', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (26, 3, 4, 'Cardiovascular', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (27, 3, 5, 'Respiratory', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (28, 3, 6, 'Gastrointestinal', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (29, 3, 7, 'Genitourinary', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (30, 3, 8, 'Musculoskeletal', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (31, 3, 9, 'Integumentary', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (32, 3, 10, 'Neurological', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (33, 3, 11, 'Psychiatric', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (34, 3, 12, 'Endocrine', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (35, 3, 13, 'Hematologic/Lymphatic/Immuno', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (36, 3, 14, 'Allergic/Immunologic', 'checkbox', 'Y', 'Y', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (37, 4, 1, 'Past Medical History', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (38, 4, 2, 'Family History', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (39, 4, 3, 'Social History', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (40, 5, 1, 'Nurse Assessment', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (41, 1, 9, 'Additional Complaint', 'text', 'N', 'N', '', NULL, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (42, 6, 1, 'Skin Preparation', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (43, 6, 2, 'Incision', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (44, 6, 3, 'Finding', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (45, 6, 4, 'Procedure', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (46, 6, 5, 'Specimen removed', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (47, 6, 6, 'Blood Loss', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (48, 6, 7, 'Drains', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (49, 6, 8, 'Complications', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (50, 6, 9, 'Post Operative findings', 'text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');
INSERT INTO section_field_desc (field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, observation_type, observation_code, file_content, content_type, filename, markers, is_mandatory, use_in_presenting_complaint, phrase_category_id, pattern_id, old_field_id, default_to_current_datetime) VALUES (51, 7, 1, 'Notes', 'wide text', 'N', 'N', '', 3, 'A', NULL, NULL, NULL, NULL, NULL, NULL, false, 'N', NULL, NULL, NULL, 'N');

--
-- Data for Name: section_field_options; Type: TABLE DATA; 
--

INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (1, 1, 0, 'Scalp', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (2, 1, 0, 'Face', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (3, 1, 0, 'Neck', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (4, 1, 0, 'Throat', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (5, 1, 0, 'Eyes', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (6, 1, 0, 'Ears', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (7, 1, 0, 'Nose', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (8, 1, 0, 'Teeth', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (9, 1, 0, 'Teeth', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (10, 1, 0, 'Tongue', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (11, 1, 0, 'Lips', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (12, 1, 0, 'Head', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (13, 1, 0, 'Chest', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (14, 1, 0, 'Abdomen', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (15, 1, 0, 'Uterus', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (16, 1, 0, 'Cervical Spine', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (17, 1, 0, 'Lubosacral Spine', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (18, 1, 0, 'Shoulders', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (19, 1, 0, 'Hand', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (20, 1, 0, 'Wrist', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (21, 1, 0, 'Fingers', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (22, 1, 0, 'Pelvis', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (23, 1, 0, 'Legs', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (24, 1, 0, 'Knee', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (25, 1, 0, 'Foot', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (26, 1, 0, 'Ankle', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (27, 1, 0, 'Toes', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (28, 2, 0, 'Sharp', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (29, 2, 0, 'Throbbing', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (30, 2, 0, 'Burning', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (31, 2, 0, 'Radiating', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (32, 2, 0, 'Others', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (33, 3, 0, 'Yesterday', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (34, 3, 0, '2 Days ago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (35, 3, 0, '4 Days ago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (36, 3, 0, '6 Days ago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (37, 3, 0, '1 Week ago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (38, 3, 0, '2 Weeks ago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (39, 3, 0, '3 Weeks ago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (40, 3, 0, '4 Weeks ago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (41, 3, 0, '2 Months ago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (42, 3, 0, '3 Months ago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (43, 3, 0, '4 Months ago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (44, 3, 0, '6 Months ago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (45, 4, 0, 'Constant', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (46, 4, 0, 'Frequent', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (47, 4, 0, 'Intermittent', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (48, 5, 0, 'Fall', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (49, 5, 0, 'Lifting Heavy Object', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (50, 6, 0, 'Better when heat is applied', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (51, 6, 0, 'Better when ice pack is applied', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (52, 6, 0, 'Better when lying down', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (53, 7, 0, 'Mild', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (54, 7, 0, 'Moderate', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (55, 7, 0, 'Severe', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (56, 7, 0, 'Pain scale 2/10', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (57, 7, 0, 'Pain scale 4/10', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (58, 7, 0, 'Pain scale 6/10', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (59, 7, 0, 'Pain scale 8/10', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (60, 7, 0, 'Pain scale 10/10', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (61, 8, 0, 'Numbness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (62, 8, 0, 'Weakness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (63, 8, 0, 'Dizziness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (64, 8, 0, 'Vomiting', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (65, 8, 0, 'Nausea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (66, 8, 0, 'Fever', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (67, 8, 0, 'Cough', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (68, 8, 0, 'Headache', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (69, 8, 0, 'Myalgia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (70, 8, 0, 'Abdominal Pain', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (71, 9, 0, 'Fever', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (72, 9, 0, 'Chills', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (73, 9, 0, 'Night Sweats', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (74, 9, 0, 'Fatigue', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (75, 10, 0, 'Blurred Vision', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (76, 10, 0, 'Eye Pain', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (77, 10, 0, 'Eye Discharge', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (78, 10, 0, 'Dry Eyes', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (79, 10, 0, 'Decreased Vision', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (80, 11, 0, 'Sore Throat', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (81, 11, 0, 'Nose Bleed', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (82, 11, 0, 'Decreased Hearing', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (83, 11, 0, 'Ear Pain', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (84, 11, 0, 'Ear Discharge', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (85, 12, 0, 'Chest Pain', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (86, 12, 0, 'Palpitation', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (87, 12, 0, 'Edema', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (88, 12, 0, 'Vertigo', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (89, 13, 0, 'Shortness of Breath', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (90, 13, 0, 'Cough', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (91, 13, 0, 'Wheezing', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (92, 14, 0, 'Nausea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (93, 14, 0, 'Vomiting', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (94, 14, 0, 'Excessive Thirst', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (95, 14, 0, 'Diarrhea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (96, 14, 0, 'Vomiting Blood', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (97, 14, 0, 'Blood in Stool', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (98, 14, 0, 'Constipation', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (99, 14, 0, 'Abdominal Pain', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (100, 15, 0, 'Hematuria', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (101, 15, 0, 'Dysuria', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (102, 15, 0, 'Urinary Frequency', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (103, 16, 0, 'Joint Pain', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (104, 16, 0, 'Muscle Pain', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (105, 16, 0, 'Muscle Weakness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (106, 16, 0, 'Joint Swelling', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (107, 17, 0, 'Rash', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (108, 17, 0, 'Dryness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (109, 17, 0, 'Itching', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (110, 17, 0, 'Sores', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (111, 17, 0, 'Nail Discoloration', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (112, 17, 0, 'Abscess', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (113, 18, 0, 'Headache', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (114, 18, 0, 'Numbness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (115, 18, 0, 'Lack of Coordination', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (116, 18, 0, 'Tremor', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (117, 18, 0, 'Vertigo', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (118, 19, 0, 'Depression', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (119, 19, 0, 'Anxiety', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (120, 19, 0, 'Alcohol Abuse', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (121, 19, 0, 'Drug Abuse', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (122, 19, 0, 'Sleeplessness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (123, 20, 0, 'Excessive Thirst', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (124, 20, 0, 'Cold Intolerance', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (125, 20, 0, 'Heat Intolerance', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (126, 21, 0, 'Swollen Glands', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (127, 21, 0, 'Easy Bruising', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (128, 21, 0, 'Blood Clots', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (129, 22, 0, 'Allergic Rhinitis', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (130, 22, 0, 'Asthma', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (131, 22, 0, 'Hives', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (132, 23, 0, 'Fever', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (133, 23, 0, 'Chills', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (134, 23, 0, 'Tremor', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (135, 23, 0, 'Weakness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (136, 24, 0, 'Eye Pain', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (137, 24, 0, 'Eye Discharge', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (138, 24, 0, 'Eye Dryness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (139, 24, 0, 'Eye Redness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (140, 24, 0, 'Myopia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (141, 24, 0, 'Hypermetropia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (142, 24, 0, 'Cataract', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (143, 24, 0, 'Diplopia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (144, 24, 0, 'Photophobia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (145, 25, 0, 'Otalgia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (146, 25, 0, 'Otorrhea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (147, 25, 0, 'Dizziness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (148, 25, 0, 'Tinnitus', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (149, 25, 0, 'Deafness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (150, 25, 0, 'Cold', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (151, 25, 0, 'Epistaxis', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (152, 25, 0, 'Nasal Congestion', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (153, 25, 0, 'Sneezing', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (154, 25, 0, 'Rhinorrhea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (155, 25, 0, 'Sinusitis', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (156, 25, 0, 'Disphagia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (157, 25, 0, 'Sore Throat', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (158, 26, 0, 'Chest Pain', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (159, 26, 0, 'Ankle Swelling', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (160, 26, 0, 'Palpitation', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (161, 26, 0, 'Tachycardia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (162, 26, 0, 'Bradychardia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (163, 26, 0, 'Dyspnea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (164, 26, 0, 'Vertigo', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (165, 26, 0, 'Cardiac Murmur', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (166, 26, 0, 'Hypertension', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (167, 26, 0, 'Hypotension', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (168, 27, 0, 'Shortness of Breath', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (169, 27, 0, 'Wheezing', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (170, 27, 0, 'Dyspnea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (171, 27, 0, 'Cough', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (172, 27, 0, 'Apnea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (173, 27, 0, 'Asthma', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (174, 28, 0, 'Nausea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (175, 28, 0, 'Vomiting', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (176, 28, 0, 'Diarrhea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (177, 28, 0, 'Melena', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (178, 28, 0, 'Abdominal Cramping', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (179, 28, 0, 'Belching', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (180, 28, 0, 'Constipation', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (181, 28, 0, 'Appetite', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (182, 28, 0, 'Abdominal Pain', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (183, 29, 0, 'Amenorrhea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (184, 29, 0, 'Leukorrhea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (185, 29, 0, 'Dysmenorrhea', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (186, 29, 0, 'Mastodynia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (187, 29, 0, 'Testicular Pain', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (188, 29, 0, 'Penile Discharge', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (189, 29, 0, 'Hematuria', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (190, 29, 0, 'Urinary Incontinence', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (191, 29, 0, 'Urinary Frequency', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (192, 29, 0, 'Renal Colic', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (193, 29, 0, 'Nocturia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (194, 30, 0, 'Lumbago', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (195, 30, 0, 'Cervicalgia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (196, 30, 0, 'Arthralgia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (197, 30, 0, 'Stiffness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (198, 30, 0, 'Swelling', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (199, 30, 0, 'Myalgia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (200, 30, 0, 'Pain in the Limb', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (201, 30, 0, 'Edema', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (202, 31, 0, 'Rash', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (203, 31, 0, 'Sunburn', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (204, 31, 0, 'Urticaria', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (205, 31, 0, 'Discoloration', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (206, 31, 0, 'Hair Loss', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (207, 31, 0, 'Itching', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (208, 31, 0, 'Warts', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (209, 31, 0, 'Pallor', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (210, 32, 0, 'Headache', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (211, 32, 0, 'Numbness', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (212, 32, 0, 'Lack of Coordination', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (213, 32, 0, 'Tremor', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (214, 32, 0, 'Vertigo', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (215, 33, 0, 'Anxiety', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (216, 33, 0, 'Depression', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (217, 33, 0, 'Mood Disorders', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (218, 33, 0, 'Insomnia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (219, 34, 0, 'Excessive Thirst', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (220, 34, 0, 'Cold Intolerance', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (221, 34, 0, 'Heat Intolerance', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (222, 35, 0, 'Enalrgement of Lymph Nodes', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (223, 35, 0, 'Anemia', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (224, 35, 0, 'Bruising', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (225, 35, 0, 'Allergy', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (226, 36, 0, 'Allergic Rhinitis', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (227, 36, 0, 'Asthma', 'A', NULL, NULL, NULL, NULL);
INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code, phrase_category_id, pattern_id, old_option_id) VALUES (228, 36, 0, 'Hives', 'A', NULL, NULL, NULL, NULL);

--
-- Data for Name: section_master; Type: TABLE DATA; 
--

INSERT INTO section_master (section_id, section_title, allow_all_normal, linked_to, status, section_mandatory, allow_duplicate) VALUES (4, 'Personal, Family & Social History', 'N', 'patient', 'A', false, false);
INSERT INTO section_master (section_id, section_title, allow_all_normal, linked_to, status, section_mandatory, allow_duplicate) VALUES (1, 'History of Present Illness', 'N', 'order item', 'A', false, false);
INSERT INTO section_master (section_id, section_title, allow_all_normal, linked_to, status, section_mandatory, allow_duplicate) VALUES (2, 'Review Of Systems', 'Y', 'order item', 'A', false, false);
INSERT INTO section_master (section_id, section_title, allow_all_normal, linked_to, status, section_mandatory, allow_duplicate) VALUES (3, 'Physical Examination', 'Y', 'order item', 'A', false, false);
INSERT INTO section_master (section_id, section_title, allow_all_normal, linked_to, status, section_mandatory, allow_duplicate) VALUES (5, 'Nurse Assessment', 'N', 'order item', 'A', false, false);
INSERT INTO section_master (section_id, section_title, allow_all_normal, linked_to, status, section_mandatory, allow_duplicate) VALUES (6, 'Operation Notes', 'N', 'order item', 'A', false, false);
INSERT INTO section_master (section_id, section_title, allow_all_normal, linked_to, status, section_mandatory, allow_duplicate) VALUES (7, 'Conduction Notes', 'N', 'order item', 'A', false, false);

--
-- Data for Name: service_consumable_usage; Type: TABLE DATA; 
--

--
-- Data for Name: service_consumables; Type: TABLE DATA; 
--

--
-- Data for Name: service_documents; Type: TABLE DATA; 
--

--
-- Data for Name: service_groups; Type: TABLE DATA; 
--

INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (1, 'A', 'Direct Charge', 1, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (2, 'A', 'Bed', 2, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (3, 'A', 'ICU', 3, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (5, 'A', 'Laboratory', 5, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (6, 'A', 'Radiology', 6, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (7, 'A', 'Operation', 7, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (8, 'A', 'Service', 8, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (9, 'A', 'Equipment', 9, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (10, 'A', 'Meal', 10, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (11, 'A', 'Other Charge', 11, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (12, 'A', 'Package', 12, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (14, 'A', 'Inventory item', 14, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (15, 'A', 'Pharmacy item', 15, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (0, 'A', 'Others', 1000, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (13, 'A', 'Pharmacy Bill', 13, 'InstaAdmin', '2011-04-26 12:42:30', NULL);
INSERT INTO service_groups (service_group_id, status, service_group_name, display_order, username, mod_time, service_group_code) VALUES (-1, 'A', 'Doctor', 4, 'InstaAdmin', '2011-04-26 12:42:30', NULL);

--
-- Data for Name: service_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: services; Type: TABLE DATA; 
--

--
-- Data for Name: service_master_charges; Type: TABLE DATA; 
--

--
-- Data for Name: service_master_charges_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: service_master_charges_backup; Type: TABLE DATA; 
--

--
-- Data for Name: service_org_details; Type: TABLE DATA; 
--

--
-- Data for Name: service_payment_master; Type: TABLE DATA; 
--

--
-- Data for Name: service_reports; Type: TABLE DATA; 
--

--
-- Data for Name: service_resource_master; Type: TABLE DATA; 
--

--
-- Data for Name: service_sub_groups; Type: TABLE DATA; 
--

INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (1, 'A', 1, 'Direct Charge', 1, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (2, 'A', 2, 'Bed', 2, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (3, 'A', 3, 'ICU', 2, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (5, 'A', 5, 'Biochemistry', 5, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (18, 'A', 7, 'Radiology', 7, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (19, 'A', 7, 'Laboratory', 7, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (20, 'A', 7, 'Pharmacy', 7, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (21, 'A', 7, 'General', 7, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (22, 'A', 7, 'Anaesthesiology', 7, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (28, 'A', 9, 'General', 9, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (32, 'A', 10, 'Meal', 10, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (33, 'A', 11, 'Other Charge', 11, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (34, 'A', 12, 'Package', 12, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (35, 'A', 15, 'general', 16, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (0, 'A', 0, 'Others', 1000, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (36, 'A', 13, 'Pharmacy Bill', 13, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);
INSERT INTO service_sub_groups (service_sub_group_id, status, service_group_id, service_sub_group_name, display_order, username, mod_time, service_sub_group_code, account_head_id, eligible_to_earn_points, eligible_to_redeem_points, redemption_cap_percent) VALUES (-1, 'A', -1, 'Doctor', 3, 'InstaAdmin', '2011-04-26 12:42:30', NULL, NULL, 'N', 'N', NULL);

--
-- Data for Name: service_sub_tasks; Type: TABLE DATA; 
--

--
-- Data for Name: service_supplies_master; Type: TABLE DATA; 
--

--
-- Data for Name: service_units; Type: TABLE DATA; 
--

INSERT INTO service_units (units) VALUES ('Hrs');
INSERT INTO service_units (units) VALUES ('Days');
INSERT INTO service_units (units) VALUES ('Unit');

--
-- Data for Name: services_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: services_departments; Type: TABLE DATA; 
--

--
-- Data for Name: services_export_interface; Type: TABLE DATA; 
--

--
-- Data for Name: services_presc_tasks; Type: TABLE DATA; 
--

--
-- Data for Name: shift_master; Type: TABLE DATA; 
--

--
-- Data for Name: shift_resources; Type: TABLE DATA; 
--

--
-- Data for Name: shift_users; Type: TABLE DATA; 
--

--
-- Data for Name: sold_packages; Type: TABLE DATA; 
--

--
-- Data for Name: sold_packages_contents; Type: TABLE DATA; 
--

--
-- Data for Name: sponsor_approved_charges; Type: TABLE DATA; 
--

--
-- Data for Name: sponsor_print_templates; Type: TABLE DATA; 
--

--
-- Data for Name: sponsor_procedure_limit; Type: TABLE DATA; 
--

--
-- Data for Name: sponsor_type; Type: TABLE DATA; 
--

INSERT INTO sponsor_type (sponsor_type_id, sponsor_type_name, sponsor_type_description, sponsor_type_label, plan_type_label, member_id_show, member_id_mandatory, member_id_label, policy_id_show, policy_id_mandatory, validity_period_show, validity_period_mandatory, prior_auth_show, status, validity_period_editable, visit_limits_show) VALUES (1, 'Corporate Sponsor', 'Corporate Sponsors With Generic Insurance Mapping', '', 'Plan Type', 'Y', 'Y', 'Employee ID', 'Y', 'N', 'N', 'N', 'N', 'A', 'N', 'Y');
INSERT INTO sponsor_type (sponsor_type_id, sponsor_type_name, sponsor_type_description, sponsor_type_label, plan_type_label, member_id_show, member_id_mandatory, member_id_label, policy_id_show, policy_id_mandatory, validity_period_show, validity_period_mandatory, prior_auth_show, status, validity_period_editable, visit_limits_show) VALUES (2, 'National Sponsor', 'National Sponsors With Insurance Mapping', '', 'Plan Type', 'Y', 'Y', 'Member ID', 'Y', 'N', 'N', 'N', 'N', 'A', 'N', 'Y');
INSERT INTO sponsor_type (sponsor_type_id, sponsor_type_name, sponsor_type_description, sponsor_type_label, plan_type_label, member_id_show, member_id_mandatory, member_id_label, policy_id_show, policy_id_mandatory, validity_period_show, validity_period_mandatory, prior_auth_show, status, validity_period_editable, visit_limits_show) VALUES (3, 'Insurance Company', 'TPA / Insurance Companies', '', 'Network / Plan Type', 'Y', 'N', 'Member ID', 'Y', 'N', 'N', 'N', 'Y', 'A', 'N', 'Y');

--
-- Data for Name: state_master; Type: TABLE DATA; 
--

INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0001', 'ANDAMAN AND NICOBAR', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0002', 'ANDHRA PRADESH', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0003', 'ARUNACHAL PRADESH', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0004', 'ASSAM', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0005', 'BIHAR', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0006', 'CHANDIGARH', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0007', 'CHATTISGARH', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0008', 'DADRA AND NAGAR HAVELI', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0009', 'DAMAN AND DIU', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0010', 'DELHI', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0011', 'GOA', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0012', 'GUJARAT', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0013', 'HARYANA', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0014', 'HIMACHAL PRADESH', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0015', 'JAMMU AND KASHMIR', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0016', 'JHARKHAND', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0017', 'KARNATAKA', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0018', 'KERALA', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0019', 'LAKSHADWEEP', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0020', 'MADHYA PRADESH', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0021', 'MAHARASHTRA', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0022', 'MANIPUR', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0023', 'MEGHALAYA', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0024', 'MIZORAM', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0025', 'NAGALAND', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0026', 'ORISSA', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0027', 'PUDUCHERRY', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0028', 'PUNJAB', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0029', 'RAJASTHAN', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0030', 'SIKKIM', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0031', 'TAMIL NADU', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0032', 'TRIPURA', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0033', 'UTTARAKHAND', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0034', 'UTTAR PRADESH', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0035', 'WEST BENGAL', 'A', 'CM0105');
INSERT INTO state_master (state_id, state_name, status, country_id) VALUES ('ST0036', 'OTHER', 'A', 'CM0105');

--
-- Data for Name: stock_adjustment_reason_master; Type: TABLE DATA; 
--

--
-- Data for Name: store_gatepass; Type: TABLE DATA; 
--

INSERT INTO store_gatepass (gatepass_id, gatepass_no, txn_type, created_date, dept_id) VALUES (0, 'G0', '', NULL, 0);

--
-- Data for Name: store_type_master; Type: TABLE DATA; 
--

INSERT INTO store_type_master (store_type_id, store_type_name) VALUES (1, 'PHARMACY');
INSERT INTO store_type_master (store_type_id, store_type_name) VALUES (2, 'INVENTORY');
INSERT INTO store_type_master (store_type_id, store_type_name) VALUES (3, 'OTHERS');

--
-- Data for Name: stores; Type: TABLE DATA; 
--

INSERT INTO stores (dept_name, counter_id, status, pharmacy_tin_no, pharmacy_drug_license_no, template_name, account_group, dept_id, auto_fill_prescriptions, purchases_store_vat_account_prefix, purchases_store_cst_account_prefix, sales_store_vat_account_prefix, store_type_id, is_super_store, sale_unit, presc_template_name, presc_lbl_template_name, allowed_raise_bill, is_sales_store, center_id, auto_fill_indents, stock_timestamp, created_timestamp, updated_timestamp, is_sterile_store, store_rate_plan_id, use_batch_mrp, auto_po_generation_frequency_in_days, allow_auto_po_generation, last_auto_po_date, auto_cancel_po_frequency_in_days, allow_auto_cancel_po, last_auto_po_cancel_date, web_template_name, web_printer) VALUES ('CENTRAL STORE', 'CNT0002', 'A', NULL, NULL, 'BUILTIN_HTML', 1, 0, false, NULL, NULL, NULL, 1, 'Y', 'I', NULL, NULL, 'Y', 'Y', 0, false, 0, '2013-10-08 10:09:20.279391', '2013-10-08 10:09:20.445655', NULL, NULL, 'N', NULL, 'N', NULL, NULL, 'N', NULL, 'Web Based Pharmacy Template', 3);
INSERT INTO stores (dept_name, counter_id, status, pharmacy_tin_no, pharmacy_drug_license_no, template_name, account_group, dept_id, auto_fill_prescriptions, purchases_store_vat_account_prefix, purchases_store_cst_account_prefix, sales_store_vat_account_prefix, store_type_id, is_super_store, sale_unit, presc_template_name, presc_lbl_template_name, allowed_raise_bill, is_sales_store, center_id, auto_fill_indents, stock_timestamp, created_timestamp, updated_timestamp, is_sterile_store, store_rate_plan_id, use_batch_mrp, auto_po_generation_frequency_in_days, allow_auto_po_generation, last_auto_po_date, auto_cancel_po_frequency_in_days, allow_auto_cancel_po, last_auto_po_cancel_date, web_template_name, web_printer) VALUES ('INVENTORY CENTRAL STORE', NULL, 'A', NULL, NULL, NULL, 1, 1, false, NULL, NULL, NULL, 2, 'Y', 'I', NULL, NULL, 'Y', 'N', 0, false, 0, '2013-10-08 10:09:20.279391', '2013-10-08 10:09:20.445655', NULL, NULL, 'N', NULL, 'N', NULL, NULL, 'N', NULL, 'Web Based Pharmacy Template', 3);
INSERT INTO stores (dept_name, counter_id, status, pharmacy_tin_no, pharmacy_drug_license_no, template_name, account_group, dept_id, auto_fill_prescriptions, purchases_store_vat_account_prefix, purchases_store_cst_account_prefix, sales_store_vat_account_prefix, store_type_id, is_super_store, sale_unit, presc_template_name, presc_lbl_template_name, allowed_raise_bill, is_sales_store, center_id, auto_fill_indents, stock_timestamp, created_timestamp, updated_timestamp, is_sterile_store, store_rate_plan_id, use_batch_mrp, auto_po_generation_frequency_in_days, allow_auto_po_generation, last_auto_po_date, auto_cancel_po_frequency_in_days, allow_auto_cancel_po, last_auto_po_cancel_date, web_template_name, web_printer) VALUES ('Diag Store', NULL, 'A', NULL, NULL, NULL, 1, -1, false, NULL, NULL, NULL, 3, 'Y', 'I', NULL, NULL, 'Y', 'N', 0, false, 0, '2013-10-08 10:09:20.279391', '2013-10-08 10:09:20.445655', NULL, NULL, 'N', NULL, 'N', NULL, NULL, 'N', NULL, 'Web Based Pharmacy Template', 3);
INSERT INTO stores (dept_name, counter_id, status, pharmacy_tin_no, pharmacy_drug_license_no, template_name, account_group, dept_id, auto_fill_prescriptions, purchases_store_vat_account_prefix, purchases_store_cst_account_prefix, sales_store_vat_account_prefix, store_type_id, is_super_store, sale_unit, presc_template_name, presc_lbl_template_name, allowed_raise_bill, is_sales_store, center_id, auto_fill_indents, stock_timestamp, created_timestamp, updated_timestamp, is_sterile_store, store_rate_plan_id, use_batch_mrp, auto_po_generation_frequency_in_days, allow_auto_po_generation, last_auto_po_date, auto_cancel_po_frequency_in_days, allow_auto_cancel_po, last_auto_po_cancel_date, web_template_name, web_printer) VALUES ('Service Store', NULL, 'A', NULL, NULL, NULL, 1, -2, false, NULL, NULL, NULL, 3, 'Y', 'I', NULL, NULL, 'Y', 'N', 0, false, 0, '2013-10-08 10:09:20.279391', '2013-10-08 10:09:20.445655', NULL, NULL, 'N', NULL, 'N', NULL, NULL, 'N', NULL, 'Web Based Pharmacy Template', 3);
INSERT INTO stores (dept_name, counter_id, status, pharmacy_tin_no, pharmacy_drug_license_no, template_name, account_group, dept_id, auto_fill_prescriptions, purchases_store_vat_account_prefix, purchases_store_cst_account_prefix, sales_store_vat_account_prefix, store_type_id, is_super_store, sale_unit, presc_template_name, presc_lbl_template_name, allowed_raise_bill, is_sales_store, center_id, auto_fill_indents, stock_timestamp, created_timestamp, updated_timestamp, is_sterile_store, store_rate_plan_id, use_batch_mrp, auto_po_generation_frequency_in_days, allow_auto_po_generation, last_auto_po_date, auto_cancel_po_frequency_in_days, allow_auto_cancel_po, last_auto_po_cancel_date, web_template_name, web_printer) VALUES ('OT CONSUMABLE STORE', NULL, 'A', NULL, NULL, NULL, 1, -3, false, NULL, NULL, NULL, 3, 'Y', 'I', NULL, NULL, 'Y', 'N', 0, false, 0, '2013-10-08 10:09:20.279391', '2013-10-08 10:09:20.445655', NULL, NULL, 'N', NULL, 'N', NULL, NULL, 'N', NULL, 'Web Based Pharmacy Template', 3);

--
-- Data for Name: stock_issue_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_indent_main; Type: TABLE DATA; 
--

--
-- Data for Name: stock_issue_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_adj_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_adj_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_checkpoint_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_checkpoint_details; Type: TABLE DATA; 
--

--
-- Data for Name: supplier_category_master; Type: TABLE DATA; 
--

INSERT INTO supplier_category_master (supp_category_id, supp_category_name) VALUES (0, 'general');

--
-- Data for Name: supplier_master; Type: TABLE DATA; 
--

--
-- Data for Name: store_invoice; Type: TABLE DATA; 
--

--
-- Data for Name: store_consignment_invoice; Type: TABLE DATA; 
--

--
-- Data for Name: store_debit_note; Type: TABLE DATA; 
--

--
-- Data for Name: store_estimate_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_estimate_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_grn_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_grn_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_grn_tax_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_hosp_user; Type: TABLE DATA; 
--

--
-- Data for Name: store_indent_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_indent_main_status; Type: TABLE DATA; 
--

INSERT INTO store_indent_main_status (display_id, display_label, display_order) VALUES ('O', 'Open', 1);
INSERT INTO store_indent_main_status (display_id, display_label, display_order) VALUES ('A', 'Approved', 2);
INSERT INTO store_indent_main_status (display_id, display_label, display_order) VALUES ('C', 'Force Closed', 3);

--
-- Data for Name: store_issue_returns_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_issue_returns_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_item_batch_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_item_batch_details_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: store_item_codes; Type: TABLE DATA; 
--

--
-- Data for Name: store_item_controltype; Type: TABLE DATA; 
--

INSERT INTO store_item_controltype (control_type_id, control_type_name) VALUES (1, 'Normal');
INSERT INTO store_item_controltype (control_type_id, control_type_name) VALUES (2, 'ScheduleH');
INSERT INTO store_item_controltype (control_type_id, control_type_name) VALUES (3, 'ScheduleX');

--
-- Data for Name: store_item_issue_rates; Type: TABLE DATA; 
--

--
-- Data for Name: store_item_lot_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_item_rates; Type: TABLE DATA; 
--

--
-- Data for Name: store_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: store_item_timestamp; Type: TABLE DATA; 
--

INSERT INTO store_item_timestamp (item_timestamp) VALUES (1);

--
-- Data for Name: store_kit_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_kit_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_kit_stock_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_main_stock_timestamp; Type: TABLE DATA; 
--

INSERT INTO store_main_stock_timestamp (medicine_timestamp) VALUES (0);

--
-- Data for Name: store_miscellaneous_settings; Type: TABLE DATA; 
--

INSERT INTO store_miscellaneous_settings (pharmacy_tin_no, pharmacy_drug_license_no, hospital_terms_conditions, credit_period, indent_process_no, delivery) VALUES (NULL, NULL, NULL, NULL, NULL, NULL);

--
-- Data for Name: store_patient_indent_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_patient_indent_item_issue_no_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_patient_indent_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_po_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_po; Type: TABLE DATA; 
--

--
-- Data for Name: store_po_main_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: store_po_tax_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_preferences; Type: TABLE DATA; 
--

INSERT INTO store_preferences (goods_tax_rate, packaged_goods_tax_rate, scm_sync_last_updated_time) VALUES (5.5, 14.5, NULL);

--
-- Data for Name: store_print_template; Type: TABLE DATA; 
--

INSERT INTO store_print_template (template_name, pharmacy_template_content, user_name, reason, template_mode) VALUES ('Web Based Pharmacy Template', '', 'InstaAdmin', 'web sharing', 'H');

--
-- Data for Name: store_purchase_invoice_report_view; Type: TABLE DATA; 
--

--
-- Data for Name: store_rate_plans; Type: TABLE DATA; 
--

--
-- Data for Name: store_reagent_usage_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_reagent_usage_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_retail_customers; Type: TABLE DATA; 
--

--
-- Data for Name: store_retail_doctor; Type: TABLE DATA; 
--

--
-- Data for Name: store_retail_sponsors; Type: TABLE DATA; 
--

--
-- Data for Name: store_sales_tax_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_stock_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_stock_details_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: store_supplier_contracts; Type: TABLE DATA; 
--

--
-- Data for Name: store_supplier_contracts_center_applicability; Type: TABLE DATA; 
--

--
-- Data for Name: store_supplier_contracts_item_rates; Type: TABLE DATA; 
--

--
-- Data for Name: store_supplier_returns_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_supplier_returns; Type: TABLE DATA; 
--

--
-- Data for Name: store_transaction_lot_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_transfer_main; Type: TABLE DATA; 
--

--
-- Data for Name: store_transfer_details; Type: TABLE DATA; 
--

--
-- Data for Name: store_transfer_receive_indent; Type: TABLE DATA; 
--

--
-- Data for Name: strength_units; Type: TABLE DATA; 
--

--
-- Data for Name: supplier_center_master; Type: TABLE DATA; 
--

--
-- Data for Name: surgery_anesthesia_details; Type: TABLE DATA; 
--

--
-- Data for Name: survey_form; Type: TABLE DATA; 
--

--
-- Data for Name: survey_form_section; Type: TABLE DATA; 
--

--
-- Data for Name: survey_question_category_master; Type: TABLE DATA; 
--

--
-- Data for Name: survey_rating_details_master; Type: TABLE DATA; 
--

INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (1, 1, 'NA', 0);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (2, 1, 'Poor', 1);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (3, 1, 'Good', 2);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (4, 1, 'Excellent', 3);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (5, 2, 'NA', 0);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (6, 2, 'Very Dissatisfied', 1);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (7, 2, 'Somewhat Dissatisfied', 2);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (8, 2, 'Neither Dissatisfied not Satisfied', 3);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (9, 2, 'Somewhat Satisfied', 4);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (10, 2, 'Very Satisfied', 5);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (11, 3, 'NA', 0);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (12, 3, 'Very Dissatisfied', 1);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (13, 3, 'Dissatisfied', 2);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (14, 3, 'Does not meet Expectations', 3);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (15, 3, 'Neither Dissatisfied not Satisfied', 4);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (16, 3, 'Somewhat Satisfied', 5);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (17, 3, 'Meets Expectations', 6);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (18, 3, 'Exceeds Expectations', 7);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (19, 4, 'NA', 0);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (20, 4, 'Extremely Dissatisfied', 1);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (21, 4, 'Very Dissatisfied', 2);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (22, 4, 'Somewhat Dissatisfied', 3);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (23, 4, 'Mostly dissatisfied though few satisfactorymoments', 4);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (24, 4, 'Missed most of my expectations', 5);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (25, 4, 'Did not meet expectations', 6);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (26, 4, 'Not very satisfied but not unhappy', 7);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (27, 4, 'Somewhat Satisfied', 8);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (28, 4, 'Very Satisfied', 9);
INSERT INTO survey_rating_details_master (rating_id, rating_type_id, rating_text, rating_value) VALUES (29, 4, 'Extremely Satisfied', 10);

--
-- Data for Name: survey_rating_type_master; Type: TABLE DATA; 
--

INSERT INTO survey_rating_type_master (rating_type_id, rating_type, status) VALUES (1, 'Rating3', 'A');
INSERT INTO survey_rating_type_master (rating_type_id, rating_type, status) VALUES (2, 'Rating5', 'A');
INSERT INTO survey_rating_type_master (rating_type_id, rating_type, status) VALUES (3, 'Rating7', 'A');
INSERT INTO survey_rating_type_master (rating_type_id, rating_type, status) VALUES (4, 'Rating10', 'A');

--
-- Data for Name: survey_section_question; Type: TABLE DATA; 
--

--
-- Data for Name: survey_visit_feedback; Type: TABLE DATA; 
--

--
-- Data for Name: survey_visit_feedback_details; Type: TABLE DATA; 
--

--
-- Data for Name: surveydata_answers; Type: TABLE DATA; 
--

--
-- Data for Name: surveydata_visit_info; Type: TABLE DATA; 
--

--
-- Data for Name: surveyform_general_info; Type: TABLE DATA; 
--

--
-- Data for Name: surveyform_header_questions; Type: TABLE DATA; 
--

--
-- Data for Name: surveyform_rating_scale_desc; Type: TABLE DATA; 
--

--
-- Data for Name: surveyform_summary_questions; Type: TABLE DATA; 
--

--
-- Data for Name: surveyform_topic_questions; Type: TABLE DATA; 
--

--
-- Data for Name: system_data; Type: TABLE DATA; 
--

INSERT INTO system_data (version, api_level, group_id) VALUES ('12.0.4-8850', 1, 'fresh');

--
-- Data for Name: system_generated_sections; Type: TABLE DATA; 
--

INSERT INTO system_generated_sections (section_id, section_name, section_mandatory, obsolete_visit_type, obsolete_form_type, op, ip, surgery, field_phrase_category_id, service, triage, initial_assessment, generic_form, op_follow_up_consult_form, display_name) VALUES (-5, 'Consultation Notes (Sys)', false, 'o', 'o', 'Y', 'N', 'N', NULL, 'N', 'N', 'N', 'N', 'Y', 'Consultation Notes');
INSERT INTO system_generated_sections (section_id, section_name, section_mandatory, obsolete_visit_type, obsolete_form_type, op, ip, surgery, field_phrase_category_id, service, triage, initial_assessment, generic_form, op_follow_up_consult_form, display_name) VALUES (-7, 'Prescriptions (Sys)', false, 'o', 'o', 'Y', 'N', 'N', NULL, 'N', 'N', 'N', 'N', 'Y', 'Prescriptions');
INSERT INTO system_generated_sections (section_id, section_name, section_mandatory, obsolete_visit_type, obsolete_form_type, op, ip, surgery, field_phrase_category_id, service, triage, initial_assessment, generic_form, op_follow_up_consult_form, display_name) VALUES (-15, 'Health Maintenance (Sys)', false, 'o', 'o', 'Y', 'N', 'N', NULL, 'N', 'N', 'N', 'N', 'Y', 'Health Maintenance');
INSERT INTO system_generated_sections (section_id, section_name, section_mandatory, obsolete_visit_type, obsolete_form_type, op, ip, surgery, field_phrase_category_id, service, triage, initial_assessment, generic_form, op_follow_up_consult_form, display_name) VALUES (-3, 'Triage Summary (Sys)', false, ' ', NULL, 'Y', 'N', 'N', NULL, 'N', 'N', 'N', 'N', 'Y', 'Triage Summary');
INSERT INTO system_generated_sections (section_id, section_name, section_mandatory, obsolete_visit_type, obsolete_form_type, op, ip, surgery, field_phrase_category_id, service, triage, initial_assessment, generic_form, op_follow_up_consult_form, display_name) VALUES (-1, 'Complaint (Sys)', false, 'b', 'a', 'Y', 'Y', 'Y', NULL, 'N', 'Y', 'N', 'Y', 'Y', 'Complaint');
INSERT INTO system_generated_sections (section_id, section_name, section_mandatory, obsolete_visit_type, obsolete_form_type, op, ip, surgery, field_phrase_category_id, service, triage, initial_assessment, generic_form, op_follow_up_consult_form, display_name) VALUES (-2, 'Allergies (Sys)', false, 'b', 'a', 'Y', 'Y', 'N', NULL, 'N', 'Y', 'N', 'Y', 'Y', 'Allergies');
INSERT INTO system_generated_sections (section_id, section_name, section_mandatory, obsolete_visit_type, obsolete_form_type, op, ip, surgery, field_phrase_category_id, service, triage, initial_assessment, generic_form, op_follow_up_consult_form, display_name) VALUES (-4, 'Vitals (Sys)', false, 'o', 'o', 'Y', 'N', 'N', NULL, 'N', 'Y', 'Y', 'Y', 'Y', 'Vitals');
INSERT INTO system_generated_sections (section_id, section_name, section_mandatory, obsolete_visit_type, obsolete_form_type, op, ip, surgery, field_phrase_category_id, service, triage, initial_assessment, generic_form, op_follow_up_consult_form, display_name) VALUES (-13, 'Obstetric History (Sys)', false, ' ', NULL, 'Y', 'Y', 'N', NULL, 'N', 'Y', 'Y', 'Y', 'Y', 'Obstetric History');
INSERT INTO system_generated_sections (section_id, section_name, section_mandatory, obsolete_visit_type, obsolete_form_type, op, ip, surgery, field_phrase_category_id, service, triage, initial_assessment, generic_form, op_follow_up_consult_form, display_name) VALUES (-14, 'Antenatal (Sys)', false, ' ', NULL, 'Y', 'Y', 'N', NULL, 'N', 'Y', 'Y', 'Y', 'Y', 'Antenatal');
INSERT INTO system_generated_sections (section_id, section_name, section_mandatory, obsolete_visit_type, obsolete_form_type, op, ip, surgery, field_phrase_category_id, service, triage, initial_assessment, generic_form, op_follow_up_consult_form, display_name) VALUES (-6, 'Diagnosis Details (Sys)', false, 'b', 'a', 'Y', 'Y', 'Y', NULL, 'N', 'N', 'N', 'Y', 'Y', 'Diagnosis Details');
INSERT INTO system_generated_sections (section_id, section_name, section_mandatory, obsolete_visit_type, obsolete_form_type, op, ip, surgery, field_phrase_category_id, service, triage, initial_assessment, generic_form, op_follow_up_consult_form, display_name) VALUES (-16, 'Pre-Anaesthetic Checkup (Sys)', false, ' ', ' ', 'Y', 'Y', 'Y', NULL, 'N', 'N', 'N', 'Y', 'Y', 'Pre-Anaesthetic Checkup');

--
-- Data for Name: system_messages; Type: TABLE DATA; 
--

INSERT INTO system_messages (message_id, messages, system_type, severity, display_order, screen_id, param1, param2) VALUES (1, '$2: Bills Now bills open more than 1 day = $1', 'Stats1', 'W', 1, 'search_bills', '0', '12:09');
INSERT INTO system_messages (message_id, messages, system_type, severity, display_order, screen_id, param1, param2) VALUES (2, '$2: Pharmacy Retail bills open more than 1 day = $1', 'Stats2', 'W', 1, 'pharma_sales', '0', '12:09');
INSERT INTO system_messages (message_id, messages, system_type, severity, display_order, screen_id, param1, param2) VALUES (3, '$2: Lab Tests pending for conduction more than 1 day = $1', 'Stats3', 'W', 1, 'lab_diag_report_list', '0', '12:09');
INSERT INTO system_messages (message_id, messages, system_type, severity, display_order, screen_id, param1, param2) VALUES (4, '$2: Radiology Tests pending for conduction more than 1 day = $1', 'Stats4', 'W', 1, 'radio_diag_report_list', '0', '12:09');
INSERT INTO system_messages (message_id, messages, system_type, severity, display_order, screen_id, param1, param2) VALUES (5, '$2: Services pending for conduction more than 1 day = $1', 'Stats5', 'W', 1, 'serive_List', '0', '12:09');
INSERT INTO system_messages (message_id, messages, system_type, severity, display_order, screen_id, param1, param2) VALUES (6, '$2: OP Visits not closed more than 1 day = $1', 'Stats6', 'W', 1, 'reg_patient_visit_search', '0', '12:09');
INSERT INTO system_messages (message_id, messages, system_type, severity, display_order, screen_id, param1, param2) VALUES (7, '$2: Beds pending to be allocated more than 1 day = $1', 'Stats7', 'W', 1, 'adt', '0', '12:09');

--
-- Data for Name: system_preferences_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: temporary_access_types; Type: TABLE DATA; 
--

--
-- Data for Name: test_cytology_results; Type: TABLE DATA; 
--

--
-- Data for Name: test_dept_tokens; Type: TABLE DATA; 
--

INSERT INTO test_dept_tokens (dept_id, center_id, token_number) VALUES ('DDept0001', 0, 0);

--
-- Data for Name: test_details; Type: TABLE DATA; 
--

--
-- Data for Name: test_details_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: test_documents; Type: TABLE DATA; 
--

--
-- Data for Name: test_equipment_master; Type: TABLE DATA; 
--

--
-- Data for Name: test_format; Type: TABLE DATA; 
--

INSERT INTO test_format (format_name, testformat_id, format_description, report_file) VALUES ('Default Report', 'FORMAT_DEF', 'Default Report', '<p style="text-align: center;"><strong>Report</strong></p>');
INSERT INTO test_format (format_name, testformat_id, format_description, report_file) VALUES ('Default Addendum Report', 'ADDENDUM_FORMAT_DEF', 'Default Addendum Report', '<p style="text-align: center;"><strong>Addendum Report</strong></p>');

--
-- Data for Name: test_histopathology_results; Type: TABLE DATA; 
--

--
-- Data for Name: test_images; Type: TABLE DATA; 
--

--
-- Data for Name: test_micro_antibiotic_details; Type: TABLE DATA; 
--

--
-- Data for Name: test_micro_org_group_details; Type: TABLE DATA; 
--

--
-- Data for Name: test_microbiology_results; Type: TABLE DATA; 
--

--
-- Data for Name: test_org_details; Type: TABLE DATA; 
--

--
-- Data for Name: test_payment_master; Type: TABLE DATA; 
--

--
-- Data for Name: test_result_ranges; Type: TABLE DATA; 
--

--
-- Data for Name: test_results_center; Type: TABLE DATA; 
--

--
-- Data for Name: test_results_master; Type: TABLE DATA; 
--

--
-- Data for Name: test_template_master; Type: TABLE DATA; 
--

--
-- Data for Name: test_units; Type: TABLE DATA; 
--

INSERT INTO test_units (units) VALUES ('mg/dl');
INSERT INTO test_units (units) VALUES ('ml');
INSERT INTO test_units (units) VALUES ('cells/cu.mm');
INSERT INTO test_units (units) VALUES ('mm');

--
-- Data for Name: test_visit_report_signatures; Type: TABLE DATA; 
--

--
-- Data for Name: test_visit_reports; Type: TABLE DATA; 
--

--
-- Data for Name: test_visit_reports_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: tests_conducted; Type: TABLE DATA; 
--

--
-- Data for Name: tests_conducted_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: tests_prescribed; Type: TABLE DATA; 
--

--
-- Data for Name: tests_prescribed_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: theatre_master; Type: TABLE DATA; 
--

--
-- Data for Name: theatre_charges; Type: TABLE DATA; 
--

--
-- Data for Name: theatre_item_sub_groups; Type: TABLE DATA; 
--

--
-- Data for Name: theatre_org_details; Type: TABLE DATA; 
--

--
-- Data for Name: ticket_comments; Type: TABLE DATA; 
--

--
-- Data for Name: ticket_recipients; Type: TABLE DATA; 
--

--
-- Data for Name: tooth_root_status; Type: TABLE DATA; 
--

INSERT INTO tooth_root_status (root_status_id, root_status_desc, color_code, status) VALUES (1, 'Carious', '#0CEF0C', 'A');
INSERT INTO tooth_root_status (root_status_id, root_status_desc, color_code, status) VALUES (2, 'Lesion', '#EF0C27', 'A');
INSERT INTO tooth_root_status (root_status_id, root_status_desc, color_code, status) VALUES (3, 'Defective', '#120CEF', 'A');
INSERT INTO tooth_root_status (root_status_id, root_status_desc, color_code, status) VALUES (4, 'Filled', '#E20CEF', 'A');
INSERT INTO tooth_root_status (root_status_id, root_status_desc, color_code, status) VALUES (5, 'Filled and Apicectomy', '#EFD70C', 'A');
INSERT INTO tooth_root_status (root_status_id, root_status_desc, color_code, status) VALUES (6, 'Core and post', '#ECEAEB', 'A');

--
-- Data for Name: tooth_surface_material_master; Type: TABLE DATA; 
--

INSERT INTO tooth_surface_material_master (material_id, material_name, color_code, status) VALUES (1, 'Amalgam', '#0CEF0C', 'A');
INSERT INTO tooth_surface_material_master (material_id, material_name, color_code, status) VALUES (2, 'Precious Metal', '#EF0C27', 'A');
INSERT INTO tooth_surface_material_master (material_id, material_name, color_code, status) VALUES (3, 'Gold', '#120CEF', 'A');
INSERT INTO tooth_surface_material_master (material_id, material_name, color_code, status) VALUES (4, 'Glass Ionomer', '#E20CEF', 'A');
INSERT INTO tooth_surface_material_master (material_id, material_name, color_code, status) VALUES (5, 'Metallic', '#EFD70C', 'A');
INSERT INTO tooth_surface_material_master (material_id, material_name, color_code, status) VALUES (6, 'Porcelain', '#ECEAEB', 'A');
INSERT INTO tooth_surface_material_master (material_id, material_name, color_code, status) VALUES (7, 'Resin', '#84DCE2', 'A');
INSERT INTO tooth_surface_material_master (material_id, material_name, color_code, status) VALUES (8, 'Unknown', '#6F29A0', 'A');

--
-- Data for Name: tooth_surface_option_master; Type: TABLE DATA; 
--

INSERT INTO tooth_surface_option_master (option_id, option_name, status) VALUES (1, 'None', 'A');
INSERT INTO tooth_surface_option_master (option_id, option_name, status) VALUES (2, 'Caries', 'A');
INSERT INTO tooth_surface_option_master (option_id, option_name, status) VALUES (3, 'Defective', 'A');
INSERT INTO tooth_surface_option_master (option_id, option_name, status) VALUES (4, 'Restored', 'A');
INSERT INTO tooth_surface_option_master (option_id, option_name, status) VALUES (5, 'Fiss Sealant', 'A');

--
-- Data for Name: tooth_surface_status_master; Type: TABLE DATA; 
--

INSERT INTO tooth_surface_status_master (surface_status_id, surface_status_name, status) VALUES (1, 'Sound', 'A');
INSERT INTO tooth_surface_status_master (surface_status_id, surface_status_name, status) VALUES (2, 'Carious', 'A');
INSERT INTO tooth_surface_status_master (surface_status_id, surface_status_name, status) VALUES (3, 'Defective', 'A');

--
-- Data for Name: tooth_treatment_details; Type: TABLE DATA; 
--

--
-- Data for Name: tpa_center_master; Type: TABLE DATA; 
--

INSERT INTO tpa_center_master (tpa_center_id, tpa_id, center_id, status, claim_format) VALUES (1, 'TPAID0001', -1, 'A', 'XML');
INSERT INTO tpa_center_master (tpa_center_id, tpa_id, center_id, status, claim_format) VALUES (2, 'TPAID0002', -1, 'A', 'XML');
INSERT INTO tpa_center_master (tpa_center_id, tpa_id, center_id, status, claim_format) VALUES (3, 'TPAID0005', -1, 'A', 'XML');
INSERT INTO tpa_center_master (tpa_center_id, tpa_id, center_id, status, claim_format) VALUES (4, 'TPAID0003', -1, 'A', 'XML');
INSERT INTO tpa_center_master (tpa_center_id, tpa_id, center_id, status, claim_format) VALUES (5, 'TPAID0006', -1, 'A', 'XML');
INSERT INTO tpa_center_master (tpa_center_id, tpa_id, center_id, status, claim_format) VALUES (6, 'TPAID0004', -1, 'A', 'XML');

--
-- Data for Name: tpa_forms_pdf; Type: TABLE DATA; 
--

--
-- Data for Name: tpa_package_applicability; Type: TABLE DATA; 
--

--
-- Data for Name: transfer_hospitals; Type: TABLE DATA; 
--

--
-- Data for Name: treatment_chart; Type: TABLE DATA; 
--

--
-- Data for Name: treatment_followups; Type: TABLE DATA; 
--

--
-- Data for Name: treatment_form_entries; Type: TABLE DATA; 
--

--
-- Data for Name: treatment_form_pdfs; Type: TABLE DATA; 
--

--
-- Data for Name: triage_components; Type: TABLE DATA; 
--

INSERT INTO triage_components (dept_id, allergies, vitals, forms, immunization, id) VALUES ('-1', 'Y', 'Y', '1,2,3,4,5', 'Y', 1);

--
-- Data for Name: unique_number; Type: TABLE DATA; 
--

INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Diagnostics_Departments', 1, 'DDept', 'DDept', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('profilemaster', 1, 'PRO', 'PRO', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SALERECPT', 1, 'SALERCPT10', 'SALERCPT10', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('BDRCPT', 1, 'RBD', 'RBD', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('NRRCPT', 1, 'RNR', 'RNR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DDRCPT', 1, 'RDD', 'RDD', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('MNRCPT', 1, 'RMN', 'RMN', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('LUXTAX', 1, 'LAT', 'LAT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SERTAX', 1, 'STAXRCPT', 'STAXRCPT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('RGRCPT', 1, 'RRG', 'RRG', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('OPNO', 1, 'OP', 'OP', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PATIENT MLC RECEIPTNO', 1, 'MLCR', 'MLCR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('CONSULTAION RECEIPTNO', 1, 'DOCR', 'DOCR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Package', 1, 'PACK', 'PACK', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SUBPACKAGE', 1, 'SPACK', 'SPACK', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Preferences', 1, 'PREF', 'PREF', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('paymentmode', 1, 'PAYMODE', 'PAYMODE', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('salutation', 1, 'SALU', 'SALU', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Specialization', 1, 'SPEC', 'SPEC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('centralstore', 1, 'CTS', 'CTS', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Services', 1, 'SERV', 'SERV', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('IPNO', 1, 'IP', 'IP', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('OP REGISTRATION BILL NUMBER', 1, 'OPB100', 'OPB100', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('IP REGISTRATION BILL NUMBER', 1, 'IPB', 'IPB', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('ORG', 1, 'ORG', 'ORG', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('MRCRECPT', 1, 'MRCRCPT1', 'MRCRCPT1', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DVRCPT', 1, 'DVRCPT', 'DVRCPT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('BEDCHARGES', 1, 'BED', 'BED', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('MRNO', 1, 'MR', 'MR', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('OTHER CHARGES', 1, 'RO', 'RO', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Doctor', 1, 'DOC', 'DOC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('WARDID', 1, 'WARD', 'WARD', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('HOSP', 1, 'HOSP', 'HOSP', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('RefDoctor', 1, 'REFDOC', 'REFDOC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('INVSALERECPT', 1, 'INSALERCPT', 'INSALERCPT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DIAG REGISTRATION BILL NUMBER', 1, 'DIAGB', 'DIAGB', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DiagnosticPatientNumber', 1, 'DIAPNO', 'DIAPNO', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SCHEDULE_DOCTOR', 1, 'SDOC', 'SDOC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('MLCRECPT', 1, 'MLC', 'MLC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('GENREL REGISTRATION', 1, 'GREG', 'GREG', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Out House Hospital', 1, 'OHHOSP', 'OHHOSP', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('TEXTFORMAT', 1, 'FORMAT', 'FORMAT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('servicereceiptno', 1, 'SREC', 'SREC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Receipt Number', 1, 'RC', 'RC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DIAGBILLING', 1, 'DTB', 'DTB', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('BILL SETTLEMENT', 1, 'BSREC', 'BSREC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Inv Purchases Requisition', 1, 'IPR', 'IPR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('InvPurchase Returns', 1, 'IPRN', 'IPRN', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PHARMACYREQUISITION', 1, 'PHR', 'PHR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PAMDNO', 1, 'PAMD', 'PAMD', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Sales Returns', 1, 'SR', 'SR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('MAINSTOCKISSUE', 1, 'MSI', 'MSI', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SUBSTOCKISSUE', 1, 'SSI', 'SSI', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('MAINCONSIGNMENTISSUE', 1, 'MCI', 'MCI', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SUBCONSIGNMENTISSUE', 1, 'SCI', 'SCI', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('CONSUMPTIONCODE', 1, 'CCODE', 'CCODE', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('STOCKENTRY', 1, 'STE', 'STE', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Sales ReceiptNo', 1, 'SREC', 'SREC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PHEDITPO', 1, 'PED', 'PED', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('RELATIONNUMBER', 1, 'RELA', 'RELA', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('OCCUPATION', 1, 'OCC', 'OCC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('CARDTYPE', 1, 'CARD', 'CARD', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('RELIGION', 1, 'REL', 'REL', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('OPERATIONID', 1, 'OPID', 'OPID', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('EQIPMENTID', 1, 'EQID', 'EQID', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('OTSALES', 1, 'OTREC', 'OTREC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('OPSCHEDULEID', 1, 'SCID', 'SCID', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('THEATERID', 1, 'THID', 'THID', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('OPERATION RECEIPT NUMBER', 1, 'ROP', 'ROP', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('INSURANCEID', 1, 'INSID', 'INSID', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SERVICE RECEIPT NUMBER', 1, 'RSER', 'RSER', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SERVICEBILLING', 1, 'DSB', 'DSB', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DUECLEARENCE', 1, 'DCREC', 'DCREC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('INV_GENERAL_DETAILS', 1, 'IGD', 'IGD', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PH_GENERAL_DETAILS', 1, 'GD', 'GD', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('GAR', 1, 'GAR', 'GAR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('AMDNO', 1, 'AMD', 'AMD', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PHARMACYGROUP', 1, 'PGR', 'PGR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SPLITBILLING', 1, 'SBR', 'SBR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PROFILE', 1, 'PRO', 'PRO', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Sample Number', 1, 'SAMP', 'SAMP', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('GroupLabTests', 1, 'GL', 'GL', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Sub Stores', 1, 'SS', '1', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('INVENTORY SUBGROUP', 1, 'ISGR', 'ISGR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('ItemCode', 1, 'IC', 'IC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Inv Stores Requisition', 1, 'ISR', 'ISR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Voucher Number', 1, 'VC', 'VC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Purchase Returns', 1, 'PR', 'PR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Purchase Order Number', 1, 'PH', 'PH', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Manufacturer', 1, 'MF', 'MF', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Sales DocNo', 1, 'DocNo', 'DocNo', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('UomMaster', 1, 'UOM', 'UOM', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Inv Purchases Order', 1, 'GS', 'GS', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('ADRCPT', 1, 'RAD', NULL, '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Supplier Code', 1, 'SP', 'SP', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('INVENTORY GROUP', 1, 'IGR', 'IGR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('vendorcode', 1, 'VENDOR', 'VENDOR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('INVENTORY CATEGORY', 1, 'ICAT', 'ICAT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SCHEDULESTATUS', 1, 'SCHST', 'ST', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SubSample Number', 1, 'SUBSAMP', 'SUBSAMP', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SCHEDULECAT', 1, 'SCHCAT', 'C', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('ohsample', 1, 'OHSAMP', 'ohsamp', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('RBN', 1, 'RBN', 'RBN', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PARTPAYMENTSBILL', 1, 'FBN', 'FBN', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('BRAND', 1, 'BR', 'BR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('BATCHCODE', 1, 'GBAT', 'GBAT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DIAGBILLCANCELLATION', 1, 'DBILL', 'DBILL', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('MEDICINETYPE', 1, 'MT', 'MT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('TPA', 1, 'TPAID', 'TPAID', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('COMPANYID', 1, 'COMP', 'COMP', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('GENERICNAME', 1, 'GN', 'GN', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('REFVOUCHER', 1, 'RVC', 'RVC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PACKAGEREGID', 1, 'PREG', 'PREG', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PHARMACYSUBGROUP', 1, 'PSGR', 'PSGR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PACKAGERECEIPT', 1, 'PRC', 'PRC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PHAR', 1, 'A', 'S', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('TESTCONFIRM', 1, 'CONF', 'CONF', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('ISOLATE', 1, 'ISO', 'ISO', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('METRO_SUPP_NUM', 2, 'MMM', 'MMM', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('department', 1, 'DEP', 'DEP', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('doc_type', 1, 'DC', 'DC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('counters', 1, 'CNT', 'CNT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('city', 1, 'CT', 'CT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('SAR', 1, 'SAR', 'SAR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('country_master', 1, 'CM', 'CM', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('diagnostics', 1, 'DGC', 'DGC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('INVEDITPO', 1, 'IED', 'IED', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('BEDEQUIPMENTCHARGE', 1, 'BEC', 'BEC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('rag', 101, 'RAG', 'RAG', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('GENERALDEPARTMENTS', 1, 'GDEPT', 'GDEPT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('ADVANCEBILLING', 1, 'ABN', 'ABN', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('ANTIBIOTIC', 1, 'ANTI', 'ANTI', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('QUICK BILL', 1, 'QB', 'QB', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('CDCRECP', 1, 'CDCRECP', 'CDCRECP', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PPRCPT', 1, 'RPP', 'RPP', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Scheme', 1, 'SCH', 'SCH', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('BILL NUMBER', 1, 'MBN', 'MBN', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('GENREL RECEIPT', 1, 'GRC', 'GRC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Medicine', 1, 'MD', 'MD', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('BN', 1, 'SF', '', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('CHARGEID', 1, 'CH', 'CH', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('REFUNDNO', 1, 'RF', 'RF', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('APPROVAL', 1, 'APR', 'APR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PHCUSTID', 1, 'PH', 'PH', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('INHOUSEVISITID', 1, 'IN', 'IN', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PAYMENTID', 1, 'P', 'P', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('VOUCHERID', 1, 'V', 'V', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('INSCLAIMNO', 1, 'CLM', 'CLM', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('REPORTID', 1, 'RID', 'RID', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DISFORMAT', 1, 'FORMAT', 'FORMAT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DISSCAN', 1, 'DIS', 'DIS', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('EMAIL_TEMPLATE', 1, 'ET', 'ET', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('REAGENT', 1, 'REA', 'REA', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('FORMID', 1, 'FM', 'FM', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DATADIC', 1, 'DD', 'DD', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DATAHEADER', 1, 'DH', 'DH', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('att_heading', 1, 'AH', 'AH', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('att_value', 1, 'AV', 'AV', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('attribute', 1, 'A', 'A', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('subheading', 1, 'SH', 'SH', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('heading', 1, 'H', 'H', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('validation', 1, 'ATV', 'ATV', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('ISSUE', 1, 'ISSUE', 'ISSUE', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('INVENTORYSTORES', 1, 'ISTORE', 'ISTORE', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('dept_transfer_details', 1, 'IDT', 'IDT', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('area_master', 1, 'AR', 'AR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('incoming_hospitals', 1, 'IH', 'IH', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('LABNO', 1, 'LAB', 'LAB', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('RADNO', 1, 'RAD', 'RAD', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PhDebitNote', 1, 'DB', 'DB', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DebitGRN', 1, 'DGR', 'DGR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('INVENTORY GRN', 1, 'IG', 'IG', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('InvDebitNote', 1, 'IDB', 'IDB', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('InvDebitGRN', 1, 'IDGR', 'IDGR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('RECEIPTNO DEFAULT', 1, 'RC', 'RC', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DEPOSIT RECEIPTS', 1, 'DR', 'DR', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('DEPOSIT REFUNDS', 1, 'DF', 'DF', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('PH RETURN BILL', 1, 'BR', 'BR', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Case Number', 1, 'CS', 'CS', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('REVERSAL_VOUCHERID', 1, 'R', 'R', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('ICM', 1, 'ICM', 'ICM', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('transfer_hospitals', 1, 'TR', 'TR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('Rule Number', 1, 'RN', 'RN', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('patient_partogram', 1, 'PP', 'PP', '0');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('patient_partogram_details', 1, 'PPD', 'PPD', '0');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('bag_barcode_seq', 1, 'BAG_CODE', 'BAG_CODE', '9900000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('bagtype_master', 1, 'BAGT', 'BAGT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('blood_bag_seq', 1, 'BAG', 'BAG', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('blood_donation_type_master', 1, 'BDT', 'BDT', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('blood_group_master', 1, 'BLG', 'BLG', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('donor_blood_component', 1, 'BC', 'BC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('donor_blood_discard', 1, 'BDISC', 'BDISC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('donor_blood_grouping', 1, 'BLG', 'BLG', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('donor_blood_issue', 1, 'ISSUE', 'ISSUE', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('donor_blood_request', 1, 'BBR', 'BBR', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('donor_collection_det', 1, 'BAG', 'BAG', '00000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('donor_registration', 1, 'BB', 'BB', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('venipuncture_site_master', 1, 'VC', 'VC', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('work_order_main', 1, 'WO', 'WO', '000000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('ph_payment_terms', 1, 'TMPL', 'TMPL', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('state_master', 1, 'ST', 'ST', '0000');
INSERT INTO unique_number (type_number, start_number, prefix, suffi, pattern) VALUES ('follow_up_details', 1, 'FUD', 'FUD', '0000');

--
-- Data for Name: units; Type: TABLE DATA; 
--

INSERT INTO units (units, status) VALUES ('METER', 'A');
INSERT INTO units (units, status) VALUES ('UNIT', 'A');
INSERT INTO units (units, status) VALUES ('PACKET', 'A');
INSERT INTO units (units, status) VALUES ('GRAMS', 'A');
INSERT INTO units (units, status) VALUES ('KILOGRAMS', 'A');

--
-- Data for Name: url_action_rights; Type: TABLE DATA; 
--

INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'bill_audit_log', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'billing_duplicate_bill', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'billing_tally_export', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'cancel_package', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'change_billtype', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'credit_bill_collection', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'deposits_realization', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'new_prepaid_bills', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'patient_deposits', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'prescribe_package', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'receipts', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'search_bills', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'transaction_approval', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'folowup_details', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'prescription', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'reg_general', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'reg_quick_estimate', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'reg_re_admit', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'discharge_summary', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'emr_patient_search', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'grp_speciality_treatment', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'rep_starttreatment', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'rep_treatment', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (6, 'discharge_summary', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (6, 'emr_patient_search', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (6, 'rep_starttreatment', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (6, 'rep_treatment', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'discharge_summary_print', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (6, 'discharge_summary_print', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'mlc_genericdoc_print', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'mlc_generic_docs', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'bill_print', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'edit_visit_details', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'reg_generic_docs', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'reg_card_print', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'treatment_list', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (6, 'treatment_list', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'patient_deposits_print', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'bill_pharma_sales_print', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'reg_quick_estimate_print', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'medicine_quick_estimate', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'patient_expenese_print', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'order', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'patient_details_search', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'patient_details_search', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'patient_details_search', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (6, 'patient_details_search', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'visit_details_search', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'visit_details_search', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'visit_details_search', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (6, 'visit_details_search', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'patient_deposits_collection', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'patient_deposits_collection', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'patient_deposits_collection', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (6, 'patient_deposits_collection', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'patient_expensive_statement', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'patient_expensive_statement', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'patient_expensive_statement', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (6, 'patient_expensive_statement', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'bills_backup', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'change_billprimary', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'receipt_print', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'deposit_receipts', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (3, 'bill_email', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (4, 'new_op_registration', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (5, 'new_discharge_summary', 'A');
INSERT INTO url_action_rights (role_id, action_id, rights) VALUES (6, 'new_discharge_summary', 'A');
--
-- Data for Name: user_home_screens; Type: TABLE DATA; 
--

--
-- Data for Name: user_hosp_role_master; Type: TABLE DATA; 
--

--
-- Data for Name: user_images; Type: TABLE DATA; 
--

--
-- Data for Name: user_location; Type: TABLE DATA; 
--

--
-- Data for Name: user_services_depts; Type: TABLE DATA; 
--

--
-- Data for Name: vaccine_master; Type: TABLE DATA; 
--

--
-- Data for Name: vaccine_dose_master; Type: TABLE DATA; 
--

--
-- Data for Name: venipuncture_site_master; Type: TABLE DATA; 
--

INSERT INTO venipuncture_site_master (vsite_id, vsite_name, status) VALUES ('1', 'Basilic vein', 'A');
INSERT INTO venipuncture_site_master (vsite_id, vsite_name, status) VALUES ('2', 'Cephalic vein', 'A');
INSERT INTO venipuncture_site_master (vsite_id, vsite_name, status) VALUES ('3', 'Median cubital vein', 'A');

--
-- Data for Name: visit_care_team; Type: TABLE DATA; 
--

--
-- Data for Name: visit_type_names; Type: TABLE DATA; 
--

INSERT INTO visit_type_names (visit_type, visit_type_name, visit_type_long_name) VALUES ('i', 'IP', 'In Patient');
INSERT INTO visit_type_names (visit_type, visit_type_name, visit_type_long_name) VALUES ('o', 'OP', 'Out Patient');
INSERT INTO visit_type_names (visit_type, visit_type_name, visit_type_long_name) VALUES ('r', 'Retail', 'Pharmacy Retail Customer');
INSERT INTO visit_type_names (visit_type, visit_type_name, visit_type_long_name) VALUES ('t', 'Test', 'Incoming Test');

--
-- Data for Name: visit_vitals; Type: TABLE DATA; 
--

--
-- Data for Name: visit_vitals_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: vital_parameter_master; Type: TABLE DATA; 
--

INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (1, 'V', 'Pulse', '', 1, 'A', 'N', NULL, NULL, NULL, NULL, 'N');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (2, 'V', 'B.P', '', 2, 'A', 'N', NULL, NULL, NULL, NULL, 'N');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (3, 'V', 'Resp', '', 3, 'A', 'N', NULL, NULL, NULL, NULL, 'N');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (4, 'V', 'Temp', '', 4, 'A', 'N', NULL, NULL, NULL, NULL, 'N');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (5, 'V', 'Height', '', 5, 'A', 'N', NULL, NULL, NULL, NULL, 'N');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (6, 'V', 'Weight', '', 6, 'A', 'N', NULL, NULL, NULL, NULL, 'N');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (7, 'I', 'Oral', '', 7, 'A', 'N', NULL, NULL, 'I', NULL, 'N');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (8, 'I', 'I.V', '', 8, 'A', 'N', NULL, NULL, 'I', NULL, 'N');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (9, 'O', 'Urine', '', 9, 'A', 'N', NULL, NULL, 'I', NULL, 'N');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (10, 'O', 'Stool''s', '', 10, 'A', 'N', NULL, NULL, 'I', NULL, 'N');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (11, 'O', 'Aspirate', '', 11, 'A', 'N', NULL, NULL, 'I', NULL, 'N');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (12, 'V', 'Height', 'cms', 1, 'I', 'N', NULL, NULL, NULL, NULL, 'Y');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (13, 'V', 'Weight', 'kgs', 2, 'I', 'N', NULL, NULL, NULL, NULL, 'Y');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (14, 'V', 'B.P.', 'mmHg', 3, 'I', 'N', NULL, NULL, NULL, NULL, 'Y');
INSERT INTO vital_parameter_master (param_id, param_container, param_label, param_uom, param_order, param_status, mandatory_in_tx, observation_type, observation_code, visit_type, expr_for_calc_result, system_vital) VALUES (15, 'V', 'Head Circumference', 'cms', 4, 'I', 'N', NULL, NULL, NULL, NULL, 'Y');

--
-- Data for Name: vital_reading; Type: TABLE DATA; 
--

--
-- Data for Name: vital_reading_audit_log; Type: TABLE DATA; 
--

--
-- Data for Name: vital_reference_range_master; Type: TABLE DATA; 
--

--
-- Data for Name: ward_names; Type: TABLE DATA; 
--

INSERT INTO ward_names (ward_no, ward_name, status, description, store_id, center_id) VALUES ('WARD0001', 'GENERAL', 'A', NULL, NULL, 0);

--
-- Data for Name: work_order_items_master; Type: TABLE DATA; 
--

--
-- Data for Name: work_order_main; Type: TABLE DATA; 
--

--
-- Data for Name: work_order_details; Type: TABLE DATA; 
--

--
-- PostgreSQL database dump complete
--

