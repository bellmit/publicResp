-- liquibase formatted sql
-- changeset goutham005:default-entry-in-print-templates-for-vaccination

INSERT INTO print_templates (template_type) (SELECT 'Vaccination' WHERE NOT EXISTS (SELECT 1 FROM print_templates WHERE template_type = 'Vaccination'));
