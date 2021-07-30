-- liquibase formatted sql
-- changeset goutham005:<index-on-visit-id>

drop index if exists oh_visit_idx;
create index oh_visit_idx on outsource_sample_details using btree (visit_id);
