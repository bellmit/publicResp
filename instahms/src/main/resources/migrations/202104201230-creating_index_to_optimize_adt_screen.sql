-- liquibase formatted sql
-- changeset asif:<creating_index_to_optimize_adt_screen>

create index ip_bed_details_status_idx on ip_bed_details(status);
create index bed_names_occupancy_idx on bed_names(occupancy);
create index doctor_center_master_doctor_id_idx on doctor_center_master(doctor_id);
create index ward_names_status_idx on ward_names(status);
create index bill_visit_type_idx on bill(visit_type);
