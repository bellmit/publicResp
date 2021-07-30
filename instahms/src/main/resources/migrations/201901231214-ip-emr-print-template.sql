-- liquibase formatted sql
-- changeset janakivg:ip-emr-default-print-template

INSERT INTO print_templates (template_type) (SELECT 'IpEmrSummaryRecord' WHERE NOT EXISTS (SELECT 1 FROM print_templates WHERE template_type = 'IpEmrSummaryRecord'));

