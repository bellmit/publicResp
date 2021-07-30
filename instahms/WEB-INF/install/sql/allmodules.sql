--
-- Script for enabling all modules (apart from the default that is enabled
-- using init.sql
--

-- mod_basic
-- insert into modules_activated values ('mod_basic', 'Y');
-- insert into modules_activated values ('mod_registration', 'Y');
-- insert into modules_activated values ('mod_billing', 'Y');
-- insert into modules_activated values ('mod_prescribe','Y');
-- insert into modules_activated VALUES ('mod_services','Y');

-- other charged modules
insert into modules_activated values ('mod_diagnostics', 'Y');
insert into modules_activated values ('mod_op', 'Y');
insert into modules_activated values ('mod_ipservices', 'Y');
insert into modules_activated values ('mod_mrd', 'Y');
insert into modules_activated values ('mod_mrd_icd', 'Y');
insert into modules_activated values ('mod_mrd_casefile', 'Y');
insert into modules_activated values ('mod_payments', 'Y');
insert into modules_activated values ('mod_insurance', 'Y');
insert into modules_activated values ('mod_scheduler', 'Y');
insert into modules_activated values ('mod_pharmacy', 'Y');
insert into modules_activated values ('mod_stores', 'Y');
insert into modules_activated values ('mod_tally', 'Y');
insert into modules_activated values ('mod_patient_portal', 'Y');
insert into modules_activated values ('mod_op_forms', 'Y');
insert into modules_activated VALUES ('mod_dietary','Y');
insert into modules_activated VALUES ('mod_adt','Y');
insert into modules_activated VALUES ('mod_trend','Y');
insert into modules_activated VALUES ('mod_consumables_flow','Y');
insert into modules_activated VALUES ('mod_bulk_upload','Y');
insert into modules_activated values ('mod_dialysis', 'Y');
insert into modules_activated values ('mod_billing_ext', 'Y');
insert into modules_activated values ('mod_messaging', 'Y');
insert into modules_activated values ('mod_general_messages', 'Y');
insert into modules_activated values ('mod_scheduler_messages', 'Y');
insert into modules_activated values ('mod_eandm', 'Y');
insert into modules_activated values ('mod_wardactivities', 'Y');
insert into modules_activated values ('mod_adv_ins', 'Y');
insert into modules_activated values ('mod_eclaim', 'Y');
insert into modules_activated values ('mod_tokendisplay', 'Y');
insert into modules_activated values ('mod_clinical_reports', 'Y');
insert into modules_activated values ('mod_patient_feedback', 'Y');
insert into modules_activated values ('mod_transactions', 'Y');
insert into modules_activated VALUES ('mod_clinical_data','Y') ;
insert into modules_activated VALUES ('mod_scorecard','Y') ;
insert into modules_activated values ('mod_dental_chart', 'Y');
insert into modules_activated values ('mod_emcalc', 'Y');
insert into modules_activated values ('mod_vaccination', 'Y');
insert into modules_activated values ('mod_cssd', 'Y');
insert into modules_activated values('mod_roster','Y');
insert into modules_activated values('mod_adv_packages','Y');
insert into modules_activated values('mod_advanced_ot','Y');
insert into modules_activated values('mod_basic_ot','N');
insert into modules_activated values('mod_mobile','Y');
-- To generate Web based reports by using the URL with visit ID and Passcode need this data.
INSERT INTO modules_activated values('mod_download_reports', 'Y');

-- Loyalty program for patients.
INSERT INTO modules_activated VALUES('mod_reward_points', 'Y');

-- Accumed E-Claim module. Note: mod_eclaim need to be disabled when this module is enabled.
-- insert into modules_activated VALUES('mod_accumed','Y');

-- ERx (e-Prescription) module. Note: mod_eclaim need to be enabled when this module needs to be enabled.
-- Module (mod_eclaim_erx) is only for Physician (Authorization of prescriptions)
-- Module (mod_eclaim_pbm) needs to be enabled for Pharmacy (Authorization of prescriptions)
insert into modules_activated values('mod_eclaim_erx','Y');

-- E-Claim PBM module. Note: mod_eclaim need to be enabled when this module needs to be enabled.
insert into modules_activated values('mod_eclaim_pbm','Y');

-- E-Claim PreAuth module. Note: mod_eclaim need to be enabled when this module needs to be enabled.
insert into modules_activated values('mod_eclaim_preauth','Y');

-- Not fully supported
insert into modules_activated values ('mod_fixed_asset_mgmt', 'Y');
insert into modules_activated values ('mod_linen', 'Y');

-- Not supported actively, no need to test
-- insert into modules_activated values ('mod_opthalmology', 'Y');
-- insert into modules_activated values ('mod_patient_self_appointment', 'N');
-- insert into modules_activated values ('mod_transportation', 'Y');

-- Not fully supported, also, needs to be enabled only in the hub of the network.
-- insert into modules_activated values ('mod_network', 'Y');

UPDATE generic_preferences SET prescription_uses_stores = 'Y';

insert into modules_activated values ('mod_notification', 'Y');

insert into modules_activated values ('mod_growth_charts', 'Y');


--- BLOOD BANK MODULE --

insert into modules_activated values('mod_blood_bank','Y');
INSERT INTO modules_activated VALUES ('mod_central_lab', 'Y');

---Partogram---

insert into modules_activated values ('mod_partogram', 'Y');
insert into modules_activated values ('mod_channeling', 'Y');

---Practo Profile Integration---

insert into modules_activated values ('mod_practo_integration', 'Y');

--- Davita Insurance Module --
INSERT INTO modules_activated values ('mod_ins_ext', 'N');

--- Practo SMS Module --
INSERT INTO modules_activated values ('mod_practo_sms', 'N');

--- Graphical Reports Module --
INSERT INTO modules_activated values ('mod_charts', 'N');

--- Insta Subscriptions ----
INSERT INTO modules_activated VALUES ('mod_insta_subscriptions', 'Y');

--- Practo advantage ---
INSERT INTO modules_activated VALUES ('mod_practo_advantage', 'N');

--- CEO Dashboard ---
INSERT INTO modules_activated VALUES('mod_ceo_dashboard', 'N');

--- Coder review ---
INSERT INTO modules_activated values('mod_coder_claim_review', 'Y');
--- sync external patient ---
INSERT INTO modules_activated values('mod_sync_external_patient', 'Y');
--- Appointment planner ---
INSERT INTO modules_activated values ('mod_patient_engagement', 'N');

--- Finger print Registration and Verification ---
INSERT INTO modules_activated VALUES ('mod_fingerprint', 'Y');

--- Salucro Integration ---
INSERT INTO modules_activated VALUES ('mod_salucro', 'N');

--- New mod for new consultation
INSERT INTO modules_activated VALUES ('mod_newcons', 'Y');

--- Zoho accounting 
integration
INSERT INTO modules_activated VALUES ('mod_zoho', 'Y');

--- Remittance reconcilation
INSERT INTO modules_activated VALUES ('mod_remittance_reconcilation', 'Y');

INSERT INTO modules_activated VALUES('mod_appointments', 'N');

--- New mod for SCM integration
INSERT INTO modules_activated VALUES('mod_scm','Y');

--- New mod for India GSTR  Reports
INSERT INTO modules_activated VALUES('mod_india_gstr_reports','N');

--- module for pending prescription dashboard
INSERT INTO modules_activated VALUES('mod_pat_pending_prescription','N');

-- Module for accounting
INSERT INTO modules_activated VALUES('mod_accounting', 'N');

-- Module for Motherhood Easy Rewards
INSERT INTO modules_activated values('mod_easy_rewards_coupon','N');
