-- liquibase formatted sql
-- changeset tejakilaru:default-mar-in-ip-forms

UPDATE form_components set sections=REPLACE(REPLACE(sections, '-19,', ''), ',-19', '') || ',-19' where form_type='Form_IP' AND istemplate=false AND sections ilike '%-7%';
