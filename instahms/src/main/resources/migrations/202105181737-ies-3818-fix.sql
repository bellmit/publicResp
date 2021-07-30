-- liquibase formatted sql
-- changeset manjular:index-on-prescribed_id-surgery-anesthesia-details-table failOnError:false

create index surgery_anesthesia_details_prescribed_idx on surgery_anesthesia_details(prescribed_id);
create index surgery_anesthesia_details_anesthesia_type_idx on surgery_anesthesia_details(anesthesia_type);
create index bill_charge_surgery_anesthesia_details_idx on bill_charge(surgery_anesthesia_details_id);

