-- liquibase formatted sql
-- changeset sreenivasayashwanth:index_on_patient_policy_details_member_id_index  failOnError:false

CREATE INDEX patient_policy_details_member_id_idx  ON patient_policy_details(member_id);
