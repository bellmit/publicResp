-- liquibase formatted sql
-- changeset sreenivasayashwanth:<creating_index_to_optimize_ip_bed_screen>

create index ip_bed_details_admit_id_idx on ip_bed_details(admit_id);

create index admission_patient_id_idx on admission(patient_id);
