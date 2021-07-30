-- liquibase formatted sql
-- changeset rajendra-talekar:remvoe-legacy-ftl-refs

UPDATE acc_voucher_templates set template_content = replace(template_content, '[#import "/accounting/legacy/${schema}/preferences.ftl" as acprefs]','');
UPDATE acc_voucher_templates set template_content = replace(template_content, '[#import "/accounting/legacy/${schema}/party_accounts.ftl" as partynames]','');
UPDATE acc_voucher_templates set template_content = replace(template_content, '[#import "/accounting/legacy/${schema}/special_accounts.ftl" as specialnames]','');