-- liquibase formatted sql
-- changeset manasaparam:ledger-definations-and-fa-configs

CREATE TABLE fa_configuration (
post_discount_vouchers character(1) NOT NULL,
use_credit_AC_for_discount character(1) DEFAULT 'Y'::bpchar NOT NULL,
post_zero_tax character(1) NOT NULL,
separate_deposit_refund character(1) DEFAULT 'Y'::bpchar NOT NULL
);

COMMENT ON table fa_configuration is '{ "type": "Master", "comment": "Accounting configurations" }';
COMMENT ON COLUMN fa_configuration.post_discount_vouchers IS 'Post Discount Voucher for Accounting, B:Both Item and Bill Discount Voucher, I:Only Bill Discount Voucher';
COMMENT ON COLUMN fa_configuration.use_credit_AC_for_discount IS 'Use credit a/c for discounts';
COMMENT ON COLUMN fa_configuration.post_zero_tax IS 'Allow Zero Tax Voucher for Accounting';
COMMENT ON COLUMN fa_configuration.separate_deposit_refund IS 'post separate deposit refund in fa';


INSERT INTO fa_configuration (post_discount_vouchers,use_credit_AC_for_discount,post_zero_tax,separate_deposit_refund) values((CASE WHEN (select item_level_discount_voucher from generic_preferences) = 'N' THEN 'I' ELSE 'B' END), 'Y',(select fa_allow_zero_tax_voucher from generic_preferences) ,'Y');


ALTER TABLE generic_preferences DROP COLUMN item_level_discount_voucher ;


ALTER TABLE generic_preferences DROP COLUMN fa_allow_zero_tax_voucher;


CREATE TABLE fa_ledger_definitions (
id INTEGER PRIMARY KEY,
ledger_key character varying(100) NOT NULL,
ledger_description  character varying(100) NOT NULL
);

COMMENT ON table fa_ledger_definitions is '{ "type": "Master", "comment": "Accounting definations" }';

INSERT INTO fa_ledger_definitions values(1,'ACCOUNT_TYPE_COUNTER_RECEIPTS','Counter Receipts');
INSERT INTO fa_ledger_definitions values(2,'ACCOUNT_TYPE_TAX_LIABILITY_ACC','Tax Liability A/C');
INSERT INTO fa_ledger_definitions values(3,'ACCOUNT_TYPE_DISCOUNTS_ACC','Discounts A/C');
INSERT INTO fa_ledger_definitions values(4,'ACCOUNT_TYPE_ROUNDOFF_ACC','Roundoff A/C');
INSERT INTO fa_ledger_definitions values(5,'ACCOUNT_TYPE_COGS_ACC','COGS A/C');
INSERT INTO fa_ledger_definitions values(6,'ACCOUNT_TYPE_INVENTORY_ACC','Inventory A/C');
INSERT INTO fa_ledger_definitions values(7,'ACCOUNT_TYPE_WRITEOFF_ACC','Write Off A/C');
INSERT INTO fa_ledger_definitions values(8,'ACCOUNT_TYPE_REWARDPOINTS_ACC','Reward Points A/C');
INSERT INTO fa_ledger_definitions values(9,'ACCOUNT_TYPE_TDS_ACC','TDS Paid A/C');

CREATE TABLE  fa_voucher_definitions (
id INTEGER PRIMARY KEY,
voucher_key character varying(100) NOT NULL,
voucher_definition character varying(15) NOT NULL,
is_sub_type character(1) DEFAULT 'F'::bpchar NOT NULL,
parent_key character varying(100)
);

COMMENT ON table fa_voucher_definitions is '{ "type": "Master", "comment": "Accounting voucher types & subTypes" }';

