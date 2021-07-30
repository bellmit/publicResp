-- liquibase formatted sql
-- changeset manasaparam:adding-systme-filter-for-teleconsult

INSERT INTO saved_searches (flow_id, search_id, search_type, search_name, is_default, query_params, created_by, updated_by, created_time, mod_time, display_order) VALUES ('OP Flow', nextval('saved_searches_seq'), 'System', 'Today''s Online Consultations', true, 'visit_date=today&visit_date=today&visit_type=o&consultations_only=Y&visit_mode=O', 'InstaAdmin', 'InstaAdmin', now(), now(), 3);