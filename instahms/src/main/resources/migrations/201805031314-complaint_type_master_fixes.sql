-- liquibase formatted sql
-- changeset akshaySuman:complaint_types_master-constraints-added failOnError:false

alter table complaint_type_master drop column complaint_type;
alter table complaint_type_master drop column duration;
alter table complaint_type_master add column duration integer CHECK (duration>0);
alter table complaint_type_master add column complaint_type varchar(40) UNIQUE;