INSERT INTO fa_voucher_definitions values(1,'VOUCHER_TYPE_HOSPBILLS','HOSPBILLS','F',null);
INSERT INTO fa_voucher_definitions values(2,'VOUCHER_TYPE_PHBILLS','PHBILLS','F',null);
INSERT INTO fa_voucher_definitions values(3,'VOUCHER_TYPE_INVTRANS','INVTRANS','F',null);
INSERT INTO fa_voucher_definitions values(4,'VOUCHER_TYPE_RECEIPT','RECEIPT','F',null);
INSERT INTO fa_voucher_definitions values(5,'VOUCHER_TYPE_PAYMENT','PAYMENT','F',null);
INSERT INTO fa_voucher_definitions values(6,'VOUCHER_TYPE_CREDITNOTE','CREDITNOTE','F',null);
INSERT INTO fa_voucher_definitions values(7,'VOUCHER_SUB_TYPE_DEPOSIT_COLLECTION','DEPOSIT','T','VOUCHER_TYPE_RECEIPT');
INSERT INTO fa_voucher_definitions values(8,'VOUCHER_SUB_TYPE_DEPOSIT_SETTLEMENT','DEP SETTLEMENT','T','VOUCHER_TYPE_RECEIPT');
INSERT INTO fa_voucher_definitions values(9,'VOUCHER_SUB_TYPE_DEPOSIT_REFUND','DEPOSIT REFUND','T','VOUCHER_TYPE_PAYMENT');
INSERT INTO fa_voucher_definitions values(10,'VOUCHER_SUB_TYPE_BILL_ADVANCE','BILL ADVANCE','T','VOUCHER_TYPE_RECEIPT');
INSERT INTO fa_voucher_definitions values(11,'VOUCHER_SUB_TYPE_BILL_SETTLEMENT','BILL SETTLEMENT','T','VOUCHER_TYPE_RECEIPT');
INSERT INTO fa_voucher_definitions values(12,'VOUCHER_SUB_TYPE_BILL_REFUND','BILL REFUND','T','VOUCHER_TYPE_PAYMENT');
INSERT INTO fa_voucher_definitions values(13,'VOUCHER_SUB_TYPE_SPONSOR_ADVANCE','SPONSOR ADVANCE','T','VOUCHER_TYPE_RECEIPT');
INSERT INTO fa_voucher_definitions values(14,'VOUCHER_SUB_TYPE_SPONSOR_SETTLEMENT','SPON SETTLEMENT','T','VOUCHER_TYPE_RECEIPT');
INSERT INTO fa_voucher_definitions values(15,'VOUCHER_SUB_TYPE_BILLNOW','BILLNOW','T','VOUCHER_TYPE_HOSPBILLS');
INSERT INTO fa_voucher_definitions values(16,'VOUCHER_SUB_TYPE_BILLLATER','BILLLATER','T','VOUCHER_TYPE_HOSPBILLS');
INSERT INTO fa_voucher_definitions values(17,'VOUCHER_SUB_TYPE_OSP_BILLNOW','OSPBILLNOW','T','VOUCHER_TYPE_HOSPBILLS');
INSERT INTO fa_voucher_definitions values(18,'VOUCHER_SUB_TYPE_OSP_BILLLATER','OSPBILLLATER','T','VOUCHER_TYPE_HOSPBILLS');
INSERT INTO fa_voucher_definitions values(19,'VOUCHER_SUB_TYPE_ISR_BILLNOW','INCBILLNOW','T','VOUCHER_TYPE_HOSPBILLS');
INSERT INTO fa_voucher_definitions values(20,'VOUCHER_SUB_TYPE_ISR_BILLLATER','INCBILLLATER','T','VOUCHER_TYPE_HOSPBILLS');
INSERT INTO fa_voucher_definitions values(21,'VOUCHER_SUB_TYPE_PHR_CREDITBILL','CASHPHBILL','T','VOUCHER_TYPE_PHBILLS');
INSERT INTO fa_voucher_definitions values(22,'VOUCHER_SUB_TYPE_PHR_RETAIL','CREDITPHBILL','T','VOUCHER_TYPE_PHBILLS');
INSERT INTO fa_voucher_definitions values(23,'VOUCHER_SUB_TYPE_PHR_HOSBILL','HOSPPHBILL','T','VOUCHER_TYPE_PHBILLS');
INSERT INTO fa_voucher_definitions values(24,'VOUCHER_SUB_TYPE_PATIENT_WRITEOFF','PatientWriteOff','T','VOUCHER_TYPE_RECEIPT');
INSERT INTO fa_voucher_definitions values(25,'VOUCHER_SUB_TYPE_SPONSOR_WRITEOFF','SponsorWriteOff','T','VOUCHER_TYPE_RECEIPT');
INSERT INTO fa_voucher_definitions values(26,'VOUCHER_SUB_TYPE_REWARD_POINTS','RewardPoint','T','VOUCHER_TYPE_RECEIPT');
INSERT INTO fa_voucher_definitions values(27,'VOUCHER_SUB_TYPE_PATIENT_ISSUE','PATISSUE','T','VOUCHER_TYPE_HOSPBILLS');
INSERT INTO fa_voucher_definitions values(28,'VOUCHER_SUB_TYPE_PATIENT_ISSUE_RETURN','PATISSUERETURNS','T','VOUCHER_TYPE_HOSPBILLS');
INSERT INTO fa_voucher_definitions values(29,'VOUCHER_SUB_TYPE_PATIENT_WRITEOFF_REFUND','PatWrOffRefund','T','VOUCHER_TYPE_PAYMENT');
INSERT INTO fa_voucher_definitions values(30,'VOUCHER_SUB_TYPE_SPONSOR_WRITEOFF_REFUND','SponWrOffRefund','T','VOUCHER_TYPE_PAYMENT');

