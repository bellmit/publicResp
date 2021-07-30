-- liquibase formatted sql
-- changeset rajendratalekar:todays-online-consultation-filter-as-non-default

UPDATE saved_searches SET is_default = false WHERE search_type='System' AND search_name='Today''s Online Consultations' AND flow_id='OP Flow';