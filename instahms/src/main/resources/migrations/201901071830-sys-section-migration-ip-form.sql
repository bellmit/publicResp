-- liquibase formatted sql
-- changeset sonam009:management-notes-vitals-section-in-ip-forms

UPDATE form_components SET sections = '-4,-18,-7,' || sections WHERE form_type='Form_IP' AND istemplate =false;