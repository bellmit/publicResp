-- liquibase formatted sql
-- changeset janakivg:default-print-templates-for-notes-and-vitals

INSERT INTO print_templates (template_type) (SELECT 'PatientNotes' WHERE NOT EXISTS (SELECT 1 FROM print_templates WHERE template_type = 'PatientNotes'));

INSERT INTO print_templates (template_type) (SELECT 'VitalsChart' WHERE NOT EXISTS (SELECT 1 FROM print_templates WHERE template_type = 'VitalsChart'));
