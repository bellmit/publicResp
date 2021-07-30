-- liquibase formatted sql
-- changeset raeshmika:adding-new-planDetails-status-column

ALTER TABLE patient_appointment_plan_details ADD COLUMN plan_details_status varchar(10) NOT NULL default 'N';
ALTER TABLE patient_appointment_plan_details ADD COLUMN doc_dept_id varchar(10);
ALTER TABLE patient_appointment_plan_details ADD FOREIGN KEY (doc_dept_id) REFERENCES department (dept_id);