-- liquibase formatted sql
-- changeset rajendratalekar:update-renamed-token-in-acc-voucher-templates

update acc_voucher_templates set template_content = replace(template_content,'acprefs.income_dept_pharma_sales','acprefs.income_dept_pharmacy');
update acc_voucher_templates set template_content = replace(template_content,'specialnames.pharm_inv_other_charges_ac_name','specialnames.pharma_inv_other_charges');
