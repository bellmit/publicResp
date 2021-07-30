-- liquibase formatted sql
-- changeset qwewrty1:Pdf-to-html-integration-changes

ALTER TABLE doc_pdf_form_templates ADD COLUMN html_template bytea;
