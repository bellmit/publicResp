DROP VIEW IF EXISTS apps_city_master_view;
CREATE OR REPLACE view apps_city_master_view AS
SELECT city_name,city_id,state_id,status FROM city;

DROP VIEW IF EXISTS apps_state_master_view;
CREATE OR REPLACE view apps_state_master_view AS
SELECT state_id,state_name,country_id,status FROM state_master;

DROP VIEW IF EXISTS apps_country_master_view;
CREATE OR REPLACE view apps_country_master_view AS
SELECT country_id,country_name,status,country_code FROM country_master;

DROP VIEW IF EXISTS apps_region_master_view;
CREATE OR REPLACE view apps_region_master_view AS
SELECT region_id,region_name,status FROM region_master;

DROP VIEW IF EXISTS apps_area_master_view;
CREATE OR REPLACE view apps_area_master_view AS
SELECT area_id,area_name,city_id,status FROM area_master;

DROP VIEW IF EXISTS apps_salutation_master_view;
CREATE OR REPLACE view apps_salutation_master_view AS
SELECT salutation_id,salutation,status,gender FROM salutation_master;

DROP VIEW IF EXISTS apps_patient_details_view;
CREATE OR REPLACE view apps_patient_details_view AS
SELECT mr_no,patient_name,middle_name,last_name,patient_gender,patient_address,patient_area,
patient_city,patient_state,patient_phone,salutation,country,dateofbirth,email_id,patient_category_id,mod_time
FROM patient_details;

DROP VIEW IF EXISTS apps_centre_master_view;
CREATE OR REPLACE view apps_centre_master_view AS
SELECT center_id,center_name,status,center_code,city_id,state_id,
country_id,center_address,region_id,center_contact_phone,created_timestamp,updated_timestamp
FROM hospital_center_master;

DROP VIEW IF EXISTS apps_doctor_master_view;
CREATE OR REPLACE view apps_doctor_master_view AS
SELECT doc.doctor_id,doctor_name,doctor_address,doctor_mail_id,res_phone,clinic_phone,doctor_mobile,
doctor_license_number,registration_no,doc.status,dept_id,schedule,overbook,dcm.center_id,
created_timestamp,updated_timestamp
FROM doctors doc
LEFT JOIN doctor_center_master dcm ON(doc.doctor_id=dcm.doctor_id);

DROP VIEW IF EXISTS apps_scheduler_appointments_view;
CREATE OR REPLACE view apps_scheduler_appointments_view AS
SELECT mr_no,patient_name,appointment_id,patient_contact,res_sch_id,res_sch_name,
duration,center_id,appointment_status,TO_CHAR(appointment_time,'dd-mm-yyyy') as appointment_date,
TO_CHAR(appointment_time,'HH24:MI') as appointment_time
FROM scheduler_appointments;
