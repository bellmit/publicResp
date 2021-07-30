-- liquibase formatted sql
-- changeset pranays:<make-documents-and-forms-setion-default-in-ipemr-forms>

UPDATE form_components set sections=REPLACE(REPLACE(sections, '-20,', ''), ',-20', '') || ',-20' where form_type='Form_IP' AND istemplate=false AND sections ilike '%-7%';
